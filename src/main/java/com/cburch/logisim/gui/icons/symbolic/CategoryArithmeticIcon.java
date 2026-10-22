package com.cburch.logisim.gui.icons.symbolic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern vector icon for Arithmetic library: rounded square containing a plus over a minus.
 */
public class CategoryArithmeticIcon extends SymbolicIcon {

  public CategoryArithmeticIcon() {
    super(20);
  }

  @Override
  protected void paintSymbolic(Graphics2D g2, int size, Color color) {
    g2.setStroke(createStroke(1.75f));
    float s = size / 20.0f;

    // Outer rounded square
    g2.draw(new RoundRectangle2D.Float(2.5f * s, 2.5f * s, 15.0f * s, 15.0f * s, 4.0f * s, 4.0f * s));

    // Plus sign (top half)
    g2.draw(new Line2D.Float(7.5f * s, 7.5f * s, 12.5f * s, 7.5f * s));
    g2.draw(new Line2D.Float(10.0f * s, 5.0f * s, 10.0f * s, 10.0f * s));

    // Minus sign (bottom half)
    g2.draw(new Line2D.Float(7.5f * s, 13.5f * s, 12.5f * s, 13.5f * s));
  }
}
