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

import com.cburch.logisim.file.LibraryEvent;
import com.cburch.logisim.file.LibraryListener;
import com.cburch.logisim.gui.generic.ProjectExplorer;
import com.cburch.logisim.gui.icons.symbolic.AddCircuitIcon;
import com.cburch.logisim.gui.icons.symbolic.AddVhdlIcon;
import com.cburch.logisim.gui.icons.symbolic.AppearanceIcon;
import com.cburch.logisim.gui.icons.symbolic.CircuitGlyphIcon;
import com.cburch.logisim.gui.icons.symbolic.DeleteIcon;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Modern Left Panel (Toolbox / ExplorerPanel) hosting both Refined List and Symbol Tiles views.
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
  private final JButton btnAppearance;
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

    // 1. Panel Header (40px)
    final var headerPanel = new JPanel(new BorderLayout());
    headerPanel.setPreferredSize(new Dimension(-1, AppPreferences.getScaled(40)));
    headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
    headerPanel.putClientProperty(FlatClientProperties.STYLE, "background: $Panel.background;");

    // Project Name + Dirty Dot
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
          Color warn = UIManager.getColor("Component.warningColor");
          if (warn == null) warn = new Color(229, 165, 10);
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
    headerPanel.add(titleBox, BorderLayout.WEST);

    // Header Right Actions: [6 circuit actions] [⊞ | ☰]
    final var actionsBox = new JPanel();
    actionsBox.setLayout(new BoxLayout(actionsBox, BoxLayout.X_AXIS));
    actionsBox.setOpaque(false);

    btnAddCircuit = createActionButton(new AddCircuitIcon(), "projectAddCircuitTip", "Add Circuit", LogisimMenuBar.ADD_CIRCUIT);
    btnAddCircuit.putClientProperty(FlatClientProperties.STYLE, "arc: 4; margin: 2,2,2,2; hoverForeground: #2ed173;");

    btnAddVhdl = createActionButton(new AddVhdlIcon(), "projectAddVhdlItem", "Add HDL Entity...", LogisimMenuBar.ADD_VHDL);
    btnAddVhdl.putClientProperty(FlatClientProperties.STYLE, "arc: 4; margin: 2,2,2,2; hoverForeground: #2ed173;");

    btnMoveUp = createActionButton(new MoveUpIcon(), "projectMoveCircuitUpTip", "Move Circuit Up", LogisimMenuBar.MOVE_CIRCUIT_UP);
    btnMoveUp.putClientProperty(FlatClientProperties.STYLE, "arc: 4; margin: 2,2,2,2; hoverForeground: #3584e4;");

    btnMoveDown = createActionButton(new MoveDownIcon(), "projectMoveCircuitDownTip", "Move Circuit Down", LogisimMenuBar.MOVE_CIRCUIT_DOWN);
    btnMoveDown.putClientProperty(FlatClientProperties.STYLE, "arc: 4; margin: 2,2,2,2; hoverForeground: #3584e4;");

    btnAppearance = createActionButton(new AppearanceIcon(), "projectEditAppearanceTip", "Edit Circuit Appearance", LogisimMenuBar.TOGGLE_APPEARANCE);
    btnAppearance.putClientProperty(FlatClientProperties.STYLE, "arc: 4; margin: 2,2,2,2; hoverForeground: #9a9996;");

    btnDelete = createActionButton(new DeleteIcon(), "projectRemoveCircuitTip", "Remove Circuit", LogisimMenuBar.REMOVE_CIRCUIT);
    btnDelete.putClientProperty(FlatClientProperties.STYLE, "arc: 4; margin: 2,2,2,2; hoverForeground: #e01b24;");

    actionsBox.add(btnAddCircuit);
    actionsBox.add(Box.createHorizontalStrut(AppPreferences.getScaled(2)));
    actionsBox.add(btnAddVhdl);
    actionsBox.add(Box.createHorizontalStrut(AppPreferences.getScaled(2)));
    actionsBox.add(btnMoveUp);
    actionsBox.add(Box.createHorizontalStrut(AppPreferences.getScaled(2)));
    actionsBox.add(btnMoveDown);
    actionsBox.add(Box.createHorizontalStrut(AppPreferences.getScaled(2)));
    actionsBox.add(btnAppearance);
    actionsBox.add(Box.createHorizontalStrut(AppPreferences.getScaled(2)));
    actionsBox.add(btnDelete);
    actionsBox.add(Box.createHorizontalStrut(AppPreferences.getScaled(6)));

    // Segmented Toggle: [⊞ | ☰]
    final var segBox = new JPanel();
    segBox.setLayout(new BoxLayout(segBox, BoxLayout.X_AXIS));
    segBox.putClientProperty(FlatClientProperties.STYLE, "arc: 6; border: 1,1,1,1,$Component.borderColor; background: $Panel.background;");
    final var viewGroup = new ButtonGroup();

    btnTiles = new JToggleButton("⊞");
    btnTiles.setToolTipText("Symbol Tiles (3-Column Cards)");
    btnTiles.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
    btnTiles.putClientProperty(FlatClientProperties.STYLE, "selectedBackground: $Component.accentColor; selectedForeground: #ffffff; arc: 4;");
    btnTiles.setPreferredSize(new Dimension(AppPreferences.getScaled(28), AppPreferences.getScaled(24)));
    btnTiles.addActionListener(e -> model.setViewMode(AppPreferences.EXPLORER_VIEW_TILES));

    btnList = new JToggleButton("☰");
    btnList.setToolTipText("Refined List (Chevron Tree)");
    btnList.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
    btnList.putClientProperty(FlatClientProperties.STYLE, "selectedBackground: $Component.accentColor; selectedForeground: #ffffff; arc: 4;");
    btnList.setPreferredSize(new Dimension(AppPreferences.getScaled(28), AppPreferences.getScaled(24)));
    btnList.addActionListener(e -> model.setViewMode(AppPreferences.EXPLORER_VIEW_LIST));

    viewGroup.add(btnTiles);
    viewGroup.add(btnList);
    if (AppPreferences.EXPLORER_VIEW_TILES.equals(model.getViewMode())) {
      btnTiles.setSelected(true);
    } else {
      btnList.setSelected(true);
    }

    segBox.add(btnTiles);
    segBox.add(btnList);
    actionsBox.add(segBox);

    headerPanel.add(actionsBox, BorderLayout.EAST);
    add(headerPanel, BorderLayout.NORTH);

    // 2. Search & Cards Area
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
    clearSearchBtn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
    clearSearchBtn.setVisible(false);
    clearSearchBtn.addActionListener(e -> searchField.setText(""));

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

    // 3. Card Container: List View vs Tile View
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
    toolbox.setRootVisible(false); // Hide root so library categories are top-level
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

    // Register shortcuts: '/' or 'Ctrl+K' focuses search
    final var imap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    final var amap = getActionMap();
    imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, 0), "focusSearch");
    imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.CTRL_MASK), "focusSearch");
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

  private void updateProjectTitle() {
    final var file = proj != null ? proj.getLogisimFile() : null;
    final var name = (file != null) ? file.getName() : "Project";
    projectTitleLabel.setText(name);
    repaint();
  }

  private JButton createActionButton(javax.swing.Icon icon, String tipKey, String defaultTip, LogisimMenuItem item) {
    final var btn = new JButton(icon);
    String tip = null;
    try {
      tip = S.get(tipKey);
    } catch (Exception ignored) {
    }
    btn.setToolTipText(tip != null && !tip.isEmpty() ? tip : defaultTip);
    btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
    btn.putClientProperty(FlatClientProperties.STYLE, "arc: 4; margin: 2,2,2,2;");
    int btnSize = AppPreferences.getScaled(24);
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

  public void updateActionButtons() {
    if (btnAddCircuit != null) btnAddCircuit.setEnabled(menu != null && menu.isEnabled(LogisimMenuBar.ADD_CIRCUIT));
    if (btnAddVhdl != null) btnAddVhdl.setEnabled(menu != null && menu.isEnabled(LogisimMenuBar.ADD_VHDL));
    if (btnMoveUp != null) btnMoveUp.setEnabled(menu != null && menu.isEnabled(LogisimMenuBar.MOVE_CIRCUIT_UP));
    if (btnMoveDown != null) btnMoveDown.setEnabled(menu != null && menu.isEnabled(LogisimMenuBar.MOVE_CIRCUIT_DOWN));
    if (btnAppearance != null) btnAppearance.setEnabled(menu != null && menu.isEnabled(LogisimMenuBar.TOGGLE_APPEARANCE));
    if (btnDelete != null) btnDelete.setEnabled(menu != null && menu.isEnabled(LogisimMenuBar.REMOVE_CIRCUIT));
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
  public void circuitChanged(com.cburch.logisim.circuit.Circuit circuit) {
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

  public JButton getBtnAppearance() {
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
