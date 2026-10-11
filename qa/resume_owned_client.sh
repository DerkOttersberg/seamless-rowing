#!/usr/bin/env bash
set -euo pipefail
profile="${1:?owned profile required}"
[[ "$profile" =~ ^client-r[0-9]+$ ]] || exit 2
for candidate in /proc/[0-9]*/cmdline; do
  command=$(tr '\0' ' ' < "$candidate" 2>/dev/null || true)
  [[ "$command" == /opt/rowing-java-test/bin/java* && "$command" == *"/SeamlessRowingQA-20261011/$profile "* ]] || continue
  pid=${candidate#/proc/};pid=${pid%/cmdline}
  while IFS= read -r entry;do
    case "$entry" in DISPLAY=*) export "$entry";; XAUTHORITY=*) export "$entry";; esac
  done < <(tr '\0' '\n' < "/proc/$pid/environ")
  [[ "${DISPLAY:-}" =~ ^:[1-9][0-9]*$ && -n "${XAUTHORITY:-}" ]] || exit 2
  ffmpeg -hide_banner -loglevel error -f x11grab -i "$DISPLAY" -frames:v 1 -y "/mnt/d/SeamlessRowingQA-20261011/$profile/private-display.png"
  window=$(xdotool search --pid "$pid" --name 'Minecraft' | head -1)
  xdotool windowfocus --sync "$window"
  xdotool key --window "$window" Escape
  printf 'Resumed owned private display %s for %s\n' "$DISPLAY" "$profile"
  exit 0
done
exit 3
