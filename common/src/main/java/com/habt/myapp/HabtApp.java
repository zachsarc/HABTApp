package com.habt.myapp;

import com.codename1.io.ConnectionRequest;
import com.codename1.io.NetworkManager;
import com.codename1.io.Preferences;
import com.codename1.io.JSONParser;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import com.codename1.system.Lifecycle;
import com.codename1.ui.*;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.components.SpanLabel;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import java.util.Properties;
import java.io.InputStream;

public class HabtApp extends Lifecycle {

    // Replace with your actual Firebase Web API key
    private static String FIREBASE_API_KEY = null;

    @Override
    public void runApp() {
        FIREBASE_API_KEY = Display.getInstance().getProperty("firebase.api.key", "");
        if(FIREBASE_API_KEY.isEmpty()) {
            Dialog.show("Error", "No API key provided", "OK", null);
        }
        UIManager.initFirstTheme("/theme");
        if (!"true".equals(Preferences.get("isLoggedIn", "false"))) {
            showLoginForm();
        } else {
            showMainTabbedForm();
        }
    }

    /**
     * Displays the login form with “Log In” and “Register” buttons.
     * Uses Firebase’s signInWithPassword for existing users.
     */
    private void showLoginForm() {
        Form login = new Form("Log In", BoxLayout.y());
        login.setUIID("LoginForm");

        TextField emailField = new TextField("", "Email", 20, TextField.EMAILADDR);
        emailField.setUIID("LoginField");
        login.add(new Label("Email:"));
        login.add(emailField);

        TextField passwordField = new TextField("", "Password", 20, TextField.PASSWORD);
        passwordField.setUIID("LoginField");
        login.add(new Label("Password:"));
        login.add(passwordField);

        Label errorLabel = new Label("");
        errorLabel.setUIID("ErrorLabel");
        login.add(errorLabel);

        Container buttonContainer = new Container(new FlowLayout(Component.CENTER));
        Button btnLogin = new Button("Log In");
        btnLogin.setUIID("LoginButton");
        buttonContainer.add(btnLogin);

        Button btnRegister = new Button("Register");
        btnRegister.setUIID("RegisterButton");
        buttonContainer.add(btnRegister);

        login.add(buttonContainer);

        // Log In listener (uses signInWithPassword)
        btnLogin.addActionListener(evt -> {
            String email = emailField.getText().trim();
            String pass  = passwordField.getText().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                errorLabel.setText("Email and password cannot be empty.");
                login.revalidate();
                return;
            }

            String payload = "{"
                    + "\"email\":\"" + email + "\","
                    + "\"password\":\"" + pass + "\","
                    + "\"returnSecureToken\":true"
                    + "}";

            ConnectionRequest req = new ConnectionRequest() {
                @Override
                protected void postResponse() {
                    int code = getResponseCode();
                    byte[] data = getResponseData();
                    String raw;
                    try {
                        raw = (data == null ? "" : new String(data, "UTF-8"));
                    } catch (Exception e) {
                        raw = "";
                    }

                    if (code != 200) {
                        if (raw.trim().startsWith("{")) {
                            try {
                                JSONParser parser = new JSONParser();
                                @SuppressWarnings("unchecked")
                                Map<String, Object> errResult = parser.parseJSON(
                                        new InputStreamReader(
                                                new ByteArrayInputStream(data), "UTF-8"
                                        )
                                );
                                if (errResult.containsKey("error")) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> errObj =
                                            (Map<String, Object>) errResult.get("error");
                                    String message = (String) errObj.get("message");
                                    errorLabel.setText("Error: " + message);
                                } else {
                                    errorLabel.setText("HTTP " + code + ": unexpected response.");
                                }
                            } catch (Exception ex) {
                                errorLabel.setText("Parse error: " + ex.getMessage());
                            }
                        } else {
                            errorLabel.setText("HTTP " + code + " — check network/endpoint.");
                        }
                        login.revalidate();
                        return;
                    }

                    if (raw.trim().isEmpty() || !raw.trim().startsWith("{")) {
                        errorLabel.setText("Expected JSON but got: " + raw);
                        login.revalidate();
                        return;
                    }

                    try {
                        JSONParser parser = new JSONParser();
                        @SuppressWarnings("unchecked")
                        Map<String, Object> result = parser.parseJSON(
                                new InputStreamReader(
                                        new ByteArrayInputStream(data), "UTF-8"
                                )
                        );
                        if (result.containsKey("idToken")) {
                            String idToken = (String) result.get("idToken");
                            String localId = (String) result.get("localId");

                            Preferences.set("firebaseIdToken", idToken);
                            Preferences.set("firebaseLocalId", localId);
                            Preferences.set("isLoggedIn", "true");
                            Preferences.set("username", email);

                            showMainTabbedForm();
                        } else {
                            errorLabel.setText("Login succeeded but no idToken found.");
                            login.revalidate();
                        }
                    } catch (Exception ex) {
                        errorLabel.setText("Parse error: " + ex.getMessage());
                        login.revalidate();
                    }
                }

                @Override
                protected void handleErrorResponseCode(int code, String message) {
                    errorLabel.setText("HTTP error: " + code);
                    login.revalidate();
                }
            };

            String signInUrl =
                    "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key="
                            + FIREBASE_API_KEY;
            req.setUrl(signInUrl);
            req.setPost(true);
            req.setContentType("application/json");
            req.setRequestBody(payload);
            req.setFailSilently(false);
            NetworkManager.getInstance().addToQueue(req);
        });

        // “Register” opens registration form
        btnRegister.addActionListener(evt -> showRegisterForm());

        login.show();
    }

    /**
     * Registration form: collects name, DOB, email, password.
     * Calls signUp, stores name + DOB in Preferences, then proceeds.
     */
    private void showRegisterForm() {
        Form register = new Form("Register", BoxLayout.y());
        register.setUIID("RegisterForm");

        TextField nameField = new TextField("", "Full Name", 30, TextField.ANY);
        nameField.setUIID("RegField");
        register.add(new Label("Name:")).setUIID("RegLabel");
        register.add(nameField);

        TextField dobField = new TextField("", "YYYY-MM-DD", 10, TextField.NUMERIC);
        dobField.setUIID("RegField");
        register.add(new Label("Date of Birth:")).setUIID("RegLabel");
        register.add(dobField);

        TextField emailField = new TextField("", "Email", 20, TextField.EMAILADDR);
        emailField.setUIID("RegField");
        register.add(new Label("Email:")).setUIID("RegLabel");
        register.add(emailField);

        TextField passwordField = new TextField("", "Password", 20, TextField.PASSWORD);
        passwordField.setUIID("RegField");
        register.add(new Label("Password:")).setUIID("RegLabel");
        register.add(passwordField);

        Label errorLabel = new Label("");
        errorLabel.setUIID("ErrorLabel");
        register.add(errorLabel);

        Button btnSubmit = new Button("Submit");
        btnSubmit.setUIID("RegisterButton");
        register.add(btnSubmit);

        Button btnBack = new Button("Back to Log In");
        btnBack.setUIID("LoginButton");
        register.add(btnBack);

        btnSubmit.addActionListener(evt -> {
            String fullName = nameField.getText().trim();
            String dob      = dobField.getText().trim();
            String email    = emailField.getText().trim();
            String pass     = passwordField.getText().trim();

            if (fullName.isEmpty() || dob.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                errorLabel.setText("All fields are required.");
                register.revalidate();
                return;
            }

            String payload = "{"
                    + "\"email\":\"" + email + "\","
                    + "\"password\":\"" + pass + "\","
                    + "\"returnSecureToken\":true"
                    + "}";

            ConnectionRequest req = new ConnectionRequest() {
                @Override
                protected void postResponse() {
                    int code = getResponseCode();
                    byte[] data = getResponseData();
                    String raw;
                    try {
                        raw = (data == null ? "" : new String(data, "UTF-8"));
                    } catch (Exception e) {
                        raw = "";
                    }

                    if (code != 200) {
                        if (raw.trim().startsWith("{")) {
                            try {
                                JSONParser parser = new JSONParser();
                                @SuppressWarnings("unchecked")
                                Map<String, Object> errResult = parser.parseJSON(
                                        new InputStreamReader(
                                                new ByteArrayInputStream(data), "UTF-8"
                                        )
                                );
                                if (errResult.containsKey("error")) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> errObj =
                                            (Map<String, Object>) errResult.get("error");
                                    String message = (String) errObj.get("message");
                                    errorLabel.setText("Error: " + message);
                                } else {
                                    errorLabel.setText("HTTP " + code + ": unexpected response.");
                                }
                            } catch (Exception ex) {
                                errorLabel.setText("Parse error: " + ex.getMessage());
                            }
                        } else {
                            errorLabel.setText("HTTP " + code + " — check network/endpoint.");
                        }
                        register.revalidate();
                        return;
                    }

                    if (raw.trim().isEmpty() || !raw.trim().startsWith("{")) {
                        errorLabel.setText("Expected JSON but got: " + raw);
                        register.revalidate();
                        return;
                    }

                    try {
                        JSONParser parser = new JSONParser();
                        @SuppressWarnings("unchecked")
                        Map<String, Object> result = parser.parseJSON(
                                new InputStreamReader(
                                        new ByteArrayInputStream(data), "UTF-8"
                                )
                        );
                        if (result.containsKey("idToken")) {
                            String idToken = (String) result.get("idToken");
                            String localId = (String) result.get("localId");

                            // Save to Preferences
                            Preferences.set("firebaseIdToken", idToken);
                            Preferences.set("firebaseLocalId", localId);
                            Preferences.set("isLoggedIn", "true");
                            Preferences.set("username", email);

                            // Store fullName + dob
                            Preferences.set("fullName", fullName);
                            Preferences.set("dateOfBirth", dob);

                            showMainTabbedForm();
                        } else {
                            errorLabel.setText("Registration succeeded but no idToken found.");
                            register.revalidate();
                        }
                    } catch (Exception ex) {
                        errorLabel.setText("Parse error (success JSON): " + ex.getMessage());
                        register.revalidate();
                    }
                }

                @Override
                protected void handleErrorResponseCode(int code, String message) {
                    errorLabel.setText("HTTP error: " + code);
                    register.revalidate();
                }
            };

            String signUpUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key="
                    + FIREBASE_API_KEY;
            req.setUrl(signUpUrl);
            req.setPost(true);
            req.setContentType("application/json");
            req.setRequestBody(payload);
            req.setFailSilently(false);
            NetworkManager.getInstance().addToQueue(req);
        });

        btnBack.addActionListener(evt -> showLoginForm());
        register.show();
    }

    /**
     * Builds the main tabbed UI: Account, Home, Calendar.
     * The Account tab now displays the user’s stored full name (if available).
     */
    private void showMainTabbedForm() {
        Form hi = new Form("HABT", new BorderLayout());
        Tabs tabs = new Tabs();

        tabs.addTab("Account", accountPage(tabs, hi));

        Container homeTab = new Container(BoxLayout.y());
        homeTab.setScrollableY(true);
        homeTab.add(quoteOfDay());
        homeTab.add(getDaySinceRegistrationLabel());
        tabs.addTab("Home", homeTab);

        Container calendarTab = new Container(BoxLayout.y());
        calendarTab.add(new Label("Calendar View Coming Soon"));
        tabs.addTab("Calendar", calendarTab);

        hi.add(BorderLayout.CENTER, tabs);

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
     * Returns the Account tab’s container. Displays the user’s full name if stored;
     * otherwise falls back to username (their email).
     */
    private Container accountPage(Tabs tabs, Form hiForm) {
        Container accountCnt = new Container(BoxLayout.y());
        accountCnt.setScrollableY(true);

        String fullName = Preferences.get("fullName", null);
        String displayName = (fullName != null)
                ? fullName
                : Preferences.get("username", "User");

        Label title = new Label("Welcome, " + displayName + "!");
        title.setUIID("Title");
        accountCnt.add(title);

        String dob = Preferences.get("dateOfBirth", null);
        if (dob != null) {
            Label dobLabel = new Label("Date of Birth: " + dob);
            dobLabel.setUIID("AccountInfo");
            accountCnt.add(dobLabel);
        }

        SpanLabel info = new SpanLabel(
                "This is your account page.\n\n"
                        + "You can do things like:\n"
                        + "- Change password\n"
                        + "- Update email\n"
                        + "- View app settings\n"
        );
        info.setUIID("AccountInfo");
        accountCnt.add(info);

        Button btnLogout = new Button("Log Out");
        btnLogout.setUIID("LogoutButton");
        accountCnt.add(btnLogout);

        btnLogout.addActionListener(evt -> {
            Preferences.delete("isLoggedIn");
            Preferences.delete("username");
            Preferences.delete("fullName");
            Preferences.delete("dateOfBirth");
            Dialog.show("Logged out", "You have been logged out.", "OK", null);
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
     * Returns a Container showing how many days have passed since “registrationDate.”
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
