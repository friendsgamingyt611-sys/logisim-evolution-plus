/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.circuit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.comp.Palette;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.prefs.AppPreferences;
import java.awt.Color;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Stage2AcceptanceTest {

  private int originalBusColor;

  @BeforeEach
  void setUp() {
    originalBusColor = AppPreferences.BUS_COLOR.get();
    AppPreferences.BUS_COLOR.set(0);
  }

  @AfterEach
  void tearDown() {
    AppPreferences.BUS_COLOR.set(originalBusColor);
  }

  @Test
  void testPaletteBusColorDefaults() {
    // Light palette bus is pure black by default
    assertEquals(Color.BLACK, Palette.LIGHT.bus());
    // Dark palette bus is #E6E6E6 by default
    assertEquals(new Color(0xE6E6E6), Palette.DARK.bus());

    // Accent blue must never be the default bus color
    assertNotEquals(new Color(0x3584e4), Palette.DARK.bus());
    assertNotEquals(Color.BLUE, Palette.DARK.bus());
  }

  @Test
  void testCustomBusColorRespected() {
    AppPreferences.BUS_COLOR.set(0x00FF00);
    assertEquals(new Color(0x00FF00), Palette.DARK.bus());
    assertEquals(new Color(0x00FF00), Palette.LIGHT.bus());
  }

  @Test
  void testForceLightScopeReturnsLightBusColor() {
    try (var scope = Palette.enterLightScope()) {
      assertEquals(Palette.LIGHT.bus(), Palette.current().bus());
      assertEquals(Color.BLACK, Palette.current().bus());
    }
  }

  @Test
  void testSplitterPaintingHeadless() {
    final var img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
    final var g = img.createGraphics();
    final var context = new ComponentDrawContext(null, null, null, null, g, false);

    final var attrs = new SplitterAttributes();
    attrs.setValue(SplitterAttributes.ATTR_WIDTH, BitWidth.create(4));
    attrs.setValue(SplitterAttributes.ATTR_FANOUT, 4);

    final var loc = Location.create(100, 100, true);

    // Standard appearance
    SplitterPainter.drawLines(context, attrs, loc);
    SplitterPainter.drawLabels(context, attrs, loc);

    // Legacy appearance
    SplitterPainter.drawLegacy(context, attrs, loc);

    g.dispose();
  }
}
