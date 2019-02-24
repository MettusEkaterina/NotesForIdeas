package ui;

import com.intellij.openapi.actionSystem.*;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
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
import java.util.List;

import manager.NotesManager;

public class NotesPanel {
    private String id;
    private JTextPane pane;
    private JPanel panel1;
    private JScrollPane noteScroller;
    private JLabel labelNoteTitle;
    private JPanel actionPanel;
    public Element element;
    private int selectedIndex;
    private Element selectedNote;
    private NotesManager notesManager;
    private JEditorPane listPane;
    public static SimpleDateFormat sdf = new SimpleDateFormat();
    private Color currentFontColor = new JBColor(new Color(0, 0, 0), new Color(0, 0, 0));
    private Color currentBackgroundColor = new JBColor(new Color(255, 255, 255), new Color(255, 255, 255));
    private Font currentFont = new Font("Arial", Font.PLAIN, 16);
    private boolean listAllNotes = false;

    public NotesPanel(final Element element) {
        notesManager = NotesManager.getInstance();
        id = notesManager.getNextPanelID();
        this.element = element;
        final ActionManager actionManager = ActionManager.getInstance();
        final DefaultActionGroup dag = new DefaultActionGroup();

        dag.addSeparator();
        dag.add(new AnAction("Add Note", "Add Note", PluginIcons.ADD) {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                addNewNote("Enter your notes here...");
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
                if (listAllNotes) {
                    selectNote(getSelectedNoteIndex(), true);
                } else {
                    listAllNotes = true;
                    listAllNotes();
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

        createPopupMenu();

        selectedIndex = 0;
        try {
            selectedIndex = Integer.parseInt(element.getAttributeValue("selectednoteindex"));
        } catch (NumberFormatException e) {
            selectedIndex = 0;
        }

        notesManager.addNotesPanel(this);
        selectNote(selectedIndex, true);
        pane.setMargin(JBUI.insets(5, 10, 5, 10));
        pane.setFont(currentFont);
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

    public JComponent getRootComponent() {
        return panel1;
    }

    public void selectNote(int index, boolean requestFocus) {
        listAllNotes = false;

        if (pane.getParent() == null) {
            noteScroller.getViewport().add(pane);
        }

        if (index >= 0 && index < element.getChildren().size()) {
            setSelectedNoteIndex(index);
            selectedNote = element.getChildren().get(index);
            pane.setText(selectedNote.getText());
            labelNoteTitle.setText(selectedNote.getAttributeValue("title"));

            if (requestFocus) {
                pane.requestFocus();
            }
        }
    }

    private void setSelectedNoteIndex(int index) {
        selectedIndex = index;
        element.setAttribute("selectednoteindex", String.valueOf(index));
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

    private Element getSelectedNote() {
        return selectedNote;
    }

    private void listAllNotes() {
        if (listPane == null) {
            listPane = new JEditorPane();
            listPane.setEditable(false);
            listPane.setContentType("text/html");
            listPane.setMargin(JBUI.emptyInsets());
            listPane.setBackground(JBColor.background());
            listPane.addHyperlinkListener(new HyperlinkListener() {
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        setSelectedNoteIndex(Integer.parseInt(e.getURL().getHost()));
                        selectNote(selectedIndex, true);
                    }
                }
            });
        }
        if (listPane.getParent() == null) {
            noteScroller.getViewport().add(listPane);
        }

        int foregroundRed = JBColor.foreground().getRed();
        int foregroundGreen = JBColor.foreground().getGreen();
        int foregroundBlue = JBColor.foreground().getBlue();
        String foregroundColor = "rgb(" + foregroundRed + "," + foregroundGreen + "," + foregroundBlue + ")";
        boolean even = true;
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='margin:0'>");
        List list = element.getChildren();
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
        listPane.setText(sb.toString());

        labelNoteTitle.setText("All Notes");
    }

    public void addNewNote(String notes) {
        Element note = new Element("note");
        note.setAttribute("title", sdf.format(new Date()));
        note.setText(notes);
        element.addContent(note);
        selectNote(element.getChildren().size() - 1, true);
        notesManager.syncNotePanels(id);
    }

    public int getSelectedNoteIndex() {
        return selectedIndex;
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

    public String getId() {
        return id;
    }

    public void setText(String text) {
        pane.setText(text);
    }

    public String getText() {
        return pane.getText();
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
                listAllNotes = true;
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
}