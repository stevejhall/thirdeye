import { Grid, Typography } from "@material-ui/core";
import classnames from "classnames";
import React, { FunctionComponent, useEffect } from "react";
import { useLocation } from "react-router-dom";
import { useAppTimeRangeStore } from "../../store/app-time-range-store/app-time-range-store";
import { getTimeRangeFromQueryString } from "../../utils/params-util/params-util";
import { isTimeRangeDurationEqual } from "../../utils/time-range-util/time-range-util";
import { TimeRangeSelector } from "../time-range-selector/time-range-selector.component";
import { PageContentsProps } from "./page-contents.interfaces";
import { usePageContentsStyles } from "./page-contents.style";

export const PageContents: FunctionComponent<PageContentsProps> = (
    props: PageContentsProps
) => {
    const pageContentsClasses = usePageContentsStyles();
    const [
        appTimeRangeDuration,
        recentCustomTimeRangeDurations,
        setAppTimeRangeDuration,
        getAppTimeRangeDuration,
    ] = useAppTimeRangeStore((state) => [
        state.appTimeRangeDuration,
        state.recentCustomTimeRangeDurations,
        state.setAppTimeRangeDuration,
        state.getAppTimeRangeDuration,
    ]);
    const location = useLocation();

    useEffect(() => {
        // Query string changed, set time range from time range query string if available
        const timeRageDuration = getTimeRangeFromQueryString();
        if (
            !timeRageDuration ||
            isTimeRangeDurationEqual(timeRageDuration, appTimeRangeDuration)
        ) {
            return;
        }

        setAppTimeRangeDuration(timeRageDuration);
    }, [location.search]);

    return (
        <main
            className={classnames(
                pageContentsClasses.container,
                props.contentsCenterAlign
                    ? pageContentsClasses.containerCenterAlign
                    : pageContentsClasses.containerExpand
            )}
        >
            <Grid container direction="column">
                {/* Header, only if title is provided and/or time range is to be displayed */}
                {(props.title || !props.hideTimeRange) && (
                    <Grid
                        container
                        item
                        alignItems="center"
                        className={pageContentsClasses.header}
                        justify="space-between"
                    >
                        {/* Title */}
                        <Grid
                            item
                            className={
                                props.titleCenterAlign
                                    ? pageContentsClasses.titleCenterAlign
                                    : ""
                            }
                        >
                            <Typography variant="h5">{props.title}</Typography>
                        </Grid>

                        {/* Time range selector */}
                        {!props.hideTimeRange && (
                            <Grid item>
                                <TimeRangeSelector
                                    getTimeRangeDuration={
                                        getAppTimeRangeDuration
                                    }
                                    recentCustomTimeRangeDurations={
                                        recentCustomTimeRangeDurations
                                    }
                                    timeRangeDuration={appTimeRangeDuration}
                                    onChange={setAppTimeRangeDuration}
                                />
                            </Grid>
                        )}
                    </Grid>
                )}

                {/* Contents */}
                <Grid item>
                    {/* Include children */}
                    {props.children}
                </Grid>
            </Grid>
        </main>
    );
};
