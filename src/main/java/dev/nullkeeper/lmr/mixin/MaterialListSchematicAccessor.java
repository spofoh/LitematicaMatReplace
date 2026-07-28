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
package dev.nullkeeper.lmr.mixin;

import fi.dy.masa.litematica.materials.MaterialListSchematic;
import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = MaterialListSchematic.class, remap = false)
public interface MaterialListSchematicAccessor {
    @Accessor(value = "schematic", remap = false)
    LitematicaSchematic lmr$getSchematic();
}
