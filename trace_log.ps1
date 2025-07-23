# adb logcat | Select-String -Pattern "Tinker.MainActivity" | Tee-Object -FilePath ".\app\logs\test.log" -Append
adb logcat | Select-String -Pattern "Tinker.MainActivity" | Tee-Object -FilePath ".\app\logs\test.log"
-Append