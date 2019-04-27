package com.jimdo.dominicdj.midiswap.Utils;

public class StringUtil {

    /**
     * @param string the string to be searched
     * @param character the character to search for in the string
     * @return how often the character occurs in the string
     */
    public static int countCharOccurrence(String string, Character character) {
        int count = 0;
        for (Character c : string.toCharArray()) {
            if (c == character) {
                count++;
            }
        }
        return count;
    }

}
