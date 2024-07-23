package com.simdo.module_freq;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.simdo.module_common.ARouterPath;

@Route(path = ARouterPath.PTAH_FREQ)
public class FreqActivity extends AppCompatActivity {
    @Autowired
    String extra = "11111";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_freq);
        ARouter.getInstance().inject(this);

        Toast.makeText(this, extra, Toast.LENGTH_SHORT).show();
    }
}