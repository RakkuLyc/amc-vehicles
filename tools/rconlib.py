"""Minimal RCON client for the test server (port 25576, password "teste": run/server.properties).

The server splits a reply longer than 4096 bytes into packets of 4096; Rcon.cmd keeps reading while a packet comes
full, so long replies come whole. One packet is sent at a time: the vanilla RCON reads one packet per read and closes
the connection when a read returns a piece shorter than a packet header (two packets sent back to back broke it).
"""
import socket, struct

PORT, PASSWORD = 25576, "teste"
CMD_ID = 10
CHUNK = 4096


class Rcon:
    def __init__(self, port=PORT, password=PASSWORD, timeout=120):
        self.s = socket.create_connection(("127.0.0.1", port), timeout=timeout)
        self._send(1, 3, password)
        rid, _ = self._recv()
        if rid == -1:
            raise SystemExit("RCON: password refused")

    def _send(self, rid, kind, body):
        data = struct.pack("<ii", rid, kind) + body.encode() + b"\x00\x00"
        self.s.sendall(struct.pack("<i", len(data)) + data)

    def _read(self, n):
        buf = b""
        while len(buf) < n:
            chunk = self.s.recv(n - len(buf))
            if not chunk:
                raise ConnectionError("RCON closed")
            buf += chunk
        return buf

    def _recv(self):
        n = struct.unpack("<i", self._read(4))[0]
        buf = self._read(n)
        rid, _ = struct.unpack("<ii", buf[:8])
        return rid, buf[8:-2].decode(errors="replace")

    def cmd(self, command):
        self._send(CMD_ID, 2, command)
        rid, body = self._recv()
        out = [body]
        # a full packet may be followed by the rest of the reply
        while len(body.encode()) >= CHUNK:
            self.s.settimeout(2)
            try:
                rid, body = self._recv()
            except socket.timeout:
                break
            finally:
                self.s.settimeout(120)
            out.append(body)
        return "".join(out)
