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

import dev.nullkeeper.lmr.mixin.MaterialListPlacementAccessor;
import dev.nullkeeper.lmr.mixin.MaterialListSchematicAccessor;
import fi.dy.masa.litematica.materials.MaterialListBase;
import fi.dy.masa.litematica.materials.MaterialListEntry;
import fi.dy.masa.litematica.materials.MaterialListPlacement;
import fi.dy.masa.litematica.materials.MaterialListSchematic;
import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.malilib.util.data.ItemType;
import java.util.Optional;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

public record ReplacementContext(
    MaterialListBase materialList,
    MaterialListEntry sourceEntry,
    ItemStack sourceStack,
    ItemType sourceType,
    LitematicaSchematic schematic,
    Screen materialListScreen
) {
    public static Optional<ReplacementContext> create(
        MaterialListBase materialList,
        MaterialListEntry sourceEntry,
        Screen materialListScreen
    ) {
        LitematicaSchematic schematic = null;

        if (materialList instanceof MaterialListPlacement placementList) {
            schematic = ((MaterialListPlacementAccessor) placementList)
                .lmr$getPlacement()
                .getSchematic();
        } else if (materialList instanceof MaterialListSchematic schematicList) {
            schematic = ((MaterialListSchematicAccessor) schematicList)
                .lmr$getSchematic();
        }

        if (schematic == null || sourceEntry == null || sourceEntry.getStack().isEmpty()) {
            return Optional.empty();
        }

        ItemStack sourceStack = sourceEntry.getStack().copy();
        sourceStack.setCount(1);
        return Optional.of(new ReplacementContext(
            materialList,
            sourceEntry,
            sourceStack,
            new ItemType(sourceStack, true, false),
            schematic,
            materialListScreen
        ));
    }
}
