package com.cburch.logisim.gui.theme;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import javax.swing.UIManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class Stage26SurfaceLayeringTest {

  private static final String ARTIFACT_DIR =
      "/home/devansh/.gemini/antigravity-ide/brain/904c724c-98b1-4fc8-adc9-6305dcb0eb9c/";

  @BeforeAll
  static void setupDir() {
    new File(ARTIFACT_DIR).mkdirs();
    PreferencesFrame.initializeManager();
  }

  private void captureWindow(java.awt.Window win, String filename) throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      try {
        int w = Math.max(100, win.getWidth());
        int h = Math.max(100, win.getHeight());
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        win.paintAll(g2);
        g2.dispose();
        File out = new File(ARTIFACT_DIR, filename);
        ImageIO.write(img, "PNG", out);
        System.out.println("Saved capture: " + out.getAbsolutePath() + " (" + w + "x" + h + ")");
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  private void runCaptureForTheme(boolean dark) throws Exception {
    final String themeStr = dark ? "dark" : "light";
    final var laf = dark ? new ModernDarkLaf() : new ModernLightLaf();

    SwingUtilities.invokeAndWait(() -> {
      try {
        UIManager.setLookAndFeel(laf);
        AppPreferences.LookAndFeel.set(laf.getClass().getName());
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    File circFile = new File("/home/devansh/Downloads/RAMexp.circ");
    assertTrue(circFile.exists(), "RAMexp.circ must exist");

    final Frame[] holder = new Frame[1];
    SwingUtilities.invokeAndWait(() -> {
      try {
        Loader loader = new Loader(null);
        var logisimFile = loader.openLogisimFile(circFile);
        Project proj = new Project(logisimFile);
        Frame f = new Frame(proj);
        proj.setFrame(f);
        f.setSize(1240, 820);
        f.setVisible(true);
        f.toFront();
        f.validate();
        f.repaint();
        holder[0] = f;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    Thread.sleep(500);
    Frame frame = holder[0];
    assertNotNull(frame);

    // --- Screenshot 1: Main Window (Design Tab) ---
    SwingUtilities.invokeAndWait(() -> {
      frame.setEditorView(Frame.EDIT_LAYOUT);
      frame.getTopTab().setSelectedIndex(0);
      frame.validate();
      frame.repaint();
    });
    Thread.sleep(300);
    captureWindow(frame, "stage26_" + themeStr + "_main_design.png");

    // --- Screenshot 2: The Simulate Tab ---
    SwingUtilities.invokeAndWait(() -> {
      frame.getTopTab().setSelectedIndex(1);
      frame.validate();
      frame.repaint();
    });
    Thread.sleep(300);
    captureWindow(frame, "stage26_" + themeStr + "_simulate_tab.png");

    // --- Screenshot 3: Appearance Editor ---
    SwingUtilities.invokeAndWait(() -> {
      frame.getTopTab().setSelectedIndex(0);
      frame.setEditorView(Frame.EDIT_APPEARANCE);
      frame.validate();
      frame.repaint();
    });
    Thread.sleep(300);
    captureWindow(frame, "stage26_" + themeStr + "_appearance_editor.png");

    // --- Screenshot 4: Preferences Window ---
    final PreferencesFrame[] prefHolder = new PreferencesFrame[1];
    SwingUtilities.invokeAndWait(() -> {
      try {
        PreferencesFrame.showPreferences();
        for (java.awt.Window w : java.awt.Window.getWindows()) {
          if (w instanceof PreferencesFrame pf) {
            prefHolder[0] = pf;
            pf.setSize(750, 520);
            pf.validate();
            pf.repaint();
            break;
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    Thread.sleep(300);
    if (prefHolder[0] != null) {
      captureWindow(prefHolder[0], "stage26_" + themeStr + "_preferences.png");
      SwingUtilities.invokeAndWait(() -> prefHolder[0].setVisible(false));
    }

    // Cleanup frame
    SwingUtilities.invokeAndWait(() -> {
      frame.setVisible(false);
    });
    Thread.sleep(200);
  }

  @Test
  void testCaptureLight() throws Exception {
    runCaptureForTheme(false);
  }

  @Test
  void testCaptureDark() throws Exception {
    runCaptureForTheme(true);
  }
}
