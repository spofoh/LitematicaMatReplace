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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public final class BlockCatalog {
    private BlockCatalog() {
    }

    public static List<Candidate> allPlaceableBlocks() {
        List<Candidate> candidates = new ArrayList<>();

        for (Block block : BuiltInRegistries.BLOCK) {
            Item item = block.asItem();
            if (!(item instanceof BlockItem blockItem) || blockItem.getBlock() != block) {
                continue;
            }

            Identifier identifier = BuiltInRegistries.BLOCK.getKey(block);
            ItemStack stack = new ItemStack(item);
            String displayName = stack.getHoverName().getString();
            candidates.add(new Candidate(
                block,
                stack,
                displayName,
                identifier.toString(),
                normalize(displayName),
                normalize(identifier.toString())
            ));
        }

        candidates.sort(
            Comparator.comparing(Candidate::normalizedName)
                .thenComparing(Candidate::normalizedId)
        );
        return List.copyOf(candidates);
    }

    public static List<Candidate> filter(List<Candidate> all, String query) {
        String normalizedQuery = normalize(query).trim();
        if (normalizedQuery.isEmpty()) {
            return all;
        }

        return all.stream()
            .map(candidate -> new ScoredCandidate(
                candidate,
                score(candidate, normalizedQuery)
            ))
            .filter(scored -> scored.score() < Integer.MAX_VALUE)
            .sorted(
                Comparator.comparingInt(ScoredCandidate::score)
                    .thenComparing(scored -> scored.candidate().normalizedName())
                    .thenComparing(scored -> scored.candidate().normalizedId())
            )
            .map(ScoredCandidate::candidate)
            .toList();
    }

    private static int score(Candidate candidate, String query) {
        int nameScore = scoreText(candidate.normalizedName(), query);
        int idScore = scoreText(candidate.normalizedId(), query);
        int biasedIdScore = idScore == Integer.MAX_VALUE
            ? Integer.MAX_VALUE
            : idScore + 2;
        return Math.min(nameScore, biasedIdScore);
    }

    private static int scoreText(String value, String query) {
        if (value.equals(query)) {
            return 0;
        }
        if (value.startsWith(query)) {
            return 10 + value.length() - query.length();
        }

        int containedAt = value.indexOf(query);
        if (containedAt >= 0) {
            return 100 + containedAt + value.length() - query.length();
        }

        int valueIndex = 0;
        int gapPenalty = 0;
        for (int queryIndex = 0; queryIndex < query.length(); queryIndex++) {
            char wanted = query.charAt(queryIndex);
            int foundAt = value.indexOf(wanted, valueIndex);
            if (foundAt < 0) {
                return Integer.MAX_VALUE;
            }
            gapPenalty += foundAt - valueIndex;
            valueIndex = foundAt + 1;
        }

        return 1_000 + gapPenalty + value.length() - query.length();
    }

    private static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replace(' ', '_');
    }

    public record Candidate(
        Block block,
        ItemStack stack,
        String displayName,
        String id,
        String normalizedName,
        String normalizedId
    ) {
    }

    private record ScoredCandidate(Candidate candidate, int score) {
    }
}
