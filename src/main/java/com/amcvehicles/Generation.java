package com.amcvehicles;

import com.abandonedmoderncity.core.ChunkPlan;
import com.abandonedmoderncity.core.Lot;
import com.abandonedmoderncity.core.WorldPlan;
import com.abandonedmoderncity.gen.Plans;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import mcinterface1211.BuilderTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The cars in the world: drawn when a chunk is generated, placed on a later tick.
 * <ul>
 *   <li>A car belongs to the chunk of its origin (the rear axle). When a chunk is <b>generated</b> (not when an old
 *   one loads: the user's choice, only new chunks get cars), its cars are drawn ({@link Draw}, a pure function of the
 *   lot's seed and the bay's position); if there is any, the chunk is written in {@link Pending} (saved with the
 *   world).</li>
 *   <li>Never inside AMC's generation: every {@link #EVERY} ticks the server looks at the pending chunks that are loaded
 *   full chunks and places each of their cars once every chunk under its body is a loaded full chunk too
 *   ({@code getChunkNow}, which never forces a load). A car whose chunks are not ready waits; a car blocked in the world
 *   (a block in the way, no floor, a vehicle there) is dropped for good. When no car of a chunk waits any more, it
 *   leaves Pending. Polling Pending itself (not a queue fed by chunk loads) also catches a chunk that drops below full
 *   and comes back at the edge of a player's view without a new load event, and the chunks pending after a restart.</li>
 * </ul>
 */
public final class Generation {
    /** How often (ticks) the pending chunks are looked at. */
    static final int EVERY = 10;

    /**
     * The cars resolved (placed or dropped) in this session in chunks that are still pending because another car of
     * theirs waits: not tried again (no log line every 10 ticks). Keys: {@link #resolvedKey}.
     */
    private static final LongOpenHashSet RESOLVED = new LongOpenHashSet();

    private Generation() {
    }

    /** A drawn car of a chunk: the structure, the bay (index in spots.txt), where, and the draw. */
    public record Planned(AmcWorld.Placed structure, int bay, Placement.Car car, Draw.Car drawn) {
    }

    /** A fuel pump of a chunk (stage 6): the structure, the pump (index in spots.txt), where, and its buckets. */
    public record PlannedPump(AmcWorld.Placed structure, int pump, Placement.PumpAt at, int buckets) {
    }

