/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.circuit.appear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cburch.draw.shapes.DrawAttr;
import com.cburch.draw.shapes.Rectangle;
import com.cburch.draw.shapes.Text;
import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitMutation;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.comp.Palette;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.file.Loader;
import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.std.wiring.Pin;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class Stage4AcceptanceTest {

  @Test
  void testPaletteHeaderTokens() {
    // Dark palette
    assertEquals(new Color(0x383838), Palette.DARK.headerFill());
    assertEquals(new Color(0xE6E6E6), Palette.DARK.headerText());

    // Light palette
    assertEquals(Color.BLACK, Palette.LIGHT.headerFill());
    assertEquals(Color.WHITE, Palette.LIGHT.headerText());

    // Export/Light scope symmetry
    try (var scope = Palette.enterLightScope()) {
      assertEquals(Color.BLACK, Palette.current().headerFill());
      assertEquals(Color.WHITE, Palette.current().headerText());
    }
  }

  @Test
  void testDefaultEvolutionAppearanceShapesNeverStoreThemeColors() {
    final var pins = new ArrayList<Instance>();
    final var shapes = DefaultEvolutionAppearance.build(pins, "MySubcircuit", true);

    var foundHeaderBand = false;
    var foundHeaderLabel = false;

    for (final var shape : shapes) {
      if (shape instanceof Rectangle rect) {
        final var fill = rect.getValue(DrawAttr.FILL_COLOR);
        if (fill != null) {
          assertNotEquals(new Color(0x383838), fill, "Stored shape must never contain theme color #383838");
          assertNotEquals(new Color(0xE6E6E6), fill, "Stored shape must never contain theme color #E6E6E6");
          if (rect.getValue(DrawAttr.PAINT_TYPE) == DrawAttr.PAINT_FILL && rect.getWidth() > 10) {
            assertEquals(Color.BLACK, fill, "Header band must be legacy Color.BLACK in stored shapes");
            foundHeaderBand = true;
          }
        }
      } else if (shape instanceof Text text) {
        final var col = text.getLabel().getColor();
        assertNotEquals(new Color(0x383838), col);
        assertNotEquals(new Color(0xE6E6E6), col);
        if ("MySubcircuit".equals(text.getText())) {
          assertEquals(Color.WHITE, col, "Header title label must be legacy Color.WHITE in stored shapes");
          foundHeaderLabel = true;
        }
      }
    }

    assertTrue(foundHeaderBand, "Must have built a header band rectangle");
    assertTrue(foundHeaderLabel, "Must have built a header text label");
  }

  @Test
  void testCircXmlContainsNoThemeColors() throws Exception {
    final var loader = new Loader(null);
    final var file = LogisimFile.createNew(loader, null);
    final var circuit = new Circuit("SubcircuitTest", file, null);
    file.addCircuit(circuit);

    // Save circuit to XML
    final var baos = new ByteArrayOutputStream();
    file.write(baos, loader);
    final var xml = baos.toString();

    // Verify no dark theme colors leaked into XML
    assertFalse(xml.contains("383838"), ".circ XML must not contain #383838");
    assertFalse(xml.contains("e6e6e6"), ".circ XML must not contain #E6E6E6");
    assertFalse(xml.contains("E6E6E6"), ".circ XML must not contain #E6E6E6");
  }

  @Test
  void testSubcircuitHeadlessPainting() {
    final var circuit = new Circuit("SubTest", null, null);
    final var appearance = circuit.getAppearance();
    assertTrue(appearance.isDefaultAppearance());

    final var img = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
    final var g = img.createGraphics();
    final var context = new ComponentDrawContext(null, circuit, null, null, g, false);
    final var painter = new InstancePainter(context, null);

    // Paint in Dark Mode
    appearance.paintSubcircuit(painter, g, Direction.NORTH);

    // Paint under Force Light Scope
    try (var scope = Palette.enterLightScope()) {
      appearance.paintSubcircuit(painter, g, Direction.NORTH);
    }

    g.dispose();
  }
}
