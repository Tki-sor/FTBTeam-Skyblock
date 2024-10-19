package com.tkisor.ftbteamskyblock.forge.register;

import com.mojang.serialization.Codec;
import com.tkisor.ftbteamskyblock.FTBTeamSkyblock;
import com.tkisor.ftbteamskyblock.forge.skyblock.OceanChuckGenerator;
import com.tkisor.ftbteamskyblock.forge.skyblock.SkyblockChunkGenerator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ChunkGeneratorsRegister {
    public static final DeferredRegister<Codec<? extends ChunkGenerator>> CHUNK_GENERATORS = DeferredRegister.create(Registries.CHUNK_GENERATOR, FTBTeamSkyblock.MOD_ID);
    public static final RegistryObject<Codec<SkyblockChunkGenerator>> SKYBLOCK = CHUNK_GENERATORS.register("skyblock", () -> SkyblockChunkGenerator.CODEC);
    public static final RegistryObject<Codec<OceanChuckGenerator>> OCEAN = CHUNK_GENERATORS.register("ocean", () -> OceanChuckGenerator.CODEC);

}
