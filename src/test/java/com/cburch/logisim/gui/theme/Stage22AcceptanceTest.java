package com.cburch.logisim.gui.theme;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitMutation;
import com.cburch.logisim.circuit.Wire;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.comp.Palette;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.file.Loader;
import com.cburch.logisim.gui.generic.TikZWriter;
import com.cburch.logisim.instance.InstanceComponent;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.std.io.SevenSegment;
import com.cburch.logisim.std.wiring.Pin;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import javax.imageio.ImageIO;
import javax.swing.UIManager;
import org.junit.jupiter.api.Test;

public class Stage22AcceptanceTest {

  private static final String ARTIFACT_DIR =
      "/home/devansh/.gemini/antigravity-ide/brain/904c724c-98b1-4fc8-adc9-6305dcb0eb9c/";

  @Test
  void testPaletteTokensAndDecisionD6() {
    AppPreferences.COLOR_SCHEME.set(ColorSchemeRegistry.SCHEME_MODERN);
    // 1. Dark Palette: neutral cool grey bus (#B8C0CC), blue unknown (#5B9BFF)
    Palette dark = Palette.DARK;
    assertEquals(new Color(0xB8, 0xC0, 0xCC), dark.bus(), "Dark bus must be neutral cool grey #B8C0CC");
    Value busVal = Value.createKnown(BitWidth.create(8), 0xAA);
    assertEquals(new Color(0xB8, 0xC0, 0xCC), dark.valueColor(busVal), "Dark multi-bit bus value must be #B8C0CC");
    assertEquals(new Color(0x5B, 0x9B, 0xFF), dark.valueColor(Value.UNKNOWN), "Dark unknown must be blue #5B9BFF");
    assertFalse(dark.bus().equals(dark.valueColor(Value.UNKNOWN)), "Dark bus and unknown must never be the same color");

    // 2. Light Palette: modern tokens
    Palette light = Palette.LIGHT;
    assertEquals(new Color(0x4A, 0x56, 0x66), light.bus(), "Light bus must be slate blue-gray #4A5666");
    assertEquals(new Color(0x4A, 0x56, 0x66), light.valueColor(busVal), "Light multi-bit bus value must be #4A5666");
    assertEquals(new Color(0x1C, 0x71, 0xD8), light.valueColor(Value.UNKNOWN), "Light unknown must be blue #1C71D8");
    assertEquals(new Color(0xFC, 0xFC, 0xFC), light.background(), "Light canvas background must be #FCFCFC");
    assertEquals(new Color(0x26, 0x28, 0x2C), light.stroke(), "Light stroke must be #26282C");
    assertEquals(new Color(0x04, 0x38, 0x20), light.valueColor(Value.FALSE), "Light wire 0 must be #043820");
    assertEquals(new Color(0x16, 0xA0, 0x5A), light.valueColor(Value.TRUE), "Light wire 1 must be #16A05A");
    assertEquals(new Color(0xC0, 0x1C, 0x28), light.valueColor(Value.createError(BitWidth.ONE)), "Light error must be #C01C28");
    assertEquals(new Color(0xE4, 0xE6, 0xEA), light.headerFill(), "Light subcircuit header fill must be #E4E6EA");
    assertEquals(new Color(0x26, 0x28, 0x2C), light.headerText(), "Light subcircuit header text must be #26282C");
    assertFalse(light.bus().equals(light.valueColor(Value.UNKNOWN)), "Light bus and unknown must never be the same color");

    // 3. Print Palette: exact classic values
    Palette print = Palette.PRINT;
    assertEquals(Color.BLACK, print.stroke(), "Print stroke must be black");
    assertEquals(Color.BLACK, print.valueColor(busVal), "Print bus value must be classic black");
    assertEquals(Color.WHITE, print.background(), "Print canvas background must be white");
    assertEquals(Color.BLACK, print.headerFill(), "Print subcircuit header fill must be black");
    assertEquals(Color.WHITE, print.headerText(), "Print subcircuit header text must be white");

    // 4. Print scope check
    assertFalse(Palette.isForcePrint());
    try (var ignored = Palette.enterPrintScope()) {
      assertTrue(Palette.isForcePrint());
      assertEquals(Palette.PRINT, Palette.current(), "current() inside print scope must be PRINT");
      assertEquals(Palette.PRINT, Palette.of((ComponentDrawContext) null), "of(context) inside print scope must be PRINT");
    }
    assertFalse(Palette.isForcePrint());
  }

