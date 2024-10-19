package com.tkisor.ftbteamskyblock.forge.mixin.ftbteams.data;

import com.mojang.logging.LogUtils;
import com.tkisor.ftbteamskyblock.forge.Config;
import com.tkisor.ftbteamskyblock.forge.data.SkyblockData;
import com.tkisor.ftbteamskyblock.forge.skyblock.SkyblockGeneratorType;
import com.tkisor.ftbteamskyblock.util.StructureGenerate;
import dev.architectury.platform.Platform;
import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftbteams.data.PartyTeam;
import dev.ftb.mods.ftbteams.data.TeamManagerImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mixin(value = TeamManagerImpl.class, remap = false)
public class TeamManagerImplMixin {
    @Inject(
            method = "createParty(Ljava/util/UUID;Lnet/minecraft/server/level/ServerPlayer;Ljava/lang/String;Ljava/lang/String;Ldev/ftb/mods/ftblibrary/icon/Color4I;)Lorg/apache/commons/lang3/tuple/Pair;",
            at = @At(
                    value = "RETURN",
                    remap = false)
    )
    private void createParty(UUID playerId, @Nullable ServerPlayer player, String name, @Nullable String description, @Nullable Color4I color, CallbackInfoReturnable<Pair<Integer, PartyTeam>> cir) {

        if (player != null) {
            ServerLevel level = player.getServer().overworld().getLevel();
            if (!(level.getChunkSource().getGenerator() instanceof SkyblockGeneratorType)) return;
            // 定义生成结构的中心点
            UUID teamId = cir.getReturnValue().getValue().getTeamId();
            SkyblockData data = SkyblockData.get(player.getServer());
            BlockPos centerPos = data.assignIsland(teamId);
            data.save();
//            BlockPos centerPos = new BlockPos(100, 106, 100);

            // 定义 .nbt 文件路径
            var path = Config.player_spawn.get();
            Path nbtPath = Platform.getGameFolder().resolve(path);

            // 调用生成结构的方法
            try {
                BlockPos pos = StructureGenerate.generateStructureFromNbt(level, centerPos, nbtPath);
                if (pos != null) {
                    player.setRespawnPosition(player.level().dimension(), pos, 0.0f, true, true);
                    player.teleportTo(pos.getX(), pos.getY(), pos.getZ());
                    data.getTeamInfo().get(teamId).setPlayerSpawnRebornXYZ(pos);
                    data.save();
                } else {
                    player.teleportTo(centerPos.getX(), centerPos.getY()+1, centerPos.getZ());
                }

                if (Platform.isModLoaded("ftbchunks")) {
                    getSurroundingChunks(new ChunkPos(centerPos), 1).forEach(chunk -> {
                        FTBChunksAPI.api().claimAsPlayer(player, player.serverLevel().dimension(), chunk, false);
                    });
                }

                LogUtils.getLogger().info("Generated island structure successfully.");
            } catch (Exception e) {
                LogUtils.getLogger().error("Failed to load NBT structure", e);
            }
        }
    }

    /**
     * 获取以中心区块为中心的指定半径范围内的区块
     *
     * @param centerChunk 中心区块坐标
     * @param radius 半径（区块的数量）
     * @return 返回中心区块和其周围指定半径内的区块
     */
    private static List<ChunkPos> getSurroundingChunks(ChunkPos centerChunk, int radius) {
        List<ChunkPos> chunks = new ArrayList<>();

        // 通过双重循环获取指定半径范围内的区块
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                // 计算每个相邻区块的坐标
                int chunkX = centerChunk.x + dx;
                int chunkZ = centerChunk.z + dz;

                // 添加到结果列表
                chunks.add(new ChunkPos(chunkX, chunkZ));
            }
        }

        return chunks;
    }
}
