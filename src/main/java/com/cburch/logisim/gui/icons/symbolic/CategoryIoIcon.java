package com.cburch.logisim.gui.icons.symbolic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern vector icon for Input/Output library: toggle switch pill with knob, and LED dot.
 */
public class CategoryIoIcon extends SymbolicIcon {

  public CategoryIoIcon() {
    super(20);
  }

  @Override
  protected void paintSymbolic(Graphics2D g2, int size, Color color) {
    g2.setStroke(createStroke(1.75f));
    float s = size / 20.0f;

    // Toggle switch pill
    float px = 2.5f * s;
    float py = 5.5f * s;
    float pw = 11.0f * s;
    float ph = 9.0f * s;
    g2.draw(new RoundRectangle2D.Float(px, py, pw, ph, ph, ph));

    // Toggle knob circle inside pill
    float knobR = 2.6f * s;
    float kx = px + ph / 2.0f;
    float ky = py + ph / 2.0f;
    g2.draw(new Ellipse2D.Float(kx - knobR, ky - knobR, 2 * knobR, 2 * knobR));

    // LED status indicator dot
    float ledR = 1.8f * s;
    float lx = 16.5f * s;
    float ly = 10.0f * s;
    g2.fill(new Ellipse2D.Float(lx - ledR, ly - ledR, 2 * ledR, 2 * ledR));
  }
}
