#!/bin/bash

# 切换到脚本当前目录
cd "$(dirname "$0")"

echo "🔧 开始修复 Android Studio 项目构建权限问题..."

PROJECT_DIR=$(pwd)
BUILD_DIR="$PROJECT_DIR/app/build"

# 修改文件拥有者
echo "👤 更改文件夹拥有者为当前用户..."
sudo chown -R $(whoami) "$BUILD_DIR"

# 设置读写权限
echo "🛡️ 添加读写权限..."
chmod -R u+rw "$BUILD_DIR"

# 清理旧构建缓存
echo "🧹 执行 gradlew clean..."
./gradlew clean  assembleDebug

echo "✅ 修复完成，请重新在 Android Studio 中运行项目。"
