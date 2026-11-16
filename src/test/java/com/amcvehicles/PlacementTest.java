package com.amcvehicles;

import com.abandonedmoderncity.core.BlockCode;
import com.abandonedmoderncity.core.Structures;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The bays of spots.txt, turned as AMC turns the structures, land where AMC puts the stripes: checked against AMC's own
 * {@code Structures.Def.code} (the blocks AMC places, with the overgrowth) in the 4 rotations.
 */
class PlacementTest {
    /** Stripe cells beside the bay that must still be the stripe block after the overgrowth (it turns some floor). */
    static final double STRIPE_SHARE = 0.6;
    /** The models whose bays lie between painted stripes, and the stripe block (the others are written by hand). */
    static final Map<String, String> PAINTED = Map.of("abandoned_wide_building", "minecraft:white_concrete");
    /** Blocks a car goes through (no collision): plants, carpets, vines, cobwebs, torches... */
    static final String[] SOFT = {"vine", "short_grass", "tall_grass", "fern", "carpet", "cobweb", "lichen",
            "dead_bush", "torch", "button", "pressure_plate", "minecraft:snow[", "flower", "sapling"};

    /** Every structure (colour variant) of a model. */
    static List<Structures.Def> variants(String model) {
        List<Structures.Def> out = new ArrayList<>();
        for (Structures.Def d : Structures.ALL) {
            if (d.model().equals(model)) {
                out.add(d);
            }
        }
        return out;
    }

    /** The bay with each way of the nose it may take (both for a drawn facing). */
    static List<Spots.Bay> ways(Spots.Bay bay) {
        return bay.drawnFacing() ? List.of(bay.oriented(false), bay.oriented(true)) : List.of(bay);
    }

    static boolean free(String state) {
        if (state.equals("air") || state.startsWith("code ")) {
            return state.equals("air") || state.equals("code " + BlockCode.KEEP);
        }
        for (String s : SOFT) {
            if (state.contains(s)) {
                return true;
            }
        }
        return false;
    }

    static Structures.Def def(String model) {
        for (Structures.Def d : Structures.ALL) {
            if (d.model().equals(model)) {
                return d;
            }
        }
        return null;
    }

    static String state(Structures.Def def, int rx, int ry, int rz, int rot) {
        int c = def.code(rx, ry, rz, rot);
        if (c < BlockCode.STRUCT) {
            return c == BlockCode.AIR ? "air" : "code " + c;
        }
        return Structures.STATES.get((c - BlockCode.STRUCT) / 4);
    }

    @Test
    void everyModelOfSpotsExists() {
        assertFalse(Spots.all().isEmpty());
        for (String model : Spots.all().keySet()) {
            assertNotNull(def(model), "spots.txt names a model AMC does not have: " + model);
        }
    }

    @Test
    void floorAndRoomInEveryRotationAndVariant() {
        int checked = 0;
        for (Map.Entry<String, List<Spots.Bay>> e : Spots.all().entrySet()) {
            for (Structures.Def def : variants(e.getKey())) {
                for (Spots.Bay bay : e.getValue()) {
                    int ry = bay.y() - def.oy() - def.ground();
                    for (int rot = 0; rot < 4; rot++) {
                        for (int x = bay.x0(); x <= bay.x1(); x++) {
                            for (int z = bay.z0(); z <= bay.z1(); z++) {
                                double[] p = Placement.turn(def, rot, x - def.ox() + 0.5, z - def.oz() + 0.5);
                                int rx = (int) Math.floor(p[0]);
                                int rz = (int) Math.floor(p[1]);
                                String floor = state(def, rx, ry, rz, rot);
                                String where = def.name() + " " + bay + " rot " + rot + " cell " + x + "," + z;
                                assertFalse(free(floor), where + ": no floor (" + floor + ")");
                                for (int k = 1; k <= 2; k++) {
                                    String above = state(def, rx, ry + k, rz, rot);
                                    assertTrue(free(above), where + ": " + above + " " + k + " over the floor");
                                }
                                checked++;
                            }
                        }
                    }
                }
            }
        }
        System.out.println("floorAndRoom: " + checked + " cells checked");
    }

