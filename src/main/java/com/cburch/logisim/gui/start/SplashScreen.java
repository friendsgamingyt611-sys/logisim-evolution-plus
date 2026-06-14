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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
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

    // Use a clean gradient-style panel
    final var imagePanel = new SplashPanel();
    imagePanel.setPreferredSize(new Dimension(480, 280));

    progress.setStringPainted(true);
    progress.setForeground(new Color(0x4a, 0x90, 0xd9));
    progress.setBackground(new Color(0xe0, 0xe0, 0xe0));
    progress.setBorder(null);
    progress.setFont(new Font("SansSerif", Font.PLAIN, 12));
    progress.setString("");

    final var contents = new JPanel(new BorderLayout());
    contents.add(imagePanel, BorderLayout.CENTER);
    contents.add(progress, BorderLayout.SOUTH);
    contents.setBorder(BorderFactory.createLineBorder(new Color(0x4a, 0x90, 0xd9), 2));

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
   * Draws a clean centered splash panel with app name, version, and loading indicator.
   */
  private static class SplashPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    SplashPanel() {
      setBackground(new Color(0xf5, 0xf5, 0xf5));
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      final var g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      final var w = getWidth();
      final var h = getHeight();

      // App name - large centered
      g2.setColor(new Color(0x2c, 0x2c, 0x2c));
      g2.setFont(new Font("SansSerif", Font.BOLD, 28));
      final var nameFm = g2.getFontMetrics();
      final var nameStr = BuildInfo.name;
      g2.drawString(nameStr, (w - nameFm.stringWidth(nameStr)) / 2, h / 2 - 30);

      // Version - smaller below
      g2.setColor(new Color(0x66, 0x66, 0x66));
      g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
      final var verStr = "v" + BuildInfo.version;
      final var verFm = g2.getFontMetrics();
      g2.drawString(verStr, (w - verFm.stringWidth(verStr)) / 2, h / 2);

      // Loading text - subtle at bottom
      g2.setColor(new Color(0x99, 0x99, 0x99));
      g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
      final var loadStr = S.get("progressStarting");
      final var loadFm = g2.getFontMetrics();
      g2.drawString(loadStr, (w - loadFm.stringWidth(loadStr)) / 2, h - 20);

      // Small accent line
      g2.setColor(new Color(0x4a, 0x90, 0xd9));
      g2.fillRect(w / 4, h - 10, w / 2, 3);
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
