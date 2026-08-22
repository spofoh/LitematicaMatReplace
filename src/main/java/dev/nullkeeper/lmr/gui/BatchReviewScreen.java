/*
 * LMR - Litematica Material Replace
 * Copyright (C) 2026 NullKeeper-dev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 only.
 */
package dev.nullkeeper.lmr.gui;

import dev.nullkeeper.lmr.FeatureGuard;
import dev.nullkeeper.lmr.ReplacementBatch;
import dev.nullkeeper.lmr.ReplacementBatch.Operation;
import dev.nullkeeper.lmr.ReplacementService;
import fi.dy.masa.litematica.gui.GuiMaterialList;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.render.GuiContext;
import fi.dy.masa.malilib.render.RenderUtils;
import net.minecraft.client.gui.screens.Screen;

public final class BatchReviewScreen extends LmrGuiBase {
    private final ReplacementBatch batch;

    public BatchReviewScreen(ReplacementBatch batch) {
        this.batch = batch;
        this.title = tr("lmr.gui.batch.title");
    }

    @Override
    protected String screenHookName() {
        return "batch-review";
    }

    @Override
    protected void initLmrGui() {
        int width = 142;
        int gap = 8;
        int startX = this.width / 2 - width - gap / 2;
        this.addButton(
            new ButtonGeneric(startX, this.height - 34, width, 20,
                tr("lmr.gui.button.replace_another")),
            (button, mouseButton) -> FeatureGuard.run(
                "batch Replace another button", this::openMaterialList
            )
        );
        this.addButton(
            new ButtonGeneric(startX + width + gap, this.height - 34, width, 20,
                tr("lmr.gui.button.save_changes")),
            (button, mouseButton) -> FeatureGuard.run(
                "batch Save changes button",
                () -> {
                    PersistenceScreen persistence = new PersistenceScreen(this.batch);
                    persistence.setParent(this);
                    GuiBase.openGui(persistence);
                }
            )
        );
    }

    @Override
    protected void drawLmrTitle(
        GuiContext context, int mouseX, int mouseY, float partialTicks
    ) {
        drawCentered(context, this.title, this.width / 2, 18, 0xFFFFFFFF);
    }

    @Override
    protected void drawLmrContents(
        GuiContext context, int mouseX, int mouseY, float partialTicks
    ) {
        int panelWidth = Math.min(420, this.width - 30);
        int panelX = (this.width - panelWidth) / 2;
        int panelY = 42;
        int rowHeight = 30;
        int visible = Math.min(this.batch.size(), Math.max(1, (this.height - 105) / rowHeight));
        int panelHeight = 36 + visible * rowHeight;
        RenderUtils.drawRect(context, panelX, panelY, panelWidth, panelHeight, 0xD0202020);
        RenderUtils.drawOutline(context, panelX, panelY, panelWidth, panelHeight, 0xFF888888);

        long affected = ReplacementService.countPlannedChanges(this.batch);
        drawCentered(context, tr("lmr.gui.batch.summary", this.batch.size(), affected),
            this.width / 2, panelY + 12, 0xFFFFFF77);

        int first = Math.max(0, this.batch.size() - visible);
        for (int index = first; index < this.batch.size(); index++) {
            Operation operation = this.batch.operations().get(index);
            int y = panelY + 31 + (index - first) * rowHeight;
            renderLargeItem(context, operation.sourceStack(), panelX + 10, y, 1.25F);
            renderLargeItem(context, operation.targetStack(), panelX + panelWidth / 2 + 8, y, 1.25F);
            this.drawString(context,
                ellipsize(operation.sourceStack().getHoverName().getString(), panelWidth / 2 - 55),
                panelX + 34, y + 6, 0xFFFFFFFF);
            drawCentered(context, "\u2192", panelX + panelWidth / 2, y + 6, 0xFFCCCCCC);
            this.drawString(context,
                ellipsize(operation.targetStack().getHoverName().getString(), panelWidth / 2 - 55),
                panelX + panelWidth / 2 + 34, y + 6, 0xFFFFFFFF);
        }
    }

    private void openMaterialList() {
        GuiMaterialList refreshed = new GuiMaterialList(this.batch.context().materialList());
        Screen previousParent = this.batch.context().materialListScreen() instanceof GuiBase materialGui
            ? materialGui.getParent() : null;
        refreshed.setParent(previousParent);
        GuiBase.openGui(refreshed);
    }
}
