package com.tkisor.ftbteamskyblock.forge;

import com.tkisor.ftbteamskyblock.FTBTeamSkyblock;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FTBTeamSkyblock.MOD_ID)
public final class FTBTeamSkyblockForge {
    public FTBTeamSkyblockForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(FTBTeamSkyblock.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        FTBTeamSkyblock.init();
        System.out.println("");
    }
}
