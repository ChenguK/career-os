package com.chengukargbo.careeros.preparation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.chengukargbo.careeros.applications.*;
import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.common.url.ApplicationUrlService;
import com.chengukargbo.careeros.preparation.ObservationDtos.*;
import com.chengukargbo.careeros.preparation.PreparationEnums.IdentitySource;
import com.chengukargbo.careeros.preparation.PreparationEnums.MaterialType;
import com.chengukargbo.careeros.questions.QuestionEnums.AnswerType;

class FormObservationServiceTest {
    private final ApplicationRepository applications = mock(ApplicationRepository.class);
    private final ApplicationFormTargetRepository targets = mock(ApplicationFormTargetRepository.class);
    private final FormObservationSnapshotRepository snapshots = mock(FormObservationSnapshotRepository.class);
    private final ObservedQuestionRepository questions = mock(ObservedQuestionRepository.class);
    private final ObservedMaterialRequirementRepository materialRequirements =
        mock(ObservedMaterialRequirementRepository.class);
    private FormObservationService service;
    private Application application;
    private ApplicationFormTarget target;
    private FormObservationSnapshot saved;

    @BeforeEach
    void setUp() {
        service = new FormObservationService(
            applications, targets, snapshots, questions, materialRequirements,
            new ApplicationUrlService()
        );
        application = mock(Application.class);
        when(application.getId()).thenReturn(12L);
        target = new ApplicationFormTarget(
            application, "https://jobs.example.com/apply/12"
        );
        when(applications.findById(12L)).thenReturn(Optional.of(application));
        when(targets.findByApplicationId(12L)).thenReturn(Optional.of(target));
        when(snapshots.findFirstByFormTargetApplicationIdOrderBySequenceNumberDesc(12L))
            .thenReturn(Optional.empty());
        when(snapshots.saveAndFlush(any())).thenAnswer(invocation -> {
            saved = invocation.getArgument(0);
            return saved;
        });
    }

    @Test
    void createsAFullSnapshotWithStructuredOptionsAndStableOrdering() {
        SnapshotResponse response = service.reconcile(12L, new SnapshotInput(
            new FormIdentityInput(
                "https://jobs.ashbyhq.com/acme/job-1#application",
                "job-1", "ashby:acme:job-1"
            ), List.of(
            question("q-b", "Second", 2, List.of(option("no", "No", 2))),
            question("q-a", "First", 1, List.of(
                option("yes", "Yes", 2), option("maybe", "Maybe", 1)
            ))), List.of(
                new MaterialRequirementInput("_systemfield_resume", MaterialType.RESUME,
                    "Resume", true, ".pdf,.doc,.docx", 0, "application"),
                new MaterialRequirementInput("portfolio_file", MaterialType.OTHER,
                    "Portfolio attachment", false, null, 1, "application")
            )
        ));

        assertEquals(1, response.sequenceNumber());
        assertEquals(2, response.activeQuestionCount());
        assertEquals(2, response.materialRequirementCount());
        assertEquals(List.of("_systemfield_resume", "portfolio_file"),
            saved.getMaterialRequirements().stream()
                .map(ObservedMaterialRequirement::getExternalFieldId).toList());
        assertTrue(saved.getMaterialRequirements().getFirst().isRequired());
        assertEquals(".pdf,.doc,.docx",
            saved.getMaterialRequirements().getFirst().getAcceptTypes());
        assertEquals(List.of("q-a", "q-b"), saved.getQuestions().stream()
            .map(ObservedQuestion::getExternalQuestionId).toList());
        assertTrue(saved.getQuestions().stream()
            .allMatch(question -> question.getPageKey().equals("application")));
        assertEquals(List.of("maybe", "yes"), saved.getQuestions().getFirst()
            .getOptions().stream().map(ObservedOption::getOptionValue).toList());
        assertEquals(64, response.fingerprint().length());
        assertEquals("job-1", target.getExternalRequisitionId());
        assertEquals(IdentitySource.USER, target.getIdentitySource());
        verify(targets).save(target);
    }

