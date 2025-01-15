import subprocess
import sys

def check_kdu_compress_version():
    """
    Checks the availability of the kdu_compress tool and prints its version if available.
    Exits with a non-zero status if the tool is not found or an error occurs.
    """
    try:
        # Attempt to run kdu_compress with the -v flag
        result = subprocess.run(
            ["kdu_compress", "-v"],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True
        )
        if result.returncode == 0:
            print("kdu_compress is available.")
            print("Version information:")
            print(result.stdout)
        else:
            print("kdu_compress is installed but returned a non-zero exit code.")
            print("Error output:")
            print(result.stderr)
            sys.exit(1)  # Exit with an error if the command fails
    except FileNotFoundError:
        print("Error: kdu_compress is not installed or not in the PATH.", file=sys.stderr)
        sys.exit(1)  # Exit with an error if the tool is not found
    except Exception as e:
        print(f"An unexpected error occurred: {e}", file=sys.stderr)
        sys.exit(1)  # Exit with an error on any other exception

if __name__ == "__main__":
    check_kdu_compress_version()
