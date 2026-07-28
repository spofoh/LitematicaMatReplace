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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import dev.nullkeeper.lmr.BlockCatalog.Candidate;
import java.util.List;
import org.junit.jupiter.api.Test;

final class BlockCatalogTest {
    private static final Candidate ACACIA_BUTTON = candidate(
        "Acacia Button",
        "minecraft:acacia_button"
    );
    private static final Candidate GRASS_BLOCK = candidate(
        "Grass Block",
        "minecraft:grass_block"
    );
    private static final Candidate SHORT_GRASS = candidate(
        "Short Grass",
        "minecraft:short_grass"
    );

    @Test
    void exactDisplayNameSearchReturnsGrassBlockBeforeUnrelatedBlocks() {
        List<Candidate> results = BlockCatalog.filter(
            List.of(ACACIA_BUTTON, GRASS_BLOCK, SHORT_GRASS),
            "grass block"
        );

        assertFalse(results.isEmpty());
        assertEquals(GRASS_BLOCK, results.getFirst());
        assertFalse(results.contains(ACACIA_BUTTON));
    }

    @Test
    void namespacedIdSearchReturnsTheExactBlock() {
        List<Candidate> results = BlockCatalog.filter(
            List.of(ACACIA_BUTTON, GRASS_BLOCK, SHORT_GRASS),
            "minecraft:grass_block"
        );

        assertEquals(List.of(GRASS_BLOCK), results);
    }

    private static Candidate candidate(String name, String id) {
        return new Candidate(
            null,
            null,
            name,
            id,
            name.toLowerCase().replace(' ', '_'),
            id
        );
    }
}
