package com.tkisor.ftbteamskyblock.neoforge;

import com.mojang.serialization.Codec;
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

public class BasedChunkGenerator extends NoiseBasedChunkGenerator {
    public static final MapCodec<BasedChunkGenerator> CODEC = RecordCodecBuilder
            .mapCodec((instance) -> instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source")
                            .forGetter((arg) -> arg.biomeSource),
                    NoiseGeneratorSettings.CODEC.fieldOf("settings")
                            .forGetter((arg) -> arg.settings),
                    TagKey.codec(Registries.STRUCTURE_SET).fieldOf("allowed_structure_sets")
                            .forGetter(gen ->  gen.allowedStructureSets),
                    Codec.BOOL.optionalFieldOf("void_end_generation", true)
                            .forGetter(gen -> gen.voidEndGeneration),
                    Codec.BOOL.optionalFieldOf("void_nether_generation", true)
                            .forGetter(gen -> gen.voidNetherGeneration)
            ).apply(instance, instance.stable(BasedChunkGenerator::new)));
    public final Holder<NoiseGeneratorSettings> settings;
    private final TagKey<StructureSet> allowedStructureSets;


    private final boolean generateNormal;
    private final boolean allowBiomeDecoration;

    private final boolean voidEndGeneration;
    private final boolean voidNetherGeneration;

    public BasedChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings, TagKey<StructureSet> allowedStructureSets, boolean voidEndGeneration, boolean voidNetherGeneration) {
        super(biomeSource, settings);
        this.settings = settings;
        this.allowedStructureSets = allowedStructureSets;
        this.voidEndGeneration = voidEndGeneration;
        this.voidNetherGeneration = voidNetherGeneration;
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
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender pBlender, RandomState pRandom, StructureManager pStructureManager, ChunkAccess chunk) {

        if (this.generateNormal) {
            return super.fillFromNoise(pBlender, pRandom, pStructureManager, chunk);
        } else {
            return CompletableFuture.completedFuture(chunk);
        }
    }

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
