#!/bin/bash

# Check if ImageMagick's magick command is available
if ! command -v magick &> /dev/null; then
    echo "Error: ImageMagick is not installed. Install it and try again."
    exit 1
fi

# Loop through all .tif files in the current directory
for file in *.tif; do
    # Extract the filename without the extension
    base_name="${file%.tif}"

    # Convert the .tif file to .png using the same base name
    magick "$file" "${base_name}.png"

    # Check if the conversion was successful
    if [ $? -eq 0 ]; then
        echo "Converted: $file -> ${base_name}.png"
    else
        echo "Failed to convert: $file"
    fi
done
