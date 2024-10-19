package com.tkisor.ftbteamskyblock.forge;

import com.mojang.brigadier.CommandDispatcher;
import com.tkisor.ftbteamskyblock.FTBTeamSkyblock;
import com.tkisor.ftbteamskyblock.forge.event.FTBTeamEvent;
import com.tkisor.ftbteamskyblock.forge.event.FTBTeamSkyblockCommand;
import com.tkisor.ftbteamskyblock.forge.register.ChunkGeneratorsRegister;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.platform.Platform;
import dev.architectury.platform.forge.EventBuses;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.nio.file.Path;

@Mod(FTBTeamSkyblock.MOD_ID)
public final class FTBTeamSkyblockForge {
    public FTBTeamSkyblockForge() {
        var fmlBus = MinecraftForge.EVENT_BUS;
        var modbus = FMLJavaModLoadingContext.get().getModEventBus();
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(FTBTeamSkyblock.MOD_ID, modbus);

        // Run our common setup.
        registerConfig();
        FTBTeamSkyblock.init();

        FTBTeamEvent.register();

        ChunkGeneratorsRegister.CHUNK_GENERATORS.register(modbus);

        CommandRegistrationEvent.EVENT.register((this::registerCommands));

    }

    private void registerConfig() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG, "ftbteamskyblock/ftbteamskyblock-common.toml");
        Path resolve = Platform.getConfigFolder().resolve("ftbteamskyblock/structures");

        if (!resolve.toFile().exists()) {
            resolve.toFile().mkdir();
        }
    }

    private void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection selection) {
        new FTBTeamSkyblockCommand().register(dispatcher);
    }
}
