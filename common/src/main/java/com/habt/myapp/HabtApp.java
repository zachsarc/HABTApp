package com.habt.myapp;

import com.codename1.system.Lifecycle;
import com.codename1.ui.*;
import com.codename1.ui.layouts.*;
import com.codename1.io.*;
import com.codename1.components.SpanLabel;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;

public class HabtApp extends Lifecycle {
    @Override
    public void runApp() {
        UIManager.initFirstTheme("/theme"); // Loads CSS theme

        Form hi = new Form("HABT", BoxLayout.y());
        Container content = new Container(BoxLayout.y());
        content.setScrollableY(true);
        content.add(quoteOfDay());
        content.add(getDaySinceRegistrationLabel());
        hi.add(content);

        hi.getToolbar().addMaterialCommandToSideMenu("Home", FontImage.MATERIAL_CHECK, 4, e -> hello());
        hi.getToolbar().addMaterialCommandToSideMenu("Calendar", FontImage.MATERIAL_CALENDAR_TODAY, 4, e -> showCalendarForm(hi));

        hi.show();
    }

    private Container quoteOfDay() {
        String[] inspirationalQuotes = {
                "The secret of change is to focus all of your energy, not on fighting the old, but on building the new.",
                "You don’t have to be perfect to start, but you have to start to be great.",
                "Habits are hard to break, but easy to change when you commit to a new path.",
                "Every moment is a fresh beginning.",
                "The chains of habit are too weak to be felt until they are too strong to be broken.",
                "Small steps every day lead to big changes over time. You are not your habits. You can change them and create the life you want.",
                "It’s never too late to be what you might have been.",
                "The greatest discovery of all time is that a person can change their future by merely changing their attitude.",
                "Your life does not get better by chance, it gets better by change.",
                "Discipline is choosing between what you want now and what you want most.",
                "You can’t go back and change the beginning, but you can start where you are and change the ending.",
                "The only way to make sense out of change is to plunge into it, move with it, and join the dance.",
                "Success is the sum of small efforts, repeated day in and day out.",
                "Break the habit today, or it will break you tomorrow."
        };

        int randomIndex = new java.util.Random().nextInt(inspirationalQuotes.length);
        String quote = inspirationalQuotes[randomIndex];
        SpanLabel quoteLabel = new SpanLabel(quote);
        quoteLabel.setUIID("QuoteLabel");
        quoteLabel.setTextUIID("CenterLabel");

        Container quoteContainer = new Container(new FlowLayout(Component.CENTER));
        quoteContainer.setUIID("QuoteContainer");
        quoteContainer.add(quoteLabel);

        return quoteContainer;
    }

    private Container getDaySinceRegistrationLabel() {
        String stored = Preferences.get("registrationDate", null);

        if (stored == null) {
            stored = String.valueOf(System.currentTimeMillis());
            Preferences.set("registrationDate", stored);
        }

        long regMillis = Long.parseLong(stored);
        long nowMillis = System.currentTimeMillis();
        long millisPerDay = 1000L * 60 * 60 * 24;
        long daysPassed = (nowMillis - regMillis) / millisPerDay + 1;

        Label dayLabel = new Label("Day " + daysPassed);
        dayLabel.setUIID("DayLabel");

        Container dayContainer = new Container(new FlowLayout(Component.CENTER));
        dayContainer.add(dayLabel);
        return dayContainer;
    }


    private void showCalendarForm(Form previousForm) {
        Form calendar = new Form("Calendar", BoxLayout.y());
        calendar.add(new Label("Calendar View Coming Soon"));

        calendar.getToolbar().addMaterialCommandToLeftBar("", FontImage.MATERIAL_ARROW_BACK, e -> {
            if (previousForm != null) {
                previousForm.showBack();
            }
        });

        calendar.show();
    }

    private void hello() {
        Form currentForm = Display.getInstance().getCurrent();
        Dialog dialog = new Dialog("Welcome to HABT");
        dialog.setLayout(new BorderLayout());
        dialog.add(BorderLayout.CENTER, new Label("Lets get started!"));

        Button toCalendar = new Button("Getting Started");
        toCalendar.addActionListener(evt -> {
            dialog.dispose();
            showCalendarForm(currentForm);
        });

        dialog.add(BorderLayout.SOUTH, toCalendar);
        dialog.show();
    }
}
