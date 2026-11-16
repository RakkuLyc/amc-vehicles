package com.amcvehicles;

import com.abandonedmoderncity.core.City;
import com.abandonedmoderncity.core.Lot;
import com.abandonedmoderncity.core.Params;
import com.abandonedmoderncity.core.Terrain;
import com.abandonedmoderncity.core.WorldPlan;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The draw of the cars, and the projection of cars per city with AMC's real planner (flat terrain). */
class DrawTest {
    /** Flat dry land: every cell where AMC's city chance says so has a city. */
    record Flat(int level) implements Terrain {
        @Override
        public int seaLevel() {
            return 63;
        }

        @Override
        public int ground(int x, int z) {
            return level;
        }

        @Override
        public boolean water(int x, int z) {
            return false;
        }
    }

    @Test
    void sameInputSameCar() {
        for (int bay = 0; bay < 200; bay++) {
            assertEquals(Draw.car(123456789L, bay, false), Draw.car(123456789L, bay, false));
        }
    }

    @Test
    void chancesModelsAndColours() {
        int n = 4_000_000;
        int cars = 0;
        int hood = 0;
        int trunk = 0;
        int withTrunk = 0;
        int doors = 0;
        int doorsOpen = 0;
        Map<String, Integer> models = new TreeMap<>();
        Map<String, Integer> colors = new HashMap<>();
        for (int i = 0; i < n; i++) {
            Draw.Car c = Draw.car(i * 7919L + 17, i % 40, false);
            if (c == null) {
                continue;
            }
            cars++;
            Cars.Model m = c.model();
            models.merge(m.key(), 1, Integer::sum);
            colors.merge(m.key() + c.color(), 1, Integer::sum);
            assertTrue(m.civilian().contains(c.color()), "a police or unknown colour outside the police station: "
                    + m.key() + c.color());
            hood += c.hoodOpen() ? 1 : 0;
            if (m.trunk() != null) {
                withTrunk++;
                trunk += c.trunkOpen() ? 1 : 0;
            } else {
                assertFalse(c.trunkOpen(), m.key() + " has no trunk");
            }
            assertEquals(m.doors().size(), c.doorsOpen().size());
            for (boolean d : c.doorsOpen()) {
                doors++;
                doorsOpen += d ? 1 : 0;
            }
        }
        double p = (double) cars / n;
        System.out.printf("draw: %d cars in %d bays = %.4f; models %s; hood %.3f trunk %.3f doors %.3f%n", cars, n, p,
                models, (double) hood / cars, (double) trunk / withTrunk, (double) doorsOpen / doors);
        assertEquals(Draw.CHANCE_PER_BAY, p, 0.001);
        assertEquals(Cars.ALL.size(), models.size());
        for (Cars.Model m : Cars.ALL) {
            int k = models.get(m.key());
            assertEquals(1.0 / Cars.ALL.size(), (double) k / cars, 0.006, m.key());
            for (String col : m.civilian()) {
                Integer v = colors.get(m.key() + col);
                assertNotNull(v, m.key() + col + " never drawn");
                assertEquals(1.0 / m.civilian().size(), (double) v / k, 0.04, m.key() + col);
            }
        }
        assertEquals(0.5, (double) hood / cars, 0.01);
        assertEquals(0.5, (double) trunk / withTrunk, 0.01);
        assertEquals(0.5, (double) doorsOpen / doors, 0.01);
    }

    @Test
    void policeOnlyPoliceModelsAndColours() {
        Random r = new Random(7);
        Map<String, Integer> seen = new TreeMap<>();
        int n = 300_000;
        for (int i = 0; i < n; i++) {
            Draw.Car c = Draw.car(what -> r.nextDouble(), true);
            assertTrue(c.model().police().contains(c.color()), c.model().key() + c.color());
            seen.merge(c.model().key() + c.color(), 1, Integer::sum);
        }
        System.out.println("police: " + seen);
        // by model: 1/3 each (Mercedes, Cagouille, Mustang); the Mercedes split between _police and _police2
        assertEquals(1.0 / 3, seen.get("cagouille_police") / (double) n, 0.01);
        assertEquals(1.0 / 3, seen.get("mustang_police") / (double) n, 0.01);
        assertEquals(1.0 / 6, seen.get("mercedes_police") / (double) n, 0.01);
        assertEquals(1.0 / 6, seen.get("mercedes_police2") / (double) n, 0.01);
        assertEquals(4, seen.size());
    }

    /**
     * Cars per city with the bays of spots.txt, AMC's real planner (flat terrain, 5 world seeds, the cities of a
     * 12 × 12 grid of cells each) and the real draw: the table shown to the user before the generation was switched on.
     */
    @Test
    void projectionPerCity() {
        long[] seeds = {20261027L, 3478477863671517581L, -3297019724594858492L, 1L, 42L};
        int cities = 0;
        int withBays = 0;
        int bays = 0;
        int cars = 0;
        Map<Integer, Integer> carsPerCity = new TreeMap<>();
        Map<String, int[]> perModel = new TreeMap<>(); // structures, bays, cars
        for (long seed : seeds) {
            WorldPlan plan = new WorldPlan(seed, Params.DEFAULT, new Flat(70));
            for (int ci = -6; ci < 6; ci++) {
                for (int cj = -6; cj < 6; cj++) {
                    City city = plan.city(ci, cj);
                    if (city == null) {
                        continue;
                    }
                    cities++;
                    int inCity = 0;
                    boolean any = false;
                    for (int bi = City.FIRST; bi <= City.LAST; bi++) {
                        for (int bj = City.FIRST; bj <= City.LAST; bj++) {
                            if (!city.isCityBlock(bi, bj)) {
                                continue;
                            }
                            for (Lot lot : city.lots(bi, bj)) {
                                if (lot.building == null) {
                                    continue;
                                }
                                var list = Spots.of(lot.building.model());
                                if (list.isEmpty()) {
                                    continue;
                                }
                                any = true;
                                boolean police = "police_station".equals(lot.building.type());
                                int[] m = perModel.computeIfAbsent(lot.building.model(), k -> new int[3]);
                                m[0]++;
                                m[1] += list.size();
                                for (int k = 0; k < list.size(); k++) {
                                    bays++;
                                    if (Draw.car(lot.seed, list.get(k).key(), police) != null) {
                                        cars++;
                                        inCity++;
                                        m[2]++;
                                    }
                                }
                            }
                        }
                    }
                    withBays += any ? 1 : 0;
                    carsPerCity.merge(inCity, 1, Integer::sum);
                }
            }
        }
        System.out.printf("projection: %d cities (5 seeds, flat), %d with bays (%.1f%%), %d bays (%.1f per city), "
                        + "%d cars (%.2f per city, %.2f%% of the bays)%n", cities, withBays, 100.0 * withBays / cities,
                bays, (double) bays / cities, cars, (double) cars / cities, 100.0 * cars / bays);
        for (Map.Entry<Integer, Integer> e : carsPerCity.entrySet()) {
            System.out.printf("  cities with %d cars: %d (%.1f%%)%n", e.getKey(), e.getValue(),
                    100.0 * e.getValue() / cities);
        }
        for (Map.Entry<String, int[]> e : perModel.entrySet()) {
            int[] m = e.getValue();
            System.out.printf("  %s: %d copies (%.2f per city), %d bays, %d cars (%.2f per copy)%n",
                    e.getKey(), m[0], (double) m[0] / cities, m[1], m[2], (double) m[2] / m[0]);
        }
        assertTrue(cities > 100);
    }
}
