package rw.health.ubuzima.constants;

public class ErrorCodes {
    
    // Authentication & Authorization
    public static final String AUTH_INVALID_CREDENTIALS = "AUTH_001";
    public static final String AUTH_TOKEN_EXPIRED = "AUTH_002";
    public static final String AUTH_TOKEN_INVALID = "AUTH_003";
    public static final String AUTH_INSUFFICIENT_PERMISSIONS = "AUTH_004";
    public static final String AUTH_USER_NOT_FOUND = "AUTH_005";
    public static final String AUTH_EMAIL_ALREADY_EXISTS = "AUTH_006";
    public static final String AUTH_PHONE_ALREADY_EXISTS = "AUTH_007";
    public static final String AUTH_INVALID_TOKEN = "AUTH_008";
    public static final String AUTH_EMAIL_NOT_FOUND = "AUTH_009";
    
    // User Management
    public static final String USER_NOT_FOUND = "USER_001";
    public static final String USER_INVALID_DATA = "USER_002";
    public static final String USER_UPDATE_FAILED = "USER_003";
    public static final String USER_DELETE_FAILED = "USER_004";
    public static final String USER_PASSWORD_WEAK = "USER_005";
    public static final String USER_PASSWORD_MISMATCH = "USER_006";
    
    // Health Records
    public static final String HEALTH_RECORD_NOT_FOUND = "HEALTH_001";
    public static final String HEALTH_RECORD_INVALID_DATA = "HEALTH_002";
    public static final String HEALTH_RECORD_ACCESS_DENIED = "HEALTH_003";
    public static final String HEALTH_RECORD_CREATE_FAILED = "HEALTH_004";
    public static final String HEALTH_RECORD_UPDATE_FAILED = "HEALTH_005";
    
    // Appointments
    public static final String APPOINTMENT_NOT_FOUND = "APPT_001";
    public static final String APPOINTMENT_SLOT_UNAVAILABLE = "APPT_002";
    public static final String APPOINTMENT_PAST_DATE = "APPT_003";
    public static final String APPOINTMENT_ALREADY_BOOKED = "APPT_004";
    public static final String APPOINTMENT_CANCEL_FAILED = "APPT_005";
    public static final String APPOINTMENT_INVALID_TIME = "APPT_006";
    
    // Medications
    public static final String MEDICATION_NOT_FOUND = "MED_001";
    public static final String MEDICATION_INVALID_DOSAGE = "MED_002";
    public static final String MEDICATION_INTERACTION_WARNING = "MED_003";
    public static final String MEDICATION_EXPIRED = "MED_004";
    
    // Contraception
    public static final String CONTRACEPTION_NOT_FOUND = "CONTRA_001";
    public static final String CONTRACEPTION_INVALID_TYPE = "CONTRA_002";
    public static final String CONTRACEPTION_EFFECTIVENESS_LOW = "CONTRA_003";
    
    // Menstrual Cycle
    public static final String CYCLE_INVALID_DATES = "CYCLE_001";
    public static final String CYCLE_DATA_INCONSISTENT = "CYCLE_002";
    public static final String CYCLE_PREDICTION_FAILED = "CYCLE_003";
    
    // Notifications
    public static final String NOTIFICATION_SEND_FAILED = "NOTIF_001";
    public static final String NOTIFICATION_INVALID_TYPE = "NOTIF_002";
    public static final String NOTIFICATION_USER_UNREACHABLE = "NOTIF_003";
    
    // Messages
    public static final String MESSAGE_SEND_FAILED = "MSG_001";
    public static final String MESSAGE_RECIPIENT_NOT_FOUND = "MSG_002";
    public static final String MESSAGE_CONTENT_INVALID = "MSG_003";
    
    // Health Facilities
    public static final String FACILITY_NOT_FOUND = "FAC_001";
    public static final String FACILITY_NO_AVAILABILITY = "FAC_002";
    public static final String FACILITY_ACCESS_RESTRICTED = "FAC_003";
    
    // System Errors
    public static final String SYSTEM_DATABASE_ERROR = "SYS_001";
    public static final String SYSTEM_NETWORK_ERROR = "SYS_002";
    public static final String SYSTEM_VALIDATION_ERROR = "SYS_003";
    public static final String SYSTEM_UNKNOWN_ERROR = "SYS_999";
    
    // Validation Errors
    public static final String VALIDATION_REQUIRED_FIELD = "VAL_001";
    public static final String VALIDATION_INVALID_FORMAT = "VAL_002";
    public static final String VALIDATION_OUT_OF_RANGE = "VAL_003";
    public static final String VALIDATION_DUPLICATE_ENTRY = "VAL_004";
}
