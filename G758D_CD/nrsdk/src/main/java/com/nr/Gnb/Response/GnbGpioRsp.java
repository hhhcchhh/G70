/**
 * 基带板参数配置
 * OAM_MSG_SET_GPIO_MODE = 207
 * 此命令放在开启或结束定位之后执行
 * typedef struct {
 *      int sync_header;
 *      int msg_type;         			//UI_2_gNB_OAM_MSG
 *      int cmd_type;            		//OAM_MSG_GET_GPIO_MODE
 *      int cmd_param;
 *      int gpio_mode[EXT_GPIO_CNT]; 	//0-static_low, 1-static_high, 2-tdd(tx=low, rx=high), 3-tdd(tx=high, rx=high)
 *      int sync_footer;
 } oam_gpio_cfg_t;		//in&out
 */
package com.nr.Gnb.Response;

public class GnbGpioRsp {
	private int gpio1_en1, gpio2_en2, gpio3_bs3, gpio4_tddSw1, gpio5_bs1, gpio6_bs2, gpio_7, gpio8_tddSw2;
	private int gpio9, gpio10, gpio11, gpio12, gpio13, gpio14, gpio15, gpio16, gpio17, gpio18, gpio19, gpio20, gpio21, gpio22, gpio23, gpio24;
	private boolean isG758 = false;
	public GnbGpioRsp () {
		/*this.gpio1_en1 = gpio1_en1;         // 取值：0~1
		this.gpio2_en2 = gpio2_en2;         // 取值：0~1
		this.gpio3_bs3 = gpio3_bs3;         // 取值：0~1
		this.gpio4_tddSw1 = gpio4_tddSw1;   // 取值：0~3
		this.gpio5_bs1 = gpio5_bs1;         // 取值：0~1
		this.gpio6_bs2 = gpio6_bs2;         // 取值：0~1
		this.gpio7 = gpio7;     // 暂未用，默认：0
		this.gpio8_tddSw2 = gpio8_tddSw2;   // 取值：0~3*/
	}

	@Override
	public String toString() {
		if (isG758) return "PaBean{" +
				"gpio1=" + gpio1_en1 +
				", gpio2=" + gpio2_en2 +
				", gpio3=" + gpio3_bs3 +
				", gpio4=" + gpio4_tddSw1 +
				", gpio5=" + gpio5_bs1 +
				", gpio6=" + gpio6_bs2 +
				", gpio7=" + gpio_7 +
				", gpio8=" + gpio8_tddSw2 +
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
		return "GnbGpioRsp{" +
				"gpio1_en1=" + gpio1_en1 +
				", gpio2_en2=" + gpio2_en2 +
				", gpio3_bs3=" + gpio3_bs3 +
				", gpio4_tddSw1=" + gpio4_tddSw1 +
				", gpio5_bs1=" + gpio5_bs1 +
				", gpio6_bs2=" + gpio6_bs2 +
				", gpio_7=" + gpio_7 +
				", gpio8_tddSw2=" + gpio8_tddSw2 +
				'}';
	}

	public int getGpio1_en1() {
		return gpio1_en1;
	}

	public void setGpio1_en1(int gpio1_en1) {
		this.gpio1_en1 = gpio1_en1;
	}

	public int getGpio2_en2() {
		return gpio2_en2;
	}

	public void setGpio2_en2(int gpio2_en2) {
		this.gpio2_en2 = gpio2_en2;
	}

	public int getGpio3_bs3() {
		return gpio3_bs3;
	}

	public void setGpio3_bs3(int gpio3_bs3) {
		this.gpio3_bs3 = gpio3_bs3;
	}

	public int getGpio4_tddSw1() {
		return gpio4_tddSw1;
	}

	public void setGpio4_tddSw1(int gpio4_tddSw1) {
		this.gpio4_tddSw1 = gpio4_tddSw1;
	}

	public int getGpio5_bs1() {
		return gpio5_bs1;
	}

	public void setGpio5_bs1(int gpio5_bs1) {
		this.gpio5_bs1 = gpio5_bs1;
	}

	public int getGpio6_bs2() {
		return gpio6_bs2;
	}

	public void setGpio6_bs2(int gpio6_bs2) {
		this.gpio6_bs2 = gpio6_bs2;
	}

	public int getGpio_7() {
		return gpio_7;
	}

	public void setGpio_7(int gpio7) {
		this.gpio_7 = gpio7;
	}

	public int getGpio8_tddSw2() {
		return gpio8_tddSw2;
	}

	public void setGpio8_tddSw2(int gpio8_tddSw2) {
		this.gpio8_tddSw2 = gpio8_tddSw2;
	}

	public int getGpio9() {
		return gpio9;
	}

	public void setGpio9(int gpio9) {
		this.gpio9 = gpio9;
	}

	public int getGpio10() {
		return gpio10;
	}

	public void setGpio10(int gpio10) {
		this.gpio10 = gpio10;
	}

	public int getGpio11() {
		return gpio11;
	}

	public void setGpio11(int gpio11) {
		this.gpio11 = gpio11;
	}

	public int getGpio12() {
		return gpio12;
	}

	public void setGpio12(int gpio12) {
		this.gpio12 = gpio12;
	}

	public int getGpio13() {
		return gpio13;
	}

	public void setGpio13(int gpio13) {
		this.gpio13 = gpio13;
	}

	public int getGpio14() {
		return gpio14;
	}

	public void setGpio14(int gpio14) {
		this.gpio14 = gpio14;
	}

	public int getGpio15() {
		return gpio15;
	}

	public void setGpio15(int gpio15) {
		this.gpio15 = gpio15;
	}

	public int getGpio16() {
		return gpio16;
	}

	public void setGpio16(int gpio16) {
		this.gpio16 = gpio16;
	}

	public int getGpio17() {
		return gpio17;
	}

	public void setGpio17(int gpio17) {
		this.gpio17 = gpio17;
	}

	public int getGpio18() {
		return gpio18;
	}

	public void setGpio18(int gpio18) {
		this.gpio18 = gpio18;
	}

	public int getGpio19() {
		return gpio19;
	}

	public void setGpio19(int gpio19) {
		this.gpio19 = gpio19;
	}

	public int getGpio20() {
		return gpio20;
	}

	public void setGpio20(int gpio20) {
		this.gpio20 = gpio20;
	}

	public int getGpio21() {
		return gpio21;
	}

	public void setGpio21(int gpio21) {
		this.gpio21 = gpio21;
	}

	public int getGpio22() {
		return gpio22;
	}

	public void setGpio22(int gpio22) {
		this.gpio22 = gpio22;
	}

	public int getGpio23() {
		return gpio23;
	}

	public void setGpio23(int gpio23) {
		this.gpio23 = gpio23;
	}

	public int getGpio24() {
		return gpio24;
	}

	public void setGpio24(int gpio24) {
		this.gpio24 = gpio24;
	}

	public boolean isG758() {
		return isG758;
	}

	public void setG758(boolean g758) {
		isG758 = g758;
	}
}