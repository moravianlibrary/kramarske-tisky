#!/bin/bash

# Check if ImageMagick's magick command is available
if ! command -v magick &> /dev/null; then
    echo "Error: ImageMagick is not installed. Install it and try again."
    exit 1
fi
