package rw.health.ubuzima.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.entity.UserSettings;
import rw.health.ubuzima.enums.SettingCategory;
import rw.health.ubuzima.enums.DataType;
import rw.health.ubuzima.repository.UserRepository;
import rw.health.ubuzima.repository.UserSettingsRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user-settings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserSettingsController {

    private final UserSettingsRepository userSettingsRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserSettings(
            @RequestParam Long userId,
            @RequestParam(required = false) String category) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            List<UserSettings> settings;
            if (category != null) {
                SettingCategory settingCategory = SettingCategory.valueOf(category.toUpperCase());
                settings = userSettingsRepository.findByUserAndSettingCategory(user, settingCategory);
            } else {
                settings = userSettingsRepository.findByUser(user);
            }

            Map<String, Object> settingsMap = convertSettingsToMap(settings);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "settings", settingsMap
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to fetch settings: " + e.getMessage()
            ));
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> saveUserSettings(
            @RequestParam Long userId,
            @RequestBody Map<String, Object> settingsData) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            saveSettingsFromMap(user, settingsData);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Settings saved successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to save settings: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{category}")
    public ResponseEntity<Map<String, Object>> updateCategorySettings(
            @PathVariable String category,
            @RequestParam Long userId,
            @RequestBody Map<String, Object> settingsData) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            SettingCategory settingCategory = SettingCategory.valueOf(category.toUpperCase());
            
            // Delete existing settings for this category
            userSettingsRepository.deleteByUserAndSettingCategory(user, settingCategory);

            // Save new settings
            for (Map.Entry<String, Object> entry : settingsData.entrySet()) {
                UserSettings setting = new UserSettings();
                setting.setUser(user);
                setting.setSettingCategory(settingCategory);
                setting.setSettingKey(entry.getKey());
                
                Object value = entry.getValue();
                if (value instanceof Boolean) {
                    setting.setBooleanValue((Boolean) value);
                } else if (value instanceof Integer) {
                    setting.setIntegerValue((Integer) value);
                } else if (value instanceof Double) {
                    setting.setDecimalValue((Double) value);
                } else {
                    setting.setSettingValue(value.toString());
                    setting.setDataType(DataType.STRING);
                }

                userSettingsRepository.save(setting);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Settings updated successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to update settings: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{category}")
    public ResponseEntity<Map<String, Object>> deleteCategorySettings(
            @PathVariable String category,
            @RequestParam Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            SettingCategory settingCategory = SettingCategory.valueOf(category.toUpperCase());
            userSettingsRepository.deleteByUserAndSettingCategory(user, settingCategory);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Settings deleted successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to delete settings: " + e.getMessage()
            ));
        }
    }

    private Map<String, Object> convertSettingsToMap(List<UserSettings> settings) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Map<String, Object>> categories = new HashMap<>();

        for (UserSettings setting : settings) {
            String categoryName = setting.getSettingCategory().name().toLowerCase();
            categories.computeIfAbsent(categoryName, k -> new HashMap<>());
            
            Object value;
            switch (setting.getDataType()) {
                case BOOLEAN:
                    value = setting.getBooleanValue();
                    break;
                case INTEGER:
                    value = setting.getIntegerValue();
                    break;
                case DECIMAL:
                    value = setting.getDecimalValue();
                    break;
                default:
                    value = setting.getSettingValue();
                    break;
            }
            
            categories.get(categoryName).put(setting.getSettingKey(), value);
        }

        result.putAll(categories);
        return result;
    }

    private void saveSettingsFromMap(User user, Map<String, Object> settingsData) {
        for (Map.Entry<String, Object> categoryEntry : settingsData.entrySet()) {
            String categoryName = categoryEntry.getKey();
            SettingCategory category = SettingCategory.valueOf(categoryName.toUpperCase());
            
            if (categoryEntry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> categorySettings = (Map<String, Object>) categoryEntry.getValue();
                
                for (Map.Entry<String, Object> settingEntry : categorySettings.entrySet()) {
                    UserSettings setting = userSettingsRepository
                        .findByUserAndSettingCategoryAndSettingKey(user, category, settingEntry.getKey())
                        .orElse(new UserSettings());
                    
                    setting.setUser(user);
                    setting.setSettingCategory(category);
                    setting.setSettingKey(settingEntry.getKey());
                    
                    Object value = settingEntry.getValue();
                    if (value instanceof Boolean) {
                        setting.setBooleanValue((Boolean) value);
                    } else if (value instanceof Integer) {
                        setting.setIntegerValue((Integer) value);
                    } else if (value instanceof Double) {
                        setting.setDecimalValue((Double) value);
                    } else {
                        setting.setSettingValue(value.toString());
                        setting.setDataType(DataType.STRING);
                    }

                    userSettingsRepository.save(setting);
                }
            }
        }
    }
}