    @Test
    void stripesOnBothSidesInEveryRotation() {
        for (Map.Entry<String, List<Spots.Bay>> e : Spots.all().entrySet()) {
            String stripe = PAINTED.get(e.getKey());
            if (stripe == null) {
                continue;
            }
            Structures.Def def = def(e.getKey());
            for (Spots.Bay bay : e.getValue()) {
                boolean alongZ = bay.alongZ();
                for (int rot = 0; rot < 4; rot++) {
                    int ry = bay.y() - def.oy() - def.ground();
                    int good = 0;
                    int all = 0;
                    // the two stripes: the column just outside each long side of the bay
                    for (int k = alongZ ? bay.z0() : bay.x0(); k <= (alongZ ? bay.z1() : bay.x1()); k++) {
                        int[][] sides = alongZ
                                ? new int[][]{{bay.x0() - 1, k}, {bay.x1() + 1, k}}
                                : new int[][]{{k, bay.z0() - 1}, {k, bay.z1() + 1}};
                        for (int[] t : sides) {
                            double[] p = Placement.turn(def, rot, t[0] - def.ox() + 0.5, t[1] - def.oz() + 0.5);
                            String s = state(def, (int) Math.floor(p[0]), ry, (int) Math.floor(p[1]), rot);
                            all++;
                            if (s.startsWith(stripe)) {
                                good++;
                            }
                        }
                    }
                    assertTrue(good >= STRIPE_SHARE * all, e.getKey() + " " + bay + " rot " + rot + ": " + good
                            + " of " + all + " stripe cells");
                }
            }
        }
    }

    @Test
    void carInsideItsBayAndAlongIt() {
        for (Map.Entry<String, List<Spots.Bay>> e : Spots.all().entrySet()) {
            Structures.Def def = def(e.getKey());
            for (Spots.Bay drawnBay : e.getValue()) {
                for (Spots.Bay bay : ways(drawnBay))
                for (int rot = 0; rot < 4; rot++) {
                    int lotX = 1000;
                    int lotZ = -2000;
                    int base = 64;
                    Placement.Car car = Placement.car(def, lotX, lotZ, rot, base, bay);
                    // the corners of the bay, turned: the body must lie inside them
                    double[] a = Placement.turn(def, rot, bay.x0() - def.ox(), bay.z0() - def.oz());
                    double[] b = Placement.turn(def, rot, bay.x1() + 1 - def.ox(), bay.z1() + 1 - def.oz());
                    double minX = lotX + Math.min(a[0], b[0]);
                    double maxX = lotX + Math.max(a[0], b[0]);
                    double minZ = lotZ + Math.min(a[1], b[1]);
                    double maxZ = lotZ + Math.max(a[1], b[1]);
                    for (int[] c : car.cells()) {
                        assertTrue(c[0] >= minX && c[0] + 1 <= maxX && c[1] >= minZ && c[1] + 1 <= maxZ,
                                e.getKey() + " " + bay + " rot " + rot + ": body cell " + c[0] + "," + c[1]
                                        + " out of the bay " + minX + ".." + maxX + ", " + minZ + ".." + maxZ);
                    }
                    // the nose runs along the long side of the bay
                    boolean longX = maxX - minX > maxZ - minZ;
                    float yaw = car.noseYaw();
                    assertEquals(longX, yaw == 90f || yaw == 270f, e.getKey() + " " + bay + " rot " + rot);
                    // floor: the layer of the bay is the floor (a block, not air) and the car stands on top of it
                    assertEquals(base + bay.y() - def.oy() - def.ground() + 1, car.y(), 1e-9);
                }
            }
        }
    }

    @Test
    void turnedDirectionMatchesTurnedPoints() {
        Structures.Def def = def("abandoned_wide_building");
        int[][] dirs = {{0, -1}, {0, 1}, {1, 0}, {-1, 0}};
        for (int rot = 0; rot < 4; rot++) {
            for (int[] d : dirs) {
                double[] p = Placement.turn(def, rot, 10, 10);
                double[] q = Placement.turn(def, rot, 10 + d[0], 10 + d[1]);
                int[] wd = Placement.turnDir(def, rot, d[0], d[1]);
                assertEquals(q[0] - p[0], wd[0], 1e-9);
                assertEquals(q[1] - p[1], wd[1], 1e-9);
            }
        }
    }

    @Test
    void yawOfDirections() {
        assertEquals(0f, Placement.yaw(0, 1));
        assertEquals(90f, Placement.yaw(-1, 0));
        assertEquals(180f, Placement.yaw(0, -1));
        assertEquals(270f, Placement.yaw(1, 0));
    }
}
