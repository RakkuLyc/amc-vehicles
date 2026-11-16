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
public final class Spots {
    /**
     * A bay: the cells x0..x1, z0..z1 (inclusive) of the floor layer y of the template; the nose of the car points to
     * {@code facing} in the template: 'N' (-Z), 'S' (+Z), 'E' (+X) or 'W' (-X); or 'X' / 'Z': along that axis, the
     * way drawn per car (gas stations, where no painted bay says how a car parks).
     */
    public record Bay(int x0, int y, int z0, int x1, int z1, char facing) {
        /**
         * The key of the bay in the draw: its corner and floor in the template, so that adding or reordering bays of a
         * model in spots.txt does not change the draw of the others.
         */
        public long key() {
            return ((long) (x0 & 0xFFFF) << 32) | ((long) (y & 0xFFFF) << 16) | (z0 & 0xFFFF);
        }

        /** Whether the way of the nose is drawn ('X' or 'Z'). */
        public boolean drawnFacing() {
            return facing == 'X' || facing == 'Z';
        }

        /** Whether the car runs along z (N, S or Z). */
        public boolean alongZ() {
            return facing == 'N' || facing == 'S' || facing == 'Z';
        }

        /** This bay with a fixed nose: for 'X' east or (flip) west, for 'Z' south or (flip) north; else itself. */
        public Bay oriented(boolean flip) {
            return switch (facing) {
                case 'X' -> new Bay(x0, y, z0, x1, z1, flip ? 'W' : 'E');
                case 'Z' -> new Bay(x0, y, z0, x1, z1, flip ? 'N' : 'S');
                default -> this;
            };
        }

        /** Unit vector {dx, dz} of the nose in the template (a fixed facing: see {@link #oriented}). */
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

    private static final Map<String, List<Bay>> BY_MODEL = load();

    private Spots() {
    }

    /** The bays of a model (empty when it has none). */
    public static List<Bay> of(String model) {
        return BY_MODEL.getOrDefault(model, List.of());
    }

    public static Map<String, List<Bay>> all() {
        return BY_MODEL;
    }

    private static Map<String, List<Bay>> load() {
        Map<String, List<Bay>> out = new HashMap<>();
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
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] f = line.split("\\s+");
                if (f.length != 8 || !f[1].equals("bay") || f[7].length() != 1 || "NSEWXZ".indexOf(f[7].charAt(0)) < 0) {
                    throw new IllegalStateException("spots.txt line " + n + ": " + line);
                }
                Bay b = new Bay(Integer.parseInt(f[2]), Integer.parseInt(f[3]), Integer.parseInt(f[4]),
                        Integer.parseInt(f[5]), Integer.parseInt(f[6]), f[7].charAt(0));
                if (b.x1() < b.x0() || b.z1() < b.z0()) {
                    throw new IllegalStateException("spots.txt line " + n + ": empty bay");
                }
                out.computeIfAbsent(f[0], k -> new ArrayList<>()).add(b);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        out.replaceAll((k, v) -> List.copyOf(v));
        return Map.copyOf(out);
    }
}
