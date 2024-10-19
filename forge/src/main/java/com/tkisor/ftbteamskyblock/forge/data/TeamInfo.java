package com.tkisor.ftbteamskyblock.forge.data;

import net.minecraft.core.BlockPos;

public class TeamInfo {
    private Boolean isDelete = false;
    private BlockPos playerSpawnXYZ;
    private BlockPos playerSpawnRebornXYZ;

    public static TeamInfo create(BlockPos playerSpawnXYZ, BlockPos playerSpawnRebornXYZ) {
        return new TeamInfo(playerSpawnXYZ, playerSpawnRebornXYZ);
    }

    protected TeamInfo(BlockPos playerSpawnXYZ, BlockPos playerSpawnRebornXYZ) {
        this.playerSpawnXYZ = playerSpawnXYZ;
        this.playerSpawnRebornXYZ = playerSpawnRebornXYZ;
    }

    public BlockPos getPlayerSpawnXYZ() {
        return playerSpawnXYZ;
    }

    public BlockPos getPlayerSpawnRebornXYZ() {
        return playerSpawnRebornXYZ;
    }

    public void setPlayerSpawnRebornXYZ(BlockPos playerSpawnRebornXYZ) {
        this.playerSpawnRebornXYZ = playerSpawnRebornXYZ;
    }

    public boolean isDelete() {
        return isDelete; // getter
    }

    public void setDelete(boolean delete) {
        isDelete = delete; // setter
    }
}
