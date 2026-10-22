package com.cburch.logisim.gui.theme;

import com.formdev.flatlaf.FlatDarkLaf;

/**
 * Modern Dark Look and Feel for Logisim-Evolution.
 * Implements clean, modern dark mode tokens, colors, and geometry.
 */
public class ModernDarkLaf extends FlatDarkLaf {
  public static final String NAME = "Modern Dark";

  public static boolean setup() {
    return setup(new ModernDarkLaf());
  }

  public static void installLafInfo() {
    installLafInfo(NAME, ModernDarkLaf.class);
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getDescription() {
    return "Modern Dark Look and Feel";
  }

  @Override
  public boolean isDark() {
    return true;
  }
}
