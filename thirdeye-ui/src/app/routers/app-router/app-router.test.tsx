import { render, screen } from "@testing-library/react";
import React from "react";
import { MemoryRouter } from "react-router-dom";
import { AppRoute } from "../../utils/routes-util/routes-util";
import { AppRouter } from "./app-router";

jest.mock("../../components/auth-provider/auth-provider.component", () => ({
    useAuth: jest.fn().mockImplementation(() => ({
        authDisabled: mockAuthDisabled,
        authenticated: mockAuthenticated,
    })),
}));

jest.mock("../alerts-router/alerts-router", () => ({
    AlertsRouter: jest.fn().mockReturnValue(<>testAlertsRouter</>),
}));

jest.mock("../anomalies-router/anomalies-router", () => ({
    AnomaliesRouter: jest.fn().mockReturnValue(<>testAnomaliesRouter</>),
}));

jest.mock("../configuration-router/configuration-router", () => ({
    ConfigurationRouter: jest
        .fn()
        .mockReturnValue(<>testConfigurationRouter</>),
}));

jest.mock(
    "../general-authenticated-router/general-authenticated-router",
    () => ({
        GeneralAuthenticatedRouter: jest
            .fn()
            .mockReturnValue(<>testGeneralAuthenticatedRouter</>),
    })
);

jest.mock(
    "../general-unauthenticated-router/general-unauthenticated-router",
    () => ({
        GeneralUnauthenticatedRouter: jest
            .fn()
            .mockReturnValue(<>testGeneralUnauthenticatedRouter</>),
    })
);

describe("App Router", () => {
    test("should direct exact alerts path to alerts router when auth disabled", async () => {
        mockAuthDisabled = true;
        render(
            <MemoryRouter initialEntries={[AppRoute.ALERTS]}>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAlertsRouter")
        ).resolves.toBeInTheDocument();
    });

    test("should direct exact alerts path to alerts router when auth enabled and authenticated", async () => {
        mockAuthDisabled = false;
        mockAuthenticated = true;
        render(
            <MemoryRouter initialEntries={[AppRoute.ALERTS]}>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAlertsRouter")
        ).resolves.toBeInTheDocument();
    });

    test("should direct alerts path to alerts router when auth disabled", async () => {
        mockAuthDisabled = true;
        render(
            <MemoryRouter initialEntries={[`${AppRoute.ALERTS}/testPath`]}>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAlertsRouter")
        ).resolves.toBeInTheDocument();
    });

    test("should direct alerts path to alerts router when auth enabled and authenticated", async () => {
        mockAuthDisabled = false;
        mockAuthenticated = true;
        render(
            <MemoryRouter initialEntries={[`${AppRoute.ALERTS}/testPath`]}>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAlertsRouter")
        ).resolves.toBeInTheDocument();
    });

    test("should direct exact anomalies path to anomalies router when auth disabled", async () => {
        mockAuthDisabled = true;
        render(
            <MemoryRouter initialEntries={[AppRoute.ANOMALIES]}>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAnomaliesRouter")
        ).resolves.toBeInTheDocument();
    });

    test("should direct exact anomalies path to anomalies router when auth enabled and authenticated", async () => {
        mockAuthDisabled = false;
        mockAuthenticated = true;
        render(
            <MemoryRouter initialEntries={[AppRoute.ANOMALIES]}>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAnomaliesRouter")
        ).resolves.toBeInTheDocument();
    });

    test("should direct anomalies path to anomalies router when auth disabled", async () => {
        mockAuthDisabled = true;
        render(
            <MemoryRouter initialEntries={[`${AppRoute.ANOMALIES}/testPath`]}>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAnomaliesRouter")
        ).resolves.toBeInTheDocument();
    });

    test("should direct anomalies path to anomalies router when auth enbled and authenticated", async () => {
        mockAuthDisabled = false;
        mockAuthenticated = true;
        render(
            <MemoryRouter initialEntries={[`${AppRoute.ANOMALIES}/testPath`]}>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAnomaliesRouter")
        ).resolves.toBeInTheDocument();
    });

    test("should direct exact configuration path to configuration router when auth disabled", async () => {
        mockAuthDisabled = false;
        render(
            <MemoryRouter initialEntries={[AppRoute.CONFIGURATION]}>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testConfigurationRouter")
        ).resolves.toBeInTheDocument();
    });

    test("should direct exact configuration path to configuration router when auth enabled and authenticated", async () => {
        mockAuthDisabled = false;
        mockAuthenticated = true;
        render(
            <MemoryRouter initialEntries={[AppRoute.CONFIGURATION]}>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testConfigurationRouter")
        ).resolves.toBeInTheDocument();
    });

    test("should direct configuration path to configuration router when auth disabled", async () => {
        mockAuthDisabled = true;
        render(
            <MemoryRouter
                initialEntries={[`${AppRoute.CONFIGURATION}/testPath`]}
            >
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testConfigurationRouter")
        ).resolves.toBeInTheDocument();
    });

    test("should direct configuration path to configuration router when auth enabled and authenticated", async () => {
        mockAuthDisabled = false;
        mockAuthenticated = true;
        render(
            <MemoryRouter
                initialEntries={[`${AppRoute.CONFIGURATION}/testPath`]}
            >
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testConfigurationRouter")
        ).resolves.toBeInTheDocument();
    });

    test("should direct any other path to general authenticated router when auth disabled", async () => {
        mockAuthDisabled = true;
        render(
            <MemoryRouter initialEntries={["/testPath"]}>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testGeneralAuthenticatedRouter")
        ).resolves.toBeInTheDocument();
    });

    test("should direct any other path to general authenticated router when auth enabled and authenticated", async () => {
        mockAuthDisabled = false;
        mockAuthenticated = true;
        render(
            <MemoryRouter initialEntries={["/testPath"]}>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testGeneralAuthenticatedRouter")
        ).resolves.toBeInTheDocument();
    });

    test("should direct to general authenticated router by default when auth disabled", async () => {
        mockAuthDisabled = true;
        render(
            <MemoryRouter>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testGeneralAuthenticatedRouter")
        ).resolves.toBeInTheDocument();
    });

    test("should direct to general authenticated router by default when auth enabled and authenticated", async () => {
        mockAuthDisabled = false;
        mockAuthenticated = true;
        render(
            <MemoryRouter>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testGeneralAuthenticatedRouter")
        ).resolves.toBeInTheDocument();
    });

    test("should direct any path to general unauthenticated router when auth enabled and not authenticated", async () => {
        mockAuthDisabled = false;
        mockAuthenticated = false;
        render(
            <MemoryRouter initialEntries={["/testPath"]}>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testGeneralUnauthenticatedRouter")
        ).resolves.toBeInTheDocument();
    });

    test("should direct to general unauthenticated router by default when auth enabled and not authenticated", async () => {
        mockAuthDisabled = false;
        mockAuthenticated = false;
        render(
            <MemoryRouter>
                <AppRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testGeneralUnauthenticatedRouter")
        ).resolves.toBeInTheDocument();
    });
});

let mockAuthDisabled = false;

let mockAuthenticated = false;
