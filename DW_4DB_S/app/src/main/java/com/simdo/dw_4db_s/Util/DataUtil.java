package com.simdo.dw_4db_s.Util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataUtil {
    //获取无符号字节
    public static short storeUnsignedByte(byte[] data) {
        short port;
        int firstbyte;
        firstbyte = (0x000000FF & (int) data[0]);
        port = (short) firstbyte;
        return port;
    }

    //获取无符号短整形
    public static char storeUnsignedShort(int[] data) {
        char port;
        int firstbyte;
        int secondbyte;
        firstbyte = (0x000000FF & data[0]);
        secondbyte = (0x000000FF & data[1]);
        port = (char) (firstbyte << 8 | secondbyte);
        return port;
    }

    //获取无符号整型
    public static long storeUnsignedIntOrLong(int[] data) {
        long port = 0;
        if (data.length == 4) {
            int firstbyte;
            int secondbyte;
            int thirdbyte;
            int fourthbyte;
            firstbyte = (0x000000FF & (int) data[0]);
            secondbyte = (0x000000FF & (int) data[1]);
            thirdbyte = (0x000000FF & (int) data[2]);
            fourthbyte = (0x000000FF & (int) data[3]);
            port = (long) (firstbyte << 24 | secondbyte << 16 | thirdbyte << 8 | fourthbyte) & 0xFFFFFFFFL;
        } else if (data.length == 5) {
            long firstbyte;
            long secondbyte;
            long thirdbyte;
            long fourthbyte;
            long fiftthbyte;
            firstbyte = (0x00000000FF & data[0]);
            secondbyte = (0x00000000FF & data[1]);
            thirdbyte = (0x00000000FF & data[2]);
            fourthbyte = (0x00000000FF & data[3]);
            fiftthbyte = (0x00000000FF & data[4]);
            port = (firstbyte << 32 | secondbyte << 24 | thirdbyte << 16 | fourthbyte << 8 | fiftthbyte) & 0x000000FFFFFFFFFFL;

        } else if (data.length == 8) {
            long firstbyte;
            long secondbyte;
            long thirdbyte;
            long fourthbyte;
            long fiftthbyte;
            long sixthbyte;
            long seventhbyte;
            long eighthbyte;
            firstbyte = (0x00000000000000FF & (long) data[0]);
            secondbyte = (0x00000000000000FF & (long) data[1]);
            thirdbyte = (0x00000000000000FF & (long) data[2]);
            fourthbyte = (0x00000000000000FF & (long) data[3]);
            fiftthbyte = (0x00000000000000FF & (long) data[4]);
            sixthbyte = (0x00000000000000FF & (long) data[5]);
            seventhbyte = (0x00000000000000FF & (long) data[6]);
            eighthbyte = (0x00000000000000FF & (long) data[7]);
            port = (long) (firstbyte << 56 | secondbyte << 48 | thirdbyte << 40 | fourthbyte << 32 | fiftthbyte << 24 | sixthbyte << 16 | seventhbyte << 8 | eighthbyte) & 0xFFFFFFFFFFFFFFFFL;
        }
        return port;
    }

    //char转byte[]
    public static byte[] charToBytes(char value) {
        byte[] src = new byte[2];
        src[0] = (byte) ((value >> 8) & 0xFF);
        src[1] = (byte) (value & 0xFF);
        return src;
    }

    //tag数据解析
    public static byte[] tagparser(byte[] bytes) {
        System.out.println("tagparser");
        byte lenth1, lenth2;
        lenth1 = (byte) (bytes[10] & 0xff);
        lenth2 = (byte) (bytes[11] & 0xff);
        short taglenth = (short) (lenth1 << 8 | lenth2);
        byte[] target = new byte[taglenth];

        System.out.println(target.length + "taglenth");
        System.out.println(target[0] + "target[0]dezhi");
        System.arraycopy(bytes, 9, target, 0, taglenth);
        if (taglenth == 1) {
            System.out.println(target[0] + ",/////1");
        } else {
            for (int i = 0; i < target.length; i++) {
                System.out.println(target[i] + ",/////");
            }
        }
        return target;
    }

    public static int bytetoint(byte tb) {
        int temp;
        temp = tb;
        if (temp < 0) {
            temp = temp & 0x7F + 128;
        }
        return temp;
    }

    public static long byteArrayToLong(byte[] byteArray) {
        long n = 0;
        int[] intArray = new int[byteArray.length];

        for (int i = 0; i < byteArray.length; i++) {
            intArray[i] = bytetoint(byteArray[i]);
        }
        n = storeUnsignedIntOrLong(intArray);

        return n;
    }

    public static char byteArrayToInt(byte[] byteArray) {
        char n = 0;
        int[] intArray = new int[2];

        for (int i = 0; i < intArray.length; i++) {
            intArray[i] = bytetoint(byteArray[i]);
        }

        n = storeUnsignedShort(intArray);
        return n;
    }

    public static byte[] long2Bytes(long num) {
        byte[] byteNum = new byte[8];
        for (int ix = 0; ix < 8; ++ix) {
            int offset = 64 - (ix + 1) * 8;
            byteNum[ix] = (byte) ((num >> offset) & 0xff);
        }
        return byteNum;
    }

    /**
     * 将int数值转换为占四个字节的byte数组，本方法适用于(低位在前，高位在后)的顺序。 和bytesToInt（）配套使用
     *
     * @param value 要转换的int值
     * @return byte数组
     */
    public static byte[] intToBytes(int value) {
        byte[] src = new byte[4];
        src[3] = (byte) ((value >> 24) & 0xFF);
        src[2] = (byte) ((value >> 16) & 0xFF);
        src[1] = (byte) ((value >> 8) & 0xFF);
        src[0] = (byte) (value & 0xFF);
        return src;
    }

    /**
     * 将int数值转换为占四个字节的byte数组，本方法适用于(高位在前，低位在后)的顺序。网络字节序  和bytesToInt2（）配套使用
     */
    public static byte[] intToBytes2(int value) {
        byte[] src = new byte[4];
        src[0] = (byte) ((value >> 24) & 0xFF);
        src[1] = (byte) ((value >> 16) & 0xFF);
        src[2] = (byte) ((value >> 8) & 0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }
    //char转byte[] (低位在前，高位在后)
    public static byte[] shortToBytes(short value) {
        byte[] src = new byte[2];
        src[1] = (byte) ((value >> 8) & 0xFF);
        src[0] = (byte) (value & 0xFF);
        return src;
    }
    //char转byte[] (高位在前，低位在后)
    public static byte[] shortToBytes2(short value) {
        byte[] src = new byte[2];
        src[0] = (byte) ((value >> 8) & 0xFF);
        src[1] = (byte) (value & 0xFF);
        return src;
    }
    public static byte[] intToBytes3(int value) {
        return new byte[]{(byte) ((value >> 8) & 0xFF), (byte) (value & 0xFF)};
    }

    /**
     * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序，和和intToBytes（）配套使用
     *
     * @param src    byte数组
     * @param offset 从数组的第offset位开始
     * @return int数值
     */
    public static int bytesToInt(byte[] src, int offset) {
        int value;
        value = (int) ((src[offset] & 0xFF)
                | ((src[offset + 1] & 0xFF) << 8)
                | ((src[offset + 2] & 0xFF) << 16)
                | ((src[offset + 3] & 0xFF) << 24));
        return value;
    }

    /**
     * byte数组中取int数值，本方法适用于(低位在后，高位在前)的顺序。网络字节序     和intToBytes2（）配套使用
     */
    public static int bytesToInt2(byte[] src) {
        int value;
        value = (((src[0] & 0xFF) << 24)
                | ((src[1] & 0xFF) << 16)
                | ((src[2] & 0xFF) << 8)
                | (src[3] & 0xFF));
        return value;
    }

    /**
     * byte数组中取int数值，本方法适用于(低位在后，高位在前)的顺序。网络字节序     和intToBytes2（）配套使用
     */
    public static int bytesToInt2(byte[] src, int offset) {
        int value;
        value = (int) (((src[offset] & 0xFF) << 24)
                | ((src[offset + 1] & 0xFF) << 16)
                | ((src[offset + 2] & 0xFF) << 8)
                | (src[offset + 3] & 0xFF));
        return value;
    }

    /**
     * byte数组中取int数值，本方法适用于(低位在后，高位在前)的顺序。网络字节序     和intToBytes3（）配套使用
     */
    public static int bytesToInt3(byte[] src) {
        return src[1] & 0xFF |
                (src[0] & 0xFF) << 8;
    }

    /**
     * 注释：字节数组到short的转换！
     */
    public static short byteToShort(byte[] b) {
        return (short) (b[1] & 0xff | (b[0] & 0xff) << 8);
    }
    /**
     * Asc2String
     * @param commond
     * @return
     */
    public static String Asc2String(byte[] commond) {
        String str = null;
        StringBuilder temp = new StringBuilder();

        for (int item : commond) {
            char ch = (char) item;
            temp.append(ch);
        }
        str = temp.toString();
        return str;
    }
    /**
     * String2Asc
     * @param value
     * @return
     */
    public static String String2Asc(String value) {
        StringBuffer sbu = new StringBuffer();
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i != chars.length - 1) {
                sbu.append((int) chars[i]).append(",");
            } else {
                sbu.append((int) chars[i]);
            }
        }
        return sbu.toString();
    }
    /**
     * radix : 2\8\10\16进制数
     *
     * return num
     * */
    public static int StringToInt(String s, int radix) {
        try {
            int num = Integer.parseInt(s, radix);// 把字符串强制转换为数字
            return num;
        } catch (Exception e) {
            return -1;
        }
    }
    /**
     * 转换成16进制
     *
     * @param paramArrayOfByte
     * @return
     */
    public static String byteToHex(byte[] paramArrayOfByte) {
        StringBuilder localStringBuilder = new StringBuilder();
        int j = paramArrayOfByte.length;
        int i = 0;
        for (;;) {
            if (i >= j) {
                return localStringBuilder.toString();
            }
            localStringBuilder.append(String.format("%02x", new Object[] { Integer.valueOf(paramArrayOfByte[i] & 0xFF) }));
            i += 1;
        }
    }
    /**
     * 可以通过修改正则表达式实现校验负数， 将正则表达式修改为“^-?[0-9]+”即可， 修改为“-?[0-9]+.?[0-9]+”即可匹配所有数字
     *
     * 使用正则表达式判断
     *
     * String str = ""; boolean isNum = str.matches("[0-9]+"); +:
     * 表示1个或多个（[0-9]+）（如"3"或"225"), *: 表示0个或多个（[0-9]*）（如""或"1"或"22"）， ?:
     * 表示0个或1个([0-9]?)(如""或"7")
     **/
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }
}
