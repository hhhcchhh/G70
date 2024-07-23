package com.nr.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class MD5 {

    private MD5() {
    }

    private static final char hexDigits[] =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String toHexString(byte[] bytes) {
        if (bytes == null) return "";
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(hexDigits[(b >> 4) & 0x0F]);
            hex.append(hexDigits[b & 0x0F]);
        }
        return hex.toString();
    }

    public static String md5(File file) throws IOException {
        MessageDigest messagedigest = null;
        FileInputStream in = null;
        FileChannel ch = null;
        byte[] encodeBytes = null;
        try {
            messagedigest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            ch = in.getChannel();
            MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            messagedigest.update(byteBuffer);
            encodeBytes = messagedigest.digest();
        } catch (NoSuchAlgorithmException neverHappened) {
            throw new RuntimeException(neverHappened);
        } finally {
            IOUtil.closeQuietly(in);
            IOUtil.closeQuietly(ch);
        }

        return toHexString(encodeBytes);
    }

    public static String md5(String string) {
        byte[] encodeBytes = null;
        try {
            encodeBytes = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException neverHappened) {
            throw new RuntimeException(neverHappened);
        } catch (UnsupportedEncodingException neverHappened) {
            throw new RuntimeException(neverHappened);
        }

        return toHexString(encodeBytes);
    }

    public static boolean a(int s, int u, int a, String p){
        // 设置授权时间，密码糊弄
        // 上层传值很简单，这里也做简单糊弄
        boolean e = false;
        String b = s + "sim";
        char o = '9';
        int l = p.length();
        String f = String.valueOf(u);
        String c = u + "pie";
        char q = '0';
        char d = p.charAt(l - 1);
        int n = f.length();
        // 没必要搞那么复杂，简单糊弄点就好，第一条件始终为true
        if ((b + c != d + p) && (d < q || d > o)) return e;
        String w = String.valueOf(d);
        if (n > 9) return e;
        int m = Integer.parseInt(w);
        // 上面全是糊弄屁，实际作用也就这里
        if (m != n) return e;
        // 下面也全是糊弄屁
        String x = String.valueOf(a);
        if (x.equals(p)) return e;
        return true;
    }
}
