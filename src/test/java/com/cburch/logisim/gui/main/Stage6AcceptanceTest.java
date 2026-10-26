/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.gui.main;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitMutation;
import com.cburch.logisim.circuit.SplitterAttributes;
import com.cburch.logisim.circuit.SplitterFactory;
import com.cburch.logisim.circuit.Wire;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.comp.ComponentFactory;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.file.Loader;
import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.std.gates.GatesLibrary;
import com.cburch.logisim.std.wiring.Pin;
import com.cburch.logisim.tools.AddTool;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.UIManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class Stage6AcceptanceTest {

  @AfterEach
  void tearDown() {
    AppPreferences.EXPERIMENTAL_ROUND_STROKES.set(false);
  }

  @Test
  void testExperimentalFlagDefaultsToFalse() {
    assertFalse(
        AppPreferences.EXPERIMENTAL_ROUND_STROKES.getBoolean(),
        "EXPERIMENTAL_ROUND_STROKES must default to false");
  }

  @Test
  void testZoomAndOvershootEvaluation() throws Exception {
    UIManager.setLookAndFeel(new com.cburch.logisim.gui.theme.ModernDarkLaf());

    final var loader = new Loader(null);
    final var logisimFile = LogisimFile.createNew(loader, null);
    final var circuit = logisimFile.getMainCircuit();
    final var project = new Project(logisimFile);
    circuit.setProject(project);
    project.setCurrentCircuit(circuit);
    final var state = project.getCircuitState();

    final var mutation = new CircuitMutation(circuit);

    // Gates
    final var gatesLib = new GatesLibrary();
    ComponentFactory andFactory = null;
    ComponentFactory orFactory = null;
    ComponentFactory notFactory = null;
    for (final var tool : gatesLib.getTools()) {
      if (tool instanceof AddTool addTool) {
        final var name = addTool.getFactory().getName();
        if ("AND Gate".equals(name)) {
          andFactory = addTool.getFactory();
        } else if ("OR Gate".equals(name)) {
          orFactory = addTool.getFactory();
        } else if ("NOT Gate".equals(name)) {
          notFactory = addTool.getFactory();
        }
      }
    }
    final var pinFactory = new Pin();

    mutation.add(andFactory.createComponent(Location.create(150, 100, true), andFactory.createAttributeSet()));
    mutation.add(orFactory.createComponent(Location.create(150, 200, true), orFactory.createAttributeSet()));
    mutation.add(notFactory.createComponent(Location.create(150, 300, true), notFactory.createAttributeSet()));

    // Pins & Bus
    final var pin1Attrs = pinFactory.createAttributeSet();
    pin1Attrs.setValue(StdAttr.WIDTH, BitWidth.create(1));
    mutation.add(pinFactory.createComponent(Location.create(50, 100, true), pin1Attrs));

    final var pin4Attrs = pinFactory.createAttributeSet();
    pin4Attrs.setValue(StdAttr.WIDTH, BitWidth.create(4));
    mutation.add(pinFactory.createComponent(Location.create(50, 200, true), pin4Attrs));

    final var splitterAttrs = SplitterFactory.instance.createAttributeSet();
    splitterAttrs.setValue(SplitterAttributes.ATTR_WIDTH, BitWidth.create(4));
    splitterAttrs.setValue(SplitterAttributes.ATTR_FANOUT, 4);
    mutation.add(SplitterFactory.instance.createComponent(Location.create(100, 200, true), splitterAttrs));

    // Multi-bit wire
    mutation.add(Wire.create(Location.create(50, 200, true), Location.create(100, 200, true)));
    // Single-bit wire to gate
    mutation.add(Wire.create(Location.create(50, 100, true), Location.create(110, 100, true)));

    mutation.execute();

    final var outDir = new File("build/stage6-eval");
    outDir.mkdirs();

    final var zooms = new double[] {1.0, 2.0, 4.32};
    final var zoomNames = new String[] {"100", "200", "432"};

    for (int i = 0; i < zooms.length; i++) {
      final var zoom = zooms[i];
      final var name = zoomNames[i];

      // 1. Render with Flag OFF (Default)
      AppPreferences.EXPERIMENTAL_ROUND_STROKES.set(false);
      renderAtZoom(circuit, state, zoom, new File(outDir, "eval_zoom_" + name + "_default.png"));

      // 2. Render with Flag ON (Round strokes & pure hints)
      AppPreferences.EXPERIMENTAL_ROUND_STROKES.set(true);
      renderAtZoom(circuit, state, zoom, new File(outDir, "eval_zoom_" + name + "_round.png"));
    }

    assertTrue(new File(outDir, "eval_zoom_100_round.png").exists());
    assertTrue(new File(outDir, "eval_zoom_200_round.png").exists());
    assertTrue(new File(outDir, "eval_zoom_432_round.png").exists());
  }

  private void renderAtZoom(Circuit circuit, com.cburch.logisim.circuit.CircuitState state, double zoom, File dest)
      throws Exception {
    int w = (int) Math.ceil(350 * zoom);
    int h = (int) Math.ceil(400 * zoom);

    final var img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    final var g = img.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setColor(new Color(0x1F1F1F));
    g.fillRect(0, 0, w, h);

    g.scale(zoom, zoom);

    final var context = new ComponentDrawContext(null, circuit, state, g, g, false);
    circuit.draw(context, null);

    g.dispose();
    ImageIO.write(img, "PNG", dest);
  }
}
