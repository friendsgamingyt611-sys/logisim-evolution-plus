/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.gui.main;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.SubcircuitFactory;
import com.cburch.logisim.gui.icons.symbolic.CircuitGlyphIcon;
import com.cburch.logisim.gui.menu.Popups;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.tools.AddTool;
import com.cburch.logisim.tools.Tool;
import com.cburch.logisim.vhdl.base.VhdlEntity;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class CircuitsList extends JPanel implements ExplorerModel.Listener {

  private static final long serialVersionUID = 1L;
  private final ExplorerModel model;
  private final JPanel itemsPanel;
  private final JLabel headerLabel;
  private AddTool hoveredTool = null;

  public CircuitsList(ExplorerModel model) {
    super(new BorderLayout());
    this.model = model;
    setOpaque(false);

    // Section header
    final var headerBox = new JPanel(new BorderLayout());
    headerBox.setOpaque(false);
    headerBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 12, 2, 12));

    headerLabel = new JLabel("CIRCUITS");
    headerLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, AppPreferences.getScaled(11)));
    Color fgMuted = UIManager.getColor("Label.disabledForeground");
    if (fgMuted == null) fgMuted = new Color(154, 153, 150);
    headerLabel.setForeground(fgMuted);
    headerBox.add(headerLabel, BorderLayout.WEST);

    add(headerBox, BorderLayout.NORTH);

    itemsPanel = new JPanel();
    itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
    itemsPanel.setOpaque(false);
    itemsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 6, 6, 6));

    add(itemsPanel, BorderLayout.CENTER);

    model.addListener(this);
    refresh();
  }

  public void refresh() {
    itemsPanel.removeAll();
    final var circuits = model.getCircuits();
    headerLabel.setText("CIRCUITS (" + circuits.size() + ")");

    for (final var tool : circuits) {
      itemsPanel.add(new CircuitRow(tool));
      itemsPanel.add(Box.createVerticalStrut(AppPreferences.getScaled(2)));
    }

    revalidate();
    repaint();
  }

  @Override
  public void toolArmed(Tool tool) {
    repaint();
  }

  @Override
  public void circuitChanged(Circuit circuit) {
    refresh();
  }

  @Override
  public void structureChanged() {
    refresh();
  }

  @Override
  public void filterChanged(String query) {
    // Circuits stay visible or filter based on query
    if (query == null || query.isBlank()) {
      for (final var comp : itemsPanel.getComponents()) {
        comp.setVisible(true);
      }
    } else {
      final var q = query.toLowerCase();
      for (final var comp : itemsPanel.getComponents()) {
        if (comp instanceof CircuitRow row) {
          row.setVisible(row.tool.getDisplayName().toLowerCase().contains(q));
        }
      }
    }
    revalidate();
    repaint();
  }

  @Override
  public void viewModeChanged(String newMode) {
    // Handled by card layout
  }

  private class CircuitRow extends JPanel {
    private static final long serialVersionUID = 1L;
    private final AddTool tool;
    private final CircuitGlyphIcon icon;

    CircuitRow(AddTool tool) {
      super(new BorderLayout());
      this.tool = tool;
      setOpaque(false);

      final var rowH = AppPreferences.getScaled(28);
      setPreferredSize(new Dimension(-1, rowH));
      setMaximumSize(new Dimension(Integer.MAX_VALUE, rowH));

      final var isActive = model.isCircuitActive(tool);
      icon = new CircuitGlyphIcon(isActive);

      final var label = new JLabel(tool.getDisplayName());
      label.setFont(new Font(Font.SANS_SERIF, isActive ? Font.BOLD : Font.PLAIN, AppPreferences.getScaled(12)));
      label.setIcon(icon);
      label.setIconTextGap(AppPreferences.getScaled(8));
      label.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 8));

      Color fgMain = UIManager.getColor("Label.foreground");
      if (fgMain == null) fgMain = new Color(245, 245, 245);
      Color fgMuted = UIManager.getColor("Label.disabledForeground");
      if (fgMuted == null) fgMuted = new Color(154, 153, 150);
      Color accent = UIManager.getColor("Component.accentColor");
      if (accent == null) accent = new Color(53, 132, 228);

      if (isActive) {
        icon.setColor(accent);
        label.setForeground(fgMain);
      } else {
        icon.setColor(fgMuted);
        label.setForeground(fgMain);
      }

      add(label, BorderLayout.CENTER);

      addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
          hoveredTool = tool;
          repaint();
        }

        @Override
        public void mouseExited(MouseEvent e) {
          if (hoveredTool == tool) {
            hoveredTool = null;
            repaint();
          }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
          if (e.getClickCount() == 2) {
            final var factory = tool.getFactory(false);
            if (factory instanceof SubcircuitFactory sub) {
              model.setCurrentCircuit(sub.getSubcircuit());
            } else if (factory instanceof VhdlEntity vhdl) {
              model.setCurrentHdl(vhdl.getContent());
            }
          } else {
            model.armTool(tool);
          }
        }

        @Override
        public void mousePressed(MouseEvent e) {
          if (e.isPopupTrigger()) showMenu(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
          if (e.isPopupTrigger()) showMenu(e);
        }

        private void showMenu(MouseEvent e) {
          final var fact = tool.getFactory(false);
          if (fact instanceof SubcircuitFactory sub) {
            final var menu = Popups.forCircuit(model.getProject(), tool, sub.getSubcircuit());
            if (menu != null) menu.show(CircuitRow.this, e.getX(), e.getY());
          } else if (fact instanceof VhdlEntity vhdl) {
            final var menu = Popups.forVhdl(model.getProject(), tool, vhdl.getContent());
            if (menu != null) menu.show(CircuitRow.this, e.getX(), e.getY());
          }
        }
      });
    }

    @Override
    protected void paintComponent(Graphics g) {
      final var g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      int w = getWidth();
      int h = getHeight();
      int arc = AppPreferences.getScaled(6);
      int marginX = AppPreferences.getScaled(4);

      Color accent = UIManager.getColor("Component.accentColor");
      if (accent == null) accent = new Color(53, 132, 228);

      final var isActive = model.isCircuitActive(tool);
      final var isHovered = (hoveredTool == tool);
      final var isArmed = (model.getArmedTool() == tool);

      if (isArmed) {
        // 18% accent fill for armed tool
        g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), (int) (255 * 0.18f)));
        g2.fillRoundRect(marginX, 1, w - 2 * marginX, h - 2, arc, arc);
      } else if (isHovered) {
        Color hoverBg = AppPreferences.isDarkTheme()
            ? new Color(255, 255, 255, 14)
            : new Color(0, 0, 0, 12);
        g2.setColor(hoverBg);
        g2.fillRoundRect(marginX, 1, w - 2 * marginX, h - 2, arc, arc);
      }

      if (isActive) {
        // Current circuit: 3px left bar, no fill
        g2.setColor(accent);
        g2.fillRoundRect(marginX, 2, AppPreferences.getScaled(3), h - 4, 2, 2);
      }

      g2.dispose();
      super.paintComponent(g);
    }
  }
}
