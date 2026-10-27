package com.cburch.logisim.gui.theme;

import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import javax.swing.UIDefaults;

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

  @Override
  public UIDefaults getDefaults() {
    final var defaults = super.getDefaults();

    // Chrome and surface tokens
    defaults.put("Chrome.barBackground", new Color(0xF3, 0xF4, 0xF6));
    defaults.put("Chrome.tabTrack", new Color(0xFA, 0xFA, 0xFA));
    defaults.put("Chrome.tabBackground", new Color(0xFA, 0xFA, 0xFA));
    defaults.put("Chrome.cardBackground", Color.WHITE);
    defaults.put("Chrome.borderColor", new Color(0xD0, 0xD5, 0xDD));
    defaults.put("Chrome.subtleBorderColor", new Color(0xE4, 0xE7, 0xEC));
    defaults.put("Chrome.mutedText", new Color(0x6A, 0x6A, 0x6F));
    defaults.put("Chrome.warning", new Color(0xB8, 0x7D, 0x00));

    // Underline tabbed pane styling
    defaults.put("TabbedPane.tabType", "underline");
    defaults.put("TabbedPane.underlineColor", new Color(0x1C, 0x71, 0xD8));
    defaults.put("TabbedPane.showContentSeparators", true);

    // Properties table defaults
    defaults.put("Table.gridColor", new Color(0xE4, 0xE7, 0xEC));
    defaults.put("Table.showHorizontalLines", true);
    defaults.put("Table.showVerticalLines", false);

    return defaults;
  }
}
