package rw.health.ubuzima.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import rw.health.ubuzima.enums.UserRole;

import java.util.Optional;

@Component
public class SecurityUtils {

    /**
     * Get the current authenticated user's ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();
        
        // If using JWT with custom claims
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            // Extract user ID from username or custom field
            // This assumes the username contains the user ID or we have a custom UserDetails implementation
            try {
                return Long.parseLong(userDetails.getUsername());
            } catch (NumberFormatException e) {
                // If username is not the ID, we need to look it up
                // For now, return a default value - this should be implemented based on your UserDetails
                throw new SecurityException("Unable to extract user ID from authentication");
            }
        }
        
        // If using simple authentication with user ID as principal
        if (principal instanceof Long) {
            return (Long) principal;
        }
        
        // If using string-based authentication
        if (principal instanceof String) {
            try {
                return Long.parseLong((String) principal);
            } catch (NumberFormatException e) {
                throw new SecurityException("Unable to parse user ID from authentication");
            }
        }
        
        throw new SecurityException("Unable to determine current user ID");
    }

    /**
     * Get the current authenticated user's email
     */
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        
        if (principal instanceof String) {
            return (String) principal;
        }
        
        throw new SecurityException("Unable to determine current user email");
    }

    /**
     * Get the current authenticated user's role
     */
    public static UserRole getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("No authenticated user found");
        }

        return authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(role -> role.startsWith("ROLE_"))
                .map(role -> role.substring(5)) // Remove "ROLE_" prefix
                .map(UserRole::valueOf)
                .findFirst()
                .orElseThrow(() -> new SecurityException("No valid role found for current user"));
    }

    /**
     * Check if the current user has a specific role
     */
    public static boolean hasRole(UserRole role) {
        try {
            UserRole currentRole = getCurrentUserRole();
            return currentRole == role;
        } catch (SecurityException e) {
            return false;
        }
    }

    /**
     * Check if the current user is a client
     */
    public static boolean isClient() {
        return hasRole(UserRole.CLIENT);
    }

    /**
     * Check if the current user is a health worker
     */
    public static boolean isHealthWorker() {
        return hasRole(UserRole.HEALTH_WORKER);
    }

    /**
     * Check if the current user is an admin
     */
    public static boolean isAdmin() {
        return hasRole(UserRole.ADMIN);
    }

    /**
     * Check if the current user has any of the specified roles
     */
    public static boolean hasAnyRole(UserRole... roles) {
        try {
            UserRole currentRole = getCurrentUserRole();
            for (UserRole role : roles) {
                if (currentRole == role) {
                    return true;
                }
            }
            return false;
        } catch (SecurityException e) {
            return false;
        }
    }

    /**
     * Get the current authentication object
     */
    public static Optional<Authentication> getCurrentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Optional.ofNullable(authentication);
    }

    /**
     * Check if there is an authenticated user
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && 
               !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * Get current user ID safely (returns null if not authenticated)
     */
    public static Long getCurrentUserIdSafe() {
        try {
            return getCurrentUserId();
        } catch (SecurityException e) {
            return null;
        }
    }

    /**
     * Get current user email safely (returns null if not authenticated)
     */
    public static String getCurrentUserEmailSafe() {
        try {
            return getCurrentUserEmail();
        } catch (SecurityException e) {
            return null;
        }
    }

    /**
     * Get current user role safely (returns null if not authenticated)
     */
    public static UserRole getCurrentUserRoleSafe() {
        try {
            return getCurrentUserRole();
        } catch (SecurityException e) {
            return null;
        }
    }

    /**
     * Validate that the current user can access a specific user's data
     * This is useful for health workers accessing client data
     */
    public static boolean canAccessUserData(Long targetUserId) {
        try {
            Long currentUserId = getCurrentUserId();
            UserRole currentRole = getCurrentUserRole();
            
            // Users can always access their own data
            if (currentUserId.equals(targetUserId)) {
                return true;
            }
            
            // Admins can access any user's data
            if (currentRole == UserRole.ADMIN) {
                return true;
            }
            
            // Health workers can access their assigned clients' data
            // This would need to be implemented with actual assignment checking
            if (currentRole == UserRole.HEALTH_WORKER) {
                // TODO: Implement actual assignment checking
                return true; // For now, allow all health workers
            }
            
            return false;
        } catch (SecurityException e) {
            return false;
        }
    }
}
