package com.amcvehicles;

import com.abandonedmoderncity.core.ChunkPlan;
import com.abandonedmoderncity.core.Lot;
import com.abandonedmoderncity.core.Structures;
import com.abandonedmoderncity.core.WorldPlan;
import com.abandonedmoderncity.gen.Plans;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/** The AMC plan read from the world: which structure stands at a column, and whether a car fits in the world. */
public final class AmcWorld {
    private AmcWorld() {
    }

    /**
     * An AMC structure in the world: {@code def} with its box corner at (x0, z0), turned {@code rot}, its city ground
     * at y = {@code base}.
     */
    public record Placed(Structures.Def def, int x0, int z0, int rot, int base) {
        public Placement.Car car(Spots.Bay bay) {
            return Placement.car(def, x0, z0, rot, base, bay);
        }

        public boolean police() {
            return "police_station".equals(def.type());
        }
    }

    /** The structure whose lot holds column (x, z), or null (no AMC plan, not a structure lot). */
    public static Placed at(ServerLevel level, int x, int z) {
        WorldPlan plan = Plans.get(level);
        if (plan == null) {
            return null;
        }
        ChunkPlan cp = plan.chunk(x >> 4, z >> 4);
        int i = ChunkPlan.idx(x & 15, z & 15);
        Lot lot = cp.lot[i];
        if (lot == null || lot.building == null) {
            return null;
        }
        // the base of a lot column, as AMC's ChunkBuilder works it out
        int base = cp.h2[i] / 2 - 1;
        return new Placed(lot.building, lot.x0, lot.z0, lot.rot, base);
    }

    /**
     * Why the car cannot stand where it is planned, or null when it fits: under every column of its body a floor
     * (a block with a collision shape) and 2 blocks free over it (no collision: air, carpets, grass and vines).
     */
    public static String blocked(ServerLevel level, Placement.Car car) {
        int y = (int) Math.round(car.y());
        for (int[] c : car.cells()) {
            BlockPos floor = new BlockPos(c[0], y - 1, c[1]);
            if (!level.isLoaded(floor)) {
                return "chunk not loaded at " + c[0] + " " + c[1];
            }
            BlockState f = level.getBlockState(floor);
            if (f.getCollisionShape(level, floor).isEmpty()) {
                return "no floor at " + c[0] + " " + (y - 1) + " " + c[1] + " (" + f.getBlock().getName().getString()
                        + ")";
            }
            for (int k = 0; k < 2; k++) {
                BlockPos p = new BlockPos(c[0], y + k, c[1]);
                BlockState s = level.getBlockState(p);
                if (!s.getCollisionShape(level, p).isEmpty()) {
                    return s.getBlock().getName().getString() + " at " + c[0] + " " + (y + k) + " " + c[1];
                }
            }
        }
        return null;
    }
}
