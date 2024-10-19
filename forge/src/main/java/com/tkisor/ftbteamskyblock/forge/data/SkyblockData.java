package com.tkisor.ftbteamskyblock.forge.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.tkisor.ftbteamskyblock.FTBTeamSkyblock;
import com.tkisor.ftbteamskyblock.forge.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SkyblockData {
    private static SkyblockData instance;

    private BlockPos worldSpawn;
    private Map<UUID, TeamInfo> teamInfo;

    private static MinecraftServer server;

    // 私有构造函数，防止外部实例化
    private SkyblockData(BlockPos worldSpawn, Map<UUID, TeamInfo> teamInfo) {
        this.worldSpawn = worldSpawn;
        this.teamInfo = teamInfo;
    }

    /**
     * <p>获取{@link SkyblockData}的单例。</p>
     * <p>由于数据被持久化存储在每个世界的文件夹中，所以需要传入 server</p>
     * <p>通常你不需要关注如何存储与读取的，只需要使用这个get即可。</p>
     *
     * @param server
     * @return
     */
    public static SkyblockData get(@NotNull MinecraftServer server) {
        if (SkyblockData.server != server) {
            SkyblockData.server = server;
            readSkyblockInfo(server);
        }
        return instance;
    }

    private static void readSkyblockInfo(@NotNull MinecraftServer server) {
        Path resolve = server.getWorldPath(LevelResource.ROOT).resolve(FTBTeamSkyblock.MOD_ID).resolve("server");
        Path skyblock_info = resolve.resolve("skyblock_info.json");
        if (skyblock_info.toFile().exists()) {
            try {
                String infoJson = Files.readString(skyblock_info, StandardCharsets.UTF_8);
                instance = SkyblockData.fromJson(infoJson);
            } catch (Exception e) {
                LogUtils.getLogger().error("read skyblock_info error", e);
            }
        } else {
            try {
                Files.createDirectories(resolve);
                List<? extends Integer> xyz = Config.world_spawn_xyz.get();
                BlockPos centerPos = xyz.size() >= 3 ? new BlockPos(xyz.get(0), xyz.get(1), xyz.get(2)) : new BlockPos(0, 106, 0);
                SkyblockData data = new SkyblockData(centerPos, new HashMap<>());
                Files.writeString(skyblock_info, data.toJson(), StandardOpenOption.CREATE);
                instance = data;
            } catch (Exception e) {
                LogUtils.getLogger().error("create/wrtie skyblock_info error", e);
            }
        }
    }

    protected static SkyblockData create() {
        return new SkyblockData(new BlockPos(0, 0, 0), new HashMap<>());
    }

    /**
     * <p>查找并分配岛屿</p>
     * <p>分配后请使用这个坐标，不然这很浪费。或者，我也不知道有什么后果。</p>
     * <p>如果你是需要获取Team的坐标，请在这里查找：{@link TeamInfo}</p>
     *
     * @param teamId
     * @return 被分配到的坐标 {@link TeamInfo#getPlayerSpawnXYZ()}
     */
    public BlockPos assignIsland(UUID teamId) {
        // 获取当前的岛屿起始点
        BlockPos spawnPoint = this.getWorldSpawn();

        int spacing = Config.player_spawn_spacing.get();

        // 计算当前玩家的岛屿位置
        int playerCount = teamInfo.size();

        // 计算 x 和 z 的偏移量
        int islandX = spawnPoint.getX() + (playerCount % 2 == 0 ? spacing : -spacing) * (playerCount / 2 + 1);
        int islandZ = spawnPoint.getZ() + (playerCount / 2) * spacing * (playerCount % 2 == 0 ? 1 : -1);

        BlockPos islandPos = new BlockPos(islandX, spawnPoint.getY(), islandZ);

        // 检查这个位置是否被占用
        while (true) {
            boolean occupied = false;

            // 检查是否有TeamInfo存在且未标记为删除
            for (TeamInfo info : teamInfo.values()) {
                if (!info.isDelete() && info.getPlayerSpawnXYZ().equals(islandPos)) {
                    occupied = true; // 已被占用
                    break;
                }
            }

            if (!occupied) {
                // 如果没有被占用，分配该位置
                TeamInfo newTeamInfo = new TeamInfo(islandPos, islandPos); // 根据需要设置出生点和重生点
                teamInfo.put(teamId, newTeamInfo);
                return islandPos; // 返回分配到的坐标
            }

            // 如果被占用，增加 x 和 z 轴的偏移量，寻找新的位置
            islandX += (playerCount % 2 == 0 ? spacing : -spacing);
            islandZ += (playerCount % 2 == 0 ? spacing : -spacing);
            islandPos = new BlockPos(islandX, spawnPoint.getY(), islandZ);
        }
    }

    public BlockPos getWorldSpawn() {
        return worldSpawn;
    }

    /**
     * <p><span style="color: #FF6347;">请勿使用该方法。</span></p>
     * <p>由于对空岛位置的查找使用了较为简单的方式，也就是基于起点然后对xy寻找合适点，并未详细的对这个点进行检查。</p>
     * <p>对空岛的世界重生点直接设置极度危险！</p>
     * <p>正确的做法是，在配置文件设定好世界重生点。虽然这不会影响现有世界的重生点。</p>
     * @deprecated 请勿使用它！
     *
     * @param worldSpawn
     */
    @Deprecated
    public void setWorldSpawn(BlockPos worldSpawn) {
        this.worldSpawn = worldSpawn;
    }

    public Map<UUID, TeamInfo> getTeamInfo() {
        return teamInfo;
    }

    /**
     * <p>直接对队伍的Map进行设置</p>
     * 这是内部使用的方法，如果需要对Map进行操作，请使用get获取到Map后再操作
     * @param teamInfo
     */
    protected void setTeamInfo(Map<UUID, TeamInfo> teamInfo) {
        this.teamInfo = teamInfo;
    }

    // 添加或更新团队信息
    public void addOrUpdateTeamInfo(UUID teamId, TeamInfo info) {
        this.teamInfo.put(teamId, info);
    }


    /**
     * <p>需手动调用。立即将SkyblockData的数据存储。</p>
     * <p>若不这么做，数据将无法被持久化保存。<p/>
     *
     */
    public void save() {
        Path resolve = server.getWorldPath(LevelResource.ROOT).resolve(FTBTeamSkyblock.MOD_ID).resolve("server");
        Path skyblock_info = resolve.resolve("skyblock_info.json");
        try {
            Files.writeString(skyblock_info, toJson(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            LogUtils.getLogger().error("Save SkyblockData ERROR", e);
        }
    }

    // 新增 toJson 方法，用于将单例对象转换为 JSON 字符串
    public String toJson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(SkyblockData.class, new SkyblockDataSerializer());
        Gson gson = gsonBuilder.create();
        return gson.toJson(this);
    }

    // 新增 fromJson 方法，用于从 JSON 字符串反序列化
    public static SkyblockData fromJson(String json) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(SkyblockData.class, new SkyblockDataSerializer());
        Gson gson = gsonBuilder.create();
        return gson.fromJson(json, SkyblockData.class);
    }


}
