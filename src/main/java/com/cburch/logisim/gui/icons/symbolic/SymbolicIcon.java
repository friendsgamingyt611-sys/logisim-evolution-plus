package com.cburch.logisim.gui.icons.symbolic;

import com.cburch.logisim.prefs.AppPreferences;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;
import javax.swing.UIManager;

/**
 * Base class for modern symbolic vector icons.
 * Automatically adapts to light/dark themes and HiDPI display scaling.
 */
public abstract class SymbolicIcon implements Icon {

  private final int baseSize;
  private Color fixedColor = null;

  public SymbolicIcon() {
    this(AppPreferences.IconSize);
  }

  public SymbolicIcon(int baseSize) {
    this.baseSize = baseSize;
  }

  public void setColor(Color color) {
    this.fixedColor = color;
  }

  @Override
  public int getIconWidth() {
    return AppPreferences.getScaled(baseSize);
  }

  @Override
  public int getIconHeight() {
    return AppPreferences.getScaled(baseSize);
  }

  public Color getSemanticColor() {
    return null;
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    final var g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

    final var isEnabled = (c == null || c.isEnabled());
    Color fg;
    if (fixedColor != null) {
      fg = fixedColor;
    } else {
      final var semantic = getSemanticColor();
      if (!isEnabled) {
        if (semantic != null) {
          fg = new Color(semantic.getRed(), semantic.getGreen(), semantic.getBlue(), 90);
        } else {
          Color disabled = UIManager.getColor("Button.disabledText");
          fg = (disabled != null) ? disabled : new Color(128, 128, 128, 128);
        }
      } else if (semantic != null) {
        fg = semantic;
      } else if (c != null && c.getForeground() != null) {
        fg = c.getForeground();
      } else {
        Color defaultFg = UIManager.getColor("Button.foreground");
        if (defaultFg == null) defaultFg = UIManager.getColor("Label.foreground");
        fg = (defaultFg != null) ? defaultFg : (AppPreferences.isDarkTheme() ? new Color(235, 235, 235) : new Color(30, 30, 30));
      }
    }

    g2.setColor(fg);
    g2.translate(x, y);

    int size = getIconWidth();
    paintSymbolic(g2, size, fg);
    g2.dispose();
  }

  protected abstract void paintSymbolic(Graphics2D g2, int size, Color color);

  protected float scale(float val) {
    return AppPreferences.getScaled(val);
  }

  protected BasicStroke createStroke(float width) {
    return new BasicStroke(
        AppPreferences.getScaledStroke(width), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
  }
}
