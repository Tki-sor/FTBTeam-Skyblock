package com.tkisor.ftbteamskyblock.forge.data;

import com.google.gson.*;
import net.minecraft.core.BlockPos;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkyblockDataSerializer implements JsonSerializer<SkyblockData>, JsonDeserializer<SkyblockData> {

    @Override
    public JsonElement serialize(SkyblockData src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        // 序列化 worldSpawn
        jsonObject.add("world_spawn", context.serialize(src.getWorldSpawn()));

        // 序列化 teamInfo
        JsonObject teamInfoObject = new JsonObject();
        for (Map.Entry<UUID, TeamInfo> entry : src.getTeamInfo().entrySet()) {
//            teamInfoObject.addProperty("is_delete", entry.getValue().isDelete()); // 添加 isDeleted 字段
            teamInfoObject.add(entry.getKey().toString(), context.serialize(entry.getValue()));
        }
        jsonObject.add("team_info", teamInfoObject);

        return jsonObject;
    }

    @Override
    public SkyblockData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        // 反序列化 worldSpawn
        BlockPos worldSpawn = context.deserialize(jsonObject.get("world_spawn"), BlockPos.class);

        // 反序列化 teamInfo
        JsonObject teamInfoObject = jsonObject.getAsJsonObject("team_info");
        Map<UUID, TeamInfo> teamInfoMap = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : teamInfoObject.entrySet()) {
            UUID teamId = UUID.fromString(entry.getKey());
            TeamInfo teamInfo = context.deserialize(entry.getValue(), TeamInfo.class);
            teamInfoMap.put(teamId, teamInfo);
        }

        // 单例化返回
        SkyblockData instance = SkyblockData.create();
        instance.setWorldSpawn(worldSpawn);
        instance.setTeamInfo(teamInfoMap);
        return instance;
    }
}
