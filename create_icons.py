#!/usr/bin/env python3
import struct
import zlib
import os

def create_png(width, height, r, g, b, filepath):
    """Create a simple solid color PNG file"""
    sig = b'\x89PNG\r\n\x1a\n'

    # IHDR chunk
    ihdr_data = struct.pack('>IIBBBBB', width, height, 8, 2, 0, 0, 0)
    ihdr = b'IHDR' + ihdr_data
    ihdr_chunk = struct.pack('>I', len(ihdr_data)) + ihdr + struct.pack('>I', zlib.crc32(ihdr))

    # IDAT chunk - raw image data
    raw = b''
    for y in range(height):
        raw += b'\x00'  # Filter type: None
        for x in range(width):
            raw += bytes([r, g, b])

    compressed = zlib.compress(raw, 9)
    idat = b'IDAT' + compressed
    idat_chunk = struct.pack('>I', len(compressed)) + idat + struct.pack('>I', zlib.crc32(idat))

    # IEND chunk
    iend_chunk = struct.pack('>I', 0) + b'IEND' + struct.pack('>I', zlib.crc32(b'IEND'))

    # Write file
    os.makedirs(os.path.dirname(filepath), exist_ok=True)
    with open(filepath, 'wb') as f:
        f.write(sig + ihdr_chunk + idat_chunk + iend_chunk)

    return filepath

# Create icons at all densities
base_path = 'app/src/main/res'
sizes = {
    'mipmap-mdpi': 48,
    'mipmap-hdpi': 72,
    'mipmap-xhdpi': 96,
    'mipmap-xxhdpi': 144,
    'mipmap-xxxhdpi': 192
}

created = []
for dir_name, size in sizes.items():
    dir_path = os.path.join(base_path, dir_name)
    launcher = create_png(size, size, 94, 53, 177, os.path.join(dir_path, 'ic_launcher.png'))
    launcher_round = create_png(size, size, 94, 53, 177, os.path.join(dir_path, 'ic_launcher_round.png'))
    created.extend([launcher, launcher_round])

print(f"Created {len(created)} launcher icons:")
for icon in created:
    print(f"  ✓ {icon}")

