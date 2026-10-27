package com.cburch.logisim.gui.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.comp.Palette;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.file.Loader;
import com.cburch.logisim.gui.main.Frame;
import com.cburch.logisim.gui.prefs.PreferencesFrame;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.proj.Project;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Stage28VariationBTest {

  private static final String ARTIFACT_DIR =
      "/home/devansh/.gemini/antigravity-ide/brain/904c724c-98b1-4fc8-adc9-6305dcb0eb9c/";
  private static final File RAMEXP_FILE = new File("/home/devansh/Downloads/RAMexp.circ");

  @BeforeAll
  static void setup() {
    System.setProperty("java.awt.headless", "false");
    new File(ARTIFACT_DIR).mkdirs();
    PreferencesFrame.initializeManager();
  }

  @Test
  @Order(1)
  void testVariationBLightTokens() {
    var laf = new ModernLightLaf();
    var defaults = laf.getDefaults();

    // Surface tokens
    assertEquals(new Color(0xFF, 0xFF, 0xFF), defaults.get("Surface.canvas"), "Canvas must be #ffffff");
    assertEquals(new Color(0xFF, 0xFF, 0xFF), defaults.get("Surface.well"), "Well must be #ffffff");
    assertEquals(new Color(0xF2, 0xF3, 0xF5), defaults.get("Surface.panel"), "Panel must be #f2f3f5");
    assertEquals(new Color(0xE4, 0xE6, 0xEA), defaults.get("Surface.chrome"), "Chrome must be #e4e6ea");
    assertEquals(new Color(0xFF, 0xFF, 0xFF), defaults.get("Surface.group"), "Group must be #ffffff");
    assertEquals(new Color(0xC0, 0xC5, 0xCE), defaults.get("Surface.groupBorder"), "Group border must be #c0c5ce");
    assertEquals(new Color(0xC4, 0xC8, 0xD0), defaults.get("Surface.borderStrong"), "Border strong must be #c4c8d0");
    assertEquals(new Color(0xDA, 0xDD, 0xE3), defaults.get("Surface.borderSubtle"), "Border subtle must be #dadde3");
    assertEquals(new Color(0x54, 0x5A, 0x64), defaults.get("Surface.mutedText"), "Muted text must be #545a64");

    // Semantic action icons
    assertEquals(new Color(0x1E, 0x8E, 0x4E), defaults.get("Action.create"), "Create must be #1e8e4e");
    assertEquals(new Color(0x1C, 0x71, 0xD8), defaults.get("Action.move"), "Move must be #1c71d8");
    assertEquals(new Color(0xC2, 0x5A, 0x00), defaults.get("Action.edit"), "Edit must be #c25a00");
    assertEquals(new Color(0xC0, 0x1C, 0x28), defaults.get("Action.destroy"), "Destroy must be #c01c28");

    // Simulation wire colors
    ColorScheme light = ColorSchemeRegistry.MODERN_LIGHT;
    assertEquals(new Color(0x16, 0xA0, 0x5A), light.trueColor(), "True must be #16a05a");
    assertEquals(new Color(0x0B, 0x4A, 0x2C), light.falseColor(), "False must be #0b4a2c");
    assertEquals(new Color(0x1C, 0x71, 0xD8), light.unknownColor(), "Unknown must be #1c71d8");
    assertEquals(new Color(0xC0, 0x1C, 0x28), light.errorColor(), "Error must be #c01c28");
    assertEquals(new Color(0xB3, 0x5C, 0x00), light.mismatchColor(), "Mismatch must be #b35c00");
    assertEquals(new Color(0x4A, 0x53, 0x61), light.busColor(), "Bus must be #4a5361");
    assertEquals(new Color(0x8A, 0x90, 0x99), light.unconnectedColor(), "Unconnected must be #8a9099");
    assertEquals(new Color(0x26, 0x28, 0x2C), light.strokeColor(), "Stroke must be #26282c");
  }

  @Test
  @Order(2)
  void testVariationBDarkTokens() {
    var laf = new ModernDarkLaf();
    var defaults = laf.getDefaults();

    // Surface tokens
    assertEquals(new Color(0x1F, 0x1F, 0x1F), defaults.get("Surface.canvas"), "Canvas must be #1f1f1f");
    assertEquals(new Color(0x25, 0x28, 0x2C), defaults.get("Surface.well"), "Well must be #25282c");
    assertEquals(new Color(0x2A, 0x2D, 0x32), defaults.get("Surface.panel"), "Panel must be #2a2d32");
    assertEquals(new Color(0x32, 0x35, 0x3B), defaults.get("Surface.chrome"), "Chrome must be #32353b");
    assertEquals(new Color(0x3B, 0x3E, 0x45), defaults.get("Surface.group"), "Group must be #3b3e45");
    assertEquals(new Color(0x4D, 0x51, 0x5A), defaults.get("Surface.groupBorder"), "Group border must be #4d515a");
    assertEquals(new Color(0x4D, 0x51, 0x5A), defaults.get("Surface.borderStrong"), "Border strong must be #4d515a");
    assertEquals(new Color(0x38, 0x3B, 0x41), defaults.get("Surface.borderSubtle"), "Border subtle must be #383b41");
    assertEquals(new Color(0xAC, 0xAC, 0xAC), defaults.get("Surface.mutedText"), "Muted text must be #acacac");

    // Semantic action icons
    assertEquals(new Color(0x4C, 0xD0, 0x80), defaults.get("Action.create"), "Create must be #4cd080");
    assertEquals(new Color(0x62, 0xA0, 0xEA), defaults.get("Action.move"), "Move must be #62a0ea");
    assertEquals(new Color(0xF0, 0xA0, 0x30), defaults.get("Action.edit"), "Edit must be #f0a030");
    assertEquals(new Color(0xF6, 0x61, 0x51), defaults.get("Action.destroy"), "Destroy must be #f66151");

    // Simulation wire colors
    ColorScheme dark = ColorSchemeRegistry.MODERN_DARK;
    assertEquals(new Color(0x55, 0xF0, 0x8A), dark.trueColor(), "True must be #55f08a");
    assertEquals(new Color(0x1B, 0x84, 0x58), dark.falseColor(), "False must be #1b8458");
    assertEquals(new Color(0x5B, 0x9B, 0xFF), dark.unknownColor(), "Unknown must be #5b9bff");
    assertEquals(new Color(0xFF, 0x6B, 0x5E), dark.errorColor(), "Error must be #ff6b5e");
    assertEquals(new Color(0xF0, 0xA0, 0x30), dark.mismatchColor(), "Mismatch must be #f0a030");
    assertEquals(new Color(0x9F, 0xA9, 0xB8), dark.busColor(), "Bus must be #9fa9b8");
    assertEquals(new Color(0x7C, 0x82, 0x8C), dark.unconnectedColor(), "Unconnected must be #7c828c");
    assertEquals(new Color(0xE6, 0xE6, 0xE6), dark.strokeColor(), "Stroke must be #e6e6e6");
  }

  @Test
  @Order(3)
  void testCaptureStage28CircuitAndCrops() throws Exception {
    assertTrue(RAMEXP_FILE.exists(), "RAMexp.circ must exist");
    Loader loader = new Loader(null);
    var logisimFile = loader.openLogisimFile(RAMEXP_FILE);
    Project proj = new Project(logisimFile);
    Circuit mainCirc = proj.getLogisimFile().getCircuit("main");

    // Light Theme 100% and 200%
    BufferedImage light100 = renderCircuitToImage(mainCirc, proj, false, 1.0);
    BufferedImage light200 = renderCircuitToImage(mainCirc, proj, false, 2.0);
    ImageIO.write(light100, "png", new File(ARTIFACT_DIR + "stage28_ramexp_light_100.png"));
    ImageIO.write(light200, "png", new File(ARTIFACT_DIR + "stage28_ramexp_light_200.png"));

    int cropX1 = Math.min(light100.getWidth() - 100, Math.max(0, (int) (light100.getWidth() * 0.22)));
    int cropY1 = Math.min(light100.getHeight() - 100, Math.max(0, (int) (light100.getHeight() * 0.18)));
    int cropW1 = Math.min(light100.getWidth() - cropX1, 460);
    int cropH1 = Math.min(light100.getHeight() - cropY1, 340);

    BufferedImage cropLight100 = light100.getSubimage(cropX1, cropY1, cropW1, cropH1);
    ImageIO.write(cropLight100, "png", new File(ARTIFACT_DIR + "stage28_crop_wires_light_100.png"));

    int cropX2 = cropX1 * 2;
    int cropY2 = cropY1 * 2;
    int cropW2 = Math.min(light200.getWidth() - cropX2, cropW1 * 2);
    int cropH2 = Math.min(light200.getHeight() - cropY2, cropH1 * 2);
    BufferedImage cropLight200 = light200.getSubimage(cropX2, cropY2, cropW2, cropH2);
    ImageIO.write(cropLight200, "png", new File(ARTIFACT_DIR + "stage28_crop_wires_light_200.png"));

    // Dark Theme 100% and 200%
    BufferedImage dark100 = renderCircuitToImage(mainCirc, proj, true, 1.0);
    BufferedImage dark200 = renderCircuitToImage(mainCirc, proj, true, 2.0);
    ImageIO.write(dark100, "png", new File(ARTIFACT_DIR + "stage28_ramexp_dark_100.png"));
    ImageIO.write(dark200, "png", new File(ARTIFACT_DIR + "stage28_ramexp_dark_200.png"));

    BufferedImage cropDark100 = dark100.getSubimage(cropX1, cropY1, cropW1, cropH1);
    ImageIO.write(cropDark100, "png", new File(ARTIFACT_DIR + "stage28_crop_wires_dark_100.png"));

    BufferedImage cropDark200 = dark200.getSubimage(cropX2, cropY2, cropW2, cropH2);
    ImageIO.write(cropDark200, "png", new File(ARTIFACT_DIR + "stage28_crop_wires_dark_200.png"));
  }

  @Test
  @Order(4)
  void testCaptureStage28Windows() throws Exception {
    assertTrue(RAMEXP_FILE.exists(), "RAMexp.circ must exist");

    // Capture Light Window
    runCaptureWindow(false);

    // Capture Dark Window
    runCaptureWindow(true);
  }

  private void runCaptureWindow(boolean dark) throws Exception {
    final String themeStr = dark ? "dark" : "light";
    final var laf = dark ? new ModernDarkLaf() : new ModernLightLaf();

    SwingUtilities.invokeAndWait(() -> {
      try {
        UIManager.setLookAndFeel(laf);
        AppPreferences.LookAndFeel.set(laf.getClass().getName());
        AppPreferences.COLOR_SCHEME.set(ColorSchemeRegistry.SCHEME_MODERN);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    final Frame[] frameHolder = new Frame[1];
    SwingUtilities.invokeAndWait(() -> {
      try {
        Loader loader = new Loader(null);
        var logisimFile = loader.openLogisimFile(RAMEXP_FILE);
        Project proj = new Project(logisimFile);
        proj.getSimulator().setAutoTicking(true);
        proj.getSimulator().step();
        Frame f = new Frame(proj);
        proj.setFrame(f);
        f.setSize(1240, 820);
        f.setVisible(true);
        f.toFront();
        f.validate();
        f.repaint();
        frameHolder[0] = f;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    Thread.sleep(600);
    Frame frame = frameHolder[0];
    assertNotNull(frame);

    // Capture Main Window
    captureWindow(frame, "stage28_" + themeStr + "_main.png");

    // Capture Preferences Simulation Tab
    PreferencesFrame prefsFrame = PreferencesFrame.getPreferencesFrame();
    SwingUtilities.invokeAndWait(() -> {
      SwingUtilities.updateComponentTreeUI(prefsFrame);
      prefsFrame.setSize(850, 600);
      prefsFrame.setSimulationTab();
      prefsFrame.setVisible(true);
      prefsFrame.toFront();
      prefsFrame.validate();
      prefsFrame.repaint();
    });
    Thread.sleep(400);

    BufferedImage prefImg = new BufferedImage(850, 600, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = prefImg.createGraphics();
    prefsFrame.paintAll(g2);
    g2.dispose();
    ImageIO.write(prefImg, "png", new File(ARTIFACT_DIR + "stage28_prefs_" + themeStr + ".png"));

    SwingUtilities.invokeAndWait(() -> {
      prefsFrame.setVisible(false);
      frame.setVisible(false);
    });
    Thread.sleep(200);
  }

  private BufferedImage renderCircuitToImage(Circuit circ, Project proj, boolean dark, double scale) {
    Palette palette = dark ? Palette.DARK : Palette.LIGHT;
    Bounds bds = circ.getBounds();
    int pad = (int) (40 * scale);
    int w = (int) Math.ceil(bds.getWidth() * scale) + pad * 2;
    int h = (int) Math.ceil(bds.getHeight() * scale) + pad * 2;
    if (w < 200) w = 200;
    if (h < 200) h = 200;

    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = img.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    g.setColor(palette.background());
    g.fillRect(0, 0, w, h);

    g.scale(scale, scale);
    g.translate(-bds.getX() + (pad / scale), -bds.getY() + (pad / scale));

    ComponentDrawContext context =
        new ComponentDrawContext(null, circ, proj.getCircuitState(circ), img.getGraphics(), g, false);
    circ.draw(context, null);
    g.dispose();
    return img;
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
}
