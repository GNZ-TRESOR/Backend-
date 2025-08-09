package rw.health.ubuzima.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.health.ubuzima.entity.ContraceptionMethod;
import rw.health.ubuzima.entity.SideEffectReport;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.enums.ContraceptionType;
import rw.health.ubuzima.exception.ResourceNotFoundException;
import rw.health.ubuzima.repository.ContraceptionMethodRepository;
import rw.health.ubuzima.repository.SideEffectReportRepository;
import rw.health.ubuzima.repository.UserRepository;
import rw.health.ubuzima.service.ContraceptionService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ContraceptionServiceImpl implements ContraceptionService {

    private final ContraceptionMethodRepository contraceptionMethodRepository;
    private final SideEffectReportRepository sideEffectReportRepository;
    private final UserRepository userRepository;

    @Override
    public List<ContraceptionMethod> getAvailableMethods() {
        return contraceptionMethodRepository.findByUserIdIsNull();
    }

    @Override
    public List<ContraceptionMethod> getUserMethods(Long userId) {
        return contraceptionMethodRepository.findByUserId(userId);
    }

    @Override
    public Optional<ContraceptionMethod> getActiveMethod(Long userId) {
        List<ContraceptionMethod> activeMethods = contraceptionMethodRepository.findByUserIdAndIsActiveTrue(userId);
        return activeMethods.isEmpty() ? Optional.empty() : Optional.of(activeMethods.get(0));
    }

    @Override
    public ContraceptionMethod createMethod(ContraceptionMethod method) {
        return contraceptionMethodRepository.save(method);
    }

    @Override
    public ContraceptionMethod updateMethod(Long methodId, ContraceptionMethod method) {
        ContraceptionMethod existingMethod = contraceptionMethodRepository.findById(methodId)
                .orElseThrow(() -> new ResourceNotFoundException("Contraception method not found"));
        
        existingMethod.setName(method.getName());
        existingMethod.setDescription(method.getDescription());
        existingMethod.setStartDate(method.getStartDate());
        existingMethod.setEndDate(method.getEndDate());
        existingMethod.setEffectiveness(method.getEffectiveness());
        existingMethod.setInstructions(method.getInstructions());
        existingMethod.setNextAppointment(method.getNextAppointment());
        existingMethod.setIsActive(method.getIsActive());
        existingMethod.setPrescribedBy(method.getPrescribedBy());
        existingMethod.setAdditionalData(method.getAdditionalData());
        
        return contraceptionMethodRepository.save(existingMethod);
    }

    @Override
    public void deactivateMethod(Long methodId) {
        ContraceptionMethod method = contraceptionMethodRepository.findById(methodId)
                .orElseThrow(() -> new ResourceNotFoundException("Contraception method not found"));
        
        method.setIsActive(false);
        method.setEndDate(LocalDate.now());
        contraceptionMethodRepository.save(method);
    }

    @Override
    public void deleteMethod(Long methodId) {
        contraceptionMethodRepository.deleteById(methodId);
    }

    @Override
    public List<SideEffectReport> getSideEffects(Long methodId) {
        return sideEffectReportRepository.findByContraceptionMethodId(methodId);
    }

    @Override
    public SideEffectReport createSideEffect(SideEffectReport sideEffect) {
        return sideEffectReportRepository.save(sideEffect);
    }

    @Override
    public void deleteSideEffect(Long sideEffectId) {
        sideEffectReportRepository.deleteById(sideEffectId);
    }

    @Override
    public List<ContraceptionType> getContraceptionTypes() {
        return List.of(ContraceptionType.values());
    }

    @Override
    public List<ContraceptionMethod> getMethodsByType(ContraceptionType type) {
        return contraceptionMethodRepository.findByType(type);
    }

    @Override
    public List<ContraceptionMethod> getMethodsByUserAndType(Long userId, ContraceptionType type) {
        return contraceptionMethodRepository.findByUserIdAndType(userId, type);
    }

    @Override
    public boolean hasActiveMethod(Long userId) {
        return contraceptionMethodRepository.existsByUserIdAndIsActiveTrue(userId);
    }

    @Override
    public List<ContraceptionMethod> getMethodsByPrescriber(String prescribedBy) {
        return contraceptionMethodRepository.findByPrescribedBy(prescribedBy);
    }

    @Override
    public List<ContraceptionMethod> getMethodsByDateRange(LocalDate startDate, LocalDate endDate) {
        return contraceptionMethodRepository.findByStartDateBetween(startDate, endDate);
    }

    @Override
    public List<SideEffectReport> getSideEffectsByUser(Long userId) {
        return sideEffectReportRepository.findByUserId(userId);
    }

    @Override
    public List<SideEffectReport> getSideEffectsBySeverity(SideEffectReport.SideEffectSeverity severity) {
        return sideEffectReportRepository.findBySeverity(severity);
    }

    @Override
    public List<SideEffectReport> getOngoingSideEffects(Long userId) {
        return sideEffectReportRepository.findByUserIdAndIsOngoingTrue(userId);
    }

    @Override
    public void addSideEffect(Long methodId, String sideEffect) {
        ContraceptionMethod method = contraceptionMethodRepository.findById(methodId)
                .orElseThrow(() -> new ResourceNotFoundException("Contraception method not found"));
        
        method.getSideEffects().add(sideEffect);
        contraceptionMethodRepository.save(method);
    }

    @Override
    public void removeSideEffect(Long methodId, String sideEffect) {
        ContraceptionMethod method = contraceptionMethodRepository.findById(methodId)
                .orElseThrow(() -> new ResourceNotFoundException("Contraception method not found"));
        
        method.getSideEffects().remove(sideEffect);
        contraceptionMethodRepository.save(method);
    }

    @Override
    public List<ContraceptionMethod> getMethodsNeedingFollowUp() {
        LocalDate today = LocalDate.now();
        return contraceptionMethodRepository.findByNextAppointmentBeforeAndIsActiveTrue(today);
    }

    @Override
    public List<ContraceptionMethod> getExpiredMethods() {
        LocalDate today = LocalDate.now();
        return contraceptionMethodRepository.findByEndDateBeforeAndIsActiveTrue(today);
    }
} 