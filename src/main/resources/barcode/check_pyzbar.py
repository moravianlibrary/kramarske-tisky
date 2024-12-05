try:
    import pyzbar
except ImportError:
    print("pyzbar is not available")
    exit(1)

import ctypes
from ctypes.util import find_library

zbar_path = find_library('zbar')
if not zbar_path:
    print("zbar is not available")
    exit(1)

try:
    ctypes.CDLL(zbar_path)
except OSError:
    print("zbar is not available")
    exit(1)

print("All dependencies available")
