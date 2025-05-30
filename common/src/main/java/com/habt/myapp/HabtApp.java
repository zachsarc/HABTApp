package com.habt.myapp;

import static com.codename1.ui.CN.*;
import com.codename1.system.Lifecycle;
import com.codename1.ui.*;
import com.codename1.ui.layouts.*;
import com.codename1.io.*;
import com.codename1.ui.plaf.*;
import com.codename1.ui.util.Resources;


public class HabtApp extends Lifecycle {
    @Override
    public void runApp() { // Entry point for app
        Form hi = new Form("HABT", BoxLayout.y()); // Creates new form
        Button helloButton = new Button("Hello World"); // Create button with label
        hi.add(helloButton);
        helloButton.addActionListener(e -> hello()); // Calls hello() method when it is clicked
        hi.getToolbar().addMaterialCommandToSideMenu("Hello Command",
        FontImage.MATERIAL_CHECK, 4, e -> hello()); // Adds a comment to the side menu which also calls hello() method when it is clicked from the side menu
        hi.show(); // Show form
    }

    private void hello() {
        Dialog.show("Hello Codename One", "Welcome to Codename One", "OK", null);
    }

}