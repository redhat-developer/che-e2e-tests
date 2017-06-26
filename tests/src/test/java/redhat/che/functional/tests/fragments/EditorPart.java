package redhat.che.functional.tests.fragments;

import org.openqa.selenium.support.FindBy;

/**
 * id = "gwt-debug-editorMultiPartStack-contentPanel"
 */
public class EditorPart {

    @FindBy(id = "gwt-debug-editorPartStack-contentPanel")
    protected CodeEditorFragment codeEditor;

    @FindBy(id = "gwt-debug-editorPartStack-tabsPanel")
    protected TabsPanel tabsPanel;

    public CodeEditorFragment codeEditor(){
        return codeEditor;
    }

    public TabsPanel tabsPanel(){
        return tabsPanel;
    }
}
