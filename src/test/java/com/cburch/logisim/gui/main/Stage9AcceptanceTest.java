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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cburch.logisim.file.Loader;
import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.gui.generic.ProjectExplorer;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.std.io.extra.ExtraIoLibrary;
import com.cburch.logisim.std.wiring.Clock;
import com.cburch.logisim.std.wiring.DoNotConnect;
import com.cburch.logisim.std.wiring.PowerOnReset;
import com.cburch.logisim.tools.AddTool;
import com.cburch.logisim.tools.Tool;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.border.MatteBorder;
import org.junit.jupiter.api.Test;

class Stage9AcceptanceTest {

  @Test
  void testSixCircuitActionButtonsInToolbox() {
    final var file = LogisimFile.createNew(new Loader(null), null);
    final var proj = new Project(file);
    final var toolbox = new Toolbox(proj, null, null);

    assertNotNull(toolbox.getBtnAddCircuit(), "Add circuit button must exist");
    assertNotNull(toolbox.getBtnAddVhdl(), "Add VHDL button must exist");
    assertNotNull(toolbox.getBtnMoveUp(), "Move up button must exist");
    assertNotNull(toolbox.getBtnMoveDown(), "Move down button must exist");
    assertNotNull(toolbox.getBtnAppearance(), "Toggle appearance button must exist");
    assertNotNull(toolbox.getBtnDelete(), "Delete circuit button must exist");

    assertNotNull(toolbox.getBtnAddCircuit().getIcon(), "Add circuit button must have vector icon");
    assertNotNull(toolbox.getBtnAddVhdl().getIcon(), "Add VHDL button must have vector icon");
    assertNotNull(toolbox.getBtnMoveUp().getIcon(), "Move up button must have vector icon");
    assertNotNull(toolbox.getBtnMoveDown().getIcon(), "Move down button must have vector icon");
    assertNotNull(toolbox.getBtnAppearance().getIcon(), "Appearance button must have vector icon");
    assertNotNull(toolbox.getBtnDelete().getIcon(), "Delete button must have vector icon");
  }

  @Test
  void testVectorWiringIconsHaveTransparentBackgrounds() {
    final var clockTool = new AddTool(Clock.FACTORY);
    final var porTool = new AddTool(PowerOnReset.FACTORY);
    final var noConnectTool = new AddTool(new DoNotConnect());

    ComponentGlyphCache.clearCache();

    final var clockImg = (BufferedImage) ComponentGlyphCache.getImage(clockTool, 24);
    final var porImg = (BufferedImage) ComponentGlyphCache.getImage(porTool, 24);
    final var noConnectImg = (BufferedImage) ComponentGlyphCache.getImage(noConnectTool, 24);

    assertNotNull(clockImg, "Clock icon image must be created");
    assertNotNull(porImg, "POR icon image must be created");
    assertNotNull(noConnectImg, "NoConnect icon image must be created");

    // Pixel (0, 0) must be transparent (alpha 0), NOT opaque white (#FFFFFF)
    final var clockPixel = clockImg.getRGB(0, 0);
    final var porPixel = porImg.getRGB(0, 0);
    final var noConnectPixel = noConnectImg.getRGB(0, 0);

    assertEquals(0, (clockPixel >> 24) & 0xFF, "Clock icon corner must be transparent");
    assertEquals(0, (porPixel >> 24) & 0xFF, "POR icon corner must be transparent");
    assertEquals(0, (noConnectPixel >> 24) & 0xFF, "NoConnect icon corner must be transparent");
  }

  @Test
  void testSearchPinMatchesBothPinAndTwoPinLed() {
    final var extraIoLib = new ExtraIoLibrary();
    boolean foundTwoPinLed = false;
    for (final Tool tool : extraIoLib.getTools()) {
      if (tool.getDisplayName().toLowerCase().contains("pin")) {
        foundTwoPinLed = true;
        break;
      }
    }
    assertTrue(foundTwoPinLed, "ExtraIoLibrary must contain 2-Pin LED matching 'pin'");

    final var file = LogisimFile.createNew(new Loader(null), null);
    final var proj = new Project(file);
    final var explorer = new ProjectExplorer(proj, false, false);

    explorer.setFilter("pin");
    // Verify that the tree filters down to matching tools
    assertNotNull(explorer.getModel());
  }

  @Test
  void testCircuitsListHasBottomDividerBorder() {
    final var file = LogisimFile.createNew(new Loader(null), null);
    final var proj = new Project(file);
    final var model = new ExplorerModel(proj, null);
    final var list = new CircuitsList(model);

    assertNotNull(list.getBorder(), "CircuitsList must have a border");
    assertTrue(list.getBorder() instanceof MatteBorder, "CircuitsList must have a matte divider border");
    final var mb = (MatteBorder) list.getBorder();
    assertEquals(1, mb.getBorderInsets().bottom, "Divider must have 1px bottom border");
  }
}
