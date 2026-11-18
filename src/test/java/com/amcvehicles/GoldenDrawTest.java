package com.amcvehicles;

import com.abandonedmoderncity.core.City;
import com.abandonedmoderncity.core.Lot;
import com.abandonedmoderncity.core.Params;
import com.abandonedmoderncity.core.Structures;
import com.abandonedmoderncity.core.WorldPlan;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Golden test of the draw (safety net for the 1.0.0 clean-up, 2026-11-18): the complete outcome of the draw and of the
 * placement for a fixed set of inputs, compared line by line with {@code src/test/resources/golden/draw_golden.txt}.
 * Any change of a car, colour, door, hood, trunk, bucket, position or angle fails here. Part A: the pure draw, 200 lot
 * seeds x every bay (civilian and police) x every pump. Part B: AMC's real planner (2 world seeds, flat terrain), the
 * world position and angle of every drawn car and every pump. When the reference is missing it is written and the test
 * fails once, so that a new reference is always looked at; on a difference the actual output goes to
 * {@code build/golden/draw_golden.actual.txt}.
 */
class GoldenDrawTest {
    private static final Path REFERENCE = Path.of("src/test/resources/golden/draw_golden.txt");
    private static final Path ACTUAL = Path.of("build/golden/draw_golden.actual.txt");

    static List<String> models() {
        TreeSet<String> out = new TreeSet<>();
        for (Structures.Def d : Structures.ALL) {
            out.add(d.model());
        }
        return List.copyOf(out);
    }

    static String car(Draw.Car c) {
        StringBuilder doors = new StringBuilder();
        for (boolean d : c.doorsOpen()) {
            doors.append(d ? '1' : '0');
        }
        return c.model().key() + " " + c.color() + " hood=" + (c.hoodOpen() ? 1 : 0) + " trunk=" + (c.trunkOpen() ? 1 : 0)
                + " doors=" + doors;
    }

    static List<String> output() {
        List<String> out = new ArrayList<>();
        List<String> models = models();
        // Part A: the pure draw
        for (int i = 0; i < 200; i++) {
            long seed = i * 0x9E3779B97F4A7C15L + 1;
            for (String model : models) {
                for (Spots.Bay b : Spots.of(model)) {
                    for (boolean police : new boolean[]{false, true}) {
                        Draw.Car c = Draw.car(seed, b.key(), police);
                        if (c != null) {
                            out.add("A " + seed + " " + model + " bay " + b.x0() + "," + b.y() + "," + b.z0() + " "
                                    + (police ? "pol " : "civ ") + car(c));
                        }
                    }
                }
                for (Spots.Pump p : Spots.pumps(model)) {
                    out.add("A " + seed + " " + model + " pump " + p.x() + "," + p.y() + "," + p.z() + " buckets="
                            + Draw.buckets(seed, p.key()));
                }
            }
        }
        // Part B: AMC's real planner, world positions
        int base = 70;
        for (long world : new long[]{20261027L, 42L}) {
            WorldPlan plan = new WorldPlan(world, Params.DEFAULT, new DrawTest.Flat(base));
            for (int ci = -3; ci < 3; ci++) {
                for (int cj = -3; cj < 3; cj++) {
                    City city = plan.city(ci, cj);
                    if (city == null) {
                        continue;
                    }
                    for (int bi = City.FIRST; bi <= City.LAST; bi++) {
                        for (int bj = City.FIRST; bj <= City.LAST; bj++) {
                            if (!city.isCityBlock(bi, bj)) {
                                continue;
                            }
                            for (Lot lot : city.lots(bi, bj)) {
                                if (lot.building != null) {
                                    lot(out, world, lot, base);
                                }
                            }
                        }
                    }
                }
            }
        }
        return out;
    }

    static void lot(List<String> out, long world, Lot lot, int base) {
        Structures.Def def = lot.building;
        boolean police = "police_station".equals(def.type());
        String head = "B " + world + " " + def.name() + " lot " + lot.x0 + "," + lot.z0 + " rot " + lot.rot + " seed "
                + lot.seed;
        for (Spots.Bay b : Spots.of(def.model())) {
            Draw.Car c = Draw.car(lot.seed, b.key(), police);
            if (c == null) {
                continue;
            }
            Placement.Car at = Placement.car(def, lot.x0, lot.z0, lot.rot, base, b.oriented(c.flip()), c.model());
            out.add(String.format(Locale.ROOT, "%s bay %d,%d,%d %s at %.5f %.5f %.5f yaw %.1f cells %d", head, b.x0(),
                    b.y(), b.z0(), car(c), at.x(), at.y(), at.z(), at.noseYaw(), at.cells().size()));
        }
        for (Spots.Pump p : Spots.pumps(def.model())) {
            Placement.PumpAt at = Placement.pump(def, lot.x0, lot.z0, lot.rot, base, p);
            out.add(String.format(Locale.ROOT, "%s pump %d,%d,%d at %d %d %d yaw %.1f buckets=%d", head, p.x(), p.y(),
                    p.z(), at.x(), at.y(), at.z(), at.yaw(), Draw.buckets(lot.seed, p.key())));
        }
    }

    @Test
    void drawAndPlacementMatchTheReference() throws IOException, NoSuchAlgorithmException {
        List<String> actual = output();
        String text = String.join("\n", actual) + "\n";
        byte[] sha = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
        long a = actual.stream().filter(l -> l.startsWith("A ") && l.contains(" bay ")).count();
        long b = actual.stream().filter(l -> l.startsWith("B ") && l.contains(" bay ")).count();
        System.out.printf("golden: %d lines (A cars %d, B cars %d), sha256 %s%n", actual.size(), a, b,
                HexFormat.of().formatHex(sha));
        if (!Files.exists(REFERENCE)) {
            Files.createDirectories(REFERENCE.getParent());
            Files.writeString(REFERENCE, text, StandardCharsets.UTF_8);
            fail("reference written to " + REFERENCE + ": look at it and run again");
        }
        List<String> expected = Files.readAllLines(REFERENCE, StandardCharsets.UTF_8);
        if (expected.equals(actual)) {
            return;
        }
        Files.createDirectories(ACTUAL.getParent());
        Files.writeString(ACTUAL, text, StandardCharsets.UTF_8);
        for (int i = 0; i < Math.max(expected.size(), actual.size()); i++) {
            String e = i < expected.size() ? expected.get(i) : "(none)";
            String g = i < actual.size() ? actual.get(i) : "(none)";
            if (!e.equals(g)) {
                fail("draw changed at line " + (i + 1) + "\n expected: " + e + "\n actual:   " + g + "\n(" + expected.size()
                        + " lines expected, " + actual.size() + " actual; full output in " + ACTUAL + ")");
            }
        }
    }
}
