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
package dev.nullkeeper.lmr.gui;

import dev.nullkeeper.lmr.BlockCatalog.Candidate;
import dev.nullkeeper.lmr.FeatureGuard;
import dev.nullkeeper.lmr.ReplacementContext;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.render.GuiContext;
import fi.dy.masa.malilib.render.RenderUtils;

public final class ConfirmationScreen extends LmrGuiBase {
    private final ReplacementContext replacementContext;
    private final Candidate target;
    private final long affectedPositions;

    public ConfirmationScreen(
        ReplacementContext replacementContext,
        Candidate target,
        long affectedPositions
    ) {
        this.replacementContext = replacementContext;
        this.target = target;
        this.affectedPositions = affectedPositions;
        this.title = tr("lmr.gui.confirm.title");
    }

    @Override
    protected String screenHookName() {
        return "confirmation";
    }

    @Override
    protected void initLmrGui() {
        int buttonWidth = 100;
        int gap = 8;
        int startX = this.width / 2 - buttonWidth - gap / 2;
        ButtonGeneric confirmButton = new ButtonGeneric(
            startX,
            this.height - 34,
            buttonWidth,
            20,
            tr("lmr.gui.button.confirm")
        );
        confirmButton.setEnabled(this.affectedPositions > 0);
        this.addButton(confirmButton, (button, mouseButton) ->
            FeatureGuard.run("confirmation Confirm button", () -> {
                PersistenceScreen persistence = new PersistenceScreen(
                    this.replacementContext,
                    this.target,
                    this.affectedPositions
                );
                persistence.setParent(this);
                GuiBase.openGui(persistence);
            })
        );

        this.addButton(
            new ButtonGeneric(
                startX + buttonWidth + gap,
                this.height - 34,
                buttonWidth,
                20,
                tr("lmr.gui.button.cancel")
            ),
            (button, mouseButton) -> FeatureGuard.run(
                "confirmation Cancel button",
                () -> GuiBase.openGui(getParent())
            )
        );
    }

    @Override
    protected void drawLmrTitle(
        GuiContext context,
        int mouseX,
        int mouseY,
        float partialTicks
    ) {
        drawCentered(context, this.title, this.width / 2, 18, 0xFFFFFFFF);
    }

    @Override
    protected void drawLmrContents(
        GuiContext context,
        int mouseX,
        int mouseY,
        float partialTicks
    ) {
        drawConfirmation(context);
    }

    private void drawConfirmation(GuiContext context) {
        int panelWidth = Math.min(360, this.width - 30);
        int panelHeight = 126;
        int panelX = (this.width - panelWidth) / 2;
        int panelY = Math.max(42, (this.height - panelHeight) / 2 - 8);

        RenderUtils.drawRect(
            context,
            panelX,
            panelY,
            panelWidth,
            panelHeight,
            0xB0101010
        );
        RenderUtils.drawOutline(
            context,
            panelX,
            panelY,
            panelWidth,
            panelHeight,
            0xFF888888
        );

        int sourceCenter = panelX + panelWidth / 4;
        int targetCenter = panelX + panelWidth * 3 / 4;
        renderLargeItem(
            context,
            this.replacementContext.sourceStack(),
            sourceCenter - 24,
            panelY + 18,
            3.0F
        );
        renderLargeItem(
            context,
            this.target.stack(),
            targetCenter - 24,
            panelY + 18,
            3.0F
        );
        drawCentered(
            context,
            "\u2192",
            panelX + panelWidth / 2,
            panelY + 37,
            0xFFFFFFFF
        );
        drawCentered(
            context,
            ellipsize(
                this.replacementContext.sourceStack().getHoverName().getString(),
                panelWidth / 2 - 18
            ),
            sourceCenter,
            panelY + 75,
            0xFFFFFFFF
        );
        drawCentered(
            context,
            ellipsize(this.target.displayName(), panelWidth / 2 - 18),
            targetCenter,
            panelY + 75,
            0xFFFFFFFF
        );

        int countColor = this.affectedPositions > 0
            ? 0xFFFFFF55
            : 0xFFFF5555;
        drawCentered(
            context,
            this.affectedPositions > 0
                ? tr("lmr.gui.confirm.affected", this.affectedPositions)
                : tr("lmr.gui.error.no_matches"),
            this.width / 2,
            panelY + 101,
            countColor
        );
    }
}
