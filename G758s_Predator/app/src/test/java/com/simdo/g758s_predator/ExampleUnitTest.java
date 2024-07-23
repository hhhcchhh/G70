package com.simdo.g758s_predator;

import android.text.TextUtils;

import com.dwdbsdk.Bean.NrBand;
import com.dwdbsdk.MessageControl.MessageController;
import com.simdo.g758s_predator.Util.TraceUtil;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
        System.out.println(NrBand.earfcn2band(528522));
    }
}
