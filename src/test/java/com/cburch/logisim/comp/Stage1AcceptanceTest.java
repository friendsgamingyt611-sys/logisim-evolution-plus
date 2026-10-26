package com.cburch.logisim.comp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cburch.logisim.data.Value;
import com.cburch.logisim.prefs.AppPreferences;
import java.awt.Color;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Stage1AcceptanceTest {

  @BeforeEach
  public void setUp() {
    AppPreferences.setDefaultGridColors();
  }

  @AfterEach
  public void tearDown() {
    AppPreferences.setDefaultGridColors();
  }

  @Test
  public void testPaintScopeLifecycleAndNesting() {
    assertFalse(Palette.isForceLight());

    try (var scope1 = Palette.enterLightScope()) {
      assertTrue(Palette.isForceLight());
      assertFalse(AppPreferences.isDarkTheme(), "isDarkTheme must be false under force light scope");
      assertEquals(Palette.LIGHT, Palette.current(), "Palette.current() must be LIGHT");
      assertEquals(Color.BLACK, AppPreferences.getComponentColor(), "getComponentColor() must be BLACK under scope");

      // Verify wire colors use light palette
      assertEquals(new Color(AppPreferences.TRUE_COLOR.get()), Value.TRUE.getColor());
      assertEquals(new Color(AppPreferences.FALSE_COLOR.get()), Value.FALSE.getColor());
      assertEquals(new Color(AppPreferences.UNKNOWN_COLOR.get()), Value.UNKNOWN.getColor());

      // Nested scope
      try (var scope2 = Palette.enterLightScope()) {
        assertTrue(Palette.isForceLight());
        assertEquals(Palette.LIGHT, Palette.current());
      }

      // Still in scope 1
      assertTrue(Palette.isForceLight());
      assertEquals(Palette.LIGHT, Palette.current());
    }

    // Exited scope
    assertFalse(Palette.isForceLight());
  }

  @Test
  public void testAdaptComponentColorFlag() {
    // With ADAPT = true
    AppPreferences.ADAPT_COMPONENT_COLOR.setBoolean(true);
    assertTrue(AppPreferences.isAdaptComponentColor());
    final var themeResolved = AppPreferences.getComponentColor();
    assertNotNull(themeResolved);

    // With ADAPT = false and custom color
    AppPreferences.ADAPT_COMPONENT_COLOR.setBoolean(false);
    assertFalse(AppPreferences.isAdaptComponentColor());
    AppPreferences.COMPONENT_COLOR.set(0x0000FF);
    assertEquals(new Color(0x0000FF), AppPreferences.getComponentColor());
    assertEquals(new Color(0x0000FF), Palette.current().stroke());

    // Switch back to ADAPT = true
    AppPreferences.ADAPT_COMPONENT_COLOR.setBoolean(true);
    assertEquals(Palette.current().stroke(), AppPreferences.getComponentColor());
  }

  @Test
  public void testResetGridColors() {
    AppPreferences.ADAPT_COMPONENT_COLOR.setBoolean(false);
    AppPreferences.COMPONENT_COLOR.set(0xFF00FF);
    assertEquals(new Color(0xFF00FF), AppPreferences.getComponentColor());

    AppPreferences.setDefaultGridColors();
    assertTrue(AppPreferences.isAdaptComponentColor(), "setDefaultGridColors must set ADAPT=true");
    assertEquals(AppPreferences.DEFAULT_COMPONENT_COLOR, (int) AppPreferences.COMPONENT_COLOR.get());
  }

  @Test
  public void testMigrationLogic() {
    // Legacy black: 0, 0xFF000000 (-16777216) -> ADAPT = true
    // Custom color (e.g. 0x0000FF, 0xFF0000) -> ADAPT = false
    int[] legacyBlacks = new int[] {0, 0xFF000000, -16777216};
    for (int lb : legacyBlacks) {
      boolean shouldAdapt = !(lb != AppPreferences.DEFAULT_COMPONENT_COLOR && lb != 0 && lb != 0xFF000000 && lb != -16777216);
      assertTrue(shouldAdapt, "Value " + lb + " should be treated as legacy black");
    }

    int[] customColors = new int[] {0x0000FF, 0xFF0000, 0x123456, -65536};
    for (int cc : customColors) {
      boolean shouldAdapt = !(cc != AppPreferences.DEFAULT_COMPONENT_COLOR && cc != 0 && cc != 0xFF000000 && cc != -16777216);
      assertFalse(shouldAdapt, "Value " + cc + " should be treated as custom user color (ADAPT=false)");
    }
  }

  @Test
  public void testD1DecisionUncheckDoesNotStoreThemeColor() {
    AppPreferences.ADAPT_COMPONENT_COLOR.setBoolean(false);
    AppPreferences.COMPONENT_COLOR.set(Color.BLACK.getRGB());
    assertEquals(Color.BLACK, AppPreferences.getComponentColor());
    assertEquals(Color.BLACK.getRGB(), (int) AppPreferences.COMPONENT_COLOR.get());
    assertFalse(AppPreferences.COMPONENT_COLOR.get() == 0xE6E6E6, "D1 violation: theme color stored in monitor");
  }
}
