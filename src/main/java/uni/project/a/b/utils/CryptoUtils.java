package uni.project.a.b.utils;

import java.util.Arrays;

public class CryptoUtils {

    public static byte[][] split(byte[] source, int size) {


        byte[][] ret = new byte[(int) Math.ceil(source.length / (double) size)][size];

        int start = 0;

        for (int i = 0; i < ret.length; i++) {
            ret[i] = Arrays.copyOfRange(source, start, start + size);
            start += size;
        }

        return ret;

    }
}
