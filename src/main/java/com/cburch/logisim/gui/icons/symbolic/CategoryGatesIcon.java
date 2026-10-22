package com.cburch.logisim.gui.icons.symbolic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

/**
 * Modern vector icon for Gates library: AND-gate outline with two input leads and one output lead.
 */
public class CategoryGatesIcon extends SymbolicIcon {

  public CategoryGatesIcon() {
    super(20);
  }

  @Override
  protected void paintSymbolic(Graphics2D g2, int size, Color color) {
    g2.setStroke(createStroke(1.75f));
    float s = size / 20.0f;

    // Two input leads
    g2.draw(new Line2D.Float(2.0f * s, 6.5f * s, 5.5f * s, 6.5f * s));
    g2.draw(new Line2D.Float(2.0f * s, 13.5f * s, 5.5f * s, 13.5f * s));

    // AND gate body
    Path2D.Float andGate = new Path2D.Float();
    andGate.moveTo(5.5f * s, 4.0f * s);
    andGate.lineTo(10.0f * s, 4.0f * s);
    andGate.curveTo(13.8f * s, 4.0f * s, 15.5f * s, 7.0f * s, 15.5f * s, 10.0f * s);
    andGate.curveTo(15.5f * s, 13.0f * s, 13.8f * s, 16.0f * s, 10.0f * s, 16.0f * s);
    andGate.lineTo(5.5f * s, 16.0f * s);
    andGate.closePath();
    g2.draw(andGate);

    // One output lead
    g2.draw(new Line2D.Float(15.5f * s, 10.0f * s, 18.5f * s, 10.0f * s));
  }
}
