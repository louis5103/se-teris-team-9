package seoultech.se.client.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.springframework.stereotype.Service;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.Stage;

@Service
public class SettingsService {

    private Stage primaryStage;
    private final DoubleProperty stageWidth = new SimpleDoubleProperty(500);
    private final DoubleProperty stageHeight = new SimpleDoubleProperty(700);

    private final DoubleProperty soundVolume = new SimpleDoubleProperty(80); // Default volume is 80
    private final StringProperty colorMode = new SimpleStringProperty("colorModeDefault"); // default, rg_blind, yb_blind
    private final StringProperty screenSize = new SimpleStringProperty("screenSizeM"); // XS, S, M, L, XL

    private static final String SETTINGS_FILE = "tetris_settings";

    public SettingsService() {
        // Load settings from file or set defaults
        loadSettings();
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void applyResolution(double width, double height) {
        stageWidth.set(width);
        stageHeight.set(height);
        if (primaryStage != null) {
            primaryStage.setWidth(width);
            primaryStage.setHeight(height);
            primaryStage.centerOnScreen();
        }
    }

    public void loadSettings() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(new File(SETTINGS_FILE))) {
            props.load(in);
            soundVolume.set(Double.parseDouble(props.getProperty("soundVolume", "80")));
            colorMode.set(props.getProperty("colorMode", "colorModeDefault"));
            screenSize.set(props.getProperty("screenSize", "screenSizeM"));
            double width = Double.parseDouble(props.getProperty("stageWidth", "500"));
            double height = Double.parseDouble(props.getProperty("stageHeight", "700"));
            applyResolution(width, height);
            System.out.println("✅ Settings loaded successfully.");
        } catch (Exception e) {
            System.out.println("❗ Failed to load settings, using defaults.");
            restoreDefaults();
        }
    }

    public void saveSettings() {
        Properties props = new Properties();
        props.setProperty("soundVolume", String.valueOf(soundVolume.get()));
        props.setProperty("colorMode", colorMode.get());
        props.setProperty("screenSize", screenSize.get());
        props.setProperty("stageWidth", String.valueOf(stageWidth.get()));
        props.setProperty("stageHeight", String.valueOf(stageHeight.get()));
        try {
            props.store(new java.io.FileOutputStream(new File(SETTINGS_FILE)), null);
            System.out.println("✅ Settings saved successfully.");
        } catch (Exception e) {
            System.out.println("❗ Failed to save settings.");
        }
    }

    public void restoreDefaults() {
        soundVolume.set(80);
        colorMode.set("colorModeDefault");
        screenSize.set("screenSizeM");
        applyResolution(500, 700);
        saveSettings();
    }

    public DoubleProperty soundVolumeProperty() { 
        return soundVolume;
    }

    public StringProperty colorModeProperty() {
        return colorMode;
    }

    public StringProperty screenSizeProperty() {
        return screenSize;
    }

    public DoubleProperty stageWidthProperty() {
        return stageWidth;
    }

    public DoubleProperty stageHeightProperty() {
        return stageHeight;
    }
}