    static void onChunkLoad(ChunkEvent.Load e) {
        if (!e.isNewChunk() || !(e.getLevel() instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD) {
            return;
        }
        ChunkPos pos = e.getChunk().getPos();
        if (!carsOf(level, pos.x, pos.z).isEmpty() || !pumpsOf(level, pos.x, pos.z).isEmpty()) {
            Pending.of(level).add(pos.toLong());
        }
    }

    static void onLevelTick(LevelTickEvent.Post e) {
        if (!(e.getLevel() instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD
                || level.getGameTime() % EVERY != 0) {
            return;
        }
        Pending pending = Pending.of(level);
        if (pending.size() == 0) {
            return;
        }
        for (long chunk : pending.toArray()) {
            int cx = ChunkPos.getX(chunk);
            int cz = ChunkPos.getZ(chunk);
            if (level.getChunkSource().getChunkNow(cx, cz) == null) {
                continue; // not loaded: it stays pending
            }
            boolean waiting = false;
            List<Long> done = new ArrayList<>();
            for (Planned p : carsOf(level, cx, cz)) {
                long key = resolvedKey(p);
                if (RESOLVED.contains(key)) {
                    continue;
                }
                if (!ready(level, p.car())) {
                    waiting = true;
                    continue;
                }
                place(level, p, "generated");
                done.add(key);
            }
            for (PlannedPump p : pumpsOf(level, cx, cz)) {
                long key = resolvedKey(p);
                if (RESOLVED.contains(key)) {
                    continue;
                }
                if (!pumpReady(level, cx, cz)) {
                    waiting = true;
                    continue;
                }
                placePump(level, p, "generated");
                done.add(key);
            }
            if (waiting) {
                RESOLVED.addAll(done);
            } else {
                pending.remove(chunk);
                for (Planned p : carsOf(level, cx, cz)) {
                    RESOLVED.remove(resolvedKey(p));
                }
                for (PlannedPump p : pumpsOf(level, cx, cz)) {
                    RESOLVED.remove(resolvedKey(p));
                }
            }
        }
    }

    static void onServerStopped(ServerStoppedEvent e) {
        RESOLVED.clear();
    }

    /** A key of a drawn car: its copy (lot seed) and bay. */
    static long resolvedKey(Planned p) {
        return Draw.mix(p.structure().seed() ^ Draw.mix(p.bay() + 1L));
    }

    /** A key of a pump: its copy and pump (never equal to a car's: another salt). */
    static long resolvedKey(PlannedPump p) {
        return Draw.mix(p.structure().seed() ^ Draw.mix(0x50554D50L + p.pump()));
    }

    /**
     * Whether a pump of chunk (cx, cz) may be placed: the chunk and its 8 neighbours are loaded full chunks. AMC's
     * NeighbourRepair puts the planned blocks back in a built chunk whenever a neighbour of it is generated later
     * (the cell of a pump is air in the template: a pump placed before would be taken out); once the 8 neighbours are
     * full, none of them is generated again.
     */
    static boolean pumpReady(ServerLevel level, int cx, int cz) {
        for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (level.getChunkSource().getChunkNow(cx + dx, cz + dz) == null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Places a fuel pump unless it is there already (an IV block entity in the cell) or the world blocks it (not air
     * or a plant, or no floor under it); returns what happened ("placed", "already there" or the reason).
     */
    static String placePump(ServerLevel level, PlannedPump p, String why) {
        Placement.PumpAt at = p.at();
        BlockPos pos = new BlockPos(at.x(), at.y(), at.z());
        String name = p.structure().def().name();
        if (!pumpReady(level, at.x() >> 4, at.z() >> 4)) {
            // never forces a load, never before the neighbours are full (see pumpReady)
            return "chunks around not ready";
        }
        if (level.getBlockEntity(pos) instanceof BuilderTileEntity) {
            return "already there";
        }
        BlockState s = level.getBlockState(pos);
        BlockState under = level.getBlockState(pos.below());
        // the cell may hold air, a plant, a vine, a cobweb or a thin block (a carpet), as PumpTest allows
        VoxelShape shape = s.getCollisionShape(level, pos);
        boolean free = !s.hasBlockEntity() && (s.canBeReplaced() || shape.isEmpty()
                || shape.max(Direction.Axis.Y) <= AmcWorld.THIN);
        String blocked = !free ? s.getBlock().getName().getString() + " in the cell"
                : under.getCollisionShape(level, pos.below()).isEmpty() ? "no floor" : null;
        if (blocked == null) {
            blocked = FuelPumps.place(level, pos, at.yaw(), p.buckets() * FuelPumps.BUCKET);
        }
        if (blocked != null) {
            AmcVehicles.LOG.info("[AMCV] {} pump {} skipped ({}): {}", name, p.pump() + 1, why, blocked);
            return blocked;
        }
        AmcVehicles.LOG.info("[AMCV] fuel pump {} in {} at {} {} {}, {} bucket(s) of lava ({})", p.pump() + 1, name,
                at.x(), at.y(), at.z(), p.buckets(), why);
        return "placed";
    }

    /**
     * Places a planned car unless the world blocks it; returns what happened ("placed" or the reason). The occupied
     * check also keeps a car that was placed before a crash, and not taken out of Pending, from being placed twice.
     */
    static String place(ServerLevel level, Planned p, String why) {
        String blocked = AmcWorld.blocked(level, p.car());
        String name = p.structure().def().name();
        if (blocked != null) {
            AmcVehicles.LOG.info("[AMCV] {} bay {} skipped ({}): {}", name, p.bay() + 1, why, blocked);
            return blocked;
        }
        Draw.Car d = p.drawn();
        Placement.Car c = p.car();
        Vehicles.spawn(level, c.x(), c.y(), c.z(), c.noseYaw(), d);
        AmcVehicles.LOG.info("[AMCV] {}{} in {} bay {} at {} {} {} ({})", d.model().key(), d.color(), name, p.bay() + 1,
                (int) Math.floor(c.x()), (int) Math.floor(c.y()), (int) Math.floor(c.z()), why);
        return "placed";
    }

    /** Whether every chunk under the body of the car is a loaded full chunk (no load is forced). */
    static boolean ready(ServerLevel level, Placement.Car car) {
        for (int[] cell : car.cells()) {
            if (level.getChunkSource().getChunkNow(cell[0] >> 4, cell[1] >> 4) == null) {
                return false;
            }
        }
        return true;
    }

    /** The drawn cars whose origin lies in chunk (cx, cz). */
    static List<Planned> carsOf(ServerLevel level, int cx, int cz) {
        List<Planned> out = new ArrayList<>();
        for (AmcWorld.Placed s : structuresIn(level, cx, cz)) {
            out.addAll(drawn(s, cx, cz));
        }
        return out;
    }

    /** The fuel pumps whose cell lies in chunk (cx, cz). */
    static List<PlannedPump> pumpsOf(ServerLevel level, int cx, int cz) {
        List<PlannedPump> out = new ArrayList<>();
        for (AmcWorld.Placed s : structuresIn(level, cx, cz)) {
            out.addAll(pumps(s, cx, cz));
        }
        return out;
    }

    /** The structures with bays or pumps that have a column in chunk (cx, cz) (a car or pump lies in its lot). */
    static List<AmcWorld.Placed> structuresIn(ServerLevel level, int cx, int cz) {
        WorldPlan plan = Plans.get(level);
        if (plan == null) {
            return List.of();
        }
        ChunkPlan cp = plan.chunk(cx, cz);
        Map<Lot, AmcWorld.Placed> structures = new LinkedHashMap<>();
        for (int i = 0; i < 256; i++) {
            Lot lot = cp.lot[i];
            if (lot != null && lot.building != null && !structures.containsKey(lot)
                    && (!Spots.of(lot.building.model()).isEmpty() || !Spots.pumps(lot.building.model()).isEmpty())) {
                structures.put(lot, new AmcWorld.Placed(lot.building, lot.x0, lot.z0, lot.rot, cp.h2[i] / 2 - 1,
                        lot.seed));
            }
        }
        return List.copyOf(structures.values());
    }

    /** The pumps of a structure whose cell lies in chunk (cx, cz); in any chunk if cx is null. */
    static List<PlannedPump> pumps(AmcWorld.Placed s, Integer cx, Integer cz) {
        List<Spots.Pump> pumps = Spots.pumps(s.def().model());
        List<PlannedPump> out = new ArrayList<>();
        for (int k = 0; k < pumps.size(); k++) {
            Placement.PumpAt at = s.pump(pumps.get(k));
            if (cx != null && (at.x() >> 4 != cx || at.z() >> 4 != cz)) {
                continue;
            }
            out.add(new PlannedPump(s, k, at, Draw.buckets(s.seed(), pumps.get(k).key())));
        }
        return out;
    }

    /** The drawn cars of a structure whose origin lies in chunk (cx, cz); in any chunk if cx is null. */
    static List<Planned> drawn(AmcWorld.Placed s, Integer cx, Integer cz) {
        List<Spots.Bay> bays = Spots.of(s.def().model());
        List<Planned> out = new ArrayList<>();
        for (int k = 0; k < bays.size(); k++) {
            Draw.Car d = Draw.car(s.seed(), bays.get(k).key(), s.police());
            if (d == null) {
                continue;
            }
            Placement.Car car = s.car(bays.get(k), d.model());
            if (cx != null && (((int) Math.floor(car.x())) >> 4 != cx || ((int) Math.floor(car.z())) >> 4 != cz)) {
                continue;
            }
            out.add(new Planned(s, k, car, d));
        }
        return out;
    }
}
