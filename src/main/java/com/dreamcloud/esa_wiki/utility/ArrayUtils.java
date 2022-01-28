package com.dreamcloud.esa_wiki.utility;

public class ArrayUtils {
    public static boolean tooShort(String[] array, int minLength) {
        return array == null || array.length < minLength;
    }
}
