package com.tkisor.ftbteamskyblock.forge.event;

import com.mojang.logging.LogUtils;
import com.tkisor.ftbteamskyblock.forge.Config;
import com.tkisor.ftbteamskyblock.forge.data.SkyblockData;
import com.tkisor.ftbteamskyblock.forge.skyblock.SkyblockGeneratorType;
import com.tkisor.ftbteamskyblock.util.StructureGenerate;
import dev.architectury.platform.Platform;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FTBTeamEvent {
    public static void register() {

        var fmlBus = MinecraftForge.EVENT_BUS;
        var modbus = FMLJavaModLoadingContext.get().getModEventBus();

        fmlBus.addListener(FTBTeamEvent::createSpawnPosition);
        fmlBus.addListener(EventPriority.LOW, FTBTeamEvent::onPlayerRespawn);
    }

    private static void createSpawnPosition(LevelEvent.CreateSpawnPosition event) {
        if (event.getLevel() instanceof ServerLevel level) {

            if (!(level.getChunkSource().getGenerator() instanceof SkyblockGeneratorType)) return;
            // 定义生成结构的中心点
            List<? extends Integer> xyz = Config.world_spawn_xyz.get();
            BlockPos centerPos = xyz.size() >= 3 ? new BlockPos(xyz.get(0), xyz.get(1), xyz.get(2)) : new BlockPos(0, 106, 0);

            // 调用生成结构的方法
            try {
                var path = Config.world_spawn.get();
                Path nbtPath = Platform.getGameFolder().resolve(path);
                BlockPos spawn = StructureGenerate.generateStructureFromNbt(level, centerPos, nbtPath);
                if (spawn != null) {
                    level.setDefaultSpawnPos(spawn, 0.0f);
                } else {
                    level.setDefaultSpawnPos(centerPos, 0.0f);
                }
                LogUtils.getLogger().info("Set new world spawn point at: " + spawn);
            } catch (Exception e) {
                LogUtils.getLogger().error("Failed to load NBT structure", e);
            }
        }
    }

    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (!(player.getServer().overworld().getChunkSource().getGenerator() instanceof SkyblockGeneratorType)) return;
            FTBTeamsAPI.api().getManager().getTeamForPlayer(player).ifPresent(t -> {
                FTBTeamEvent.spawn(player);
            });
        }
    }

    private static void spawn(ServerPlayer player) {
            Optional<Team> team = FTBTeamsAPI.api().getManager().getTeamForPlayer(player);
            team.ifPresent(t -> {
                SkyblockData data = SkyblockData.get(player.getServer());
                UUID teamId = t.getTeamId();
                BlockPos rebornXYZ = data.getTeamInfo().get(teamId).getPlayerSpawnRebornXYZ();

                ServerLevel overworld = ServerLifecycleHooks.getCurrentServer().getLevel(Level.OVERWORLD);
                player.teleportTo(overworld, rebornXYZ.getX(), rebornXYZ.getY() + 1, rebornXYZ.getZ(), player.getYRot(), player.getXRot());
            });
        return;
    }

}
