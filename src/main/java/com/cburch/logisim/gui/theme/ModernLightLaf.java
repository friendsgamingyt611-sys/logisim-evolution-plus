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

    // 7 Tonal Tiers (Stage 28 Variation B)
    Color canvas = new Color(0xFF, 0xFF, 0xFF); // #ffffff
    Color well = new Color(0xFF, 0xFF, 0xFF); // #ffffff
    Color panel = new Color(0xF2, 0xF3, 0xF5); // #f2f3f5
    Color chrome = new Color(0xE4, 0xE6, 0xEA); // #e4e6ea
    Color group = new Color(0xFF, 0xFF, 0xFF); // #ffffff
    Color groupBorder = new Color(0xC0, 0xC5, 0xCE); // #c0c5ce (gb)
    Color borderStrong = new Color(0xC4, 0xC8, 0xD0); // #c4c8d0 (bs)
    Color borderSubtle = new Color(0xDA, 0xDD, 0xE3); // #dadde3 (bsub)
    Color mutedText = new Color(0x54, 0x5A, 0x64); // #545a64 (muted)

    defaults.put("Surface.canvas", canvas);
    defaults.put("Surface.well", well);
    defaults.put("Surface.panel", panel);
    defaults.put("Surface.chrome", chrome);
    defaults.put("Surface.group", group);
    defaults.put("Surface.groupBorder", groupBorder);
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
    defaults.put("Group.borderColor", groupBorder);
    defaults.put("Button.borderColor", groupBorder);

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

    // Semantic action icon tokens (Variation B)
    defaults.put("Action.create", new Color(0x1E, 0x8E, 0x4E)); // #1e8e4e
    defaults.put("Action.move", new Color(0x1C, 0x71, 0xD8));   // #1c71d8
    defaults.put("Action.edit", new Color(0xC2, 0x5A, 0x00));   // #c25a00
    defaults.put("Action.destroy", new Color(0xC0, 0x1C, 0x28)); // #c01c28

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
