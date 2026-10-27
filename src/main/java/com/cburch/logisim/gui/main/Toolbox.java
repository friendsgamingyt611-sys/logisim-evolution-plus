/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.gui.main;

import static com.cburch.logisim.gui.Strings.S;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.file.LibraryEvent;
import com.cburch.logisim.file.LibraryListener;
import com.cburch.logisim.gui.generic.ProjectExplorer;
import com.cburch.logisim.gui.icons.symbolic.AddCircuitIcon;
import com.cburch.logisim.gui.icons.symbolic.AddVhdlIcon;
import com.cburch.logisim.gui.icons.symbolic.AppearanceIcon;
import com.cburch.logisim.gui.icons.symbolic.CircuitGlyphIcon;
import com.cburch.logisim.gui.icons.symbolic.DeleteIcon;
import com.cburch.logisim.gui.icons.symbolic.GridToggleIcon;
import com.cburch.logisim.gui.icons.symbolic.ListToggleIcon;
import com.cburch.logisim.gui.icons.symbolic.MoveDownIcon;
import com.cburch.logisim.gui.icons.symbolic.MoveUpIcon;
import com.cburch.logisim.gui.menu.LogisimMenuBar;
import com.cburch.logisim.gui.menu.LogisimMenuItem;
import com.cburch.logisim.gui.menu.MenuListener;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.ProjectEvent;
import com.cburch.logisim.proj.ProjectListener;
import com.cburch.logisim.tools.Tool;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Modern Left Panel (Toolbox / ExplorerPanel) hosting two-row header, action strip,
 * in-place search, and Refined List / Symbol Tiles views.
 */
public class Toolbox extends JPanel implements ExplorerModel.Listener, ProjectListener, LibraryListener {

  private static final long serialVersionUID = 1L;

  private final Project proj;
  private final Frame frame;
  private final MenuListener menu;
  private final ExplorerModel model;

  private final ProjectExplorer toolbox;
  private final CircuitsList circuitsList;
  private final TileView tileView;

  private final JLabel projectTitleLabel;
  private final JButton btnAddCircuit;
  private final JButton btnAddVhdl;
  private final JButton btnMoveUp;
  private final JButton btnMoveDown;
  private final JToggleButton btnAppearance;
  private final JButton btnDelete;
  private final JToggleButton btnTiles;
  private final JToggleButton btnList;

  private final JTextField searchField;
  private final JButton clearSearchBtn;

  private final CardLayout cardLayout;
  private final JPanel cardsPanel;

