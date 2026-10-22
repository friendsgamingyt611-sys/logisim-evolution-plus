/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.gui.icons.symbolic;

import com.cburch.logisim.prefs.AppPreferences;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import javax.swing.Icon;
import javax.swing.UIManager;

/**
 * 16px rotating vector chevron icon for tree groups (0° collapsed, 90° expanded).
 */
public class TreeChevronIcon implements Icon {

  private final boolean expanded;
  private Color color;

  public TreeChevronIcon(boolean expanded) {
    this.expanded = expanded;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  @Override
  public int getIconWidth() {
    return AppPreferences.getScaled(16);
  }

  @Override
  public int getIconHeight() {
    return AppPreferences.getScaled(16);
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    final var g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

    int size = AppPreferences.getScaled(16);
    float strokeW = AppPreferences.getScaledStroke(2.0f);
    g2.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

    Color drawCol = (color != null) ? color : UIManager.getColor("Label.disabledForeground");
    if (drawCol == null) {
      drawCol = new Color(154, 160, 168); // #9aa0a8
    }
    g2.setColor(drawCol);

    int cx = x + size / 2;
    int cy = y + size / 2;
    int arm = AppPreferences.getScaled(4);

    final var path = new Path2D.Float();
    if (expanded) {
      // Points down (expanded)
      path.moveTo(cx - arm, cy - arm / 2);
      path.lineTo(cx, cy + arm / 2);
      path.lineTo(cx + arm, cy - arm / 2);
    } else {
      // Points right (collapsed)
      path.moveTo(cx - arm / 2, cy - arm);
      path.lineTo(cx + arm / 2, cy);
      path.lineTo(cx - arm / 2, cy + arm);
    }

    g2.draw(path);
    g2.dispose();
  }
}
