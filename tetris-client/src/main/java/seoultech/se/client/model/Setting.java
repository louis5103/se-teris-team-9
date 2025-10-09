package seoultech.se.client.model;

import java.util.HashMap;
import java.util.Map;

public class Setting {
    private String name;
    private boolean isSelected;
    private String key;
    private Map<String, String> configurations;

    public Setting(String name) {
        this.name = name;
        this.isSelected = false;
        this.key = generateKey(name);
        this.configurations = new HashMap<>();
    }

    public String getName() { return name; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
    public String getKey() { return key; }

    private String generateKey(String name){
        return name.toLowerCase()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-z0-9_]", "");
    }

    public Map<String, String> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(Map<String, String> configurations) {
        this.configurations = configurations;
    }
}
