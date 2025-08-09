package rw.health.ubuzima.service;

import rw.health.ubuzima.entity.ContraceptionMethod;
import rw.health.ubuzima.entity.SideEffectReport;
import rw.health.ubuzima.enums.ContraceptionType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ContraceptionService {

    /**
     * Get all available contraception methods (not assigned to any user)
     */
    List<ContraceptionMethod> getAvailableMethods();

    /**
     * Get all contraception methods for a specific user
     */
    List<ContraceptionMethod> getUserMethods(Long userId);

    /**
     * Get the active contraception method for a user
     */
    Optional<ContraceptionMethod> getActiveMethod(Long userId);

    /**
     * Create a new contraception method
     */
    ContraceptionMethod createMethod(ContraceptionMethod method);

    /**
     * Update an existing contraception method
     */
    ContraceptionMethod updateMethod(Long methodId, ContraceptionMethod method);

    /**
     * Deactivate a contraception method
     */
    void deactivateMethod(Long methodId);

    /**
     * Delete a contraception method
     */
    void deleteMethod(Long methodId);

    /**
     * Get side effects for a specific contraception method
     */
    List<SideEffectReport> getSideEffects(Long methodId);

    /**
     * Create a new side effect report
     */
    SideEffectReport createSideEffect(SideEffectReport sideEffect);

    /**
     * Delete a side effect report
     */
    void deleteSideEffect(Long sideEffectId);

    /**
     * Get all contraception types
     */
    List<ContraceptionType> getContraceptionTypes();

    /**
     * Get contraception methods by type
     */
    List<ContraceptionMethod> getMethodsByType(ContraceptionType type);

    /**
     * Get contraception methods by user and type
     */
    List<ContraceptionMethod> getMethodsByUserAndType(Long userId, ContraceptionType type);

    /**
     * Check if user has an active contraception method
     */
    boolean hasActiveMethod(Long userId);

    /**
     * Get contraception methods by prescriber
     */
    List<ContraceptionMethod> getMethodsByPrescriber(String prescribedBy);

    /**
     * Get contraception methods by date range
     */
    List<ContraceptionMethod> getMethodsByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Get side effects by user
     */
    List<SideEffectReport> getSideEffectsByUser(Long userId);

    /**
     * Get side effects by severity
     */
    List<SideEffectReport> getSideEffectsBySeverity(SideEffectReport.SideEffectSeverity severity);

    /**
     * Get ongoing side effects for a user
     */
    List<SideEffectReport> getOngoingSideEffects(Long userId);

    /**
     * Add a side effect to a contraception method
     */
    void addSideEffect(Long methodId, String sideEffect);

    /**
     * Remove a side effect from a contraception method
     */
    void removeSideEffect(Long methodId, String sideEffect);

    /**
     * Get methods that need follow-up appointments
     */
    List<ContraceptionMethod> getMethodsNeedingFollowUp();

    /**
     * Get expired contraception methods
     */
    List<ContraceptionMethod> getExpiredMethods();
} 