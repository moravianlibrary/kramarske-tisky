#!/bin/bash

# Check if ImageMagick's magick command is available
if ! command -v magick &> /dev/null; then
    echo "Error: ImageMagick is not installed. Install it and try again."
    exit 1
fi

# Check if a directory is provided as an argument
if [ -z "$1" ]; then
    echo "Usage: $0 <directory>"
    exit 1
fi

# Get the directory from the first argument
input_dir="$1"

# Check if the directory exists
if [ ! -d "$input_dir" ]; then
    echo "Error: Directory '$input_dir' does not exist."
    exit 1
fi

# Loop through all .tif files in the given directory
for file in "$input_dir"/*.tif; do
    # Check if any .tif files exist
    if [ ! -e "$file" ]; then
        echo "No .tif files found in the directory '$input_dir'."
        exit 1
    fi

    # Extract the filename without the extension
    base_name="${file%.tif}"

    # Convert the .tif file to .png using the same base name
    magick "$file" "${base_name}.png"

    # Check if the conversion was successful
    if [ $? -eq 0 ]; then
        echo "Converted: $file -> ${base_name}.png"

        # Delete the original .tif file
        rm "$file"
        if [ $? -eq 0 ]; then
            echo "Deleted original file: $file"
        else
            echo "Failed to delete original file: $file"
        fi
    else
        echo "Failed to convert: $file"
    fi
done
