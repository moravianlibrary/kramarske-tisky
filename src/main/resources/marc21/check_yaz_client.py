import subprocess
import sys

def check_yaz_client():
    try:
        # Attempt to get the version using a non-interactive flag
        process = subprocess.run(
            ['yaz-client', '-V'],
            check=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            timeout=10
        )

        # Print the version information, stripping extra whitespace
        print(process.stdout.strip())
        sys.exit(0)

    except FileNotFoundError:
        print("Error: yaz-client is not installed or not available in the PATH.", file=sys.stderr)
        sys.exit(1)

    except subprocess.TimeoutExpired:
        print("Error: yaz-client timed out.", file=sys.stderr)
        sys.exit(1)

    except subprocess.CalledProcessError as e:
        print(f"Error: yaz-client returned an error.\n{e.stderr.strip()}", file=sys.stderr)
        sys.exit(1)

if __name__ == '__main__':
    check_yaz_client()
