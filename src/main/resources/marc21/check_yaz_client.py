import subprocess
import sys

def check_yaz_client():
    try:
        # Attempt to get the version using a non-interactive flag, if available
        process = subprocess.run(
            ['yaz-client', '-V'],  # Use the correct flag for version if -V exists
            check=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            timeout=10  # Set a timeout to prevent hanging
        )

        # Print the version information
        print(process.stdout)
        sys.exit(0)

    except FileNotFoundError:
        # yaz-client is not found
        print("Error: yaz-client is not installed or not available in the PATH.", file=sys.stderr)
        sys.exit(1)

    except subprocess.TimeoutExpired:
        # yaz-client did not respond in time
        print("Error: yaz-client timed out.", file=sys.stderr)
        sys.exit(1)

    except subprocess.CalledProcessError as e:
        # Handle other errors, like incorrect usage
        print(f"Error: yaz-client returned an error.\n{e.stderr}", file=sys.stderr)
        sys.exit(1)

if __name__ == '__main__':
    check_yaz_client()
