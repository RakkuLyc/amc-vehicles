#!/usr/bin/env python3
"""Cliente RCON mínimo para o servidor de teste (porta 25576, senha "teste": run/server.properties).
Uso: python3 -I tools/rcon.py "comando 1" "comando 2" ...   (imprime a resposta de cada um, inteira)"""
import os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from rconlib import Rcon

r = Rcon()
for c in sys.argv[1:]:
    print(">", c)
    print(r.cmd(c))
