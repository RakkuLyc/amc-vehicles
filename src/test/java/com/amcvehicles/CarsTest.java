package com.amcvehicles;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The table of {@link Cars} against the pack jars in libs/ (read as zips, no game): every colour exists, the police
 * ones are all the pack's police colours, the door and trunk variables exist, and the lift and the top match the .obj
 * model (the bottom of the support blocks cube_* and the highest point).
 */
class CarsTest {
    static final Map<String, String> JARS = Map.of(
            Cars.OFFICIAL, "libs/MTS Official Pack-1.21.1-V29.jar",
            Cars.OAMP, "libs/OAmP-1.21.1-V3.jar");

    static String read(String pack, String path) throws IOException {
        Path jar = Path.of(JARS.get(pack));
        assertTrue(Files.exists(jar), "missing " + jar + " (copy it to libs/, see README)");
        try (ZipFile z = new ZipFile(jar.toFile())) {
            ZipEntry e = z.getEntry(path);
            assertNotNull(e, pack + ": no " + path);
            return new String(z.getInputStream(e).readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void tableMatchesThePacks() throws IOException {
        for (Cars.Model m : Cars.ALL) {
            String json = read(m.pack(), "assets/" + m.pack() + "/jsondefs/vehicles/" + m.system() + ".json")
                    .replaceAll("//[^\\n]*", "");
            List<String> subs = new ArrayList<>();
            Matcher sm = Pattern.compile("\"subName\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
            while (sm.find()) {
                subs.add(sm.group(1));
            }
            for (String c : m.civilian()) {
                assertTrue(subs.contains(c), m.key() + ": no colour " + c);
                assertTrue(!c.contains("police"), m.key() + ": " + c + " is a police colour");
            }
            List<String> police = subs.stream().filter(s -> s.contains("police")).toList();
            assertEquals(police, m.police(), m.key() + ": police colours");
            assertEquals(subs.size(), m.civilian().size() + m.police().size(), m.key() + ": every colour listed");
            for (String d : m.doors()) {
                assertTrue(json.contains("\"" + d + "\""), m.key() + ": no variable " + d);
            }
            if (m.trunk() != null) {
                assertTrue(json.contains("\"" + m.trunk() + "\""), m.key() + ": no variable " + m.trunk());
            } else {
                assertTrue(!json.contains("\"door_trunk\""), m.key() + " has a trunk after all");
            }
            assertTrue(json.contains("\"" + Cars.Model.HOOD + "\""), m.key() + ": no hood");

            String obj = read(m.pack(), "assets/" + m.pack() + "/objmodels/vehicles/" + m.system() + ".obj");
            double cubeMin = Double.MAX_VALUE;
            double top = -Double.MAX_VALUE;
            String cur = "";
            for (String line : obj.split("\n")) {
                if (line.startsWith("o ") || line.startsWith("g ")) {
                    cur = line.substring(2).trim();
                } else if (line.startsWith("v ")) {
                    double y = Double.parseDouble(line.trim().split("\\s+")[2]);
                    top = Math.max(top, y);
                    if (cur.toLowerCase().startsWith("cube")) {
                        cubeMin = Math.min(cubeMin, y);
                    }
                }
            }
            assertEquals(m.lift(), -cubeMin, 1e-4, m.key() + ": lift = bottom of the support blocks");
            assertEquals(m.top(), top, 1e-4, m.key() + ": top");
            assertTrue(m.lift() + m.top() <= Cars.HEADROOM, m.key() + " taller than the headroom");
        }
    }

    @Test
    void policeModels() {
        assertEquals(List.of("mercedes", "cagouille", "mustang"), Cars.POLICE.stream().map(Cars.Model::key).toList());
        assertEquals(3, Cars.HEADROOM);
    }
}
