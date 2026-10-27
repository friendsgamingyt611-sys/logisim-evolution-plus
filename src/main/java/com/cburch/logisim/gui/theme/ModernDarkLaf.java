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

    // 7 Tonal Tiers (Stage 26)
    Color canvas = new Color(0x1F, 0x1F, 0x1F);
    Color well = new Color(0x22, 0x24, 0x28);
    Color panel = new Color(0x27, 0x29, 0x2D);
    Color chrome = new Color(0x30, 0x33, 0x38);
    Color group = new Color(0x3B, 0x3E, 0x45);
    Color borderStrong = new Color(0x14, 0x15, 0x17);
    Color borderSubtle = new Color(0x39, 0x3C, 0x42);
    Color mutedText = new Color(0xAC, 0xAC, 0xAC);

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
    defaults.put("Chrome.warning", new Color(0xE5, 0xA5, 0x0A));
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

    // Semantic action icon tokens tuned for high visibility and contrast
    defaults.put("Action.create", new Color(0x52, 0xD6, 0x8A));
    defaults.put("Action.move", new Color(0x58, 0xA6, 0xFF));
    defaults.put("Action.edit", new Color(0xFF, 0xB3, 0x47));
    defaults.put("Action.destroy", new Color(0xFF, 0x6B, 0x6B));

    // Underline tabbed pane styling
    defaults.put("TabbedPane.tabType", "underline");
    defaults.put("TabbedPane.underlineColor", new Color(0x35, 0x84, 0xE4));
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
