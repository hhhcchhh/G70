package com.g50;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.Logcat.APPLog;
import com.Util.Util;
import com.g50.Bean.PaCtl;

public class LoginActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
    }

    int select = -1;
    LinearLayout ll_vehicle, ll_portable;
    CheckBox cb_login;

    private void initView() {

        ll_vehicle = findViewById(R.id.ll_vehicle);
        ll_vehicle.setOnClickListener(this);

        ll_portable = findViewById(R.id.ll_portable);
        ll_portable.setOnClickListener(this);

        cb_login = findViewById(R.id.cb_login);

        findViewById(R.id.btn_login).setOnClickListener(this);
        findViewById(R.id.tv_user_agreement).setOnClickListener(this);
        findViewById(R.id.tv_back).setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.ll_vehicle:
                select = 1;
                ll_vehicle.setBackgroundResource(R.drawable.layer_list_vehicle_bg);
                ll_portable.setBackgroundResource(R.drawable.layer_list_portable_normal_bg);
                break;
            case R.id.ll_portable:
                select = 2;
                ll_vehicle.setBackgroundResource(R.drawable.layer_list_vehicle_normal_bg);
                ll_portable.setBackgroundResource(R.drawable.layer_list_portable_bg);
                break;
            case R.id.btn_login:
//                if (select == -1) {
//                    Util.showToast(getApplicationContext(), "请先选择车载或便携作为您的设备使用");
//                    return;
//                }
//                if (!cb_login.isChecked()) {
//                    Util.showToast(getApplicationContext(), "请先仔细阅读并同意车载便携《用户协议》");
//                    return;
//                }
                APPLog.I("Login  check "+select);
                PaCtl.build().setVehicle(select == 1);
                Bundle bundle = new Bundle();
                bundle.putInt("PA_Type",0);
                if (select == 1){
                    bundle.putInt("PA_Type",2);
                }else {
                    bundle.putInt("PA_Type",0);
                }
                Intent intent = new Intent(this, G50Activity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
                break;
            case R.id.tv_user_agreement:
                showAgreementDialog();
                break;
            case R.id.tv_back:
                finish();
                break;
            default:
                break;
        }
    }

    private void showAgreementDialog() {
        final Dialog mDialog = new Dialog(this, R.style.style_dialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        mDialog.setCancelable(true);   // 返回键不消失

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_user_agreement, null);
        TextView tv_info = view.findViewById(R.id.tv_info);
        tv_info.setText("\n\t本协议系由心派用户与深圳心派科技有限公司（以下简称“心派科技”）就《G7+系列APP》所订立的相关权利义务规范。因此，请于使用此软件前，确实详细阅读本协议的所有内容，当心派用户勾选“请在进入主程序前仔细阅读并同意我们的《用户协议》”并立即登录后，即视为同意接受本协议的所有规范并愿受其约束。本协议条款心派科技有权进行修改，修改后的协议一旦公布即有效代替原来的协议。心派用户可随时查阅最新协议。 心派用户在进入本应用主程序之前，应仔细阅读本协议；如心派用户不同意本协议及 / 或对其的修改，请停止接受心派科技依据本协议提供的服务。\n" +
                "\n" +
                "\t本协议所指心派科技是指用户需要通过计算机等电子设备与心派科技供应的基带硬件设备联机才可以执行的，由心派科技所全权服务运营的可视化操控基带硬件软件《G7+系列APP》，上述可视化操控包括但不限于：一个单独的计算机程序，其中、英文名称或标题以及相关的书面文档、图片文件、影片、用户手册（载明软件程序的安装与应用方法）以及与可视化操控相关的说明书、商标、标识以及任何其他的美术品。\n" +
                "\n" +
                "\t心派用户或用户是指使用心派科技提供的G7+系列APP或基带硬件设备，并同意此协议而获得使用应用能力进而接受心派科技服务的个人。" +
                "\n" +
                "\t1. 车载、便携选择说明：\n" +
                "\tG7+系列APP 针对车载、便携使用不同的功放操作可视化基带硬件设备，因此用户在使用G7+系列APP前，需谨慎选择，若未正确选择，使用中将因功放差异导致达不到指定的功能需求效果。" +
                "\n" +
                "\t2. 知识产权声明：\n" +
                "\t物联应用《G7+系列APP》的所有相关著作权、专利权、商标、商业秘密及其它任何所有权或权利，均属心派科技或其原始授权人所有。非经心派科技或原始授权人的同意，任何人或用户均不得擅自下载、复制、传输、改作、编辑于任何为物联应用目的以外的使用、任何非为实现软件功能本身目的或任何以获利为目的的使用，否则应负所有法律责任。 物联应用或软件运营过程中产生并储存于心派科技数据库的任何数据信息（包括但不限于基带数据信息、应用数据信息等，但用户的姓名、身份证号、电话号码等个人身份数据信息除外）的所有权均属于心派科技 ，用户在按照本协议正常使用心派科技运营的物联设备或软件的过程中对属于其用户账号的数据信息享有有限使用权。" +
                "\n" +
                "\t3. 用户使用G7+系列APP的限制\n" +
                "\t3.1 用户使用心派科技的G7+系列APP，应遵守法律、本协议条款的规范，否则心派科技有权按照本协议处理。\n" +
                "\n" +
                "\t3.2 心派科技严禁用户利用心派科技提供的服务做与物联应用且与软件目的无关的行为，包括妨害他人名誉或隐私权；或使用自己、匿名或冒用他人或心派科技及其关联公司名义散播诽谤、不实、威胁、不雅、猥亵、不法、攻击性或侵害他人权利的消息或文字，传播色情或其它违反社会公德的言论；传输或散布计算机病毒；从事广告或贩卖商品；从事不法交易或张贴虚假不实或引人犯罪的信息；使用物联应用内任何传达方式来传播广告；或任何违反中华人民共和国法律或其它法令的行为，亦不得利用物联应用的机会，与其它用户或他人进行非法的交涉或对话。" +
                "\n" +
                "\t3.3 任何用户不得在物联应用或软件中实施侵害物联应用或软件公平性的行为，否则应承担的法律责任。 侵害物联应用或软件公平性的行为包括但不限于：\n" +
                "\n" +
                "\t3.3.1 利用反向工程、编译或反向编译、反汇编等技术手段制作软件对物联应用或软件进行分析、修改、攻击，最终达到作弊的目的；\n" +
                "\t3.3.2 使用任何应用修改程序，对G7+系列APP软件或心派的其他软件进行还原工程、编译或译码，包括修改软件所使用的任何专有通讯协议，或对动态随机存取内存（ RAM ）中资料进行修改或锁定；\n" +
                "\t3.3.3 传播和/或使用各种作弊软件程序，或组织、教唆他人使用此类软件程序，或销售此类软件程序而为私人或组织谋取经济利益；" +
                "\n" +
                "\t3.4 对反向工程 (Reverse Engineering) 、反向编译 (Decompilation) 、反汇编 (Disassembly) 的限制。您不得对本 “ 软件产品 ” 进行反向工程 (Reverse Engineering) 、反向编译 (Decompile) 或反汇编 (Disassemble) ，但尽管有这项限制，如适用法律明示允许上述活动，则不在此限。" +
                "\n" +
                "\t3.5. 相应措施\n" +
                "\n" +
                "\t心派科技对任何违反上述使用限制的行为有权采取一切必要措施，包括但不限于：\n" +
                "\n" +
                "\t3.5.1 有权单方面解除和发生此类行为的用户之间的协议，由此产生的后果由该用户自行承担；\n" +
                "\t3.5.2 有权采取其他任何形式的保护性措施；\n" +
                "\t3.5.3 保留向该用户提起诉讼，追索经济损失赔偿的权利；" +
                "\n" +
                "\t4. 风险承担\n" +
                "\t用户同意使用心派科技运营的各物联应用软件或其他软件是出于用户个人意愿，并愿自负任何风险及因此可能产生的损害，包括但不限于因第三方原因，在其执行操作软件或自行从非官方对接人员提供安装的G7+系列APP或资料图片而导致用户或其所使用的计算机系统损害，或发生任何资料的流失等。\n" +
                "\n" +
                "\t5．责任的限制\n" +
                "\t5.1 用户应就其在物联应用或软件中的行为或活动自负责任， 心派科技仅提供用户自行执行或与其它用户依照软件设定的方式进行工作或竞赛等。" +
                "\n" +
                "\t6. 适用法律\n" +
                "\t本协议的解释，效力及纠纷的解决，适用于中华人民共和国法律，如用户与心派科技就本协议内容或其执行发生任何争议，双方同意首先尽量友好协商；协商不成时，双方同意交由协议签署地深圳市南山区人民法院管辖并处理。\n" +
                "\n" +
                "\t7. 附则\n" +
                "\t本协议由心派科技发布，用户使用本公司提供的物联应用或软件服务，即应当遵守本协议的约定。\n" +
                "\n" +
                "\t深圳心派科技有限公司\n");
        view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                cb_login.setChecked(true);
            }
        });
        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        mDialog.setContentView(view);

        Window window = mDialog.getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);

        mDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ll_vehicle = null;
        ll_portable = null;
        cb_login = null;
    }
}
