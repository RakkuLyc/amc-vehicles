#!/usr/bin/env python3
"""Minimal RCON client for the test server (port 25576; password from AMCV_RCON_PASSWORD, default "teste").
usage: python3 -I tools/rcon.py "command 1" "command 2" ...   (prints the whole reply of each one)"""
import os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from rconlib import Rcon

r = Rcon()
for c in sys.argv[1:]:
    print(">", c)
    print(r.cmd(c))
