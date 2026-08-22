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
import dev.nullkeeper.lmr.ReplacementBatch;
import dev.nullkeeper.lmr.ReplacementContext;
import dev.nullkeeper.lmr.ReplacementService;
import dev.nullkeeper.lmr.ReplacementService.SaveFailure;
import dev.nullkeeper.lmr.ReplacementService.SaveResult;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.gui.GuiMaterialList;
import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.litematica.util.FileType;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.Message;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.render.GuiContext;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.InfoUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class PersistenceScreen extends LmrGuiBase {
    private final ReplacementBatch batch;
    private final ReplacementContext replacementContext;
    private final Path originalFile;
    private final Path outputDirectory;
    private GuiTextFieldGeneric exportNameField;
    private String exportName;
    private String errorMessage;

    public PersistenceScreen(ReplacementBatch batch) {
        this.batch = batch;
        this.replacementContext = batch.context();
        this.originalFile = replacementContext.schematic().getFile();
        this.outputDirectory = this.originalFile != null
            && this.originalFile.getParent() != null
            ? this.originalFile.getParent()
            : DataManager.getSchematicsBaseDirectory();
        this.exportName = createAvailableExportName();
        this.title = tr("lmr.gui.persistence.title");
    }

    @Override
    protected String screenHookName() {
        return "persistence";
    }

    @Override
    protected void initLmrGui() {
        int panelWidth = Math.min(420, this.width - 30);
        int panelX = (this.width - panelWidth) / 2;
        int buttonWidth = Math.min(150, panelWidth / 2 - 12);
        int rowY = this.height / 2 - 44;

        ButtonGeneric overwriteButton = new ButtonGeneric(
            panelX + 8,
            rowY,
            buttonWidth,
            20,
            tr("lmr.gui.button.overwrite")
        );
        overwriteButton.setEnabled(canOverwriteOriginal());
        this.addButton(overwriteButton, (button, mouseButton) ->
            FeatureGuard.run(
                "persistence Overwrite button",
                () -> save(true)
            )
        );

        int fieldX = panelX + 8;
        int fieldY = rowY + 57;
        int exportButtonWidth = 120;
        int fieldWidth = panelWidth - exportButtonWidth - 22;
        this.exportNameField = new GuiTextFieldGeneric(
            fieldX,
            fieldY,
            fieldWidth,
            20,
            this.font
        );
        this.exportNameField.setHint(
            Component.literal(tr("lmr.gui.persistence.export_hint"))
        );
        this.exportNameField.setMaxLengthWrapper(128);
        this.exportNameField.setValueWrapper(this.exportName);
        this.addTextField(this.exportNameField, field -> {
            return FeatureGuard.call(
                "persistence export-name change",
                () -> {
                    this.exportName = field.getValueWrapper();
                    this.errorMessage = null;
                    return true;
                },
                false
            );
        });

        this.addButton(
            new ButtonGeneric(
                fieldX + fieldWidth + 6,
                fieldY,
                exportButtonWidth,
                20,
                tr("lmr.gui.button.export")
            ),
            (button, mouseButton) -> FeatureGuard.run(
                "persistence Export button",
                () -> save(false)
            )
        );

        this.addButton(
            new ButtonGeneric(
                this.width / 2 - 45,
                this.height - 31,
                90,
                20,
                tr("lmr.gui.button.cancel")
            ),
            (button, mouseButton) -> FeatureGuard.run(
                "persistence Cancel button",
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
        drawPersistenceOptions(context);
    }

    private void drawPersistenceOptions(GuiContext context) {
        int panelWidth = Math.min(420, this.width - 30);
        int panelHeight = 150;
        int panelX = (this.width - panelWidth) / 2;
        int panelY = this.height / 2 - 65;

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

        int textX = panelX + Math.min(170, panelWidth / 2);
        this.drawString(
            context,
            canOverwriteOriginal()
                ? tr("lmr.gui.persistence.overwrite_note")
                : tr("lmr.gui.error.no_file"),
            textX,
            panelY + 17,
            canOverwriteOriginal() ? 0xFFFF7777 : 0xFFAAAAAA
        );
        this.drawString(
            context,
            tr("lmr.gui.persistence.export_note"),
            panelX + 8,
            panelY + 62,
            0xFF77FF77
        );
        this.drawString(
            context,
            ellipsize(
                tr(
                    "lmr.gui.persistence.path",
                    this.outputDirectory.toAbsolutePath()
                ),
                panelWidth - 16
            ),
            panelX + 8,
            panelY + 116,
            0xFFAAAAAA
        );

        if (this.errorMessage != null) {
            drawCentered(
                context,
                this.errorMessage,
                this.width / 2,
                panelY + 134,
                0xFFFF5555
            );
        }
    }

    private void save(boolean overwrite) {
        String fileName;
        if (overwrite) {
            if (!canOverwriteOriginal()) {
                this.errorMessage = tr("lmr.gui.error.no_file");
                return;
            }
            fileName = this.originalFile.getFileName().toString();
        } else {
            fileName = this.exportName == null ? "" : this.exportName.trim();
            if (fileName.isEmpty()) {
                this.errorMessage = tr("lmr.gui.error.invalid_name");
                return;
            }

            Path exportFile = LitematicaSchematic.fileFromDirAndName(
                this.outputDirectory,
                fileName,
                FileType.LITEMATICA_SCHEMATIC
            );
            if (Files.exists(exportFile)) {
                this.errorMessage = tr("lmr.gui.error.export_exists");
                return;
            }
        }

        SaveResult result = FeatureGuard.call(
            "schematic save and apply",
            () -> ReplacementService.saveAndApply(
                this.batch,
                this.outputDirectory,
                fileName,
                overwrite
            ),
            null
        );
        if (result == null) {
            return;
        }
        if (!result.success()) {
            this.errorMessage = result.failure() == SaveFailure.NO_MATCHES
                ? tr("lmr.gui.error.no_matches")
                : tr("lmr.gui.error.save_failed");
            return;
        }

        InfoUtils.showGuiOrInGameMessage(
            Message.MessageType.SUCCESS,
            overwrite ? "lmr.gui.success.overwritten" : "lmr.gui.success.exported",
            result.changedPositions(),
            result.targetFile().getFileName()
        );
        this.batch.finish();
        openRefreshedMaterialList();
    }

    private void openRefreshedMaterialList() {
        GuiMaterialList refreshed = new GuiMaterialList(
            this.replacementContext.materialList()
        );
        Screen previousParent = this.replacementContext.materialListScreen()
            instanceof GuiBase materialGui
            ? materialGui.getParent()
            : null;
        refreshed.setParent(previousParent);
        GuiBase.openGui(refreshed);
    }

    private boolean canOverwriteOriginal() {
        return this.originalFile != null
            && FileType.fromFile(this.originalFile) == FileType.LITEMATICA_SCHEMATIC;
    }

    private String createAvailableExportName() {
        String baseName = this.originalFile != null
            ? this.originalFile.getFileName().toString()
            : this.replacementContext.schematic().getMetadata().getName();
        String lowerName = baseName.toLowerCase(Locale.ROOT);
        if (lowerName.endsWith(LitematicaSchematic.FILE_EXTENSION)) {
            baseName = baseName.substring(
                0,
                baseName.length() - LitematicaSchematic.FILE_EXTENSION.length()
            );
        }

        String candidate = baseName + "_replaced" + LitematicaSchematic.FILE_EXTENSION;
        int suffix = 2;
        while (Files.exists(this.outputDirectory.resolve(candidate))) {
            candidate = baseName
                + "_replaced_"
                + suffix
                + LitematicaSchematic.FILE_EXTENSION;
            suffix++;
        }
        return candidate;
    }
}
