/**
 * 5G频点算BADN
 */
package com.dwdbsdk.Bean;

public class NrBand {
	//direction: 0:DL;1:UL
	public static int earfcn2band(int earfcn) {
	    int m = 0;
	    for (int i = 0; i < NMM_NUM_RADIO_BAND; i++) {
	        if ((earfcn >= nmm_earfcn_to_freq_lut[i][m + 3])
	                && (earfcn <= nmm_earfcn_to_freq_lut[i][m + 4])) {
	            return nmm_earfcn_to_freq_lut[i][5];
	        }
	    }
	    return 0;
	}
	public static int earfcn2band(int earfcn, boolean isFDD) {
		int m = 0;
		for (int i = 0; i < NMM_NUM_RADIO_BAND; i++) {
			if ((earfcn >= nmm_earfcn_to_freq_lut[i][m + 3])
					&& (earfcn <= nmm_earfcn_to_freq_lut[i][m + 4])) {
				if (isFDD) {
					if (nmm_earfcn_to_freq_lut[i][1] == nmm_earfcn_to_freq_lut[i][3]) continue;
				}else {
					if (nmm_earfcn_to_freq_lut[i][1] != nmm_earfcn_to_freq_lut[i][3]) continue;
				}
				return nmm_earfcn_to_freq_lut[i][5];
			}
		}
		return 0;
	}
	public static final int NMM_NUM_RADIO_BAND = 25;
	private static final int[][] nmm_earfcn_to_freq_lut = {
	/* F_Raster [kHz], min UL, max UL, min DL, max DL */
		{ 100, 384000, 396000, 422000, 434000, 1 },
		{ 100, 370000, 382000, 386000, 398000, 2 },
		{ 100, 342000, 357000, 361000, 376000, 3 },
		{ 100, 164800, 169800, 173800, 178800, 5 },
		{ 100, 176000, 183000, 185000, 192000, 8 },
		{ 100, 139800, 143200, 145800, 149200, 12 },
		{ 100, 166400, 172400, 158200, 164200, 20 },
		{ 100, 370000, 383000, 386000, 399000, 25 },
		{ 100, 140600, 149600, 151600, 160600, 28 },
		{ 100, 402000, 405000, 402000, 405000, 34 },
		//{ 100, 514000, 524000, 514000, 524000, 38 },
		{ 100, 376000, 384000, 376000, 384000, 39 },
		{ 100, 460000, 480000, 460000, 480000, 40 },
		{ 30, 499200, 537999, 499200, 537999, 41 },
		{ 100, 500000, 514000, 524000, 538000, 7 },
		{ 100, 286400, 303400, 286400, 303400, 50 },
		{ 100, 285400, 286400, 285400, 286400, 51 },
		{ 100, 342000, 356000, 422000, 440000, 66 },
		{ 100, 339000, 342000, 399000, 404000, 70 },
		{ 100, 132600, 139600, 123400, 130400, 71 },
		{ 100, 285400, 294000, 295000, 303600, 74 },
		{ 100, 0, 0, 286400, 303400, 75 },
		{ 100, 0, 0, 285400, 286400, 76 },
		{ 30, 620000, 653333, 620000, 653333, 78 },
		{ 30, 620000, 680000, 620000, 680000, 77 },
		{ 30, 693334, 733333, 693334, 733333, 79 }
		/*{ 100, 384000, 396000, 422000, 434000, 80 },
		{ 100, 384000, 396000, 422000, 434000, 81 },
		{ 100, 384000, 396000, 422000, 434000, 82 },
		{ 100, 384000, 396000, 422000, 434000, 83 },
		{ 100, 384000, 396000, 422000, 434000, 84 },
		{ 100, 384000, 396000, 422000, 434000, 86 }*/
	};
}
