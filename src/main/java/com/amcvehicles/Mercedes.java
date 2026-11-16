package com.amcvehicles;

import mcinterface1211.WrapperWorld;
import minecrafttransportsimulator.baseclasses.Point3D;
import minecrafttransportsimulator.entities.instances.APart;
import minecrafttransportsimulator.entities.instances.EntityVehicleF_Physics;
import minecrafttransportsimulator.entities.instances.PartEngine;
import minecrafttransportsimulator.items.components.AItemPack;
import minecrafttransportsimulator.items.instances.ItemInstrument;
import minecrafttransportsimulator.items.instances.ItemVehicle;
import minecrafttransportsimulator.jsondefs.JSONPart;
import minecrafttransportsimulator.mcinterface.AWrapperWorld;
import minecrafttransportsimulator.mcinterface.InterfaceManager;
import minecrafttransportsimulator.packloading.PackParser;
import minecrafttransportsimulator.systems.ConfigSystem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The abandoned Mercedes 230 of the MTS Official Pack ({@code mtsofficialpack:merc230}): always without engine,
 * wheels and seats, 10% of fuel, a speedometer (km/h) and a fuel gauge; hood, trunk and each side door open or
 * closed, drawn.
 */
public final class Mercedes {
    public static final String PACK = "mtsofficialpack";
    public static final String SYSTEM = "merc230";
    /** The colours and the chances live in {@link Draw} (no Minecraft classes, so the tests can load them). */
    public static final List<String> CIVILIAN = Draw.CIVILIAN;
    public static final List<String> POLICE = Draw.POLICE;
    /** Fuel left in the tank: 10% of {@code motorized.fuelCapacity} (15,000 mB → 1,500 mB). */
    public static final double FUEL_FRACTION = 0.10;
    public static final double HOOD_OPEN_CHANCE = Draw.HOOD_OPEN_CHANCE;
    public static final double TRUNK_OPEN_CHANCE = Draw.TRUNK_OPEN_CHANCE;
    public static final double DOOR_OPEN_CHANCE = Draw.DOOR_OPEN_CHANCE;

    /** The engine the pack gives the car (its fuel type, gasoline, picks the fluid of the tank). */
    static final String ENGINE = "enginemercedesm102";
    /** Instruments: the car has 3 slots on the dashboard (0 left, 1 middle and larger, 2 right). */
    static final String SPEEDOMETER = "instrument_car_spedometer_kph";
    static final int SPEEDOMETER_SLOT = 1;
    static final String FUEL_GAUGE = "instrument_car_fuelqty";
    static final int FUEL_GAUGE_SLOT = 2;

    private Mercedes() {
    }

    /** What was spawned. */
    public record Result(String color, boolean hoodOpen, boolean trunkOpen, boolean doorLeftOpen,
                         boolean doorRightOpen, String fluid, double fuel,
                         double fuelCapacity, int enginesRemoved, List<String> parts) {
    }

    /** A colour drawn among the civilian ones, or among the police ones. */
    public static String drawColor(RandomSource r, boolean police) {
        List<String> l = police ? POLICE : CIVILIAN;
        return l.get(r.nextInt(l.size()));
    }

