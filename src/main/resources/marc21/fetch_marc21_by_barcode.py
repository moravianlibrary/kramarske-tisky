import subprocess
import sys
import re

def search_record(domain, port, base, barcode, debug=False):
    try:
        # Start the yaz-client process
        process = subprocess.Popen(['yaz-client'], stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)

        # Commands to run in yaz-client
        commands = f"""open {domain}:{port}
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
            if debug:
                print("Error: Failed to connect to the server.")
            sys.exit(1)

        # Check for search errors
        if 'Number of hits: 0' in stdout:
            if debug:
                print("Error: No records found.")
            sys.exit(1)
        elif 'Number of hits: 1' not in stdout:
            if debug:
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
        if debug:
            print(f"Error: {str(e)}")
        sys.exit(1)

if __name__ == '__main__':
    domain = 'aleph.mzk.cz'
    port = '9991'
    base = 'MZK03CPK'
    barcode = '2610798805'
    debug = False  # Set this to True to enable debug prints

    search_record(domain, port, base, barcode, debug)
