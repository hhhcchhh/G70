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
package com.dwdbsdk.Response.DW;

public class GnbGpioRsp {
	private int gpio1_en1, gpio2_en2, gpio3_bs3, gpio4_tddSw1, gpio5_bs1, gpio6_bs2, gpio7, gpio8_tddSw2;

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
		return "GnbGpioRsp{" +
				"gpio1_en1=" + gpio1_en1 +
				", gpio2_en2=" + gpio2_en2 +
				", gpio3_bs3=" + gpio3_bs3 +
				", gpio4_tddSw1=" + gpio4_tddSw1 +
				", gpio5_bs1=" + gpio5_bs1 +
				", gpio6_bs2=" + gpio6_bs2 +
				", gpio7=" + gpio7 +
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

	public int getGpio7() {
		return gpio7;
	}

	public void setGpio7(int gpio7) {
		this.gpio7 = gpio7;
	}

	public int getGpio8_tddSw2() {
		return gpio8_tddSw2;
	}

	public void setGpio8_tddSw2(int gpio8_tddSw2) {
		this.gpio8_tddSw2 = gpio8_tddSw2;
	}
}
