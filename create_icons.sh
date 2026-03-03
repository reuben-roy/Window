#!/bin/bash
# Create launcher icons using base64-encoded minimal PNG

# Minimal 48x48 purple PNG (base64)
PURPLE_PNG="iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP4z8DwHwAFBQIB6fq+UwAAAABJRU5ErkJggg=="

cd "$(dirname "$0")"

for dir in app/src/main/res/mipmap-{m,h,xh,xxh,xxxh}dpi; do
    echo "$PURPLE_PNG" | base64 -D > "$dir/ic_launcher.png" 2>/dev/null || echo "$PURPLE_PNG" | base64 -d > "$dir/ic_launcher.png"
    echo "$PURPLE_PNG" | base64 -D > "$dir/ic_launcher_round.png" 2>/dev/null || echo "$PURPLE_PNG" | base64 -d > "$dir/ic_launcher_round.png"
    echo "Created icons in $dir"
done

echo "All icons created!"
ls -lh app/src/main/res/mipmap-mdpi/

