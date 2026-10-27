package com.cburch.logisim.gui.icons.symbolic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern monochrome vector Toggle Appearance icon: circuit box with pin stubs and an edit pencil badge.
 */
public class AppearanceIcon extends SymbolicIcon {

  public AppearanceIcon() {
    this(20);
  }

  public AppearanceIcon(int baseSize) {
    super(baseSize);
  }

  @Override
  protected void paintSymbolic(Graphics2D g2, int size, Color color, Component c) {
    float s = size;
    g2.setStroke(createStroke(1.75f));
    g2.setColor(color);

    // Box bounds
    float x = s * 0.22f;
    float y = s * 0.18f;
    float w = s * 0.56f;
    float h = s * 0.64f;
    float arc = s * 0.12f;

    // Chip outline
    g2.draw(new RoundRectangle2D.Float(x, y, w, h, arc, arc));

    // Pin stubs on left and right
    float pinLen = s * 0.12f;
    g2.drawLine((int) (x - pinLen), (int) (y + h * 0.30f), (int) x, (int) (y + h * 0.30f));
    g2.drawLine((int) (x - pinLen), (int) (y + h * 0.70f), (int) x, (int) (y + h * 0.70f));
    g2.drawLine((int) (x + w), (int) (y + h * 0.30f), (int) (x + w + pinLen), (int) (y + h * 0.30f));
    g2.drawLine((int) (x + w), (int) (y + h * 0.70f), (int) (x + w + pinLen), (int) (y + h * 0.70f));

    // Diagonal edit pencil glyph across the chip interior
    Path2D.Float pencil = new Path2D.Float();
    float px1 = x + w * 0.30f;
    float py1 = y + h * 0.70f;
    float px2 = x + w * 0.72f;
    float py2 = y + h * 0.28f;

    // Pencil shaft
    g2.drawLine((int) px1, (int) py1, (int) px2, (int) py2);

    // Small pencil tip and eraser tick
    g2.drawLine((int) (px1 - s * 0.06f), (int) (py1 + s * 0.06f), (int) px1, (int) py1);
    g2.drawLine((int) px2, (int) py2, (int) (px2 + s * 0.05f), (int) (py2 - s * 0.05f));
  }
}
