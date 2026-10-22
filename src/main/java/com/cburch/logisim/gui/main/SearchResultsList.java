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
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.tools.AddTool;
import com.cburch.logisim.tools.Library;
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
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

/**
 * Filtered search results view displaying matches across all collapsed/expanded libraries
 * with highlighted query text and breadcrumbs (e.g. Memory › Register).
 */
public class SearchResultsList extends JPanel implements ExplorerModel.Listener {

  private static final long serialVersionUID = 1L;

  private final ExplorerModel model;
  private final JLabel headerCountLabel;
  private final JPanel itemsContainer;
  private final JScrollPane scrollPane;
  private final JLabel emptyStateLabel;
  private final List<SearchResultRow> rows = new ArrayList<>();
  private Tool hoveredTool = null;

  public SearchResultsList(ExplorerModel model) {
    super(new BorderLayout());
    this.model = model;
    setOpaque(false);

    // Header count badge (e.g. "5 RESULTS")
    final var topPanel = new JPanel(new BorderLayout());
    topPanel.setOpaque(false);
    topPanel.setPreferredSize(new Dimension(-1, AppPreferences.getScaled(26)));
    topPanel.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

    headerCountLabel = new JLabel("0 RESULTS");
    headerCountLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, AppPreferences.getScaled(10)));
    headerCountLabel.setForeground(new Color(154, 160, 168));
    topPanel.add(headerCountLabel, BorderLayout.WEST);
    add(topPanel, BorderLayout.NORTH);

    // Results container
    itemsContainer = new JPanel();
    itemsContainer.setLayout(new BoxLayout(itemsContainer, BoxLayout.Y_AXIS));
    itemsContainer.setOpaque(false);
    itemsContainer.setBorder(BorderFactory.createEmptyBorder(2, 6, 8, 6));

    emptyStateLabel = new JLabel("", JLabel.CENTER);
    emptyStateLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, AppPreferences.getScaled(12)));
    emptyStateLabel.setForeground(new Color(154, 160, 168));
    emptyStateLabel.setBorder(BorderFactory.createEmptyBorder(32, 16, 32, 16));

    scrollPane = new JScrollPane(itemsContainer);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    scrollPane.setOpaque(false);
    scrollPane.getViewport().setOpaque(false);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

    add(scrollPane, BorderLayout.CENTER);

    model.addListener(this);
    refresh();
  }

  public void refresh() {
    itemsContainer.removeAll();
    rows.clear();

    final var query = model.getSearchQuery();
    if (query == null || query.isBlank()) {
      headerCountLabel.setText("0 RESULTS");
      revalidate();
      repaint();
      return;
    }

    final var q = query.trim().toLowerCase();
    List<MatchItem> matches = new ArrayList<>();

    // 1. Search circuits
    for (final var tool : model.getCircuits()) {
      if (matches(tool, "Circuits", q)) {
        matches.add(new MatchItem(tool, "Circuits", "Circuits \u203a " + tool.getDisplayName()));
      }
    }

    // 2. Search all library tools (even inside collapsed groups)
    for (final var lib : model.getLibraries()) {
      collectMatches(lib, lib.getDisplayName(), q, matches);
    }

    int count = matches.size();
    headerCountLabel.setText(count + (count == 1 ? " RESULT" : " RESULTS"));

    if (matches.isEmpty()) {
      emptyStateLabel.setText("<html><center>No components match '<b>" + escapeHtml(query) + "</b>'.<br><small style='color:#888'>Press Esc to clear search.</small></center></html>");
      itemsContainer.add(emptyStateLabel);
    } else {
      for (final var m : matches) {
        final var row = new SearchResultRow(m.tool, m.breadcrumb, query);
        rows.add(row);
        itemsContainer.add(row);
        itemsContainer.add(Box.createVerticalStrut(AppPreferences.getScaled(2)));
      }
    }

    revalidate();
    repaint();
  }

  private void collectMatches(Library lib, String parentPath, String query, List<MatchItem> out) {
    if (lib == null || lib.isHidden()) return;
    String libName = lib.getDisplayName();
    String currentPath = parentPath.equals(libName) ? libName : parentPath + " \u203a " + libName;

    for (final var tool : lib.getTools()) {
      if (matches(tool, currentPath, query)) {
        out.add(new MatchItem(tool, libName, currentPath + " \u203a " + tool.getDisplayName()));
      }
    }

    for (final var sub : lib.getLibraries()) {
      collectMatches(sub, currentPath, query, out);
    }
  }

  private boolean matches(Tool tool, String path, String query) {
    if (tool == null) return false;
    final var name = tool.getDisplayName();
    final var desc = tool.getDescription();
    if (name != null && name.toLowerCase().contains(query)) return true;
    if (desc != null && desc.toLowerCase().contains(query)) return true;
    return path != null && path.toLowerCase().contains(query);
  }

  private static String escapeHtml(String text) {
    if (text == null) return "";
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
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
    refresh();
  }

  @Override
  public void viewModeChanged(String newMode) {}

  private static class MatchItem {
    final Tool tool;
    final String libName;
    final String breadcrumb;

    MatchItem(Tool tool, String libName, String breadcrumb) {
      this.tool = tool;
      this.libName = libName;
      this.breadcrumb = breadcrumb;
    }
  }

  private class SearchResultRow extends JPanel {
    private static final long serialVersionUID = 1L;
    private final Tool tool;

    SearchResultRow(Tool tool, String breadcrumb, String query) {
      super(new BorderLayout());
      this.tool = tool;
      setOpaque(false);
      int h = AppPreferences.getScaled(36);
      setPreferredSize(new Dimension(-1, h));
      setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
      setBorder(BorderFactory.createEmptyBorder(0, AppPreferences.getScaled(6), 0, AppPreferences.getScaled(10)));

      // 1. Icon (20px)
      final var icon = ComponentGlyphCache.getIcon(tool, AppPreferences.getScaled(20));
      final var iconLabel = new JLabel(icon);
      iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, AppPreferences.getScaled(8)));
      add(iconLabel, BorderLayout.WEST);

      // 2. Name with Highlighted Query
      String name = tool.getDisplayName();
      String highlighted = highlightQuery(name, query);
      final var nameLabel = new JLabel("<html>" + highlighted + "</html>");
      nameLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, AppPreferences.getScaled(12)));
      add(nameLabel, BorderLayout.CENTER);

      // 3. Breadcrumb on the right (e.g. Memory › Register)
      // Extract parent category path
      String catPath = breadcrumb;
      int lastArrow = catPath.lastIndexOf('\u203a');
      if (lastArrow > 0) {
        catPath = catPath.substring(0, lastArrow).trim();
      }
      final var bcLabel = new JLabel(catPath);
      bcLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, AppPreferences.getScaled(11)));
      bcLabel.setForeground(new Color(154, 160, 168));
      add(bcLabel, BorderLayout.EAST);

      setToolTipText("<html><b>" + name + "</b><br><span style='color:#888'>" + breadcrumb + "</span><br>" + tool.getDescription() + "</html>");

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
            if (tool instanceof AddTool addTool) {
              final var fact = addTool.getFactory(false);
              if (fact instanceof SubcircuitFactory sub) {
                model.setCurrentCircuit(sub.getSubcircuit());
              } else if (fact instanceof VhdlEntity vhdl) {
                model.setCurrentHdl(vhdl.getContent());
              }
            }
          } else {
            model.armTool(tool);
          }
        }
      });
    }

    private String highlightQuery(String text, String query) {
      if (text == null) return "";
      if (query == null || query.isBlank()) return escapeHtml(text);

      String lower = text.toLowerCase();
      String qLower = query.toLowerCase();
      int idx = lower.indexOf(qLower);
      if (idx < 0) return escapeHtml(text);

      String before = escapeHtml(text.substring(0, idx));
      String match = escapeHtml(text.substring(idx, idx + query.length()));
      String after = escapeHtml(text.substring(idx + query.length()));

      Color accent = UIManager.getColor("Component.accentColor");
      String accentHex = (accent != null)
          ? String.format("#%02x%02x%02x", accent.getRed(), accent.getGreen(), accent.getBlue())
          : "#3584e4";

      return before + "<b style='color:" + accentHex + ";'>" + match + "</b>" + after;
    }

    @Override
    protected void paintComponent(Graphics g) {
      final var g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      int w = getWidth();
      int h = getHeight();
      int arc = AppPreferences.getScaled(6);
      int marginX = AppPreferences.getScaled(2);

      final var isArmed = (model.getArmedTool() == tool);
      final var isHovered = (hoveredTool == tool);

      Color accent = UIManager.getColor("Component.accentColor");
      if (accent == null) accent = new Color(53, 132, 228);

      if (isArmed) {
        g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 55));
        g2.fillRoundRect(marginX, 2, w - 2 * marginX, h - 4, arc, arc);

        g2.setColor(accent);
        g2.fillRoundRect(marginX, 3, AppPreferences.getScaled(3), h - 6, 2, 2);
      } else if (isHovered) {
        Color hoverBg = AppPreferences.isDarkTheme()
            ? new Color(255, 255, 255, 14)
            : new Color(0, 0, 0, 12);
        g2.setColor(hoverBg);
        g2.fillRoundRect(marginX, 2, w - 2 * marginX, h - 4, arc, arc);
      }

      g2.dispose();
      super.paintComponent(g);
    }
  }
}
