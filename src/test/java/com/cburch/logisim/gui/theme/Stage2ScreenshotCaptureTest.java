/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.gui.theme;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.file.Loader;
import com.cburch.logisim.gui.main.Frame;
import com.cburch.logisim.gui.prefs.PreferencesFrame;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.proj.Project;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

public class Stage2ScreenshotCaptureTest {

  private static final String ARTIFACT_DIR =
      "/home/devansh/.gemini/antigravity-ide/brain/904c724c-98b1-4fc8-adc9-6305dcb0eb9c/";
  private static final File CIRC_FILE = new File("docs/mhl_cpu_zero_09.circ");

  public static void main(String[] args) {
    try {
      new Stage2ScreenshotCaptureTest().captureAllFourScreenshots();
      System.out.println("All 4 Stage 2 screenshots captured successfully.");
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }

  @Test
  void captureAllFourScreenshots() throws Exception {
    System.setProperty("java.awt.headless", "false");
    new File(ARTIFACT_DIR).mkdirs();
    ThemeSyncListener.install();
    PreferencesFrame.initializeManager();

    assertTrue(CIRC_FILE.exists(), "mhl_cpu_zero_09.circ must exist");
    Loader loader = new Loader(null);
    var logisimFile = loader.openLogisimFile(CIRC_FILE);
    Project proj = new Project(logisimFile);

    final Frame[] frameHolder = new Frame[1];
    SwingUtilities.invokeAndWait(() -> {
      try {
        Circuit targetCirc = logisimFile.getCircuit("hex_display_8_bit");
        if (targetCirc == null) {
          targetCirc = logisimFile.getCircuit("main");
        }
        if (targetCirc != null) {
          proj.setCurrentCircuit(targetCirc);
        }
        proj.getSimulator().setAutoTicking(true);
        proj.getSimulator().step();

        Frame frame = new Frame(proj);
        proj.setFrame(frame);
        frame.setSize(1280, 820);
        frame.setVisible(true);
        frame.toFront();
        frame.validate();
        frame.repaint();
        frameHolder[0] = frame;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    Thread.sleep(800);
    Frame frame = frameHolder[0];
    assertNotNull(frame, "Frame must be instantiated");

    // 1. Current Dark Mode
    System.out.println("1/4 Capturing Current Dark Mode...");
    setThemeAndWait(ModernDarkLaf.class.getName(), frame);
    saveFrameScreenshot(frame, "stage2_1_current_dark_mode.png");

    // 2. Current Light Mode
    System.out.println("2/4 Capturing Current Light Mode...");
    setThemeAndWait(ModernLightLaf.class.getName(), frame);
    saveFrameScreenshot(frame, "stage2_2_current_light_mode.png");

    // 3. Post Dark Mode to Light Mode (Start Dark -> Transition to Light)
    System.out.println("3/4 Capturing Post Dark Mode to Light Mode transition...");
    setThemeAndWait(ModernDarkLaf.class.getName(), frame);
    Thread.sleep(400);
    setThemeAndWait(ModernLightLaf.class.getName(), frame);
    saveFrameScreenshot(frame, "stage2_3_post_dark_to_light.png");

    // 4. Post Light Mode to Dark Mode (Start Light -> Transition to Dark)
    System.out.println("4/4 Capturing Post Light Mode to Dark Mode transition...");
    setThemeAndWait(ModernLightLaf.class.getName(), frame);
    Thread.sleep(400);
    setThemeAndWait(ModernDarkLaf.class.getName(), frame);
    saveFrameScreenshot(frame, "stage2_4_post_light_to_dark.png");

    SwingUtilities.invokeAndWait(() -> {
      frame.setVisible(false);
      frame.dispose();
    });
  }

  private void setThemeAndWait(String lafClassName, Frame frame) throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      try {
        ThemeManager.applyTheme(lafClassName);
        if (frame != null) {
          frame.updateThemeUI();
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
    Thread.sleep(600);
    SwingUtilities.invokeAndWait(() -> {
      if (frame != null) {
        frame.validate();
        frame.repaint();
      }
    });
    Thread.sleep(300);
  }

  private void saveFrameScreenshot(Frame frame, String filename) throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      try {
        int w = frame.getWidth();
        int h = frame.getHeight();
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        frame.paintAll(g2);
        g2.dispose();

        File outFile = new File(ARTIFACT_DIR, filename);
        ImageIO.write(img, "png", outFile);
        System.out.println("Saved screenshot: " + outFile.getAbsolutePath() + " (" + w + "x" + h + ")");
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
    Thread.sleep(200);
  }
}
