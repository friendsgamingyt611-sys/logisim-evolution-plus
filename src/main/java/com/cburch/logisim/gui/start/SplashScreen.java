/*
 * Logisim-evolution+ - digital logic design tool and simulator
 * Copyright by the Logisim-evolution+ developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.gui.start;

import static com.cburch.logisim.gui.Strings.S;

import com.cburch.logisim.generated.BuildInfo;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SplashScreen extends JWindow {

  public static final int LIBRARIES = 0;
  public static final int TEMPLATE_CREATE = 1;
  public static final int TEMPLATE_OPEN = 2;
  public static final int TEMPLATE_LOAD = 3;
  public static final int TEMPLATE_CLOSE = 4;
  public static final int GUI_INIT = 5;
  public static final int FILE_CREATE = 6;
  public static final int FILE_LOAD = 7;
  public static final int PROJECT_CREATE = 8;
  public static final int FRAME_CREATE = 9;
  static final Logger logger = LoggerFactory.getLogger(SplashScreen.class);
  private static final long serialVersionUID = 1L;
  private static final int PROGRESS_MAX = 3568;
  private static final boolean PRINT_TIMES = false;
  Marker[] markers =
      new Marker[] {
        new Marker(377, S.get("progressLibraries")),
        new Marker(990, S.get("progressTemplateCreate")),
        new Marker(1002, S.get("progressTemplateOpen")),
        new Marker(1002, S.get("progressTemplateLoad")),
        new Marker(1470, S.get("progressTemplateClose")),
        new Marker(1478, S.get("progressGuiInitialize")),
        new Marker(2114, S.get("progressFileCreate")),
        new Marker(2114, S.get("progressFileLoad")),
        new Marker(2383, S.get("progressProjectCreate")),
        new Marker(2519, S.get("progressFrameCreate")),
      };
  boolean inClose = false;
  final JProgressBar progress = new JProgressBar(0, PROGRESS_MAX);
  final long startTime = System.currentTimeMillis();

  public SplashScreen() {
    setName(BuildInfo.displayName);

    final var imagePanel = new SplashPanel();
    imagePanel.setPreferredSize(new Dimension(520, 300));

    progress.setStringPainted(true);
    progress.setForeground(new Color(0x4a, 0xd4, 0xff));
    progress.setBackground(new Color(0x1a, 0x2a, 0x3a));
    progress.setBorder(null);
    progress.setFont(new Font("SansSerif", Font.PLAIN, 11));
    progress.setString("");

    final var contents = new JPanel(new BorderLayout());
    contents.add(imagePanel, BorderLayout.CENTER);
    contents.add(progress, BorderLayout.SOUTH);
    contents.setBorder(BorderFactory.createLineBorder(new Color(0x4a, 0xd4, 0xff, 180), 2));

    setContentPane(contents);
    pack();
  }

  public void close() {
    if (inClose) return;
    inClose = true;
    setVisible(false);
    inClose = false;
    if (PRINT_TIMES) {
      logger.info("{} closed", System.currentTimeMillis() - startTime);
    }
    markers = null;
  }

  public void setProgress(int markerId) {
    final var marker = markers == null ? null : markers[markerId];
    if (marker != null) {
      SwingUtilities.invokeLater(
          () -> {
            progress.setString(marker.message);
            progress.setValue(marker.count);
          });
      if (PRINT_TIMES) {
        logger.info("{} {}", System.currentTimeMillis() - startTime, marker.message);
      }
    } else {
      if (PRINT_TIMES) {
        logger.info("{} ??", System.currentTimeMillis() - startTime);
      }
    }
  }

  @Override
  public void setVisible(boolean value) {
    if (value) {
      pack();
      final var dim = getToolkit().getScreenSize();
      final var x = (int) (dim.getWidth() - getWidth()) / 2;
      final var y = (int) (dim.getHeight() - getHeight()) / 2;
      setLocation(x, y);
    }
    super.setVisible(value);
  }

  /**
   * Draws a premium dark-themed splash panel with circuit board aesthetic.
   */
  private static class SplashPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    // Accent color
    private static final Color ACCENT = new Color(0x4a, 0xd4, 0xff);
    private static final Color BG_TOP = new Color(0x0d, 0x1b, 0x2a);
    private static final Color BG_BOT = new Color(0x1a, 0x2e, 0x44);

    SplashPanel() {
      setBackground(BG_TOP);
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      final var g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

      final var w = getWidth();
      final var h = getHeight();

      // --- Background gradient ---
      final var grad = new GradientPaint(0, 0, BG_TOP, 0, h, BG_BOT);
      g2.setPaint(grad);
      g2.fillRect(0, 0, w, h);

      // --- Subtle grid lines (circuit board feel) ---
      g2.setColor(new Color(0xff, 0xff, 0xff, 12));
      g2.setStroke(new BasicStroke(0.5f));
      for (int x = 0; x < w; x += 20) g2.drawLine(x, 0, x, h);
      for (int y = 0; y < h; y += 20) g2.drawLine(0, y, w, y);

      // --- Corner accent brackets ---
      g2.setColor(ACCENT);
      g2.setStroke(new BasicStroke(2f));
      final int cs = 18; // corner size
      final int cm = 10; // corner margin
      // top-left
      g2.drawLine(cm, cm, cm + cs, cm);
      g2.drawLine(cm, cm, cm, cm + cs);
      // top-right
      g2.drawLine(w - cm - cs, cm, w - cm, cm);
      g2.drawLine(w - cm, cm, w - cm, cm + cs);
      // bottom-left
      g2.drawLine(cm, h - cm, cm + cs, h - cm);
      g2.drawLine(cm, h - cm - cs, cm, h - cm);
      // bottom-right
      g2.drawLine(w - cm - cs, h - cm, w - cm, h - cm);
      g2.drawLine(w - cm, h - cm - cs, w - cm, h - cm);

      // --- Decorative circuit nodes (small dots) ---
      g2.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 60));
      final int[] nodeX = {50, 80, w - 60, w - 90, 50, w - 70};
      final int[] nodeY = {50, 80, 50, 80, h - 60, h - 60};
      for (int i = 0; i < nodeX.length; i++) {
        g2.fillOval(nodeX[i] - 4, nodeY[i] - 4, 8, 8);
        g2.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 30));
        g2.fillOval(nodeX[i] - 8, nodeY[i] - 8, 16, 16);
        g2.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 60));
      }

      // --- App name ---
      g2.setColor(Color.WHITE);
      g2.setFont(new Font("SansSerif", Font.BOLD, 32));
      FontMetrics nameFm = g2.getFontMetrics();
      final var nameStr = BuildInfo.name;
      final int nameX = (w - nameFm.stringWidth(nameStr)) / 2;
      final int nameY = h / 2 - 28;
      // Glow effect
      g2.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 40));
      g2.setFont(new Font("SansSerif", Font.BOLD, 34));
      g2.drawString(nameStr, nameX - 1, nameY + 1);
      g2.setFont(new Font("SansSerif", Font.BOLD, 32));
      g2.setColor(Color.WHITE);
      g2.drawString(nameStr, nameX, nameY);

      // --- Accent underline below title ---
      g2.setColor(ACCENT);
      g2.setStroke(new BasicStroke(2f));
      final int ulW = nameFm.stringWidth(nameStr) + 40;
      g2.drawLine((w - ulW) / 2, nameY + 8, (w + ulW) / 2, nameY + 8);

      // --- Version ---
      g2.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 220));
      g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
      final var verStr = "v" + BuildInfo.version;
      final var verFm = g2.getFontMetrics();
      g2.drawString(verStr, (w - verFm.stringWidth(verStr)) / 2, nameY + 34);

      // --- Tagline ---
      g2.setColor(new Color(0xbb, 0xcc, 0xdd, 180));
      g2.setFont(new Font("SansSerif", Font.ITALIC, 12));
      final var tagStr = "Digital Logic Design & Simulation";
      final var tagFm = g2.getFontMetrics();
      g2.drawString(tagStr, (w - tagFm.stringWidth(tagStr)) / 2, nameY + 58);

      // --- "Starting..." text at bottom ---
      g2.setColor(new Color(0x88, 0x99, 0xaa, 200));
      g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
      final var loadStr = S.get("progressStarting");
      final var loadFm = g2.getFontMetrics();
      g2.drawString(loadStr, (w - loadFm.stringWidth(loadStr)) / 2, h - 14);
    }
  }

  private static class Marker {
    final int count;
    final String message;

    Marker(int count, String message) {
      this.count = count;
      this.message = message;
    }
  }
}
