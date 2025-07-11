#!/usr/bin/env bash

git diff --name-only HEAD~1 HEAD | \
while read file; do
  dir=$(dirname "$file")
  while [ "$dir" != "." ] && [ "$dir" != "/" ]; do
    if [ -f "$dir/pom.xml" ]; then echo "$dir"; break; fi
    dir=$(dirname "$dir")
  done
done | sort -u | tr '\n' ',' | sed 's/,$//'

# 如果为空，则使用默认值 '.'
if [ -z "$modules" ]; then
  echo "."
else
  echo "$modules"
fi