package com.cburch.logisim.gui.icons.symbolic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

/**
 * Modern vector icon for Wiring library: horizontal wire with branch and filled junction dot.
 */
public class CategoryWiringIcon extends SymbolicIcon {

  public CategoryWiringIcon() {
    super(20);
  }

  @Override
  protected void paintSymbolic(Graphics2D g2, int size, Color color) {
    g2.setStroke(createStroke(1.75f));
    float s = size / 20.0f;

    // Horizontal main wire
    g2.draw(new Line2D.Float(2.5f * s, 10.0f * s, 17.5f * s, 10.0f * s));
    // Branch wire going up
    g2.draw(new Line2D.Float(10.0f * s, 10.0f * s, 10.0f * s, 2.5f * s));

    // Filled junction dot at (10, 10)
    float dotR = 2.4f * s;
    g2.fill(new Ellipse2D.Float(10.0f * s - dotR, 10.0f * s - dotR, 2 * dotR, 2 * dotR));
  }
}
