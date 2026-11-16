#!/usr/bin/env python3
"""Compares, on the test server, the cars the generation placed in AMC structures with what the draw predicts.

usage: python3 -I tools/check_generation.py [--load] "x0,z0 .. x1,z1" ...   (the boxes /amcs locate prints)
--load: forceload each box first (that generates it) and wait for the mod to place the cars.
For each box: the IV vehicles inside it (counted by the game) and the reply of `/amcv draw` run in its middle: the
bays the draw gives a car and, for each, why it was not placed now ("occupied by a vehicle" = the generation already
put it there). The counts must be equal and every drawn bay occupied.
"""
import os, re, sys, time
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from rconlib import Rcon


def main():
    args = sys.argv[1:]
    load = "--load" in args
    boxes = []
    for a in args:
        m = re.match(r"\s*(-?\d+),(-?\d+)\s*\.\.\s*(-?\d+),(-?\d+)", a)
        if m:
            boxes.append(tuple(int(v) for v in m.groups()))
    r = Rcon()
    if load:
        for x0, z0, x1, z1 in boxes:
            print(r.cmd("forceload add %d %d %d %d" % (x0, z0, x1, z1)).strip())
        time.sleep(8)
    total_cars = total_drawn = total_occupied = 0
    for x0, z0, x1, z1 in boxes:
        cars = r.cmd("execute if entity @e[type=mts:builder_existing,x=%d,y=-64,z=%d,dx=%d,dy=400,dz=%d]"
                   % (x0, z0, x1 - x0 + 1, z1 - z0 + 1))
        m = re.search(r"count: (\d+)", cars)
        n = int(m.group(1)) if m else 0
        reply = r.cmd("execute positioned %d 100 %d run amcv draw" % ((x0 + x1) // 2, (z0 + z1) // 2))
        m = re.search(r"(\d+) drawn", reply)
        drawn = int(m.group(1)) if m else -1
        occupied = reply.count("occupied by a vehicle")
        total_cars += n
        total_drawn += drawn
        total_occupied += occupied
        status = "OK" if n == drawn == occupied else "MISMATCH"
        print("%s box %d,%d..%d,%d: %d cars in the box, %d drawn, %d occupied" % (status, x0, z0, x1, z1, n, drawn,
                                                                                 occupied))
        for line in reply.split("\n")[1:]:
            print("    " + line)
    print("total: %d cars, %d drawn, %d occupied" % (total_cars, total_drawn, total_occupied))


main()