    @Test
    void reconciliationPreservesRemovedQuestionsAndOptionsAsInactive() {
        service.reconcile(12L, new SnapshotInput(null, List.of(
            question("q-a", "First", 1, List.of(option("yes", "Yes", 1))),
            question("q-b", "Second", 2, List.of(
                option("one", "One", 1), option("two", "Two", 2)
            ))
        )));
        FormObservationSnapshot previous = saved;
        when(snapshots.findFirstByFormTargetApplicationIdOrderBySequenceNumberDesc(12L))
            .thenReturn(Optional.of(previous));

        SnapshotResponse response = service.reconcile(12L, new SnapshotInput(null, List.of(
            question("q-b", "Second wording", 2,
                List.of(option("two", "Two", 2)))
        )));

        assertEquals(2, response.sequenceNumber());
        ObservedQuestion removed = saved.getQuestions().stream()
            .filter(question -> question.getExternalQuestionId().equals("q-a"))
            .findFirst().orElseThrow();
        ObservedQuestion retained = saved.getQuestions().stream()
            .filter(question -> question.getExternalQuestionId().equals("q-b"))
            .findFirst().orElseThrow();
        assertFalse(removed.isActive());
        assertTrue(removed.getOptions().stream().noneMatch(ObservedOption::isActive));
        assertTrue(retained.isActive());
        assertEquals("Second wording", retained.getQuestionText());
        assertFalse(retained.getOptions().stream()
            .filter(option -> option.getOptionValue().equals("one"))
            .findFirst().orElseThrow().isActive());
    }

    @Test
    void rejectsDuplicateQuestionAndOptionIdentity() {
        assertThrows(BusinessValidationException.class, () -> service.reconcile(
            12L, new SnapshotInput(null, List.of(
                question("q-a", "First", 1, List.of()),
                question("q-a", "Duplicate", 2, List.of())
            ))
        ));
        assertThrows(BusinessValidationException.class, () -> service.reconcile(
            12L, new SnapshotInput(null, List.of(question("q-a", "First", 1,
                List.of(option("same", "First", 1),
                    option("same", "Duplicate", 2)))))
        ));
        verify(snapshots, never()).saveAndFlush(any());
    }

    @Test
    void fingerprintIsDeterministicAcrossInputOrdering() {
        SnapshotInput first = new SnapshotInput(null, List.of(
            question("q-b", "Second", 2, List.of()),
            question("q-a", "First", 1, List.of(
                option("two", "Two", 2), option("one", "One", 1)
            ))
        ));
        String firstFingerprint = service.reconcile(12L, first).fingerprint();
        when(snapshots.findFirstByFormTargetApplicationIdOrderBySequenceNumberDesc(12L))
            .thenReturn(Optional.empty());
        SnapshotInput reordered = new SnapshotInput(null, List.of(
            question("q-a", "First", 1, List.of(
                option("one", "One", 1), option("two", "Two", 2)
            )),
            question("q-b", "Second", 2, List.of())
        ));
        assertEquals(firstFingerprint,
            service.reconcile(12L, reordered).fingerprint());
    }

    @Test
    void materialRequirementsAreImmutableSnapshotDataAndAffectFingerprint() {
        SnapshotInput withResume = new SnapshotInput(null, List.of(), List.of(
            new MaterialRequirementInput("_systemfield_resume", MaterialType.RESUME,
                "Resume", true, ".pdf", 0, "application")
        ));
        String firstFingerprint = service.reconcile(12L, withResume).fingerprint();
        FormObservationSnapshot first = saved;
        when(snapshots.findFirstByFormTargetApplicationIdOrderBySequenceNumberDesc(12L))
            .thenReturn(Optional.of(first));

        SnapshotResponse second = service.reconcile(12L,
            new SnapshotInput(null, List.of(), List.of()));

        assertNotEquals(firstFingerprint, second.fingerprint());
        assertEquals(1, first.getMaterialRequirements().size());
        assertTrue(saved.getMaterialRequirements().isEmpty());
    }

    @Test
    void readModelsUseDeterministicRepositoryOrdering() {
        FormObservationSnapshot snapshot = new FormObservationSnapshot(
            target, null, 1, "a".repeat(64)
        );
        when(snapshots.findByFormTargetApplicationIdOrderBySequenceNumberDesc(12L))
            .thenReturn(List.of(snapshot));
        when(snapshots.findFirstByFormTargetApplicationIdOrderBySequenceNumberDesc(12L))
            .thenReturn(Optional.of(snapshot));
        when(questions.findBySnapshotIdOrderByDisplayOrderAscExternalQuestionIdAsc(null))
            .thenReturn(List.of());

        assertEquals(1, service.snapshots(12L).size());
        assertTrue(service.latestQuestions(12L).isEmpty());
        verify(questions)
            .findBySnapshotIdOrderByDisplayOrderAscExternalQuestionIdAsc(null);
    }

    private QuestionInput question(String id, String text, int order,
        List<OptionInput> options) {
        return new QuestionInput(
            id, text, AnswerType.SINGLE_SELECT, true, order, options
        );
    }

    private OptionInput option(String value, String label, int order) {
        return new OptionInput("external-" + value, value, label, order);
    }
}
