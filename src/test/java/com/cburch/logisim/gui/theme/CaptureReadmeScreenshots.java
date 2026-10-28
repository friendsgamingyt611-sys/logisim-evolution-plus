package com.cburch.logisim.gui.theme;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.file.Loader;
import com.cburch.logisim.gui.main.Frame;
import com.cburch.logisim.gui.main.SelectionActions;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.proj.Project;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class CaptureReadmeScreenshots {

  private static final File CIRC_FILE = new File("docs/mhl_cpu_zero_09.circ");
  private static final File DOCS_IMG_DIR = new File("docs/img");

  public static void main(String[] args) {
    try {
      System.setProperty("java.awt.headless", "false");
      new CaptureReadmeScreenshots().runAll();
      System.out.println("ALL SCREENSHOTS CAPTURED SUCCESSFULLY!");
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }

  public void runAll() throws Exception {
    DOCS_IMG_DIR.mkdirs();

    // Start with Modern Light Laf
    SwingUtilities.invokeAndWait(() -> {
      try {
        var laf = new ModernLightLaf();
        UIManager.setLookAndFeel(laf);
        AppPreferences.LookAndFeel.set(laf.getClass().getName());
        AppPreferences.COLOR_SCHEME.set(ColorSchemeRegistry.SCHEME_MODERN);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    final Frame[] frameHolder = new Frame[1];
    final Project[] projHolder = new Project[1];

    SwingUtilities.invokeAndWait(() -> {
      try {
        Loader loader = new Loader(null);
        var logisimFile = loader.openLogisimFile(CIRC_FILE);
        Project proj = new Project(logisimFile);
        projHolder[0] = proj;
        Frame f = new Frame(proj);
        proj.setFrame(f);
        f.setSize(1400, 960);
        f.setVisible(true);
        f.toFront();
        frameHolder[0] = f;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    Thread.sleep(800);
    Frame frame = frameHolder[0];
    Project proj = projHolder[0];

    // 1. Screenshot 1: Modern Light Theme with hex_led_16bit
    captureView(frame, proj, false, "hex_led_16bit", null, new Rectangle(260, 150, 950, 750), "logisim-evolution-01.png", "logisim-evolution-01-small.png");

    // 2. Screenshot 2: Modern Light Theme with progam_counter and Pin "I" selected
    captureView(frame, proj, false, "progam_counter", "I", new Rectangle(260, 220, 950, 750), "logisim-evolution-02.png", "logisim-evolution-02-small.png");

    // 3. Screenshot 3: Modern Dark Theme with hex_display_8_bit
    captureView(frame, proj, true, "hex_display_8_bit", null, new Rectangle(320, 220, 950, 750), "logisim-evolution-03.png", "logisim-evolution-03-small.png");

    SwingUtilities.invokeAndWait(() -> {
      frame.setVisible(false);
      frame.dispose();
    });
  }

  private void captureView(Frame frame, Project proj, boolean dark, String circuitName, String selectCompLabel, Rectangle scrollRect, String filename, String thumbFilename) throws Exception {
    System.out.println("Capturing: " + filename + " (circuit: " + circuitName + ", dark: " + dark + ")");

    SwingUtilities.invokeAndWait(() -> {
      try {
        var laf = dark ? new ModernDarkLaf() : new ModernLightLaf();
        UIManager.setLookAndFeel(laf);
        AppPreferences.LookAndFeel.set(laf.getClass().getName());
        AppPreferences.COLOR_SCHEME.set(ColorSchemeRegistry.SCHEME_MODERN);
        com.formdev.flatlaf.FlatLaf.updateUI();
        com.cburch.logisim.gui.main.ComponentGlyphCache.clearCache();
        frame.updateThemeUI();

        var circ = proj.getLogisimFile().getCircuit(circuitName);
        if (circ != null) {
          proj.setCurrentCircuit(circ);
        }
        proj.getSimulator().step();

        if (frame.getCanvas() != null && frame.getCanvas().getSelection() != null) {
          SelectionActions.dropAll(frame.getCanvas().getSelection());
        }
        if (selectCompLabel != null && circ != null) {
          for (Component c : circ.getNonWires()) {
            if (c.getAttributeSet().containsAttribute(StdAttr.LABEL)
                && selectCompLabel.equals(c.getAttributeSet().getValue(StdAttr.LABEL))) {
              frame.getCanvas().getSelection().add(c);
              break;
            }
          }
        }

        frame.validate();
        frame.repaint();
        if (frame.getCanvas() != null && scrollRect != null) {
          frame.getCanvas().scrollRectToVisible(scrollRect);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    Thread.sleep(600);

    SwingUtilities.invokeAndWait(() -> {
      try {
        if (frame.getCanvas() != null && scrollRect != null) {
          frame.getCanvas().scrollRectToVisible(scrollRect);
        }
        frame.validate();
        frame.repaint();
      } catch (Exception ignored) {
      }
    });
    Thread.sleep(300);

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

        File outFile = new File(DOCS_IMG_DIR, filename);
        ImageIO.write(img, "png", outFile);
        System.out.println("Saved: " + outFile.getAbsolutePath() + " (" + w + "x" + h + ")");

        // Generate small thumbnail (200px wide, preserving aspect ratio)
        int thumbW = 200;
        int thumbH = (int) Math.round(((double) h / (double) w) * thumbW);
        BufferedImage thumb = new BufferedImage(thumbW, thumbH, BufferedImage.TYPE_INT_RGB);
        Graphics2D gt = thumb.createGraphics();
        gt.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        gt.drawImage(img.getScaledInstance(thumbW, thumbH, Image.SCALE_SMOOTH), 0, 0, null);
        gt.dispose();

        File thumbFile = new File(DOCS_IMG_DIR, thumbFilename);
        ImageIO.write(thumb, "png", thumbFile);
        System.out.println("Saved thumb: " + thumbFile.getAbsolutePath() + " (" + thumbW + "x" + thumbH + ")");
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    Thread.sleep(200);
  }
}
