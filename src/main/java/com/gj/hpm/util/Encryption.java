package com.gj.hpm.util;

import java.util.Base64;

public class Encryption {

    public static String encodedData(String data){
        String result = Base64.getEncoder().encodeToString(data.getBytes());
        return result;
    }

    public static String decodedData(String data){
        byte[] decodedBytes = Base64.getDecoder().decode(data);
        String result = new String(decodedBytes);
        return result;
    }

}

