package com.cburch.logisim.gui.theme;

import com.formdev.flatlaf.FlatLightLaf;

/**
 * Modern Light Look and Feel for Logisim-Evolution.
 * Implements clean, modern desktop design tokens, colors, and geometry.
 */
public class ModernLightLaf extends FlatLightLaf {
  public static final String NAME = "Modern Light";

  public static boolean setup() {
    return setup(new ModernLightLaf());
  }

  public static void installLafInfo() {
    installLafInfo(NAME, ModernLightLaf.class);
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getDescription() {
    return "Modern Light Look and Feel";
  }

  @Override
  public boolean isDark() {
    return false;
  }
}
