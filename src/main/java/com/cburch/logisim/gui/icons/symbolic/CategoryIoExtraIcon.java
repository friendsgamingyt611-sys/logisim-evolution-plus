package com.cburch.logisim.gui.icons.symbolic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

/**
 * Modern vector icon for I/O Extra library: 3x3 dot matrix.
 */
public class CategoryIoExtraIcon extends SymbolicIcon {

  public CategoryIoExtraIcon() {
    super(20);
  }

  @Override
  protected void paintSymbolic(Graphics2D g2, int size, Color color) {
    float s = size / 20.0f;
    float dotR = 1.4f * s;

    float[] coords = {5.0f, 10.0f, 15.0f};
    for (float x : coords) {
      for (float y : coords) {
        g2.fill(new Ellipse2D.Float(x * s - dotR, y * s - dotR, 2 * dotR, 2 * dotR));
      }
    }
  }
}
