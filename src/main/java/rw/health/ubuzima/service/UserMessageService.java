package rw.health.ubuzima.service;

import org.springframework.stereotype.Service;
import rw.health.ubuzima.constants.ErrorCodes;
import rw.health.ubuzima.enums.UserRole;

@Service
public class UserMessageService {
    
    // Success Messages by Role
    public String getSuccessMessage(UserRole role, String operation, Object data) {
        return switch (role) {
            case ADMIN -> getAdminSuccessMessage(operation, data);
            case HEALTH_WORKER -> getHealthWorkerSuccessMessage(operation, data);
            case CLIENT -> getClientSuccessMessage(operation, data);
        };
    }
    
    // Error Messages by Role and Error Code
    public String getErrorMessage(UserRole role, String operation, String errorCode, String details) {
        return switch (role) {
            case ADMIN -> getAdminErrorMessage(operation, errorCode, details);
            case HEALTH_WORKER -> getHealthWorkerErrorMessage(operation, errorCode, details);
            case CLIENT -> getClientErrorMessage(operation, errorCode, details);
        };
    }
    
    // Admin Success Messages
    private String getAdminSuccessMessage(String operation, Object data) {
        return switch (operation) {
            case "USER_CREATE" -> "✅ Admin: New user account created successfully";
            case "USER_UPDATE" -> "✅ Admin: User profile updated successfully";
            case "USER_DELETE" -> "✅ Admin: User account deactivated successfully";
            case "SYSTEM_BACKUP" -> "✅ Admin: System backup completed successfully";
            case "REPORT_GENERATE" -> "✅ Admin: System report generated successfully";
            case "FACILITY_MANAGE" -> "✅ Admin: Health facility updated successfully";
            default -> "✅ Admin: Operation completed successfully";
        };
    }
    
    // Health Worker Success Messages
    private String getHealthWorkerSuccessMessage(String operation, Object data) {
        return switch (operation) {
            case "APPOINTMENT_CREATE" -> "✅ Health Worker: Appointment scheduled successfully";
            case "APPOINTMENT_UPDATE" -> "✅ Health Worker: Appointment updated successfully";
            case "HEALTH_RECORD_CREATE" -> "✅ Health Worker: Patient health record added successfully";
            case "HEALTH_RECORD_UPDATE" -> "✅ Health Worker: Patient health record updated successfully";
            case "CLIENT_MESSAGE" -> "✅ Health Worker: Message sent to client successfully";
            case "CONSULTATION_COMPLETE" -> "✅ Health Worker: Consultation completed and recorded";
            default -> "✅ Health Worker: Operation completed successfully";
        };
    }
    
    // Client Success Messages
    private String getClientSuccessMessage(String operation, Object data) {
        return switch (operation) {
            case "APPOINTMENT_BOOK" -> "✅ Your appointment has been booked successfully";
            case "APPOINTMENT_CANCEL" -> "✅ Your appointment has been cancelled successfully";
            case "PROFILE_UPDATE" -> "✅ Your profile has been updated successfully";
            case "HEALTH_RECORD_ADD" -> "✅ Your health information has been recorded successfully";
            case "MEDICATION_ADD" -> "✅ Your medication has been added to your records";
            case "CYCLE_TRACK" -> "✅ Your menstrual cycle has been tracked successfully";
            case "MESSAGE_SEND" -> "✅ Your message has been sent successfully";
            default -> "✅ Operation completed successfully";
        };
    }
    
    // Admin Error Messages
    private String getAdminErrorMessage(String operation, String errorCode, String details) {
        return switch (errorCode) {
            case ErrorCodes.AUTH_INSUFFICIENT_PERMISSIONS -> 
                "❌ Admin: Access denied - Insufficient administrative privileges";
            case ErrorCodes.USER_NOT_FOUND -> 
                "❌ Admin: User not found - The specified user account does not exist";
            case ErrorCodes.SYSTEM_DATABASE_ERROR -> 
                "❌ Admin: Database error - Please check system connectivity and try again";
            case ErrorCodes.VALIDATION_DUPLICATE_ENTRY -> 
                "❌ Admin: Duplicate entry - This record already exists in the system";
            default -> String.format("❌ Admin: %s failed - %s", operation, details);
        };
    }
    
    // Health Worker Error Messages
    private String getHealthWorkerErrorMessage(String operation, String errorCode, String details) {
        return switch (errorCode) {
            case ErrorCodes.APPOINTMENT_SLOT_UNAVAILABLE -> 
                "❌ Health Worker: Appointment slot unavailable - Please select a different time";
            case ErrorCodes.HEALTH_RECORD_ACCESS_DENIED -> 
                "❌ Health Worker: Access denied - You don't have permission to view this patient's records";
            case ErrorCodes.USER_NOT_FOUND -> 
                "❌ Health Worker: Patient not found - Please verify the patient information";
            case ErrorCodes.APPOINTMENT_PAST_DATE -> 
                "❌ Health Worker: Invalid date - Cannot schedule appointments in the past";
            case ErrorCodes.MEDICATION_INTERACTION_WARNING -> 
                "❌ Health Worker: Medication interaction detected - Please review before prescribing";
            default -> String.format("❌ Health Worker: %s failed - %s", operation, details);
        };
    }
    
    // Client Error Messages
    private String getClientErrorMessage(String operation, String errorCode, String details) {
        return switch (errorCode) {
            case ErrorCodes.AUTH_INVALID_CREDENTIALS -> 
                "❌ Login failed - Please check your email and password";
            case ErrorCodes.APPOINTMENT_SLOT_UNAVAILABLE -> 
                "❌ Appointment unavailable - This time slot is already booked. Please choose another time";
            case ErrorCodes.APPOINTMENT_PAST_DATE -> 
                "❌ Invalid date - You cannot book appointments for past dates";
            case ErrorCodes.USER_PASSWORD_WEAK -> 
                "❌ Weak password - Please use at least 8 characters with numbers and special characters";
            case ErrorCodes.HEALTH_RECORD_INVALID_DATA -> 
                "❌ Invalid health data - Please check your entries and try again";
            case ErrorCodes.FACILITY_NOT_FOUND -> 
                "❌ Health facility not found - Please select a valid health center";
            case ErrorCodes.SYSTEM_NETWORK_ERROR -> 
                "❌ Connection error - Please check your internet connection and try again";
            default -> String.format("❌ %s failed - %s", operation, details);
        };
    }
    
    // Get operation-specific guidance
    public String getOperationGuidance(UserRole role, String operation, String errorCode) {
        return switch (errorCode) {
            case ErrorCodes.APPOINTMENT_SLOT_UNAVAILABLE -> 
                "💡 Try selecting a different time slot or contact the health facility directly";
            case ErrorCodes.USER_PASSWORD_WEAK -> 
                "💡 Use a combination of uppercase, lowercase, numbers, and special characters";
            case ErrorCodes.HEALTH_RECORD_INVALID_DATA -> 
                "💡 Ensure all required fields are filled and values are within normal ranges";
            case ErrorCodes.SYSTEM_NETWORK_ERROR -> 
                "💡 Check your internet connection or try again in a few minutes";
            default -> "💡 If the problem persists, please contact support";
        };
    }
}
