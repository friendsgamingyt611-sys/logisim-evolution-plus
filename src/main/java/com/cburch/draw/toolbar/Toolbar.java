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
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class Toolbar extends JPanel {
  public static final Object VERTICAL = new Object();
  public static final Object HORIZONTAL = new Object();
  private static final long serialVersionUID = 1L;
  private final JPanel subpanel;
  private final JPanel centerPanel;
  private final MyListener myListener;
  private ToolbarModel model;
  private Object orientation;
  private ToolbarButton curPressed;

  public Toolbar(ToolbarModel model) {
    super(new BorderLayout());
    this.subpanel = new JPanel();
    this.subpanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 6, 3, 6));
    Color tbBg = javax.swing.UIManager.getColor("Chrome.barBackground");
    if (tbBg == null) {
      tbBg = AppPreferences.isDarkTheme() ? new Color(37, 37, 38) : new Color(243, 244, 246);
    }
    this.setBackground(tbBg);
    this.subpanel.setBackground(tbBg);
    Color tbBorder = javax.swing.UIManager.getColor("Chrome.borderColor");
    if (tbBorder == null) {
      tbBorder = AppPreferences.isDarkTheme() ? new Color(51, 51, 51) : new Color(208, 213, 221);
    }
    this.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, tbBorder));
    this.model = model;
    this.orientation = HORIZONTAL;
    this.myListener = new MyListener();
    this.curPressed = null;

    this.centerPanel = new JPanel();
    this.centerPanel.setOpaque(false);
    this.centerPanel.setBackground(tbBg);
    this.add(this.centerPanel, BorderLayout.CENTER);
    setOrientation(HORIZONTAL);

    computeContents();
    if (model != null) model.addToolbarModelListener(myListener);
  }

  @Override
  public void updateUI() {
    super.updateUI();
    if (subpanel != null) {
      Color tbBg = javax.swing.UIManager.getColor("Chrome.barBackground");
      if (tbBg == null) {
        tbBg = AppPreferences.isDarkTheme() ? new Color(37, 37, 38) : new Color(243, 244, 246);
      }
      setBackground(tbBg);
      subpanel.setBackground(tbBg);
      if (centerPanel != null) {
        centerPanel.setBackground(tbBg);
      }
      Color tbBorder = javax.swing.UIManager.getColor("Chrome.borderColor");
      if (tbBorder == null) {
        tbBorder = AppPreferences.isDarkTheme() ? new Color(51, 51, 51) : new Color(208, 213, 221);
      }
      setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, tbBorder));
    }
  }

  private void computeContents() {
    subpanel.removeAll();
    final var m = model;
    if (m != null) {
      for (ToolbarItem item : m.getItems()) {
        subpanel.add(new ToolbarButton(this, item));
      }
      subpanel.add(Box.createGlue());
    }
    revalidate();
  }

  Object getOrientation() {
    return orientation;
  }

  public void setOrientation(Object value) {
    int axis;
    String position;
    if (value.equals(HORIZONTAL)) {
      axis = BoxLayout.X_AXIS;
      position = BorderLayout.LINE_START;
    } else if (value.equals(VERTICAL)) {
      axis = BoxLayout.Y_AXIS;
      position = BorderLayout.NORTH;
    } else {
      throw new IllegalArgumentException();
    }
    this.remove(subpanel);
    subpanel.setLayout(new BoxLayout(subpanel, axis));
    this.add(subpanel, position);
    this.orientation = value;
  }

  ToolbarButton getPressed() {
    return curPressed;
  }

  void setPressed(ToolbarButton value) {
    final var oldValue = curPressed;
    if (oldValue != value) {
      curPressed = value;
      if (oldValue != null) oldValue.repaint();
      if (value != null) value.repaint();
    }
  }

  public ToolbarModel getToolbarModel() {
    return model;
  }

  public void setToolbarModel(ToolbarModel value) {
    final var oldValue = model;
    if (value != oldValue) {
      if (oldValue != null) oldValue.removeToolbarModelListener(myListener);
      if (value != null) value.addToolbarModelListener(myListener);
      model = value;
      computeContents();
    }
  }

  private class MyListener implements ToolbarModelListener {
    @Override
    public void toolbarAppearanceChanged(ToolbarModelEvent event) {
      repaint();
    }

    @Override
    public void toolbarContentsChanged(ToolbarModelEvent event) {
      computeContents();
    }
  }
}
