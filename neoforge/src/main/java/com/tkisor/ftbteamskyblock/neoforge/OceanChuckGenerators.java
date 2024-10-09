package com.tkisor.ftbteamskyblock.neoforge;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class OceanChuckGenerators extends NoiseBasedChunkGenerator implements FTBTeamSkyblockGeneratorsType {
    public static final MapCodec<OceanChuckGenerators> CODEC = RecordCodecBuilder
            .mapCodec((instance) -> instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source")
                            .forGetter((arg) -> arg.biomeSource),
                    NoiseGeneratorSettings.CODEC.fieldOf("settings")
                            .forGetter((arg) -> arg.settings),
                    TagKey.codec(Registries.STRUCTURE_SET).fieldOf("allowed_structure_sets")
                            .forGetter(gen ->  gen.allowedStructureSets)
            ).apply(instance, instance.stable(OceanChuckGenerators::new)));
    public final Holder<NoiseGeneratorSettings> settings;
    private final TagKey<StructureSet> allowedStructureSets;


    private final boolean generateNormal;
    private final boolean allowBiomeDecoration;

    private final boolean voidEndGeneration = true;
    private final boolean voidNetherGeneration = true;

    public OceanChuckGenerators(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings, TagKey<StructureSet> allowedStructureSets) {
        super(biomeSource, settings);
        this.settings = settings;
        this.allowedStructureSets = allowedStructureSets;
        this.generateNormal = (settings.is(ResourceLocation.parse("minecraft:end")) && !voidEndGeneration) || (settings.is(ResourceLocation.parse("minecraft:nether")) && !voidNetherGeneration);
        this.allowBiomeDecoration = !settings.is(ResourceLocation.parse("minecraft:overworld"));
    }

    @Override
    public Holder<NoiseGeneratorSettings> generatorSettings() {
        return this.settings;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(WorldGenRegion pLevel, long pSeed, RandomState pRandom, BiomeManager pBiomeManager, StructureManager pStructureManager, ChunkAccess pChunk, GenerationStep.Carving pStep) {
        if (this.generateNormal) {
            super.applyCarvers(pLevel, pSeed, pRandom, pBiomeManager, pStructureManager, pChunk, pStep);
        }
    }

    @Override
    public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> lookup, RandomState pRandomState, long pSeed) {
        return this.generateNormal ? super.createState(lookup, pRandomState, pSeed) : super.createState(new SkyblockChunkGenerator.FilteredLookup(lookup, this.allowedStructureSets), pRandomState, pSeed);
    }

    @Override
    public void buildSurface(WorldGenRegion pLevel, StructureManager pStructureManager, RandomState pRandom, ChunkAccess pChunk) {
        if (this.generateNormal) {
            super.buildSurface(pLevel, pStructureManager, pRandom, pChunk);
        }
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion pLevel) {
        if (this.generateNormal) {
            super.spawnOriginalMobs(pLevel);
        }
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender arg, RandomState arg2, StructureManager arg3, ChunkAccess chunk) {
        NoiseSettings noiseSettings = this.settings.value().noiseSettings().clampToHeightAccessor(chunk.getHeightAccessorForGeneration());
        int minY = noiseSettings.minY();
        int maxY = minY + noiseSettings.height();

        // 设置海平面和地表层高度
        int seaLevel = 62; // 海平面高度
        int groundLevel = 30; // 低于此高度的区域填充为地面方块（如石头）

        // 开始生成区块
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("wgen_fill_noise", () -> {

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = minY; y < maxY; y++) {
                        BlockPos pos = new BlockPos(x, y, z);

                        if (y < groundLevel) {
                            // 在较低的高度生成地面方块，例如石头
                            chunk.setBlockState(pos, Blocks.STONE.defaultBlockState(), false);
                        } else if (y < seaLevel) {
                            // 在 60 到 120 之间生成水
                            chunk.setBlockState(pos, Blocks.WATER.defaultBlockState(), true);
                        } else {
                            // 高于海平面的区域填充空气
                            chunk.setBlockState(pos, Blocks.AIR.defaultBlockState(), false);
                        }
                    }
                }
            }

            return chunk;
        }), Util.backgroundExecutor());
    }



//    @Override
//    public CompletableFuture<ChunkAccess> fillFromNoise(Blender pBlender, RandomState pRandom, StructureManager pStructureManager, ChunkAccess chunk) {
//
//        if (this.generateNormal) {
//            return super.fillFromNoise(pBlender, pRandom, pStructureManager, chunk);
//        } else {
//            return CompletableFuture.completedFuture(chunk);
//        }
//    }

    @Override
    public int getBaseHeight(int pX, int pZ, Heightmap.Types pType, LevelHeightAccessor pLevel, RandomState pRandom) {
        if (this.generateNormal) {
            return super.getBaseHeight(pX, pZ, pType, pLevel, pRandom);
        } else {
            return getMinY();
        }
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
        if (this.generateNormal || this.allowBiomeDecoration) {
            super.applyBiomeDecoration(pLevel, pChunk, pStructureManager);
        }
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


    public static class FilteredLookup implements HolderLookup<StructureSet> {
        protected final HolderLookup<StructureSet> parent;
        private final TagKey<StructureSet> allowedValues;

        public FilteredLookup(HolderLookup<StructureSet> pParent, TagKey<StructureSet> allowedValues) {
            this.parent = pParent;
            this.allowedValues = allowedValues;
        }

        public Optional<Holder.Reference<StructureSet>> get(ResourceKey<StructureSet> pResourceKey) {
            return this.parent.get(pResourceKey).filter(obj -> obj.is(this.allowedValues));
        }

        public Stream<Holder.Reference<StructureSet>> listElements() {
            return this.parent.listElements().filter(obj -> obj.is(this.allowedValues));
        }

        public Optional<HolderSet.Named<StructureSet>> get(TagKey<StructureSet> pTagKey) {
            return this.parent.get(pTagKey);
        }

        public Stream<HolderSet.Named<StructureSet>> listTags() {
            return this.parent.listTags();
        }
    }
}
