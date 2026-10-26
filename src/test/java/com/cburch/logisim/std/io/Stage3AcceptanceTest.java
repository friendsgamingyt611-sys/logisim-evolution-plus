/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.std.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cburch.logisim.comp.Palette;
import com.cburch.logisim.data.Value;
import java.awt.Color;
import org.junit.jupiter.api.Test;

class Stage3AcceptanceTest {

  @Test
  void testDarkPaletteTokens() {
    final var dark = Palette.DARK;
    assertTrue(dark.isDark());
    assertEquals(new Color(0x262626), dark.switchBody());
    assertEquals(new Color(0xE6E6E6), dark.switchOutline());
    assertEquals(new Color(0x141414), dark.switchChannel(true));
    assertEquals(new Color(0x141414), dark.switchChannel(false));

    // Actuator ON vs OFF brightness and contrast differentiation
    final var onColor = dark.switchActuator(true);
    final var offColor = dark.switchActuator(false);
    assertEquals(new Color(0xF0F0F0), onColor);
    assertEquals(new Color(0x9E9E9E), offColor);
    assertNotEquals(onColor, offColor);

    // Channel must be darker than the canvas background (#1F1F1F)
    final var channelVal = dark.switchChannel(false).getRed();
    final var bgVal = dark.background().getRed();
    assertTrue(channelVal < bgVal, "Channel (" + channelVal + ") must be darker than canvas (" + bgVal + ")");

    assertEquals(new Color(0xE6E6E6), dark.switchLabel());
  }

  @Test
  void testLightPaletteTokens() {
    final var light = Palette.LIGHT;
    assertFalse(light.isDark());
    assertEquals(Color.DARK_GRAY, light.switchBody());
    assertNull(light.switchOutline(), "Light mode must not draw a switch outline");
    assertEquals(Value.trueColor, light.switchChannel(true));
    assertEquals(Color.WHITE, light.switchChannel(false));
    assertEquals(Color.DARK_GRAY, light.switchActuator(true));
    assertEquals(Color.GRAY, light.switchActuator(false));
    assertEquals(Color.WHITE, light.switchLabel());
  }

  @Test
  void testForceLightScopeSymmetry() {
    try (var scope = Palette.enterLightScope()) {
      final var cur = Palette.current();
      assertFalse(cur.isDark());
      assertEquals(Color.DARK_GRAY, cur.switchBody());
      assertNull(cur.switchOutline());
      assertEquals(Value.trueColor, cur.switchChannel(true));
      assertEquals(Color.WHITE, cur.switchChannel(false));
      assertEquals(Color.DARK_GRAY, cur.switchActuator(true));
      assertEquals(Color.GRAY, cur.switchActuator(false));
      assertEquals(Color.WHITE, cur.switchLabel());
    }
  }
}
