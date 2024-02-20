package com.gj.hpm.util;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DateTimeUtil {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter DATE_FORMATTER_YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static LocalDate parseDate(String dateStr) {
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
    }

    public static LocalDateTime parseDateTime(String dateTimeStr, DateTimeFormatter dateTimeFormatter) {
        return LocalDateTime.parse(dateTimeStr, dateTimeFormatter);
    }

    public static LocalTime parseTime(String timeStr) {
        return LocalTime.parse(timeStr, TIME_FORMATTER);
    }

    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    public static String formatDateTime(LocalDateTime dateTime, DateTimeFormatter dateTimeFormatter) {
        return dateTime.format(dateTimeFormatter);
    }

    public static String formatTime(LocalTime time) {
        return time.format(TIME_FORMATTER);
    }

    public static LocalDateTime resetTimeToMidnight(LocalDateTime time) {
        return time.with(LocalTime.MIDNIGHT);
    }

    public static LocalDateTime copy(LocalDateTime obj1) {
        return LocalDateTime.of(obj1.getYear(), obj1.getMonth(), obj1.getDayOfMonth(), obj1.getHour(), obj1.getMinute(),
                obj1.getSecond());
    }

    public static Date convertToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static List<LocalDateTime> splitDates(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<LocalDateTime> dateTimeList = new ArrayList<>();

        LocalDateTime currentDateTime = startDateTime;
        while (!currentDateTime.isAfter(endDateTime)) {
            dateTimeList.add(currentDateTime);
            currentDateTime = currentDateTime.plusDays(1);
        }

        if (!dateTimeList.contains(endDateTime)) {
            dateTimeList.add(endDateTime);
        }

        return dateTimeList;
    }

    public static boolean isSameDay(LocalDateTime startDate, LocalDateTime endDate) {
        LocalDate startDay = startDate.toLocalDate();
        LocalDate endDay = endDate.toLocalDate();

        return startDay.isEqual(endDay);
    }

    public static long calculateMinuteDifference(LocalTime time1, LocalTime time2) {
        Duration duration = Duration.between(time1, time2);
        long minuteDifference = duration.toMinutes();

        return minuteDifference;
    }

    public static long calculateIntersectionMinutes(LocalTime start1, LocalTime end1, LocalTime start2,
            LocalTime end2) {
        LocalTime intersectionStart = start1.isAfter(start2) ? start1 : start2;
        LocalTime intersectionEnd = end1.isBefore(end2) ? end1 : end2;

        long intersectionMinutes = 0;

        if (intersectionStart.isBefore(intersectionEnd) || intersectionStart.equals(intersectionEnd)) {
            intersectionMinutes = intersectionStart.until(intersectionEnd, java.time.temporal.ChronoUnit.MINUTES);
        }

        return intersectionMinutes;
    }

    public static LocalDateTime convertDate(Date utilDate) {

        // Step 1: Convert java.util.Date to java.time.Instant
        Instant instant = utilDate.toInstant();

        // Step 2: Convert java.time.Instant to java.time.LocalDateTime
        ZoneId zoneId = ZoneId.systemDefault(); // Change this to the desired time zone if needed
        LocalDateTime localDateTime = instant.atZone(zoneId).toLocalDateTime();

        return localDateTime;
    }
}
