package com.cburch.logisim.gui.theme;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Standard WCAG 2.1 relative luminance and contrast ratio verifier for Logisim design tokens.
 */
public class ContrastTokensTest {

  public static double getRelativeLuminance(Color color) {
    double r = srgbToLinear(color.getRed() / 255.0);
    double g = srgbToLinear(color.getGreen() / 255.0);
    double b = srgbToLinear(color.getBlue() / 255.0);
    return 0.2126 * r + 0.7152 * g + 0.0722 * b;
  }

  private static double srgbToLinear(double c) {
    return (c <= 0.04045) ? (c / 12.92) : Math.pow((c + 0.055) / 1.055, 2.4);
  }

  public static double getContrastRatio(Color c1, Color c2) {
    double l1 = getRelativeLuminance(c1);
    double l2 = getRelativeLuminance(c2);
    double lighter = Math.max(l1, l2);
    double darker = Math.min(l1, l2);
    return (lighter + 0.05) / (darker + 0.05);
  }

  @Test
  void testLightTokensContrast() {
    final Color canvas = new Color(0xFC, 0xFC, 0xFC);
    final Color stroke = new Color(0x26, 0x28, 0x2C);
    final Color mutedText = new Color(0x6A, 0x6A, 0x6F);
    final Color wire0 = new Color(0x0F, 0x6B, 0x3D);
    final Color wire1 = new Color(0x26, 0xA2, 0x69);
    final Color unknown = new Color(0x1C, 0x71, 0xD8);
    final Color error = new Color(0xC0, 0x1C, 0x28);
    final Color mismatch = new Color(0xC6, 0x46, 0x00);
    final Color bus = new Color(0x3D, 0x44, 0x52);
    final Color label = new Color(0x1A, 0x5F, 0xB4);
    final Color subcircuitHeaderBg = new Color(0xE4, 0xE6, 0xEA);
    final Color subcircuitHeaderText = new Color(0x26, 0x28, 0x2C);

    Map<String, Double> results = new LinkedHashMap<>();
    results.put("Component stroke on canvas", getContrastRatio(stroke, canvas));
    results.put("Muted text on canvas", getContrastRatio(mutedText, canvas));
    results.put("Wire 0 on canvas", getContrastRatio(wire0, canvas));
    results.put("Wire 1 on canvas", getContrastRatio(wire1, canvas));
    results.put("Unknown on canvas", getContrastRatio(unknown, canvas));
    results.put("Error on canvas", getContrastRatio(error, canvas));
    results.put("Mismatch on canvas", getContrastRatio(mismatch, canvas));
    results.put("Bus on canvas", getContrastRatio(bus, canvas));
    results.put("Label on canvas", getContrastRatio(label, canvas));
    results.put("Subcircuit header text on fill", getContrastRatio(subcircuitHeaderText, subcircuitHeaderBg));

    double wire0Lum = getRelativeLuminance(wire0);
    double wire1Lum = getRelativeLuminance(wire1);
    double wireBrightnessRatio = (wire1Lum + 0.05) / (wire0Lum + 0.05);

    System.out.println("=== WCAG 2.1 Contrast Ratios for LIGHT Tokens (Canvas #FCFCFC) ===");
    for (var entry : results.entrySet()) {
      System.out.printf("%-35s : %.2f:1%n", entry.getKey(), entry.getValue());
    }
    System.out.printf("%-35s : %.2f:1%n", "Wire 1 vs Wire 0 Luminance ratio", wireBrightnessRatio);

    assertTrue(results.get("Component stroke on canvas") >= 3.0, "Component stroke must be >= 3:1");
    assertTrue(results.get("Muted text on canvas") >= 4.5, "Muted text must be >= 4.5:1");
    assertTrue(results.get("Wire 0 on canvas") >= 3.0, "Wire 0 must be >= 3:1");
    assertTrue(results.get("Wire 1 on canvas") >= 3.0, "Wire 1 must be >= 3:1");
    assertTrue(results.get("Unknown on canvas") >= 3.0, "Unknown must be >= 3:1");
    assertTrue(results.get("Error on canvas") >= 3.0, "Error must be >= 3:1");
    assertTrue(results.get("Mismatch on canvas") >= 3.0, "Mismatch must be >= 3:1");
    assertTrue(results.get("Bus on canvas") >= 3.0, "Bus must be >= 3:1");
    assertTrue(results.get("Label on canvas") >= 4.5, "Label must be >= 4.5:1");
    assertTrue(results.get("Subcircuit header text on fill") >= 4.5, "Subcircuit text must be >= 4.5:1");
    assertTrue(wireBrightnessRatio >= 1.8, "Wire 1 vs Wire 0 ratio must be >= 1.8:1");
  }

