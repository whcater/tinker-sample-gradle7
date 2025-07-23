mkdir -p ./logs
#adb logcat | grep "$(adb shell ps | grep tinker.sample.android | awk '{print $2}')"  | tee -a ./app/logs/test.log
#adb logcat | grep "Tinker.MainActivity" | tee -a ./app/logs/test.log
# adb logcat -c && adb logcat | grep "$(adb shell ps | grep tinker.sample.android | awk '{print $2}')"
# | tee >(cat) >> ~/dev/paperless/tinker/tinker-sample-android/app/logs/test.log
adb logcat -c && adb logcat | grep Tinker 