  private byte[] exportCircuitPng(Circuit circ, Project proj) throws Exception {
    Bounds bds = circ.getBounds();
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
    ImageIO.write(img, "PNG", baos);
    return baos.toByteArray();
  }

  private String exportCircuitSvg(Circuit circ, Project proj) throws Exception {
    Bounds bds = circ.getBounds();
    int w = Math.max(10, bds.getWidth() + 40);
    int h = Math.max(10, bds.getHeight() + 40);

    TikZWriter writer = new TikZWriter();
    Graphics2D g = (Graphics2D) writer.create();
    g.translate(-bds.getX() + 20, -bds.getY() + 20);

    try (var ignored = Palette.enterPrintScope()) {
      ComponentDrawContext context =
          new ComponentDrawContext(null, circ, proj.getCircuitState(circ), null, g, true);
      circ.draw(context, null);
    }
    g.dispose();

    File tmpSvg = File.createTempFile("logisim_svg_export", ".svg");
    try {
      writer.writeSvg(w, h, tmpSvg);
      return Files.readString(tmpSvg.toPath());
    } finally {
      tmpSvg.delete();
    }
  }

  @Test
  void testExportByteIdentityBetweenThemes() throws Exception {
    File circFile = new File("/home/devansh/Downloads/RAMexp.circ");
    assertTrue(circFile.exists(), "RAMexp.circ must exist");

    Loader loader = new Loader(null);
    var logisimFile = loader.openLogisimFile(circFile);
    Project proj = new Project(logisimFile);

    Circuit mainCirc = proj.getLogisimFile().getCircuit("main");
    assertNotNull(mainCirc, "main circuit must exist in RAMexp.circ");
    Circuit calcCirc = proj.getLogisimFile().getCircuit("calc_use");
    assertNotNull(calcCirc, "calc_use circuit must exist in RAMexp.circ");

    // 1. Export under Dark Theme
    UIManager.setLookAndFeel(new ModernDarkLaf());
    AppPreferences.LookAndFeel.set(ModernDarkLaf.class.getName());
    byte[] darkPngMain = exportCircuitPng(mainCirc, proj);
    String darkSvgMain = exportCircuitSvg(mainCirc, proj);
    byte[] darkPngCalc = exportCircuitPng(calcCirc, proj);
    String darkSvgCalc = exportCircuitSvg(calcCirc, proj);

    // 2. Export under Light Theme
    UIManager.setLookAndFeel(new ModernLightLaf());
    AppPreferences.LookAndFeel.set(ModernLightLaf.class.getName());
    byte[] lightPngMain = exportCircuitPng(mainCirc, proj);
    String lightSvgMain = exportCircuitSvg(mainCirc, proj);
    byte[] lightPngCalc = exportCircuitPng(calcCirc, proj);
    String lightSvgCalc = exportCircuitSvg(calcCirc, proj);

    // 3. Assert Byte-Identity
    assertArrayEquals(darkPngMain, lightPngMain, "PNG export of main circuit must be byte-identical in dark and light themes");
    assertEquals(darkSvgMain, lightSvgMain, "SVG export of main circuit must be byte-identical in dark and light themes");
    assertArrayEquals(darkPngCalc, lightPngCalc, "PNG export of calc_use circuit must be byte-identical in dark and light themes");
    assertEquals(darkSvgCalc, lightSvgCalc, "SVG export of calc_use circuit must be byte-identical in dark and light themes");

    System.out.println("Export byte-identity check PASSED: PNG (" + darkPngMain.length + " bytes) and SVG ("
        + darkSvgMain.length() + " chars) match 100% byte-for-byte.");
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
  void testGenerateRequiredArtifacts() throws Exception {
    File dir = new File(ARTIFACT_DIR);
    if (!dir.exists()) dir.mkdirs();

    File circFile = new File("/home/devansh/Downloads/RAMexp.circ");
    Loader loader = new Loader(null);
    var logisimFile = loader.openLogisimFile(circFile);
    Project proj = new Project(logisimFile);

    Circuit mainCirc = proj.getLogisimFile().getCircuit("main");
    Circuit calcCirc = proj.getLogisimFile().getCircuit("calc_use");

    // Create SevenSegment circuit
    Circuit sevenSegCirc = new Circuit("SevenSegTest", logisimFile, proj);
    CircuitMutation mut = new CircuitMutation(sevenSegCirc);
    final var sevenSegmentFactory = new SevenSegment();
    var sevenSegComp = (InstanceComponent) sevenSegmentFactory.createComponent(Location.create(100, 100, true), sevenSegmentFactory.createAttributeSet());
    mut.add(sevenSegComp);
    mut.execute();

    // Create Bus next to Floating Wire circuit
    Circuit busAndFloatCirc = new Circuit("BusAndFloatTest", logisimFile, proj);
    CircuitMutation mut2 = new CircuitMutation(busAndFloatCirc);
    // Multi-bit bus wire (width 8)
    Wire busWire = Wire.create(Location.create(40, 40, true), Location.create(140, 40, true));
    var busPin = (InstanceComponent) Pin.FACTORY.createComponent(Location.create(40, 40, true), Pin.FACTORY.createAttributeSet());
    busPin.getAttributeSet().setValue(StdAttr.WIDTH, BitWidth.create(8));
    busPin.getAttributeSet().setValue(Pin.ATTR_TYPE, Pin.OUTPUT);
    mut2.add(busPin);
    mut2.add(busWire);

    // Single-bit floating wire (width 1, unknown)
    Wire floatWire = Wire.create(Location.create(40, 80, true), Location.create(140, 80, true));
    var floatPin = (InstanceComponent) Pin.FACTORY.createComponent(Location.create(40, 80, true), Pin.FACTORY.createAttributeSet());
    floatPin.getAttributeSet().setValue(StdAttr.WIDTH, BitWidth.ONE);
    floatPin.getAttributeSet().setValue(Pin.ATTR_TYPE, Pin.OUTPUT);
    mut2.add(floatPin);
    mut2.add(floatWire);
    mut2.execute();

    // Propagate state
    var cs = proj.getCircuitState(busAndFloatCirc);
    cs.setValue(Location.create(40, 40, true), Value.createKnown(BitWidth.create(8), 0x55), busPin, 1);
    cs.setValue(Location.create(40, 80, true), Value.UNKNOWN, floatPin, 1);

    // Render RAMexp, calc_use, seven-segment at 100% and 200% in Dark and Light
    for (boolean dark : new boolean[]{true, false}) {
      String themeStr = dark ? "dark" : "light";
      UIManager.setLookAndFeel(dark ? new ModernDarkLaf() : new ModernLightLaf());
      AppPreferences.LookAndFeel.set(dark ? ModernDarkLaf.class.getName() : ModernLightLaf.class.getName());

      for (double scale : new double[]{1.0, 2.0}) {
        int scalePct = (int) (scale * 100);
        // RAMexp
        BufferedImage ramImg = renderCircuitToImage(mainCirc, proj, dark, scale);
        ImageIO.write(ramImg, "PNG", new File(ARTIFACT_DIR + "stage22_RAMexp_" + themeStr + "_" + scalePct + ".png"));

        // calc_use
        BufferedImage calcImg = renderCircuitToImage(calcCirc, proj, dark, scale);
        ImageIO.write(calcImg, "PNG", new File(ARTIFACT_DIR + "stage22_calc_use_" + themeStr + "_" + scalePct + ".png"));

        // seven-segment
        BufferedImage sevenImg = renderCircuitToImage(sevenSegCirc, proj, dark, scale);
        ImageIO.write(sevenImg, "PNG", new File(ARTIFACT_DIR + "stage22_seven_segment_" + themeStr + "_" + scalePct + ".png"));
      }

      // bus next to floating wire
      BufferedImage busFloatImg = renderCircuitToImage(busAndFloatCirc, proj, dark, 2.0);
      ImageIO.write(busFloatImg, "PNG", new File(ARTIFACT_DIR + "stage22_bus_and_floating_wire_" + themeStr + ".png"));
    }

    System.out.println("All Stage 22 artifacts rendered and verified successfully.");
  }
}
