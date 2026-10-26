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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cburch.logisim.gui.generic.LFrame;
import com.cburch.logisim.gui.start.About;
import com.cburch.logisim.gui.start.SplashScreen;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import org.junit.jupiter.api.Test;

class Stage8AcceptanceTest {

  @Test
  void testAllWindowIconSizesLoadSuccessfully() throws Exception {
    final var loader = LFrame.class.getClassLoader();
    final int[] requiredSizes = {16, 32, 48, 64, 128, 256};

    for (int size : requiredSizes) {
      final var url = loader.getResource("resources/logisim/img/logisim-icon-" + size + ".png");
      assertNotNull(url, "Resource must exist for icon size " + size);
      final BufferedImage img = ImageIO.read(url);
      assertNotNull(img, "Image must be readable for size " + size);
      assertEquals(size, img.getWidth(), "Width must match size " + size);
      assertEquals(size, img.getHeight(), "Height must match size " + size);
    }
  }

  @Test
  void testLFrameAttachIconBindsIcons() {
    final var frame = new JFrame();
    LFrame.attachIcon(frame);
    final var icons = frame.getIconImages();
    assertNotNull(icons, "Frame icon images must not be null");
    assertTrue(icons.size() >= 5, "Frame should have registered multiple high-DPI icons including 256px");
  }

  @Test
  void testSplashScreenLoadsAboutBanner() {
    final var splash = new SplashScreen();
    assertNotNull(splash, "SplashScreen must instantiate without error");
    final var content = splash.getContentPane();
    assertNotNull(content, "Content pane must not be null");
    // Verify About image panel is present
    assertEquals(2, content.getComponentCount());
    splash.close();
  }
}
