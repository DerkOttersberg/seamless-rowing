#!/usr/bin/env bash
set -euo pipefail
profile="${1:?owned profile required}"
[[ "$profile" =~ ^client-r[0-9]+$ ]] || exit 2
for candidate in /proc/[0-9]*/cmdline;do
  command=$(tr '\0' ' ' < "$candidate" 2>/dev/null || true)
  [[ "$command" == /opt/rowing-java-test/bin/java* && "$command" == *"/SeamlessRowingQA-20261011/$profile "* ]] || continue
  pid=${candidate#/proc/};pid=${pid%/cmdline}
  kill -TERM "$pid"
  printf 'Terminated owned incomplete QA profile %s (PID %s)\n' "$profile" "$pid"
  exit 0
done
exit 3
