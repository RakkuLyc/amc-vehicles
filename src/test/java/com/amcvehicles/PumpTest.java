package com.amcvehicles;

import com.abandonedmoderncity.core.Structures;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The fuel pumps of spots.txt (stage 6) against AMC's own blocks, and their draw. */
class PumpTest {
    @Test
    void pumpsOfTheGasStations() {
        assertEquals(8, Spots.pumps("gas_station").size());
        assertEquals(2, Spots.pumps("gas_station_small").size());
        assertEquals(2, Spots.allPumps().size());
    }

    /** Every pump, in every colour variant and rotation: a floor under it, its cell and the one over it free. */
    @Test
    void floorAndRoomInEveryRotationAndVariant() {
        int checked = 0;
        for (Map.Entry<String, List<Spots.Pump>> e : Spots.allPumps().entrySet()) {
            for (Structures.Def def : PlacementTest.variants(e.getKey())) {
                for (Spots.Pump p : e.getValue()) {
                    int ry = p.y() - def.oy() - def.ground();
                    for (int rot = 0; rot < 4; rot++) {
                        Placement.PumpAt at = Placement.pump(def, 0, 0, rot, 0, p);
                        String where = def.name() + " " + p + " rot " + rot;
                        String floor = PlacementTest.state(def, at.x(), ry - 1, at.z(), rot);
                        assertFalse(PlacementTest.free(floor), where + ": no floor (" + floor + ")");
                        for (int k = 0; k <= 1; k++) {
                            String cell = PlacementTest.state(def, at.x(), ry + k, at.z(), rot);
                            assertTrue(PlacementTest.free(cell), where + ": " + cell + " at +" + k);
                        }
                        assertEquals(ry, at.y(), where);
                        checked++;
                    }
                }
            }
        }
        System.out.println("pumps: " + checked + " placements checked");
        assertTrue(checked > 0);
    }

    /** No pump stands in a bay of its structure (in any rotation: the same turn for both). */
    @Test
    void pumpsOutOfTheBays() {
        for (Map.Entry<String, List<Spots.Pump>> e : Spots.allPumps().entrySet()) {
            for (Spots.Pump p : e.getValue()) {
                for (Spots.Bay b : Spots.of(e.getKey())) {
                    boolean inside = p.x() >= b.x0() && p.x() <= b.x1() && p.z() >= b.z0() && p.z() <= b.z1();
                    assertFalse(inside, e.getKey() + " " + p + " in " + b);
                }
            }
        }
    }

    /** The front of a pump turns with the structure as the cars do. */
    @Test
    void facingTurnsWithTheStructure() {
        Structures.Def def = PlacementTest.def("gas_station");
        Spots.Pump p = Spots.pumps("gas_station").get(0);
        for (int rot = 0; rot < 4; rot++) {
            int[] wd = Placement.turnDir(def, rot, p.dir()[0], p.dir()[1]);
            assertEquals(Placement.yaw(wd[0], wd[1]), Placement.pump(def, 0, 0, rot, 0, p).yaw());
        }
    }

    @Test
    void halfEmptyHalfOneToThree() {
        int n = 1_200_000;
        int[] count = new int[Draw.MAX_BUCKETS + 1];
        for (int i = 0; i < n; i++) {
            int b = Draw.buckets(i * 6151L + 3, Spots.pumps("gas_station").get(i % 8).key());
            assertTrue(b >= 0 && b <= Draw.MAX_BUCKETS, "buckets " + b);
            count[b]++;
        }
        System.out.println("buckets 0..3: " + java.util.Arrays.toString(count));
        assertEquals(0.5, (double) count[0] / n, 0.002);
        for (int b = 1; b <= Draw.MAX_BUCKETS; b++) {
            assertEquals(0.5 / Draw.MAX_BUCKETS, (double) count[b] / n, 0.002, b + " buckets");
        }
    }

    /**
     * The pumps of one station are drawn independently: a pump has 1.25 buckets on average (E[X] = 1.25,
     * E[X^2] = 0.5 * (1 + 4 + 9 + 16) / 4 = 3.75, var 2.1875), so a station of 8: mean 10, var 17.5.
     */
    @Test
    void pumpsOfAStationIndependent() {
        List<Spots.Pump> pumps = Spots.pumps("gas_station");
        int n = 200_000;
        double sum = 0;
        double sq = 0;
        for (int i = 0; i < n; i++) {
            long seed = Draw.mix(i * 0x9E3779B97F4A7C15L + 11);
            int k = 0;
            for (Spots.Pump p : pumps) {
                k += Draw.buckets(seed, p.key());
            }
            sum += k;
            sq += (double) k * k;
        }
        double mean = sum / n;
        double var = sq / n - mean * mean;
        System.out.printf("buckets per gas station: mean %.3f var %.3f%n", mean, var);
        assertEquals(10.0, mean, 0.03);
        assertEquals(17.5, var, 0.2);
    }

    @Test
    void pumpKeysNeverBayKeys() {
        for (Map.Entry<String, List<Spots.Pump>> e : Spots.allPumps().entrySet()) {
            for (Spots.Pump p : e.getValue()) {
                for (Spots.Bay b : Spots.of(e.getKey())) {
                    assertTrue(p.key() != b.key());
                }
            }
        }
    }
}
