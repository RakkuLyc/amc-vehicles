"""Reads a gzipped NBT file (an AMC structure .nbt) into Python values; a library for find_spots.py.

usage (from another tool in tools/): from nbt import load; data = load("<file.nbt>")
"""
import gzip, struct, sys, collections

def read_nbt(path):
    data = gzip.open(path).read()
    pos = [0]
    def rd(n):
        b = data[pos[0]:pos[0]+n]; pos[0] += n; return b
    def payload(t):
        if t == 1: return struct.unpack('>b', rd(1))[0]
        if t == 2: return struct.unpack('>h', rd(2))[0]
        if t == 3: return struct.unpack('>i', rd(4))[0]
        if t == 4: return struct.unpack('>q', rd(8))[0]
        if t == 5: return struct.unpack('>f', rd(4))[0]
        if t == 6: return struct.unpack('>d', rd(8))[0]
        if t == 7: n = struct.unpack('>i', rd(4))[0]; return rd(n)
        if t == 8: n = struct.unpack('>H', rd(2))[0]; return rd(n).decode('utf-8', 'replace')
        if t == 9:
            et = rd(1)[0]; n = struct.unpack('>i', rd(4))[0]
            return [payload(et) for _ in range(n)]
        if t == 10:
            d = {}
            while True:
                tt = rd(1)[0]
                if tt == 0: return d
                nl = struct.unpack('>H', rd(2))[0]; name = rd(nl).decode('utf-8', 'replace')
                d[name] = payload(tt)
        if t == 11: n = struct.unpack('>i', rd(4))[0]; return list(struct.unpack('>%di' % n, rd(4*n)))
        if t == 12: n = struct.unpack('>i', rd(4))[0]; return list(struct.unpack('>%dq' % n, rd(8*n)))
        raise ValueError(t)
    t = rd(1)[0]; nl = struct.unpack('>H', rd(2))[0]; rd(nl)
    return payload(t)

def load(path):
    n = read_nbt(path)
    pal = n['palette'] if 'palette' in n else n['palettes'][0]
    names = []
    for p in pal:
        s = p['Name'].replace('minecraft:', '')
        if 'Properties' in p:
            s += '[' + ','.join('%s=%s' % kv for kv in sorted(p['Properties'].items())) + ']'
        names.append(s)
    blocks = {}
    for b in n['blocks']:
        x, y, z = b['pos']
        blocks[(x, y, z)] = names[b['state']]
    return n['size'], blocks

if __name__ == '__main__':
    size, blocks = load(sys.argv[1])
    print('size', size)
    c = collections.Counter(v.split('[')[0] for v in blocks.values())
    for k, v in c.most_common(int(sys.argv[2]) if len(sys.argv) > 2 else 60):
        print(v, k)
