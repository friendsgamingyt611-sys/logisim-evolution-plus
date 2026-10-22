package com.cburch.logisim.gui.icons.symbolic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern vector icon for Memory library: rounded rectangle split into a 2x3 cell grid.
 */
public class CategoryMemoryIcon extends SymbolicIcon {

  public CategoryMemoryIcon() {
    super(20);
  }

  @Override
  protected void paintSymbolic(Graphics2D g2, int size, Color color) {
    g2.setStroke(createStroke(1.75f));
    float s = size / 20.0f;

    // Outer rounded rectangle
    float x = 3.0f * s;
    float y = 3.5f * s;
    float w = 14.0f * s;
    float h = 13.0f * s;
    float arc = 4.0f * s;
    g2.draw(new RoundRectangle2D.Float(x, y, w, h, arc, arc));

    // Vertical divider (2 columns)
    float midX = x + w / 2.0f;
    g2.draw(new Line2D.Float(midX, y, midX, y + h));

    // Horizontal dividers (3 rows)
    float rowH = h / 3.0f;
    g2.draw(new Line2D.Float(x, y + rowH, x + w, y + rowH));
    g2.draw(new Line2D.Float(x, y + 2.0f * rowH, x + w, y + 2.0f * rowH));
  }
}
