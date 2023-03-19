#!/bin/sh
cd "$(dirname "$0")" || exit 1

version_pattern='(\d\.){2}\d'

if [ -z "$1" ]; then
  grep versionName app/build.gradle | grep -Po "$version_pattern"
else
  echo "$1" | grep -qPo "$version_pattern" || exit 1
  new_versionCode=$(($(grep versionCode app/build.gradle | grep -oP '\d+') + 1))
  sed -i -E \
    's/(^ *versionCode).*/\1 '"$new_versionCode/;
    "'s/(^ *versionName).*/\1 "'"$1"'"/' \
    app/build.gradle
fi
