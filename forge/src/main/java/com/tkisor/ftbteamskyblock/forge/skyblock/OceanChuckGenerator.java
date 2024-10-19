package com.tkisor.ftbteamskyblock.forge.skyblock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tkisor.ftbteamskyblock.forge.Config;
import net.minecraft.Util;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class OceanChuckGenerator extends BasedChunkGenerator implements SkyblockGeneratorType {
    public static final Codec<OceanChuckGenerator> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source")
                    .forGetter((arg) -> arg.biomeSource),
            NoiseGeneratorSettings.CODEC.fieldOf("settings")
                    .forGetter((arg) -> arg.settings),
            TagKey.codec(Registries.STRUCTURE_SET).fieldOf("allowed_structure_sets")
                    .forGetter(gen -> gen.allowedStructureSets)
    ).apply(instance, instance.stable(OceanChuckGenerator::new)));
    protected final Holder<NoiseGeneratorSettings> settings;
    protected final TagKey<StructureSet> allowedStructureSets;

    protected final boolean generateNormal;
    protected final boolean allowBiomeDecoration;

    protected final boolean voidEndGeneration = true;
    protected final boolean voidNetherGeneration = true;

    public OceanChuckGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings, TagKey<StructureSet> allowedStructureSets) {
        super(biomeSource, settings);
        this.settings = settings;
        this.allowedStructureSets = allowedStructureSets;
        this.generateNormal = (settings.is(new ResourceLocation("minecraft:end")) && !voidEndGeneration) || (settings.is(new ResourceLocation("minecraft:nether")) && !voidNetherGeneration);
        this.allowBiomeDecoration = !settings.is(new ResourceLocation("minecraft:overworld"));

    }

    @Override
    public Holder<NoiseGeneratorSettings> generatorSettings() {
        return this.settings;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(WorldGenRegion pLevel, long pSeed, RandomState pRandom, BiomeManager pBiomeManager, StructureManager pStructureManager, ChunkAccess pChunk, GenerationStep.Carving pStep) {
//        if (this.generateNormal) {
//            super.applyCarvers(pLevel, pSeed, pRandom, pBiomeManager, pStructureManager, pChunk, pStep);
//        }
    }

//    @Override
//    public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> lookup, RandomState pRandomState, long pSeed) {
//        return this.generateNormal ? super.createState(lookup, pRandomState, pSeed) : super.createState(new BasedChunkGenerator.FilteredLookup(lookup, this.allowedStructureSets), pRandomState, pSeed);
//    }

//    @Override
//    public void buildSurface(WorldGenRegion pLevel, StructureManager pStructureManager, RandomState pRandom, ChunkAccess pChunk) {
//        if (this.generateNormal) {
//            super.buildSurface(pLevel, pStructureManager, pRandom, pChunk);
//        }
//    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion pLevel) {
        if (this.generateNormal) {
            super.spawnOriginalMobs(pLevel);
        }
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender pBlender, RandomState pRandom, StructureManager pStructureManager, ChunkAccess chunk) {
        return super.fillFromNoise(executor, pBlender, pRandom, pStructureManager, chunk)
                .thenCompose(chunkAccess -> CompletableFuture.supplyAsync(
                        Util.wrapThreadWithTaskName("wgen_fill_noise", () -> {

                            NoiseSettings noiseSettings = this.settings.value().noiseSettings().clampToHeightAccessor(chunk.getHeightAccessorForGeneration());
                            int minY = noiseSettings.minY();
                            int maxY = minY + noiseSettings.height();

                            int seaLevel = Config.sea_level.get();
                            int groundLevel = Config.ground_level.get();

                            for (int x = 0; x < 16; x++) {
                                for (int z = 0; z < 16; z++) {
                                    for (int y = minY; y < maxY; y++) {
                                        BlockPos pos = new BlockPos(x, y, z);

                                        if (y < groundLevel) {
                                            if (chunkAccess.getBlockState(pos).is(Blocks.AIR)) {
                                                chunkAccess.setBlockState(pos, Blocks.WATER.defaultBlockState(), false);
                                            }
                                            continue;
                                        } else if (y <= seaLevel) {
                                            // 在 60 到 120 之间生成水
                                            chunkAccess.setBlockState(pos, Blocks.WATER.defaultBlockState(), true);
                                        } else {
                                            // 高于海平面的区域填充空气
                                            chunkAccess.setBlockState(pos, Blocks.AIR.defaultBlockState(), false);
                                        }
                                    }
                                }
                            }
                            return chunkAccess;
                        }), Util.backgroundExecutor())
                );
    }

    @Override
    public int getBaseHeight(int pX, int pZ, Heightmap.Types pType, LevelHeightAccessor pLevel, RandomState pRandom) {
//        if (this.generateNormal) {
//            return super.getBaseHeight(pX, pZ, pType, pLevel, pRandom);
//        } else {
//            return getMinY();
//        }
        return 40;
    }

    @Override
    public NoiseColumn getBaseColumn(int pX, int pZ, LevelHeightAccessor pHeight, RandomState pRandom) {
        if (this.generateNormal) {
            return super.getBaseColumn(pX, pZ, pHeight, pRandom);
        } else {
            return new NoiseColumn(0, new BlockState[0]);
        }
    }

    @Override
    public void addDebugScreenInfo(List<String> pInfo, RandomState pRandom, BlockPos pPos) {
        if (this.generateNormal) {
            super.addDebugScreenInfo(pInfo, pRandom, pPos);
        }
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel pLevel, ChunkAccess pChunk, StructureManager pStructureManager) {
//        if (this.generateNormal || this.allowBiomeDecoration) {
//            super.applyBiomeDecoration(pLevel, pChunk, pStructureManager);
//        }
        super.applyBiomeDecoration(pLevel, pChunk, pStructureManager);
    }


    @Override
    public void createReferences(WorldGenLevel level, StructureManager pStructureManager, ChunkAccess pChunk) {
        if (this.generateNormal || hasStructures(level.registryAccess())) {
            super.createReferences(level, pStructureManager, pChunk);
        }
    }

    @Override
    public void createStructures(RegistryAccess registries, ChunkGeneratorStructureState pStructureState, StructureManager pStructureManager, ChunkAccess pChunk, StructureTemplateManager pStructureTemplateManager) {
        if (this.generateNormal || hasStructures(registries)) {
            super.createStructures(registries, pStructureState, pStructureManager, pChunk, pStructureTemplateManager);
        }
    }

    private boolean hasStructures(RegistryAccess registries) {
        return registries.registryOrThrow(Registries.STRUCTURE_SET).getTagOrEmpty(this.allowedStructureSets).iterator().hasNext();
    }

}
