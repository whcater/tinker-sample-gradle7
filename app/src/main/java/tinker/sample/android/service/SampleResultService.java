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

package tinker.sample.android.service;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.tencent.tinker.lib.service.DefaultTinkerResultService;
import com.tencent.tinker.lib.service.PatchResult;
import com.tencent.tinker.lib.util.TinkerServiceInternals;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.tencent.tinker.loader.shareutil.SharePatchInfo;
import com.tencent.tinker.loader.shareutil.ShareTinkerLog;

import java.io.File;
import java.util.List;

import tinker.sample.android.util.Utils;

/**
 * optional, you can just use DefaultTinkerResultService
 * we can restart process when we are at background or screen off
 * Created by zhangshaowen on 16/4/13.
 */
public class SampleResultService extends DefaultTinkerResultService {
    private static final String TAG = "Tinker.SampleResultService";

    private boolean verifyPatchResult(PatchResult result) {
        if (!result.isSuccess) {
            return false;
        }

        try {
            // 检查补丁目录是否存在
            File patchDir = new File(result.rawPatchFilePath);
            if (!patchDir.exists()) {
                ShareTinkerLog.e(TAG, "Patch directory not exists: " + result.rawPatchFilePath);
                return false;
            }

            // 检查关键文件是否存在
            File patchInfoFile = new File(patchDir, ShareConstants.PATCH_INFO_NAME);
            if (!patchInfoFile.exists()) {
                ShareTinkerLog.e(TAG, "Patch info file not exists");
                return false;
            }

            // 检查patch.info内容是否有效
            SharePatchInfo patchInfo = SharePatchInfo.readAndCheckPropertyWithLock(patchInfoFile, patchDir);
            if (patchInfo == null) {
                ShareTinkerLog.e(TAG, "Patch info is invalid");
                return false;
            }

            ShareTinkerLog.i(TAG, "Patch verification passed");
            return true;

        } catch (Exception e) {
            ShareTinkerLog.e(TAG, "Patch verification failed", e);
            return false;
        }
    }

    private boolean isTinkerPatchServiceRunning() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = am.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo service : services) {
            if (service.service.getClassName().contains("TinkerPatchService")) {
                ShareTinkerLog.i(TAG, "TinkerPatchService is running"); 
                return true;
            }
        }
        return false;
    }


    @Override
    public void onPatchResult(final PatchResult result) {
        if (result == null) {
            ShareTinkerLog.e(TAG, "SampleResultService received null result!!!!");
            return;
        }
        ShareTinkerLog.i(TAG, "SampleResultService receive result: %s", result.toString());

        //first, we want to kill the recover process
//        TinkerServiceInternals.killTinkerPatchServiceProcess(getApplicationContext());


        // 先验证补丁结果
        boolean patchValid = verifyPatchResult(result);
        ShareTinkerLog.i(TAG, "Patch validation result: " + patchValid);

        // 只有验证通过后才杀死服务进程
        if (patchValid) {
            ShareTinkerLog.i(TAG, "TinkerPatchService is running: " + isTinkerPatchServiceRunning());
            // 延迟杀死服务进程
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    TinkerServiceInternals.killTinkerPatchServiceProcess(getApplicationContext());
                }
            }, 5000);
        }


        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (result.isSuccess) {
                    ShareTinkerLog.d(TAG, "patch success, please restart process");
                    Toast.makeText(getApplicationContext(), "patch success, please restart process", Toast.LENGTH_LONG).show();

                } else {
                    ShareTinkerLog.d(TAG, "patch fail, please check reason");
                    Toast.makeText(getApplicationContext(), "patch fail, please check reason", Toast.LENGTH_LONG).show();
                }
            }
        });
        // is success and newPatch, it is nice to delete the raw file, and restart at once
        // for old patch, you can't delete the patch file
        if (result.isSuccess) {
            deleteRawPatchFile(new File(result.rawPatchFilePath));

            //not like TinkerResultService, I want to restart just when I am at background!
            //if you have not install tinker this moment, you can use TinkerApplicationHelper api
            if (checkIfNeedKill(result)) {
                if (Utils.isBackground()) {
                    ShareTinkerLog.i(TAG, "it is in background, just restart process");
                    restartProcess();
                } else {
                    //we can wait process at background, such as onAppBackground
                    //or we can restart when the screen off
                    ShareTinkerLog.i(TAG, "tinker wait screen to restart process");
                    new Utils.ScreenState(getApplicationContext(), new Utils.ScreenState.IOnScreenOff() {
                        @Override
                        public void onScreenOff() {
                            restartProcess();
                        }
                    });
                }
            } else {
                ShareTinkerLog.i(TAG, "I have already install the newly patch version!");
            }
        }
    }

    /**
     * you can restart your process through service or broadcast
     */
    private void restartProcess() {
        ShareTinkerLog.i(TAG, "app is background now, i can kill quietly");
        //you can send service or broadcast intent to restart your process
        android.os.Process.killProcess(android.os.Process.myPid());
    }

}
