#!/usr/bin/env bash
set -euo pipefail
stage=/mnt/d/SeamlessRowingQA-20261011
pair=${1:-pair9}
[[ "$pair" =~ ^pair[0-9]+$ ]] || { echo 'Invalid pair ID' >&2; exit 1; }
output=${2:-/mnt/c/Users/derko/Desktop/minecraft/seamless-rowing/qa-artifacts/preview-26.3-fabric/media}
sample=${3:-r23}
[[ "$sample" =~ ^[[:alnum:]]+$ ]] || { echo 'Invalid sample profile' >&2; exit 1; }
mkdir -p "$output"
encode() {
    local profile="$1" first="$2" frames="$3" name="$4"
    ffmpeg -hide_banner -loglevel error -y -threads 2 -framerate 20 -start_number "$first" \
        -i "$stage/client-$profile/screenshots/mp-%04d.png" -frames:v "$frames" \
        -c:v libx264 -crf 17 -preset fast -pix_fmt yuv420p -movflags +faststart -an "$output/$name.mp4"
}
encode "${pair}rower" 1 140 first-person
encode "${pair}rower" 141 140 third-person-back
encode "${pair}rower" 281 139 third-person-front
observer_frames=$(find "$stage/client-${pair}observer/screenshots" -maxdepth 1 -name 'mp-*.png' | wc -l)
encode "${pair}observer" 1 "$observer_frames" observer
# Clean no-overlay samples, preserving actual animation and camera perspective.
for clip in first-person-stroke third-person-stroke; do
    first=30
    filter='scale=800:-1:flags=lanczos'
    if [[ "$clip" == third-person-stroke ]]; then first=220; filter='crop=640:360:320:280,scale=800:-1:flags=lanczos'; fi
    ffmpeg -hide_banner -loglevel error -y -threads 2 -framerate 20 -start_number "$first" \
        -i "$stage/client-$sample/screenshots/rowing-%03d.png" -frames:v 32 \
        -filter_complex "[0:v]$filter,split[a][b];[a]palettegen=max_colors=96:stats_mode=diff[p];[b][p]paletteuse=dither=sierra2_4a" \
        -loop 0 "$output/$clip.gif"
done
ffprobe -v error -show_entries stream=width,height,r_frame_rate -of json "$output/first-person.mp4" > "$output/video-format.json"
echo 'Encoded 20-tick-per-second native captures. Audio is intentionally silent in the isolated display.'
