#!/usr/bin/env python3
"""Cliente RCON mínimo para o servidor de teste (porta 25576, senha "teste": run/server.properties).
Uso: python3 -I tools/rcon.py "comando 1" "comando 2" ...   (imprime a resposta de cada um)"""
import socket, struct, sys

def pkt(sock, kind, body, rid=1):
    data = struct.pack("<ii", rid, kind) + body.encode() + b"\x00\x00"
    sock.sendall(struct.pack("<i", len(data)) + data)

def read(sock):
    n = struct.unpack("<i", sock.recv(4))[0]
    buf = b""
    while len(buf) < n:
        buf += sock.recv(n - len(buf))
    rid, kind = struct.unpack("<ii", buf[:8])
    return rid, buf[8:-2].decode(errors="replace")

def main():
    s = socket.create_connection(("127.0.0.1", 25576), timeout=60)
    pkt(s, 3, "teste")
    if read(s)[0] == -1:
        sys.exit("RCON: senha recusada")
    for cmd in sys.argv[1:]:
        pkt(s, 2, cmd, 2)
        print(">", cmd)
        print(read(s)[1])

main()
