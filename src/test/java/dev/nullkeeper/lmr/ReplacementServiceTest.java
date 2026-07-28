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
}
