package com.ts.hellolocation;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "HelloLocation";

    public static final int REQUEST_CODE = 1001;

    private SharedPreferences mSharedPref;
    private static final String REFUSE_COARSE = "com.ts.hello.location.refuse.coarse";
    private static final String REFUSE_FINE = "com.ts.hello.location.refuse.fine";
    private boolean mPermissionRationaleCoarse = false;
    private boolean mPermissionRationaleFine = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (mSharedPref == null) {
            mSharedPref = getPreferences(Context.MODE_PRIVATE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 可以在这里申请权限，但是还是建议需要的时候再申请，而不是应用启动就申请
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.weather) {
            boolean coarseStatus =
                    PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION);
            if (coarseStatus) {
                Toast.makeText(this, R.string.permission_grant, Toast.LENGTH_LONG).show();
            } else {
                // APP调用一个需要权限的函数时，如果用户拒绝某授权，下一次弹框时将会有一个“禁止后不再询问”的选项，来防止APP以后继续请求授权。
                // 如果这个选项在拒绝授权前被用户勾选了，下次为这个权限请求requestPermissions时，对话框就不弹出来了，结果就是app啥都不干。
                // 遇到这种情况需要在请求requestPermissions前，检查是否需要展示请求权限的提示，
                // 这时候用的就是ActivityCompat.shouldShowRequestPermissionRationale方法。
                mPermissionRationaleCoarse = ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_COARSE_LOCATION);
                Log.d(TAG, ACCESS_COARSE_LOCATION + " grant: false, shouldShowRequestPermissionRationale :" + mPermissionRationaleCoarse);
                if (mPermissionRationaleCoarse) {
                    Toast.makeText(MainActivity.this.getApplicationContext(),
                            ACCESS_COARSE_LOCATION + this.getString(R.string.permission_deny), Toast.LENGTH_LONG).show();
                    // 注意，如果这次还是拒绝，最好的建议做法是记录下，等下次进来用户再次需要权限申请时，指导用户去手动开启
                    // 因为此时的requestPermissions已经不起作用，无法给用户弹窗，见方法letUserManualSetPermission()
                }

                if (mSharedPref != null && !mSharedPref.getBoolean(REFUSE_COARSE, false)) {
                    Log.e(TAG, "Request permission : " + ACCESS_COARSE_LOCATION);
                    // 注意，这个方法调用会导致onPause/onResume执行一次，而不会执行onStop/onStart
                    ActivityCompat.requestPermissions(this, new String[]{ACCESS_COARSE_LOCATION}, REQUEST_CODE);
                } else {
                    Log.i(TAG, "Request permission will not perform ");
                    letUserManualSetPermission();
                }
            }
        } else if (view.getId() == R.id.address) {
            boolean coarseStatus =
                    PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION);
            boolean fineStatus =
                    PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION);
            if (coarseStatus && fineStatus) {
                Toast.makeText(MainActivity.this.getApplicationContext(),
                        R.string.permission_grant, Toast.LENGTH_LONG).show();
            } else {
                // 记录没有授予的权限
                List<String> permissionRequestList = new ArrayList<>();
                if (!coarseStatus) {
                    permissionRequestList.add(ACCESS_COARSE_LOCATION);
                }
                if (!fineStatus) {
                    permissionRequestList.add(ACCESS_FINE_LOCATION);
                }

                String[] needRequestPermission = new String[permissionRequestList.size()];
                permissionRequestList.toArray(needRequestPermission);

                mPermissionRationaleCoarse = ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_COARSE_LOCATION);
                Log.d(TAG, ACCESS_COARSE_LOCATION + " grant: " + coarseStatus + ", shouldShowRequestPermissionRationale :" + mPermissionRationaleCoarse);

                mPermissionRationaleFine = ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION);
                Log.d(TAG, ACCESS_FINE_LOCATION + " grant: " + fineStatus +  ", shouldShowRequestPermissionRationale :" + mPermissionRationaleFine);
                if (mPermissionRationaleFine) {
                    Toast.makeText(MainActivity.this.getApplicationContext(),
                            ACCESS_FINE_LOCATION + this.getString(R.string.permission_deny), Toast.LENGTH_LONG).show();
                }

                if (mSharedPref != null
                         && !mSharedPref.getBoolean(REFUSE_FINE, false)) {
                    Log.e(TAG, "Request permission : " + Arrays.toString(needRequestPermission));
                    // 申请fine的权限时，如果没有授予过coarse的权限，也会自动同时申请coarse的权限，但是在onRequestPermissionsResult()回调中无法检查，请留意！
                    ActivityCompat.requestPermissions(this, needRequestPermission, REQUEST_CODE);
                    //ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, REQUEST_CODE);
                } else {
                    Log.i(TAG, "Request permission will not perform ");
                    letUserManualSetPermission();
                }


            }
        } else {
            Log.d(TAG, "oops");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Button weatherButton = findViewById(R.id.weather);
        weatherButton.setOnClickListener(this);
        Button addressButton = findViewById(R.id.address);
        addressButton.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            Log.d(TAG, "onRequestPermissionsResult permissions: " + Arrays.toString(permissions)
                    + ", grantResults: " + Arrays.toString(grantResults));
            for (int i = 0; i < permissions.length; i++) {
                if (ACCESS_COARSE_LOCATION.equals(permissions[i])) {
                    if (PackageManager.PERMISSION_DENIED == grantResults[i]
                        && mPermissionRationaleCoarse) {
                        mSharedPref.edit().putBoolean(REFUSE_COARSE, true).apply();
                        Log.d(TAG, "ACCESS_COARSE_LOCATION no need request again");
                    }
                } else if (ACCESS_FINE_LOCATION.equals(permissions[i])) {
                    if (PackageManager.PERMISSION_DENIED == grantResults[i]
                            && mPermissionRationaleFine) {
                        mSharedPref.edit().putBoolean(REFUSE_FINE, true).apply();
                        Log.d(TAG, "ACCESS_FINE_LOCATION no need request again");
                    }
                } else {
                    Log.d(TAG, "no this permission request");
                }
            }
        }
    }

    /**
     * example for go to app info.
     */
    private void letUserManualSetPermission() {
        final Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

}