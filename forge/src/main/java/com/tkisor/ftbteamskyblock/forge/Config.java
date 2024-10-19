package com.tkisor.ftbteamskyblock.forge;

import com.tkisor.ftbteamskyblock.FTBTeamSkyblock;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public final class Config {
    public static final ForgeConfigSpec COMMON_CONFIG;

    public static final ForgeConfigSpec.IntValue sea_level;
    public static final ForgeConfigSpec.IntValue ground_level;
    public static final ForgeConfigSpec.ConfigValue<String> world_spawn;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> world_spawn_xyz;
    public static final ForgeConfigSpec.ConfigValue<String> player_spawn;
    public static final ForgeConfigSpec.IntValue player_spawn_spacing;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("General settings").push("general");
        sea_level = builder.comment("sea").defineInRange("sea_level", 106, -64, 319);
        ground_level = builder.comment("ground").defineInRange("ground_level", 70, -64, 319);
        world_spawn = builder.comment("path").define("world_spawn", "config/"+ FTBTeamSkyblock.MOD_ID +"/structures/world_spawn.nbt");
        world_spawn_xyz = builder.comment("xyz").defineList("world_spawn_xyz", () -> {
            List<Integer> list = new ArrayList<>();
            list.add(0);
            list.add(106);
            list.add(0);
            return list;
        }, $ -> true);

        player_spawn = builder.comment("path").define("player_spawn", "config/ftbteamskyblock/structures/player_spawn.nbt");
        player_spawn_spacing = builder.comment("spacing").defineInRange("player_spawn_spacing", 10000, Integer.MIN_VALUE, Integer.MAX_VALUE);

        builder.pop();
        COMMON_CONFIG = builder.build();
    }
}
