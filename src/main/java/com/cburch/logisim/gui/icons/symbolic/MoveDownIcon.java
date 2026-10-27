package com.cburch.logisim.gui.icons.symbolic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

/**
 * Modern vector Down Arrow icon with bold solid-filled arrowhead and thick stem.
 */
public class MoveDownIcon extends SymbolicIcon {

  private static final Color SEMANTIC_BLUE = new Color(0x35, 0x84, 0xE4);

  public MoveDownIcon() {
    super(16);
  }

  @Override
  public Color getSemanticColor() {
    return SEMANTIC_BLUE;
  }

  @Override
  protected void paintSymbolic(Graphics2D g2, int size, Color color) {
    float s = size;

    Path2D.Float arrow = new Path2D.Float();
    arrow.moveTo(s * 0.36f, s * 0.12f);
    arrow.lineTo(s * 0.64f, s * 0.12f);
    arrow.lineTo(s * 0.64f, s * 0.48f);
    arrow.lineTo(s * 0.85f, s * 0.48f);
    arrow.lineTo(s * 0.50f, s * 0.88f);
    arrow.lineTo(s * 0.15f, s * 0.48f);
    arrow.lineTo(s * 0.36f, s * 0.48f);
    arrow.closePath();

    g2.fill(arrow);
  }
}
