/**
 * PA控制口
 */
package com.dwdbsdk.Bean;

public class PaBean {
    private int gpio1_en1, gpio2_en2, gpio3_bs3, gpio4_tddSw1, gpio5_bs1, gpio6_bs2, gpio_7, gpio8_tddSw2;
    private int gpio1, gpio2, gpio3, gpio4, gpio5, gpio6, gpio7, gpio8, gpio9, gpio10, gpio11, gpio12, gpio13, gpio14, gpio15, gpio16, gpio17, gpio18, gpio19, gpio20, gpio21, gpio22, gpio23, gpio24;
    private static PaBean instance;
    private boolean isG758 = false;

    public static PaBean build() {
        synchronized (PaBean.class) {
            if (instance == null) {
                instance = new PaBean();
            }
        }
        return instance;
    }

    public PaBean() {

    }
    public void setPaGpio (int en1, int en2, int bs3, int tddSw1, int bs1, int bs2, int gpio7, int tddSw2) {
        isG758 = false;
        this.gpio1_en1 = en1;         // 取值：0~4
        this.gpio2_en2 = en2;         // 取值：0~4
        this.gpio3_bs3 = bs3;         // 取值：0~4
        this.gpio4_tddSw1 = tddSw1;   // 取值：0~4
        this.gpio5_bs1 = bs1;         // 取值：0~4
        this.gpio6_bs2 = bs2;         // 取值：0~4
        this.gpio_7 = gpio7;     // 暂未用，默认：0~4
        this.gpio8_tddSw2 = tddSw2;   // 取值：0~4
    }

    public void setPaGpio (int gpio1, int gpio2, int gpio3, int gpio4, int gpio5, int gpio6, int gpio7, int gpio8, int gpio9, int gpio10, int gpio11, int gpio12, int gpio13, int gpio14, int gpio15, int gpio16, int gpio17, int gpio18, int gpio19, int gpio20, int gpio21, int gpio22, int gpio23, int gpio24) {
        isG758 = true;
        this.gpio1 = gpio1;         // 取值：0~6
        this.gpio2 = gpio2;         // 取值：0~6
        this.gpio3 = gpio3;         // 取值：0~6
        this.gpio4 = gpio4;         // 取值：0~6
        this.gpio5 = gpio5;         // 取值：0~6
        this.gpio6 = gpio6;         // 取值：0~6
        this.gpio7 = gpio7;         // 取值：0~6
        this.gpio8 = gpio8;         // 取值：0~6
        this.gpio9 = gpio9;         // 取值：0~6
        this.gpio10 = gpio10;         // 取值：0~6
        this.gpio11 = gpio11;         // 取值：0~6
        this.gpio12 = gpio12;         // 取值：0~6
        this.gpio13 = gpio13;         // 取值：0~6
        this.gpio14 = gpio14;         // 取值：0~6
        this.gpio15 = gpio15;         // 取值：0~6
        this.gpio16 = gpio16;         // 取值：0~6
        this.gpio17 = gpio17;         // 取值：0~6
        this.gpio18 = gpio18;         // 取值：0~6
        this.gpio19 = gpio19;         // 取值：0~6
        this.gpio20 = gpio20;         // 取值：0~6
        this.gpio21 = gpio21;         // 取值：0~6
        this.gpio22 = gpio22;         // 取值：0~6
        this.gpio23 = gpio23;         // 取值：0~6
        this.gpio24 = gpio24;         // 取值：0~6
    }

    @Override
    public String toString() {
        if (isG758) return "PaBean{" +
                "gpio1=" + gpio1 +
                ", gpio2=" + gpio2 +
                ", gpio3=" + gpio3 +
                ", gpio4=" + gpio4 +
                ", gpio5=" + gpio5 +
                ", gpio6=" + gpio6 +
                ", gpio7=" + gpio7 +
                ", gpio8=" + gpio8 +
                ", gpio9=" + gpio9 +
                ", gpio10=" + gpio10 +
                ", gpio11=" + gpio11 +
                ", gpio12=" + gpio12 +
                ", gpio13=" + gpio13 +
                ", gpio14=" + gpio14 +
                ", gpio15=" + gpio15 +
                ", gpio16=" + gpio16 +
                ", gpio17=" + gpio17 +
                ", gpio18=" + gpio18 +
                ", gpio19=" + gpio19 +
                ", gpio20=" + gpio20 +
                ", gpio21=" + gpio21 +
                ", gpio22=" + gpio22 +
                ", gpio23=" + gpio23 +
                ", gpio24=" + gpio24 +
                '}';
        return "PaBean{" +
                "gpio1(en1)=" + gpio1_en1 +
                ", gpio2(en2)=" + gpio2_en2 +
                ", gpio3(bs3)=" + gpio3_bs3 +
                ", gpio4(tddSw1)=" + gpio4_tddSw1 +
                ", gpio5(bs1)=" + gpio5_bs1 +
                ", gpio6(bs2)=" + gpio6_bs2 +
                ", gpio7=" + gpio_7 +
                ", gpio8(tddSw2)=" + gpio8_tddSw2 +
                '}';
    }

    public int getGpio1_en1() {
        return gpio1_en1;
    }

    public int getGpio2_en2() {
        return gpio2_en2;
    }

    public int getGpio3_bs3() {
        return gpio3_bs3;
    }

    public int getGpio4_tddSw1() {
        return gpio4_tddSw1;
    }

    public int getGpio5_bs1() {
        return gpio5_bs1;
    }

    public int getGpio6_bs2() {
        return gpio6_bs2;
    }

    public int getGpio_7() {
        return gpio_7;
    }

    public int getGpio8_tddSw2() {
        return gpio8_tddSw2;
    }

    public int getGpio1() {
        return gpio1;
    }

    public int getGpio2() {
        return gpio2;
    }

    public int getGpio3() {
        return gpio3;
    }

    public int getGpio4() {
        return gpio4;
    }

    public int getGpio5() {
        return gpio5;
    }

    public int getGpio6() {
        return gpio6;
    }

    public int getGpio7() {
        return gpio7;
    }

    public int getGpio8() {
        return gpio8;
    }

    public int getGpio9() {
        return gpio9;
    }

    public int getGpio10() {
        return gpio10;
    }

    public int getGpio11() {
        return gpio11;
    }

    public int getGpio12() {
        return gpio12;
    }

    public int getGpio13() {
        return gpio13;
    }

    public int getGpio14() {
        return gpio14;
    }

    public int getGpio15() {
        return gpio15;
    }

    public int getGpio16() {
        return gpio16;
    }

    public int getGpio17() {
        return gpio17;
    }

    public int getGpio18() {
        return gpio18;
    }

    public int getGpio19() {
        return gpio19;
    }

    public int getGpio20() {
        return gpio20;
    }

    public int getGpio21() {
        return gpio21;
    }

    public int getGpio22() {
        return gpio22;
    }

    public int getGpio23() {
        return gpio23;
    }

    public int getGpio24() {
        return gpio24;
    }
}