  public Toolbox(Project proj, Frame frame, MenuListener menu) {
    super(new BorderLayout());
    this.proj = proj;
    this.frame = frame;
    this.menu = menu;
    this.model = new ExplorerModel(proj, frame);

    // =========================================================================
    // ROW 1: Project Header (40px)
    // =========================================================================
    final var projectHeaderPanel = new JPanel(new BorderLayout());
    projectHeaderPanel.setPreferredSize(new Dimension(-1, AppPreferences.getScaled(40)));
    projectHeaderPanel.setBorder(BorderFactory.createEmptyBorder(0, AppPreferences.getScaled(10), 0, AppPreferences.getScaled(10)));
    projectHeaderPanel.putClientProperty(FlatClientProperties.STYLE, "background: $Panel.background;");

    final var titleBox = new JPanel();
    titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.X_AXIS));
    titleBox.setOpaque(false);

    final var projIcon = new CircuitGlyphIcon(false);
    Color fgMuted = new Color(154, 160, 168);
    projIcon.setColor(fgMuted);

    projectTitleLabel = new JLabel() {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (proj != null && proj.getLogisimFile() != null && proj.getLogisimFile().isDirty()) {
          final var g2 = (Graphics2D) g.create();
          g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          Color warn = AppPreferences.isDarkTheme()
              ? new Color(0xE5, 0xA5, 0x0A)
              : new Color(0xB8, 0x7D, 0x00);
          g2.setColor(warn);
          int dotSize = AppPreferences.getScaled(6);
          int textW = getFontMetrics(getFont()).stringWidth(getText());
          int iconW = getIcon() != null ? getIcon().getIconWidth() + getIconTextGap() : 0;
          int x = iconW + textW + AppPreferences.getScaled(4);
          int y = (getHeight() - dotSize) / 2;
          g2.fillOval(x, y, dotSize, dotSize);
          g2.dispose();
        }
      }
    };
    projectTitleLabel.setIcon(projIcon);
    projectTitleLabel.setIconTextGap(AppPreferences.getScaled(6));
    projectTitleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, AppPreferences.getScaled(13)));
    updateProjectTitle();
    titleBox.add(projectTitleLabel);
    projectHeaderPanel.add(titleBox, BorderLayout.WEST);

    // Segmented Toggle: [☰] (List) | [⊞] (Tiles) with 16px vector glyphs
    final var segBox = new JPanel();
    segBox.setLayout(new BoxLayout(segBox, BoxLayout.X_AXIS));
    segBox.putClientProperty(FlatClientProperties.STYLE, "arc: 6; border: 1,1,1,1,$Component.borderColor; background: $Panel.background;");
    final var viewGroup = new ButtonGroup();

    int segW = AppPreferences.getScaled(30);
    int segH = AppPreferences.getScaled(28);

    btnList = new JToggleButton(new ListToggleIcon());
    btnList.setToolTipText("Refined List (Chevron Tree)");
    btnList.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
    btnList.putClientProperty(FlatClientProperties.STYLE, "selectedBackground: $Component.accentColor; selectedForeground: #ffffff; arc: 4;");
    btnList.setPreferredSize(new Dimension(segW, segH));
    btnList.setMinimumSize(new Dimension(segW, segH));
    btnList.setMaximumSize(new Dimension(segW, segH));
    btnList.addActionListener(e -> model.setViewMode(AppPreferences.EXPLORER_VIEW_LIST));

    btnTiles = new JToggleButton(new GridToggleIcon());
    btnTiles.setToolTipText("Symbol Tiles (3-Column Cards)");
    btnTiles.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
    btnTiles.putClientProperty(FlatClientProperties.STYLE, "selectedBackground: $Component.accentColor; selectedForeground: #ffffff; arc: 4;");
    btnTiles.setPreferredSize(new Dimension(segW, segH));
    btnTiles.setMinimumSize(new Dimension(segW, segH));
    btnTiles.setMaximumSize(new Dimension(segW, segH));
    btnTiles.addActionListener(e -> model.setViewMode(AppPreferences.EXPLORER_VIEW_TILES));

    viewGroup.add(btnList);
    viewGroup.add(btnTiles);
    if (AppPreferences.EXPLORER_VIEW_TILES.equals(model.getViewMode())) {
      btnTiles.setSelected(true);
    } else {
      btnList.setSelected(true);
    }

    segBox.add(btnList);
    segBox.add(btnTiles);
    projectHeaderPanel.add(segBox, BorderLayout.EAST);

    // =========================================================================
    // ROW 2: Full-Width Action Strip (44px)
    // =========================================================================
    final var actionStrip = new JPanel();
    actionStrip.setLayout(new BoxLayout(actionStrip, BoxLayout.X_AXIS));
    actionStrip.setOpaque(false);
    actionStrip.setPreferredSize(new Dimension(-1, AppPreferences.getScaled(44)));
    actionStrip.setBorder(BorderFactory.createEmptyBorder(
        AppPreferences.getScaled(5),
        AppPreferences.getScaled(8),
        AppPreferences.getScaled(5),
        AppPreferences.getScaled(8)));

    btnAddCircuit = createActionButton(new AddCircuitIcon(20), "Add Circuit", LogisimMenuBar.ADD_CIRCUIT);
    btnAddVhdl = createActionButton(new AddVhdlIcon(20), "Add HDL Entity...", LogisimMenuBar.ADD_VHDL);
    btnMoveUp = createActionButton(new MoveUpIcon(20), "Move Circuit Up", LogisimMenuBar.MOVE_CIRCUIT_UP);
    btnMoveDown = createActionButton(new MoveDownIcon(20), "Move Circuit Down", LogisimMenuBar.MOVE_CIRCUIT_DOWN);
    btnAppearance = createToggleActionButton(new AppearanceIcon(20), "Edit Circuit Appearance", LogisimMenuBar.TOGGLE_APPEARANCE);

    btnDelete = createActionButton(new DeleteIcon(20), "Remove Circuit", LogisimMenuBar.REMOVE_CIRCUIT);
    btnDelete.putClientProperty(FlatClientProperties.STYLE,
        "arc: 6; margin: 4,4,4,4; focusWidth: 2; focusColor: #3584e480; hoverForeground: #e01b24;");

    // Group 1: Add Circuit, Add VHDL
    actionStrip.add(btnAddCircuit);
    actionStrip.add(Box.createHorizontalStrut(AppPreferences.getScaled(2)));
    actionStrip.add(btnAddVhdl);

    // Divider 1
    actionStrip.add(createDivider());

    // Group 2: Move Up, Move Down
    actionStrip.add(btnMoveUp);
    actionStrip.add(Box.createHorizontalStrut(AppPreferences.getScaled(2)));
    actionStrip.add(btnMoveDown);

    // Divider 2
    actionStrip.add(createDivider());

    // Group 3: Toggle Appearance
    actionStrip.add(btnAppearance);

    // Flexible Space
    actionStrip.add(Box.createHorizontalGlue());

    // Group 4: Delete alone at far right
    actionStrip.add(btnDelete);

    // Top Two-Row Header Container
    final var topHeaderContainer = new JPanel();
    topHeaderContainer.setLayout(new BoxLayout(topHeaderContainer, BoxLayout.Y_AXIS));
    topHeaderContainer.setOpaque(false);
    topHeaderContainer.add(projectHeaderPanel);
    topHeaderContainer.add(actionStrip);
    add(topHeaderContainer, BorderLayout.NORTH);

    // =========================================================================
    // SEARCH & CARDS AREA
    // =========================================================================
    final var centerPanel = new JPanel(new BorderLayout());
    centerPanel.setOpaque(false);

    // Shared Search Field (36px)
    final var searchPanel = new JPanel(new BorderLayout());
    searchPanel.setPreferredSize(new Dimension(-1, AppPreferences.getScaled(36)));
    searchPanel.setBorder(BorderFactory.createEmptyBorder(2, 10, 4, 10));
    searchPanel.setOpaque(false);

    final var searchBox = new JPanel(new BorderLayout());
    searchBox.putClientProperty(FlatClientProperties.STYLE, "arc: 6; background: $TextField.background;");
    searchField = new JTextField();
    searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search components");
    searchField.putClientProperty(FlatClientProperties.STYLE, "arc: 6; margin: 0,6,0,6; showClearButton: true;");
    searchField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, AppPreferences.getScaled(12)));

    final var keyHint = new JLabel("/");
    keyHint.setFont(new Font(Font.MONOSPACED, Font.BOLD, AppPreferences.getScaled(11)));
    keyHint.setForeground(new Color(154, 160, 168));
    keyHint.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, AppPreferences.getScaled(8)));
    searchField.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, keyHint);

    // Esc clears search field
    searchField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clearSearch");
    searchField.getActionMap().put("clearSearch", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        searchField.setText("");
        searchField.transferFocus();
      }
    });

    clearSearchBtn = new JButton("✕");
    clearSearchBtn.setVisible(false);

    searchField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        update();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        update();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        update();
      }

      private void update() {
        final var txt = searchField.getText();
        clearSearchBtn.setVisible(!txt.isEmpty());
        model.setSearchQuery(txt);
      }
    });

    searchBox.add(searchField, BorderLayout.CENTER);
    searchPanel.add(searchBox, BorderLayout.CENTER);
    centerPanel.add(searchPanel, BorderLayout.NORTH);

    // Card Container: List View vs Tile View
    cardLayout = new CardLayout();
    cardsPanel = new JPanel(cardLayout);
    cardsPanel.setOpaque(false);

    // Card 1: Refined List View (Circuits List + Modern Tree with In-Place Search)
    final var listCard = new JPanel(new BorderLayout());
    listCard.setOpaque(false);

    circuitsList = new CircuitsList(model);
    listCard.add(circuitsList, BorderLayout.NORTH);

    toolbox = new ProjectExplorer(proj, false, false);
    toolbox.setListener(new ToolboxManip(proj, toolbox));
    toolbox.setRootVisible(false);
    final var treeScroll = new JScrollPane(toolbox);
    treeScroll.setBorder(BorderFactory.createEmptyBorder());
    treeScroll.putClientProperty(FlatClientProperties.STYLE, "border: 0,0,0,0;");
    listCard.add(treeScroll, BorderLayout.CENTER);

    cardsPanel.add(listCard, AppPreferences.EXPLORER_VIEW_LIST);

    // Card 2: Symbol Tiles View
    tileView = new TileView(model);
    cardsPanel.add(tileView, AppPreferences.EXPLORER_VIEW_TILES);

    centerPanel.add(cardsPanel, BorderLayout.CENTER);
    add(centerPanel, BorderLayout.CENTER);

    // Register shortcuts: '/' focuses search (Ctrl+K is reserved for Simulate ticks)
    final var imap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    final var amap = getActionMap();
    imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, 0), "focusSearch");
    amap.put("focusSearch", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        searchField.requestFocusInWindow();
        searchField.selectAll();
      }
    });

    // Model listeners
    model.addListener(this);
    proj.addProjectListener(this);
    if (proj.getLogisimFile() != null) {
      proj.getLogisimFile().addLibraryListener(this);
    }

    cardLayout.show(cardsPanel, model.getViewMode());
    updateActionButtons();
  }

  public int getMeasuredStripWidth() {
    return AppPreferences.getScaled(266);
  }

  @Override
  public Dimension getMinimumSize() {
    return new Dimension(getMeasuredStripWidth(), super.getMinimumSize().height);
  }

  @Override
  public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    return new Dimension(Math.max(d.width, getMeasuredStripWidth()), d.height);
  }

  private static JComponent createDivider() {
    final var div = new JPanel() {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final var g2 = (Graphics2D) g.create();
        Color c = UIManager.getColor("Component.borderColor");
        if (c == null) {
          c = AppPreferences.isDarkTheme() ? new Color(0x30, 0x30, 0x30) : new Color(0xD0, 0xD5, 0xDD);
        }
        g2.setColor(c);
        int midX = getWidth() / 2;
        int padY = AppPreferences.getScaled(6);
        g2.drawLine(midX, padY, midX, getHeight() - padY);
        g2.dispose();
      }
    };
    div.setOpaque(false);
    int w = AppPreferences.getScaled(13);
    int h = AppPreferences.getScaled(34);
    div.setPreferredSize(new Dimension(w, h));
    div.setMinimumSize(new Dimension(w, h));
    div.setMaximumSize(new Dimension(w, h));
    return div;
  }

  private JButton createActionButton(Icon icon, String defaultTip, LogisimMenuItem item) {
    final var btn = new JButton(icon);
    btn.setToolTipText(defaultTip);
    btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
    btn.putClientProperty(FlatClientProperties.STYLE,
        "arc: 6; margin: 4,4,4,4; focusWidth: 2; focusColor: #3584e480;");
    int btnSize = AppPreferences.getScaled(34);
    btn.setPreferredSize(new Dimension(btnSize, btnSize));
    btn.setMinimumSize(new Dimension(btnSize, btnSize));
    btn.setMaximumSize(new Dimension(btnSize, btnSize));
    btn.addActionListener(e -> {
      if (menu != null && menu.isEnabled(item)) {
        menu.doAction(item);
      }
    });
    return btn;
  }

  private JToggleButton createToggleActionButton(Icon icon, String defaultTip, LogisimMenuItem item) {
    final var btn = new JToggleButton(icon);
    btn.setToolTipText(defaultTip);
    btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
    btn.putClientProperty(FlatClientProperties.STYLE,
        "arc: 6; margin: 4,4,4,4; focusWidth: 2; focusColor: #3584e480; selectedBackground: $Component.accentColor; selectedForeground: #ffffff;");
    int btnSize = AppPreferences.getScaled(34);
    btn.setPreferredSize(new Dimension(btnSize, btnSize));
    btn.setMinimumSize(new Dimension(btnSize, btnSize));
    btn.setMaximumSize(new Dimension(btnSize, btnSize));
    btn.addActionListener(e -> {
      if (menu != null && menu.isEnabled(item)) {
        menu.doAction(item);
      }
    });
    return btn;
  }

  private void updateProjectTitle() {
    final var file = proj != null ? proj.getLogisimFile() : null;
    final var name = (file != null) ? file.getName() : "Project";
    projectTitleLabel.setText(name);
    projectTitleLabel.setToolTipText(name);
    repaint();
  }

  public void setEditorView(String view) {
    if (btnAppearance != null) {
      boolean isApp = Frame.EDIT_APPEARANCE.equals(view);
      btnAppearance.setSelected(isApp);
      btnAppearance.setToolTipText(isApp ? "Back to Circuit Layout" : "Edit Circuit Appearance");
    }
  }

  public void updateActionButtons() {
    final var cur = (proj != null) ? proj.getCurrentCircuit() : null;
    final var file = (proj != null) ? proj.getLogisimFile() : null;
    final var curName = (cur != null) ? cur.getName() : "";
    final var tools = (file != null) ? file.getTools() : null;
    final var curIndex = (file != null && cur != null) ? file.indexOfCircuit(cur) : -1;
    final var numCircuits = (tools != null) ? tools.size() : 0;

    if (btnAddCircuit != null) {
      btnAddCircuit.setEnabled(menu != null && menu.isEnabled(LogisimMenuBar.ADD_CIRCUIT));
      btnAddCircuit.setToolTipText("Add Circuit");
    }
    if (btnAddVhdl != null) {
      btnAddVhdl.setEnabled(menu != null && menu.isEnabled(LogisimMenuBar.ADD_VHDL));
      btnAddVhdl.setToolTipText("Add HDL Entity...");
    }
    if (btnMoveUp != null) {
      boolean canUp = menu != null && menu.isEnabled(LogisimMenuBar.MOVE_CIRCUIT_UP);
      btnMoveUp.setEnabled(canUp);
      if (canUp) {
        btnMoveUp.setToolTipText("Move '" + curName + "' up · Shift+Ctrl+U");
      } else {
        btnMoveUp.setToolTipText("Already first");
      }
    }
    if (btnMoveDown != null) {
      boolean canDown = menu != null && menu.isEnabled(LogisimMenuBar.MOVE_CIRCUIT_DOWN);
      btnMoveDown.setEnabled(canDown);
      if (canDown) {
        btnMoveDown.setToolTipText("Move '" + curName + "' down · Shift+Ctrl+D");
      } else {
        btnMoveDown.setToolTipText("Already last");
      }
    }
    if (btnAppearance != null) {
      boolean isApp = frame != null && Frame.EDIT_APPEARANCE.equals(frame.getEditorView());
      btnAppearance.setSelected(isApp);
      btnAppearance.setEnabled(menu != null && menu.isEnabled(LogisimMenuBar.TOGGLE_APPEARANCE));
      btnAppearance.setToolTipText(isApp ? "Back to Circuit Layout" : "Edit Circuit Appearance");
    }
    if (btnDelete != null) {
      boolean canDelete = menu != null && menu.isEnabled(LogisimMenuBar.REMOVE_CIRCUIT);
      btnDelete.setEnabled(canDelete);
      if (canDelete) {
        btnDelete.setToolTipText("Remove Circuit");
      } else {
        btnDelete.setToolTipText("Can't remove the only circuit");
      }
    }
  }

  void setHaloedTool(Tool value) {
    toolbox.setHaloedTool(value);
  }

  public void updateStructure() {
    toolbox.updateStructure();
    circuitsList.refresh();
    updateProjectTitle();
    updateActionButtons();
  }

  @Override
  public void toolArmed(Tool tool) {}

  @Override
  public void circuitChanged(Circuit circuit) {
    updateProjectTitle();
    updateActionButtons();
  }

  @Override
  public void structureChanged() {
    updateStructure();
  }

  @Override
  public void filterChanged(String query) {
    if (toolbox != null) {
      toolbox.setFilter(query);
    }
  }

  public JButton getBtnAddCircuit() {
    return btnAddCircuit;
  }

  public JButton getBtnAddVhdl() {
    return btnAddVhdl;
  }

  public JButton getBtnMoveUp() {
    return btnMoveUp;
  }

  public JButton getBtnMoveDown() {
    return btnMoveDown;
  }

  public JToggleButton getBtnAppearance() {
    return btnAppearance;
  }

  public JButton getBtnDelete() {
    return btnDelete;
  }

  @Override
  public void viewModeChanged(String newMode) {
    if (AppPreferences.EXPLORER_VIEW_TILES.equals(newMode)) {
      btnTiles.setSelected(true);
      final var selPath = toolbox.getSelectionPath();
      if (selPath != null && selPath.getPathCount() >= 2) {
        final var node = selPath.getPathComponent(1);
        if (node instanceof com.cburch.logisim.gui.generic.ProjectExplorerLibraryNode libNode) {
          tileView.selectCategory(libNode.getValue().getDisplayName());
        }
      }
    } else {
      btnList.setSelected(true);
    }
    cardLayout.show(cardsPanel, newMode);
  }

  @Override
  public void projectChanged(ProjectEvent event) {
    updateProjectTitle();
  }

  @Override
  public void libraryChanged(LibraryEvent event) {
    updateProjectTitle();
  }
}
