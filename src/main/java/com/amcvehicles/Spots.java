package com.amcvehicles;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The parking bays of the AMC structures ({@code amcvehicles/spots.txt}), by model, in template coordinates of the
 * model's .nbt. No Minecraft classes: tested with JUnit.
 */
final class Spots {
    /**
     * A bay: the cells x0..x1, z0..z1 (inclusive) of the floor layer y of the template; the nose of the car points to
     * {@code facing} in the template: 'N' (-Z), 'S' (+Z), 'E' (+X) or 'W' (-X).
     */
    public record Bay(int x0, int y, int z0, int x1, int z1, char facing) {
        /**
         * The key of the bay in the draw: its corner and floor in the template, so that adding or reordering bays of a
         * model in spots.txt does not change the draw of the others.
         */
        public long key() {
            return ((long) (x0 & 0xFFFF) << 32) | ((long) (y & 0xFFFF) << 16) | (z0 & 0xFFFF);
        }

        /** Whether the car runs along z (N or S). */
        public boolean alongZ() {
            return facing == 'N' || facing == 'S';
        }

        /** Unit vector {dx, dz} of the nose in the template. */
        public int[] dir() {
            return switch (facing) {
                case 'N' -> new int[]{0, -1};
                case 'S' -> new int[]{0, 1};
                case 'E' -> new int[]{1, 0};
                case 'W' -> new int[]{-1, 0};
                default -> throw new IllegalStateException("facing " + facing);
            };
        }
    }

    /**
     * A fuel pump of a gas station (stage 6): it stands in the cell x, y, z of the template (y = the layer over the
     * floor), its front to {@code facing} ('N', 'S', 'E' or 'W').
     */
    public record Pump(int x, int y, int z, char facing) {
        /** The key of the pump in the draw (its position; tagged so it never equals the key of a bay). */
        public long key() {
            return (1L << 48) | ((long) (x & 0xFFFF) << 32) | ((long) (y & 0xFFFF) << 16) | (z & 0xFFFF);
        }

        public int[] dir() {
            return new Bay(x, y, z, x, z, facing).dir();
        }
    }

    private static final Map<String, List<Bay>> BY_MODEL = new HashMap<>();
    private static final Map<String, List<Pump>> PUMPS = new HashMap<>();

    static {
        load();
    }

    private Spots() {
    }

    /** The bays of a model (empty when it has none). */
    public static List<Bay> of(String model) {
        return BY_MODEL.getOrDefault(model, List.of());
    }

    public static Map<String, List<Bay>> all() {
        return BY_MODEL;
    }

    /** The fuel pumps of a model (empty when it has none). */
    public static List<Pump> pumps(String model) {
        return PUMPS.getOrDefault(model, List.of());
    }

    public static Map<String, List<Pump>> allPumps() {
        return PUMPS;
    }

    private static void load() {
        Map<String, List<Bay>> out = new HashMap<>();
        Map<String, List<Pump>> pumps = new HashMap<>();
        try (InputStream in = Spots.class.getResourceAsStream("/amcvehicles/spots.txt")) {
            if (in == null) {
                throw new IllegalStateException("amcvehicles/spots.txt missing from the jar");
            }
            BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            int n = 0;
            while ((line = r.readLine()) != null) {
                n++;
                line = line.strip();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    parseLine(line, n, out, pumps);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        out.forEach((k, v) -> BY_MODEL.put(k, List.copyOf(v)));
        pumps.forEach((k, v) -> PUMPS.put(k, List.copyOf(v)));
    }

    /** Whether a field is one of the four facings. */
    private static boolean isFacing(String f) {
        return f.length() == 1 && "NSEW".indexOf(f.charAt(0)) >= 0;
    }

    /**
     * One line of spots.txt ({@code <model> pump <x> <y> <z> <facing>} or
     * {@code <model> bay <x0> <y> <z0> <x1> <z1> <facing>}) into {@code bays} or {@code pumps}; a malformed line stops
     * the game with its number.
     */
    private static void parseLine(String line, int n, Map<String, List<Bay>> bays, Map<String, List<Pump>> pumps) {
        String[] f = line.split("\\s+");
        if (f.length == 6 && f[1].equals("pump") && isFacing(f[5])) {
            pumps.computeIfAbsent(f[0], k -> new ArrayList<>()).add(new Pump(Integer.parseInt(f[2]),
                    Integer.parseInt(f[3]), Integer.parseInt(f[4]), f[5].charAt(0)));
            return;
        }
        if (f.length != 8 || !f[1].equals("bay") || !isFacing(f[7])) {
            throw new IllegalStateException("spots.txt line " + n + ": " + line);
        }
        Bay b = new Bay(Integer.parseInt(f[2]), Integer.parseInt(f[3]), Integer.parseInt(f[4]),
                Integer.parseInt(f[5]), Integer.parseInt(f[6]), f[7].charAt(0));
        if (b.x1() < b.x0() || b.z1() < b.z0()) {
            throw new IllegalStateException("spots.txt line " + n + ": empty bay");
        }
        bays.computeIfAbsent(f[0], k -> new ArrayList<>()).add(b);
    }
}
