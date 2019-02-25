package ui;

import com.intellij.openapi.actionSystem.*;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import manager.NotesManager;
import org.jdom.Element;
import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NotesPanel {
    private String id;
    private JPanel panel1;
    private JTextPane pane;
    private JScrollPane noteScroller;
    private JLabel labelNoteTitle;
    private JPanel actionPanel;
    private JComboBox comboBoxFontSize;
    private JComboBox comboBoxFont;
    private JComboBox comboBoxColor;
    private JToolBar noteToolBar;
    public Element element;
    private int selectedIndex;
    private Element selectedNote;
    private NotesManager notesManager;
    private JEditorPane searchPane;
    public Color currentFontColor = new JBColor(new Color(0, 0, 0), new Color(0, 0, 0));
    private Color currentBackgroundColor;
    public static SimpleDateFormat sdf = new SimpleDateFormat();

    public NotesPanel(final Element element) {
        notesManager = NotesManager.getInstance();
        id = notesManager.getNextPanelID();
        this.element = element;
        final ActionManager actionManager = ActionManager.getInstance();
        final DefaultActionGroup dag = new DefaultActionGroup();

        String currentFontName = notesManager.getNotesFont().getFontName();
        String currentFontSize = String.valueOf(notesManager.getNotesFont().getSize());
        String currentColorName = String.valueOf(notesManager.getColorName());
        switch(currentColorName){
            case "yellow":
                currentBackgroundColor = new JBColor(new Color(253, 254, 192), new Color(253, 254, 192));
                break;
            case "green":
                currentBackgroundColor = new JBColor(new Color(221, 254, 212), new Color(221, 254, 212));
                break;
            case "pink":
                currentBackgroundColor = new JBColor(new Color(254, 224, 251), new Color(254, 224, 251));
                break;
            case "purple":
                currentBackgroundColor = new JBColor(new Color(233, 212, 254), new Color(233, 212, 254));
                break;
            case "blue":
                currentBackgroundColor = new JBColor(new Color(214, 239, 254), new Color(214, 239, 254));
                break;
            case "white":
                currentBackgroundColor = new JBColor(new Color(251, 251, 251), new Color(251, 251, 251));
                break;
        }

        String[] fontList = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String aFontList : fontList) {
            comboBoxFont.addItem(aFontList);
        }
        comboBoxFont.setSelectedItem(currentFontName);

        String[] fontSizes = {"8", "10", "11", "12", "14", "16", "18", "20", "24", "28", "32", "36", "40", "48", "52", "56", "64", "72", "92"};
        for (String fontSize : fontSizes) {
            comboBoxFontSize.addItem(fontSize);
        }
        comboBoxFontSize.setSelectedItem(currentFontSize);

        String[] noteColors = {"yellow", "green", "pink", "purple", "blue", "white"};
        for (String noteColor : noteColors) {
            comboBoxColor.addItem(noteColor);
        }
        comboBoxColor.setSelectedItem(currentColorName);

        comboBoxFont.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                notesManager.setNotesFont(new Font(String.valueOf(comboBoxFont.getSelectedItem()), Font.PLAIN, Integer.parseInt(String.valueOf(comboBoxFontSize.getSelectedItem()))));
                selectedNote = element.getChildren().get(getSelectedNoteIndex());
                selectedNote.setAttribute("fontname", String.valueOf(comboBoxFont.getSelectedItem()));
                notesManager.syncNotePanels(id);
            }
        });

        comboBoxFontSize.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                notesManager.setNotesFont(new Font(String.valueOf(comboBoxFont.getSelectedItem()), Font.PLAIN, Integer.parseInt(String.valueOf(comboBoxFontSize.getSelectedItem()))));
                selectedNote = element.getChildren().get(getSelectedNoteIndex());
                selectedNote.setAttribute("fontsize", String.valueOf(comboBoxFontSize.getSelectedItem()));
                notesManager.syncNotePanels(id);
            }
        });

        comboBoxColor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String choice = String.valueOf(comboBoxColor.getSelectedItem());
                setBackgroundColor(choice);
                notesManager.setColorName(choice);
                selectedNote = element.getChildren().get(getSelectedNoteIndex());
                selectedNote.setAttribute("colorname", choice);
                notesManager.syncNotePanels(id);
            }
        });

        dag.addSeparator();
        dag.add(new AnAction("Add Note", "Add Note", PluginIcons.ADD) {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                addNewNote("Enter your notes here...");
                noteToolBar.setVisible(true);
            }
        });
        dag.add(new AnAction("Delete Note", "Delete Note", PluginIcons.DELETE) {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                deleteNote();
            }
        });
        dag.add(new AnAction("Show All Notes", "Show All Notes", PluginIcons.LIST) {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                if (!noteToolBar.isVisible()) {
                    selectNote(getSelectedNoteIndex(), true);
                } else {
                    listAllNotes();
                    noteToolBar.setVisible(false);
                }
            }
        });

        final ActionToolbar actionToolbar = actionManager.createActionToolbar("plugin.NotesForIdeas", dag, true);
        final JComponent actionToolbarComponent = actionToolbar.getComponent();
        actionToolbar.setReservePlaceAutoPopupIcon(false);
        actionPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
        actionPanel.add(actionToolbarComponent);

        labelNoteTitle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        labelNoteTitle.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                renameNote();
            }
        });

        pane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                getSelectedNote().setText(pane.getText());
                notesManager.syncNoteText(id);
            }
        });

        pane.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (pane.getText().equals("Enter your notes here...")) {
                    pane.select(0, pane.getDocument().getLength());
                } else {
                    pane.setCaretPosition(0);
                }
            }
        });
        createPopupMenu();

        pane.setMargin(JBUI.insets(5, 10, 5, 10));
        selectedIndex = Integer.parseInt(element.getAttributeValue("selectednoteindex"));
        notesManager.addNotesPanel(this);
        selectNote(selectedIndex, true);
        pane.setFont(notesManager.getNotesFont());
        pane.setForeground(currentFontColor);
        pane.setBackground(currentBackgroundColor);

        panel1.addAncestorListener(new AncestorListener() {
            public void ancestorAdded(AncestorEvent event) {
            }

            public void ancestorRemoved(AncestorEvent event) {
                NotesManager.saveSettings(element);
            }

            public void ancestorMoved(AncestorEvent event) {
            }
        });
    }

    private void setSelectedNoteIndex(int index) {
        selectedIndex = index;
        element.setAttribute("selectednoteindex", String.valueOf(index));
    }

    public int getSelectedNoteIndex() {
        return selectedIndex;
    }

    private Element getSelectedNote() {
        return selectedNote;
    }

    public JComponent getRootComponent() {
        return panel1;
    }

    public void selectNote(int index, boolean requestFocus) {
        if (pane.getParent() == null) {
            noteScroller.getViewport().add(pane);
        }

        if (index >= 0 && index < element.getChildren().size()) {
            setSelectedNoteIndex(index);
            selectedNote = element.getChildren().get(index);
            pane.setText(selectedNote.getText());
            labelNoteTitle.setText(selectedNote.getAttributeValue("title"));
            pane.setFont(new Font(selectedNote.getAttributeValue("fontname"), Font.PLAIN, Integer.parseInt(selectedNote.getAttributeValue("fontsize"))));
            notesManager.setColorName(selectedNote.getAttributeValue("colorname"));
            setBackgroundColor(selectedNote.getAttributeValue("colorname"));
            comboBoxColor.setSelectedItem(selectedNote.getAttributeValue("colorname"));
            comboBoxFont.setSelectedItem(selectedNote.getAttributeValue("fontname"));
            comboBoxFontSize.setSelectedItem(selectedNote.getAttributeValue("fontsize"));
            if (requestFocus) {
                pane.requestFocus();
            }
        }

        noteToolBar.setVisible(true);
    }

    private void deleteNote() {
        if (element.getChildren().size() > 1) {
            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(panel1, "Are you sure you want to delete this Note?",
                    "Confirm Note Delete", JOptionPane.YES_NO_OPTION)) {
                if (getSelectedNoteIndex() >= 0 && getSelectedNoteIndex() < element.getChildren().size()) {
                    Element note = getSelectedNote();
                    element.removeContent(note);
                    if (getSelectedNoteIndex() > 0) {
                        setSelectedNoteIndex(getSelectedNoteIndex() - 1);
                    }
                    selectNote(getSelectedNoteIndex(), true);
                    notesManager.syncNotePanels(id);
                }
            }
        }
    }

    private void renameNote() {
        String notetitle = getSelectedNote().getAttributeValue("title");
        String title = JOptionPane.showInputDialog(panel1, "Please enter title for this Note", notetitle);
        if (title != null && title.length() > 0 && !title.equals(notetitle)) {
            getSelectedNote().setAttribute("title", title);
            labelNoteTitle.setText(title);
            notesManager.syncNotePanels(id);
        }
    }

    private void listAllNotes() {
        if (searchPane == null) {
            searchPane = new JEditorPane();
            searchPane.setEditable(false);
            searchPane.setContentType("text/html");
            searchPane.setMargin(JBUI.emptyInsets());
            searchPane.setBackground(JBColor.background());
            searchPane.addHyperlinkListener(new HyperlinkListener() {
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        setSelectedNoteIndex(Integer.parseInt(e.getURL().getHost()));
                        selectNote(selectedIndex, true);
                    }
                }
            });
        }
        if (searchPane.getParent() == null) {
            noteScroller.getViewport().add(searchPane);
        }

        int foregroundRed = JBColor.foreground().getRed();
        int foregroundGreen = JBColor.foreground().getGreen();
        int foregroundBlue = JBColor.foreground().getBlue();
        String foregroundColor = "rgb(" + foregroundRed + "," + foregroundGreen + "," + foregroundBlue + ")";
        boolean even = true;
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='margin:0'>");
        java.util.List list = element.getChildren();
        for (int i = 0; i < list.size(); i++) {
            Element e = (Element) list.get(i);
            String txt = e.getText();
            int end = 50;
            if (txt.length() < end) {
                end = txt.length();
            }
            txt = txt.substring(0, end);
            txt = txt.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br>");

            sb.append("<div style='padding:3px;border-bottom:1px solid ").append(foregroundColor).append(";'>");
            sb.append("<div style='font:bold 10px sans-serif'>");
            sb.append("<a href='http://").append(i).append("'>");
            sb.append(e.getAttributeValue("title")).append("</a></div>");
            sb.append("<table><tr><td style='font:normal 9px verdana;color:gray;padding-left:10px'>").append(txt).append("</td></tr></table>");
            sb.append("</div>");
            even = !even;
        }
        sb.append("</body></html>");
        searchPane.setText(sb.toString());

        labelNoteTitle.setText("All Notes");
    }

    public void addNewNote(String notes) {
        Element note = new Element("note");
        note.setAttribute("title", sdf.format(new Date()));
        note.setAttribute("fontname", "Arial");
        note.setAttribute("fontsize", "12");
        note.setAttribute("colorname", "yellow");
        note.setText(notes);
        element.addContent(note);
        selectNote(element.getChildren().size() - 1, true);
        notesManager.syncNotePanels(id);
    }

    public String getId() {
        return id;
    }

    public void createPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem cut = new JMenuItem(new DefaultEditorKit.CutAction());
        cut.setText("Cut");
        cut.setIcon(PluginIcons.CUT);
        cut.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                getSelectedNote().setText(pane.getText());
                notesManager.syncNoteText(id);
            }
        });

        JMenuItem copy = new JMenuItem(new DefaultEditorKit.CopyAction());
        copy.setText("Copy");
        copy.setIcon(PluginIcons.COPY);

        JMenuItem paste = new JMenuItem(new DefaultEditorKit.PasteAction());
        paste.setText("Paste");
        paste.setIcon(PluginIcons.PASTE);
        paste.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                getSelectedNote().setText(pane.getText());
                notesManager.syncNoteText(id);
            }
        });

        JMenuItem popupList = new JMenuItem("List All Notes", PluginIcons.LIST);
        popupList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listAllNotes();
                noteToolBar.setVisible(false);
            }
        });

        JMenuItem delete = new JMenuItem("Delete Note", PluginIcons.DELETE);
        delete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteNote();
            }
        });

        popupMenu.add(cut);
        popupMenu.add(copy);
        popupMenu.add(paste);
        popupMenu.addSeparator();
        popupMenu.add(popupList);
        popupMenu.addSeparator();
        popupMenu.add(delete);
        pane.addMouseListener(new PopupListener(popupMenu));
    }

    public String getText() {
        return pane.getText();
    }

    public void setText(String text) {
        pane.setText(text);
    }

    public void setNotesFont(Font notesFont) {
        pane.setFont(notesFont);
    }

    public void setBackgroundColor(String colorName) {
        Color newColor = null;
        switch(colorName){
            case "yellow":
                newColor = new JBColor(new Color(253, 254, 192), new Color(253, 254, 192));
                break;
            case "green":
                newColor = new JBColor(new Color(221, 254, 212), new Color(221, 254, 212));
                break;
            case "pink":
                newColor = new JBColor(new Color(254, 224, 251), new Color(254, 224, 251));
                break;
            case "purple":
                newColor = new JBColor(new Color(233, 212, 254), new Color(233, 212, 254));
                break;
            case "blue":
                newColor = new JBColor(new Color(214, 239, 254), new Color(214, 239, 254));
                break;
            case "white":
                newColor = new JBColor(new Color(251, 251, 251), new Color(251, 251, 251));
                break;
        }
        pane.setBackground(newColor);
    }
}