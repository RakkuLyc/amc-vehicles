package com.amcvehicles;

import mcinterface1211.BuilderBlock;
import mcinterface1211.BuilderTileEntity;
import mcinterface1211.WrapperWorld;
import minecrafttransportsimulator.baseclasses.Point3D;
import minecrafttransportsimulator.blocks.components.ABlockBase;
import minecrafttransportsimulator.blocks.components.ABlockBaseTileEntity;
import minecrafttransportsimulator.blocks.tileentities.components.ATileEntityBase;
import minecrafttransportsimulator.items.instances.ItemDecor;
import minecrafttransportsimulator.mcinterface.IWrapperNBT;
import minecrafttransportsimulator.mcinterface.InterfaceManager;
import minecrafttransportsimulator.packloading.PackParser;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.server.level.ServerLevel;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * The fuel pumps of the gas stations: Immersive Vehicles' own {@code mts:fuelpump} (an IV decor block; the user's
 * choice, 16 Nov 2026, instead of the Official Pack's {@code mtsofficialpack:fuelpump}), placed without a player.
 * IV's own {@code WrapperWorld.setBlock} with no player only sets the block (no tile entity), so this does what it
 * does for a player: the block state of IV's block, then the tile entity, created with data that carry
 * the angle ({@code angles}, read by the constructor before it sizes the bounding box) and the tank. Two members of IV
 * are protected and reached by reflection: {@code BuilderBlock.blockMap} and {@code BuilderTileEntity.setTileEntity}.
 */
final class FuelPumps {
    public static final String PACK = "mts";
    public static final String PUMP = "fuelpump";
    /** A bucket, in mB: the pumps get 0 to {@link Draw#MAX_BUCKETS} buckets of lava (the user's choice). */
    public static final int BUCKET = 1000;

    private FuelPumps() {
    }

    /**
     * Places a pump at {@code pos}, its front to the Minecraft yaw {@code yaw} (0 south ... as the cars), with
     * {@code mb} of lava (0 = empty). Returns null when placed, else why not.
     */
    @SuppressWarnings("unchecked")
    public static String place(ServerLevel level, BlockPos pos, float yaw, int mb) {
        ItemDecor item = PackParser.getItem(PACK, PUMP);
        if (item == null) {
            return "Immersive Vehicles has no item " + PACK + ":" + PUMP;
        }
        ABlockBase block = item.getBlock();
        try {
            Field mapField = BuilderBlock.class.getDeclaredField("blockMap");
            mapField.setAccessible(true);
            BuilderBlock builder = ((Map<ABlockBase, BuilderBlock>) mapField.get(null)).get(block);
            if (builder == null) {
                return "no Minecraft block for " + PACK + ":" + PUMP;
            }
            if (!level.setBlock(pos, builder.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE)) {
                return "the block was not set";
            }
            if (!(level.getBlockEntity(pos) instanceof BuilderTileEntity tile)) {
                return "no IV block entity at " + pos.toShortString();
            }
            WrapperWorld world = WrapperWorld.getWrapperFor(level);
            ATileEntityBase<?> te = ((ABlockBaseTileEntity) block).createTileEntity(world,
                    new Point3D(pos.getX(), pos.getY(), pos.getZ()), null, item, pumpData(yaw, mb));
            Method set = BuilderTileEntity.class.getDeclaredMethod("setTileEntity", ATileEntityBase.class);
            set.setAccessible(true);
            set.invoke(tile, te);
            world.addEntity(te);
            level.blockEntityChanged(pos);
            return null;
        } catch (ReflectiveOperationException | RuntimeException e) {
            AmcVehicles.LOG.error("Could not place the fuel pump at {}", pos, e);
            return e.toString();
        }
    }

    /** The data a new pump is built from: its angle and, with {@code mb} > 0, a tank of lava. */
    private static IWrapperNBT pumpData(float yaw, int mb) {
        IWrapperNBT data = InterfaceManager.coreInterface.getNewNBTWrapper();
        // IV turns by its own yaw, Minecraft's with the sign flipped (as the cars)
        data.setPoint3d("angles", new Point3D(0.0, -yaw, 0.0));
        if (mb > 0) {
            // what IV stores when a player pours a lava bucket in: the fluid's path and namespace
            IWrapperNBT tank = InterfaceManager.coreInterface.getNewNBTWrapper();
            tank.setString("currentFluid", "lava");
            tank.setString("currentFluidMod", "minecraft");
            tank.setDouble("fluidLevel", mb);
            data.setData("tank", tank);
        }
        return data;
    }
}
