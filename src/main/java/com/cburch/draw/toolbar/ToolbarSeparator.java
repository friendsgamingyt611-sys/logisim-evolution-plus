/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.draw.toolbar;

import com.cburch.logisim.prefs.AppPreferences;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

public class ToolbarSeparator implements ToolbarItem {
  private final int size;

  public ToolbarSeparator(int size) {
    this.size = size;
  }

  @Override
  public Dimension getDimension(Object orientation) {
    return new Dimension(AppPreferences.getScaled(17), AppPreferences.getScaled(34));
  }

  @Override
  public String getToolTip() {
    return null;
  }

  @Override
  public boolean isSelectable() {
    return false;
  }

  @Override
  public void paintIcon(Component destination, Graphics gfx) {
    final var dim = destination.getSize();
    final var x = dim.width / 2;
    final var padY = AppPreferences.getScaled(7);
    gfx.setColor(new Color(128, 128, 128, 75));
    gfx.drawLine(x, padY, x, Math.max(padY, dim.height - padY));
  }
}
