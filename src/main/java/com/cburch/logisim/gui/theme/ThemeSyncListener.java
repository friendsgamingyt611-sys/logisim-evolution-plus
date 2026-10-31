/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.gui.theme;

import com.cburch.logisim.gui.main.ComponentGlyphCache;
import com.cburch.logisim.gui.main.Frame;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.proj.Projects;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Global Swing UIManager lookAndFeel bridge and component refresh dispatcher.
 * Dispatches UI invalidation across the canvas, GridPainter, and sidebar delegates
 * whenever the look and feel changes.
 */
public class ThemeSyncListener implements PropertyChangeListener {

  private static final ThemeSyncListener INSTANCE = new ThemeSyncListener();
  private static boolean installed = false;

  public static ThemeSyncListener getInstance() {
    return INSTANCE;
  }

  public static synchronized void install() {
    if (!installed) {
      UIManager.addPropertyChangeListener(INSTANCE);
      installed = true;
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if ("lookAndFeel".equals(evt.getPropertyName())) {
      onLookAndFeelChanged();
    }
  }

  public void onLookAndFeelChanged() {
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(this::onLookAndFeelChanged);
      return;
    }

    // 1. Invalidate glyph caches
    ComponentGlyphCache.clearCache();

    // 2. Refresh open project windows
    for (final var window : java.awt.Window.getWindows()) {
      if (window instanceof Frame frame) {
        final var canvas = frame.getCanvas();
        if (canvas != null) {
          canvas.setBackground(AppPreferences.getCanvasBgColor());
          canvas.invalidateGrid();
          canvas.repaint();
        }
        frame.updateThemeUI();
      }
    }
    for (final var proj : Projects.getOpenProjects()) {
      final var frame = proj.getFrame();
      if (frame != null) {
        final var canvas = frame.getCanvas();
        if (canvas != null) {
          canvas.setBackground(AppPreferences.getCanvasBgColor());
          canvas.invalidateGrid();
          canvas.repaint();
        }
        frame.updateThemeUI();
      }
    }
  }
}
