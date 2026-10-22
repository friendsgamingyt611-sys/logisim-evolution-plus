package com.cburch.logisim.gui.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.UIManager;
import org.junit.jupiter.api.Test;

class ModernLafTest {

  @Test
  void testModernLightLaf() {
    ModernLightLaf laf = new ModernLightLaf();
    assertEquals("Modern Light", laf.getName());
    assertFalse(laf.isDark());

    ModernLightLaf.installLafInfo();
    boolean found = false;
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      if ("Modern Light".equals(info.getName())) {
        found = true;
        assertEquals(ModernLightLaf.class.getName(), info.getClassName());
        break;
      }
    }
    assertTrue(found, "Modern Light should be registered in UIManager");
  }

  @Test
  void testModernDarkLaf() {
    ModernDarkLaf laf = new ModernDarkLaf();
    assertEquals("Modern Dark", laf.getName());
    assertTrue(laf.isDark());

    ModernDarkLaf.installLafInfo();
    boolean found = false;
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      if ("Modern Dark".equals(info.getName())) {
        found = true;
        assertEquals(ModernDarkLaf.class.getName(), info.getClassName());
        break;
      }
    }
    assertTrue(found, "Modern Dark should be registered in UIManager");
  }

  @Test
  void testDefaultsLoaded() {
    ModernLightLaf lightLaf = new ModernLightLaf();
    var lightDefaults = lightLaf.getDefaults();
    assertNotNull(lightDefaults);

    ModernDarkLaf darkLaf = new ModernDarkLaf();
    var darkDefaults = darkLaf.getDefaults();
    assertNotNull(darkDefaults);
  }
}
