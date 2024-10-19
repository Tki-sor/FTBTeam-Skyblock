package com.tkisor.ftbteamskyblock.forge.mixin.ftbteams.data;

import com.mojang.authlib.GameProfile;
import com.tkisor.ftbteamskyblock.forge.data.SkyblockData;
import com.tkisor.ftbteamskyblock.forge.data.TeamInfo;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.data.PartyTeam;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Optional;

@Mixin(value = PartyTeam.class, remap = false)
public class PartyTeamMixin {
    @Inject(method = "kick", at = @At(value = "RETURN", remap = false))
    private void kick(CommandSourceStack from, Collection<GameProfile> players, CallbackInfoReturnable<Integer> cir) {
        SkyblockData data = SkyblockData.get(from.getServer());
        ServerPlayer player = from.getPlayer();
        if (player == null) return;
        Optional<Team> team = FTBTeamsAPI.api().getManager().getTeamForPlayer(player);
        team.ifPresent(t -> {
            TeamInfo teamInfo = data.getTeamInfo().get(t.getTeamId());
            teamInfo.setDelete(true);

        });

    }
}