    /**
     * Spawns the car with the rear axle (the origin of the model) at (x, y, z), its nose pointing to the Minecraft
     * yaw {@code noseYaw} (0 = south, +Z; 90 = west; 180 = north; 270 = east).
     */
    public static Result spawn(ServerLevel level, double x, double y, double z, float noseYaw, String color,
                               boolean hoodOpen, boolean trunkOpen, boolean doorLeftOpen, boolean doorRightOpen) {
        ItemVehicle item = PackParser.getItem(PACK, SYSTEM, color);
        if (item == null) {
            throw new IllegalStateException("Immersive Vehicles has no item " + PACK + ":" + SYSTEM + color);
        }
        AWrapperWorld world = WrapperWorld.getWrapperFor(level);
        // data null = a new car, as from a fresh item: the pack's initialVariables (door_l, door_r, door_hood = open,
        // set again below) and default instruments; the parts come with addPartsPostAddition below
        EntityVehicleF_Physics v = new EntityVehicleF_Physics(world, null, item, null);
        v.position.set(x, y, z);
        v.prevPosition.set(v.position);
        // IV turns by its own yaw, which is Minecraft's with the sign flipped (WrapperEntity.getYaw)
        v.orientation.setToAngles(new Point3D(0.0, -noseYaw, 0.0));
        v.prevOrientation.set(v.orientation);
        v.motion.set(0.0, 0.0, 0.0);
        v.prevMotion.set(v.motion);

        v.getOrCreateVariable("door_hood").setTo(hoodOpen ? 1.0 : 0.0, false);
        v.getOrCreateVariable("door_trunk").setTo(trunkOpen ? 1.0 : 0.0, false);
        // door_l is the door on the +x side of the model, the left of someone sitting in the car
        v.getOrCreateVariable("door_l").setTo(doorLeftOpen ? 1.0 : 0.0, false);
        v.getOrCreateVariable("door_r").setTo(doorRightOpen ? 1.0 : 0.0, false);

        ItemInstrument speedometer = PackParser.getItem(PACK, SPEEDOMETER);
        ItemInstrument fuelGauge = PackParser.getItem(PACK, FUEL_GAUGE);
        if (speedometer != null) {
            v.addInstrument(speedometer, SPEEDOMETER_SLOT);
        }
        if (fuelGauge != null) {
            v.addInstrument(fuelGauge, FUEL_GAUGE_SLOT);
        }

        world.spawnEntity(v);
        // the pack's default parts (the engine, and the permanent ones: the glovebox and the invisible standing seat of
        // the trunk); merc230 has no default wheels nor seats
        v.addPartsPostAddition(null, null);
        int removed = 0;
        for (APart p : new ArrayList<>(v.parts)) {
            if (p instanceof PartEngine) {
                v.removePart(p, false, true);
                removed++;
            }
        }

        int capacity = item.definition.motorized.fuelCapacity;
        String fluid = gasolineFluid();
        double fuel = fluid.isEmpty() ? 0.0 : capacity * FUEL_FRACTION;
        if (fluid.isEmpty()) {
            AmcVehicles.LOG.warn("No valid fluid for the fuel type of {}:{}; the car spawns without fuel", PACK, ENGINE);
        } else {
            v.fuelTank.manuallySet(fluid, "", fuel);
        }

        List<String> parts = new ArrayList<>();
        for (APart p : v.parts) {
            parts.add(p.placementSlot + ":" + p.definition.packID + ":" + p.definition.systemName);
        }
        return new Result(color, hoodOpen, trunkOpen, doorLeftOpen, doorRightOpen, fluid, fuel, capacity, removed, parts);
    }

    /**
     * The fluid IV itself would put in a new car with the pack's engine: the most potent fluid of the engine's fuel
     * type ({@code gasoline}) in IV's fuel config that exists in the game (by default lava, gasoline or ethanol;
     * AEntityVehicleE_Powered.addPartsPostAddition).
     */
    static String gasolineFluid() {
        String fuelType = "gasoline";
        AItemPack<?> engine = PackParser.getItem(PACK, ENGINE);
        if (engine != null && engine.definition instanceof JSONPart part && part.engine != null
                && part.engine.fuelType != null) {
            fuelType = part.engine.fuelType;
        }
        Map<String, Double> fluids = ConfigSystem.settings.fuel.fuels.get(fuelType);
        String best = "";
        if (fluids != null) {
            for (Map.Entry<String, Double> e : fluids.entrySet()) {
                if (InterfaceManager.coreInterface.isFluidValid(e.getKey())
                        && (best.isEmpty() || fluids.get(best) < e.getValue())) {
                    best = e.getKey();
                }
            }
        }
        return best;
    }
}
