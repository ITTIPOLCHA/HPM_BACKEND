package com.gj.hpm.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

// @Log4j2
public class Helper {

	public static String objectToString(Object obj) {

		return obj != null ? obj.toString() : "";
	}

	public static String fillOtpStr(int otp) {
		return String.format("%06d", otp);
	}

	public static boolean isValidEmail(String email) {
		String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

		Pattern pattern = Pattern.compile(emailRegex);
		Matcher matcher = pattern.matcher(email);

		return matcher.matches();
	}

	public static boolean isValidPassword(String password) {
		String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!#$%&'*+,-./:;<=>?@^_`{|}~])[A-Za-z\\d!#$%&'*+,-./:;<=>?@^_`{|}~]{8,}$";

		Pattern pattern = Pattern.compile(passwordRegex);
		Matcher matcher = pattern.matcher(password);

		return matcher.matches();
	}

	public static boolean isValidMobileNumber(String number) {
		String numeberRegex = "\\d+";

		Pattern pattern = Pattern.compile(numeberRegex);
		Matcher matcher = pattern.matcher(number);

		return matcher.matches();
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

	public static List<LocalDateTime> splitDates(String startDate, String endDate) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		LocalDateTime startDateTime = LocalDateTime.parse(startDate, formatter);
		LocalDateTime endDateTime = LocalDateTime.parse(endDate, formatter);

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

	public static long calculateHourDifference(LocalTime time1, LocalTime time2) {
		Duration duration = Duration.between(time1, time2);
		long hourDifference = duration.toMinutes();

		return hourDifference;
	}

	public static LocalTime findIntersectionStart(LocalDateTime startDate1, LocalDateTime endDate1,
			LocalDateTime startDate2, LocalDateTime endDate2) {
		LocalDateTime intersectionStart = startDate1.isBefore(startDate2) ? startDate2 : startDate1;
		LocalDateTime intersectionEnd = endDate1.isBefore(endDate2) ? endDate1 : endDate2;

		if (intersectionStart.isAfter(intersectionEnd)) {
			return null; // No intersection
		} else {
			return intersectionStart.toLocalTime();
		}
	}

	public static LocalTime findIntersectionEnd(LocalDateTime startDate1, LocalDateTime endDate1,
			LocalDateTime startDate2, LocalDateTime endDate2) {
		LocalDateTime intersectionStart = startDate1.isBefore(startDate2) ? startDate2 : startDate1;
		LocalDateTime intersectionEnd = endDate1.isBefore(endDate2) ? endDate1 : endDate2;

		if (intersectionStart.isAfter(intersectionEnd)) {
			return null; // No intersection
		} else {
			return intersectionEnd.toLocalTime();
		}
	}

	public static String camelcasify(String in) {
		StringBuilder sb = new StringBuilder();
		boolean capitalizeNext = false;
		char[] var3 = in.toLowerCase().toCharArray();
		int var4 = var3.length;

		for (int var5 = 0; var5 < var4; ++var5) {
			char c = var3[var5];
			if (c == '_') {
				capitalizeNext = true;
			} else if (capitalizeNext) {
				sb.append(Character.toUpperCase(c));
				capitalizeNext = false;
			} else {
				sb.append(c);
			}
		}

		return sb.toString();
	}

	public static <Any> Any getMapper(Object object, Class<Any> classMap) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return (Any) mapper.convertValue(object, classMap);
	}
}
