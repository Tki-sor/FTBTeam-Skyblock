package com.tkisor.ftbteamskyblock.forge.event;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.tkisor.ftbteamskyblock.forge.data.SkyblockData;
import com.tkisor.ftbteamskyblock.forge.data.TeamInfo;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.data.TeamManagerImpl;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Optional;
import java.util.UUID;

public class FTBTeamSkyblockCommand {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("ftbskyblock")
                        .then(Commands.literal("create")
                                .requires(this::hasNoParty)
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .executes(ctx -> createParty(ctx.getSource(), StringArgumentType.getString(ctx, "name")))
                                )
                                .executes(ctx -> createParty(ctx.getSource(), ""))
                        )
                        .executes(this::spawn)
        );

        dispatcher.register(
                Commands.literal("island")
                        .requires(source -> !hasNoParty(source))
                        .then(Commands.literal("info")
                                .executes(ctx -> {
                                    /*
                                     * == Skyblock Info ==
                                     * Team ID: UUID
                                     * Spawn Pos:
                                     * Chunk Pos:
                                     * Reborn Pos:
                                     * Owner:
                                     *
                                     */
                                    Optional<Team> team = getTeamForContext(ctx);
                                    team.ifPresent(t -> {
                                        SkyblockData data = SkyblockData.get(ctx.getSource().getServer());
                                        UUID teamId = t.getTeamId();
                                        BlockPos spawnXYZ = data.getTeamInfo().get(teamId).getPlayerSpawnXYZ();
                                        ChunkPos chunkPos = new ChunkPos(spawnXYZ);
                                        BlockPos rebornXYZ = data.getTeamInfo().get(teamId).getPlayerSpawnRebornXYZ();

                                        MutableComponent literal = Component.literal("== Skyblock Info ==");
                                        literal.append("\nTeam Id: ")
                                                .append(teamId.toString());
                                        literal.append("\nSpawn Pos: ")
                                                .append("[" + spawnXYZ.toShortString() + "]");
                                        literal.append("\nChunk Pos: ")
                                                .append(chunkPos.toString());
                                        literal.append("\nReborn Pos: ")
                                                .append("[" + rebornXYZ.toShortString() + "]");
                                        Component playerName = TeamManagerImpl.INSTANCE.getPlayerName(t.getOwner());
                                        literal.append("\nOwner: ")
                                                .append(playerName);

                                        ctx.getSource().sendSystemMessage(literal);
                                    });

                                    return 0;
                                })
                        )
                        .then(Commands.literal("setreborn")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayer();
                                    if (player == null) return 0;
                                    SkyblockData data = SkyblockData.get(ctx.getSource().getServer());
                                    getTeamForContext(ctx).ifPresent(t-> {
                                        if (isOwner(t.getTeamId(), player.getUUID())) {
                                            TeamInfo teamInfo = data.getTeamInfo().get(t.getTeamId());
                                            teamInfo.setPlayerSpawnRebornXYZ(player.getOnPos());
                                            data.save();
                                            MutableComponent literal = Component.literal("Set Player Reborn Pos: ");
                                            literal.append(player.getOnPos().toShortString());
                                            player.sendSystemMessage(literal);
                                        }
                                    });
                                    return 0;
                                })
                        )
                        .executes(this::spawn)
        );
    }

    private boolean isOwner(UUID teamId, UUID playerId) {
        Optional<Team> teamByID = FTBTeamsAPI.api().getManager().getTeamByID(teamId);
        return teamByID.filter(team -> team.getOwner().equals(playerId)).isPresent();
    }

    private int createParty(CommandSourceStack source, String partyName) throws CommandSyntaxException {
        return TeamManagerImpl.INSTANCE.createParty(source.getPlayerOrException(), partyName).getLeft();
    }

    private boolean hasNoParty(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer) {
            return FTBTeamsAPI.api().getManager().getTeamForPlayerID(source.getEntity().getUUID())
                    .map(team -> !team.isPartyTeam())
                    .orElse(false);
        }

        return false;
    }

    private int spawn(CommandContext<CommandSourceStack> ctx) {
        if (!hasNoParty(ctx.getSource())) {
            Optional<Team> team = getTeamForContext(ctx);
            team.ifPresent(t -> {
                SkyblockData data = SkyblockData.get(ctx.getSource().getServer());
                UUID teamId = t.getTeamId();
                BlockPos rebornXYZ = data.getTeamInfo().get(teamId).getPlayerSpawnRebornXYZ();

                ServerLevel overworld = ServerLifecycleHooks.getCurrentServer().getLevel(Level.OVERWORLD);
                ServerPlayer player = ctx.getSource().getPlayer();
                ctx.getSource().getPlayer().teleportTo(overworld, rebornXYZ.getX(), rebornXYZ.getY() + 1, rebornXYZ.getZ(), player.getYRot(), player.getXRot());
            });
        }
        return 0;
    }

    private static Optional<Team> getTeamForContext(CommandContext<CommandSourceStack> ctx) {
        return FTBTeamsAPI.api().getManager().getTeamForPlayer(ctx.getSource().getPlayer());
    }
}
