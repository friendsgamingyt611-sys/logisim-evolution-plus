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

    // 7 Tonal Tiers (Stage 26)
    Color canvas = new Color(0xFF, 0xFF, 0xFF);
    Color well = new Color(0xFF, 0xFF, 0xFF);
    Color panel = new Color(0xF3, 0xF5, 0xF8);
    Color chrome = new Color(0xE6, 0xE9, 0xEE);
    Color group = new Color(0xF8, 0xF9, 0xFB);
    Color borderStrong = new Color(0xBC, 0xC3, 0xCD);
    Color borderSubtle = new Color(0xD5, 0xDA, 0xE2);
    Color mutedText = new Color(0x5A, 0x5D, 0x66);

    defaults.put("Surface.canvas", canvas);
    defaults.put("Surface.well", well);
    defaults.put("Surface.panel", panel);
    defaults.put("Surface.chrome", chrome);
    defaults.put("Surface.group", group);
    defaults.put("Surface.borderStrong", borderStrong);
    defaults.put("Surface.borderSubtle", borderSubtle);
    defaults.put("Surface.mutedText", mutedText);

    // Chrome and surface tokens
    defaults.put("Chrome.barBackground", chrome);
    defaults.put("Chrome.tabTrack", chrome);
    defaults.put("Chrome.tabBackground", chrome);
    defaults.put("Chrome.cardBackground", well);
    defaults.put("Chrome.borderColor", borderStrong);
    defaults.put("Chrome.subtleBorderColor", borderSubtle);
    defaults.put("Chrome.mutedText", mutedText);
    defaults.put("Chrome.warning", new Color(0xB8, 0x7D, 0x00));
    defaults.put("Group.background", group);

    // Component surface bindings
    defaults.put("MenuBar.background", chrome);
    defaults.put("MenuBar.borderColor", borderStrong);
    defaults.put("ToolBar.background", chrome);
    defaults.put("ToolBar.borderColor", borderStrong);
    defaults.put("Panel.background", panel);

    // Well surfaces
    defaults.put("TextField.background", well);
    defaults.put("Tree.background", well);
    defaults.put("Table.background", well);
    defaults.put("ScrollPane.background", well);
    defaults.put("Viewport.background", well);

    // Splitters
    defaults.put("SplitPane.dividerSize", 5);

    // Semantic action icon tokens
    defaults.put("Action.create", new Color(0x1E, 0x8E, 0x4E));
    defaults.put("Action.move", new Color(0x1C, 0x71, 0xD8));
    defaults.put("Action.edit", new Color(0xC2, 0x5A, 0x00));
    defaults.put("Action.destroy", new Color(0xC0, 0x1C, 0x28));

    // Underline tabbed pane styling
    defaults.put("TabbedPane.tabType", "underline");
    defaults.put("TabbedPane.underlineColor", new Color(0x1C, 0x71, 0xD8));
    defaults.put("TabbedPane.underlineHeight", 2);
    defaults.put("TabbedPane.background", chrome);
    defaults.put("TabbedPane.selectedBackground", panel);
    defaults.put("TabbedPane.contentAreaColor", panel);
    defaults.put("TabbedPane.showContentSeparators", true);

    // Properties table defaults
    defaults.put("Table.gridColor", borderSubtle);
    defaults.put("Table.showHorizontalLines", true);
    defaults.put("Table.showVerticalLines", false);

    return defaults;
  }
}
