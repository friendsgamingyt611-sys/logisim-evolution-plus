package com.cburch.logisim.gui.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cburch.logisim.prefs.AppPreferences;
import org.junit.jupiter.api.Test;

public class ThemeManagerStartupTest {

  @Test
  void testPairedLafMapping() {
    assertEquals(
        ModernDarkLaf.class.getName(),
        ThemeManager.getPairedLaf(ModernLightLaf.class.getName(), true));
    assertEquals(
        ModernLightLaf.class.getName(),
        ThemeManager.getPairedLaf(ModernDarkLaf.class.getName(), false));
  }

  @Test
  void testSystemDarkDetectionOnLinux() {
    boolean isDark = ThemeManager.querySystemDarkNow();
    // On the test machine, gsettings returns 'prefer-dark' and portal returns u 1
    assertTrue(isDark, "querySystemDarkNow should detect dark theme on this system");
  }

  @Test
  void testColdStartAutoSync() {
    try {
      AppPreferences.AUTO_SYNC_THEME.set(true);
      AppPreferences.LookAndFeel.set(ModernLightLaf.class.getName());

      // Simulate Main startup pairing logic
      var initialLaf = AppPreferences.LookAndFeel.get();
      if (AppPreferences.AUTO_SYNC_THEME.get()) {
        final var sysDark = ThemeManager.querySystemDarkNow();
        final var paired = ThemeManager.getPairedLaf(initialLaf, sysDark);
        if (paired != null && !paired.equals(initialLaf)) {
          initialLaf = paired;
          AppPreferences.LookAndFeel.set(initialLaf);
        }
      }

      assertEquals(
          ModernDarkLaf.class.getName(),
          initialLaf,
          "Initial LookAndFeel should auto-align to ModernDarkLaf on dark OS cold start");
    } finally {
      // restore
      AppPreferences.AUTO_SYNC_THEME.set(true);
    }
  }
}
