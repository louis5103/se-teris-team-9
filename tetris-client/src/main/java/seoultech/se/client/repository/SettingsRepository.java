package seoultech.se.client.repository;

import org.springframework.stereotype.Repository;
import seoultech.se.client.model.Setting;

import java.io.*;
import java.util.*;

@Repository
public class SettingsRepository {
    private static final String SETTINGS_FILE = "custom_settings.properties";
    private Properties properties;

    public SettingsRepository() {
        properties = new Properties();
        loadProperties();
    }

    private void loadProperties() {
        try (FileInputStream in = new FileInputStream(SETTINGS_FILE)) {
            properties.load(in);
        } catch (IOException e) {
            // File doesn't exist yet - that's okay for first run
        }
    }

    private void saveProperties() {
        try (FileOutputStream out = new FileOutputStream(SETTINGS_FILE)) {
            properties.store(out, "Tetris Custom Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Setting> loadSettings() {
        List<Setting> settings = new ArrayList<>();
        Set<String> settingNames = new HashSet<>();

        for (String key : properties.stringPropertyNames()) {
            if (key.endsWith(".name")) {
                String baseName = key.substring(0, key.length() - 5);
                String name = properties.getProperty(key);
                boolean selected = Boolean.parseBoolean(properties.getProperty(baseName + ".selected", "false"));
                
                Setting setting = new Setting(name);
                setting.setSelected(selected);
                settings.add(setting);
                settingNames.add(name);
            }
        }

        return settings;
    }

    public void saveSettings(List<Setting> settings) {
        properties.clear();
        for (Setting setting : settings) {
            String baseKey = setting.getKey();
            properties.setProperty(baseKey + ".name", setting.getName());
            properties.setProperty(baseKey + ".selected", String.valueOf(setting.isSelected()));
            
            // Save configurations
            Map<String, String> configs = setting.getConfigurations();
            for (Map.Entry<String, String> entry : configs.entrySet()) {
                properties.setProperty(baseKey + ".config." + entry.getKey(), entry.getValue());
            }
        }
        saveProperties();
    }

    public Setting getActiveSetting() {
        return loadSettings().stream()
            .filter(Setting::isSelected)
            .findFirst()
            .orElse(null);
    }
}
