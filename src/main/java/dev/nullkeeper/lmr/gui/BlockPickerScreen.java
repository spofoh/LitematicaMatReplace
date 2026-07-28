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

import dev.nullkeeper.lmr.BlockCatalog;
import dev.nullkeeper.lmr.BlockCatalog.Candidate;
import dev.nullkeeper.lmr.FeatureGuard;
import dev.nullkeeper.lmr.ReplacementContext;
import dev.nullkeeper.lmr.ReplacementService;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.render.GuiContext;
import fi.dy.masa.malilib.render.RenderUtils;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class BlockPickerScreen extends LmrGuiBase {
    private static final int CELL_SIZE = 52;
    private static final int GRID_MARGIN = 18;
    private static final int GRID_TOP = 58;
    private static final int BOTTOM_MARGIN = 34;

    private final ReplacementContext replacementContext;
    private final List<Candidate> allCandidates;
    private List<Candidate> filteredCandidates;
    private GuiTextFieldGeneric searchField;
    private String query = "";
    private int scrollRow;
    private int columns;
    private int visibleRows;
    private Candidate hoveredCandidate;

    public BlockPickerScreen(ReplacementContext replacementContext) {
        this.replacementContext = replacementContext;
        this.allCandidates = BlockCatalog.allPlaceableBlocks();
        this.filteredCandidates = this.allCandidates;
        this.title = tr(
            "lmr.gui.picker.title",
            replacementContext.sourceStack().getHoverName().getString()
        );
    }

    @Override
    protected String screenHookName() {
        return "block-picker";
    }

    @Override
    protected void initLmrGui() {
        int searchWidth = Math.min(320, this.width - GRID_MARGIN * 2 - 110);
        int searchX = (this.width - searchWidth) / 2;
        this.searchField = new GuiTextFieldGeneric(
            searchX,
            30,
            searchWidth,
            20,
            this.font
        );
        this.searchField.setHint(Component.literal(tr("lmr.gui.picker.search_hint")));
        this.searchField.setMaxLengthWrapper(128);
        this.searchField.setValueWrapper(this.query);
        this.searchField.setFocusedWrapper(true);
        this.searchField.setResponder(value ->
            FeatureGuard.run(
                "block-picker native search change",
                () -> updateQuery(value)
            )
        );
        this.addTextField(this.searchField, field -> {
            return FeatureGuard.call(
                "block-picker search change",
                () -> {
                    updateQuery(field.getValueWrapper());
                    return true;
                },
                false
            );
        });

        int cancelWidth = 90;
        this.addButton(
            new ButtonGeneric(
                this.width - GRID_MARGIN - cancelWidth,
                this.height - 27,
                cancelWidth,
                20,
                tr("lmr.gui.button.cancel")
            ),
            (button, mouseButton) -> FeatureGuard.run(
                "block-picker Cancel button",
                () -> GuiBase.openGui(getParent())
            )
        );

        updateGridDimensions();
    }

    @Override
    protected void drawLmrTitle(
        GuiContext context,
        int mouseX,
        int mouseY,
        float partialTicks
    ) {
        drawCentered(context, this.title, this.width / 2, 10, 0xFFFFFFFF);
    }

    @Override
    protected void drawLmrContents(
        GuiContext context,
        int mouseX,
        int mouseY,
        float partialTicks
    ) {
        drawPicker(context, mouseX, mouseY);
    }

    private void drawPicker(GuiContext context, int mouseX, int mouseY) {
        updateGridDimensions();
        this.hoveredCandidate = null;

        int gridWidth = this.columns * CELL_SIZE;
        int gridLeft = (this.width - gridWidth) / 2;
        int gridHeight = this.visibleRows * CELL_SIZE;
        RenderUtils.drawRect(
            context,
            gridLeft - 2,
            GRID_TOP - 2,
            gridWidth + 4,
            gridHeight + 4,
            0xA0000000
        );

        int firstIndex = this.scrollRow * this.columns;
        int visibleCount = this.visibleRows * this.columns;
        int lastIndex = Math.min(
            this.filteredCandidates.size(),
            firstIndex + visibleCount
        );

        for (int index = firstIndex; index < lastIndex; index++) {
            int visibleIndex = index - firstIndex;
            int column = visibleIndex % this.columns;
            int row = visibleIndex / this.columns;
            int x = gridLeft + column * CELL_SIZE;
            int y = GRID_TOP + row * CELL_SIZE;
            Candidate candidate = this.filteredCandidates.get(index);
            boolean hovered = mouseX >= x
                && mouseX < x + CELL_SIZE
                && mouseY >= y
                && mouseY < y + CELL_SIZE;

            RenderUtils.drawRect(
                context,
                x + 1,
                y + 1,
                CELL_SIZE - 2,
                CELL_SIZE - 2,
                hovered ? 0xA0555555 : 0x80303030
            );
            RenderUtils.drawOutline(
                context,
                x + 1,
                y + 1,
                CELL_SIZE - 2,
                CELL_SIZE - 2,
                hovered ? 0xFFFFFFFF : 0xFF777777
            );

            renderLargeItem(context, candidate.stack(), x + 10, y + 3, 2.0F);
            drawCentered(
                context,
                ellipsize(candidate.displayName(), CELL_SIZE - 6),
                x + CELL_SIZE / 2,
                y + 39,
                0xFFFFFFFF
            );

            if (hovered) {
                this.hoveredCandidate = candidate;
            }
        }

        if (this.filteredCandidates.isEmpty()) {
            drawCentered(
                context,
                tr("lmr.gui.picker.empty"),
                this.width / 2,
                GRID_TOP + gridHeight / 2,
                0xFFAAAAAA
            );
        }

        this.drawString(
            context,
            tr("lmr.gui.picker.results", this.filteredCandidates.size()),
            GRID_MARGIN,
            this.height - 22,
            0xFFAAAAAA
        );

        if (this.hoveredCandidate != null) {
            context.renderTooltip(
                this.font,
                List.of(
                    Component.literal(this.hoveredCandidate.displayName()),
                    Component.literal(this.hoveredCandidate.id())
                ),
                mouseX,
                mouseY
            );
        }
    }

    @Override
    public boolean onMouseClicked(MouseButtonEvent click, boolean doubleClick) {
        if (super.onMouseClicked(click, doubleClick)) {
            return true;
        }

        return FeatureGuard.call(
            "block-picker click",
            () -> selectCandidate(click),
            false
        );
    }

    private boolean selectCandidate(MouseButtonEvent click) {
        if (click.input() != 0) {
            return false;
        }

        updateGridDimensions();
        int gridWidth = this.columns * CELL_SIZE;
        int gridLeft = (this.width - gridWidth) / 2;
        int gridHeight = this.visibleRows * CELL_SIZE;
        int mouseX = (int) click.x();
        int mouseY = (int) click.y();
        if (mouseX < gridLeft
            || mouseX >= gridLeft + gridWidth
            || mouseY < GRID_TOP
            || mouseY >= GRID_TOP + gridHeight) {
            return false;
        }

        int column = (mouseX - gridLeft) / CELL_SIZE;
        int row = (mouseY - GRID_TOP) / CELL_SIZE;
        int index = (this.scrollRow + row) * this.columns + column;
        if (index < 0 || index >= this.filteredCandidates.size()) {
            return false;
        }

        Candidate selected = this.filteredCandidates.get(index);
        long affected = ReplacementService.countMatches(this.replacementContext);
        ConfirmationScreen confirmation = new ConfirmationScreen(
            this.replacementContext,
            selected,
            affected
        );
        confirmation.setParent(this);
        GuiBase.openGui(confirmation);
        return true;
    }

    @Override
    public boolean onMouseScrolled(
        double mouseX,
        double mouseY,
        double horizontalAmount,
        double verticalAmount
    ) {
        updateGridDimensions();
        int gridHeight = this.visibleRows * CELL_SIZE;
        if (mouseY >= GRID_TOP && mouseY < GRID_TOP + gridHeight) {
            return FeatureGuard.call(
                "block-picker scroll",
                () -> {
                    int direction = verticalAmount > 0 ? -1 : 1;
                    this.scrollRow = clampScroll(this.scrollRow + direction);
                    return true;
                },
                false
            );
        }

        return super.onMouseScrolled(
            mouseX,
            mouseY,
            horizontalAmount,
            verticalAmount
        );
    }

    private void updateQuery(String value) {
        if (Objects.equals(this.query, value)) {
            return;
        }

        this.query = value;
        applyFilter();
    }

    private void applyFilter() {
        this.filteredCandidates = BlockCatalog.filter(this.allCandidates, this.query);
        this.scrollRow = 0;
        updateGridDimensions();
    }

    private void updateGridDimensions() {
        this.columns = Math.max(1, (this.width - GRID_MARGIN * 2) / CELL_SIZE);
        this.visibleRows = Math.max(
            1,
            (this.height - GRID_TOP - BOTTOM_MARGIN) / CELL_SIZE
        );
        this.scrollRow = clampScroll(this.scrollRow);
    }

    private int clampScroll(int requested) {
        int totalRows = (this.filteredCandidates.size() + this.columns - 1)
            / this.columns;
        int maximum = Math.max(0, totalRows - this.visibleRows);
        return Math.max(0, Math.min(requested, maximum));
    }
}
