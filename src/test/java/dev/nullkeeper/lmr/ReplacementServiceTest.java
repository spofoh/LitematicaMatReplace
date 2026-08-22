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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import dev.nullkeeper.lmr.BlockCatalog.Candidate;
import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.litematica.schematic.container.LitematicaBlockStateContainer;
import fi.dy.masa.litematica.selection.AreaSelection;
import fi.dy.masa.litematica.selection.Box;
import fi.dy.masa.malilib.util.data.ItemType;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class ReplacementServiceTest {
    @BeforeAll
    static void bootstrapRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        BuiltInRegistries.ITEM.listElements().forEach(holder -> {
            if (!holder.areComponentsBound()) {
                holder.bindComponents(DataComponentMap.EMPTY);
            }
        });
    }

    @Test
    void replacesEveryMatchingPositionWithTheTargetsDefaultState() {
        AreaSelection area = new AreaSelection();
        area.setName("LMR replacement test");
        area.addSubRegionBox(
            new Box(BlockPos.ZERO, new BlockPos(2, 0, 0), "main"),
            true
        );

        LitematicaSchematic schematic =
            LitematicaSchematic.createEmptySchematic(area, "LMR test");
        assertNotNull(schematic);

        LitematicaBlockStateContainer container =
            schematic.getSubRegionContainer("main");
        assertNotNull(container);

        BlockState rotatedStairs = Blocks.OAK_STAIRS
            .defaultBlockState()
            .setValue(HorizontalDirectionalBlock.FACING, Direction.EAST);
        container.set(0, 0, 0, rotatedStairs);
        container.set(1, 0, 0, Blocks.OAK_STAIRS.defaultBlockState());
        container.set(2, 0, 0, Blocks.COBBLESTONE.defaultBlockState());

        ItemStack sourceStack = new ItemStack(Blocks.OAK_STAIRS);
        ItemType sourceType = new ItemType(sourceStack, true, false);
        ReplacementContext context = new ReplacementContext(
            null,
            null,
            sourceStack,
            sourceType,
            schematic,
            null
        );

        assertEquals(2, ReplacementService.countMatches(context));
        assertEquals(
            2,
            ReplacementService.replaceMatchingPositions(
                schematic,
                sourceType,
                Blocks.COBBLESTONE.defaultBlockState()
            )
        );
        assertSame(
            Blocks.COBBLESTONE.defaultBlockState(),
            container.get(0, 0, 0)
        );
        assertSame(
            Blocks.COBBLESTONE.defaultBlockState(),
            container.get(1, 0, 0)
        );
        assertSame(
            Blocks.COBBLESTONE.defaultBlockState(),
            container.get(2, 0, 0)
        );
    }

    @Test
    void countsBatchedReplacementsWithoutCascadingTargets() {
        AreaSelection area = new AreaSelection();
        area.setName("LMR batch test");
        area.addSubRegionBox(
            new Box(BlockPos.ZERO, new BlockPos(2, 0, 0), "main"),
            true
        );

        LitematicaSchematic schematic =
            LitematicaSchematic.createEmptySchematic(area, "LMR batch test");
        assertNotNull(schematic);
        LitematicaBlockStateContainer container =
            schematic.getSubRegionContainer("main");
        assertNotNull(container);
        container.set(0, 0, 0, Blocks.STONE.defaultBlockState());
        container.set(1, 0, 0, Blocks.DIRT.defaultBlockState());
        container.set(2, 0, 0, Blocks.OAK_PLANKS.defaultBlockState());

        ReplacementContext stone = contextFor(schematic, Blocks.STONE);
        ReplacementContext dirt = contextFor(schematic, Blocks.DIRT);
        ReplacementBatch batch = ReplacementBatch.forContext(stone);
        batch.put(stone, candidateFor(Blocks.DIRT));
        batch.put(dirt, candidateFor(Blocks.OAK_PLANKS));

        assertEquals(2, batch.size());
        assertEquals(2, ReplacementService.countPlannedChanges(batch));
        assertSame(Blocks.STONE.defaultBlockState(), container.get(0, 0, 0));
        assertSame(Blocks.DIRT.defaultBlockState(), container.get(1, 0, 0));
        assertEquals(
            2,
            ReplacementService.applyOperations(schematic, batch.operations(), true)
        );
        assertSame(Blocks.DIRT.defaultBlockState(), container.get(0, 0, 0));
        assertSame(Blocks.OAK_PLANKS.defaultBlockState(), container.get(1, 0, 0));
        assertSame(Blocks.OAK_PLANKS.defaultBlockState(), container.get(2, 0, 0));
        batch.finish();
    }

    private static ReplacementContext contextFor(
        LitematicaSchematic schematic,
        net.minecraft.world.level.block.Block block
    ) {
        ItemStack stack = new ItemStack(block);
        return new ReplacementContext(
            null,
            null,
            stack,
            new ItemType(stack, true, false),
            schematic,
            null
        );
    }

    private static Candidate candidateFor(net.minecraft.world.level.block.Block block) {
        ItemStack stack = new ItemStack(block);
        String name = stack.getHoverName().getString();
        return new Candidate(block, stack, name, name, name, name);
    }
}
