/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.draw.toolbar;

import com.cburch.contracts.BaseMouseListenerContract;
import com.cburch.logisim.prefs.AppPreferences;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.UIManager;

class ToolbarButton extends JComponent implements BaseMouseListenerContract {
  private static final long serialVersionUID = 1L;

  private static final int MIN_SIZE = 34;

  private final Toolbar toolbar;
  private final ToolbarItem item;
  private boolean isHovered = false;

  ToolbarButton(Toolbar toolbar, ToolbarItem item) {
    this.toolbar = toolbar;
    this.item = item;
    addMouseListener(this);
    setFocusable(true);
    setToolTipText("");
  }

  public ToolbarItem getItem() {
    return item;
  }

  @Override
  public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  @Override
  public Dimension getPreferredSize() {
    if (item instanceof ToolbarSeparator) {
      int sepW = AppPreferences.getScaled(17);
      int sepH = AppPreferences.getScaled(MIN_SIZE);
      return new Dimension(sepW, sepH);
    }
    final var dim = item.getDimension(toolbar.getOrientation());
    int w = Math.max(AppPreferences.getScaled(MIN_SIZE), dim.width + AppPreferences.getScaled(6));
    int h = Math.max(AppPreferences.getScaled(MIN_SIZE), dim.height + AppPreferences.getScaled(6));
    return new Dimension(w, h);
  }

  @Override
  public String getToolTipText(MouseEvent e) {
    return item.getToolTip();
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    // dummy
  }

  @Override
  public void mouseEntered(MouseEvent e) {
    isHovered = true;
    repaint();
  }

  @Override
  public void mouseExited(MouseEvent e) {
    isHovered = false;
    toolbar.setPressed(null);
    repaint();
  }

  @Override
  public void mousePressed(MouseEvent e) {
    if (item != null && (item.isSelectable() || (item instanceof ToolbarClickableItem))) {
      toolbar.setPressed(this);
      repaint();
    }
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    if (toolbar.getPressed() == this) {
      toolbar.setPressed(null);
      repaint();
      if (item != null && item.isSelectable()) {
        toolbar.getToolbarModel().itemSelected(item);
      } else if (item != null && item instanceof ToolbarClickableItem clickableItem) {
        clickableItem.clicked();
      }
    }
  }

  @Override
  public void paintComponent(Graphics g) {
    final var g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    if (item instanceof ToolbarSeparator) {
      item.paintIcon(ToolbarButton.this, g);
      g2.dispose();
      return;
    }

    int w = getWidth();
    int h = getHeight();
    int arc = AppPreferences.getScaled(6);

    boolean selected = toolbar.getToolbarModel().isSelected(item);
    boolean pressed = (toolbar.getPressed() == this);

    if (selected) {
      Color accent = UIManager.getColor("Component.accentColor");
      if (accent == null) accent = new Color(53, 132, 228);

      g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 55));
      g2.fillRoundRect(2, 2, w - 4, h - 4, arc, arc);

      g2.setColor(accent);
      g2.setStroke(new BasicStroke(AppPreferences.getScaledStroke(1.5f)));
      g2.drawRoundRect(2, 2, w - 4, h - 4, arc, arc);
    } else if (pressed) {
      Color pressBg = UIManager.getColor("Button.pressedBackground");
      if (pressBg == null) pressBg = new Color(128, 128, 128, 60);
      g2.setColor(pressBg);
      g2.fillRoundRect(2, 2, w - 4, h - 4, arc, arc);
    } else if (isHovered) {
      Color hoverBg = UIManager.getColor("Button.hoverBackground");
      if (hoverBg == null) hoverBg = new Color(128, 128, 128, 35);
      g2.setColor(hoverBg);
      g2.fillRoundRect(2, 2, w - 4, h - 4, arc, arc);
    }

    if (hasFocus()) {
      Color accent = UIManager.getColor("Component.accentColor");
      if (accent == null) accent = new Color(53, 132, 228);
      g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 120));
      g2.setStroke(new BasicStroke(AppPreferences.getScaledStroke(2.0f)));
      g2.drawRoundRect(1, 1, w - 2, h - 2, arc + 1, arc + 1);
    }

    if (pressed && item instanceof ToolbarClickableItem clickableItem) {
      final var gPress = g.create();
      int pad = AppPreferences.getScaled(5);
      gPress.translate(pad, pad);
      clickableItem.paintPressedIcon(ToolbarButton.this, gPress);
      gPress.dispose();
      g2.dispose();
      return;
    }

    // Paint item centered
    final var dim = item.getDimension(toolbar.getOrientation());
    int iconX = (w - dim.width) / 2;
    int iconY = (h - dim.height) / 2;
    final var gIcon = g.create();
    gIcon.translate(iconX, iconY);
    item.paintIcon(ToolbarButton.this, gIcon);
    gIcon.dispose();

    g2.dispose();
  }
}
