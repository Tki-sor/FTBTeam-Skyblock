package com.tkisor.ftbteamskyblock.forge.skyblock;

import net.minecraft.core.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import java.util.Optional;
import java.util.stream.Stream;

public class BasedChunkGenerator extends NoiseBasedChunkGenerator {
    public BasedChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        super(biomeSource, settings);
    }

    public static class FilteredLookup extends HolderLookup.Delegate<StructureSet> {
        private final TagKey<StructureSet> allowedValues;

        public FilteredLookup(HolderLookup<StructureSet> pParent, TagKey<StructureSet> allowedValues) {
            super(pParent);
            this.allowedValues = allowedValues;
        }

        @Override
        public Optional<Holder.Reference<StructureSet>> get(ResourceKey<StructureSet> key) {
            return this.parent.get(key).filter(obj -> obj.is(this.allowedValues));
        }

        @Override
        public Stream<Holder.Reference<StructureSet>> listElements() {
            return this.parent.listElements().filter(obj -> obj.is(this.allowedValues));
        }
    }
}
