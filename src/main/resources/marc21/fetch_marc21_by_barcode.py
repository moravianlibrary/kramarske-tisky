import subprocess
import sys
import re
import argparse

def search_record(host, port, base, barcode, debug=False):
    try:
        # Start the yaz-client process
        process = subprocess.Popen(['yaz-client'], stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)

        # Commands to run in yaz-client
        commands = f"""open {host}:{port}
base {base}
format marc21
find @attr 1=1063 {barcode}
show
"""

        # Communicate with the yaz-client process
        stdout, stderr = process.communicate(commands)

        if debug:
            # Print the full output for debugging
            print("DEBUG: Full output from yaz-client:")
            print(stdout)
            print("DEBUG: End of yaz-client output")

        # Check for connection errors
        if 'Connection accepted' not in stdout:
            print("Error: Failed to connect to the server.")
            sys.exit(1)

        # Check for search errors
        if 'Number of hits: 0' in stdout:
            print("Error: No records found.")
            sys.exit(1)
        elif 'Number of hits: 1' not in stdout:
            print("Error: More than one record found.")
            sys.exit(1)

        # Search for the first line that looks like the start of a MARC record
        record_start_match = re.search(r'^\d{5}[a-z]{3}', stdout, re.MULTILINE)
        if record_start_match:
            record_start = record_start_match.start()
            if debug:
                print(f"DEBUG: Record start position: {record_start}")

            # Extract the record from the start position
            record = stdout[record_start:].split("\n\n", 1)[0]  # Splitting by double newlines for better separation
            print(record)
        else:
            if debug:
                print("Error: Record not properly retrieved.")
            sys.exit(1)

        # Return success
        sys.exit(0)

    except Exception as e:
        print(f"Error: {str(e)}")
        sys.exit(1)

if __name__ == '__main__':
    # Argument parsing with an example in the help text
    parser = argparse.ArgumentParser(
        description="Fetch MARC21 record by barcode using yaz-client.",
        epilog="Example usage: python3 fetch_marc21_by_barcode.py --host aleph.mzk.cz --port 9991 --base MZK03CPK --barcode 2610798805"
    )
    parser.add_argument('--host', required=True, help='The host of the yaz-client server.')
    parser.add_argument('--port', required=True, help='The port of the yaz-client server.')
    parser.add_argument('--base', required=True, help='The base to use for the search.')
    parser.add_argument('--barcode', required=True, help='The barcode to search for.')
    parser.add_argument('--debug', action='store_true', help='Enable debug output.')

    args = parser.parse_args()

    # Call the search function with the parsed arguments
    search_record(args.host, args.port, args.base, args.barcode, args.debug)
