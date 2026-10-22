package com.cburch.logisim.gui.icons.symbolic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern vector icon for Floating Point Arithmetic library: rounded square with decimal point and exponent tick.
 */
public class CategoryFloatingPointIcon extends SymbolicIcon {

  public CategoryFloatingPointIcon() {
    super(20);
  }

  @Override
  protected void paintSymbolic(Graphics2D g2, int size, Color color) {
    g2.setStroke(createStroke(1.75f));
    float s = size / 20.0f;

    // Outer rounded square
    g2.draw(new RoundRectangle2D.Float(2.5f * s, 2.5f * s, 15.0f * s, 15.0f * s, 4.0f * s, 4.0f * s));

    // Exponent tick in top right
    g2.draw(new Line2D.Float(11.0f * s, 8.0f * s, 14.5f * s, 5.5f * s));
    g2.draw(new Line2D.Float(12.5f * s, 5.5f * s, 14.5f * s, 5.5f * s));

    // Decimal point (filled dot)
    float dotR = 1.6f * s;
    g2.fill(new Ellipse2D.Float(6.5f * s - dotR, 13.0f * s - dotR, 2 * dotR, 2 * dotR));

    // Baseline value dash next to point
    g2.draw(new Line2D.Float(9.5f * s, 13.0f * s, 13.5f * s, 13.0f * s));
  }
}
