/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.gui.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cburch.logisim.comp.Palette;
import com.cburch.logisim.fpga.gui.HdlColorRenderer;
import com.cburch.logisim.prefs.AppPreferences;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Stage7AcceptanceTest {

  private String originalLaf;
  private int originalCanvasBg;
  private int originalGridBg;

  @BeforeEach
  void setUp() {
    originalLaf = AppPreferences.LookAndFeel.get();
    originalCanvasBg = AppPreferences.CANVAS_BG_COLOR.get();
    originalGridBg = AppPreferences.GRID_BG_COLOR.get();
  }

  @AfterEach
  void tearDown() {
    AppPreferences.LookAndFeel.set(originalLaf);
    AppPreferences.CANVAS_BG_COLOR.set(originalCanvasBg);
    AppPreferences.GRID_BG_COLOR.set(originalGridBg);
  }

  @Test
  void testCanvasBgDynamicThemeResolution() throws Exception {
    // 1. In Dark Mode, default / theme colors resolve to dark surface (#1F1F1F)
    UIManager.setLookAndFeel(new com.cburch.logisim.gui.theme.ModernDarkLaf());
    AppPreferences.LookAndFeel.set(com.cburch.logisim.gui.theme.ModernDarkLaf.class.getName());
    AppPreferences.CANVAS_BG_COLOR.set(AppPreferences.DEFAULT_CANVAS_BG_COLOR);
    AppPreferences.GRID_BG_COLOR.set(AppPreferences.DEFAULT_GRID_BG_COLOR);

    assertTrue(AppPreferences.isDarkTheme(), "Should be dark theme");
    assertEquals(new Color(AppPreferences.DARK_CANVAS_BG_COLOR), AppPreferences.getCanvasBgColor());
    assertEquals(new Color(AppPreferences.DARK_CANVAS_BG_COLOR), AppPreferences.getGridBgColor());

    // 2. Switch to Light Mode without changing saved color preferences (they remain default/theme values)
    UIManager.setLookAndFeel(new com.cburch.logisim.gui.theme.ModernLightLaf());
    AppPreferences.LookAndFeel.set(com.cburch.logisim.gui.theme.ModernLightLaf.class.getName());

    assertEquals(Color.WHITE, AppPreferences.getCanvasBgColor(), "Canvas background must resolve to WHITE in Light Mode");
    assertEquals(Color.WHITE, AppPreferences.getGridBgColor(), "Grid background must resolve to WHITE in Light Mode");

    // 3. Even if CANVAS_BG_COLOR was stored as DARK_CANVAS_BG_COLOR (0xFF1F1F1F), it must dynamically resolve to WHITE in Light Mode
    AppPreferences.CANVAS_BG_COLOR.set(AppPreferences.DARK_CANVAS_BG_COLOR);
    AppPreferences.GRID_BG_COLOR.set(AppPreferences.DARK_CANVAS_BG_COLOR);
    assertEquals(Color.WHITE, AppPreferences.getCanvasBgColor(), "Canvas background must resolve to WHITE in Light Mode even if previously stored as dark");
    assertEquals(Color.WHITE, AppPreferences.getGridBgColor(), "Grid background must resolve to WHITE in Light Mode even if previously stored as dark");
  }

  @Test
  void testHdlColorRendererSelectedRowContrast() throws Exception {
    // Test Light Mode table row selection contrast
    UIManager.setLookAndFeel(new com.cburch.logisim.gui.theme.ModernLightLaf());
    AppPreferences.LookAndFeel.set(com.cburch.logisim.gui.theme.ModernLightLaf.class.getName());

    final var renderer = new HdlColorRenderer();
    final var table = new JTable(2, 2);

    // Row 0: FPGA supported (label, column 0) and Supported (pill, column 1)
    final var labelComp = (JLabel) renderer.getTableCellRendererComponent(
        table, "FPGA supported", true, false, 0, 0);
    // Background must be a subtle translucent tint (not opaque saturated blue)
    final var bg = labelComp.getBackground();
    assertNotEquals(255, bg.getAlpha(), "Selected table background must be translucent tint");

    // Status pill component
    final var pillComp = (JLabel) renderer.getTableCellRendererComponent(
        table, HdlColorRenderer.SUPPORT_STRING, true, false, 0, 1);
    assertEquals(bg, pillComp.getBackground());
  }
}
