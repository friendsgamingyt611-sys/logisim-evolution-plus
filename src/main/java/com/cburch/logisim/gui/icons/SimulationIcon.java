/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.gui.icons;

import com.cburch.logisim.prefs.AppPreferences;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;

public class SimulationIcon extends BaseIcon {

  public static final int SIM_PLAY = 0;
  public static final int SIM_PAUSE = 1;
  public static final int SIM_STEP = 2;
  public static final int SIM_ENABLE = 3;
  public static final int SIM_DISABLE = 4;
  public static final int SIM_HALF_TICK = 5;
  public static final int SIM_FULL_TICK = 6;

  private static final Color PLAY_GREEN = new Color(51, 209, 122);
  private static final Color PAUSE_RED = new Color(246, 97, 81);
  private static final Color STEP_BLUE = new Color(53, 132, 228);

  private int currentType;

  public SimulationIcon(int type) {
    currentType = type;
  }

  public void setType(int type) {
    currentType = type;
  }

  @Override
  protected void paintIcon(Graphics2D g2) {
    final var w = (float) getIconWidth();
    final var h = (float) getIconHeight();
    final var fg = AppPreferences.getComponentIconColor();

    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

    switch (currentType) {
      case SIM_PLAY -> paintPlay(g2, w, h);
      case SIM_PAUSE -> paintPause(g2, w, h);
      case SIM_STEP -> paintStep(g2, w, h, fg);
      case SIM_ENABLE -> paintClock(g2, w, h, fg, true);
      case SIM_DISABLE -> paintClock(g2, w, h, fg, false);
      case SIM_HALF_TICK -> paintHalfTick(g2, w, h, fg);
      case SIM_FULL_TICK -> paintFullTick(g2, w, h, fg);
      default -> { }
    }
  }

  private void paintPlay(Graphics2D g2, float w, float h) {
    g2.setColor(PLAY_GREEN);
    final var path = new GeneralPath();
    final var x0 = w * 0.26f;
    final var x1 = w * 0.78f;
    final var y0 = h * 0.20f;
    final var y1 = h * 0.50f;
    final var y2 = h * 0.80f;
    path.moveTo(x0, y0);
    path.lineTo(x1, y1);
    path.lineTo(x0, y2);
    path.closePath();
    g2.fill(path);
  }

  private void paintPause(Graphics2D g2, float w, float h) {
    g2.setColor(PAUSE_RED);
    final var barW = w * 0.18f;
    final var barH = h * 0.60f;
    final var y = h * 0.20f;
    final var arc = Math.max(2f, barW * 0.4f);
    g2.fill(new RoundRectangle2D.Float(w * 0.26f, y, barW, barH, arc, arc));
    g2.fill(new RoundRectangle2D.Float(w * 0.56f, y, barW, barH, arc, arc));
  }

  private void paintStep(Graphics2D g2, float w, float h, Color fg) {
    g2.setColor(fg);
    final var path = new GeneralPath();
    final var x0 = w * 0.18f;
    final var x1 = w * 0.60f;
    final var y0 = h * 0.22f;
    final var y1 = h * 0.50f;
    final var y2 = h * 0.78f;
    path.moveTo(x0, y0);
    path.lineTo(x1, y1);
    path.lineTo(x0, y2);
    path.closePath();
    g2.fill(path);

    final var barW = Math.max(2f, w * 0.12f);
    final var barH = h * 0.56f;
    final var barX = w * 0.70f;
    final var barY = h * 0.22f;
    g2.fill(new RoundRectangle2D.Float(barX, barY, barW, barH, 2f, 2f));
  }

  private void paintClock(Graphics2D g2, float w, float h, Color fg, boolean enableAction) {
    g2.setColor(fg);
    final var strokeW = Math.max(1.5f, scale(1.5f));
    g2.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

    final var cx = w * 0.46f;
    final var cy = h * 0.48f;
    final var r = Math.min(w, h) * 0.36f;
    g2.drawOval((int) (cx - r), (int) (cy - r), (int) (2 * r), (int) (2 * r));

    g2.drawLine((int) cx, (int) cy, (int) cx, (int) (cy - r * 0.65f));
    g2.drawLine((int) cx, (int) cy, (int) (cx + r * 0.60f), (int) cy);

    if (enableAction) {
      g2.setColor(PLAY_GREEN);
      final var badge = new GeneralPath();
      final var bx0 = w * 0.62f;
      final var bx1 = w * 0.94f;
      final var by0 = h * 0.58f;
      final var by1 = h * 0.76f;
      final var by2 = h * 0.94f;
      badge.moveTo(bx0, by0);
      badge.lineTo(bx1, by1);
      badge.lineTo(bx0, by2);
      badge.closePath();
      g2.fill(badge);
    } else {
      g2.setColor(PAUSE_RED);
      final var bw = Math.max(2f, w * 0.10f);
      final var bh = h * 0.36f;
      final var by = h * 0.58f;
      g2.fill(new RoundRectangle2D.Float(w * 0.66f, by, bw, bh, 1f, 1f));
      g2.fill(new RoundRectangle2D.Float(w * 0.82f, by, bw, bh, 1f, 1f));
    }
  }

  private void paintHalfTick(Graphics2D g2, float w, float h, Color fg) {
    final var strokeW = Math.max(1.8f, scale(1.8f));
    g2.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.setColor(fg);

    final var yLow = h * 0.72f;
    final var yHigh = h * 0.28f;
    final var x0 = w * 0.14f;
    final var xMid = w * 0.50f;
    final var x1 = w * 0.86f;

    final var wave = new GeneralPath();
    wave.moveTo(x0, yLow);
    wave.lineTo(xMid, yLow);
    wave.lineTo(xMid, yHigh);
    wave.lineTo(x1, yHigh);
    g2.draw(wave);

    g2.setColor(STEP_BLUE);
    final var arrow = new GeneralPath();
    final var aw = w * 0.14f;
    final var ah = h * 0.16f;
    arrow.moveTo(xMid - aw, yHigh + ah);
    arrow.lineTo(xMid, yHigh);
    arrow.lineTo(xMid + aw, yHigh + ah);
    g2.draw(arrow);
  }

  private void paintFullTick(Graphics2D g2, float w, float h, Color fg) {
    final var strokeW = Math.max(1.8f, scale(1.8f));
    g2.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.setColor(fg);

    final var yLow = h * 0.72f;
    final var yHigh = h * 0.28f;
    final var x0 = w * 0.10f;
    final var xUp = w * 0.32f;
    final var xDown = w * 0.68f;
    final var x1 = w * 0.90f;

    final var pulse = new GeneralPath();
    pulse.moveTo(x0, yLow);
    pulse.lineTo(xUp, yLow);
    pulse.lineTo(xUp, yHigh);
    pulse.lineTo(xDown, yHigh);
    pulse.lineTo(xDown, yLow);
    pulse.lineTo(x1, yLow);
    g2.draw(pulse);
  }
}
