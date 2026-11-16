package com.amcvehicles;

import java.util.List;

/**
 * The cars that spawn abandoned, with what the mod needs of each (the user's list, 16 Nov 2026: the Official Pack's
 * Mercedes 230, Mustang 69 and Scout; the Official Automobile Pack's Cagouille, Camaro, Escargot, Highwayman and
 * Station Wagon). The numbers come from the pack jars, printed by
 * {@code python3 -I tools/car_info.py <pack.jar> <pack id> <system names...>}. No Minecraft classes: tested with JUnit.
 */
public final class Cars {
    /**
     * A car model.
     *
     * @param key       name in the commands and the logs
     * @param civilian  its civilian colours (subNames), drawn with equal chance outside the police station
     * @param police    its police colours: only in the police station
     * @param rear      the body behind the origin (the rear axle), blocks (the .obj model)
     * @param front     the body ahead of the origin
     * @param halfWidth half the width of the body
     * @param lift      height of the origin over the floor: the car rests on the support blocks of its model
     *                  ({@code cube_fl/fr/rl/rr}, shown when the wheel of that corner is missing), bottom at -lift
     * @param top       the highest point of the model over the origin
     * @param doors     the variables of its side doors, each open or closed (drawn separately)
     * @param trunk     the variable of its trunk (the Scout's tailgate), or null (the Mustang has none)
     */
    public record Model(String key, String pack, String system, List<String> civilian, List<String> police,
                        double rear, double front, double halfWidth, double lift, double top, List<String> doors,
                        String trunk) {
        /** The variable of the hood, the same in every car. */
        public static final String HOOD = "door_hood";

        public String id() {
            return pack + ":" + system;
        }
    }

    public static final String OFFICIAL = "mtsofficialpack";
    public static final String OAMP = "oamp";

    public static final Model MERCEDES = new Model("mercedes", OFFICIAL, "merc230",
            List.of("_blue", "_white", "_seagreen", "_maroon", "_brown", "_green", "_black", "_red", "_gray",
                    "_extravagant", "_olive", "_beige", "_silver", "_yellow", "_salmon"),
            List.of("_police", "_police2"),
            1.47164, 4.29688, 1.25030, 0.46875, 1.625, List.of("door_l", "door_r"), "door_trunk");
    public static final Model CAGOUILLE = new Model("cagouille", OAMP, "cagouille",
            List.of("_black", "_blue", "_brown", "_gray", "_joepeach", "_lightyellow", "_maroon", "_purple",
                    "_jadegreen", "_mint", "_pink", "_red", "_salmon", "_seagreen", "_skyblue", "_white", "_yellow",
                    "_pinkish"),
            List.of("_police"),
            1.5, 4.875, 1.1875, 0.4375, 1.6875, List.of("door_l", "door_r"), "door_trunk");
    public static final Model CAMARO = new Model("camaro", OAMP, "camaro",
            List.of("_white", "_gray", "_black", "_red", "_brown", "_blue", "_lime", "_seagreen", "_orange", "_yellow",
                    "_extravagant", "_maroon", "_bluestriped", "_skyblue"),
            List.of(),
            1.5675, 4.755, 1.22252, 0.5, 1.1925, List.of("door_l", "door_r"), "door_trunk");
    public static final Model ESCARGOT = new Model("escargot", OAMP, "escargot",
            List.of("_black", "_blue", "_brown", "_gray", "_joepeach", "_lightyellow", "_maroon", "_mascar", "_mint",
                    "_pink", "_red", "_salmon", "_seagreen", "_skyblue", "_white", "_yellow"),
            List.of(),
            1.75072, 4.5998, 1.22174, 0.46875, 1.50586, List.of("door_l", "door_r"), "door_trunk");
    public static final Model HIGHWAYMAN = new Model("highwayman", OAMP, "highwayman",
            List.of("_lightblue", "_red", "_purple", "_white", "_blue", "_pink", "_jadegreen", "_seagreen", "_salmon"),
            List.of(),
            1.3125, 4.25, 1.125, 0.4375, 1.25, List.of("door_left", "door_right"), "door_trunk");
    public static final Model STATION_WAGON = new Model("station_wagon", OAMP, "stationmerc",
            List.of("_blue", "_white", "_seagreen", "_maroon", "_brown", "_green", "_black", "_red", "_gray", "_olive",
                    "_beige", "_silver", "_yellow", "_salmon"),
            List.of(),
            1.53286, 4.28353, 1.24807, 0.46875, 1.40625, List.of("door_fl", "door_fr", "door_rl", "door_rr"),
            "door_trunk");
    public static final Model MUSTANG = new Model("mustang", OFFICIAL, "fordmustang69",
            List.of("_red", "_yellow", "_black", "_silver", "_extravagant", "_orange", "_white", "_seagreen",
                    "_bluestripe", "_redstripe", "_whitestripe", "_lime"),
            List.of("_police"),
            1.64338, 4.56495, 1.48632, 0.4375, 1.8125, List.of("door_l", "door_r"), null);
    public static final Model SCOUT = new Model("scout", OFFICIAL, "scout",
            List.of("_blue", "_yellow", "_white", "_red", "_olive", "_black", "_gray", "_gold", "_tan", "_orange",
                    "_brown", "_yellowwhite", "_seagreen", "_maroon"),
            List.of(),
            1.40625, 3.59375, 1.3181, 0.46875, 1.64688, List.of("door_l", "door_r"), "door_tailgate");

    /** All the models, drawn with equal chance outside the police station (the user's choice). */
    public static final List<Model> ALL = List.of(MERCEDES, CAGOUILLE, CAMARO, ESCARGOT, HIGHWAYMAN, STATION_WAGON,
            MUSTANG, SCOUT);
    /** The models with a police colour, drawn with equal chance in the police station (the user's choice). */
    public static final List<Model> POLICE = ALL.stream().filter(m -> !m.police().isEmpty()).toList();

    /** Free blocks a car needs over the floor: the tallest car on its support blocks (the Mustang, 2.25). */
    public static final int HEADROOM = (int) Math.ceil(ALL.stream().mapToDouble(m -> m.lift() + m.top()).max()
            .orElse(2));

    private Cars() {
    }

    /** The model of a key, or null. */
    public static Model byKey(String key) {
        for (Model m : ALL) {
            if (m.key().equals(key)) {
                return m;
            }
        }
        return null;
    }
}
