package rw.health.ubuzima.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import rw.health.ubuzima.enums.SettingCategory;
import rw.health.ubuzima.enums.DataType;

@Entity
@Table(name = "user_settings")
@Data
@EqualsAndHashCode(callSuper = true)
public class UserSettings extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "setting_category", nullable = false)
    private SettingCategory settingCategory;

    @Column(name = "setting_key", nullable = false, length = 100)
    private String settingKey;

    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String settingValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type")
    private DataType dataType = DataType.STRING;

    // Constructors
    public UserSettings() {}

    public UserSettings(User user, SettingCategory category, String key, String value, DataType dataType) {
        this.user = user;
        this.settingCategory = category;
        this.settingKey = key;
        this.settingValue = value;
        this.dataType = dataType;
    }

    // Helper methods for type conversion
    public Boolean getBooleanValue() {
        if (dataType == DataType.BOOLEAN && settingValue != null) {
            return Boolean.parseBoolean(settingValue);
        }
        return null;
    }

    public Integer getIntegerValue() {
        if (dataType == DataType.INTEGER && settingValue != null) {
            try {
                return Integer.parseInt(settingValue);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public Double getDecimalValue() {
        if (dataType == DataType.DECIMAL && settingValue != null) {
            try {
                return Double.parseDouble(settingValue);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public void setBooleanValue(Boolean value) {
        this.dataType = DataType.BOOLEAN;
        this.settingValue = value != null ? value.toString() : null;
    }

    public void setIntegerValue(Integer value) {
        this.dataType = DataType.INTEGER;
        this.settingValue = value != null ? value.toString() : null;
    }

    public void setDecimalValue(Double value) {
        this.dataType = DataType.DECIMAL;
        this.settingValue = value != null ? value.toString() : null;
    }
}
