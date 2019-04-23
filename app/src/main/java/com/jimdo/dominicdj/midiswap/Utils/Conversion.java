package com.jimdo.dominicdj.midiswap.Utils;

public class Conversion {

    // http://www.java2s.com/Code/Java/Data-Type/hexStringToByteArray.htm
    public static byte[] toByteArray(String hexString) {
        byte[] b = new byte[hexString.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(hexString.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

    // https://stackoverflow.com/questions/2817752/java-code-to-convert-byte-to-hexadecimal
    public static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b)); // print at least 2 digits, prepend it with 0's if there's less
        }
        return sb.toString();
    }

    // https://stackoverflow.com/questions/6430841/java-byte-to-byte
    // Byte[] to byte[]
    public static byte[] toPrimitives(Byte[] oBytes) {
        byte[] bytes = new byte[oBytes.length];

        for (int i = 0; i < oBytes.length; i++) {
            bytes[i] = oBytes[i];
        }
        return bytes;
    }

    // https://stackoverflow.com/questions/6430841/java-byte-to-byte
    // byte[]to Byte[]
    public static Byte[] toObjects(byte[] bytesPrim) {
        Byte[] bytes = new Byte[bytesPrim.length];

        int i = 0;
        for (byte b : bytesPrim) {
            bytes[i++] = b;
        }
        return bytes;
    }

}
