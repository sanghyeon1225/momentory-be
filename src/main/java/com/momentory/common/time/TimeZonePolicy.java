package com.momentory.common.time;

import java.time.ZoneId;

public final class TimeZonePolicy {

    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Asia/Seoul");
    public static final String DEFAULT_TIME_ZONE_ID = DEFAULT_ZONE_ID.getId();

    private TimeZonePolicy() {
    }
}
