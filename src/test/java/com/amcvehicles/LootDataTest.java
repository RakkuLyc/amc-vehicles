package com.amcvehicles;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The loot data written by tools/gen_loot.py (src/main/resources/data) against the pack jars in libs/. */
class LootDataTest {
    static final Path DATA = Path.of("src/main/resources/data");
    static final Path TABLES = DATA.resolve("amcvehicles/loot_table");
    static final Path MODS = DATA.resolve("amcvehicles/loot_modifiers");

    static String read(Path p) throws IOException {
        return Files.readString(p, StandardCharsets.UTF_8);
    }

    static List<Path> files(Path dir) throws IOException {
        try (Stream<Path> s = Files.walk(dir)) {
            return s.filter(f -> f.toString().endsWith(".json")).sorted().collect(Collectors.toList());
        }
    }

    /** Item ids that exist in the jars: the item model assets/mts/models/item/<pack>.<name>.json. */
    static Set<String> itemsInJars() throws IOException {
        Set<String> have = new HashSet<>();
        // the IV base (the jerrycan, the jumper pack) and the two packs
        for (String jar : List.of("libs/Immersive Vehicles-1.21.1-24.0.0.jar", CarsTest.JARS.get(Cars.OFFICIAL),
                CarsTest.JARS.get(Cars.OAMP))) {
            try (ZipFile z = new ZipFile(jar)) {
                z.stream().forEach(e -> {
                    String n = e.getName();
                    if (n.startsWith("assets/mts/models/item/") && n.endsWith(".json")) {
                        have.add("mts:" + n.substring("assets/mts/models/item/".length(), n.length() - 5));
                    }
                });
            }
        }
        return have;
    }

    @Test
    void everyItemExistsAndNothingExcluded() throws IOException {
        Set<String> have = itemsInJars();
        Pattern p = Pattern.compile("\"name\": \"([^\"]+)\"");
        int n = 0;
        for (Path f : files(TABLES)) {
            Matcher m = p.matcher(read(f));
            while (m.find()) {
                String id = m.group(1);
                assertTrue(have.contains(id), f + ": no item " + id);
                assertFalse(id.contains("police"), "police item outside the police station: " + id);
                assertFalse(id.contains("legacy") || id.contains("huge") || id.contains("harness"), id);
                n++;
            }
        }
        assertTrue(n >= 50, "items found: " + n);
        System.out.println("loot: " + n + " item entries, all in the jars");
    }

    @Test
    void referencedTablesExist() throws IOException {
        Pattern p = Pattern.compile("\"(?:value|table)\": \"amcvehicles:([^\"]+)\"");
        for (Path f : Stream.concat(files(TABLES).stream(), files(MODS).stream()).collect(Collectors.toList())) {
            Matcher m = p.matcher(read(f));
            while (m.find()) {
                assertTrue(Files.exists(TABLES.resolve(m.group(1) + ".json")), f + ": no table " + m.group(1));
            }
        }
    }

    @Test
    void everyPoolAddsUpToTenThousand() throws IOException {
        Pattern w = Pattern.compile("\"weight\": (\\d+)");
        for (Path f : files(TABLES)) {
            String s = read(f);
            if (f.toString().contains("/pick/")) {
                continue; // pick tables: equal weights, no empty entry
            }
            // each pool of the generator is {item weight W, empty weight 10000 - W}
            Matcher m = w.matcher(s);
            int a = -1;
            int pools = 0;
            while (m.find()) {
                int v = Integer.parseInt(m.group(1));
                if (a < 0) {
                    a = v;
                } else {
                    assertEquals(10000, a + v, f + ": a pool does not add up to 10000");
                    a = -1;
                    pools++;
                }
            }
            assertEquals(-1, a, f.toString());
            assertTrue(pools >= 2, f.toString());
        }
    }

    @Test
    void modifiersAreRegisteredAndPointAtTheAmcTables() throws IOException {
        Set<String> listed = new HashSet<>();
        Matcher m = Pattern.compile("\"amcvehicles:([^\"]+)\"").matcher(
                read(DATA.resolve("neoforge/loot_modifiers/global_loot_modifiers.json")));
        while (m.find()) {
            listed.add(m.group(1));
        }
        Set<String> onDisk = files(MODS).stream().map(f -> f.getFileName().toString().replace(".json", ""))
                .collect(Collectors.toSet());
        assertEquals(onDisk, listed);
        assertEquals(8, onDisk.size());
        for (Path f : files(MODS)) {
            String s = read(f);
            String name = f.getFileName().toString().replace(".json", "");
            assertTrue(s.contains("\"loot_table_id\": \"abandonedmoderncity:chests/"
                    + name.replaceFirst("_(?=(chest|barrel))", "/") + "\""), f.toString());
            assertTrue(s.contains("neoforge:add_table"), f.toString());
        }
    }
}
