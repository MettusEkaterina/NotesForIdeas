package ui;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import javax.swing.*;
import java.awt.*;

public class NotesPanel {
    private JTextPane pane;
    private JPanel panel1;
    private JScrollPane noteScroller;
    private Color currentFontColor = new JBColor(new Color(0, 0, 0), new Color(0, 0, 0));
    private Color currentBackgroundColor = new JBColor(new Color(255, 255, 255), new Color(255, 255, 255));
    private Font currentFont = new Font("Arial", Font.PLAIN, 16);

    public NotesPanel() {
        pane.setMargin(JBUI.insets(5, 10, 5, 10));
        pane.setFont(currentFont);
        pane.setForeground(currentFontColor);
        pane.setBackground(currentBackgroundColor);
    }

    public JComponent getRootComponent() {
        return panel1;
    }
}