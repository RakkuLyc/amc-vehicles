package com.amcvehicles;

import mcinterface1211.WrapperWorld;
import minecrafttransportsimulator.baseclasses.Point3D;
import minecrafttransportsimulator.entities.instances.APart;
import minecrafttransportsimulator.entities.instances.EntityVehicleF_Physics;
import minecrafttransportsimulator.entities.instances.PartEngine;
import minecrafttransportsimulator.entities.instances.PartGroundDevice;
import minecrafttransportsimulator.entities.instances.PartSeat;
import minecrafttransportsimulator.items.components.AItemPack;
import minecrafttransportsimulator.items.instances.ItemInstrument;
import minecrafttransportsimulator.items.instances.ItemVehicle;
import minecrafttransportsimulator.jsondefs.JSONInstrumentDefinition;
import minecrafttransportsimulator.jsondefs.JSONPart;
import minecrafttransportsimulator.jsondefs.JSONPartDefinition;
import minecrafttransportsimulator.mcinterface.AWrapperWorld;
import minecrafttransportsimulator.mcinterface.InterfaceManager;
import minecrafttransportsimulator.packloading.PackParser;
import minecrafttransportsimulator.systems.ConfigSystem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builds an abandoned car of {@link Cars} in the world: always without engine, wheels (the Escargot's spare too) and
 * seats, {@link #FUEL_FRACTION} of its tank, a flat battery, only a speedometer (km/h) and a fuel gauge on the
 * dashboard; hood, trunk and each side door as drawn. The other default parts of the pack stay (the glovebox, the
 * invisible standing seat of the trunk, the Scout's pickup bed and bumper sticker, the Mustang's mirror ornament).
 */
final class Vehicles {
    /** Fuel left in the tank: 15% of the car's {@code motorized.fuelCapacity} (the user's choice, 17 Nov 2026). */
    public static final double FUEL_FRACTION = 0.15;
    /**
     * Charge of the battery: 0 (flat; the user's choice, 17 Nov 2026), so the jumper pack matters. IV's new car has
     * 85.7% ({@code batteryCapacity * 0.85715}, set in its constructor for a vehicle with no saved data);
     * {@code electricPower} is the field IV saves and reads.
     */
    public static final double BATTERY_FRACTION = 0.0;
    static final String INSTRUMENTS = "mtsofficialpack";
    static final String SPEEDOMETER = "instrument_car_spedometer_kph";
    static final String FUEL_GAUGE = "instrument_car_fuelqty";

    private Vehicles() {
    }

    /** What was spawned. */
    public record Result(Draw.Car car, String fluid, double fuel, double fuelCapacity, int speedometerSlot,
                         int fuelGaugeSlot, List<String> removed, List<String> parts) {
    }

    /** The dashboard slots used: the speedometer's and the fuel gauge's (-1 when none). */
    private record Dashboard(int speedometer, int fuelGauge) {
    }

    /**
     * Spawns the car with its origin (the rear axle) at (x, y, z), its nose pointing to the Minecraft yaw
     * {@code noseYaw} (0 = south, +Z; 90 = west; 180 = north; 270 = east). The order matters to IV: the variables and
     * the instruments go in before the car is in the world (they travel in its first data), the parts after it.
     */
    public static Result spawn(ServerLevel level, double x, double y, double z, float noseYaw, Draw.Car car) {
        Cars.Model m = car.model();
        ItemVehicle item = PackParser.getItem(m.pack(), m.system(), car.color());
        if (item == null) {
            throw new IllegalStateException("Immersive Vehicles has no item " + m.id() + car.color());
        }
        AWrapperWorld world = WrapperWorld.getWrapperFor(level);
        EntityVehicleF_Physics v = newVehicle(world, item, x, y, z, noseYaw);
        setOpenings(v, car);
        Dashboard dash = setDashboard(v, item);
        world.spawnEntity(v);
        // the pack's default parts, then out go the ones an abandoned car lacks
        v.addPartsPostAddition(null, null);
        List<String> removed = removeParts(v);
        int capacity = item.definition.motorized.fuelCapacity;
        String fluid = fluidFor(item);
        double fuel = fluid.isEmpty() ? 0.0 : capacity * FUEL_FRACTION;
        if (fluid.isEmpty()) {
            AmcVehicles.LOG.warn("No valid fluid for the fuel type of {}; the car spawns without fuel", m.id());
        } else {
            // the fluid's namespace, as IV stores a poured bucket: with "" a bucket of lava could not top the car up
            // (EntityFluidTank.fill takes the same fluid only with the same mod, or "wildcard" as the pumps send)
            v.fuelTank.manuallySet(fluid, namespace(fluid), fuel);
        }
        List<String> parts = new ArrayList<>();
        for (APart p : v.parts) {
            parts.add(partName(p));
        }
        return new Result(car, fluid, fuel, capacity, dash.speedometer(), dash.fuelGauge(), removed, parts);
    }

    /**
     * A new car, not yet in the world, at (x, y, z) facing {@code noseYaw}, standing still, with a flat battery. Data
     * null = a new car, as from a fresh item: the pack's initialVariables (some doors and the hood open, set again by
     * {@link #setOpenings}) and default instruments (replaced by {@link #setDashboard}).
     */
    private static EntityVehicleF_Physics newVehicle(AWrapperWorld world, ItemVehicle item, double x, double y,
                                                     double z, float noseYaw) {
        EntityVehicleF_Physics v = new EntityVehicleF_Physics(world, null, item, null);
        v.electricPower = item.definition.motorized.batteryCapacity * BATTERY_FRACTION;
        v.position.set(x, y, z);
        v.prevPosition.set(v.position);
        // IV turns by its own yaw, which is Minecraft's with the sign flipped (WrapperEntity.getYaw)
        v.orientation.setToAngles(new Point3D(0.0, -noseYaw, 0.0));
        v.prevOrientation.set(v.orientation);
        v.motion.set(0.0, 0.0, 0.0);
        v.prevMotion.set(v.motion);
        return v;
    }

    /** The hood, the trunk and each side door, open (1) or closed (0) as drawn. */
    private static void setOpenings(EntityVehicleF_Physics v, Draw.Car car) {
        Cars.Model m = car.model();
        v.getOrCreateVariable(Cars.Model.HOOD).setTo(car.hoodOpen() ? 1.0 : 0.0, false);
        if (m.trunk() != null) {
            v.getOrCreateVariable(m.trunk()).setTo(car.trunkOpen() ? 1.0 : 0.0, false);
        }
        for (int k = 0; k < m.doors().size(); k++) {
            v.getOrCreateVariable(m.doors().get(k)).setTo(car.doorsOpen().get(k) ? 1.0 : 0.0, false);
        }
    }

    /** The dashboard: only the speedometer (km/h) and the fuel gauge; the pack's other instruments go. */
    private static Dashboard setDashboard(EntityVehicleF_Physics v, ItemVehicle item) {
        List<JSONInstrumentDefinition> slots = item.definition.instruments;
        if (slots == null || slots.isEmpty()) {
            return new Dashboard(-1, -1);
        }
        int speedo = speedometerSlot(slots);
        int fuelSlot = fuelGaugeSlot(slots, speedo);
        for (int k = 0; k < slots.size(); k++) {
            if (k != speedo && k != fuelSlot && v.instruments.get(k) != null) {
                v.removeIntrument(k);
            }
        }
        ItemInstrument s = PackParser.getItem(INSTRUMENTS, SPEEDOMETER);
        ItemInstrument f = PackParser.getItem(INSTRUMENTS, FUEL_GAUGE);
        if (s != null) {
            v.addInstrument(s, speedo);
        }
        if (f != null && fuelSlot >= 0) {
            v.addInstrument(f, fuelSlot);
        }
        return new Dashboard(speedo, fuelSlot);
    }

    /**
     * Takes out the engine, the wheels (a spare too) and the seats that are not permanent (the invisible standing
     * seat of the trunk is); returns them as "slot:pack:name".
     */
    private static List<String> removeParts(EntityVehicleF_Physics v) {
        List<String> removed = new ArrayList<>();
        for (APart p : new ArrayList<>(v.parts)) {
            boolean seat = p instanceof PartSeat && !p.placementDefinition.isPermanent;
            if (p instanceof PartEngine || p instanceof PartGroundDevice || seat) {
                removed.add(partName(p));
                v.removePart(p, false, true);
            }
        }
        return removed;
    }

    private static String partName(APart p) {
        return p.placementSlot + ":" + p.definition.packID + ":" + p.definition.systemName;
    }

    /**
     * The slot of the speedometer: where the pack puts its own (a default instrument named *spedometer*), else the
     * middle one by position on the dashboard (hudX).
     */
    static int speedometerSlot(List<JSONInstrumentDefinition> slots) {
        for (int k = 0; k < slots.size(); k++) {
            String d = slots.get(k).defaultInstrument;
            if (d != null && d.contains("spedometer")) {
                return k;
            }
        }
        List<Integer> byX = new ArrayList<>();
        for (int k = 0; k < slots.size(); k++) {
            byX.add(k);
        }
        byX.sort((a, b) -> slots.get(a).hudX != slots.get(b).hudX ? Integer.compare(slots.get(a).hudX,
                slots.get(b).hudX) : Integer.compare(a, b));
        return byX.get(slots.size() / 2);
    }

    /**
     * The slot of the fuel gauge: where the pack puts its own (*fuelqty*), else the first to the right of the
     * speedometer (the smallest hudX above it), else the nearest to its left; -1 when the car has one slot only.
     */
    static int fuelGaugeSlot(List<JSONInstrumentDefinition> slots, int speedo) {
        for (int k = 0; k < slots.size(); k++) {
            String d = slots.get(k).defaultInstrument;
            if (d != null && d.contains("fuelqty") && k != speedo) {
                return k;
            }
        }
        int sx = slots.get(speedo).hudX;
        int right = -1;
        int left = -1;
        for (int k = 0; k < slots.size(); k++) {
            if (k == speedo) {
                continue;
            }
            int x = slots.get(k).hudX;
            if (x > sx && (right < 0 || x < slots.get(right).hudX)) {
                right = k;
            }
            if (x <= sx && (left < 0 || x > slots.get(left).hudX)) {
                left = k;
            }
        }
        return right >= 0 ? right : left;
    }

    /** The namespace of the registered fluid of this path (minecraft first), as IV reads a bucket's fluid. */
    static String namespace(String path) {
        String found = "";
        for (ResourceLocation key : BuiltInRegistries.FLUID.keySet()) {
            if (key.getPath().equals(path)) {
                if (key.getNamespace().equals("minecraft")) {
                    return "minecraft";
                }
                if (found.isEmpty()) {
                    found = key.getNamespace();
                }
            }
        }
        return found;
    }

    /**
     * The fluid IV itself would put in a new car with the pack's engine: the most potent fluid of the engine's fuel
     * type in IV's fuel config that exists in the game (gasoline by default: lava, gasoline or ethanol;
     * AEntityVehicleE_Powered.addPartsPostAddition).
     */
    static String fluidFor(ItemVehicle item) {
        Map<String, Double> fluids = ConfigSystem.settings.fuel.fuels.get(engineFuelType(item));
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

    /** The fuel type of the car's first default engine ("gasoline" when the pack does not say). */
    static String engineFuelType(ItemVehicle item) {
        if (item.definition.parts == null) {
            return "gasoline";
        }
        for (JSONPartDefinition p : item.definition.parts) {
            if (p.defaultPart == null || p.types == null || p.types.stream().noneMatch(t -> t.startsWith("engine"))) {
                continue;
            }
            String id = p.defaultPart;
            int colon = id.indexOf(':');
            AItemPack<?> engine = PackParser.getItem(id.substring(0, colon), id.substring(colon + 1));
            if (engine != null && engine.definition instanceof JSONPart part && part.engine != null
                    && part.engine.fuelType != null) {
                return part.engine.fuelType;
            }
            return "gasoline";
        }
        return "gasoline";
    }
}
