/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.comp;

import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.prefs.AppPreferences;
import java.awt.Color;

public interface Palette {
  Color stroke();

  Color valueColor(Value v);

  Color label();

  Color pinOutline();

  Color fill();

  Color blueIndex();

  Color background();

  Color nil();

  Color widthError();

  Color bus();

  boolean isDark();

  Color switchBody();

  Color switchOutline();

  Color switchChannel(boolean on);

  Color switchActuator(boolean on);

  Color switchLabel();

  Palette DARK = new DarkPalette();
  Palette LIGHT = new LightPalette();

  ThreadLocal<Integer> FORCE_LIGHT_DEPTH = ThreadLocal.withInitial(() -> 0);

  static boolean isForceLight() {
    return FORCE_LIGHT_DEPTH.get() > 0;
  }

  @FunctionalInterface
  interface Scope extends AutoCloseable {
    @Override
    void close();
  }

  static Scope enterLightScope() {
    FORCE_LIGHT_DEPTH.set(FORCE_LIGHT_DEPTH.get() + 1);
    return () -> {
      final var depth = FORCE_LIGHT_DEPTH.get() - 1;
      if (depth <= 0) {
        FORCE_LIGHT_DEPTH.remove();
      } else {
        FORCE_LIGHT_DEPTH.set(depth);
      }
    };
  }

  static void withLightScope(Runnable r) {
    try (var ignored = enterLightScope()) {
      r.run();
    }
  }

  static Palette current() {
    return (isForceLight() || !AppPreferences.isDarkTheme()) ? LIGHT : DARK;
  }

  static Palette of(ComponentDrawContext context) {
    if (isForceLight() || (context != null && context.isPrintView())) {
      return LIGHT;
    }
    return current();
  }

  static Palette of(InstancePainter painter) {
    if (isForceLight() || (painter != null && painter.isPrintView())) {
      return LIGHT;
    }
    if (painter != null) {
      return painter.getPalette();
    }
    return current();
  }

  class DarkPalette implements Palette {
    private static final Color STROKE = new Color(0xE6E6E6);
    private static final Color WIRE_0 = new Color(0x2E9D62);
    private static final Color WIRE_1 = new Color(0x4BE38A);
    private static final Color UNKNOWN = new Color(0x5B9BFF);
    private static final Color ERROR = new Color(0xFF6B5E);
    private static final Color MISMATCH = new Color(0xF0A030);
    private static final Color LABEL = new Color(0x7AA7FF);
    private static final Color FILL = new Color(0x262626);
    private static final Color BG = new Color(0x1F1F1F);
    private static final Color NIL = new Color(0x888888);
    private static final Color SWITCH_RECESSED = new Color(0x141414);
    private static final Color SWITCH_ACTUATOR_ON = new Color(0xF0F0F0);
    private static final Color SWITCH_ACTUATOR_OFF = new Color(0x9E9E9E);

    @Override
    public boolean isDark() {
      return true;
    }

    @Override
    public Color switchBody() {
      return FILL;
    }

    @Override
    public Color switchOutline() {
      return stroke();
    }

    @Override
    public Color switchChannel(boolean on) {
      return SWITCH_RECESSED;
    }

    @Override
    public Color switchActuator(boolean on) {
      return on ? SWITCH_ACTUATOR_ON : SWITCH_ACTUATOR_OFF;
    }

    @Override
    public Color switchLabel() {
      return stroke();
    }

    @Override
    public Color stroke() {
      if (!AppPreferences.isAdaptComponentColor()) {
        return new Color(AppPreferences.COMPONENT_COLOR.get());
      }
      return STROKE;
    }

    @Override
    public Color bus() {
      final var custom = AppPreferences.BUS_COLOR.get();
      return (custom != 0) ? new Color(custom) : stroke();
    }

