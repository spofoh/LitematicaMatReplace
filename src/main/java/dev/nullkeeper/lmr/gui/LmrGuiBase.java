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

import dev.nullkeeper.lmr.FeatureGuard;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.render.GuiContext;
import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.world.item.ItemStack;

abstract class LmrGuiBase extends GuiBase {
    protected LmrGuiBase() {
        this.useTitleHierarchy = false;
    }

    protected static String tr(String key, Object... arguments) {
        return StringUtils.translate(key, arguments);
    }

    @Override
    public final void initGui() {
        super.initGui();
        FeatureGuard.run(screenHookName() + " initialization", this::initLmrGui);
    }

    @Override
    protected final void drawTitle(
        GuiContext context,
        int mouseX,
        int mouseY,
        float partialTicks
    ) {
        FeatureGuard.run(
            screenHookName() + " title render",
            () -> drawLmrTitle(context, mouseX, mouseY, partialTicks)
        );
    }

    @Override
    protected final void drawContents(
        GuiContext context,
        int mouseX,
        int mouseY,
        float partialTicks
    ) {
        if (drawDisabledMessage(context)) {
            return;
        }

        FeatureGuard.run(
            screenHookName() + " content render",
            () -> drawLmrContents(context, mouseX, mouseY, partialTicks)
        );
    }

    protected abstract String screenHookName();

    protected abstract void initLmrGui();

    protected abstract void drawLmrTitle(
        GuiContext context,
        int mouseX,
        int mouseY,
        float partialTicks
    );

    protected abstract void drawLmrContents(
        GuiContext context,
        int mouseX,
        int mouseY,
        float partialTicks
    );

    protected void drawCentered(
        GuiContext context,
        String text,
        int centerX,
        int y,
        int color
    ) {
        context.drawCenteredString(this.font, text, centerX, y, color);
    }

    protected void renderLargeItem(
        GuiContext context,
        ItemStack stack,
        int x,
        int y,
        float scale
    ) {
        context.pose().pushMatrix();
        context.pose().translate(x, y);
        context.pose().scale(scale, scale);
        context.renderItem(stack, 0, 0);
        context.pose().popMatrix();
    }

    protected String ellipsize(String value, int maxWidth) {
        if (this.font.width(value) <= maxWidth) {
            return value;
        }

        String suffix = "...";
        int end = value.length();
        while (end > 0 && this.font.width(value.substring(0, end) + suffix) > maxWidth) {
            end--;
        }
        return value.substring(0, end) + suffix;
    }

    protected boolean drawDisabledMessage(GuiContext context) {
        if (FeatureGuard.isEnabled()) {
            return false;
        }

        drawCentered(
            context,
            tr("lmr.gui.error.disabled"),
            this.width / 2,
            this.height / 2,
            0xFFFF5555
        );
        return true;
    }
}
