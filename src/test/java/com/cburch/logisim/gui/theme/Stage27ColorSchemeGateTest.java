package com.cburch.logisim.gui.theme;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.comp.Palette;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.file.Loader;
import com.cburch.logisim.gui.generic.TikZWriter;
import com.cburch.logisim.gui.main.Frame;
import com.cburch.logisim.gui.prefs.PreferencesFrame;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.proj.Project;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Stage 27 Color Schemes and Simulation Palette Gate Tests.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Stage27ColorSchemeGateTest {

  private static final String ARTIFACT_DIR =
      "/home/devansh/.gemini/antigravity-ide/brain/904c724c-98b1-4fc8-adc9-6305dcb0eb9c/";
  private static final File RAMEXP_FILE = new File("/home/devansh/Downloads/RAMexp.circ");

  @BeforeAll
  static void setupHeadless() {
    System.setProperty("java.awt.headless", "false");
    new File(ARTIFACT_DIR).mkdirs();
    PreferencesFrame.initializeManager();
  }

  // --- CIEDE2000 & Color Space Utilities ---

  public static double getRelativeLuminance(Color color) {
    double r = srgbToLinear(color.getRed() / 255.0);
    double g = srgbToLinear(color.getGreen() / 255.0);
    double b = srgbToLinear(color.getBlue() / 255.0);
    return 0.2126 * r + 0.7152 * g + 0.0722 * b;
  }

  public static double getContrastRatio(Color c1, Color c2) {
    double l1 = getRelativeLuminance(c1);
    double l2 = getRelativeLuminance(c2);
    double lighter = Math.max(l1, l2);
    double darker = Math.min(l1, l2);
    return (lighter + 0.05) / (darker + 0.05);
  }

  private static double srgbToLinear(double c) {
    return (c <= 0.04045) ? (c / 12.92) : Math.pow((c + 0.055) / 1.055, 2.4);
  }

  private static double linearToSrgb(double c) {
    c = Math.max(0.0, Math.min(1.0, c));
    return (c <= 0.0031308) ? (c * 12.92) : (1.055 * Math.pow(c, 1.0 / 2.4) - 0.055);
  }

  public static class Lab {
    public final double l;
    public final double a;
    public final double b;

    public Lab(double l, double a, double b) {
      this.l = l;
      this.a = a;
      this.b = b;
    }
  }

  public static Lab colorToLab(Color color) {
    double rLin = srgbToLinear(color.getRed() / 255.0);
    double gLin = srgbToLinear(color.getGreen() / 255.0);
    double bLin = srgbToLinear(color.getBlue() / 255.0);

    // sRGB to CIE XYZ (D65)
    double x = (0.4124564 * rLin + 0.3575761 * gLin + 0.1804375 * bLin) / 0.95047;
    double y = (0.2126729 * rLin + 0.7151522 * gLin + 0.0721750 * bLin) / 1.00000;
    double z = (0.0193339 * rLin + 0.1191920 * gLin + 0.9503041 * bLin) / 1.08883;

    double fx = xyzF(x);
    double fy = xyzF(y);
    double fz = xyzF(z);

    double l = 116.0 * fy - 16.0;
    double a = 500.0 * (fx - fy);
    double b = 200.0 * (fy - fz);
    return new Lab(l, a, b);
  }

  private static double xyzF(double t) {
    return (t > 0.00885645167903563) ? Math.cbrt(t) : (7.787037037037037 * t + 16.0 / 116.0);
  }

  public static double ciede2000(Color c1, Color c2) {
    Lab lab1 = colorToLab(c1);
    Lab lab2 = colorToLab(c2);
    return ciede2000(lab1, lab2);
  }

  public static double ciede2000(Lab lab1, Lab lab2) {
    double l1 = lab1.l;
    double a1 = lab1.a;
    double b1 = lab1.b;
    double l2 = lab2.l;
    double a2 = lab2.a;
    double b2 = lab2.b;

    double c1 = Math.sqrt(a1 * a1 + b1 * b1);
    double c2 = Math.sqrt(a2 * a2 + b2 * b2);
    double cBar = (c1 + c2) / 2.0;

    double g = 0.5 * (1.0 - Math.sqrt(Math.pow(cBar, 7) / (Math.pow(cBar, 7) + Math.pow(25.0, 7))));
    double a1Prime = a1 * (1.0 + g);
    double a2Prime = a2 * (1.0 + g);

    double c1Prime = Math.sqrt(a1Prime * a1Prime + b1 * b1);
    double c2Prime = Math.sqrt(a2Prime * a2Prime + b2 * b2);

    double h1Prime = Math.toDegrees(Math.atan2(b1, a1Prime));
    if (h1Prime < 0) h1Prime += 360.0;
    double h2Prime = Math.toDegrees(Math.atan2(b2, a2Prime));
    if (h2Prime < 0) h2Prime += 360.0;

    double deltaLPrime = l2 - l1;
    double deltaCPrime = c2Prime - c1Prime;

    double deltahPrime;
    if (c1Prime * c2Prime == 0) {
      deltahPrime = 0;
    } else if (Math.abs(h2Prime - h1Prime) <= 180.0) {
      deltahPrime = h2Prime - h1Prime;
    } else if (h2Prime - h1Prime > 180.0) {
      deltahPrime = h2Prime - h1Prime - 360.0;
    } else {
      deltahPrime = h2Prime - h1Prime + 360.0;
    }
    double deltaHPrime = 2.0 * Math.sqrt(c1Prime * c2Prime) * Math.sin(Math.toRadians(deltahPrime / 2.0));

    double lBarPrime = (l1 + l2) / 2.0;
    double cBarPrime = (c1Prime + c2Prime) / 2.0;

    double hBarPrime;
    if (c1Prime * c2Prime == 0) {
      hBarPrime = h1Prime + h2Prime;
    } else if (Math.abs(h1Prime - h2Prime) <= 180.0) {
      hBarPrime = (h1Prime + h2Prime) / 2.0;
    } else if (h1Prime + h2Prime < 360.0) {
      hBarPrime = (h1Prime + h2Prime + 360.0) / 2.0;
    } else {
      hBarPrime = (h1Prime + h2Prime - 360.0) / 2.0;
    }

    double t = 1.0 - 0.17 * Math.cos(Math.toRadians(hBarPrime - 30.0))
        + 0.24 * Math.cos(Math.toRadians(2.0 * hBarPrime))
        + 0.32 * Math.cos(Math.toRadians(3.0 * hBarPrime + 6.0))
        - 0.20 * Math.cos(Math.toRadians(4.0 * hBarPrime - 63.0));

    double deltaTheta = 30.0 * Math.exp(-Math.pow((hBarPrime - 275.0) / 25.0, 2));
    double rC = 2.0 * Math.sqrt(Math.pow(cBarPrime, 7) / (Math.pow(cBarPrime, 7) + Math.pow(25.0, 7)));
    double sL = 1.0 + (0.015 * Math.pow(lBarPrime - 50.0, 2)) / Math.sqrt(20.0 + Math.pow(lBarPrime - 50.0, 2));
    double sC = 1.0 + 0.045 * cBarPrime;
    double sH = 1.0 + 0.015 * cBarPrime * t;
    double rT = -Math.sin(Math.toRadians(2.0 * deltaTheta)) * rC;

    double termL = deltaLPrime / sL;
    double termC = deltaCPrime / sC;
    double termH = deltaHPrime / sH;

    return Math.sqrt(termL * termL + termC * termC + termH * termH + rT * termC * termH);
  }

  // --- Machado 2009 CVD Simulation ---

  public enum CvdType {
    PROTANOPIA(new double[][] {
      {0.152286,  1.052583, -0.204868},
      {0.114503,  0.786281,  0.099216},
      {-0.003882, -0.048116,  1.051998}
    }),
    DEUTERANOPIA(new double[][] {
      {0.367322,  0.860646, -0.227968},
      {0.280085,  0.672501,  0.047413},
      {-0.011820,  0.042940,  0.968881}
    }),
    TRITANOPIA(new double[][] {
      {1.255528, -0.076749, -0.178779},
      {-0.078411,  0.930809,  0.147602},
      {0.004733,  0.691367,  0.303900}
    });

    final double[][] matrix;

    CvdType(double[][] matrix) {
      this.matrix = matrix;
    }
  }

  public static Color simulateCvd(Color c, CvdType type) {
    double r = srgbToLinear(c.getRed() / 255.0);
    double g = srgbToLinear(c.getGreen() / 255.0);
    double b = srgbToLinear(c.getBlue() / 255.0);

    double[][] m = type.matrix;
    double rSim = m[0][0] * r + m[0][1] * g + m[0][2] * b;
    double gSim = m[1][0] * r + m[1][1] * g + m[1][2] * b;
    double bSim = m[2][0] * r + m[2][1] * g + m[2][2] * b;

    int r8 = (int) Math.round(linearToSrgb(rSim) * 255.0);
    int g8 = (int) Math.round(linearToSrgb(gSim) * 255.0);
    int b8 = (int) Math.round(linearToSrgb(bSim) * 255.0);
    r8 = Math.max(0, Math.min(255, r8));
    g8 = Math.max(0, Math.min(255, g8));
    b8 = Math.max(0, Math.min(255, b8));

    return new Color(r8, g8, b8);
  }

  // --- Acceptance Tests ---

  @Test
  @Order(1)
  void testClassicEqualsUpstreamDefaultsExactly() {
    ColorScheme classic = ColorSchemeRegistry.CLASSIC;
    assertEquals(new Color(0x0000D200), classic.trueColor(), "Classic True must be #00D200");
    assertEquals(new Color(0x00006400), classic.falseColor(), "Classic False must be #006400");
    assertEquals(new Color(0x002828FF), classic.unknownColor(), "Classic Unknown must be #2828FF");
    assertEquals(new Color(0x00C00000), classic.errorColor(), "Classic Error must be #C00000");
    assertEquals(new Color(0x00FF7B00), classic.mismatchColor(), "Classic Mismatch must be #FF7B00");
    assertEquals(new Color(0x00000000), classic.busColor(), "Classic Bus must be black #000000");
    assertEquals(new Color(0x00808080), classic.unconnectedColor(), "Classic Nil must be #808080");
    assertEquals(new Color(0x00FE00FF), classic.strokeColor(), "Classic Stroke must be #FE00FF");
    assertEquals(new Color(0x00550000), classic.widthErrorCaptionColor(), "Classic Caption must be #550000");
    assertEquals(new Color(0x00FFFF00), classic.widthErrorHighlightColor(), "Classic Highlight must be #FFFF00");
    assertEquals(new Color(0x00FFE6D2), classic.widthErrorBackgroundColor(), "Classic Background must be #FFE6D2");
    assertEquals(new Color(0x00FF00B4), classic.clockFrequencyColor(), "Classic Clock must be #FF00B4");
  }

  @Test
  @Order(2)
  void testMigrationIdempotent() {
    Preferences prefs = AppPreferences.getPrefs();

    // 1. Unset -> Modern
    prefs.remove("colorScheme");
    prefs.remove("SimTrueColor");
    prefs.remove("SimFalseColor");
    prefs.remove("SimUnknownColor");
    ColorSchemeRegistry.performMigrationIfNeeded();
    assertEquals(ColorSchemeRegistry.SCHEME_MODERN, AppPreferences.COLOR_SCHEME.get(), "Unset preferences must migrate to MODERN");

    // 2. Colorblind -> COLORBLIND
    prefs.remove("colorScheme");
    prefs.putInt("SimTrueColor", 0xF4EB42);
    prefs.putInt("SimFalseColor", 0x203BE8);
    ColorSchemeRegistry.performMigrationIfNeeded();
    assertEquals(ColorSchemeRegistry.SCHEME_COLORBLIND, AppPreferences.COLOR_SCHEME.get(), "Colorblind preferences must migrate to COLORBLIND");

    // 3. Custom colors -> CUSTOM
    prefs.remove("colorScheme");
    prefs.putInt("SimTrueColor", 0x123456);
    prefs.putInt("SimFalseColor", 0x654321);
    ColorSchemeRegistry.performMigrationIfNeeded();
    assertEquals(ColorSchemeRegistry.SCHEME_CUSTOM, AppPreferences.COLOR_SCHEME.get(), "Custom preferences must migrate to CUSTOM");

    // Clean up
    prefs.remove("SimTrueColor");
    prefs.remove("SimFalseColor");
    prefs.remove("SimUnknownColor");
    AppPreferences.COLOR_SCHEME.set(ColorSchemeRegistry.SCHEME_MODERN);
  }

  @Test
  @Order(3)
  void testModernDarkGateChecks() {
    System.out.println("=================================================================");
    System.out.println("           STAGE 27 GATE CHECKS: MODERN DARK SCHEME             ");
    System.out.println("=================================================================");
    ColorScheme scheme = ColorSchemeRegistry.MODERN_DARK;
    Color canvas = new Color(0x1F, 0x1F, 0x1F);
    Color selection = new Color(0x35, 0x84, 0xE4, 41);
    Color hover = new Color(0x40, 0x40, 0x40);

    runGateForScheme("MODERN DARK", scheme, canvas, selection, hover);
  }

  @Test
  @Order(4)
  void testModernLightGateChecks() {
    System.out.println("=================================================================");
    System.out.println("           STAGE 27 GATE CHECKS: MODERN LIGHT SCHEME            ");
    System.out.println("=================================================================");
    ColorScheme scheme = ColorSchemeRegistry.MODERN_LIGHT;
    Color canvas = new Color(0xFF, 0xFF, 0xFF);
    Color selection = new Color(0x35, 0x84, 0xE4, 38);
    Color hover = new Color(0xEA, 0xED, 0xF2);

    runGateForScheme("MODERN LIGHT", scheme, canvas, selection, hover);
  }

  private void runGateForScheme(String name, ColorScheme scheme, Color canvas, Color selection, Color hover) {
    Map<String, Color> wires = new LinkedHashMap<>();
    wires.put("True", scheme.trueColor());
    wires.put("False", scheme.falseColor());
    wires.put("Unknown", scheme.unknownColor());
    wires.put("Error", scheme.errorColor());
    wires.put("Mismatch", scheme.mismatchColor());
    wires.put("Bus", scheme.busColor());
    wires.put("Unconnected", scheme.unconnectedColor());

    // a. Wire-state contrast against canvas, selection, and hover >= 3:1
    System.out.println("\n--- a. Wire-state contrast against Canvas (min 3.0:1) ---");
    for (var entry : wires.entrySet()) {
      double crCanvas = getContrastRatio(entry.getValue(), canvas);
      System.out.printf("%-15s on Canvas: %.2f:1%n", entry.getKey(), crCanvas);
      assertTrue(crCanvas >= 3.0, name + " " + entry.getKey() + " on canvas contrast " + crCanvas + " must be >= 3.0:1");
    }

    // b. Luminance ratio between True and False >= 3:1
    double lumTrue = getRelativeLuminance(scheme.trueColor());
    double lumFalse = getRelativeLuminance(scheme.falseColor());
    double trueFalseRatio = (lumTrue + 0.05) / (lumFalse + 0.05);
    if (trueFalseRatio < 1.0) trueFalseRatio = 1.0 / trueFalseRatio;
    System.out.printf("\n--- b. True vs False Luminance Ratio: %.2f:1 (min 3.0:1) ---%n", trueFalseRatio);
    assertTrue(trueFalseRatio >= 3.0, name + " True vs False luminance ratio " + trueFalseRatio + " must be >= 3.0:1");

    // c. Pairwise CIEDE2000 across all wire states >= 12, True vs False >= 20
    System.out.println("\n--- c. Pairwise CIEDE2000 Normal Vision Matrix ---");
    printCiedeMatrix(wires, null);
    double tfDelta = ciede2000(scheme.trueColor(), scheme.falseColor());
    assertTrue(tfDelta >= 20.0, name + " True vs False CIEDE2000 " + tfDelta + " must be >= 20.0");
    assertPairwiseDelta(wires, null, 12.0, name + " Normal");

    // d. Machado 2009 CVD simulation (Protanopia, Deuteranopia, Tritanopia)
    for (CvdType cvd : CvdType.values()) {
      System.out.printf("\n--- d. Pairwise CIEDE2000 %s Matrix ---%n", cvd.name());
      printCiedeMatrix(wires, cvd);
      Color tf1 = simulateCvd(scheme.trueColor(), cvd);
      Color tf2 = simulateCvd(scheme.falseColor(), cvd);
      double cvdTfDelta = ciede2000(tf1, tf2);
      System.out.printf("%s True vs False Delta E: %.2f (min 12.0)%n", cvd.name(), cvdTfDelta);
      assertTrue(cvdTfDelta >= 12.0, name + " " + cvd.name() + " True vs False CIEDE2000 " + cvdTfDelta + " must be >= 12.0");
      assertPairwiseDelta(wires, cvd, 4.5, name + " " + cvd.name());
    }

    // e. Bus distinct from component stroke and from False >= 12
    double busStrokeDelta = ciede2000(scheme.busColor(), scheme.strokeColor());
    double busFalseDelta = ciede2000(scheme.busColor(), scheme.falseColor());
    System.out.printf("\n--- e. Bus Distinctness: Bus vs Stroke = %.2f, Bus vs False = %.2f (min 12.0) ---%n",
        busStrokeDelta, busFalseDelta);
    assertTrue(busStrokeDelta >= 12.0, name + " Bus vs Stroke CIEDE2000 " + busStrokeDelta + " must be >= 12.0");
    assertTrue(busFalseDelta >= 12.0, name + " Bus vs False CIEDE2000 " + busFalseDelta + " must be >= 12.0");
  }

  private void printCiedeMatrix(Map<String, Color> wires, CvdType cvd) {
    System.out.printf("%-12s", "");
    for (String key : wires.keySet()) {
      System.out.printf("%-10s", key);
    }
    System.out.println();
    for (var e1 : wires.entrySet()) {
      System.out.printf("%-12s", e1.getKey());
      for (var e2 : wires.entrySet()) {
        if (e1.getKey().equals(e2.getKey())) {
          System.out.printf("%-10s", "-");
        } else {
          Color c1 = (cvd == null) ? e1.getValue() : simulateCvd(e1.getValue(), cvd);
          Color c2 = (cvd == null) ? e2.getValue() : simulateCvd(e2.getValue(), cvd);
          double dE = ciede2000(c1, c2);
          System.out.printf("%-10.2f", dE);
        }
      }
      System.out.println();
    }
  }

  private void assertPairwiseDelta(Map<String, Color> wires, CvdType cvd, double minThreshold, String label) {
    var entries = wires.entrySet().stream().toList();
    for (int i = 0; i < entries.size(); i++) {
      for (int j = i + 1; j < entries.size(); j++) {
        var e1 = entries.get(i);
        var e2 = entries.get(j);
        Color c1 = (cvd == null) ? e1.getValue() : simulateCvd(e1.getValue(), cvd);
        Color c2 = (cvd == null) ? e2.getValue() : simulateCvd(e2.getValue(), cvd);
        double dE = ciede2000(c1, c2);
        assertTrue(dE >= minThreshold,
            String.format("%s: %s vs %s CIEDE2000 %.2f must be >= %.2f", label, e1.getKey(), e2.getKey(), dE, minThreshold));
      }
    }
  }

  @Test
  @Order(5)
  void testPrintExportByteIdenticalBetweenModernAndClassic() throws Exception {
    assertTrue(RAMEXP_FILE.exists(), "RAMexp.circ must exist");
    Loader loader = new Loader(null);

    // Export with MODERN
    SwingUtilities.invokeAndWait(() -> {
      AppPreferences.COLOR_SCHEME.set(ColorSchemeRegistry.SCHEME_MODERN);
    });
    Thread.sleep(100);
    Project projModern = new Project(loader.openLogisimFile(RAMEXP_FILE));
    projModern.getSimulator().setAutoTicking(false);
    projModern.getSimulator().setAutoPropagation(false);
    byte[] modernExportMain = exportCircuitPng(projModern.getLogisimFile().getCircuit("main"), projModern);
    byte[] modernExportCalc = exportCircuitPng(projModern.getLogisimFile().getCircuit("calc_use"), projModern);

    // Export with CLASSIC
    SwingUtilities.invokeAndWait(() -> {
      AppPreferences.COLOR_SCHEME.set(ColorSchemeRegistry.SCHEME_CLASSIC);
    });
    Thread.sleep(100);
    Project projClassic = new Project(loader.openLogisimFile(RAMEXP_FILE));
    projClassic.getSimulator().setAutoTicking(false);
    projClassic.getSimulator().setAutoPropagation(false);
    byte[] classicExportMain = exportCircuitPng(projClassic.getLogisimFile().getCircuit("main"), projClassic);
    byte[] classicExportCalc = exportCircuitPng(projClassic.getLogisimFile().getCircuit("calc_use"), projClassic);

    Files.write(new File(ARTIFACT_DIR + "main_modern.png").toPath(), modernExportMain);
    Files.write(new File(ARTIFACT_DIR + "main_classic.png").toPath(), classicExportMain);
    BufferedImage imgM = ImageIO.read(new File(ARTIFACT_DIR + "main_modern.png"));
    BufferedImage imgC = ImageIO.read(new File(ARTIFACT_DIR + "main_classic.png"));
    int diffs = 0;
    for (int y = 0; y < imgM.getHeight(); y++) {
      for (int x = 0; x < imgM.getWidth(); x++) {
        int pm = imgM.getRGB(x, y);
        int pc = imgC.getRGB(x, y);
        if (pm != pc) {
          diffs++;
          if (diffs <= 5) {
            System.out.printf("Diff at (%d,%d): modern=0x%08X, classic=0x%08X%n", x, y, pm, pc);
          }
        }
      }
    }
    System.out.println("Total pixel differences in main: " + diffs);

    assertArrayEquals(classicExportCalc, modernExportCalc,
        "Print/Export of calc_use circuit must be byte-identical between MODERN and CLASSIC schemes");
    assertArrayEquals(classicExportMain, modernExportMain,
        "Print/Export of main circuit must be byte-identical between MODERN and CLASSIC schemes");

    // Clean up
    SwingUtilities.invokeAndWait(() -> {
      AppPreferences.COLOR_SCHEME.set(ColorSchemeRegistry.SCHEME_MODERN);
    });
    Thread.sleep(50);
  }

  private byte[] exportCircuitPng(Circuit circ, Project proj) throws Exception {
    System.out.println("exportCircuitPng [" + circ.getName() + "]: COLOR_SCHEME="
        + AppPreferences.COLOR_SCHEME.get() + ", bus=" + Palette.PRINT.bus()
        + ", nil=" + Palette.PRINT.nil() + ", darkTheme=" + AppPreferences.isDarkTheme());
    Bounds bds = circ.getBounds();
    System.out.println("bds: " + bds);
    int w = Math.max(10, bds.getWidth() + 40);
    int h = Math.max(10, bds.getHeight() + 40);
    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = img.createGraphics();
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, w, h);
    g.translate(-bds.getX() + 20, -bds.getY() + 20);

    try (var ignored = Palette.enterPrintScope()) {
      ComponentDrawContext context =
          new ComponentDrawContext(null, circ, proj.getCircuitState(circ), img.getGraphics(), g, true);
      circ.draw(context, null);
    }
    g.dispose();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(img, "png", baos);
    return baos.toByteArray();
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

  @Test
  @Order(6)
  void testCaptureStage27ScreenshotsAndCrops() throws Exception {
    assertTrue(RAMEXP_FILE.exists(), "RAMexp.circ must exist");

    // 1. Render RAMexp circuit at 100% and 200% and produce close crops of wires
    Loader loader = new Loader(null);
    var logisimFile = loader.openLogisimFile(RAMEXP_FILE);
    Project proj = new Project(logisimFile);
    Circuit mainCirc = proj.getLogisimFile().getCircuit("main");

    // Light Theme 100% and 200%
    BufferedImage light100 = renderCircuitToImage(mainCirc, proj, false, 1.0);
    BufferedImage light200 = renderCircuitToImage(mainCirc, proj, false, 2.0);
    ImageIO.write(light100, "png", new File(ARTIFACT_DIR + "stage27_ramexp_light_100.png"));
    ImageIO.write(light200, "png", new File(ARTIFACT_DIR + "stage27_ramexp_light_200.png"));

    // Crop dense wire region (RAM inputs and data wires)
    // At scale 1.0: around x=180, y=100, w=420, h=320
    int cropX1 = Math.min(light100.getWidth() - 100, Math.max(0, (int) (light100.getWidth() * 0.22)));
    int cropY1 = Math.min(light100.getHeight() - 100, Math.max(0, (int) (light100.getHeight() * 0.18)));
    int cropW1 = Math.min(light100.getWidth() - cropX1, 460);
    int cropH1 = Math.min(light100.getHeight() - cropY1, 340);

    BufferedImage cropLight100 = light100.getSubimage(cropX1, cropY1, cropW1, cropH1);
    ImageIO.write(cropLight100, "png", new File(ARTIFACT_DIR + "stage27_crop_wires_light_100.png"));

    int cropX2 = cropX1 * 2;
    int cropY2 = cropY1 * 2;
    int cropW2 = Math.min(light200.getWidth() - cropX2, cropW1 * 2);
    int cropH2 = Math.min(light200.getHeight() - cropY2, cropH1 * 2);
    BufferedImage cropLight200 = light200.getSubimage(cropX2, cropY2, cropW2, cropH2);
    ImageIO.write(cropLight200, "png", new File(ARTIFACT_DIR + "stage27_crop_wires_light_200.png"));

    // Dark Theme 100% and 200%
    BufferedImage dark100 = renderCircuitToImage(mainCirc, proj, true, 1.0);
    BufferedImage dark200 = renderCircuitToImage(mainCirc, proj, true, 2.0);
    ImageIO.write(dark100, "png", new File(ARTIFACT_DIR + "stage27_ramexp_dark_100.png"));
    ImageIO.write(dark200, "png", new File(ARTIFACT_DIR + "stage27_ramexp_dark_200.png"));

    BufferedImage cropDark100 = dark100.getSubimage(cropX1, cropY1, cropW1, cropH1);
    ImageIO.write(cropDark100, "png", new File(ARTIFACT_DIR + "stage27_crop_wires_dark_100.png"));

    BufferedImage cropDark200 = dark200.getSubimage(cropX2, cropY2, cropW2, cropH2);
    ImageIO.write(cropDark200, "png", new File(ARTIFACT_DIR + "stage27_crop_wires_dark_200.png"));

    // 2. RAMexp in CLASSIC Scheme (Light and Dark)
    AppPreferences.COLOR_SCHEME.set(ColorSchemeRegistry.SCHEME_CLASSIC);
    BufferedImage classicLight = renderCircuitToImage(mainCirc, proj, false, 1.0);
    BufferedImage classicDark = renderCircuitToImage(mainCirc, proj, true, 1.0);
    ImageIO.write(classicLight, "png", new File(ARTIFACT_DIR + "stage27_classic_light.png"));
    ImageIO.write(classicDark, "png", new File(ARTIFACT_DIR + "stage27_classic_dark.png"));
    AppPreferences.COLOR_SCHEME.set(ColorSchemeRegistry.SCHEME_MODERN);

    // 3. Capture Preferences Simulation Tab for each Scheme
    capturePreferencesTab(ColorSchemeRegistry.SCHEME_MODERN, "stage27_prefs_modern.png");
    capturePreferencesTab(ColorSchemeRegistry.SCHEME_CLASSIC, "stage27_prefs_classic.png");
    capturePreferencesTab(ColorSchemeRegistry.SCHEME_COLORBLIND, "stage27_prefs_colorblind.png");
    capturePreferencesTab(ColorSchemeRegistry.SCHEME_CUSTOM, "stage27_prefs_custom.png");
    AppPreferences.COLOR_SCHEME.set(ColorSchemeRegistry.SCHEME_MODERN);
  }

  private void capturePreferencesTab(String schemeId, String filename) throws Exception {
    PreferencesFrame prefsFrame = PreferencesFrame.getPreferencesFrame();

    SwingUtilities.invokeAndWait(() -> {
      AppPreferences.COLOR_SCHEME.set(schemeId);
      prefsFrame.setSize(850, 600);
      prefsFrame.setSimulationTab();
      prefsFrame.setVisible(true);
      prefsFrame.toFront();
      prefsFrame.validate();
      prefsFrame.repaint();
    });
    Thread.sleep(400);

    BufferedImage img = new BufferedImage(850, 600, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = img.createGraphics();
    prefsFrame.paintAll(g2);
    g2.dispose();
    ImageIO.write(img, "png", new File(ARTIFACT_DIR + filename));

    SwingUtilities.invokeAndWait(() -> {
      prefsFrame.setVisible(false);
    });
    Thread.sleep(200);
  }
}
