package com.vaadin.incubator.dragdroplayouts.demo;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.vaadin.Application;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;

public class DragdropApplication extends Application {

    private class MainWindow extends Window {

        private TabSheet tabs;

        private Label code;

        public MainWindow() {
            setTheme("dragdrop");

            VerticalSplitPanel content = new VerticalSplitPanel();
            content.setSizeFull();

            tabs = new TabSheet();
            tabs.setSizeFull();
            tabs.setImmediate(true);

            tabs.addComponent(new DragdropAbsoluteLayoutDemo());
            tabs.addComponent(new DragdropVerticalLayoutDemo());
            tabs.addComponent(new DragdropHorizontalLayoutDemo());
            tabs.addComponent(new DragdropGridLayoutDemo());
            tabs.addComponent(new DragdropLayoutDraggingDemo());
            tabs.addComponent(new DragdropHorizontalSplitPanelDemo());
            tabs.addComponent(new DragdropVerticalSplitPanelDemo());
            tabs.addComponent(new DragdropTabsheetDemo());
            tabs.addComponent(new DragdropAccordionDemo());
            tabs.addComponent(new DragdropDragFilterDemo());

            tabs.addListener(new TabSheet.SelectedTabChangeListener(){
                public void selectedTabChange(SelectedTabChangeEvent event) {
                    tabChanged(event.getTabSheet().getSelectedTab());
                }
            });
            
            content.addComponent(tabs);

            code = new Label("", Label.CONTENT_PREFORMATTED);

            Panel codePanel = new Panel();
            codePanel.setSizeFull();
            codePanel.addComponent(code);
            content.addComponent(codePanel);

            setContent(content);

            tabChanged(tabs.getComponentIterator().next());
        }

        private void tabChanged(Component tab) {
            try {
                String path = "";

                if (tab instanceof DragdropDemo) {
                    path = ((DragdropDemo) tab).getCodePath();
                } else {
                    throw new IllegalArgumentException();
                }

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(getClass().getClassLoader()
                                .getResourceAsStream(path)));

                StringBuilder codelines = new StringBuilder();
                String line = reader.readLine();
                while (line != null) {
                    codelines.append(line);
                    codelines.append("\n");
                    line = reader.readLine();
                }

                reader.close();
                code.setValue(codelines.toString());

            } catch (Exception e) {
                code.setValue("No code available.");
            }
        }
    }

    @Override
    public void init() {
        setMainWindow(new MainWindow());
    }
}
