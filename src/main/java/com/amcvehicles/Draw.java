package com.amcvehicles;

import java.util.List;

/**
 * The draw of the cars: a pure function of the copy of the structure (the seed AMC gives its lot, itself from the world
 * seed) and of the bay, never of the order in which chunks load. No Minecraft classes: tested with JUnit.
 */
public final class Draw {
    /** Chance of a car in each bay (the user's choice, 16 Nov 2026: 3% per bay). */
    public static final double CHANCE_PER_BAY = 0.03;

    /** The 15 civilian colours (the subNames of merc230.json), drawn with equal chance outside the police station. */
    public static final List<String> CIVILIAN = List.of("_blue", "_white", "_seagreen", "_maroon", "_brown", "_green",
            "_black", "_red", "_gray", "_extravagant", "_olive", "_beige", "_silver", "_yellow", "_salmon");
    /** The 2 police colours: only in the police station (and there only they). */
    public static final List<String> POLICE = List.of("_police", "_police2");
    /** Chance of the hood being open, of the trunk, and of each side door (each drawn separately). */
    public static final double HOOD_OPEN_CHANCE = 0.5;
    public static final double TRUNK_OPEN_CHANCE = 0.5;
    public static final double DOOR_OPEN_CHANCE = 0.5;

    private static final long SALT = 0x414D4356L; // "AMCV"

    private Draw() {
    }

    /** A drawn car: colour (a subName of merc230) and what is open. */
    public record Car(String color, boolean hoodOpen, boolean trunkOpen, boolean doorLeftOpen, boolean doorRightOpen) {
    }

    /**
     * The car of the bay of key {@code bay} ({@link Spots.Bay#key}: its position in the template) of the copy of seed
     * {@code lotSeed}, or null when the bay is empty.
     */
    public static Car car(long lotSeed, long bay, boolean police) {
        if (unit(lotSeed, bay, 0) >= CHANCE_PER_BAY) {
            return null;
        }
        List<String> colors = police ? POLICE : CIVILIAN;
        String color = colors.get((int) Math.floor(unit(lotSeed, bay, 1) * colors.size()));
        return new Car(color,
                unit(lotSeed, bay, 2) < HOOD_OPEN_CHANCE,
                unit(lotSeed, bay, 3) < TRUNK_OPEN_CHANCE,
                unit(lotSeed, bay, 4) < DOOR_OPEN_CHANCE,
                unit(lotSeed, bay, 5) < DOOR_OPEN_CHANCE);
    }

    /** A number in [0, 1) from the lot seed, the bay and what is drawn. */
    static double unit(long lotSeed, long bay, int what) {
        long h = mix(lotSeed ^ SALT);
        h = mix(h + bay * 0x9E3779B97F4A7C15L);
        h = mix(h + what * 0xC2B2AE3D27D4EB4FL);
        return (h >>> 11) * 0x1.0p-53;
    }

    /** SplitMix64 finaliser. */
    static long mix(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }
}
