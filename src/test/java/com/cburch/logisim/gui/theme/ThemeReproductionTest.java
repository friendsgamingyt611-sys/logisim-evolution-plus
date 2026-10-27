package com.cburch.logisim.gui.theme;

import com.cburch.logisim.gui.main.Frame;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.ProjectActions;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.junit.jupiter.api.Test;

public class ThemeReproductionTest {

  private static final String ARTIFACT_DIR =
      "/home/devansh/.gemini/antigravity-ide/brain/904c724c-98b1-4fc8-adc9-6305dcb0eb9c/";

  private Frame setupFrameWithLaf(String lafClass) throws Exception {
    UIManager.setLookAndFeel(lafClass);
    AppPreferences.LookAndFeel.set(lafClass);
    final Frame[] frameHolder = new Frame[1];
    SwingUtilities.invokeAndWait(() -> {
      Project proj = ProjectActions.doNew((Project) null);
      Frame f = proj.getFrame();
      f.setSize(new Dimension(1024, 768));
      f.validate();
      f.repaint();
      frameHolder[0] = f;
    });
    Thread.sleep(400);
    return frameHolder[0];
  }

  private void captureWindow(Frame frame, String filename) throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      try {
        int w = Math.max(100, frame.getWidth());
        int h = Math.max(100, frame.getHeight());
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        frame.paintAll(g2);
        g2.dispose();
        File out = new File(ARTIFACT_DIR + filename);
        ImageIO.write(img, "PNG", out);
        System.out.println("Saved capture: " + out.getAbsolutePath() + " (" + w + "x" + h + ")");
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  private void hideFrame(Frame frame) throws Exception {
    if (frame != null) {
      SwingUtilities.invokeAndWait(() -> {
        frame.setVisible(false);
      });
      Thread.sleep(100);
    }
  }

  @Test
  void testFourSituations() throws Exception {
    File dir = new File(ARTIFACT_DIR);
    if (!dir.exists()) dir.mkdirs();

    System.out.println("=== 1. Cold start in Light ===");
    Frame fLightCold = setupFrameWithLaf(FlatLightLaf.class.getName());
    captureWindow(fLightCold, "cold_start_light.png");
    hideFrame(fLightCold);

    System.out.println("=== 2. Cold start in Dark ===");
    Frame fDarkCold = setupFrameWithLaf(FlatDarkLaf.class.getName());
    captureWindow(fDarkCold, "cold_start_dark.png");
    hideFrame(fDarkCold);

    System.out.println("=== 3. Start in Dark, live switch to Light ===");
    Frame fDarkToLight = setupFrameWithLaf(FlatDarkLaf.class.getName());
    SwingUtilities.invokeAndWait(() -> {
      ThemeManager.applyTheme(FlatLightLaf.class.getName());
    });
    Thread.sleep(800);
    captureWindow(fDarkToLight, "dark_live_switch_to_light.png");
    hideFrame(fDarkToLight);

    System.out.println("=== 4. Start in Light, live switch to Dark ===");
    Frame fLightToDark = setupFrameWithLaf(FlatLightLaf.class.getName());
    SwingUtilities.invokeAndWait(() -> {
      ThemeManager.applyTheme(FlatDarkLaf.class.getName());
    });
    Thread.sleep(800);
    captureWindow(fLightToDark, "light_live_switch_to_dark.png");
    hideFrame(fLightToDark);

    System.out.println("All 4 captures completed.");
  }
}
