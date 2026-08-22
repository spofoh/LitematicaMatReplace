/*
 * LMR - Litematica Material Replace
 * Copyright (C) 2026 NullKeeper-dev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 only.
 */
package dev.nullkeeper.lmr;

import dev.nullkeeper.lmr.BlockCatalog.Candidate;
import fi.dy.masa.malilib.util.data.ItemType;
import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public final class ReplacementBatch {
    private static final Map<LitematicaSchematic, ReplacementBatch> ACTIVE =
        Collections.synchronizedMap(new WeakHashMap<>());

    private final ReplacementContext context;
    private final List<Operation> operations = new ArrayList<>();

    private ReplacementBatch(ReplacementContext context) {
        this.context = context;
    }

    public static ReplacementBatch forContext(ReplacementContext context) {
        return ACTIVE.computeIfAbsent(
            context.schematic(),
            ignored -> new ReplacementBatch(context)
        );
    }

    public ReplacementContext context() {
        return this.context;
    }

    public List<Operation> operations() {
        return List.copyOf(this.operations);
    }

    public int size() {
        return this.operations.size();
    }

    public void put(ReplacementContext source, Candidate target) {
        this.operations.removeIf(operation ->
            operation.sourceType().equals(source.sourceType())
        );
        this.operations.add(new Operation(
            source.sourceStack().copy(),
            source.sourceType(),
            target.stack().copy(),
            target.block()
        ));
    }

    public void finish() {
        ACTIVE.remove(this.context.schematic());
        this.operations.clear();
    }

    public record Operation(
        ItemStack sourceStack,
        ItemType sourceType,
        ItemStack targetStack,
        Block targetBlock
    ) {
    }
}
