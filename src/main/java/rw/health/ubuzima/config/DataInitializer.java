package rw.health.ubuzima.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.entity.HealthFacility;
import rw.health.ubuzima.entity.EducationLesson;
import rw.health.ubuzima.entity.MenstrualCycle;
import rw.health.ubuzima.entity.Medication;
import rw.health.ubuzima.entity.ContraceptionMethod;
import rw.health.ubuzima.enums.UserRole;
import rw.health.ubuzima.enums.UserStatus;
import rw.health.ubuzima.enums.FacilityType;
import rw.health.ubuzima.enums.EducationCategory;
import rw.health.ubuzima.enums.EducationLevel;
import rw.health.ubuzima.enums.ContraceptionType;
import rw.health.ubuzima.enums.FlowIntensity;
import rw.health.ubuzima.repository.UserRepository;
import rw.health.ubuzima.repository.HealthFacilityRepository;
import rw.health.ubuzima.repository.EducationLessonRepository;
import rw.health.ubuzima.repository.MenstrualCycleRepository;
import rw.health.ubuzima.repository.MedicationRepository;
import rw.health.ubuzima.repository.ContraceptionMethodRepository;

import java.util.List;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final HealthFacilityRepository healthFacilityRepository;
    private final EducationLessonRepository educationLessonRepository;
    private final MenstrualCycleRepository menstrualCycleRepository;
    private final MedicationRepository medicationRepository;
    private final ContraceptionMethodRepository contraceptionMethodRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeUsers();
        initializeHealthFacilities();
        initializeEducationLessons();
        initializeSampleData();
    }

    private void initializeUsers() {
        // Create admin user if not exists
        if (!userRepository.existsByEmail("admin@ubuzima.rw")) {
            User admin = new User();
            admin.setName("System Administrator");
            admin.setEmail("admin@ubuzima.rw");
            admin.setPhone("+250788000001");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setRole(UserRole.ADMIN);
            admin.setStatus(UserStatus.ACTIVE);
            admin.setDistrict("Kigali");
            admin.setSector("Nyarugenge");
            admin.setCell("Nyarugenge");
            admin.setVillage("Kigali");
            admin.setEmailVerified(true);
            admin.setPhoneVerified(true);
            userRepository.save(admin);
        }

        // Create health worker if not exists
        if (!userRepository.existsByEmail("healthworker@ubuzima.rw")) {
            User healthWorker = new User();
            healthWorker.setName("Dr. Marie Uwimana");
            healthWorker.setEmail("healthworker@ubuzima.rw");
            healthWorker.setPhone("+250788000002");
            healthWorker.setPasswordHash(passwordEncoder.encode("healthworker123"));
            healthWorker.setRole(UserRole.HEALTH_WORKER);
            healthWorker.setStatus(UserStatus.ACTIVE);
            healthWorker.setFacilityId("1");
            healthWorker.setDistrict("Kigali");
            healthWorker.setSector("Gasabo");
            healthWorker.setCell("Kimisagara");
            healthWorker.setVillage("Kimisagara");
            healthWorker.setEmailVerified(true);
            healthWorker.setPhoneVerified(true);
            userRepository.save(healthWorker);
        }

        // Create client if not exists
        if (!userRepository.existsByEmail("client@ubuzima.rw")) {
            User client = new User();
            client.setName("Grace Mukamana");
            client.setEmail("client@ubuzima.rw");
            client.setPhone("+250788000003");
            client.setPasswordHash(passwordEncoder.encode("client123"));
            client.setRole(UserRole.CLIENT);
            client.setStatus(UserStatus.ACTIVE);
            client.setDistrict("Kigali");
            client.setSector("Kicukiro");
            client.setCell("Gahanga");
            client.setVillage("Gahanga");
            client.setEmailVerified(true);
            client.setPhoneVerified(true);
            userRepository.save(client);
        }
    }

    private void initializeHealthFacilities() {
        // Create sample health facilities if not exist
        if (healthFacilityRepository.count() == 0) {
            // Kigali University Teaching Hospital
            HealthFacility chuk = new HealthFacility();
            chuk.setName("Kigali University Teaching Hospital (CHUK)");
            chuk.setFacilityType(FacilityType.HOSPITAL);
            chuk.setAddress("KN 4 Ave, Kigali, Rwanda");
            chuk.setPhoneNumber("+250788300000");
            chuk.setEmail("info@chuk.rw");
            chuk.setLatitude(-1.9441);
            chuk.setLongitude(30.0619);
            chuk.setOperatingHours("24/7");
            chuk.setServicesOffered("Emergency Care, Surgery, Maternity, Family Planning, General Medicine");
            chuk.setIsActive(true);
            healthFacilityRepository.save(chuk);

            // King Faisal Hospital
            HealthFacility kfh = new HealthFacility();
            kfh.setName("King Faisal Hospital");
            kfh.setFacilityType(FacilityType.HOSPITAL);
            kfh.setAddress("KG 544 St, Kigali, Rwanda");
            kfh.setPhoneNumber("+250788500000");
            kfh.setEmail("info@kfh.rw");
            kfh.setLatitude(-1.9536);
            kfh.setLongitude(30.0606);
            kfh.setOperatingHours("24/7");
            kfh.setServicesOffered("Specialized Care, Surgery, Maternity, Family Planning, Cardiology");
            kfh.setIsActive(true);
            healthFacilityRepository.save(kfh);

            // Kimisagara Health Center
            HealthFacility kimisagara = new HealthFacility();
            kimisagara.setName("Kimisagara Health Center");
            kimisagara.setFacilityType(FacilityType.HEALTH_CENTER);
            kimisagara.setAddress("Kimisagara, Nyarugenge, Kigali");
            kimisagara.setPhoneNumber("+250788100000");
            kimisagara.setEmail("kimisagara@moh.gov.rw");
            kimisagara.setLatitude(-1.9706);
            kimisagara.setLongitude(30.0588);
            kimisagara.setOperatingHours("Mon-Fri: 7:00-17:00, Sat: 8:00-12:00");
            kimisagara.setServicesOffered("Primary Care, Family Planning, Maternal Health, Vaccination");
            kimisagara.setIsActive(true);
            healthFacilityRepository.save(kimisagara);

            // Gahanga Health Center
            HealthFacility gahanga = new HealthFacility();
            gahanga.setName("Gahanga Health Center");
            gahanga.setFacilityType(FacilityType.HEALTH_CENTER);
            gahanga.setAddress("Gahanga, Kicukiro, Kigali");
            gahanga.setPhoneNumber("+250788200000");
            gahanga.setEmail("gahanga@moh.gov.rw");
            gahanga.setLatitude(-1.9897);
            gahanga.setLongitude(30.1026);
            gahanga.setOperatingHours("Mon-Fri: 7:00-17:00, Sat: 8:00-12:00");
            gahanga.setServicesOffered("Primary Care, Family Planning, Maternal Health, Child Health");
            gahanga.setIsActive(true);
            healthFacilityRepository.save(gahanga);

            // Remera Health Center
            HealthFacility remera = new HealthFacility();
            remera.setName("Remera Health Center");
            remera.setFacilityType(FacilityType.HEALTH_CENTER);
            remera.setAddress("Remera, Gasabo, Kigali");
            remera.setPhoneNumber("+250788300000");
            remera.setEmail("remera@moh.gov.rw");
            remera.setLatitude(-1.9353);
            remera.setLongitude(30.1059);
            remera.setOperatingHours("Mon-Fri: 7:00-17:00, Sat: 8:00-12:00");
            remera.setServicesOffered("Primary Care, Family Planning, HIV/AIDS Care, Mental Health");
            remera.setIsActive(true);
            healthFacilityRepository.save(remera);
        }
    }

    private void initializeEducationLessons() {
        // Update existing lessons to be published if they aren't already
        List<EducationLesson> existingLessons = educationLessonRepository.findAll();
        for (EducationLesson lesson : existingLessons) {
            if (lesson.getIsPublished() == null || !lesson.getIsPublished()) {
                lesson.setIsPublished(true);
                educationLessonRepository.save(lesson);
            }
        }

        if (educationLessonRepository.count() == 0) {
            // Family Planning Lessons
            EducationLesson lesson1 = new EducationLesson();
            lesson1.setTitle("Ubwiyunge bw'umuryango");
            lesson1.setDescription("Amabwiriza y'ibanze ku bwiyunge bw'umuryango");
            lesson1.setContent("Ubwiyunge bw'umuryango ni ugutegura no guhitamo igihe cyiza cyo kubyara...");
            lesson1.setCategory(EducationCategory.FAMILY_PLANNING);
            lesson1.setLevel(EducationLevel.BEGINNER);
            lesson1.setDurationMinutes(15);
            lesson1.setLanguage("rw");
            lesson1.setAuthor("Dr. Uwimana");
            lesson1.setOrderIndex(1);
            lesson1.setIsPublished(true);
            educationLessonRepository.save(lesson1);

            // Contraception Lesson
            EducationLesson lesson2 = new EducationLesson();
            lesson2.setTitle("Uburyo bwo kurinda inda");
            lesson2.setDescription("Uburyo butandukanye bwo kurinda inda");
            lesson2.setContent("Hari uburyo butandukanye bwo kurinda inda...");
            lesson2.setCategory(EducationCategory.CONTRACEPTION);
            lesson2.setLevel(EducationLevel.BEGINNER);
            lesson2.setDurationMinutes(20);
            lesson2.setLanguage("rw");
            lesson2.setAuthor("Dr. Mukamana");
            lesson2.setOrderIndex(2);
            lesson2.setIsPublished(true);
            educationLessonRepository.save(lesson2);

            // Menstrual Health Lesson
            EducationLesson lesson3 = new EducationLesson();
            lesson3.setTitle("Ubuzima bw'imihango");
            lesson3.setDescription("Kubana n'imihango mu buryo bwiza");
            lesson3.setContent("Imihango ni ibintu bisanzwe ku bagore...");
            lesson3.setCategory(EducationCategory.MENSTRUAL_HEALTH);
            lesson3.setLevel(EducationLevel.BEGINNER);
            lesson3.setDurationMinutes(18);
            lesson3.setLanguage("rw");
            lesson3.setAuthor("Nurse Uwimana");
            lesson3.setOrderIndex(3);
            lesson3.setIsPublished(true);
            educationLessonRepository.save(lesson3);
        }
    }

    private void initializeSampleData() {
        // Initialize sample contraception methods
        if (contraceptionMethodRepository.count() == 0) {
            System.out.println("🔄 Initializing sample contraception methods...");

            // Get a sample user (client)
            User sampleUser = userRepository.findByRole(UserRole.CLIENT).stream().findFirst().orElse(null);

            if (sampleUser != null) {
                // Birth Control Pills
                ContraceptionMethod pills = new ContraceptionMethod();
                pills.setUser(sampleUser);
                pills.setType(ContraceptionType.PILL);
                pills.setName("Birth Control Pills");
                pills.setDescription("Daily oral contraceptive pills containing hormones to prevent pregnancy");
                pills.setStartDate(LocalDate.now().minusMonths(3));
                pills.setIsActive(true);
                pills.setEffectiveness(91.0);
                pills.setInstructions("Take daily at the same time");
                contraceptionMethodRepository.save(pills);

                // IUD
                ContraceptionMethod iud = new ContraceptionMethod();
                iud.setUser(sampleUser);
                iud.setType(ContraceptionType.IUD);
                iud.setName("Intrauterine Device (IUD)");
                iud.setDescription("Long-term reversible contraceptive device inserted into the uterus");
                iud.setStartDate(LocalDate.now().minusYears(1));
                iud.setEndDate(LocalDate.now().plusYears(4)); // 5-year IUD
                iud.setIsActive(false); // Previous method
                iud.setEffectiveness(99.0);
                iud.setInstructions("Copper IUD - hormone-free option");
                contraceptionMethodRepository.save(iud);

                // Condoms
                ContraceptionMethod condoms = new ContraceptionMethod();
                condoms.setUser(sampleUser);
                condoms.setType(ContraceptionType.CONDOM);
                condoms.setName("Male Condoms");
                condoms.setDescription("Barrier method that prevents sperm from reaching the egg");
                condoms.setStartDate(LocalDate.now().minusMonths(6));
                condoms.setIsActive(true);
                condoms.setEffectiveness(85.0);
                condoms.setInstructions("Also provides STI protection");
                contraceptionMethodRepository.save(condoms);

                System.out.println("✅ Sample contraception methods initialized successfully!");
            }
        }

        // Initialize sample menstrual cycles
        if (menstrualCycleRepository.count() == 0) {
            System.out.println("🔄 Initializing sample menstrual cycles...");

            // Get a sample user (client)
            User sampleUser = userRepository.findByRole(UserRole.CLIENT).stream().findFirst().orElse(null);

            if (sampleUser != null) {
                // Create sample menstrual cycles for the past 3 months
                LocalDate today = LocalDate.now();

                // Cycle 1 - 3 months ago
                MenstrualCycle cycle1 = new MenstrualCycle();
                cycle1.setUser(sampleUser);
                cycle1.setStartDate(today.minusDays(90));
                cycle1.setEndDate(today.minusDays(85));
                cycle1.setCycleLength(28);
                cycle1.setFlowDuration(5);
                cycle1.setFlowIntensity(FlowIntensity.NORMAL);
                cycle1.setNotes("Regular cycle, no issues");
                cycle1.setIsPredicted(false);
                menstrualCycleRepository.save(cycle1);

                // Cycle 2 - 2 months ago
                MenstrualCycle cycle2 = new MenstrualCycle();
                cycle2.setUser(sampleUser);
                cycle2.setStartDate(today.minusDays(62));
                cycle2.setEndDate(today.minusDays(57));
                cycle2.setCycleLength(28);
                cycle2.setFlowDuration(5);
                cycle2.setFlowIntensity(FlowIntensity.HEAVY);
                cycle2.setNotes("Heavier flow than usual");
                cycle2.setIsPredicted(false);
                menstrualCycleRepository.save(cycle2);

                // Cycle 3 - 1 month ago
                MenstrualCycle cycle3 = new MenstrualCycle();
                cycle3.setUser(sampleUser);
                cycle3.setStartDate(today.minusDays(34));
                cycle3.setEndDate(today.minusDays(29));
                cycle3.setCycleLength(28);
                cycle3.setFlowDuration(5);
                cycle3.setFlowIntensity(FlowIntensity.LIGHT);
                cycle3.setNotes("Light flow, some cramping");
                cycle3.setIsPredicted(false);
                menstrualCycleRepository.save(cycle3);

                // Cycle 4 - Current/Recent
                MenstrualCycle cycle4 = new MenstrualCycle();
                cycle4.setUser(sampleUser);
                cycle4.setStartDate(today.minusDays(6));
                cycle4.setEndDate(today.minusDays(1));
                cycle4.setCycleLength(28);
                cycle4.setFlowDuration(5);
                cycle4.setFlowIntensity(FlowIntensity.NORMAL);
                cycle4.setNotes("Normal cycle");
                cycle4.setIsPredicted(false);
                menstrualCycleRepository.save(cycle4);

                System.out.println("✅ Sample menstrual cycles initialized successfully!");
            }
        }
    }
}
