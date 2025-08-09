package rw.health.ubuzima.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import rw.health.ubuzima.entity.EducationLesson;
import rw.health.ubuzima.enums.EducationCategory;
import rw.health.ubuzima.enums.EducationLevel;
import rw.health.ubuzima.repository.EducationLessonRepository;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EducationDataInitializer implements CommandLineRunner {

    private final EducationLessonRepository educationLessonRepository;

    @Override
    public void run(String... args) throws Exception {
        if (educationLessonRepository.count() == 0) {
            initializeEducationLessons();
        }
    }

    private void initializeEducationLessons() {
        List<EducationLesson> lessons = Arrays.asList(
            // Family Planning Lessons
            createLesson(
                "Kubana n'ubwiyunge - Ibanze",
                "Menya ibanze ku kubana n'ubwiyunge n'uburyo bwo gutegura umuryango wawe.",
                "Iri somo rigufasha gusobanukirwa ibanze ku kubana n'ubwiyunge. Uzamenya uburyo bwo gutegura umuryango wawe, guhitamo igihe cyo kubyara, n'ubwoko bw'ubufasha bushobora kuboneka. Ni ngombwa ko buri munyangire azi ibyo akeneye kumenya ku bijyanye n'ubuzima bwe bw'imyororokere.",
                EducationCategory.FAMILY_PLANNING,
                EducationLevel.BEGINNER,
                15,
                Arrays.asList("kubana", "umuryango", "ibanze", "gutegura"),
                1
            ),
            
            createLesson(
                "Uburyo bwo gukumira inda",
                "Iga uburyo butandukanye bwo gukumira inda n'uburyo bwo guhitamo ubukwiye.",
                "Iri somo riragaragaza uburyo butandukanye bwo gukumira inda. Uzamenya ibikoresho bitandukanye, uburyo bukora, n'ubwoba bushobora kubaho. Ni ngombwa ko wumva neza buryo buri buryo bukora mbere yo gufata icyemezo.",
                EducationCategory.CONTRACEPTION,
                EducationLevel.INTERMEDIATE,
                20,
                Arrays.asList("gukumira", "inda", "uburyo", "ibikoresho"),
                2
            ),
            
            createLesson(
                "Ubuzima bw'imyororokere bw'abagore",
                "Menya byinshi ku buzima bw'imyororokere bw'abagore n'uburyo bwo bwita.",
                "Iri somo riragaragaza ibintu byingenzi ku buzima bw'imyororokere bw'abagore. Uzamenya ku mihango y'ukwezi, ibimenyetso by'ubuzima bwiza, n'igihe cyo gusura muganga. Ni ngombwa ko buri mukobwa n'umugore azi uburyo bwo kwita ku buzima bwe.",
                EducationCategory.REPRODUCTIVE_HEALTH,
                EducationLevel.BEGINNER,
                18,
                Arrays.asList("ubuzima", "abagore", "imyororokere", "kwita"),
                3
            ),
            
            createLesson(
                "Inda n'ubwiteganyirize",
                "Menya uburyo bwo kwihangana inda n'ubwiteganyirize bukenewe.",
                "Iri somo riragaragaza ibintu byingenzi ku bijyanye n'inda. Uzamenya ibimenyetso by'inda, ubwiteganyirize bukenewe, n'ubufasha bushobora kuboneka. Ni ngombwa ko buri mugore azi uburyo bwo kwihangana inda neza.",
                EducationCategory.PREGNANCY,
                EducationLevel.INTERMEDIATE,
                25,
                Arrays.asList("inda", "ubwiteganyirize", "kwihangana", "ibimenyetso"),
                4
            ),
            
            createLesson(
                "Indwara zandurira mu mibonano mpuzabitsina",
                "Menya ku ndwara zandurira mu mibonano mpuzabitsina n'uburyo bwo zirinda.",
                "Iri somo riragaragaza indwara zandurira mu mibonano mpuzabitsina (STIs). Uzamenya ibimenyetso, uburyo bwo zirinda, n'ubuvuzi bushobora kuboneka. Ni ngombwa ko buri muntu azi uburyo bwo kwirinda izi ndwara.",
                EducationCategory.REPRODUCTIVE_HEALTH,
                EducationLevel.ADVANCED,
                30,
                Arrays.asList("indwara", "mibonano", "zirinda", "ubuvuzi"),
                5
            ),
            
            createLesson(
                "Ubuzima bw'abana bato",
                "Menya uburyo bwo kwita ku buzima bw'abana bato n'ubwiteganyirize bukenewe.",
                "Iri somo riragaragaza uburyo bwo kwita ku buzima bw'abana bato. Uzamenya ku bijyanye n'indyo, ubuzima, n'iterambere ry'abana. Ni ngombwa ko buri mubyeyi azi uburyo bwo kwita ku mwana we neza.",
                EducationCategory.FAMILY_PLANNING,
                EducationLevel.INTERMEDIATE,
                22,
                Arrays.asList("abana", "kwita", "ubuzima", "iterambere"),
                6
            ),
            
            createLesson(
                "Gukoresha ibikoresho byo gukumira inda neza",
                "Iga uburyo bwo gukoresha ibikoresho byo gukumira inda mu buryo bukwiye.",
                "Iri somo riragaragaza uburyo bwo gukoresha ibikoresho byo gukumira inda neza. Uzamenya uburyo bwo gukoresha preservative, ibipilisi, n'ibindi bikoresho. Ni ngombwa ko wumva neza uburyo bwo gukoresha buri gikoresho.",
                EducationCategory.CONTRACEPTION,
                EducationLevel.BEGINNER,
                16,
                Arrays.asList("ibikoresho", "gukoresha", "preservative", "ibipilisi"),
                7
            ),
            
            createLesson(
                "Ubuzima bw'abagabo mu myororokere",
                "Menya ku buzima bw'imyororokere bw'abagabo n'inshingano zabo.",
                "Iri somo riragaragaza ubuzima bw'imyororokere bw'abagabo. Uzamenya ku nshingano z'abagabo mu kubana n'ubwiyunge, kwita ku muryango, n'ubufasha bwo gutanga. Ni ngombwa ko buri mugabo azi inshingano ze.",
                EducationCategory.FAMILY_PLANNING,
                EducationLevel.INTERMEDIATE,
                20,
                Arrays.asList("abagabo", "inshingano", "ubufasha", "muryango"),
                8
            )
        );

        educationLessonRepository.saveAll(lessons);
    }

    private EducationLesson createLesson(String title, String description, String content,
                                       EducationCategory category, EducationLevel level,
                                       int duration, List<String> tags, int orderIndex) {
        EducationLesson lesson = new EducationLesson();
        lesson.setTitle(title);
        lesson.setDescription(description);
        lesson.setContent(content);
        lesson.setCategory(category);
        lesson.setLevel(level);
        lesson.setDurationMinutes(duration);
        lesson.setTags(tags);
        lesson.setOrderIndex(orderIndex);
        lesson.setIsPublished(true);
        lesson.setViewCount(0L);
        lesson.setLanguage("rw");
        lesson.setAuthor("Ubuzima Health Team");
        return lesson;
    }
}
