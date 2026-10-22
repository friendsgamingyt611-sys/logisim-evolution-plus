package com.cburch.logisim.gui.icons.symbolic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern vector icon for SoC (System on a Chip) library: square chip with small core.
 */
public class CategorySocIcon extends SymbolicIcon {

  public CategorySocIcon() {
    super(20);
  }

  @Override
  protected void paintSymbolic(Graphics2D g2, int size, Color color) {
    g2.setStroke(createStroke(1.75f));
    float s = size / 20.0f;

    // Square QFP / BGA chip body
    float cx = 4.5f * s;
    float cy = 4.5f * s;
    float cw = 11.0f * s;
    g2.draw(new RoundRectangle2D.Float(cx, cy, cw, cw, 2.5f * s, 2.5f * s));

    // Inner square core
    float coreX = 7.5f * s;
    float coreY = 7.5f * s;
    float coreW = 5.0f * s;
    g2.draw(new RoundRectangle2D.Float(coreX, coreY, coreW, coreW, 1.0f * s, 1.0f * s));

    // Leads on all 4 sides
    g2.draw(new Line2D.Float(1.5f * s, 10.0f * s, cx, 10.0f * s));
    g2.draw(new Line2D.Float(cx + cw, 10.0f * s, 18.5f * s, 10.0f * s));
    g2.draw(new Line2D.Float(10.0f * s, 1.5f * s, 10.0f * s, cy));
    g2.draw(new Line2D.Float(10.0f * s, cy + cw, 10.0f * s, 18.5f * s));
  }
}
