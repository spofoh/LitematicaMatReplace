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

import fi.dy.masa.malilib.gui.Message;
import fi.dy.masa.malilib.util.InfoUtils;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public final class FeatureGuard {
    private static final String FEATURE_ID = "material-replace";
    private static final AtomicBoolean DISABLED = new AtomicBoolean();

    private FeatureGuard() {
    }

    public static boolean isEnabled() {
        return !DISABLED.get();
    }

    public static void run(String hook, Runnable action) {
        if (!isEnabled()) {
            return;
        }

        try {
            action.run();
        } catch (Throwable throwable) {
            disable(hook, throwable);
        }
    }

    public static <T> T call(String hook, Supplier<T> action, T fallback) {
        if (!isEnabled()) {
            return fallback;
        }

        try {
            return action.get();
        } catch (Throwable throwable) {
            disable(hook, throwable);
            return fallback;
        }
    }

    private static void disable(String hook, Throwable throwable) {
        if (DISABLED.compareAndSet(false, true)) {
            LmrClient.LOGGER.error(
                "LMR feature '{}' failed in hook '{}' and is disabled for this session",
                FEATURE_ID,
                hook,
                throwable
            );

            try {
                InfoUtils.showGuiOrInGameMessage(
                    Message.MessageType.ERROR,
                    "lmr.gui.error.disabled"
                );
            } catch (Throwable notificationFailure) {
                LmrClient.LOGGER.error(
                    "LMR could not display its feature-disable notification",
                    notificationFailure
                );
            }
        }
    }
}
