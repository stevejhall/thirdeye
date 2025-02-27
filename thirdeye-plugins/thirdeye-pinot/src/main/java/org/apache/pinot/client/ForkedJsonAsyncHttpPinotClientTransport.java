/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.pinot.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO spyne remove this class once we make changes to pinot
 * This class is a fork of org.apache.pinot.client.JsonAsyncHttpPinotClientTransport and
 * adds certain capabilites which the above class fails to provide
 *
 * Originally added to customize timeout value
 */
public class ForkedJsonAsyncHttpPinotClientTransport implements PinotClientTransport {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      ForkedJsonAsyncHttpPinotClientTransport.class);
  private static final ObjectReader OBJECT_READER = new ObjectMapper().reader();

  private final Map<String, String> _headers;
  private final String _scheme;
  private final AsyncHttpClient _httpClient;
  private final int brokerResponseTimeoutMs;


  public ForkedJsonAsyncHttpPinotClientTransport(
      final Map<String, String> headers,
      final String scheme,
      final AsyncHttpClient httpClient, final int brokerResponseTimeoutMs) {
    _headers = headers;
    _scheme = scheme;
    _httpClient = httpClient;
    this.brokerResponseTimeoutMs = brokerResponseTimeoutMs;
  }

  @Override
  public BrokerResponse executeQuery(String brokerAddress, String query)
      throws PinotClientException {
    try {
      return executeQueryAsync(brokerAddress, query).get(brokerResponseTimeoutMs,
          TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      throw new PinotClientException(e);
    }
  }

  @Override
  public Future<BrokerResponse> executeQueryAsync(String brokerAddress, final String query) {
    return executeQueryAsync(brokerAddress, new Request("pql", query));
  }

  public Future<BrokerResponse> executePinotQueryAsync(String brokerAddress,
      final Request request) {
    try {
      ObjectNode json = JsonNodeFactory.instance.objectNode();
      String queryFormat = request.getQueryFormat();
      json.put(queryFormat, request.getQuery());

      final String url;
      if (queryFormat.equalsIgnoreCase("sql")) {
        url = _scheme + "://" + brokerAddress + "/query/sql";
        json.put("queryOptions", "groupByMode=sql;responseFormat=sql");
      } else {
        url = _scheme + "://" + brokerAddress + "/query";
      }

      BoundRequestBuilder requestBuilder = _httpClient.preparePost(url);

      if (_headers != null) {
        _headers.forEach((k, v) -> requestBuilder.addHeader(k, v));
      }

      final Future<Response> response =
          requestBuilder.addHeader("Content-Type", "application/json; charset=utf-8")
              .setBody(json.toString())
              .execute();

      return new BrokerResponseFuture(
          response,
          request.getQuery(),
          url,
          brokerResponseTimeoutMs);
    } catch (Exception e) {
      throw new PinotClientException(e);
    }
  }

  @Override
  public BrokerResponse executeQuery(String brokerAddress, Request request)
      throws PinotClientException {
    try {
      return executeQueryAsync(brokerAddress, request).get(brokerResponseTimeoutMs,
          TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      throw new PinotClientException(e);
    }
  }

  @Override
  public Future<BrokerResponse> executeQueryAsync(String brokerAddress, Request request)
      throws PinotClientException {
    return executePinotQueryAsync(brokerAddress, request);
  }

  @Override
  public void close()
      throws PinotClientException {
    if (_httpClient.isClosed()) {
      throw new PinotClientException("Connection is already closed!");
    }
    try {
      _httpClient.close();
    } catch (IOException exception) {
      throw new PinotClientException("Error while closing connection!");
    }
  }

  private static class BrokerResponseFuture implements Future<BrokerResponse> {

    private final Future<Response> _response;
    private final String _query;
    private final String _url;
    private final int brokerResponseTimeoutMs;

    public BrokerResponseFuture(Future<Response> response, String query, String url,
        final int brokerResponseTimeoutMs) {
      _response = response;
      _query = query;
      _url = url;
      this.brokerResponseTimeoutMs = brokerResponseTimeoutMs;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      return _response.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
      return _response.isCancelled();
    }

    @Override
    public boolean isDone() {
      return _response.isDone();
    }

    @Override
    public BrokerResponse get()
        throws ExecutionException {
      return get(brokerResponseTimeoutMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public BrokerResponse get(long timeout, TimeUnit unit)
        throws ExecutionException {
      try {
        LOGGER.debug("Sending query {} to {}", _query, _url);

        Response httpResponse = _response.get(timeout, unit);

        LOGGER.debug("Completed query, HTTP status is {}", httpResponse.getStatusCode());

        if (httpResponse.getStatusCode() != 200) {
          throw new PinotClientException(
              "Pinot returned HTTP status " + httpResponse.getStatusCode() + ", expected 200");
        }

        String responseBody = httpResponse.getResponseBody(StandardCharsets.UTF_8);
        return BrokerResponse.fromJson(OBJECT_READER.readTree(responseBody));
      } catch (Exception e) {
        throw new ExecutionException(e);
      }
    }
  }
}