  @Test
  void testDarkTokensContrast() {
    final Color canvas = new Color(0x1F, 0x1F, 0x1F);
    final Color stroke = new Color(0xE6, 0xE6, 0xE6);
    final Color mutedText = new Color(0x9A, 0x99, 0x96);
    final Color wire0 = new Color(0x2E, 0x9D, 0x62);
    final Color wire1 = new Color(0x4B, 0xE3, 0x8A);
    final Color unknown = new Color(0x5B, 0x9B, 0xFF);
    final Color error = new Color(0xFF, 0x6B, 0x5E);
    final Color mismatch = new Color(0xF0, 0xA0, 0x30);
    final Color bus = new Color(0xB4, 0xBC, 0xC8); // Decision D6: neutral cool grey
    final Color label = new Color(0x7A, 0xA7, 0xFF);

    double strokeCr = getContrastRatio(stroke, canvas);
    double mutedCr = getContrastRatio(mutedText, canvas);
    double wire0Cr = getContrastRatio(wire0, canvas);
    double wire1Cr = getContrastRatio(wire1, canvas);
    double unknownCr = getContrastRatio(unknown, canvas);
    double errorCr = getContrastRatio(error, canvas);
    double mismatchCr = getContrastRatio(mismatch, canvas);
    double busCr = getContrastRatio(bus, canvas);
    double labelCr = getContrastRatio(label, canvas);

    System.out.println("=== WCAG 2.1 Contrast Ratios for DARK Tokens (Canvas #1F1F1F) ===");
    System.out.printf("%-35s : %.2f:1%n", "Component stroke on canvas", strokeCr);
    System.out.printf("%-35s : %.2f:1%n", "Muted text on canvas", mutedCr);
    System.out.printf("%-35s : %.2f:1%n", "Wire 0 on canvas", wire0Cr);
    System.out.printf("%-35s : %.2f:1%n", "Wire 1 on canvas", wire1Cr);
    System.out.printf("%-35s : %.2f:1%n", "Unknown on canvas", unknownCr);
    System.out.printf("%-35s : %.2f:1%n", "Error on canvas", errorCr);
    System.out.printf("%-35s : %.2f:1%n", "Mismatch on canvas", mismatchCr);
    System.out.printf("%-35s : %.2f:1%n", "Bus (#B4BCC8) on canvas", busCr);
    System.out.printf("%-35s : %.2f:1%n", "Label on canvas", labelCr);

    assertTrue(strokeCr >= 3.0);
    assertTrue(mutedCr >= 4.5);
    assertTrue(wire0Cr >= 3.0);
    assertTrue(wire1Cr >= 3.0);
    assertTrue(unknownCr >= 3.0);
    assertTrue(errorCr >= 3.0);
    assertTrue(mismatchCr >= 3.0);
    assertTrue(busCr >= 3.0);
    assertTrue(labelCr >= 4.5);
  }

  @Test
  void testPrintTokensContrast() {
    final Color paper = Color.WHITE;
    final Color black = Color.BLACK;
    double cr = getContrastRatio(black, paper);
    System.out.println("=== WCAG 2.1 Contrast Ratio for PRINT Tokens (Paper #FFFFFF) ===");
    System.out.printf("%-35s : %.2f:1%n", "Black on Paper", cr);
    assertTrue(cr >= 7.0);
  }

  @Test
  void testActionTokensContrast() {
    // Light mode surfaces
    final Color lightChrome = new Color(0xE6, 0xE9, 0xEE);
    final Color lightGroup = new Color(0xF8, 0xF9, 0xFB);

    // Light action tokens
    final Color lightCreate = new Color(0x1E, 0x8E, 0x4E);
    final Color lightMove = new Color(0x1C, 0x71, 0xD8);
    final Color lightEdit = new Color(0xC2, 0x5A, 0x00);
    final Color lightDestroy = new Color(0xC0, 0x1C, 0x28);

    System.out.println("=== WCAG 2.1 Contrast Ratios for LIGHT Action Tokens ===");
    assertAndPrintContrast("Light Create on Chrome", lightCreate, lightChrome, 3.0);
    assertAndPrintContrast("Light Create on Group", lightCreate, lightGroup, 3.0);
    assertAndPrintContrast("Light Move on Chrome", lightMove, lightChrome, 3.0);
    assertAndPrintContrast("Light Move on Group", lightMove, lightGroup, 3.0);
    assertAndPrintContrast("Light Edit on Chrome", lightEdit, lightChrome, 3.0);
    assertAndPrintContrast("Light Edit on Group", lightEdit, lightGroup, 3.0);
    assertAndPrintContrast("Light Destroy on Chrome", lightDestroy, lightChrome, 3.0);
    assertAndPrintContrast("Light Destroy on Group", lightDestroy, lightGroup, 3.0);

    // Dark mode surfaces
    final Color darkChrome = new Color(0x30, 0x33, 0x38);
    final Color darkGroup = new Color(0x3B, 0x3E, 0x45);

    // Dark action tokens tuned for high visibility and contrast
    final Color darkCreate = new Color(0x52, 0xD6, 0x8A);
    final Color darkMove = new Color(0x58, 0xA6, 0xFF);
    final Color darkEdit = new Color(0xFF, 0xB3, 0x47);
    final Color darkDestroy = new Color(0xFF, 0x6B, 0x6B);

    System.out.println("=== WCAG 2.1 Contrast Ratios for DARK Action Tokens ===");
    assertAndPrintContrast("Dark Create on Chrome", darkCreate, darkChrome, 3.0);
    assertAndPrintContrast("Dark Create on Group", darkCreate, darkGroup, 3.0);
    assertAndPrintContrast("Dark Move on Chrome", darkMove, darkChrome, 3.0);
    assertAndPrintContrast("Dark Move on Group", darkMove, darkGroup, 3.0);
    assertAndPrintContrast("Dark Edit on Chrome", darkEdit, darkChrome, 3.0);
    assertAndPrintContrast("Dark Edit on Group", darkEdit, darkGroup, 3.0);
    assertAndPrintContrast("Dark Destroy on Chrome", darkDestroy, darkChrome, 3.0);
    assertAndPrintContrast("Dark Destroy on Group", darkDestroy, darkGroup, 3.0);
  }

  private static void assertAndPrintContrast(String label, Color fg, Color bg, double minRatio) {
    double cr = getContrastRatio(fg, bg);
    System.out.printf("%-35s : %.2f:1%n", label, cr);
    assertTrue(cr >= minRatio, label + " contrast " + cr + " must be >= " + minRatio + ":1");
  }
}
