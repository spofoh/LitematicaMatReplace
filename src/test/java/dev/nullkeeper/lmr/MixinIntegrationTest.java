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

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.nullkeeper.lmr.mixin.MaterialListPlacementAccessor;
import dev.nullkeeper.lmr.mixin.MaterialListSchematicAccessor;
import fi.dy.masa.litematica.materials.MaterialListPlacement;
import fi.dy.masa.litematica.materials.MaterialListSchematic;
import fi.dy.masa.litematica.gui.widgets.WidgetMaterialListEntry;
import org.junit.jupiter.api.Test;

final class MixinIntegrationTest {
    @Test
    void litematicaTargetsLoadAndAccessorMixinsApply() {
        assertTrue(
            MaterialListPlacementAccessor.class.isAssignableFrom(
                MaterialListPlacement.class
            )
        );
        assertTrue(
            MaterialListSchematicAccessor.class.isAssignableFrom(
                MaterialListSchematic.class
            )
        );
        assertTrue(
            WidgetMaterialListEntry.class.getDeclaredConstructors().length > 0
        );
    }
}
