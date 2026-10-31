/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.gui.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cburch.logisim.comp.Palette;
import com.cburch.logisim.gui.generic.GridPainter;
import com.cburch.logisim.prefs.AppPreferences;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.junit.jupiter.api.Test;

public class Stage2ThemeSyncTest {

  @Test
  void testCanvasBgDefaultDetection() {
    // Standard white and dark tokens
    assertTrue(AppPreferences.isCanvasBgDefault(0xFFFFFFFF), "0xFFFFFFFF must be default");
    assertTrue(AppPreferences.isCanvasBgDefault(-1), "-1 must be default");
    assertTrue(AppPreferences.isCanvasBgDefault(0xFF1F1F1F), "0xFF1F1F1F must be default");
    assertTrue(AppPreferences.isCanvasBgDefault(0xFFFCFCFB), "0xFFFCFCFB must be default");

    // Variation B and FlatLaf dark surface tokens
    assertTrue(AppPreferences.isCanvasBgDefault(0xFF2B2D33), "0xFF2B2D33 must be default");
    assertTrue(AppPreferences.isCanvasBgDefault(0xFF2A2D32), "0xFF2A2D32 must be default");
    assertTrue(AppPreferences.isCanvasBgDefault(0xFF25282C), "0xFF25282C must be default");
    assertTrue(AppPreferences.isCanvasBgDefault(0xFF2B2D30), "0xFF2B2D30 must be default");
    assertTrue(AppPreferences.isCanvasBgDefault(0xFF2B2B2B), "0xFF2B2B2B must be default");
    assertTrue(AppPreferences.isCanvasBgDefault(-13948117), "-13948117 must be default");

    // Palette background tokens
    assertTrue(AppPreferences.isCanvasBgDefault(Palette.DARK.background().getRGB()), "Dark palette background must be default");
    assertTrue(AppPreferences.isCanvasBgDefault(Palette.LIGHT.background().getRGB()), "Light palette background must be default");

    // Custom colors must not be default
    assertFalse(AppPreferences.isCanvasBgDefault(0xFFFF0000), "Red should not be default");
    assertFalse(AppPreferences.isCanvasBgDefault(0xFF00FF00), "Green should not be default");
    assertFalse(AppPreferences.isCanvasBgDefault(0xFF123456), "Custom hex should not be default");

    // Grid background default checks
    assertTrue(AppPreferences.isGridBgDefault(0xFFFFFFFF));
    assertTrue(AppPreferences.isGridBgDefault(0xFF2B2D33));
    assertTrue(AppPreferences.isGridBgDefault(0xFF2B2D30));
    assertTrue(AppPreferences.isGridBgDefault(0xFF2B2B2B));
    assertTrue(AppPreferences.isGridBgDefault(-13948117));
    assertFalse(AppPreferences.isGridBgDefault(0xFFFF5555));

    // Component icon default checks
    assertTrue(AppPreferences.isComponentIconDefault(0));
    assertTrue(AppPreferences.isComponentIconDefault(-1));
    assertTrue(AppPreferences.isComponentIconDefault(0xFFFFFFFF));
    assertTrue(AppPreferences.isComponentIconDefault(0xFF000000));
    assertTrue(AppPreferences.isComponentIconDefault(-16777216));
    assertFalse(AppPreferences.isComponentIconDefault(0xFFFF0000));
  }

  @Test
  void testPairedLafBiDirectional() {
    assertEquals(
        ModernDarkLaf.class.getName(),
        ThemeManager.getPairedLaf(ModernLightLaf.class.getName(), true));
    assertEquals(
        ModernLightLaf.class.getName(),
        ThemeManager.getPairedLaf(ModernDarkLaf.class.getName(), false));

    assertEquals(
        FlatDarkLaf.class.getName(),
        ThemeManager.getPairedLaf(FlatLightLaf.class.getName(), true));
    assertEquals(
        FlatLightLaf.class.getName(),
        ThemeManager.getPairedLaf(FlatDarkLaf.class.getName(), false));

    assertEquals(
        FlatDarculaLaf.class.getName(),
        ThemeManager.getPairedLaf(FlatIntelliJLaf.class.getName(), true));
    assertEquals(
        FlatIntelliJLaf.class.getName(),
        ThemeManager.getPairedLaf(FlatDarculaLaf.class.getName(), false));

    assertEquals(
        FlatMacDarkLaf.class.getName(),
        ThemeManager.getPairedLaf(FlatMacLightLaf.class.getName(), true));
    assertEquals(
        FlatMacLightLaf.class.getName(),
        ThemeManager.getPairedLaf(FlatMacDarkLaf.class.getName(), false));
  }

  @Test
  void testThemeSyncListenerInstallation() {
    ThemeSyncListener.install();
    assertNotNull(ThemeSyncListener.getInstance());

    // Verify it handles lookAndFeel property changes without throwing exceptions
    PropertyChangeEvent event = new PropertyChangeEvent(UIManager.class, "lookAndFeel", null, null);
    ThemeSyncListener.getInstance().propertyChange(event);
  }

  @Test
  void testGridPainterInvalidateGrid() {
    Component dummy = new JPanel();
    GridPainter painter = new GridPainter(dummy);
    painter.invalidateGrid();

    // Verify property change for lookAndFeel doesn't fail
    PropertyChangeEvent event = new PropertyChangeEvent(UIManager.class, "lookAndFeel", null, null);
    painter.propertyChange(event);
  }
}
