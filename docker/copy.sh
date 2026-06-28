#!/bin/sh

# 复制项目的文件到对应docker路径，便于一键生成镜像。
usage() {
	echo "Usage: sh copy.sh"
	exit 1
}


# copy sql
echo "begin copy sql "
cp ../sql/ry_20250523.sql ./mysql/db
cp ../sql/ry_config_20250224.sql ./mysql/db

# copy html
echo "begin copy html "
cp -r ../xunfang-ui/dist/** ./nginx/html/dist


# copy jar
echo "begin copy xunfang-gateway "
cp ../xunfang-gateway/target/xunfang-gateway.jar ./xunfang/gateway/jar

echo "begin copy xunfang-auth "
cp ../xunfang-auth/target/xunfang-auth.jar ./xunfang/auth/jar

echo "begin copy xunfang-visual "
cp ../xunfang-visual/xunfang-monitor/target/xunfang-visual-monitor.jar  ./xunfang/visual/monitor/jar

echo "begin copy xunfang-modules-system "
cp ../xunfang-modules/xunfang-system/target/xunfang-modules-system.jar ./xunfang/modules/system/jar

echo "begin copy xunfang-modules-file "
cp ../xunfang-modules/xunfang-file/target/xunfang-modules-file.jar ./xunfang/modules/file/jar

echo "begin copy xunfang-modules-job "
cp ../xunfang-modules/xunfang-job/target/xunfang-modules-job.jar ./xunfang/modules/job/jar

echo "begin copy xunfang-modules-gen "
cp ../xunfang-modules/xunfang-gen/target/xunfang-modules-gen.jar ./xunfang/modules/gen/jar

