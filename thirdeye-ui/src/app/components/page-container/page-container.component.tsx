import { Toolbar } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { AppBreadcrumbs } from "../app-breadcrumbs/app-breadcrumbs.component";
import { PageContainerProps } from "./page-container.interfaces";
import { usePageContainerStyles } from "./page-container.styles";

export const PageContainer: FunctionComponent<PageContainerProps> = (
    props: PageContainerProps
) => {
    const pageContainerClasses = usePageContainerStyles();

    return (
        <div className={pageContainerClasses.outerContainer}>
            {/* Required to clip the subsequent container under app bar */}
            <Toolbar />

            {/* App breadcrumbs */}
            <AppBreadcrumbs />

            {/* Contents */}
            <div className={pageContainerClasses.innerContainer}>
                {props.children}
            </div>
        </div>
    );
};