    @Override
    public Color valueColor(Value v) {
      if (v == null) return UNKNOWN;
      if (v.isErrorValue()) {
        final var custom = AppPreferences.ERROR_COLOR.get();
        return (custom != 0x00C00000) ? new Color(custom) : ERROR;
      }
      if (v.getWidth() == 0) return NIL;
      if (v.getWidth() == 1) {
        if (v == Value.UNKNOWN) {
          final var custom = AppPreferences.UNKNOWN_COLOR.get();
          return (custom != 0x002828FF) ? new Color(custom) : UNKNOWN;
        } else if (v == Value.TRUE) {
          final var custom = AppPreferences.TRUE_COLOR.get();
          return (custom != 0x0000D200) ? new Color(custom) : WIRE_1;
        } else if (v == Value.FALSE) {
          final var custom = AppPreferences.FALSE_COLOR.get();
          return (custom != 0x00006400) ? new Color(custom) : WIRE_0;
        }
      }
      if (v.isFullyDefined()) {
        final var custom = AppPreferences.BUS_COLOR.get();
        return (custom != 0) ? new Color(custom) : LABEL;
      }
      return MISMATCH;
    }

    @Override
    public Color label() {
      return LABEL;
    }

    @Override
    public Color pinOutline() {
      return stroke();
    }

    @Override
    public Color fill() {
      return FILL;
    }

    @Override
    public Color blueIndex() {
      return LABEL;
    }

    @Override
    public Color background() {
      return BG;
    }

    @Override
    public Color nil() {
      final var custom = AppPreferences.NIL_COLOR.get();
      return (custom != 0x808080) ? new Color(custom) : NIL;
    }

    @Override
    public Color widthError() {
      final var custom = AppPreferences.WIDTH_ERROR_COLOR.get();
      return (custom != 0xFF7B00) ? new Color(custom) : MISMATCH;
    }
  }

  class LightPalette implements Palette {
    private static final Color STROKE = Color.BLACK;
    private static final Color ERROR = new Color(0xC00000);
    private static final Color MISMATCH = new Color(0xFF7B00);
    private static final Color LABEL = Color.BLACK;
    private static final Color FILL = Color.WHITE;
    private static final Color BG = Color.WHITE;
    private static final Color NIL = Color.GRAY;

    @Override
    public Color stroke() {
      if (!AppPreferences.isAdaptComponentColor()) {
        return new Color(AppPreferences.COMPONENT_COLOR.get());
      }
      return STROKE;
    }

    @Override
    public Color bus() {
      final var custom = AppPreferences.BUS_COLOR.get();
      return (custom != 0) ? new Color(custom) : stroke();
    }

    @Override
    public Color valueColor(Value v) {
      if (v == null) return new Color(AppPreferences.UNKNOWN_COLOR.get());
      if (v.isErrorValue()) return new Color(AppPreferences.ERROR_COLOR.get());
      if (v.getWidth() == 0) return NIL;
      if (v.getWidth() == 1) {
        if (v == Value.UNKNOWN) return new Color(AppPreferences.UNKNOWN_COLOR.get());
        if (v == Value.TRUE) return new Color(AppPreferences.TRUE_COLOR.get());
        if (v == Value.FALSE) return new Color(AppPreferences.FALSE_COLOR.get());
      }
      if (v.isFullyDefined()) {
        final var custom = AppPreferences.BUS_COLOR.get();
        return (custom != 0) ? new Color(custom) : Color.BLACK;
      }
      return MISMATCH;
    }

    @Override
    public Color label() {
      return LABEL;
    }

    @Override
    public Color pinOutline() {
      return stroke();
    }

    @Override
    public boolean isDark() {
      return false;
    }

    @Override
    public Color switchBody() {
      return Color.DARK_GRAY;
    }

    @Override
    public Color switchOutline() {
      return null;
    }

    @Override
    public Color switchChannel(boolean on) {
      return on ? Value.trueColor : Color.white;
    }

    @Override
    public Color switchActuator(boolean on) {
      return on ? Color.DARK_GRAY : Color.GRAY;
    }

    @Override
    public Color switchLabel() {
      return Color.white;
    }

    @Override
    public Color fill() {
      return FILL;
    }

    @Override
    public Color blueIndex() {
      return Color.BLUE;
    }

    @Override
    public Color background() {
      return BG;
    }

    @Override
    public Color nil() {
      return new Color(AppPreferences.NIL_COLOR.get());
    }

    @Override
    public Color widthError() {
      return new Color(AppPreferences.WIDTH_ERROR_COLOR.get());
    }
  }
}
