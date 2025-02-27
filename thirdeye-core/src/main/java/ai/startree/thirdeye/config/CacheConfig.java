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
package ai.startree.thirdeye.config;

/**
 * Config file for cache-related stuff.
 * Mapped from cache-config.yml
 */
@Deprecated  // todo cache needs reimplementation with v2 query system - kept to not break config yaml - can be removed
public class CacheConfig {

  private boolean useInMemoryCache = true;
  private boolean useCentralizedCache = false;

  public boolean useCentralizedCache() {
    return useCentralizedCache;
  }

  public boolean useInMemoryCache() {
    return useInMemoryCache;
  }

  public CacheConfig setUseCentralizedCache(boolean useCentralizedCache) {
    this.useCentralizedCache = useCentralizedCache;
    return this;
  }

  public CacheConfig setUseInMemoryCache(boolean useInMemoryCache) {
    this.useInMemoryCache = useInMemoryCache;
    return this;
  }
}
