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

import dev.nullkeeper.lmr.FeatureGuard;
import dev.nullkeeper.lmr.ReplacementBatch;
import dev.nullkeeper.lmr.ReplacementContext;
import dev.nullkeeper.lmr.gui.BlockPickerScreen;
import fi.dy.masa.litematica.gui.widgets.WidgetListMaterialList;
import fi.dy.masa.litematica.gui.widgets.WidgetMaterialListEntry;
import fi.dy.masa.litematica.materials.MaterialListBase;
import fi.dy.masa.litematica.materials.MaterialListEntry;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.widgets.WidgetListEntrySortable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WidgetMaterialListEntry.class, remap = false)
public abstract class WidgetMaterialListEntryMixin
    extends WidgetListEntrySortable<MaterialListEntry> {

    @Shadow(remap = false)
    @Final
    private MaterialListBase materialList;

    @Shadow(remap = false)
    @Final
    private WidgetListMaterialList listWidget;

    @Shadow(remap = false)
    @Final
    private MaterialListEntry entry;

    protected WidgetMaterialListEntryMixin(
        int x,
        int y,
        int width,
        int height,
        MaterialListEntry entry,
        int listIndex
    ) {
        super(x, y, width, height, entry, listIndex);
    }

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void lmr$addReplaceButton(
        int x,
        int y,
        int width,
        int height,
        boolean isOdd,
        MaterialListBase materialList,
        MaterialListEntry entry,
        int listIndex,
        WidgetListMaterialList listWidget,
        CallbackInfo callbackInfo
    ) {
        FeatureGuard.run("material-list row construction", () -> {
            if (entry == null) {
                return;
            }

            Minecraft minecraft = Minecraft.getInstance();
            //? if >=26.2 {
            Screen currentScreen = minecraft.gui.screen();
            //?} else {
            /*Screen currentScreen = minecraft.screen;
            *///?}
            ReplacementContext context = ReplacementContext.create(
                materialList,
                entry,
                currentScreen
            ).orElse(null);
            if (context == null) {
                return;
            }

            ReplacementBatch.Operation queuedOperation = ReplacementBatch
                .find(context)
                .flatMap(batch -> batch.operationFor(context.sourceType()))
                .orElse(null);

            ButtonGeneric ignoreSizingButton = new ButtonGeneric(
                x + width,
                y + 1,
                -1,
                true,
                "litematica.gui.button.material_list.ignore"
            );
            ButtonGeneric replaceButton = new ButtonGeneric(
                ignoreSizingButton.getX(),
                y + 1,
                -1,
                true,
                queuedOperation == null
                    ? "lmr.gui.button.replace"
                    : "lmr.gui.button.queued"
            );
            if (queuedOperation != null) {
                replaceButton.setHoverStrings(
                    fi.dy.masa.malilib.util.StringUtils.translate(
                        "lmr.gui.button.queued_hover",
                        queuedOperation.targetStack().getHoverName().getString()
                    )
                );
            }

            this.addButton(replaceButton, (button, mouseButton) ->
                FeatureGuard.run("material-list Replace button", () -> {
                    //? if >=26.2 {
                    Screen parent = minecraft.gui.screen();
                    //?} else {
                    /*Screen parent = minecraft.screen;
                    *///?}
                    ReplacementContext.create(
                        this.materialList,
                        this.entry,
                        parent
                    ).ifPresent(replacementContext -> {
                        BlockPickerScreen picker = new BlockPickerScreen(
                            replacementContext
                        );
                        picker.setParent(parent);
                        GuiBase.openGui(picker);
                    });
                })
            );
        });
    }
}
