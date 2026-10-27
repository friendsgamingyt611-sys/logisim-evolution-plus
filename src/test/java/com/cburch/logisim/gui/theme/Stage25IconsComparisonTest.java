package com.cburch.logisim.gui.theme;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cburch.logisim.gui.icons.AppearEditIcon;
import com.cburch.logisim.gui.icons.CircuitIcon;
import com.cburch.logisim.gui.icons.FatArrowIcon;
import com.cburch.logisim.gui.icons.ProjectAddIcon;
import com.cburch.logisim.gui.icons.symbolic.AddCircuitIcon;
import com.cburch.logisim.gui.icons.symbolic.AddVhdlIcon;
import com.cburch.logisim.gui.icons.symbolic.AppearanceIcon;
import com.cburch.logisim.gui.icons.symbolic.DeleteIcon;
import com.cburch.logisim.gui.icons.symbolic.MoveDownIcon;
import com.cburch.logisim.gui.icons.symbolic.MoveUpIcon;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.UIManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class Stage25IconsComparisonTest {

  private static final String ARTIFACTS_DIR =
      "/home/devansh/.gemini/antigravity-ide/brain/904c724c-98b1-4fc8-adc9-6305dcb0eb9c/";

  @BeforeAll
  static void setupThemes() {
    new File(ARTIFACTS_DIR).mkdirs();
  }

  @Test
  void testRenderVhdlVariants() throws Exception {
    int w = 600;
    int h = 340;
    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = img.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    // Background
    g.setColor(new Color(0x27, 0x29, 0x2D)); // dark panel
    g.fillRect(0, 0, w, h);

    g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
    g.setColor(Color.WHITE);
    g.drawString("Trademark Icons: Classic Roots with Modern Gradient Colors", 20, 30);

    // Light card
    drawVhdlCard(g, 20, 50, 260, 260, false, "Light Mode Surface (#E6E9EE)");
    // Dark card
    drawVhdlCard(g, 310, 50, 260, 260, true, "Dark Mode Surface (#303338)");

    g.dispose();
    File out = new File(ARTIFACTS_DIR, "stage25_vhdl_variants_comparison.png");
    ImageIO.write(img, "PNG", out);
    assertTrue(out.exists());
    System.out.println("Saved VHDL comparison to: " + out.getAbsolutePath());
  }

  private void drawVhdlCard(Graphics2D g, int x, int y, int cardW, int cardH, boolean isDark, String title) {
    Color surface = isDark ? new Color(0x30, 0x33, 0x38) : new Color(0xE6, 0xE9, 0xEE);
    Color fg = isDark ? Color.WHITE : new Color(0x20, 0x20, 0x20);

    g.setColor(surface);
    g.fillRoundRect(x, y, cardW, cardH, 12, 12);
    g.setColor(isDark ? new Color(0x40, 0x43, 0x48) : new Color(0xCC, 0xD0, 0xD8));
    g.drawRoundRect(x, y, cardW, cardH, 12, 12);

    g.setColor(fg);
    g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
    g.drawString(title, x + 15, y + 25);

    // Setup LaF tokens in UIManager temporarily
    if (isDark) {
      ModernDarkLaf.setup();
    } else {
      ModernLightLaf.setup();
    }

    JButton dummyBtn = new JButton();
    dummyBtn.setForeground(isDark ? new Color(235, 235, 235) : new Color(38, 40, 44));
    dummyBtn.setBackground(surface);

    // 100% Scale (20px)
    g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
    g.drawString("100% (20px)", x + 15, y + 60);

    g.drawString("Add Circuit:", x + 15, y + 90);
    AddCircuitIcon c100 = new AddCircuitIcon(20);
    c100.paintIcon(dummyBtn, g, x + 95, y + 74);

    g.drawString("Add VHDL:", x + 135, y + 90);
    AddVhdlIcon v100 = new AddVhdlIcon(20);
    v100.paintIcon(dummyBtn, g, x + 205, y + 74);

    // 200% Scale (40px)
    g.drawString("200% (40px)", x + 15, y + 140);

    g.drawString("Add Circuit:", x + 15, y + 185);
    AddCircuitIcon c200 = new AddCircuitIcon(40);
    c200.paintIcon(dummyBtn, g, x + 85, y + 160);

    g.drawString("Add VHDL:", x + 135, y + 185);
    AddVhdlIcon v200 = new AddVhdlIcon(40);
    v200.paintIcon(dummyBtn, g, x + 200, y + 160);
  }

  @Test
  void testRenderActionStripComparisonMatrix() throws Exception {
    int w = 920;
    int h = 720;
    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = img.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    // Canvas background
    g.setColor(new Color(0x1B, 0x1C, 0x1E));
    g.fillRect(0, 0, w, h);

    g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
    g.setColor(Color.WHITE);
    g.drawString("Stage 25: Action Strip Two-Tone Vector Icons vs Old Icons", 25, 35);
    g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
    g.setColor(new Color(0x9A, 0x99, 0x96));
    g.drawString("Semantic Roles: Create (#1E8E4E / #4CD080) | Move (#1C71D8 / #62A0EA) | Edit (#C25A00 / #F0A030) | Destroy (#C01C28 / #F66151)", 25, 58);

    // Render Light Theme Matrix Row
    renderThemeBlock(g, 25, 75, 870, 290, false);

    // Render Dark Theme Matrix Row
    renderThemeBlock(g, 25, 385, 870, 290, true);

    g.dispose();
    File out = new File(ARTIFACTS_DIR, "stage25_action_strip_side_by_side_matrix.png");
    ImageIO.write(img, "PNG", out);
    assertTrue(out.exists());
    System.out.println("Saved Action Strip comparison matrix to: " + out.getAbsolutePath());
  }

  private void renderThemeBlock(Graphics2D g, int x, int y, int bw, int bh, boolean isDark) {
    Color surface = isDark ? new Color(0x30, 0x33, 0x38) : new Color(0xE6, 0xE9, 0xEE);
    Color cardBg = isDark ? new Color(0x27, 0x29, 0x2D) : new Color(0xF3, 0xF5, 0xF8);
    Color textFg = isDark ? Color.WHITE : new Color(0x20, 0x20, 0x20);

    g.setColor(cardBg);
    g.fillRoundRect(x, y, bw, bh, 10, 10);
    g.setColor(isDark ? new Color(0x3B, 0x3E, 0x45) : new Color(0xBC, 0xC3, 0xCD));
    g.drawRoundRect(x, y, bw, bh, 10, 10);

    g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
    g.setColor(textFg);
    g.drawString(isDark ? "DARK THEME (#303338 Chrome / #27292D Panel)" : "LIGHT THEME (#E6E9EE Chrome / #F3F5F8 Panel)", x + 20, y + 30);

    if (isDark) {
      ModernDarkLaf.setup();
    } else {
      ModernLightLaf.setup();
    }

    int curY = y + 50;

    // Row 1: Old Icons (Classic Logisim)
    renderStripRow(g, x + 20, curY, isDark, "Old Classic Icons (100%):", false, true, 20);

    // Row 2: New Vector Icons (100% Enabled)
    curY += 55;
    renderStripRow(g, x + 20, curY, isDark, "New Two-Tone Vectors (100% Enabled):", true, false, 20);

    // Row 3: New Vector Icons (100% Disabled - 40% & Desaturated)
    curY += 55;
    renderStripRow(g, x + 20, curY, isDark, "New Two-Tone Vectors (100% Disabled):", false, false, 20);

    // Row 4: New Vector Icons (200% Scale HiDPI)
    curY += 55;
    renderStripRow(g, x + 20, curY, isDark, "New Two-Tone Vectors (200% HiDPI):", true, false, 40);
  }

  private void renderStripRow(Graphics2D g, int x, int y, boolean isDark, String label, boolean enabled, boolean oldIcons, int size) {
    Color surface = isDark ? new Color(0x30, 0x33, 0x38) : new Color(0xE6, 0xE9, 0xEE);
    Color groupBg = isDark ? new Color(0x3B, 0x3E, 0x45) : new Color(0xF8, 0xF9, 0xFB);
    Color labelFg = isDark ? new Color(0xDD, 0xDD, 0xDD) : new Color(0x33, 0x33, 0x33);

    g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
    g.setColor(labelFg);
    g.drawString(label, x, y + (size == 40 ? 26 : 16));

    int stripX = x + 270;
    int stripH = size + 10;
    int stripW = 6 * (size + 14) + 60;

    // Draw group strip track
    g.setColor(surface);
    g.fillRoundRect(stripX - 6, y - 2, stripW, stripH, 6, 6);
    g.setColor(isDark ? new Color(0x22, 0x24, 0x28) : new Color(0xBC, 0xC3, 0xCD));
    g.drawRoundRect(stripX - 6, y - 2, stripW, stripH, 6, 6);

    JButton dummyBtn = new JButton();
    dummyBtn.setEnabled(enabled);
    dummyBtn.setBackground(surface);
    dummyBtn.setForeground(isDark ? new Color(235, 235, 235) : new Color(38, 40, 44));

    int curX = stripX;

    if (oldIcons) {
      // Old icons
      ProjectAddIcon oldAdd = new ProjectAddIcon(false); // classic green plus
      oldAdd.paintIcon(dummyBtn, g, curX, y + 4);
      curX += size + 14;

      ProjectAddIcon oldVhdl = new ProjectAddIcon(); // classic stacked VH/DL
      oldVhdl.paintIcon(dummyBtn, g, curX, y + 4);
      curX += size + 14;

      FatArrowIcon oldUp = new FatArrowIcon(com.cburch.logisim.data.Direction.NORTH);
      oldUp.paintIcon(dummyBtn, g, curX, y + 4);
      curX += size + 14;

      FatArrowIcon oldDown = new FatArrowIcon(com.cburch.logisim.data.Direction.SOUTH);
      oldDown.paintIcon(dummyBtn, g, curX, y + 4);
      curX += size + 14;

      AppearEditIcon oldApp = new AppearEditIcon();
      oldApp.paintIcon(dummyBtn, g, curX, y + 4);
      curX += size + 14;

      ProjectAddIcon oldDel = new ProjectAddIcon(true);
      oldDel.paintIcon(dummyBtn, g, curX, y + 4);
    } else {
      // New vector icons
      AddCircuitIcon addCirc = new AddCircuitIcon(size);
      addCirc.paintIcon(dummyBtn, g, curX, y + 4);
      curX += size + 14;

      AddVhdlIcon addVhdl = new AddVhdlIcon(size);
      addVhdl.paintIcon(dummyBtn, g, curX, y + 4);
      curX += size + 14;

      MoveUpIcon moveUp = new MoveUpIcon(size);
      moveUp.paintIcon(dummyBtn, g, curX, y + 4);
      curX += size + 14;

      MoveDownIcon moveDown = new MoveDownIcon(size);
      moveDown.paintIcon(dummyBtn, g, curX, y + 4);
      curX += size + 14;

      AppearanceIcon app = new AppearanceIcon(size);
      app.paintIcon(dummyBtn, g, curX, y + 4);
      curX += size + 14;

      DeleteIcon del = new DeleteIcon(size);
      del.paintIcon(dummyBtn, g, curX, y + 4);
    }
  }
}
