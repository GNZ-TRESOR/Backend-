package rw.health.ubuzima.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import rw.health.ubuzima.enums.StiTestType;
import rw.health.ubuzima.enums.TestResultStatus;

import java.time.LocalDate;

@Entity
@Table(name = "sti_test_records")
@Data
@EqualsAndHashCode(callSuper = true)
public class StiTestRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_type", nullable = false)
    private StiTestType testType;

    @Column(name = "test_date", nullable = false)
    private LocalDate testDate;

    @Column(name = "test_location")
    private String testLocation;

    @Column(name = "test_provider")
    private String testProvider;

    @Enumerated(EnumType.STRING)
    @Column(name = "result_status")
    private TestResultStatus resultStatus = TestResultStatus.PENDING;

    @Column(name = "result_date")
    private LocalDate resultDate;

    @Column(name = "follow_up_required")
    private Boolean followUpRequired = false;

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_confidential")
    private Boolean isConfidential = true;

    // Constructors
    public StiTestRecord() {}

    public StiTestRecord(User user, StiTestType testType, LocalDate testDate) {
        this.user = user;
        this.testType = testType;
        this.testDate = testDate;
    }
}
