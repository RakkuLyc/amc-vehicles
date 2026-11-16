package com.amcvehicles;

import com.abandonedmoderncity.core.ChunkPlan;
import com.abandonedmoderncity.core.Lot;
import com.abandonedmoderncity.core.Structures;
import com.abandonedmoderncity.core.WorldPlan;
import com.abandonedmoderncity.gen.Plans;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

/** The AMC plan read from the world: which structure stands at a column, and whether a car fits in the world. */
public final class AmcWorld {
    /** The Minecraft entity of IV's vehicles (and of its other "existing" entities). */
    static final ResourceLocation VEHICLE = ResourceLocation.fromNamespaceAndPath("mts", "builder_existing");
    /** Height (blocks) up to which a block on the floor does not stop a car: carpets (1/16), a thin snow layer. */
    static final double THIN = 0.25;

    private AmcWorld() {
    }

    /**
     * An AMC structure in the world: {@code def} with its box corner at (x0, z0), turned {@code rot}, its city ground
     * at y = {@code base}; {@code seed} = the seed AMC gives its lot (the copy), which the draw of the cars uses.
     */
    public record Placed(Structures.Def def, int x0, int z0, int rot, int base, long seed) {
        public Placement.Car car(Spots.Bay bay, Cars.Model m) {
            return Placement.car(def, x0, z0, rot, base, bay, m);
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
        return new Placed(lot.building, lot.x0, lot.z0, lot.rot, base, lot.seed);
    }

    /**
     * Why the car cannot stand where it is planned, or null when it fits: under every column of its body a floor
     * (a block with a collision shape) and {@link Placement#HEADROOM} (3) blocks free over it (no collision: air, grass and vines; on the first one a
     * thin block, such as a carpet, is allowed: {@link #THIN}), and no
     * Immersive Vehicles vehicle there already ({@code mts:builder_existing}: a car of an earlier {@code /amcv fill} or of
     * the generation; not IV's helper entities, such as the ones that follow the players).
     */
    public static String blocked(ServerLevel level, Placement.Car car) {
        // the first block over the floor (the car's origin is CAR_LIFT over it)
        int y = (int) Math.floor(car.y());
        int minX = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (int[] c : car.cells()) {
            minX = Math.min(minX, c[0]);
            minZ = Math.min(minZ, c[1]);
            maxX = Math.max(maxX, c[0]);
            maxZ = Math.max(maxZ, c[1]);
        }
        AABB box = new AABB(minX, y, minZ, maxX + 1, y + Placement.HEADROOM, maxZ + 1);
        for (Entity e : level.getEntities((Entity) null, box,
                e -> BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).equals(VEHICLE))) {
            return "occupied by a vehicle at " + e.getBlockX() + " " + e.getBlockY() + " " + e.getBlockZ();
        }
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
            for (int k = 0; k < Placement.HEADROOM; k++) {
                BlockPos p = new BlockPos(c[0], y + k, c[1]);
                BlockState s = level.getBlockState(p);
                VoxelShape shape = s.getCollisionShape(level, p);
                // on the floor, a thin block (a carpet of the overgrowth, 1/16 high) does not stop the car
                if (!shape.isEmpty() && (k > 0 || shape.max(Direction.Axis.Y) > THIN)) {
                    return s.getBlock().getName().getString() + " at " + c[0] + " " + (y + k) + " " + c[1];
                }
            }
        }
        return null;
    }
}
