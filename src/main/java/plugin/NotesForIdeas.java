package plugin;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import manager.NotesManager;
import ui.NotesPanel;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.xml.sax.InputSource;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class NotesForIdeas implements ApplicationComponent {
    private String enotes = "";
    private Element notesElement;
    public static final String FILELOCATION = System.getProperty("user.home") + System.getProperty("file.separator") + ".notesforideas";

    public NotesForIdeas() {}

    public String getComponentName() {
        return "Notes For Ideas";
    }
        public void initComponent() {
        notesElement = readSettings();
        ProjectManager.getInstance().addProjectManagerListener(new ProjectManagerListener() {
            Key noteskey = new Key("notesforideasid");

            public void projectOpened(final Project project) {
                final NotesPanel notesPanel = new NotesPanel(notesElement);
                final ToolWindowManager twm = ToolWindowManager.getInstance(project);

                Runnable task1 = new Runnable() {
                    @Override
                    public void run() {
                        ToolWindow toolWindow = twm.registerToolWindow("Notes For Ideas", false, ToolWindowAnchor.RIGHT);
                        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
                        Content content = contentFactory.createContent(notesPanel.getRootComponent(), "", false);
                        toolWindow.getContentManager().addContent(content);
                        project.putUserData(noteskey, notesPanel);
                    }
                };
                twm.invokeLater(task1);
            }

            public void projectClosed(final Project project) {
                NotesManager.getInstance().clearLocks((NotesPanel) project.getUserData(noteskey));

                if (NotesManager.saveSettings(notesElement)) {
                    enotes = "";
                } else {
                    enotes = new XMLOutputter().outputString(notesElement);
                }
            }
        });
    }

    public void disposeComponent() {}

    private Element readSettings() {
        Element element = null;
        File settingsFile = NotesManager.getSettingsFile();
        SAXBuilder builder = new SAXBuilder();
        try {
            if (settingsFile != null) {
                if (settingsFile.length() == 0) {
                    if (enotes != null && enotes.trim().length() > 0) {
                        element = builder.build(new InputSource(new StringReader(enotes))).getRootElement();
                    }
                } else {
                    FileInputStream fis = new FileInputStream(settingsFile);
                    element = builder.build(fis).getRootElement();
                    fis.close();
                }
            } else if (enotes != null && enotes.length() > 0) {
                element = builder.build(new InputSource(new StringReader(enotes))).getRootElement();
            }
        } catch (Exception e) {}

        if (element == null) {
            element = new Element("notes");
            element.setAttribute("selectednoteindex", "0");
            element.setAttribute("fontname", "Arial");
            element.setAttribute("fontsize", "12");
            element.setAttribute("colorname", "yellow");
        }

        NotesManager mgr = NotesManager.getInstance();
        int fontsize = 12;
        try {
            fontsize = Integer.parseInt(Objects.requireNonNull(element.getAttributeValue("fontsize")));
        } catch (NumberFormatException e) {}
        mgr.setNotesFont(new Font(element.getAttributeValue("fontname"), Font.PLAIN, fontsize));

        String colorName = element.getAttributeValue("colorname");
        mgr.setColorName(colorName);

        List notes = element.getChildren();
        if (notes.size() == 0) {
            Element note = new Element("note");
            note.setAttribute("title", NotesPanel.sdf.format(new Date()));
            note.setAttribute("fontname", "Arial");
            note.setAttribute("fontsize", "12");
            note.setAttribute("colorname", "yellow");
            note.setText("Enter your notes here...");
            element.addContent(note);
        } else {
            for (Object note1 : notes) {
                Element note = (Element) note1;
                if (note.getAttributeValue("title") == null) {
                    note.setAttribute("title", "New Note");
                }
                if (note.getAttributeValue("fontname") == null) {
                    note.setAttribute("fontname", "Arial");
                }
                if (note.getAttributeValue("fontsize") == null) {
                    note.setAttribute("fontsize", "12");
                }
                if (note.getAttributeValue("colorname") == null) {
                    note.setAttribute("colorname", "yellow");
                }
            }
        }

        return element;
    }
}
