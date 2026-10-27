package com.cburch.logisim.gui.icons.symbolic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

/**
 * Modern monochrome stroked Down Arrow icon (shaft with chevron head, round caps).
 */
public class MoveDownIcon extends SymbolicIcon {

  public MoveDownIcon() {
    this(20);
  }

  public MoveDownIcon(int baseSize) {
    super(baseSize);
  }

  @Override
  protected void paintSymbolic(Graphics2D g2, int size, Color color, Component c) {
    float s = size;
    g2.setStroke(createStroke(1.85f));
    g2.setColor(color);

    float midX = s * 0.50f;
    float topY = s * 0.22f;
    float botY = s * 0.78f;
    float spreadX = s * 0.28f;
    float chevY = s * 0.52f;

    // Chevron head pointing down
    Path2D.Float chevron = new Path2D.Float();
    chevron.moveTo(midX - spreadX, chevY);
    chevron.lineTo(midX, botY);
    chevron.lineTo(midX + spreadX, chevY);
    g2.draw(chevron);

    // Arrow shaft
    g2.draw(new Line2D.Float(midX, topY, midX, botY));
  }
}
