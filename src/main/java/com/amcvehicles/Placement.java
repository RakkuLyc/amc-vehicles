package com.amcvehicles;

import com.abandonedmoderncity.core.Structures;

import java.util.ArrayList;
import java.util.List;

/**
 * Where a car stands in the world for a bay of a structure placed by AMC: the template coordinates of the bay turned
 * as AMC turns the structure ({@code Structures.Def}: the box of its solid blocks at ox, oy, oz, size w × h × d,
 * {@code ground} layers buried, {@code turn} + the lot's {@code rot} quarter turns clockwise) and moved to the lot.
 * No Minecraft classes: tested with JUnit against {@code Def.code}.
 */
final class Placement {
    /** Free blocks a car needs over the floor (the tallest model on its support blocks: {@link Cars#HEADROOM}). */
    public static final int HEADROOM = Cars.HEADROOM;
    /** A column counts under the body only when the body covers more than this sliver (blocks) of it. */
    private static final double SLIVER = 0.01;

    /**
     * A car: its origin (the rear axle) at x, y, z (y = the top of the floor + the model's lift), the nose to the
     * Minecraft yaw {@code noseYaw} (0 south, 90 west, 180 north, 270 east), and the columns its body covers.
     */
    public record Car(double x, double y, double z, float noseYaw, List<int[]> cells) {
    }

    private Placement() {
    }

    /** Quarter turns from the template to the world. */
    static int turns(Structures.Def def, int rot) {
        return (rot + def.turn()) & 3;
    }

    /**
     * A point (lx, lz) of the template's box (continuous, lx in [0, w], lz in [0, d]) in the turned box (as
     * {@code Def.template} maps the cells, read backwards).
     */
    public static double[] turn(Structures.Def def, int rot, double lx, double lz) {
        int w = def.w();
        int d = def.d();
        return switch (turns(def, rot)) {
            case 0 -> new double[]{lx, lz};
            case 1 -> new double[]{d - lz, lx};
            case 2 -> new double[]{w - lx, d - lz};
            default -> new double[]{lz, w - lx};
        };
    }

    /** A direction {dx, dz} of the template turned with the structure. */
    public static int[] turnDir(Structures.Def def, int rot, int dx, int dz) {
        return switch (turns(def, rot)) {
            case 0 -> new int[]{dx, dz};
            case 1 -> new int[]{-dz, dx};
            case 2 -> new int[]{-dx, -dz};
            default -> new int[]{dz, -dx};
        };
    }

    /** A fuel pump in the world: its block at x, y, z, its front to the Minecraft yaw {@code yaw}. */
    public record PumpAt(int x, int y, int z, float yaw) {
    }

    /** Where a pump of the template stands for the structure {@code def} placed as in {@link #car}. */
    public static PumpAt pump(Structures.Def def, int lotX0, int lotZ0, int rot, int base, Spots.Pump pump) {
        double[] c = turn(def, rot, pump.x() - def.ox() + 0.5, pump.z() - def.oz() + 0.5);
        int[] dir = pump.dir();
        int[] wd = turnDir(def, rot, dir[0], dir[1]);
        return new PumpAt(lotX0 + (int) Math.floor(c[0]), base + (pump.y() - def.oy() - def.ground()),
                lotZ0 + (int) Math.floor(c[1]), yaw(wd[0], wd[1]));
    }

    /** Minecraft yaw of a horizontal unit direction: south 0, west 90, north 180, east 270. */
    public static float yaw(int dx, int dz) {
        if (dz > 0) {
            return 0f;
        }
        if (dx < 0) {
            return 90f;
        }
        if (dz < 0) {
            return 180f;
        }
        return 270f;
    }

    /**
     * The car of a bay, for the structure {@code def} with its box corner at (lotX0, lotZ0), turned {@code rot},
     * whose city ground (layer {@code ground} of the box) lies at y = {@code base}.
     */
    public static Car car(Structures.Def def, int lotX0, int lotZ0, int rot, int base, Spots.Bay bay, Cars.Model m) {
        // middle of the bay, in the box (the template less the offset of its solid blocks)
        double cx = (bay.x0() + bay.x1() + 1) / 2.0 - def.ox();
        double cz = (bay.z0() + bay.z1() + 1) / 2.0 - def.oz();
        double[] c = turn(def, rot, cx, cz);
        int[] dir = bay.dir();
        int[] wd = turnDir(def, rot, dir[0], dir[1]);
        // the body is centred in the bay: the origin (rear axle) sits back from the middle
        double back = (m.front() - m.rear()) / 2.0;
        double x = lotX0 + c[0] - wd[0] * back;
        double z = lotZ0 + c[1] - wd[1] * back;
        // the floor block of the bay is at layer y of the template; the car rests on its support blocks over it
        int floorY = base + (bay.y() - def.oy() - def.ground());
        return new Car(x, floorY + 1 + m.lift(), z, yaw(wd[0], wd[1]), cells(x, z, wd, m));
    }

    /** Columns (x, z) under the body of a car with its origin at (x, z), the nose to {@code wd}. */
    static List<int[]> cells(double x, double z, int[] wd, Cars.Model m) {
        double ax0 = -m.rear();
        double ax1 = m.front();
        double hw = m.halfWidth();
        double minX = wd[0] != 0 ? x + Math.min(ax0 * wd[0], ax1 * wd[0]) : x - hw;
        double maxX = wd[0] != 0 ? x + Math.max(ax0 * wd[0], ax1 * wd[0]) : x + hw;
        double minZ = wd[1] != 0 ? z + Math.min(ax0 * wd[1], ax1 * wd[1]) : z - hw;
        double maxZ = wd[1] != 0 ? z + Math.max(ax0 * wd[1], ax1 * wd[1]) : z + hw;
        List<int[]> out = new ArrayList<>();
        for (int ix = (int) Math.floor(minX + SLIVER); ix <= (int) Math.floor(maxX - SLIVER); ix++) {
            for (int iz = (int) Math.floor(minZ + SLIVER); iz <= (int) Math.floor(maxZ - SLIVER); iz++) {
                out.add(new int[]{ix, iz});
            }
        }
        return out;
    }
}
