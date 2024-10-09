package com.tkisor.ftbteamskyblock.neoforge;

import com.tkisor.ftbteamskyblock.FTBTeamSkyblock;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(FTBTeamSkyblock.MOD_ID)
public final class FTBTeamSkyblockNeoForge {
    public FTBTeamSkyblockNeoForge(IEventBus modBus) {
        // Run our common setup.
        FTBTeamSkyblock.init();

        EChunkGenerators.CHUNK_GENERATORS.register(modBus);

        TickEvent.SERVER_PRE.register(server -> {
            if (server.overworld().getChunkSource().getGenerator().getClass() == SkyblockChunkGenerator.class) {
                server.getPlayerList().getPlayers().forEach(serverPlayer -> {
                    serverPlayer.sendSystemMessage(Component.literal("是虚空"));

                });
            }

        });
    }
}
