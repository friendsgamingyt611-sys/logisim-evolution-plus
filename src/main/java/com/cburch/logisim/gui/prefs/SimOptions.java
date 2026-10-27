/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.gui.prefs;

import static com.cburch.logisim.gui.Strings.S;

import com.cburch.logisim.data.Value;
import com.cburch.logisim.gui.theme.ColorScheme;
import com.cburch.logisim.gui.theme.ColorSchemeRegistry;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.prefs.PrefMonitor;
import com.cburch.logisim.proj.Projects;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class SimOptions extends OptionsPanel {

  private static final long serialVersionUID = 1L;

  private final JLabel schemeTitle = new JLabel();
  private final JComboBox<String> schemeDropdown = new JComboBox<>();
  private final JRadioButton previewLightBtn = new JRadioButton("Light");
  private final JRadioButton previewDarkBtn = new JRadioButton("Dark");
  private final JButton editColorsButton = new JButton("Edit colors");
  private final JButton resetModernButton = new JButton("Reset to Modern");
  private final JLabel classicNoticeLabel = new JLabel();
  private final WirePreviewStrip wirePreviewStrip = new WirePreviewStrip();

  private boolean previewIsDark = AppPreferences.isDarkTheme();

  private final JLabel trueColorTitle = new JLabel();
  private final ColorChooserButton trueColor;
  private final JLabel trueCharTitle = new JLabel();
  private final SymbolChooser trueChar = new SymbolChooser(AppPreferences.TRUE_CHAR, "1T");
  private final JLabel falseColorTitle = new JLabel();
  private final ColorChooserButton falseColor;
  private final JLabel falseCharTitle = new JLabel();
  private final SymbolChooser falseChar = new SymbolChooser(AppPreferences.FALSE_CHAR, "0F");
  private final JLabel unknownColorTitle = new JLabel();
  private final ColorChooserButton unknownColor;
  private final JLabel unknownCharTitle = new JLabel();
  private final SymbolChooser unknownChar = new SymbolChooser(AppPreferences.UNKNOWN_CHAR, "U?Z");
  private final JLabel errorColorTitle = new JLabel();
  private final ColorChooserButton errorColor;
  private final JLabel errorCharTitle = new JLabel();
  private final SymbolChooser errorChar = new SymbolChooser(AppPreferences.ERROR_CHAR, "E!X");
  private final JLabel nilColorTitle = new JLabel();
  private final ColorChooserButton nilColor;
  private final JLabel dontCareCharTitle = new JLabel();
  private final SymbolChooser dontCareChar = new SymbolChooser(AppPreferences.DONTCARE_CHAR, "-X");
  private final JLabel busColorTitle = new JLabel();
  private final ColorChooserButton busColor;
  private final JLabel highlightColorTitle = new JLabel();
  private final ColorChooserButton highlightColor;
  private final JLabel widthErrorColorTitle = new JLabel();
  private final ColorChooserButton widthErrorColor;
  private final JLabel widthErrorCaptionColorTitle = new JLabel();
  private final ColorChooserButton widthErrorCaptionColor;
  private final JLabel widthErrorHighlightColorTitle = new JLabel();
  private final ColorChooserButton widthErrorHighlightColor;
  private final JLabel widthErrorBackgroundColorTitle = new JLabel();
  private final ColorChooserButton clockFrequencyColor;
  private final JLabel clockFrequencyColorTitle = new JLabel();
  private final ColorChooserButton widthErrorBackgroundColor;

  private final JLabel kmap1ColorTitle = new JLabel();
  private final JLabel kmap2ColorTitle = new JLabel();
  private final JLabel kmap3ColorTitle = new JLabel();
  private final JLabel kmap4ColorTitle = new JLabel();
  private final JLabel kmap5ColorTitle = new JLabel();
  private final JLabel kmap6ColorTitle = new JLabel();
  private final JLabel kmap7ColorTitle = new JLabel();
  private final JLabel kmap8ColorTitle = new JLabel();
  private final JLabel kmap9ColorTitle = new JLabel();
  private final JLabel kmap10ColorTitle = new JLabel();
  private final JLabel kmap11ColorTitle = new JLabel();
  private final JLabel kmap12ColorTitle = new JLabel();
  private final JLabel kmap13ColorTitle = new JLabel();
  private final JLabel kmap14ColorTitle = new JLabel();
  private final JLabel kmap15ColorTitle = new JLabel();
  private final JLabel kmap16ColorTitle = new JLabel();
  private final JLabel kmapColorsTitle = new JLabel("", SwingConstants.CENTER);
  private final ColorChooserButton kmap1Color;
  private final ColorChooserButton kmap2Color;
  private final ColorChooserButton kmap3Color;
  private final ColorChooserButton kmap4Color;
  private final ColorChooserButton kmap5Color;
  private final ColorChooserButton kmap6Color;
  private final ColorChooserButton kmap7Color;
  private final ColorChooserButton kmap8Color;
  private final ColorChooserButton kmap9Color;
  private final ColorChooserButton kmap10Color;
  private final ColorChooserButton kmap11Color;
  private final ColorChooserButton kmap12Color;
  private final ColorChooserButton kmap13Color;
  private final ColorChooserButton kmap14Color;
  private final ColorChooserButton kmap15Color;
  private final ColorChooserButton kmap16Color;

  public SimOptions(PreferencesFrame window) {
    super(window);
    AppPreferences.getPrefs().addPreferenceChangeListener(new MyListener());
    AppPreferences.COLOR_SCHEME.addPropertyChangeListener(evt -> {
      SwingUtilities.invokeLater(() -> {
        syncDropdownSelection();
        updateSwatchesAndPreview();
      });
    });

    final var gbc = new GridBagConstraints();
    setLayout(new GridBagLayout());
    gbc.insets = new Insets(2, 4, 4, 2);
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // --- Row 0: Color Scheme Selection ---
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 1;
    schemeTitle.setText("Color Scheme:");
    add(schemeTitle, gbc);

    gbc.gridx++;
    schemeDropdown.addItem(ColorSchemeRegistry.DISPLAY_MODERN);
    schemeDropdown.addItem(ColorSchemeRegistry.DISPLAY_CLASSIC);
    schemeDropdown.addItem(ColorSchemeRegistry.DISPLAY_COLORBLIND);
    schemeDropdown.addItem(ColorSchemeRegistry.DISPLAY_CUSTOM);
    syncDropdownSelection();
    schemeDropdown.addActionListener(e -> onSchemeDropdownChanged());
    add(schemeDropdown, gbc);

    // Variant preview toggle
    gbc.gridx++;
    final var variantPanel = new JPanel();
    final var group = new ButtonGroup();
    group.add(previewLightBtn);
    group.add(previewDarkBtn);
    previewLightBtn.setSelected(!previewIsDark);
    previewDarkBtn.setSelected(previewIsDark);
    previewLightBtn.addActionListener(e -> {
      previewIsDark = false;
      updateSwatchesAndPreview();
    });
    previewDarkBtn.addActionListener(e -> {
      previewIsDark = true;
      updateSwatchesAndPreview();
    });
    variantPanel.add(previewLightBtn);
    variantPanel.add(previewDarkBtn);
    add(variantPanel, gbc);

    gbc.gridx++;
    final var btnPanel = new JPanel();
    editColorsButton.addActionListener(e -> switchToCustomAndEdit(null));
    resetModernButton.addActionListener(e -> {
      AppPreferences.COLOR_SCHEME.set(ColorSchemeRegistry.SCHEME_MODERN);
      syncDropdownSelection();
      updateSwatchesAndPreview();
      notifyCanvasesRepaint();
    });
    btnPanel.add(editColorsButton);
    btnPanel.add(resetModernButton);
    add(btnPanel, gbc);

    // --- Row 1: Classic dark warning notice ---
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.gridwidth = 4;
    classicNoticeLabel.setForeground(new Color(0xD9534F));
    classicNoticeLabel.setText("Notice: Classic colors were designed for a white canvas and may be hard to read in dark mode.");
    classicNoticeLabel.setVisible(false);
    add(classicNoticeLabel, gbc);

    // --- Row 2: Live Wire Preview Strip ---
    gbc.gridy++;
    add(wirePreviewStrip, gbc);

    // --- Wire State Swatches ---
    gbc.gridwidth = 1;

    gbc.gridx = 0;
    gbc.gridy++;
    add(trueColorTitle, gbc);
    gbc.gridx++;
    trueColor = new ColorChooserButton(window, AppPreferences.TRUE_COLOR);
    add(trueColor, gbc);
    gbc.gridx++;
    add(trueCharTitle, gbc);
    gbc.gridx++;
    add(trueChar, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    add(falseColorTitle, gbc);
    gbc.gridx++;
    falseColor = new ColorChooserButton(window, AppPreferences.FALSE_COLOR);
    add(falseColor, gbc);
    gbc.gridx++;
    add(falseCharTitle, gbc);
    gbc.gridx++;
    add(falseChar, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    add(unknownColorTitle, gbc);
    gbc.gridx++;
    unknownColor = new ColorChooserButton(window, AppPreferences.UNKNOWN_COLOR);
    add(unknownColor, gbc);
    gbc.gridx++;
    add(unknownCharTitle, gbc);
    gbc.gridx++;
    add(unknownChar, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    add(errorColorTitle, gbc);
    gbc.gridx++;
    errorColor = new ColorChooserButton(window, AppPreferences.ERROR_COLOR);
    add(errorColor, gbc);
    gbc.gridx++;
    add(errorCharTitle, gbc);
    gbc.gridx++;
    add(errorChar, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    add(nilColorTitle, gbc);
    gbc.gridx++;
    nilColor = new ColorChooserButton(window, AppPreferences.NIL_COLOR);
    add(nilColor, gbc);
    gbc.gridx++;
    add(dontCareCharTitle, gbc);
    gbc.gridx++;
    add(dontCareChar, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    add(busColorTitle, gbc);
    gbc.gridx++;
    busColor = new ColorChooserButton(window, AppPreferences.BUS_COLOR);
    add(busColor, gbc);
    gbc.gridx++;
    add(highlightColorTitle, gbc);
    gbc.gridx++;
    highlightColor = new ColorChooserButton(window, AppPreferences.STROKE_COLOR);
    add(highlightColor, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    add(widthErrorColorTitle, gbc);
    gbc.gridx++;
    widthErrorColor = new ColorChooserButton(window, AppPreferences.WIDTH_ERROR_COLOR);
    add(widthErrorColor, gbc);
    gbc.gridx++;
    add(widthErrorCaptionColorTitle, gbc);
    gbc.gridx++;
    widthErrorCaptionColor =
        new ColorChooserButton(window, AppPreferences.WIDTH_ERROR_CAPTION_COLOR);
    add(widthErrorCaptionColor, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    add(widthErrorHighlightColorTitle, gbc);
    gbc.gridx++;
    widthErrorHighlightColor =
        new ColorChooserButton(window, AppPreferences.WIDTH_ERROR_HIGHLIGHT_COLOR);
    add(widthErrorHighlightColor, gbc);
    gbc.gridx++;
    add(widthErrorBackgroundColorTitle, gbc);
    gbc.gridx++;
    widthErrorBackgroundColor =
        new ColorChooserButton(window, AppPreferences.WIDTH_ERROR_BACKGROUND_COLOR);
    add(widthErrorBackgroundColor, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    add(clockFrequencyColorTitle, gbc);
    gbc.gridx++;
    clockFrequencyColor = new ColorChooserButton(window, AppPreferences.CLOCK_FREQUENCY_COLOR);
    add(clockFrequencyColor, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.gridwidth = 4;
    add(kmapColorsTitle, gbc);

    gbc.gridy++;
    gbc.gridwidth = 1;
    add(kmap1ColorTitle, gbc);
    gbc.gridx++;
    kmap1Color = new ColorChooserButton(window, AppPreferences.KMAP1_COLOR);
    add(kmap1Color, gbc);
    gbc.gridx++;
    add(kmap2ColorTitle, gbc);
    gbc.gridx++;
    kmap2Color = new ColorChooserButton(window, AppPreferences.KMAP2_COLOR);
    add(kmap2Color, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    add(kmap3ColorTitle, gbc);
    gbc.gridx++;
    kmap3Color = new ColorChooserButton(window, AppPreferences.KMAP3_COLOR);
    add(kmap3Color, gbc);
    gbc.gridx++;
    add(kmap4ColorTitle, gbc);
    gbc.gridx++;
    kmap4Color = new ColorChooserButton(window, AppPreferences.KMAP4_COLOR);
    add(kmap4Color, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    add(kmap5ColorTitle, gbc);
    gbc.gridx++;
    kmap5Color = new ColorChooserButton(window, AppPreferences.KMAP5_COLOR);
    add(kmap5Color, gbc);
    gbc.gridx++;
    add(kmap6ColorTitle, gbc);
    gbc.gridx++;
    kmap6Color = new ColorChooserButton(window, AppPreferences.KMAP6_COLOR);
    add(kmap6Color, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    add(kmap7ColorTitle, gbc);
    gbc.gridx++;
    kmap7Color = new ColorChooserButton(window, AppPreferences.KMAP7_COLOR);
    add(kmap7Color, gbc);
    gbc.gridx++;
    add(kmap8ColorTitle, gbc);
    gbc.gridx++;
    kmap8Color = new ColorChooserButton(window, AppPreferences.KMAP8_COLOR);
    add(kmap8Color, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    add(kmap9ColorTitle, gbc);
    gbc.gridx++;
    kmap9Color = new ColorChooserButton(window, AppPreferences.KMAP9_COLOR);
    add(kmap9Color, gbc);
    gbc.gridx++;
    add(kmap10ColorTitle, gbc);
    gbc.gridx++;
    kmap10Color = new ColorChooserButton(window, AppPreferences.KMAP10_COLOR);
    add(kmap10Color, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    add(kmap11ColorTitle, gbc);
    gbc.gridx++;
    kmap11Color = new ColorChooserButton(window, AppPreferences.KMAP11_COLOR);
    add(kmap11Color, gbc);
    gbc.gridx++;
    add(kmap12ColorTitle, gbc);
    gbc.gridx++;
    kmap12Color = new ColorChooserButton(window, AppPreferences.KMAP12_COLOR);
    add(kmap12Color, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    add(kmap13ColorTitle, gbc);
    gbc.gridx++;
    kmap13Color = new ColorChooserButton(window, AppPreferences.KMAP13_COLOR);
    add(kmap13Color, gbc);
    gbc.gridx++;
    add(kmap14ColorTitle, gbc);
    gbc.gridx++;
    kmap14Color = new ColorChooserButton(window, AppPreferences.KMAP14_COLOR);
    add(kmap14Color, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    add(kmap15ColorTitle, gbc);
    gbc.gridx++;
    kmap15Color = new ColorChooserButton(window, AppPreferences.KMAP15_COLOR);
    add(kmap15Color, gbc);
    gbc.gridx++;
    add(kmap16ColorTitle, gbc);
    gbc.gridx++;
    kmap16Color = new ColorChooserButton(window, AppPreferences.KMAP16_COLOR);
    add(kmap16Color, gbc);

    // Setup edit callbacks for wire swatches
    ColorChooserButton[] swatches = {
      trueColor, falseColor, unknownColor, errorColor, nilColor, busColor,
      highlightColor, widthErrorColor, widthErrorCaptionColor, widthErrorHighlightColor,
      widthErrorBackgroundColor, clockFrequencyColor
    };
    for (var b : swatches) {
      b.setOnEditRequested(btn -> switchToCustomAndEdit(btn));
    }

    localeChanged();
    updateSwatchesAndPreview();
  }

  private boolean isUpdatingDropdown = false;

  private void syncDropdownSelection() {
    isUpdatingDropdown = true;
    try {
      String current = AppPreferences.COLOR_SCHEME.get();
      if (ColorSchemeRegistry.SCHEME_CLASSIC.equalsIgnoreCase(current)) {
        schemeDropdown.setSelectedItem(ColorSchemeRegistry.DISPLAY_CLASSIC);
      } else if (ColorSchemeRegistry.SCHEME_COLORBLIND.equalsIgnoreCase(current)) {
        schemeDropdown.setSelectedItem(ColorSchemeRegistry.DISPLAY_COLORBLIND);
      } else if (ColorSchemeRegistry.SCHEME_CUSTOM.equalsIgnoreCase(current)) {
        schemeDropdown.setSelectedItem(ColorSchemeRegistry.DISPLAY_CUSTOM);
      } else {
        schemeDropdown.setSelectedItem(ColorSchemeRegistry.DISPLAY_MODERN);
      }
    } finally {
      isUpdatingDropdown = false;
    }
  }

  private void onSchemeDropdownChanged() {
    if (isUpdatingDropdown) {
      return;
    }
    String selected = (String) schemeDropdown.getSelectedItem();
    if (ColorSchemeRegistry.DISPLAY_CLASSIC.equals(selected)) {
      AppPreferences.COLOR_SCHEME.set(ColorSchemeRegistry.SCHEME_CLASSIC);
    } else if (ColorSchemeRegistry.DISPLAY_COLORBLIND.equals(selected)) {
      AppPreferences.COLOR_SCHEME.set(ColorSchemeRegistry.SCHEME_COLORBLIND);
    } else if (ColorSchemeRegistry.DISPLAY_CUSTOM.equals(selected)) {
      AppPreferences.COLOR_SCHEME.set(ColorSchemeRegistry.SCHEME_CUSTOM);
    } else {
      AppPreferences.COLOR_SCHEME.set(ColorSchemeRegistry.SCHEME_MODERN);
    }
    updateSwatchesAndPreview();
    notifyCanvasesRepaint();
  }

  private ColorScheme getPreviewScheme() {
    String selected = (String) schemeDropdown.getSelectedItem();
    String schemeId = ColorSchemeRegistry.SCHEME_MODERN;
    if (ColorSchemeRegistry.DISPLAY_CLASSIC.equals(selected)) {
      schemeId = ColorSchemeRegistry.SCHEME_CLASSIC;
    } else if (ColorSchemeRegistry.DISPLAY_COLORBLIND.equals(selected)) {
      schemeId = ColorSchemeRegistry.SCHEME_COLORBLIND;
    } else if (ColorSchemeRegistry.DISPLAY_CUSTOM.equals(selected)) {
      schemeId = ColorSchemeRegistry.SCHEME_CUSTOM;
    }
    return ColorSchemeRegistry.getColorScheme(schemeId, previewIsDark);
  }

  private void updateSwatchesAndPreview() {
    ColorScheme scheme = getPreviewScheme();
    String schemeId = AppPreferences.COLOR_SCHEME.get();
    boolean isCustom = ColorSchemeRegistry.SCHEME_CUSTOM.equalsIgnoreCase(schemeId);

    trueColor.setDisplayColor(isCustom ? null : scheme.trueColor());
    falseColor.setDisplayColor(isCustom ? null : scheme.falseColor());
    unknownColor.setDisplayColor(isCustom ? null : scheme.unknownColor());
    errorColor.setDisplayColor(isCustom ? null : scheme.errorColor());
    nilColor.setDisplayColor(isCustom ? null : scheme.unconnectedColor());
    busColor.setDisplayColor(isCustom ? null : scheme.busColor());
    highlightColor.setDisplayColor(isCustom ? null : scheme.strokeColor());
    widthErrorColor.setDisplayColor(isCustom ? null : scheme.mismatchColor());
    widthErrorCaptionColor.setDisplayColor(isCustom ? null : scheme.widthErrorCaptionColor());
    widthErrorHighlightColor.setDisplayColor(isCustom ? null : scheme.widthErrorHighlightColor());
    widthErrorBackgroundColor.setDisplayColor(isCustom ? null : scheme.widthErrorBackgroundColor());
    clockFrequencyColor.setDisplayColor(isCustom ? null : scheme.clockFrequencyColor());

    boolean readOnly = !isCustom;
    ColorChooserButton[] swatches = {
      trueColor, falseColor, unknownColor, errorColor, nilColor, busColor,
      highlightColor, widthErrorColor, widthErrorCaptionColor, widthErrorHighlightColor,
      widthErrorBackgroundColor, clockFrequencyColor
    };
    for (var b : swatches) {
      b.setReadOnly(readOnly);
    }

    editColorsButton.setEnabled(!isCustom);
    resetModernButton.setEnabled(!ColorSchemeRegistry.SCHEME_MODERN.equalsIgnoreCase(schemeId));

    boolean isClassicDark = ColorSchemeRegistry.SCHEME_CLASSIC.equalsIgnoreCase(schemeId) && previewIsDark;
    classicNoticeLabel.setVisible(isClassicDark);

    wirePreviewStrip.repaint();
    repaint();
  }

  private void switchToCustomAndEdit(ColorChooserButton target) {
    ColorScheme current = getPreviewScheme();
    ColorSchemeRegistry.copySchemeToCustom(current);
    AppPreferences.COLOR_SCHEME.set(ColorSchemeRegistry.SCHEME_CUSTOM);
    syncDropdownSelection();
    updateSwatchesAndPreview();
    notifyCanvasesRepaint();
    if (target != null) {
      target.triggerEditDialog();
    }
  }

  private void notifyCanvasesRepaint() {
    Value.updateColors();
    for (final var proj : Projects.getOpenProjects()) {
      if (proj.getFrame() != null && proj.getFrame().getCanvas() != null) {
        proj.getFrame().getCanvas().repaint();
      }
    }
  }

  @Override
  public String getHelpText() {
    return S.get("simHelp");
  }

  @Override
  public String getTitle() {
    return S.get("simTitle");
  }

  @Override
  public void localeChanged() {
    trueColorTitle.setText(S.get("simTrueColTitle"));
    trueCharTitle.setText(S.get("simTrueCharTitle"));
    falseColorTitle.setText(S.get("simFalseColTitle"));
    falseCharTitle.setText(S.get("simFalseCharTitle"));
    unknownColorTitle.setText(S.get("simUnknownColTitle"));
    unknownCharTitle.setText(S.get("simUnknownCharTitle"));
    errorColorTitle.setText(S.get("simErrorColTitle"));
    errorCharTitle.setText(S.get("simErrorCharTitle"));
    nilColorTitle.setText(S.get("simNilColTitle"));
    dontCareCharTitle.setText(S.get("simDontCareCharTitle"));
    busColorTitle.setText(S.get("simBusColTitle"));
    highlightColorTitle.setText(S.get("simStrokeColTitle"));
    widthErrorColorTitle.setText(S.get("simWidthErrorTitle"));
    widthErrorCaptionColorTitle.setText(S.get("simWidthErrorCaptionTitle"));
    widthErrorHighlightColorTitle.setText(S.get("simWidthErrorHighlightTitle"));
    widthErrorBackgroundColorTitle.setText(S.get("simWidthErrorBackgroundTitle"));
    clockFrequencyColorTitle.setText(S.get("simClockFrequencyTitle"));
    kmap1ColorTitle.setText(S.get("simKmapColors", 1));
    kmap2ColorTitle.setText(S.get("simKmapColors", 2));
    kmap3ColorTitle.setText(S.get("simKmapColors", 3));
    kmap4ColorTitle.setText(S.get("simKmapColors", 4));
    kmap5ColorTitle.setText(S.get("simKmapColors", 5));
    kmap6ColorTitle.setText(S.get("simKmapColors", 6));
    kmap7ColorTitle.setText(S.get("simKmapColors", 7));
    kmap8ColorTitle.setText(S.get("simKmapColors", 8));
    kmap9ColorTitle.setText(S.get("simKmapColors", 9));
    kmap10ColorTitle.setText(S.get("simKmapColors", 10));
    kmap11ColorTitle.setText(S.get("simKmapColors", 11));
    kmap12ColorTitle.setText(S.get("simKmapColors", 12));
    kmap13ColorTitle.setText(S.get("simKmapColors", 13));
    kmap14ColorTitle.setText(S.get("simKmapColors", 14));
    kmap15ColorTitle.setText(S.get("simKmapColors", 15));
    kmap16ColorTitle.setText(S.get("simKmapColors", 16));
    kmapColorsTitle.setText(S.get("simKmapColorsTitle"));
  }

  private class WirePreviewStrip extends JPanel {
    public WirePreviewStrip() {
      setPreferredSize(new Dimension(420, 36));
      setMinimumSize(new Dimension(300, 36));
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      var g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

      boolean dark = previewIsDark;
      Color canvasBg = dark ? new Color(0x1F, 0x1F, 0x1F) : Color.WHITE;
      g2.setColor(canvasBg);
      g2.fillRect(0, 0, getWidth(), getHeight());

      g2.setColor(dark ? new Color(0x4D, 0x51, 0x5A) : new Color(0xC8, 0xCC, 0xD2));
      g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

      ColorScheme scheme = getPreviewScheme();
      Object[][] samples = {
        {"0", scheme.falseColor(), 1},
        {"1", scheme.trueColor(), 1},
        {"U", scheme.unknownColor(), 1},
        {"E", scheme.errorColor(), 1},
        {"Mis", scheme.mismatchColor(), 1},
        {"Bus", scheme.busColor(), 3},
        {"Nil", scheme.unconnectedColor(), 1}
      };

      int count = samples.length;
      int colWidth = getWidth() / count;
      Font font = new Font("SansSerif", Font.PLAIN, 10);
      g2.setFont(font);

      for (int i = 0; i < count; i++) {
        int x0 = i * colWidth;
        String label = (String) samples[i][0];
        Color color = (Color) samples[i][1];
        int strokeWidth = (Integer) samples[i][2];

        // Label
        g2.setColor(dark ? new Color(0xAC, 0xAC, 0xAC) : new Color(0x5A, 0x5D, 0x66));
        var fm = g2.getFontMetrics();
        int labelW = fm.stringWidth(label);
        g2.drawString(label, x0 + (colWidth - labelW) / 2, 12);

        // Wire line
        g2.setColor(color);
        g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int y = 24;
        g2.drawLine(x0 + 8, y, x0 + colWidth - 8, y);
      }
      g2.dispose();
    }
  }

  private class MyListener implements PreferenceChangeListener {
    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
      SwingUtilities.invokeLater(() -> {
        syncDropdownSelection();
        updateSwatchesAndPreview();
        notifyCanvasesRepaint();
      });
    }
  }

  private static class SymbolChooser extends JComboBox<Character> {
    private static final long serialVersionUID = 1L;
    private final PrefMonitor<String> myPref;

    public SymbolChooser(PrefMonitor<String> pref, String choices) {
      super();
      myPref = pref;
      this.addActionListener(new MyactionListener());
      final Character def = pref.get().charAt(0);
      var seldef = -1;
      for (var i = 0; i < choices.length(); i++) {
        final Character sel = choices.charAt(i);
        if (sel.equals(def)) seldef = i;
        this.addItem(sel);
      }
      if (seldef >= 0) this.setSelectedIndex(seldef);
    }

    private class MyactionListener implements ActionListener {
      @Override
      public void actionPerformed(ActionEvent e) {
        @SuppressWarnings("unchecked")
        final var me = (JComboBox<Character>) e.getSource();
        final Character s = (Character) me.getSelectedItem();
        if (s != myPref.get().charAt(0)) {
          myPref.set(Character.toString(s));
        }
      }
    }
  }
}
