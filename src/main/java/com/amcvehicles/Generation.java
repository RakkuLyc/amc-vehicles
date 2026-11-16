package com.amcvehicles;

import com.abandonedmoderncity.core.ChunkPlan;
import com.abandonedmoderncity.core.Lot;
import com.abandonedmoderncity.core.WorldPlan;
import com.abandonedmoderncity.gen.Plans;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
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
 *   lot's seed and the bay); if there is any, the chunk is written in {@link Pending} and queued.</li>
 *   <li>Never inside AMC's generation: the cars are placed by the server tick, in the real world, once every chunk under
 *   the body is a loaded full chunk ({@code getChunkNow}, which never forces a load). A car whose chunks are not ready
 *   waits; a car blocked in the world (a block in the way, no floor, a vehicle there) is dropped for good.</li>
 *   <li>A pending chunk that loads again (after a restart) is queued again.</li>
 * </ul>
 */
public final class Generation {
    /** How often (ticks) the queue is looked at. */
    static final int EVERY = 10;

    /** Chunks to look at, in order. Only the overworld has AMC cities. */
    private static final LongLinkedOpenHashSet QUEUE = new LongLinkedOpenHashSet();

    private Generation() {
    }

    /** A drawn car of a chunk: the structure, the bay (index in spots.txt), where, and the draw. */
    public record Planned(AmcWorld.Placed structure, int bay, Placement.Car car, Draw.Car drawn) {
    }

    static void onChunkLoad(ChunkEvent.Load e) {
        if (!(e.getLevel() instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD) {
            return;
        }
        ChunkPos pos = e.getChunk().getPos();
        Pending pending = Pending.of(level);
        if (e.isNewChunk()) {
            if (!carsOf(level, pos.x, pos.z).isEmpty()) {
                pending.add(pos.toLong());
                QUEUE.add(pos.toLong());
            }
        } else if (pending.contains(pos.toLong())) {
            QUEUE.add(pos.toLong());
        }
    }

    static void onLevelTick(LevelTickEvent.Post e) {
        if (!(e.getLevel() instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD || QUEUE.isEmpty()
                || level.getGameTime() % EVERY != 0) {
            return;
        }
        Pending pending = Pending.of(level);
        for (long chunk : QUEUE.toLongArray()) {
            int cx = ChunkPos.getX(chunk);
            int cz = ChunkPos.getZ(chunk);
            if (level.getChunkSource().getChunkNow(cx, cz) == null) {
                // unloaded: it stays pending and comes back when it loads
                QUEUE.remove(chunk);
                continue;
            }
            boolean waiting = false;
            for (Planned p : carsOf(level, cx, cz)) {
                if (!ready(level, p.car())) {
                    waiting = true;
                    continue;
                }
                place(level, p, "generated");
            }
            if (!waiting) {
                pending.remove(chunk);
                QUEUE.remove(chunk);
            }
        }
    }

    static void onServerStopped(ServerStoppedEvent e) {
        QUEUE.clear();
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
        Mercedes.spawn(level, c.x(), c.y(), c.z(), c.noseYaw(), d.color(), d.hoodOpen(), d.trunkOpen(),
                d.doorLeftOpen(), d.doorRightOpen());
        AmcVehicles.LOG.info("[AMCV] Mercedes{} in {} bay {} at {} {} {} ({})", d.color(), name, p.bay() + 1,
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
        WorldPlan plan = Plans.get(level);
        if (plan == null) {
            return List.of();
        }
        ChunkPlan cp = plan.chunk(cx, cz);
        // the structures with bays that have a column in this chunk (a car's origin lies in its bay, in its lot)
        Map<Lot, AmcWorld.Placed> structures = new LinkedHashMap<>();
        for (int i = 0; i < 256; i++) {
            Lot lot = cp.lot[i];
            if (lot != null && lot.building != null && !structures.containsKey(lot)
                    && !Spots.of(lot.building.model()).isEmpty()) {
                structures.put(lot, new AmcWorld.Placed(lot.building, lot.x0, lot.z0, lot.rot, cp.h2[i] / 2 - 1,
                        lot.seed));
            }
        }
        List<Planned> out = new ArrayList<>();
        for (AmcWorld.Placed s : structures.values()) {
            out.addAll(drawn(s, cx, cz));
        }
        return out;
    }

    /** The drawn cars of a structure whose origin lies in chunk (cx, cz); in any chunk if cx is null. */
    static List<Planned> drawn(AmcWorld.Placed s, Integer cx, Integer cz) {
        List<Spots.Bay> bays = Spots.of(s.def().model());
        List<Planned> out = new ArrayList<>();
        for (int k = 0; k < bays.size(); k++) {
            Draw.Car d = Draw.car(s.seed(), k, s.police());
            if (d == null) {
                continue;
            }
            Placement.Car car = s.car(bays.get(k));
            if (cx != null && (((int) Math.floor(car.x())) >> 4 != cx || ((int) Math.floor(car.z())) >> 4 != cz)) {
                continue;
            }
            out.add(new Planned(s, k, car, d));
        }
        return out;
    }

    static int queued() {
        return QUEUE.size();
    }
}
