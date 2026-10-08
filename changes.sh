#!/usr/bin/env bash

set -euo pipefail

# 收集变更模块。
# PR 场景按目标分支对比，避免 CI 修复类提交只修改 .github 时退化为全仓库测试；
# push 场景保持原有 HEAD~1..HEAD 行为，兼容发布工作流。
if [ -n "${GITHUB_BASE_REF:-}" ]; then
  git fetch origin "${GITHUB_BASE_REF}" >/dev/null 2>&1 || true
  if git rev-parse --verify "origin/${GITHUB_BASE_REF}" >/dev/null 2>&1; then
    diff_args=("origin/${GITHUB_BASE_REF}...HEAD")
  else
    diff_args=("HEAD~1" "HEAD")
  fi
else
  diff_args=("HEAD~1" "HEAD")
fi

modules=$(git diff --name-only "${diff_args[@]}" | while read -r file; do
  dir=$(dirname "$file")
  while [ "$dir" != "." ] && [ "$dir" != "/" ]; do
    if [ -f "$dir/pom.xml" ]; then echo "$dir"; break; fi
    dir=$(dirname "$dir")
  done
done | sort -u | paste -sd, -)

# 如果为空，则使用默认值 '.'
if [ -z "$modules" ]; then
  echo "."
else
  echo "$modules"
fi
