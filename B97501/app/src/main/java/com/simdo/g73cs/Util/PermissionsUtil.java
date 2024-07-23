package com.simdo.g73cs.Util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionsUtil {
    public final int mRequestCode = 100;//权限请求码
    public static boolean showSystemSetting = true;
    private Context mContext;
    private static PermissionsUtil permissionsUtil;

    private IPermissionsResult mPermissionsResult;

    private PermissionsUtil() {

    }

    public static PermissionsUtil getInstance() {
        if (permissionsUtil == null) {
            permissionsUtil = new PermissionsUtil();
        }
        return permissionsUtil;
    }

    public void checkPermissions(final Activity context, final String[] permissions, IPermissionsResult permissionsResult) {
        mPermissionsResult = permissionsResult;
        mContext = context;
        //6.0才用动态权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            permissionsResult.permitPermissions();
            return;
        }
        //unPermissionList，逐个判断哪些权限未授予，未授予的权限存储到unPermissionList中
        List<String> unPermissionList = new ArrayList<>();
        //逐个判断你要的权限是否已经通过
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(context, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(context, permissions[i])) {
                    rationaleDialog = new AlertDialog.Builder(mContext).setMessage("只有授权该权限才可以正常使用该功能")
                            .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(context, permissions, mRequestCode);
                                }
                            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    cancelPermissionDialog();
                                    mPermissionsResult.refusePermissions();
                                }
                            }).show();
                    return;
                } else {
                    unPermissionList.add(permissions[i]);//添加还未授予的权限
                }
            }
        }
        if (unPermissionList.size() > 0) {
            //申请权限
            ActivityCompat.requestPermissions(context, permissions, mRequestCode);
        } else {
            //说明权限都已经通过，可以做你想做的事情去
            permissionsResult.permitPermissions();
            return;
        }
    }
    private AlertDialog rationaleDialog;
    public void onRequestPermissionsResult(Activity context, int requestCode, String[] permissions,
                                           int[] grantResults) {
        boolean isUnauthorized = false;//有权限没有通过
        boolean isMANAGE_EXTERNAL_STORAGE = false;
        if (mRequestCode == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    AppLog.E("onRequestPermissionsResult(): i = " + i);
                    isUnauthorized = true;
                    if (permissions[i].equals(Manifest.permission.MANAGE_EXTERNAL_STORAGE)) isMANAGE_EXTERNAL_STORAGE = true;
                    break;
                }
            }
            //如果有为授权的权限，提示dialog让用户手动选择通过权限
            if (isUnauthorized) {
                if (showSystemSetting) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (isMANAGE_EXTERNAL_STORAGE && Environment.isExternalStorageManager()) mPermissionsResult.permitPermissions();
                        else showSystemPermissionsSettingDialog(context);//跳转到系统设置权限页面，或者直接关闭页面，不让他继续访问
                    }else showSystemPermissionsSettingDialog(context);//跳转到系统设置权限页面，或者直接关闭页面，不让他继续访问
                } else {
                    mPermissionsResult.refusePermissions();
                }
            } else {
                //全部权限通过，可以进行下一步操作。。。
                mPermissionsResult.permitPermissions();
            }
        }
    }

    /**
     * 进入Android 11或更高版本的文件访问权限页面
     */
    private void goManagerFileAccess(Activity activity) {
        // Android 11 (Api 30)或更高版本的写文件权限需要特殊申请，需要动态申请管理所有文件的权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent appIntent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            appIntent.setData(Uri.parse("package:" + activity.getPackageName()));
            //appIntent.setData(Uri.fromParts("package", activity.getPackageName(), null));
            try {
                activity.startActivityForResult(appIntent, 100);
            } catch (ActivityNotFoundException ex) {
                ex.printStackTrace();
                Intent allFileIntent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                activity.startActivityForResult(allFileIntent, 100);
            }
        }else {
            Uri packageURI = Uri.parse("package:" + activity.getPackageName());
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
            activity.startActivityForResult(intent, 100);
        }
    }

    /**
     * 不再提示权限时的展示对话框
     */
    AlertDialog mPermissionDialog;

    public void showSystemPermissionsSettingDialog(final Activity context) {
        final String mPackName = context.getPackageName();
        if (mPermissionDialog == null) {
            mPermissionDialog = new AlertDialog.Builder(context)
                    .setMessage("无法申请到所需权限，请手动设置允许权限")
                    .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelPermissionDialog();
                            goManagerFileAccess(context);
                            /*Uri packageURI = Uri.parse("package:" + mPackName);
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                            context.startActivity(intent);*/
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //关闭页面或者做其他操作
                            cancelPermissionDialog();
                            mPermissionsResult.refusePermissions();
                        }
                    })
                    .create();
        }
        mPermissionDialog.show();
    }
    //关闭对话框
    private void cancelPermissionDialog() {
        if (mPermissionDialog != null) {
            mPermissionDialog.cancel();
            mPermissionDialog = null;
        }
    }

    public interface IPermissionsResult {
        /**
         * 通过请求
         */
        void permitPermissions();
        /**
         * 拒绝请求
         */
        void refusePermissions();
    }
}
