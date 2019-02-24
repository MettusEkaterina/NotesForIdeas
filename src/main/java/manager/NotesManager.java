package manager;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import ui.NotesPanel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import static plugin.NotesForIdeas.FILELOCATION;

public class NotesManager {
    private HashMap<String, NotesPanel> panelMap;
    private int index = 0;
    private static NotesManager instance = new NotesManager();

    private NotesManager() {
        panelMap = new HashMap<>();
    }

    public static NotesManager getInstance() {
        return instance;
    }

    public void addNotesPanel(NotesPanel panel) {
        panelMap.put(panel.getId(), panel);
    }

    public void syncNotePanels(String panelid) {
        if (panelid != null) {
            for (String id : panelMap.keySet()) {
                if (id != null && !panelid.equals(id)) {
                    NotesPanel qnp = panelMap.get(id);
                    int index = qnp.getSelectedNoteIndex();
                    if (index == qnp.element.getChildren().size()) {
                        index--;
                    }
                    qnp.selectNote(index, false);
                }
            }
        }
    }

    public String getNextPanelID() {
        return "panel_" + index++;
    }

    public void clearLocks(NotesPanel panel) {
        panelMap.remove(panel.getId());
    }

    public static File getSettingsFile() {
        File settingsFile = null;
        File fileLocationFolder = new File(FILELOCATION);
        try {
            if (!fileLocationFolder.exists()) {
                if (fileLocationFolder.mkdir()) {
                    settingsFile = new File(fileLocationFolder, "notesforideas.xml");
                    if (!settingsFile.exists()) {
                        settingsFile.createNewFile();
                    }
                }
            } else {
                settingsFile = new File(fileLocationFolder, "notesforideas.xml");
                if (!settingsFile.exists()) {
                    settingsFile.createNewFile();
                }
            }
        } catch (IOException e) {
            settingsFile = null;
        }
        return settingsFile;
    }

    public static boolean saveSettings(Element element) {
        XMLOutputter outputter = new XMLOutputter();
        File settingsFile = getSettingsFile();
        if (settingsFile != null) {
            try {
                FileOutputStream fos = new FileOutputStream(settingsFile);
                outputter.setFormat(Format.getPrettyFormat());
                outputter.output(element, fos);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    public void syncNoteText(String panelid) {
        NotesPanel panel = panelMap.get(panelid);
        for (Object o : panelMap.keySet()) {
            String id = (String) o;
            if (id != null && !panelid.equals(id)) {
                NotesPanel qnp = panelMap.get(id);
                if (qnp.getSelectedNoteIndex() == panel.getSelectedNoteIndex()) {
                    qnp.setText(panel.getText());
                }
            }
        }
    }
}
