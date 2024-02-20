package com.gj.hpm.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThaiIDCardValidator {

    // เช็ครูปแบบของหมายเลขบัตรประชาชน
    private static final String ID_CARD_PATTERN = "\\d{13}";

    public static boolean isValidThaiIDCard(String idCard) {
        // ตรวจสอบว่าเป็นตัวเลข 13 หลักหรือไม่
        Pattern pattern = Pattern.compile(ID_CARD_PATTERN);
        Matcher matcher = pattern.matcher(idCard);

        if (!matcher.matches()) {
            return false;
        }

        // ตรวจสอบว่าเป็นหมายเลขบัตรประชาชนที่ถูกต้อง
        return isValidChecksum(idCard);
    }

    private static boolean isValidChecksum(String idCard) {
        // คำนวณ Checksum
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += Character.getNumericValue(idCard.charAt(i)) * (13 - i);
        }

        int checksum = sum % 11;
        checksum = checksum <= 1 ? 1 - checksum : 11 - checksum;

        // เปรียบเทียบ Checksum ที่คำนวณได้กับ Checksum ที่อยู่ในหลักที่ 13
        return checksum == Character.getNumericValue(idCard.charAt(12));
    }

    public static void main(String[] args) {
        String idCard = "1234567890123"; // ใส่หมายเลขบัตรประชาชนที่ต้องการตรวจสอบ
        if (isValidThaiIDCard(idCard)) {
            System.out.println("Card ID is correct");
        } else {
            System.out.println("Card ID is wrong");
        }
    }
}
