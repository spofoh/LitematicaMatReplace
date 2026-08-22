/*
 * LMR - Litematica Material Replace
 * Copyright (C) 2026 NullKeeper-dev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 */
package dev.nullkeeper.lmr;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.materials.MaterialCache;
import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.litematica.schematic.container.LitematicaBlockStateContainer;
import fi.dy.masa.litematica.util.FileType;
import fi.dy.masa.malilib.util.data.ItemType;
import java.nio.file.Path;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class ReplacementService {
    private ReplacementService() {
    }

    public static long countMatches(ReplacementContext context) {
        return visitMatchingPositions(context.schematic(), context.sourceType(), null);
    }

    public static long countPlannedChanges(ReplacementBatch batch) {
        return applyOperations(batch.context().schematic(), batch.operations(), false);
    }

    static long replaceMatchingPositions(
        LitematicaSchematic schematic,
        ItemType sourceType,
        BlockState replacement
    ) {
        return visitMatchingPositions(schematic, sourceType, replacement);
    }

    public static SaveResult saveAndApply(
        ReplacementContext context,
        Block targetBlock,
        Path directory,
        String fileName,
        boolean overwrite
    ) {
        Path targetFile = LitematicaSchematic.fileFromDirAndName(
            directory,
            fileName,
            FileType.LITEMATICA_SCHEMATIC
        );

        CompoundTag snapshot = context.schematic().writeToNBT();
        LitematicaSchematic outputSchematic = new LitematicaSchematic(
            targetFile,
            snapshot,
            FileType.LITEMATICA_SCHEMATIC
        );
        BlockState targetState = targetBlock.defaultBlockState();

        long outputChanges = replaceMatchingPositions(
            outputSchematic,
            context.sourceType(),
            targetState
        );
        if (outputChanges == 0) {
            return new SaveResult(false, 0, targetFile, SaveFailure.NO_MATCHES);
        }

        if (!outputSchematic.writeToFile(directory, fileName, overwrite)) {
            return new SaveResult(false, 0, targetFile, SaveFailure.WRITE_FAILED);
        }

        long liveChanges = replaceMatchingPositions(
            context.schematic(),
            context.sourceType(),
            targetState
        );
        if (liveChanges != outputChanges) {
            LmrClient.LOGGER.warn(
                "LMR wrote {} replacements but applied {} to the live schematic",
                outputChanges,
                liveChanges
            );
        }

        DataManager.getSchematicPlacementManager()
            .markAllPlacementsOfSchematicForRebuild(context.schematic());
        context.materialList().reCreateMaterialList();

        return new SaveResult(true, liveChanges, targetFile, SaveFailure.NONE);
    }

    public static SaveResult saveAndApply(
        ReplacementBatch batch,
        Path directory,
        String fileName,
        boolean overwrite
    ) {
        ReplacementContext context = batch.context();
        Path targetFile = LitematicaSchematic.fileFromDirAndName(
            directory,
            fileName,
            FileType.LITEMATICA_SCHEMATIC
        );

        CompoundTag snapshot = context.schematic().writeToNBT();
        LitematicaSchematic outputSchematic = new LitematicaSchematic(
            targetFile,
            snapshot,
            FileType.LITEMATICA_SCHEMATIC
        );
        long outputChanges = applyOperations(outputSchematic, batch.operations(), true);
        if (outputChanges == 0) {
            return new SaveResult(false, 0, targetFile, SaveFailure.NO_MATCHES);
        }

        if (!outputSchematic.writeToFile(directory, fileName, overwrite)) {
            return new SaveResult(false, 0, targetFile, SaveFailure.WRITE_FAILED);
        }

        long liveChanges = applyOperations(context.schematic(), batch.operations(), true);
        if (liveChanges != outputChanges) {
            LmrClient.LOGGER.warn(
                "LMR wrote {} batched replacements but applied {} to the live schematic",
                outputChanges,
                liveChanges
            );
        }

        DataManager.getSchematicPlacementManager()
            .markAllPlacementsOfSchematicForRebuild(context.schematic());
        context.materialList().reCreateMaterialList();
        return new SaveResult(true, liveChanges, targetFile, SaveFailure.NONE);
    }

    static long applyOperations(
        LitematicaSchematic schematic,
        java.util.List<ReplacementBatch.Operation> operations,
        boolean replace
    ) {
        long changes = 0;
        for (String regionName : schematic.getAreas().keySet()) {
            LitematicaBlockStateContainer container =
                schematic.getSubRegionContainer(regionName);
            if (container == null) {
                continue;
            }

            Vec3i size = container.getSize();
            for (int y = 0; y < size.getY(); y++) {
                for (int z = 0; z < size.getZ(); z++) {
                    for (int x = 0; x < size.getX(); x++) {
                        BlockState currentState = container.get(x, y, z);
                        for (ReplacementBatch.Operation operation : operations) {
                            if (!matchesMaterial(currentState, operation.sourceType())) {
                                continue;
                            }
                            changes++;
                            if (replace) {
                                container.set(
                                    x,
                                    y,
                                    z,
                                    operation.targetBlock().defaultBlockState()
                                );
                            }
                            break;
                        }
                    }
                }
            }
        }
        return changes;
    }

    private static long visitMatchingPositions(
        LitematicaSchematic schematic,
        ItemType sourceType,
        BlockState replacement
    ) {
        long matches = 0;

        for (String regionName : schematic.getAreas().keySet()) {
            LitematicaBlockStateContainer container =
                schematic.getSubRegionContainer(regionName);
            if (container == null) {
                continue;
            }

            Vec3i size = container.getSize();
            for (int y = 0; y < size.getY(); y++) {
                for (int z = 0; z < size.getZ(); z++) {
                    for (int x = 0; x < size.getX(); x++) {
                        BlockState currentState = container.get(x, y, z);
                        if (!matchesMaterial(currentState, sourceType)) {
                            continue;
                        }

                        matches++;
                        if (replacement != null) {
                            container.set(x, y, z, replacement);
                        }
                    }
                }
            }
        }

        return matches;
    }

    private static boolean matchesMaterial(BlockState state, ItemType sourceType) {
        if (state.isAir()) {
            return false;
        }

        BlockState stateToConvert = state;
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)
            && state.getValue(BlockStateProperties.WATERLOGGED)) {
            if (matchesStack(new ItemStack(Items.WATER_BUCKET), sourceType)) {
                return true;
            }
            stateToConvert = state.setValue(BlockStateProperties.WATERLOGGED, false);
        }

        MaterialCache cache = MaterialCache.getInstance();
        if (cache.requiresMultipleItems(stateToConvert)) {
            for (ItemStack stack : cache.getItems(stateToConvert)) {
                if (!stack.isEmpty() && matchesStack(stack, sourceType)) {
                    return true;
                }
            }
            return false;
        }

        ItemStack requiredStack = cache.getRequiredBuildItemForState(stateToConvert);
        return !requiredStack.isEmpty() && matchesStack(requiredStack, sourceType);
    }

    private static boolean matchesStack(ItemStack stack, ItemType sourceType) {
        ItemStack normalized = stack.copy();
        normalized.setCount(1);
        return sourceType.equals(new ItemType(normalized, true, false));
    }

    public record SaveResult(
        boolean success,
        long changedPositions,
        Path targetFile,
        SaveFailure failure
    ) {
    }

    public enum SaveFailure {
        NONE,
        NO_MATCHES,
        WRITE_FAILED
    }
}
