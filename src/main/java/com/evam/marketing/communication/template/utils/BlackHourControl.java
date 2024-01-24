package com.evam.marketing.communication.template.utils;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class BlackHourControl {

    public static boolean isTimeWindowOk(String startTime,String stopTime,LocalDateTime now) {
        LocalTime localTime = now.toLocalTime();
        LocalTime silentModeStartTime = LocalTime.parse(startTime);
        boolean before = silentModeStartTime.isAfter(localTime);
        LocalTime silentModeEndTime = LocalTime.parse(stopTime);
        boolean after = silentModeEndTime.isBefore(localTime);
        return before && after;
    }
}
