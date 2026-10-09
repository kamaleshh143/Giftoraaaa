#!/usr/bin/env sh
set -e

# Bind Tomcat's HTTP connector to Render's $PORT (defaults to 8080 locally).
: "${PORT:=8080}"
sed -i "s/port=\"8080\"/port=\"${PORT}\"/g" /usr/local/tomcat/conf/server.xml

# Ensure the H2 database directory exists and is writable
mkdir -p "${GIFTORA_DB_DIR:-/data}"

exec catalina.sh run
