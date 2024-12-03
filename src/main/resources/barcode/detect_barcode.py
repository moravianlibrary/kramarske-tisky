import sys
import os
from pyzbar.pyzbar import decode
from PIL import Image, ImageEnhance

# Preprocess the image
def preprocess_image(image_path):
    img = Image.open(image_path)

    # Convert to grayscale
    img = img.convert("L")

    # Increase contrast
    enhancer = ImageEnhance.Contrast(img)
    img = enhancer.enhance(2.0)  # Adjust factor as needed

    # Make it smaller
    img = img.resize((img.width // 4, img.height // 4))

    return img

# Decode and check for Code39
def detect_code39(image_path):
    img = preprocess_image(image_path)
    barcodes = decode(img)

    if not barcodes:
        print(f"No barcode detected in the image: {image_path}")
    else:
        code39_found = False
        for barcode in barcodes:
            barcode_type = barcode.type
            barcode_data = barcode.data.decode("utf-8")

            # Check specifically for Code39
            if barcode_type == "CODE39":
                code39_found = True
                print(f"Detected Code39 Barcode: {barcode_data}")

        if not code39_found:
            print("No Code39 barcode detected, but other types were found.")
            for barcode in barcodes:
                print(f"Other Barcode Detected: {barcode.data.decode('utf-8')} (Type: {barcode.type})")

# Main script
if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python detect_barcode.py <image_path>")
        sys.exit(1)

    # Get the image path from the command line argument
    image_path = sys.argv[1]

    # Check if the file exists
    if not os.path.isfile(image_path):
        print(f"Error: File does not exist: {image_path}")
        sys.exit(1)

    # Detect barcode
    detect_code39(image_path)
