package com.habt.myapp;

import com.codename1.io.Preferences;
import com.codename1.system.Lifecycle;
import com.codename1.ui.*;
import com.codename1.ui.layouts.*;
import com.codename1.components.SpanLabel;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;

public class HabtApp extends Lifecycle {

    @Override
    public void runApp() {
        UIManager.initFirstTheme("/theme");

        // 1) On startup, check whether the user is logged in
        if(!"true".equals(Preferences.get("isLoggedIn", "false"))) {
            showLoginForm();
        } else {
            showMainTabbedForm();
        }
    }

    /**
     * Builds and displays a simple login form.
     * Adapts to our own backend authentication logic as needed.
     */
    private void showLoginForm() {
        Form login = new Form("Log In", BoxLayout.y());
        login.setUIID("LoginForm");

        TextField usernameField = new TextField("", "Username", 20, TextField.ANY);
        usernameField.setUIID("LoginField");
        login.add(new Label("Username:"));
        login.add(usernameField);

        TextField passwordField = new TextField("", "Password", 20, TextField.PASSWORD);
        passwordField.setUIID("LoginField");
        login.add(new Label("Password:"));
        login.add(passwordField);

        Label errorLabel = new Label("");
        errorLabel.setUIID("ErrorLabel");
        login.add(errorLabel);

        Button btnLogin = new Button("Log In");
        btnLogin.setUIID("LoginButton");
        login.add(btnLogin);

        btnLogin.addActionListener(evt -> {
            String user = usernameField.getText().trim();
            String pass = passwordField.getText().trim();

            // Replace this with your real validation!!!
            if(user.isEmpty() || pass.isEmpty()) {
                errorLabel.setText("Please enter both username & password.");
                login.revalidate();
                return;
            }

            // Assume login success:
            Preferences.set("isLoggedIn", "true");
            Preferences.set("username", user);

            // Proceed to the main form:
            showMainTabbedForm();
        });

        login.show();
    }

    /**
     * Constructs our main “Tabbed” UI with Account, Home, and Calendar tabs.
     */
    private void showMainTabbedForm() {
        Form hi = new Form("HABT", new BorderLayout());
        Tabs tabs = new Tabs();

        // ACCOUNT TAB (with real Log Out)
        tabs.addTab("Account", accountPage(tabs, hi));

        // HOME TAB (quote + day counter)
        Container homeTab = new Container(BoxLayout.y());
        homeTab.setScrollableY(true);
        homeTab.add( quoteOfDay() );
        homeTab.add( getDaySinceRegistrationLabel() );
        tabs.addTab("Home", homeTab);

        // CALENDAR TAB (placeholder)
        Container calendarTab = new Container(BoxLayout.y());
        calendarTab.add( new Label("Calendar View Coming Soon") );
        tabs.addTab("Calendar", calendarTab);

        hi.add(BorderLayout.CENTER, tabs);

        // Side menu entries to switch between tabs
        hi.getToolbar().addMaterialCommandToSideMenu(
                "Home", FontImage.MATERIAL_CHECK, 4,
                e -> tabs.setSelectedIndex(1, true)
        );
        hi.getToolbar().addMaterialCommandToSideMenu(
                "Calendar", FontImage.MATERIAL_CALENDAR_TODAY, 4,
                e -> tabs.setSelectedIndex(2, true)
        );

        hi.show();
    }

    /**
     * Returns a Container for the Account tab, including a working “Log Out” button.
     */
    private Container accountPage(Tabs tabs, Form hiForm) {
        Container accountCnt = new Container(BoxLayout.y());
        accountCnt.setScrollableY(true);

        // Greet the user by name (pulled from Preferences)
        String user = Preferences.get("username", "User");
        Label title = new Label("Welcome, " + user + "!");
        title.setUIID("Title");
        accountCnt.add(title);

        SpanLabel info = new SpanLabel(
                "This is your account page.\n\n"
                        + "You can do things like:\n"
                        + "- Change password\n"
                        + "- Update email\n"
                        + "- View app settings\n"
        );
        info.setUIID("AccountInfo");
        accountCnt.add(info);

        // Real Log Out button:
        Button btnLogout = new Button("Log Out");
        btnLogout.setUIID("LogoutButton");
        accountCnt.add(btnLogout);

        btnLogout.addActionListener(evt -> {
            // 1) Remove any stored login flags/credentials
            Preferences.delete("isLoggedIn");
            Preferences.delete("username");
            // If you have more keys for auth tokens, delete those too.

            // 2) Show a confirmation (optional)
            Dialog.show("Logged out", "You have been logged out.", "OK", null);

            // 3) Go back to the login screen
            showLoginForm();
        });

        return accountCnt;
    }

    /**
     * Returns a Container containing a random “Quote of the Day.”
     */
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

    /**
     * Returns a Container showing how many days since registration.
     */
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
}
