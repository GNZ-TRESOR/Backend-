package rw.health.ubuzima.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.health.ubuzima.dto.request.UserCreateRequest;
import rw.health.ubuzima.dto.response.UserResponse;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.enums.UserRole;
import rw.health.ubuzima.enums.UserStatus;
import rw.health.ubuzima.exception.ResourceNotFoundException;
import rw.health.ubuzima.exception.DuplicateResourceException;
import rw.health.ubuzima.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse createUser(UserCreateRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User with email " + request.getEmail() + " already exists");
        }
        
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("User with phone " + request.getPhone() + " already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : UserRole.CLIENT);
        user.setStatus(UserStatus.ACTIVE);
        user.setGender(request.getGender());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setDistrict(request.getDistrict());
        user.setSector(request.getSector());
        user.setCell(request.getCell());
        user.setVillage(request.getVillage());
        user.setEmergencyContact(request.getEmergencyContact());
        user.setPreferredLanguage(request.getPreferredLanguage() != null ? request.getPreferredLanguage() : "rw");

        // Auto-verify @gmail.com users
        if (user.getEmail() != null && user.getEmail().toLowerCase().endsWith("@gmail.com")) {
            user.setEmailVerified(true);
        }

        User savedUser = userRepository.save(user);
        return convertToResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return convertToResponse(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByStatus(UserStatus status) {
        return userRepository.findByStatus(status).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse updateUser(Long id, UserCreateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Check for email conflicts
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User with email " + request.getEmail() + " already exists");
        }

        // Check for phone conflicts
        if (!user.getPhone().equals(request.getPhone()) && userRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("User with phone " + request.getPhone() + " already exists");
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        user.setGender(request.getGender());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setDistrict(request.getDistrict());
        user.setSector(request.getSector());
        user.setCell(request.getCell());
        user.setVillage(request.getVillage());
        user.setEmergencyContact(request.getEmergencyContact());
        user.setPreferredLanguage(request.getPreferredLanguage());

        // Auto-verify @gmail.com users on update
        if (user.getEmail() != null && user.getEmail().toLowerCase().endsWith("@gmail.com")) {
            user.setEmailVerified(true);
        }

        User savedUser = userRepository.save(user);
        return convertToResponse(savedUser);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    public void updateLastLogin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        user.setLastLoginAt(LocalDate.now());
        userRepository.save(user);
    }

    public boolean validatePassword(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return passwordEncoder.matches(password, user.getPasswordHash());
    }

    private UserResponse convertToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setGender(user.getGender());
        response.setDateOfBirth(user.getDateOfBirth());
        response.setDistrict(user.getDistrict());
        response.setSector(user.getSector());
        response.setCell(user.getCell());
        response.setVillage(user.getVillage());
        response.setEmergencyContact(user.getEmergencyContact());
        response.setPreferredLanguage(user.getPreferredLanguage());
        response.setProfilePictureUrl(user.getProfilePictureUrl());
        response.setEmailVerified(user.getEmailVerified());
        response.setPhoneVerified(user.getPhoneVerified());
        response.setLastLoginAt(user.getLastLoginAt());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}
