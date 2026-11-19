package com.amcvehicles;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntToDoubleFunction;

/**
 * The draw of the cars: a pure function of the copy of the structure (the seed AMC gives its lot, itself from the world
 * seed) and of the bay, never of the order in which chunks load. No Minecraft classes: tested with JUnit.
 */
final class Draw {
    /** Chance of a car in each bay (the user's choice: 3% on 2026-11-16, 9% on 2026-11-17). */
    public static final double CHANCE_PER_BAY = 0.09;
    /** Chance of the hood being open, of the trunk, and of each side door (each drawn separately). */
    public static final double HOOD_OPEN_CHANCE = 0.5;
    public static final double TRUNK_OPEN_CHANCE = 0.5;
    public static final double DOOR_OPEN_CHANCE = 0.5;
    /**
     * Lava in a fuel pump (the user's choice, 0.6.2): {@link #PUMP_EMPTY_CHANCE} empty, else 1 to
     * {@link #MAX_BUCKETS} buckets with the same chance (1/8 each since 0.7.13, when the user raised 3 to 4; 1/6 each
     * with 3). History: 0 to 3 the same chance (first choice), then 70% empty and 30% with 1 bucket (0.6.0, after the
     * balance of 2026-11-16).
     */
    public static final double PUMP_EMPTY_CHANCE = 0.5;
    public static final int MAX_BUCKETS = 4;

    private static final long SALT = 0x414D4356L; // "AMCV"
    /**
     * What each number of the draw decides (the indices of {@link #unit}): 0 whether the bay has a car (unchanged since
     * stage 3), 1 the colour, 2 the hood, 3 the trunk, 4 and 5 the first two doors, 7 the model (stage 5), 8 and 9 the
     * rear doors of the Station Wagon. Index 6 drew the way of the nose of a bay with a drawn facing until 0.9.0, when
     * that unused feature was removed; it stays free so the others keep their numbers (and the same draw).
     */
    static final int CAR = 0, COLOR = 1, HOOD = 2, TRUNK = 3, DOOR_A = 4, DOOR_B = 5, MODEL = 7, DOOR_C = 8, DOOR_D = 9;
    private static final int[] DOORS = {DOOR_A, DOOR_B, DOOR_C, DOOR_D};

    private Draw() {
    }

    /**
     * A drawn car: its model and colour (a subName) and what is open ({@code doorsOpen} in the order of
     * {@link Cars.Model#doors}; {@code trunkOpen} is false for a car without a trunk).
     */
    public record Car(Cars.Model model, String color, boolean hoodOpen, boolean trunkOpen, List<Boolean> doorsOpen) {
    }

    /**
     * The car of the bay of key {@code bay} ({@link Spots.Bay#key}: its position in the template) of the copy of seed
     * {@code lotSeed}, or null when the bay is empty.
     */
    public static Car car(long lotSeed, long bay, boolean police) {
        if (unit(lotSeed, bay, CAR) >= CHANCE_PER_BAY) {
            return null;
        }
        return car(what -> unit(lotSeed, bay, what), police);
    }

    /** A car drawn with any source of numbers in [0, 1), one per index of {@link #unit} (the debug commands). */
    public static Car car(IntToDoubleFunction u, boolean police) {
        List<Cars.Model> models = police ? Cars.POLICE : Cars.ALL;
        Cars.Model m = models.get(pick(u.applyAsDouble(MODEL), models.size()));
        return car(u, m, police);
    }

    /** A car of model {@code m} drawn with any source of numbers (its colour among the police ones if police). */
    public static Car car(IntToDoubleFunction u, Cars.Model m, boolean police) {
        List<String> colors = police ? m.police() : m.civilian();
        String color = colors.get(pick(u.applyAsDouble(COLOR), colors.size()));
        List<Boolean> doors = new ArrayList<>();
        for (int k = 0; k < m.doors().size(); k++) {
            doors.add(u.applyAsDouble(DOORS[k]) < DOOR_OPEN_CHANCE);
        }
        return new Car(m, color,
                u.applyAsDouble(HOOD) < HOOD_OPEN_CHANCE,
                m.trunk() != null && u.applyAsDouble(TRUNK) < TRUNK_OPEN_CHANCE,
                List.copyOf(doors));
    }

    /** Buckets of lava in a fuel pump, from the copy and the pump ({@link Spots.Pump#key}). */
    public static int buckets(long lotSeed, long pumpKey) {
        return buckets(unit(lotSeed, pumpKey, CAR), unit(lotSeed, pumpKey, COLOR));
    }

    /** From two numbers in [0, 1): whether the pump has lava, then how many buckets. */
    public static int buckets(double has, double amount) {
        return has < PUMP_EMPTY_CHANCE ? 0 : 1 + pick(amount, MAX_BUCKETS);
    }

    static int pick(double u, int n) {
        return Math.min(n - 1, (int) Math.floor(u * n));
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
