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

    // 7 Tonal Tiers (Stage 28 Variation B Dark)
    final var canvas = new javax.swing.plaf.ColorUIResource(0x1F, 0x1F, 0x1F); // #1f1f1f
    final var well = new javax.swing.plaf.ColorUIResource(0x25, 0x28, 0x2C); // #25282c
    final var panel = new javax.swing.plaf.ColorUIResource(0x2A, 0x2D, 0x32); // #2a2d32
    final var chrome = new javax.swing.plaf.ColorUIResource(0x32, 0x35, 0x3B); // #32353b
    final var group = new javax.swing.plaf.ColorUIResource(0x3B, 0x3E, 0x45); // #3b3e45
    final var groupBorder = new javax.swing.plaf.ColorUIResource(0x4D, 0x51, 0x5A); // #4d515a (gb)
    final var borderStrong = new javax.swing.plaf.ColorUIResource(0x4D, 0x51, 0x5A); // #4d515a (bs)
    final var borderSubtle = new javax.swing.plaf.ColorUIResource(0x38, 0x3B, 0x41); // #383b41 (bsub)
    final var mutedText = new javax.swing.plaf.ColorUIResource(0xAC, 0xAC, 0xAC); // #acacac (muted)

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
    defaults.put("Chrome.warning", new javax.swing.plaf.ColorUIResource(0xE5, 0xA5, 0x0A));
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

    // Semantic action icon tokens (Variation B Dark)
    defaults.put("Action.create", new javax.swing.plaf.ColorUIResource(0x4C, 0xD0, 0x80)); // #4cd080
    defaults.put("Action.move", new javax.swing.plaf.ColorUIResource(0x62, 0xA0, 0xEA));   // #62a0ea
    defaults.put("Action.edit", new javax.swing.plaf.ColorUIResource(0xF0, 0xA0, 0x30));   // #f0a030
    defaults.put("Action.destroy", new javax.swing.plaf.ColorUIResource(0xF6, 0x61, 0x51)); // #f66151

    // Underline tabbed pane styling
    defaults.put("TabbedPane.tabType", "underline");
    defaults.put("TabbedPane.underlineColor", new javax.swing.plaf.ColorUIResource(0x35, 0x84, 0xE4));
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
