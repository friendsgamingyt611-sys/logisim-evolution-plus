package com.cburch.logisim.gui.theme;

import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.Color;
import javax.swing.UIDefaults;

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

  @Override
  public UIDefaults getDefaults() {
    final var defaults = super.getDefaults();

    // Chrome and surface tokens
    defaults.put("Chrome.barBackground", new Color(0x25, 0x25, 0x26));
    defaults.put("Chrome.tabTrack", new Color(0x24, 0x24, 0x24));
    defaults.put("Chrome.tabBackground", new Color(0x28, 0x28, 0x28));
    defaults.put("Chrome.cardBackground", new Color(0x2E, 0x2E, 0x2E));
    defaults.put("Chrome.borderColor", new Color(0x30, 0x30, 0x30));
    defaults.put("Chrome.subtleBorderColor", new Color(0x30, 0x30, 0x30));
    defaults.put("Chrome.mutedText", new Color(0x9A, 0x99, 0x96));
    defaults.put("Chrome.warning", new Color(0xE5, 0xA5, 0x0A));

    // Underline tabbed pane styling
    defaults.put("TabbedPane.tabType", "underline");
    defaults.put("TabbedPane.underlineColor", new Color(0x35, 0x84, 0xE4));
    defaults.put("TabbedPane.showContentSeparators", true);

    // Properties table defaults
    defaults.put("Table.gridColor", new Color(0x30, 0x30, 0x30));
    defaults.put("Table.showHorizontalLines", true);
    defaults.put("Table.showVerticalLines", false);

    return defaults;
  }
}
