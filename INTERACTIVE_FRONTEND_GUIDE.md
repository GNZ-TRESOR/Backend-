# Interactive Frontend Integration Guide

## Overview
This guide shows how to integrate the interactive success/failure notification system with your Flutter frontend.

## Backend Response Format

All API endpoints now return responses in this standardized format:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "userMessage": "✅ Your appointment has been booked successfully",
  "errorCode": null,
  "data": { ... },
  "metadata": { ... },
  "timestamp": "2025-01-08T19:45:00",
  "userRole": "CLIENT"
}
```

## Error Response Format

```json
{
  "success": false,
  "message": "Operation failed",
  "userMessage": "❌ Appointment unavailable - This time slot is already booked. Please choose another time",
  "errorCode": "APPT_002",
  "data": null,
  "metadata": {
    "guidance": "💡 Try selecting a different time slot or contact the health facility directly"
  },
  "timestamp": "2025-01-08T19:45:00",
  "userRole": "CLIENT"
}
```

## Flutter Integration

### 1. Update API Service

```dart
class ApiResponse<T> {
  final bool success;
  final String message;
  final String userMessage;
  final String? errorCode;
  final T? data;
  final Map<String, dynamic>? metadata;
  final DateTime timestamp;
  final String userRole;

  ApiResponse({
    required this.success,
    required this.message,
    required this.userMessage,
    this.errorCode,
    this.data,
    this.metadata,
    required this.timestamp,
    required this.userRole,
  });

  factory ApiResponse.fromJson(Map<String, dynamic> json, T Function(Map<String, dynamic>) fromJsonT) {
    return ApiResponse<T>(
      success: json['success'],
      message: json['message'],
      userMessage: json['userMessage'],
      errorCode: json['errorCode'],
      data: json['data'] != null ? fromJsonT(json['data']) : null,
      metadata: json['metadata'],
      timestamp: DateTime.parse(json['timestamp']),
      userRole: json['userRole'],
    );
  }
}
```

### 2. Create Notification Widget

```dart
class InteractiveNotification extends StatelessWidget {
  final ApiResponse response;
  final VoidCallback? onDismiss;

  const InteractiveNotification({
    Key? key,
    required this.response,
    this.onDismiss,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: EdgeInsets.all(16),
      padding: EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: response.success ? Colors.green.shade50 : Colors.red.shade50,
        border: Border.all(
          color: response.success ? Colors.green : Colors.red,
          width: 1,
        ),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Row(
        children: [
          Icon(
            response.success ? Icons.check_circle : Icons.error,
            color: response.success ? Colors.green : Colors.red,
            size: 24,
          ),
          SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  response.userMessage,
                  style: TextStyle(
                    fontWeight: FontWeight.w600,
                    color: response.success ? Colors.green.shade800 : Colors.red.shade800,
                  ),
                ),
                if (response.metadata?['guidance'] != null) ...[
                  SizedBox(height: 8),
                  Text(
                    response.metadata!['guidance'],
                    style: TextStyle(
                      fontSize: 12,
                      color: Colors.grey.shade600,
                    ),
                  ),
                ],
              ],
            ),
          ),
          if (onDismiss != null)
            IconButton(
              icon: Icon(Icons.close, size: 20),
              onPressed: onDismiss,
            ),
        ],
      ),
    );
  }
}
```

### 3. Show Notifications in UI

```dart
class NotificationService {
  static void showInteractiveNotification(BuildContext context, ApiResponse response) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: InteractiveNotification(response: response),
        backgroundColor: Colors.transparent,
        elevation: 0,
        duration: Duration(seconds: response.success ? 3 : 5),
      ),
    );
  }

  static void showRoleBasedNotification(BuildContext context, ApiResponse response) {
    String rolePrefix = _getRolePrefix(response.userRole);
    
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Row(
          children: [
            Icon(
              response.success ? Icons.check_circle : Icons.error,
              color: response.success ? Colors.green : Colors.red,
            ),
            SizedBox(width: 8),
            Text(rolePrefix),
          ],
        ),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(response.userMessage),
            if (response.metadata?['guidance'] != null) ...[
              SizedBox(height: 16),
              Container(
                padding: EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: Colors.blue.shade50,
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Text(
                  response.metadata!['guidance'],
                  style: TextStyle(color: Colors.blue.shade800),
                ),
              ),
            ],
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: Text('OK'),
          ),
        ],
      ),
    );
  }

  static String _getRolePrefix(String userRole) {
    switch (userRole) {
      case 'ADMIN':
        return 'Admin Dashboard';
      case 'HEALTH_WORKER':
        return 'Health Worker';
      case 'CLIENT':
        return 'Ubuzima Health';
      default:
        return 'Notification';
    }
  }
}
```

### 4. Usage Examples

```dart
// In your API calls
Future<void> bookAppointment(AppointmentRequest request) async {
  try {
    final response = await apiService.post<Map<String, dynamic>>(
      '/appointments',
      request.toJson(),
    );

    final apiResponse = ApiResponse.fromJson(
      response.data,
      (json) => json,
    );

    // Show interactive notification
    NotificationService.showInteractiveNotification(context, apiResponse);

    if (apiResponse.success) {
      // Handle success - maybe navigate to confirmation page
      Navigator.pushNamed(context, '/appointment-confirmation');
    }
  } catch (e) {
    // Handle network errors
    final errorResponse = ApiResponse<Map<String, dynamic>>(
      success: false,
      message: 'Network error',
      userMessage: '❌ Connection failed - Please check your internet connection',
      errorCode: 'NETWORK_ERROR',
      timestamp: DateTime.now(),
      userRole: 'CLIENT',
      metadata: {
        'guidance': '💡 Check your internet connection or try again in a few minutes'
      },
    );
    
    NotificationService.showInteractiveNotification(context, errorResponse);
  }
}
```

## Role-Specific UI Customization

### Admin Dashboard
- Use admin-specific colors (e.g., purple theme)
- Show detailed error codes and technical information
- Include system-level guidance

### Health Worker Interface
- Use professional medical colors (e.g., blue theme)
- Show patient-related context
- Include medical workflow guidance

### Client Interface
- Use friendly, approachable colors (e.g., green theme)
- Show simple, clear messages
- Include helpful next steps

## Error Code Handling

```dart
class ErrorCodeHandler {
  static Widget getErrorIcon(String? errorCode) {
    switch (errorCode) {
      case 'AUTH_001':
      case 'AUTH_003':
        return Icon(Icons.lock, color: Colors.red);
      case 'APPT_002':
        return Icon(Icons.schedule, color: Colors.orange);
      case 'HEALTH_001':
        return Icon(Icons.medical_services, color: Colors.red);
      default:
        return Icon(Icons.error, color: Colors.red);
    }
  }

  static Color getErrorColor(String? errorCode) {
    switch (errorCode) {
      case 'AUTH_001':
      case 'AUTH_003':
        return Colors.red;
      case 'APPT_002':
        return Colors.orange;
      case 'HEALTH_001':
        return Colors.purple;
      default:
        return Colors.red;
    }
  }
}
```

This integration provides a comprehensive, user-friendly notification system that gives precise feedback for every operation in your Ubuzima app.
