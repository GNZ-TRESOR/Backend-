package rw.health.ubuzima.constants;

public class NotificationConstants {
    
    // Priority levels
    public static final int PRIORITY_LOW = 1;
    public static final int PRIORITY_MEDIUM = 2;
    public static final int PRIORITY_HIGH = 3;
    public static final int PRIORITY_CRITICAL = 4;
    
    // Icons
    public static final String ICON_SUCCESS = "✅";
    public static final String ICON_ERROR = "❌";
    public static final String ICON_WARNING = "⚠️";
    public static final String ICON_INFO = "ℹ️";
    public static final String ICON_APPOINTMENT = "📅";
    public static final String ICON_MEDICATION = "💊";
    public static final String ICON_HEALTH = "🏥";
    public static final String ICON_EMERGENCY = "🚨";
    public static final String ICON_MESSAGE = "💬";
    public static final String ICON_EDUCATION = "📚";
    
    // Colors (for frontend use)
    public static final String COLOR_SUCCESS = "#4CAF50";
    public static final String COLOR_ERROR = "#F44336";
    public static final String COLOR_WARNING = "#FF9800";
    public static final String COLOR_INFO = "#2196F3";
    public static final String COLOR_APPOINTMENT = "#9C27B0";
    public static final String COLOR_MEDICATION = "#E91E63";
    public static final String COLOR_HEALTH = "#00BCD4";
    public static final String COLOR_EMERGENCY = "#FF5722";
    
    // Notification categories
    public static final String CATEGORY_INTERACTIVE = "interactive";
    public static final String CATEGORY_REMINDER = "reminder";
    public static final String CATEGORY_ALERT = "alert";
    public static final String CATEGORY_SYSTEM = "system";
    public static final String CATEGORY_COMMUNICATION = "communication";
    
    // Auto-dismiss timeouts (in seconds)
    public static final int TIMEOUT_SUCCESS = 3;
    public static final int TIMEOUT_ERROR = 5;
    public static final int TIMEOUT_WARNING = 4;
    public static final int TIMEOUT_INFO = 3;
    public static final int TIMEOUT_CRITICAL = 0; // Never auto-dismiss
}
