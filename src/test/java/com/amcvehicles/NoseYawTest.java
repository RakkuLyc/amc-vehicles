package com.amcvehicles;

import minecrafttransportsimulator.baseclasses.Point3D;
import minecrafttransportsimulator.baseclasses.RotationMatrix;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The nose of the car (the +Z of the model: the front axle of merc230 is at z 3.19) turned by the orientation
 * Mercedes.spawn gives IV ({@code setToAngles(0, -noseYaw, 0)}), with IV's own rotation math: it must point to the
 * Minecraft direction of noseYaw (south 0, west 90, north 180, east 270). A wrong sign would keep north and south
 * and swap east and west.
 */
class NoseYawTest {
    static Point3D nose(float noseYaw) {
        RotationMatrix m = new RotationMatrix().setToAngles(new Point3D(0.0, -noseYaw, 0.0));
        return new Point3D(0.0, 0.0, 1.0).rotate(m);
    }

    static void assertDir(float yaw, double dx, double dz) {
        Point3D p = nose(yaw);
        assertEquals(dx, p.x, 1e-6, "yaw " + yaw + " x");
        assertEquals(dz, p.z, 1e-6, "yaw " + yaw + " z");
    }

    @Test
    void noseOfEachYaw() {
        assertDir(0f, 0, 1);      // south, +Z
        assertDir(90f, -1, 0);    // west, -X
        assertDir(180f, 0, -1);   // north, -Z
        assertDir(270f, 1, 0);    // east, +X
    }

    @Test
    void placementYawAgreesWithIv() {
        int[][] dirs = {{0, 1}, {-1, 0}, {0, -1}, {1, 0}};
        for (int[] d : dirs) {
            assertDir(Placement.yaw(d[0], d[1]), d[0], d[1]);
        }
    }
}
