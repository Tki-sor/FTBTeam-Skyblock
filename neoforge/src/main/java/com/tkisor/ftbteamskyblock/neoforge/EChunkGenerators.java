package com.tkisor.ftbteamskyblock.neoforge;

import com.mojang.serialization.MapCodec;
import com.tkisor.ftbteamskyblock.FTBTeamSkyblock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EChunkGenerators {
    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS = DeferredRegister.create(Registries.CHUNK_GENERATOR, FTBTeamSkyblock.MOD_ID);
    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<SkyblockChunkGenerator>> SKYBLOCK = CHUNK_GENERATORS.register("skyblock", () -> SkyblockChunkGenerator.CODEC);
    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<OceanChuckGenerators>> OCEAN = CHUNK_GENERATORS.register("ocean", () -> OceanChuckGenerators.CODEC);

}
