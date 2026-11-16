package com.amcvehicles;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * The chunks (ChunkPos longs) whose drawn cars are not placed yet, saved with the world
 * ({@code data/amcvehicles_pending.dat} of the dimension): a chunk is written here when it is generated with a car
 * drawn in it, and taken out only when every car of it is placed or found blocked. A restart in between resumes it.
 */
public final class Pending extends SavedData {
    static final String NAME = "amcvehicles_pending";
    private final LongSet chunks = new LongOpenHashSet();

    static Pending of(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(Pending::new, Pending::load), NAME);
    }

    private static Pending load(CompoundTag tag, HolderLookup.Provider registries) {
        Pending p = new Pending();
        for (long c : tag.getLongArray("chunks")) {
            p.chunks.add(c);
        }
        return p;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLongArray("chunks", chunks.toLongArray());
        return tag;
    }

    boolean contains(long chunk) {
        return chunks.contains(chunk);
    }

    void add(long chunk) {
        if (chunks.add(chunk)) {
            setDirty();
        }
    }

    void remove(long chunk) {
        if (chunks.remove(chunk)) {
            setDirty();
        }
    }

    int size() {
        return chunks.size();
    }
}
