/*
 * Tencent is pleased to support the open source community by making Tinker available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tinker.sample.android.app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.tencent.tinker.lib.library.TinkerLoadLibrary;
import com.tencent.tinker.lib.listener.DefaultPatchListener;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.tencent.tinker.loader.shareutil.SharePatchFileUtil;
import com.tencent.tinker.loader.shareutil.ShareTinkerInternals;

import java.io.File;

import tinker.sample.android.R;
import tinker.sample.android.util.Utils;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Tinker.MainActivity";

    private TextView mTvMessage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean isARKHotRunning = ShareTinkerInternals.isArkHotRuning();
        Log.e(TAG, "ARK HOT Running status = " + isARKHotRunning);
        Log.e(TAG, "i am on onCreate classloader:" + MainActivity.class.getClassLoader().toString());
        //test resource change
        Log.e(TAG, "i am on onCreate string:" + getResources().getString(R.string.test_resource));
        Log.e(TAG, "i am on patch onCreate16");

        mTvMessage = findViewById(R.id.tv_message);

        askForRequiredPermissions();

        Button loadPatchButton = (Button) findViewById(R.id.loadPatch);

        loadPatchButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                TinkerInstaller.onReceiveUpgradePatch(getApplicationContext(), Environment.getExternalStorageDirectory().getAbsolutePath() + "/patch_signed_7zip.apk");
//            }

            @Override
            public void onClick(View v) {
                // 使用应用特定目录，无需存储权限
                String patchPath = getExternalFilesDir(null).getAbsolutePath() + "/app-debug-patch_signed_7zip.apk";
                Log.d(TAG, "=== Tinker Patch Loading Started ===");
                Log.d(TAG, "App external files directory: " + getExternalFilesDir(null).getAbsolutePath());
                Log.d(TAG, "Patch file path: " + patchPath);

                // 检查 Tinker 状态
                Tinker tinker = Tinker.with(getApplicationContext()); 
                Log.d(TAG, "Tinker loaded: " + tinker.isTinkerLoaded());
                Log.d(TAG, "Tinker installed: " + tinker.isTinkerInstalled());
                Log.d(TAG, "Tinker enabled: " + tinker.isTinkerEnabled());
                Log.d(TAG, "Current TINKER_ID: " + BuildInfo.TINKER_ID);
                Log.d(TAG, "Current PLATFORM: " + BuildInfo.PLATFORM);

                // Check if patch file exists
                java.io.File patchFile = new java.io.File(patchPath);
                if (patchFile.exists()) {
                    Log.d(TAG, "Patch file exists, size: " + patchFile.length() + " bytes");
                    Log.d(TAG, "Patch file readable: " + patchFile.canRead());

                    // 检查 patch 文件的元数据
                    try {
                        java.util.Properties properties = ShareTinkerInternals.fastGetPatchPackageMeta(patchFile);
                        if (properties != null) {
                            Log.d(TAG, "Patch platform: " + properties.getProperty("platform"));
                            Log.d(TAG, "Patch TINKER_ID: " + properties.getProperty("TINKER_ID"));
                            Log.d(TAG, "Patch properties: " + properties.toString());
                        } else {
                            Log.e(TAG, "Failed to read patch metadata");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading patch metadata: " + e.getMessage());
                    }

                    Log.d(TAG, "Starting patch installation...");
                    TinkerInstaller.onReceiveUpgradePatch(getApplicationContext(), patchPath);

                    // 添加结果监听
                    DefaultPatchListener patchListener = new DefaultPatchListener(getApplicationContext()) {
                        @Override
                        protected int patchCheck(String path, String patchMd5) {
                            int result = super.patchCheck(path, patchMd5);
                            Log.d(TAG, "Patch check result: " + result);
                            return result;
                        }
                    };

                    Log.d(TAG, "Patch installation request sent");
                } else {
                    Log.e(TAG, "ERROR: Patch file does not exist at: " + patchPath);
                    Log.e(TAG, "Please push the patch file to app's external files directory");
                    Log.e(TAG, "Use command: adb push app-debug-patch_signed_7zip.apk " + patchPath);
                }
                Log.d(TAG, "=== Tinker Patch Loading End ===");
            }
        });

        Button loadLibraryButton = (Button) findViewById(R.id.loadLibrary);

        loadLibraryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // #method 1, hack classloader library path
                TinkerLoadLibrary.installNavitveLibraryABI(getApplicationContext(), "armeabi");
                System.loadLibrary("stlport_shared");

                // #method 2, for lib/armeabi, just use TinkerInstaller.loadLibrary
               TinkerLoadLibrary.loadArmLibrary(getApplicationContext(), "stlport_shared");

                // #method 3, load tinker patch library directly
//               TinkerInstaller.loadLibraryFromTinker(getApplicationContext(), "assets/x86", "stlport_shared");

            }
        });

        Button cleanPatchButton = (Button) findViewById(R.id.cleanPatch);

        cleanPatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tinker.with(getApplicationContext()).cleanPatch();
            }
        });

        Button killSelfButton = (Button) findViewById(R.id.killSelf);

        killSelfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareTinkerInternals.killAllOtherProcess(getApplicationContext());
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });

        Button buildInfoButton = (Button) findViewById(R.id.showInfo);

        buildInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfo(MainActivity.this);
            }
        });
    }

    private void askForRequiredPermissions() {
        if (Build.VERSION.SDK_INT < 23) {
            return;
        }
        if (!hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }
    }

    private boolean hasRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= 16) {
            final int res = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
            return res == PackageManager.PERMISSION_GRANTED;
        } else {
            // When SDK_INT is below 16, READ_EXTERNAL_STORAGE will also be granted if WRITE_EXTERNAL_STORAGE is granted.
            final int res = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return res == PackageManager.PERMISSION_GRANTED;
        }
    }

    public boolean showInfo(Context context) {
        // 检查是否有待安装的 patch
        File patchDirectory = SharePatchFileUtil.getPatchDirectory(getApplicationContext());
        Log.d("Tinker", "Patch directory: " + patchDirectory.getAbsolutePath());

        // add more Build Info
        final StringBuilder sb = new StringBuilder();
        Tinker tinker = Tinker.with(getApplicationContext());
        if (tinker.isTinkerLoaded()) {
            sb.append(String.format("[patch is loaded] \n"));
            sb.append(String.format("[buildConfig TINKER_ID] %s \n", BuildInfo.TINKER_ID));
            sb.append(String.format("[buildConfig BASE_TINKER_ID] %s \n", BaseBuildInfo.BASE_TINKER_ID));

            sb.append(String.format("[buildConfig MESSSAGE] %s \n", BuildInfo.MESSAGE));
            sb.append(String.format("[TINKER_ID] %s \n", tinker.getTinkerLoadResultIfPresent().getPackageConfigByName(ShareConstants.TINKER_ID)));
            sb.append(String.format("[packageConfig patchMessage] %s \n", tinker.getTinkerLoadResultIfPresent().getPackageConfigByName("patchMessage")));
            sb.append(String.format("[TINKER_ID Rom Space] %d k \n", tinker.getTinkerRomSpace()));

        } else {
            sb.append(String.format("[patch is not loaded] \n"));
            sb.append(String.format("[buildConfig TINKER_ID] %s \n", BuildInfo.TINKER_ID));
            sb.append(String.format("[buildConfig BASE_TINKER_ID] %s \n", BaseBuildInfo.BASE_TINKER_ID));

            sb.append(String.format("[buildConfig MESSSAGE] %s \n", BuildInfo.MESSAGE));
            sb.append(String.format("[TINKER_ID] %s \n", ShareTinkerInternals.getManifestTinkerID(getApplicationContext())));
        }
        sb.append(String.format("[BaseBuildInfo Message] %s \n", BaseBuildInfo.TEST_MESSAGE));

        final TextView v = new TextView(context);
        v.setText(sb);
        v.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
        v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        v.setTextColor(0xFF000000);
        v.setTypeface(Typeface.MONOSPACE);
        final int padding = 16;
        v.setPadding(padding, padding, padding, padding);

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setView(v);
        final AlertDialog alert = builder.create();
        alert.show();
        return true;
    }

    @Override
    protected void onResume() {
        Log.e(TAG, "i am on onResume");
        Log.e(TAG, "i am on patch onResume16");

        super.onResume();
        Utils.setBackground(false);

        if (hasRequiredPermissions()) {
            mTvMessage.setVisibility(View.GONE);
        } else {
            mTvMessage.setText(R.string.msg_no_permissions);
            mTvMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            mTvMessage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.setBackground(true);
    }
}
