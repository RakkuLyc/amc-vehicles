#!/usr/bin/env python3
"""Checks, on the test server, that each IV car near a point stands between two painted stripes.

usage: python3 -I tools/check_bays.py <x> <z> <radius> [stripe block, default white_concrete]
For each car (entity mts:builder_existing within radius): its origin, the IV yaw (anglesy), the middle of its body
(1.425 ahead of the rear axle), and how many cells of the two columns 2 blocks to each side of the middle, along
the 5 cells of the body, are the stripe block on the floor (y of the car - 1).
"""
import math, re, socket, struct, sys

PORT, PASSWORD = 25576, "teste"


def connect():
    s = socket.create_connection(("127.0.0.1", PORT), timeout=60)
    send(s, 3, PASSWORD)
    return s


def send(s, kind, body):
    data = struct.pack("<ii", 7, kind) + body.encode() + b"\x00\x00"
    s.sendall(struct.pack("<i", len(data)) + data)
    if kind == 3:
        recv(s)


def recv(s):
    n = struct.unpack("<i", s.recv(4))[0]
    buf = b""
    while len(buf) < n:
        buf += s.recv(n - len(buf))
    return buf[8:-2].decode(errors="replace")


def cmd(s, c):
    send(s, 2, c)
    return recv(s)


def main():
    x, z, r = float(sys.argv[1]), float(sys.argv[2]), float(sys.argv[3])
    stripe = sys.argv[4] if len(sys.argv) > 4 else "white_concrete"
    s = connect()
    pos = re.findall(r"\[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]",
                     cmd(s, "execute as @e[type=mts:builder_existing] run data get entity @s Pos"))
    yaws = re.findall(r"(-?[\d.]+)d", cmd(s, "execute as @e[type=mts:builder_existing] run data get entity @s anglesy"))
    ok = 0
    for i, ((px, py, pz), yw) in enumerate(zip(pos, yaws)):
        px, py, pz, yw = float(px), float(py), float(pz), float(yw)
        if math.hypot(px - x, pz - z) > r:
            continue
        mc = (-yw) % 360          # IV yaw = - Minecraft yaw
        dx, dz = -math.sin(math.radians(mc)), math.cos(math.radians(mc))
        dx, dz = round(dx), round(dz)
        cx, cz = px + dx * 1.425, pz + dz * 1.425
        floor = int(math.floor(py + 0.5)) - 1
        good = all_ = 0
        for t in (-2, -1, 0, 1, 2):
            for side in (-2, 2):
                # along the body t, across side (perpendicular)
                bx = cx + dx * t + (-dz) * side
                bz = cz + dz * t + dx * side
                res = cmd(s, "execute if block %d %d %d minecraft:%s" % (math.floor(bx), floor, math.floor(bz), stripe))
                all_ += 1
                good += "passed" in res
        face = {(0, 1): "S", (-1, 0): "W", (0, -1): "N", (1, 0): "E"}[(dx, dz)]
        print("car %2d at %.2f %.2f %.2f nose %s: %d/%d stripe cells" % (i + 1, px, py, pz, face, good, all_))
        ok += good >= 0.6 * all_
    print("%d cars between stripes" % ok)


main()
