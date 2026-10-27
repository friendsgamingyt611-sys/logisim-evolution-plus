/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.gui.main;

import com.cburch.logisim.Main;
import java.util.Locale;
import java.util.prefs.Preferences;

/**
 * Isolated bootstrap that initializes hardware-accelerated 2D graphics pipelines
 * before any AWT or Swing classes are initialized.
 * Includes crash-loop protection and manual override guards.
 */
public final class GraphicsBootstrap {

  public static final String PREF_GPU_ACCELERATION = "gpuAcceleration";
  public static final String PREF_TRIAL_IN_PROGRESS = "gpuTrialInProgress";

  private GraphicsBootstrap() {
    // Utility class
  }

  public static void init(String[] args) {
    // 1. Check command-line override
    if (args != null) {
      for (String arg : args) {
        if ("--no-gpu".equalsIgnoreCase(arg)) {
          disableGpuTrial();
          return;
        }
      }
    }

    // 2. Check environment variable override
    final var envOverride = System.getenv("LOGISIM_NO_GPU");
    if (envOverride != null
        && ("1".equals(envOverride) || "true".equalsIgnoreCase(envOverride) || "yes".equalsIgnoreCase(envOverride))) {
      disableGpuTrial();
      return;
    }

    // 3. Read Java Preferences directly without loading AWT/Swing
    try {
      final var prefs = Preferences.userNodeForPackage(Main.class);
      final var trialInProgress = prefs.getBoolean(PREF_TRIAL_IN_PROGRESS, false);
      final var gpuEnabled = prefs.getBoolean(PREF_GPU_ACCELERATION, false);

      // Crash-loop protection: if previous launch aborted while trial was in progress
      if (trialInProgress) {
        System.err.println("[GraphicsBootstrap] GPU acceleration crash-loop detected on previous launch. Reverting to safe software rendering.");
        prefs.putBoolean(PREF_GPU_ACCELERATION, false);
        prefs.putBoolean(PREF_TRIAL_IN_PROGRESS, false);
        prefs.flush();
        return;
      }

      if (!gpuEnabled) {
        return;
      }

      // Mark trial in progress before enabling GPU subsystem
      prefs.putBoolean(PREF_TRIAL_IN_PROGRESS, true);
      prefs.flush();

      // Enable platform-specific GPU acceleration pipelines
      final var os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
      if (os.contains("mac")) {
        System.setProperty("sun.java2d.opengl", "true");
        System.setProperty("sun.java2d.metal", "true");
      } else if (os.contains("win")) {
        System.setProperty("sun.java2d.d3d", "true");
        System.setProperty("sun.java2d.opengl", "true");
      } else {
        // Linux / X11 / Wayland
        System.setProperty("sun.java2d.opengl", "true");
      }

      // Register shutdown hook as an extra safety measure to clear trial flag
      Runtime.getRuntime().addShutdownHook(new Thread(GraphicsBootstrap::markStartupSuccessful));
    } catch (Throwable t) {
      System.err.println("[GraphicsBootstrap] Warning: Failed to configure GPU acceleration: " + t.getMessage());
    }
  }

  public static void markStartupSuccessful() {
    try {
      final var prefs = Preferences.userNodeForPackage(Main.class);
      if (prefs.getBoolean(PREF_TRIAL_IN_PROGRESS, false)) {
        prefs.putBoolean(PREF_TRIAL_IN_PROGRESS, false);
        prefs.flush();
      }
    } catch (Throwable ignored) {
      // Do not fail during shutdown or preference flush
    }
  }

  private static void disableGpuTrial() {
    try {
      final var prefs = Preferences.userNodeForPackage(Main.class);
      if (prefs.getBoolean(PREF_TRIAL_IN_PROGRESS, false)) {
        prefs.putBoolean(PREF_TRIAL_IN_PROGRESS, false);
        prefs.flush();
      }
    } catch (Throwable ignored) {
    }
  }
}
