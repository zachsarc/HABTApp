package com.habt.myapp;

import static com.codename1.ui.CN.*;
import com.codename1.system.Lifecycle;
import com.codename1.ui.*;
import com.codename1.ui.layouts.*;
import com.codename1.io.*;
import com.codename1.ui.plaf.*;
import com.codename1.ui.util.Resources;
import com.codename1.io.Preferences.*;
import jdk.internal.access.JavaIOFileDescriptorAccess;

import java.util.Date;


public class HabtApp extends Lifecycle {
    @Override
    public void runApp() { // Entry point for app
        String stored = Preferences.get("registrationDate", null);
        Form hi = new Form("HABT", BoxLayout.y()); // Creates new form
        hi.add(getDaySinceRegistrationLabel());
        hi.getToolbar().addMaterialCommandToSideMenu("Hello Command",
        FontImage.MATERIAL_CHECK, 4, e -> hello()); // Adds a comment to the side menu which also calls hello() method when it is clicked from the side menu
        hi.getToolbar().addMaterialCommandToSideMenu("Calendar", FontImage.MATERIAL_CHECK, 4, e -> showCalendarForm());
        hi.show(); // Show form
    }

    private void showCalendarForm() {
        Form calendar = new Form("Calendar", BoxLayout.y()); // Creates a new form for calendar

        //Add Back button
        calendar.getToolbar().addMaterialCommandToLeftBar("", FontImage.MATERIAL_ARROW_BACK, e -> {
            Form hi = new Form("HABT", BoxLayout.y());
            hi.add(getDaySinceRegistrationLabel());
            String stored = Preferences.get("registrationDate", null);
            Toolbar tb = hi.getToolbar();
            tb.addMaterialCommandToSideMenu("Habits to Break!",
                    FontImage.MATERIAL_CHECK, 4, evt -> hello());
            tb.addMaterialCommandToSideMenu("Calendar",
                    FontImage.MATERIAL_CALENDAR_TODAY, 4, evt -> showCalendarForm());
            hi.showBack();
        });
        calendar.show(); // Show calendar page

    }

    private Label getDaySinceRegistrationLabel() {
        String stored = Preferences.get("registrationDate", null);

        if (stored == null) {
            stored = String.valueOf(System.currentTimeMillis());
            Preferences.set("registrationDate", stored);
        }

        long regMillis = Long.parseLong(stored);
        long nowMillis = System.currentTimeMillis();
        long millisPerDay = 1000L * 60 * 60 * 24;
        long daysPassed = (nowMillis - regMillis) / millisPerDay + 1;

        return new Label("Day " + daysPassed);
    }


    private void hello() {
        Dialog dialog = new Dialog("Welcome to HABT");
        dialog.setLayout(new BorderLayout());
        dialog.add(BorderLayout.CENTER, new Label("Lets get started!"));
        Button toCalendar = new Button("Getting Started");
        toCalendar.addActionListener(evt -> {
            dialog.dispose();
            showCalendarForm();
        });
        dialog.add(BorderLayout.SOUTH, toCalendar);
        dialog.show();
    }

}