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
import ui.NotesPanel;

public class NotesForIdeas implements ApplicationComponent {
    public NotesForIdeas() {}

    public String getComponentName() {
        return "Notes For Ideas";
    }
        public void initComponent() {
        ProjectManager.getInstance().addProjectManagerListener(new ProjectManagerListener() {
            Key noteskey = new Key("notesforideasid");

            public void projectOpened(final Project project) {
                final NotesPanel notesPanel = new NotesPanel();
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
        });
    }

    public void disposeComponent() {}
}
