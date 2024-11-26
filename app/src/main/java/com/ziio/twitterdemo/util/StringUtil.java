package com.ziio.twitterdemo.util;

public class StringUtil {

    public static String splitEmail(String email){
        String[] split = email.split("@");
        return split[0];
    }

}
