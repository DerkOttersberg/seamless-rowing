#!/usr/bin/env bash
set -euo pipefail
repo=/mnt/c/Users/derko/Desktop/minecraft/seamless-rowing
out="$repo/qa-artifacts/preview-3-26.3-fabric/media"
bash "$repo/qa/encode_preview.sh" pair14 "$out" s1
# Full 256-color palettes fit the delivery budget for these native captures.
for clip in first-person-stroke third-person-stroke; do
    first=30;filter='scale=800:-1:flags=lanczos'
    if [[ "$clip" == third-person-stroke ]]; then first=220;filter='crop=640:360:320:280,scale=800:-1:flags=lanczos';fi
    ffmpeg -hide_banner -loglevel error -y -threads 2 -filter_complex_threads 2 -framerate 20 -start_number "$first" \
        -i /mnt/d/SeamlessRowingQA-20261011/client-s1/screenshots/rowing-%03d.png -frames:v 32 \
        -filter_complex "[0:v]$filter,split[a][b];[a]palettegen=max_colors=256:stats_mode=diff[p];[b][p]paletteuse=dither=sierra2_4a" \
        -loop 0 "$out/$clip.gif"
done
python3 "$repo/qa/validate_motion_media.py"
