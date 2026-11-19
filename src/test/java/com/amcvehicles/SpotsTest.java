package com.amcvehicles;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A malformed line of spots.txt stops the game early with its line number; good lines are read. */
class SpotsTest {
    static String error(String line) {
        return assertThrows(IllegalStateException.class,
                () -> Spots.parseLine(line, 42, new HashMap<>(), new HashMap<>())).getMessage();
    }

    @Test
    void malformedLinesNameTheirLine() {
        for (String bad : new String[]{"m bay 1 0 2 3 X", "m bay 1 0 2 3 4 Q", "m bay one 0 2 3 4 N",
                "m pump 1 2 three S", "m pump 1 2 3 X", "m parking 1 0 2 3 4 N", "m bay 5 0 2 3 4 N"}) {
            assertTrue(error(bad).startsWith("spots.txt line 42: "), bad + " -> " + error(bad));
        }
    }

    @Test
    void goodLinesAreRead() {
        Map<String, List<Spots.Bay>> bays = new HashMap<>();
        Map<String, List<Spots.Pump>> pumps = new HashMap<>();
        Spots.parseLine("m bay 1 0 2 3 9 S", 1, bays, pumps);
        Spots.parseLine("m pump 15 1 9 N", 2, bays, pumps);
        assertEquals(List.of(new Spots.Bay(1, 0, 2, 3, 9, 'S')), bays.get("m"));
        assertEquals(List.of(new Spots.Pump(15, 1, 9, 'N')), pumps.get("m"));
    }
}
