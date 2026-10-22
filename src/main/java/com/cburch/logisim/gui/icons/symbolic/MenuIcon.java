package com.cburch.logisim.gui.icons.symbolic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern vector Hamburger Menu icon (3 rounded horizontal bars).
 */
public class MenuIcon extends SymbolicIcon {

  public MenuIcon() {
    super();
  }

  @Override
  protected void paintSymbolic(Graphics2D g2, int size, Color color) {
    float s = size;
    float left = s * 0.20f;
    float w = s * 0.60f;
    float barH = s * 0.12f;
    float arc = barH;

    g2.fill(new RoundRectangle2D.Float(left, s * 0.24f, w, barH, arc, arc));
    g2.fill(new RoundRectangle2D.Float(left, s * 0.44f, w, barH, arc, arc));
    g2.fill(new RoundRectangle2D.Float(left, s * 0.64f, w, barH, arc, arc));
  }
}
