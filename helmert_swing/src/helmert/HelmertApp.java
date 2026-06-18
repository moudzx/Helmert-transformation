package helmert;

import helmert.Geodesy.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class HelmertApp extends JFrame {

    // Technical GIS/Geography Mapping Dashboard Palette
    private static final Color C_BG        = new Color(236, 239, 241); // Map canvas grey (Slate 100)
    private static final Color C_SIDEBAR   = Color.WHITE;
    private static final Color C_TEXT      = new Color(38, 50, 56);    // Deep Charcoal (Blue Grey 900)
    private static final Color C_SUBTEXT   = new Color(120, 144, 156); // Muted Tech Grey (Blue Grey 400)
    private static final Color C_DIVIDER   = new Color(207, 216, 220); // Border Grey (Blue Grey 200)
    private static final Color C_FIELD_BG  = new Color(245, 247, 248); // Crisp data field background
    private static final Color C_SEC_BG    = new Color(230, 235, 238); // Section block headers
    private static final Color C_BORDER    = new Color(176, 190, 197); // Active field borders
    private static final Color C_RESULT_BG = Color.WHITE;
    private static final Color C_BTN_NAVY  = new Color(55, 71, 79);    // Map UI Primary Button (Blue Grey 800)

    // Clear Technical Topography Typography
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_VALUE  = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_MONO   = new Font("Consolas", Font.BOLD, 14);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_SECTION= new Font("Segoe UI", Font.BOLD, 11);

    private JTextField fLat, fLon, fH;
    private JTextField fTx, fTy, fTz, fRx, fRy, fRz, fS;
    private JComboBox<Ellipsoid> cbSrc, cbDst;
    private JTextField fSrcA, fSrcF, fDstA, fDstF;
    private JPanel customSrcPanel, customDstPanel;
    private JPanel resultsPanel;

    public HelmertApp() {
        super("Helmert 7-Parameter Coordinate Transformation");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 750));
        setSize(1200, 820);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_BG);

        root.add(buildSidebarLayout(), BorderLayout.WEST);
        root.add(buildResultsPanel(), BorderLayout.CENTER);

        setContentPane(root);
        setLocationRelativeTo(null);
    }

    private JPanel buildSidebarLayout() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(C_SIDEBAR);
        container.setPreferredSize(new Dimension(450, 0));
        container.setBorder(new MatteBorder(0, 0, 0, 1, C_DIVIDER));

        container.add(buildTitleBar(), BorderLayout.NORTH);

        // Core Scroller implementation to guarantee visibility of the button below
        JPanel scrollableContent = new JPanel();
        scrollableContent.setLayout(new BoxLayout(scrollableContent, BoxLayout.Y_AXIS));
        scrollableContent.setBackground(C_SIDEBAR);

        scrollableContent.add(buildSection("SOURCE COORDINATES (GEOGRAPHIC)", buildCoordPanel()));
        scrollableContent.add(buildSection("HELMERT 7-PARAMETER DATA", buildHelmertPanel()));
        scrollableContent.add(buildSection("ELLIPSOID DATUM SPECIFICATIONS", buildEllipsoidPanel()));
        scrollableContent.add(Box.createVerticalGlue());

        JScrollPane sidebarScroll = new JScrollPane(scrollableContent);
        sidebarScroll.setBorder(null);
        sidebarScroll.getVerticalScrollBar().setUnitIncrement(12);
        sidebarScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        container.add(sidebarScroll, BorderLayout.CENTER);
        container.add(buildTransformButton(), BorderLayout.SOUTH);

        return container;
    }

    private JPanel buildTitleBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 20));
        p.setBackground(C_SIDEBAR);
        p.setBorder(new MatteBorder(0, 0, 1, 0, C_DIVIDER));

        JLabel title = new JLabel("Helmert");
        title.setFont(FONT_TITLE);
        title.setForeground(C_TEXT);

        JLabel sub = new JLabel("7-Parameter Coordinate Transformation");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(C_SUBTEXT);

        p.add(title);
        p.add(sub);
        return p;
    }

    private JPanel buildSection(String title, JPanel content) {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(C_SIDEBAR);
        wrapper.setAlignmentX(LEFT_ALIGNMENT);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        header.setBackground(C_SEC_BG);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        header.setBorder(new MatteBorder(0, 0, 1, 0, C_DIVIDER));

        JLabel lbl = new JLabel(title);
        lbl.setFont(FONT_SECTION);
        lbl.setForeground(C_TEXT);
        header.add(lbl);

        wrapper.add(header);
        wrapper.add(content);
        return wrapper;
    }

    private JPanel buildCoordPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(C_SIDEBAR);
        p.setBorder(new EmptyBorder(12, 24, 16, 24));

        fLat = makeField("33.888630");
        fLon = makeField("35.495480");
        fH   = makeField("100.0");

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        addLabeledField(p, g, "Latitude (decimal degrees)", fLat, 0);
        addLabeledField(p, g, "Longitude (decimal degrees)", fLon, 1);
        addLabeledField(p, g, "Ellipsoidal Height h (m)", fH, 2);

        return p;
    }

    private JPanel buildHelmertPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(C_SIDEBAR);
        p.setBorder(new EmptyBorder(12, 24, 16, 24));

        fTx = makeField("-199.87");
        fTy = makeField("74.79");
        fTz = makeField("246.62");
        fRx = makeField("0.0");
        fRy = makeField("0.0");
        fRz = makeField("0.0");
        fS  = makeField("0.0");

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 4, 4, 4);

        addTripleRow(p, g, 0, "Tx (m)", fTx, "Ty (m)", fTy, "Tz (m)", fTz);
        addTripleRow(p, g, 2, "Rx (arcsec)", fRx, "Ry (arcsec)", fRy, "Rz (arcsec)", fRz);

        g.gridx = 0; g.gridy = 4; g.gridwidth = 3; g.weightx = 1;
        g.insets = new Insets(8, 4, 4, 4);
        p.add(labeledField("Scale Correction s (ppm)", fS), g);

        return p;
    }

    private JPanel buildEllipsoidPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(C_SIDEBAR);
        p.setBorder(new EmptyBorder(12, 24, 16, 24));

        cbSrc = makeCombo();
        cbDst = makeCombo();

        fSrcA = makeField("6378137.0");
        fSrcF = makeField("298.257223563");
        fDstA = makeField("6378137.0");
        fDstF = makeField("298.257223563");

        customSrcPanel = makeCustomEllipsoidRow(fSrcA, fSrcF);
        customDstPanel = makeCustomEllipsoidRow(fDstA, fDstF);

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.insets = new Insets(4, 0, 4, 0);

        g.gridx = 0; g.gridy = 0;
        p.add(labeledField("Source Reference Ellipsoid", cbSrc), g);
        g.gridy = 1;
        p.add(customSrcPanel, g);
        g.gridy = 2;
        p.add(labeledField("Target Reference Ellipsoid", cbDst), g);
        g.gridy = 3;
        p.add(customDstPanel, g);

        cbSrc.addActionListener(e -> {
            customSrcPanel.setVisible(cbSrc.getSelectedIndex() == 5);
            p.revalidate();
        });
        cbDst.addActionListener(e -> {
            customDstPanel.setVisible(cbDst.getSelectedIndex() == 5);
            p.revalidate();
        });

        customSrcPanel.setVisible(false);
        customDstPanel.setVisible(false);

        return p;
    }

    private JPanel makeCustomEllipsoidRow(JTextField fA, JTextField fF) {
        JPanel p = new JPanel(new GridLayout(1, 2, 12, 0));
        p.setBackground(C_SIDEBAR);
        p.setBorder(new EmptyBorder(4, 0, 4, 0));
        p.add(labeledField("Semi-major axis a (m)", fA));
        p.add(labeledField("Inverse flattening 1/f", fF));
        return p;
    }

    private JPanel buildTransformButton() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_SIDEBAR);
        p.setBorder(new EmptyBorder(16, 24, 24, 24));

        JButton btn = new JButton("Transform Coordinates") {
            @Override
            protected void paintComponent(Graphics g2) {
                Graphics2D g = (Graphics2D) g2;
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(getModel().isRollover() ? new Color(38, 50, 56) : C_BTN_NAVY);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g.setColor(Color.WHITE);
                g.setFont(getFont());
                FontMetrics fm = g.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g.drawString(getText(), tx, ty);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(0, 46));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> runTransform());

        p.add(btn, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildResultsPanel() {
        resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setBackground(C_BG);
        resultsPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel header = new JLabel("Transformation Registry Output");
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(C_TEXT);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel innerHint = new JPanel(new GridBagLayout());
        innerHint.setBackground(C_BG);
        JLabel hint = new JLabel("Await execution. Run mapping configurations to generate geodetic tracking records.");
        hint.setFont(FONT_VALUE);
        hint.setForeground(C_SUBTEXT);
        innerHint.add(hint);

        resultsPanel.add(header, BorderLayout.NORTH);
        resultsPanel.add(innerHint, BorderLayout.CENTER);

        return resultsPanel;
    }

    private void runTransform() {
        try {
            double lat = Double.parseDouble(fLat.getText().trim());
            double lon = Double.parseDouble(fLon.getText().trim());
            double h   = Double.parseDouble(fH.getText().trim());

            HelmertParams params = new HelmertParams();
            params.Tx = Double.parseDouble(fTx.getText().trim());
            params.Ty = Double.parseDouble(fTy.getText().trim());
            params.Tz = Double.parseDouble(fTz.getText().trim());
            params.Rx = Double.parseDouble(fRx.getText().trim());
            params.Ry = Double.parseDouble(fRy.getText().trim());
            params.Rz = Double.parseDouble(fRz.getText().trim());
            params.s  = Double.parseDouble(fS.getText().trim());

            Ellipsoid srcE = (Ellipsoid) cbSrc.getSelectedItem();
            Ellipsoid dstE = (Ellipsoid) cbDst.getSelectedItem();

            if (cbSrc.getSelectedIndex() == 5) {
                srcE = new Ellipsoid("Custom", Double.parseDouble(fSrcA.getText().trim()), Double.parseDouble(fSrcF.getText().trim()));
            }
            if (cbDst.getSelectedIndex() == 5) {
                dstE = new Ellipsoid("Custom", Double.parseDouble(fDstA.getText().trim()), Double.parseDouble(fDstF.getText().trim()));
            }

            LLH srcLLH = new LLH(Math.toRadians(lat), Math.toRadians(lon), h);
            Result result = Geodesy.transform(srcLLH, srcE, dstE, params);

            showResults(result);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Numeric runtime parser configuration error: " + ex.getMessage(),
                    "Format Parsing Deviation", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showResults(Result r) {
        resultsPanel.removeAll();

        JLabel header = new JLabel("Transformation Registry Output");
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(C_TEXT);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));
        resultsPanel.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(C_BG);

        double srcLat = Math.toDegrees(r.srcLLH.lat);
        double srcLon = Math.toDegrees(r.srcLLH.lon);
        double dstLat = Math.toDegrees(r.dstLLH.lat);
        double dstLon = Math.toDegrees(r.dstLLH.lon);
        double dLat = (dstLat - srcLat) * 3600.0;
        double dLon = (dstLon - srcLon) * 3600.0;
        double dH   = r.dstLLH.h - r.srcLLH.h;

        content.add(buildResultSection("SOURCE GRID PARAMETERS", new String[][]{
            {"Geographic Notation", null},
            {"Latitude",   String.format("%.8f°", srcLat)},
            {"Longitude",  String.format("%.8f°", srcLon)},
            {"Height (h)", String.format("%.4f m",   r.srcLLH.h)},
            {"Geocentric Cartesian Coordinates (ECEF)", null},
            {"X",     String.format("%.4f m",   r.srcXYZ.x)},
            {"Y",     String.format("%.4f m",   r.srcXYZ.y)},
            {"Z",     String.format("%.4f m",   r.srcXYZ.z)},
        }));

        content.add(Box.createVerticalStrut(16));

        content.add(buildResultSection("TARGET GRID TRANSLATION", new String[][]{
            {"Geographic Notation", null},
            {"Latitude",   String.format("%.8f°", dstLat)},
            {"Longitude",  String.format("%.8f°", dstLon)},
            {"Height (h)", String.format("%.4f m",   r.dstLLH.h)},
            {"Geocentric Cartesian Coordinates (ECEF)", null},
            {"X",     String.format("%.4f m",   r.dstXYZ.x)},
            {"Y",     String.format("%.4f m",   r.dstXYZ.y)},
            {"Z",     String.format("%.4f m",   r.dstXYZ.z)},
        }));

        content.add(Box.createVerticalStrut(16));

        content.add(buildResultSection("DELTA TRANSLATION RESIDUALS", new String[][]{
            {"Latitude Offset",  String.format("%+.4f arcsec", dLat)},
            {"Longitude Offset", String.format("%+.4f arcsec", dLon)},
            {"Height Offset",    String.format("%+.4f m",      dH)},
        }));

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setBackground(C_BG);
        scroll.getViewport().setBackground(C_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        resultsPanel.add(scroll, BorderLayout.CENTER);
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private JPanel buildResultSection(String title, String[][] rows) {
        // Map style card adjustments: Completely removed colored edge strips
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(C_RESULT_BG);
        card.setBorder(new CompoundBorder(
            new MatteBorder(1, 1, 1, 1, C_DIVIDER),
            new EmptyBorder(20, 24, 20, 24)
        ));

        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.WEST;

        // Clean fix for the layout "hallucination": explicit scoping parameters per block context row
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2; g.weightx = 1.0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 12, 0);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_HEADER);
        titleLabel.setForeground(C_TEXT);
        card.add(titleLabel, g);

        int rowIdx = 1;
        for (String[] entry : rows) {
            if (entry[1] == null) {
                // Secondary Segment Layout Headers
                g.gridx = 0; g.gridy = rowIdx; g.gridwidth = 2; g.weightx = 1.0;
                g.fill = GridBagConstraints.HORIZONTAL;
                g.insets = new Insets(16, 0, 6, 0);
                JLabel subHeader = new JLabel(entry[0].toUpperCase());
                subHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
                subHeader.setForeground(C_SUBTEXT);
                card.add(subHeader, g);
            } else {
                // Guaranteed safety isolation reset to prevent GridBag grouping overlapping
                g.gridwidth = 1;
                g.fill = GridBagConstraints.NONE;

                // Technical data fields mapping
                g.gridx = 0; g.gridy = rowIdx; g.weightx = 0.0;
                g.insets = new Insets(6, 0, 6, 24);
                JLabel lbl = new JLabel(entry[0]);
                lbl.setFont(FONT_LABEL);
                lbl.setForeground(C_SUBTEXT);
                lbl.setPreferredSize(new Dimension(190, 22));
                card.add(lbl, g);

                g.gridx = 1; g.weightx = 1.0; g.fill = GridBagConstraints.HORIZONTAL;
                g.insets = new Insets(6, 0, 6, 0);
                JLabel val = new JLabel(entry[1]);
                val.setFont(FONT_MONO);
                val.setForeground(C_TEXT);
                card.add(val, g);
            }
            rowIdx++;
        }

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(C_BG);
        wrapper.setBorder(new EmptyBorder(0, 0, 6, 0));
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    private JTextField makeField(String value) {
        JTextField f = new JTextField(value) {
            @Override
            protected void paintComponent(Graphics g2) {
                Graphics2D g = (Graphics2D) g2;
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(getBackground());
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 4, 4);
                super.paintComponent(g2);
            }
            @Override
            protected void paintBorder(Graphics g2) {
                Graphics2D g = (Graphics2D) g2;
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(isFocusOwner() ? C_TEXT : C_DIVIDER);
                g.setStroke(new BasicStroke(isFocusOwner() ? 1.5f : 1.0f));
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 4, 4);
            }
        };
        f.setFont(FONT_VALUE);
        f.setBackground(C_FIELD_BG);
        f.setForeground(C_TEXT);
        f.setCaretColor(C_TEXT);
        f.setBorder(new EmptyBorder(6, 10, 6, 10));
        f.setOpaque(false);
        f.setPreferredSize(new Dimension(0, 36));
        return f;
    }

    private JComboBox<Ellipsoid> makeCombo() {
        JComboBox<Ellipsoid> cb = new JComboBox<>(Geodesy.PRESETS);
        cb.setFont(FONT_VALUE);
        cb.setBackground(C_FIELD_BG);
        cb.setForeground(C_TEXT);
        cb.setPreferredSize(new Dimension(0, 36));
        return cb;
    }

    private JPanel labeledField(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setBackground(C_SIDEBAR);
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(C_TEXT);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void addLabeledField(JPanel panel, GridBagConstraints g, String label, JTextField field, int row) {
        g.gridx = 0; g.gridy = row; g.gridwidth = 1; g.weightx = 1;
        g.insets = new Insets(row == 0 ? 0 : 12, 0, 0, 0);
        panel.add(labeledField(label, field), g);
    }

    private void addTripleRow(JPanel panel, GridBagConstraints g, int startRow,
                               String l1, JTextField f1,
                               String l2, JTextField f2,
                               String l3, JTextField f3) {
        g.gridy = startRow; g.gridwidth = 1;
        g.weightx = 1.0 / 3.0;
        g.gridx = 0; panel.add(labeledField(l1, f1), g);
        g.gridx = 1; panel.add(labeledField(l2, f2), g);
        g.gridx = 2; panel.add(labeledField(l3, f3), g);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        UIManager.put("ComboBox.background", C_FIELD_BG);
        UIManager.put("ComboBox.foreground", C_TEXT);
        UIManager.put("ComboBox.selectionBackground", new Color(207, 216, 220));
        UIManager.put("ComboBox.selectionForeground", C_TEXT);

        SwingUtilities.invokeLater(() -> new HelmertApp().setVisible(true));
    }
}