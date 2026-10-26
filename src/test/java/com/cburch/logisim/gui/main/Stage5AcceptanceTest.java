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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitMutation;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.comp.Palette;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.file.Loader;
import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.std.wiring.Constant;
import com.cburch.logisim.std.wiring.Pin;
import com.cburch.logisim.std.wiring.Probe;
import com.cburch.logisim.std.wiring.WiringLibrary;
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.swing.UIManager;
import org.junit.jupiter.api.Test;

class Stage5AcceptanceTest {

  @Test
  void testStage5DarkPaletteTokens() {
    final var dark = Palette.DARK;
    assertTrue(dark.isDark());
    assertEquals(new Color(0x35, 0x84, 0xE4), dark.accent());
    assertEquals(new Color(0xC0, 0x61, 0xCB), dark.selectionHalo(), "Dark halo must be GNOME Orchid #C061CB");
    assertEquals(new Color(0x35, 0x84, 0xE4), dark.selectionMarqueeBorder());
    assertEquals(new Color(0x35, 0x84, 0xE4, 41), dark.selectionMarqueeFill());
    assertEquals(new Color(0x26, 0x26, 0x26), dark.probeFill());
    assertEquals(new Color(0x26, 0x26, 0x26), dark.raisedSurface());
    assertEquals(new Color(0x62, 0xA0, 0xEA), dark.blueIndex());
  }

  @Test
  void testStage5LightPaletteTokens() {
    final var light = Palette.LIGHT;
    assertFalse(light.isDark());
    assertEquals(new Color(0x35, 0x84, 0xE4), light.accent());
    assertEquals(Color.MAGENTA, light.selectionHalo(), "Light halo must preserve legacy Color.MAGENTA");
    assertEquals(new Color(0, 64, 128, 255), light.selectionMarqueeBorder());
    assertEquals(new Color(192, 192, 255, 192), light.selectionMarqueeFill());
    assertEquals(new Color(0xFF, 0xF0, 0x99), light.probeFill());
    assertEquals(new Color(230, 230, 230), light.raisedSurface());
    assertEquals(new Color(0x00, 0x66, 0xCC), light.blueIndex());
  }

  @Test
  void testCanvasHaloColorToken() {
    assertEquals(Color.MAGENTA, Canvas.HALO_COLOR, "Legacy static Canvas.HALO_COLOR must remain Color.MAGENTA for D4");
  }

  @Test
  void testContrastRatiosAgainstDarkCanvas() {
    final var canvasBg = new Color(0x1F, 0x1F, 0x1F);
    final var halo = Palette.DARK.selectionHalo();
    final var ghost = new Color(AppPreferences.COMPONENT_GHOST_COLOR.get(), false);

    double haloContrast = getContrastRatio(halo, canvasBg);
    double ghostContrast = getContrastRatio(ghost, canvasBg);

    assertTrue(haloContrast >= 4.5, "Halo contrast (" + haloContrast + ") must meet 4.5:1");
    assertTrue(ghostContrast >= 5.0, "Ghost contrast (" + ghostContrast + ") must meet 5.0:1");
  }

  private double getLuminance(Color c) {
    double r = c.getRed() / 255.0;
    double g = c.getGreen() / 255.0;
    double b = c.getBlue() / 255.0;
    r = (r <= 0.04045) ? r / 12.92 : Math.pow((r + 0.055) / 1.055, 2.4);
    g = (g <= 0.04045) ? g / 12.92 : Math.pow((g + 0.055) / 1.055, 2.4);
    b = (b <= 0.04045) ? b / 12.92 : Math.pow((b + 0.055) / 1.055, 2.4);
    return 0.2126 * r + 0.7152 * g + 0.0722 * b;
  }

  private double getContrastRatio(Color c1, Color c2) {
    double l1 = getLuminance(c1);
    double l2 = getLuminance(c2);
    double lighter = Math.max(l1, l2);
    double darker = Math.min(l1, l2);
    return (lighter + 0.05) / (darker + 0.05);
  }

  @Test
  void testHeadlessProbeAndConstantPainting() throws Exception {
    UIManager.setLookAndFeel(new com.cburch.logisim.gui.theme.ModernDarkLaf());

    final var probeFactory = new Probe();
    final var constFactory = new Constant();
    final var pinFactory = new Pin();

    final var loader = new Loader(null);
    final var logisimFile = LogisimFile.createNew(loader, null);
    final var circuit = logisimFile.getMainCircuit();
    final var project = new Project(logisimFile);
    circuit.setProject(project);
    project.setCurrentCircuit(circuit);
    final var state = project.getCircuitState();

    final var mutation = new CircuitMutation(circuit);

    // 1. Probe
    final var probeAttrs = probeFactory.createAttributeSet();
    final var probeComp = probeFactory.createComponent(Location.create(100, 100, true), probeAttrs);
    mutation.add(probeComp);

    // 2. Multi-bit Constant
    final var constAttrs = constFactory.createAttributeSet();
    constAttrs.setValue(StdAttr.WIDTH, BitWidth.create(8));
    constAttrs.setValue(Constant.ATTR_VALUE, (long) 0xAB);
    final var constComp = constFactory.createComponent(Location.create(200, 100, true), constAttrs);
    mutation.add(constComp);

    // 3. Multi-bit Pin
    final var pinAttrs = pinFactory.createAttributeSet();
    pinAttrs.setValue(StdAttr.WIDTH, BitWidth.create(4));
    final var pinComp = pinFactory.createComponent(Location.create(300, 100, true), pinAttrs);
    mutation.add(pinComp);

    mutation.execute();

    final var img = new BufferedImage(500, 300, BufferedImage.TYPE_INT_ARGB);
    final var g = img.createGraphics();
    final var context = new ComponentDrawContext(null, circuit, state, g, g, false);

    // Verify paint executes cleanly without exceptions
    circuit.draw(context, null);
    g.dispose();

    // Verify enterLightScope() restores light tokens
    try (var scope = Palette.enterLightScope()) {
      assertEquals(new Color(0xFF, 0xF0, 0x99), Palette.current().probeFill());
      assertEquals(new Color(230, 230, 230), Palette.current().raisedSurface());
      assertEquals(new Color(0x00, 0x66, 0xCC), Palette.current().blueIndex());
    }
  }
}
