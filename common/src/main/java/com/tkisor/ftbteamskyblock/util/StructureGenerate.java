package com.tkisor.ftbteamskyblock.util;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class StructureGenerate {
    /**
     * 根据 NBT 文件在 Minecraft 世界中生成结构。
     * 结构的生成以给定的 BlockPos 为中心，Y 坐标保持不变，X 和 Z 坐标根据 NBT 文件的偏移量进行调整，
     * 使结构的 XZ 中心对齐到提供的中心点位置。
     *
     * @param level     {@link ServerLevel} 实例，表示在哪个世界生成结构。
     * @param centerPos {@link BlockPos} 表示结构生成的中心点。
     * @param nbtPath   NBT 文件的路径，用于加载结构信息。
     * @return
     * @throws IOException 如果读取 NBT 文件时出现问题。
     */
    public static @Nullable BlockPos generateStructureFromNbt(ServerLevel level, BlockPos centerPos, Path nbtPath) throws IOException {
        if (Files.exists(nbtPath)) {
            // 读取 .nbt 文件
            CompoundTag nbt = NbtIo.readCompressed(nbtPath.toFile());
            ListTag blocks = nbt.getList("blocks", 10);
            ListTag palette = nbt.getList("palette", 10);

            // 计算 .nbt 结构的 XZ 平面的包围盒
            int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

            // 遍历所有方块，计算出最小和最大 XZ 坐标
            for (int i = 0; i < blocks.size(); i++) {
                CompoundTag blockTag = blocks.getCompound(i);
                ListTag posList = blockTag.getList("pos", 3); // 获取方块相对坐标

                int x = posList.getInt(0);
                int z = posList.getInt(2);

                // 更新最小和最大 XZ 坐标
                minX = Math.min(minX, x);
                minZ = Math.min(minZ, z);
                maxX = Math.max(maxX, x);
                maxZ = Math.max(maxZ, z);
            }

            // 计算 .nbt 结构的 XZ 平面的中心点
            int centerX = (minX + maxX) / 2;
            int centerZ = (minZ + maxZ) / 2;
            BlockPos nbtCenterXZ = new BlockPos(centerX, 0, centerZ); // 中心点只取 X 和 Z

            // 获取 Block 注册表
            HolderGetter<Block> blockRegistry = level.registryAccess().registryOrThrow(Registries.BLOCK).asLookup();

            BlockPos spawn = null;
            // 遍历所有方块，基于 XZ 中心点进行偏移，Y 坐标保持不变
            for (int i = 0; i < blocks.size(); i++) {
                CompoundTag blockTag = blocks.getCompound(i);
                ListTag posList = blockTag.getList("pos", 3);

                int x = posList.getInt(0);
                int y = posList.getInt(1); // Y 坐标保持原样
                int z = posList.getInt(2);
                int stateIndex = blockTag.getInt("state");

                // 获取方块状态
                CompoundTag stateTag = palette.getCompound(stateIndex);
                BlockState blockState = NbtUtils.readBlockState(blockRegistry, stateTag);

                // 基于 .nbt 结构的 XZ 中心点和世界生成的 XZ 中心点进行偏移
                BlockPos blockPos = centerPos.offset(x - nbtCenterXZ.getX(), y, z - nbtCenterXZ.getZ());

                // 判断拼图方块
                if (blockState.getBlock().equals(Blocks.JIGSAW)) {
                    spawn = blockPos;
                    continue;
                }

                // 在世界中放置方块
                level.setBlock(blockPos, blockState, 3); // 3 表示更新邻近方块

                // 检查是否有 BlockEntity 数据 (如箱子等)
                if (blockTag.contains("nbt")) {
                    CompoundTag blockEntityTag = blockTag.getCompound("nbt");
                    BlockEntity blockEntity = level.getBlockEntity(blockPos);
                    if (blockEntity != null) {
                        blockEntity.load(blockEntityTag); // 应用 NBT 到 BlockEntity
                    }
                }
            }
            LogUtils.getLogger().info("qwq {}", spawn);
            return spawn;
        }
        LogUtils.getLogger().error("Not Find NBT");
        return null;
    }



    /**
     * 在给定的 Level 世界中生成一个空心长方体。
     *
     * @param level       要生成的 Level 世界
     * @param center      盒子的中心点 (Vec3i)
     * @param extendSize  在 X 和 Z 方向的延伸长度
     * @param height      盒子的高度
     * @param outerBlock  边缘方块
     * @param centerBlock 中心点的方块
     */
    public static void generateHollowBox(Level level, Vec3i center, int extendSize, int height, BlockState outerBlock, BlockState centerBlock) {
        // 中心点
        int centerX = center.getX();
        int centerY = center.getY();
        int centerZ = center.getZ();

        // 遍历盒子的每个方块位置
        for (int x = centerX - extendSize; x <= centerX + extendSize; x++) {
            for (int y = centerY; y < centerY + height; y++) {
                for (int z = centerZ - extendSize; z <= centerZ + extendSize; z++) {
                    // 检查是否在边缘
                    boolean isEdge = (x == centerX - extendSize || x == centerX + extendSize ||
                            z == centerZ - extendSize || z == centerZ + extendSize ||
                            y == centerY || y == centerY + height - 1);

                    BlockPos currentPos = new BlockPos(x, y, z); // 将 Vec3i 转换为 BlockPos

                    if (isEdge) {
                        // 在边缘处放置 outerBlock
                        level.setBlock(currentPos, outerBlock, 3);
                    }
                }
            }
        }
        level.setBlock(new BlockPos(center), centerBlock, 3);
    }
}
