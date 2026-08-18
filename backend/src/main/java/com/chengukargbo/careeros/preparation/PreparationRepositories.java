package com.chengukargbo.careeros.preparation;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface ApplicationFormTargetRepository extends JpaRepository<ApplicationFormTarget, Long> {
    Optional<ApplicationFormTarget> findByApplicationId(Long applicationId);
}

interface ApplicationPreparationSessionRepository
    extends JpaRepository<ApplicationPreparationSession, Long> {
    Optional<ApplicationPreparationSession> findFirstByApplicationIdOrderByCreatedAtDescIdDesc(Long applicationId);
    List<ApplicationPreparationSession> findByApplicationIdOrderByCreatedAtDescIdDesc(Long applicationId);
    Optional<ApplicationPreparationSession> findByIdAndApplicationId(Long id, Long applicationId);
}

interface PreparationSessionEventRepository extends JpaRepository<PreparationSessionEvent, Long> {
    List<PreparationSessionEvent> findBySessionApplicationIdOrderByOccurredAtAscIdAsc(Long applicationId);
}

interface FormObservationSnapshotRepository
    extends JpaRepository<FormObservationSnapshot, Long> {
    Optional<FormObservationSnapshot> findFirstByFormTargetApplicationIdOrderBySequenceNumberDesc(Long applicationId);
    List<FormObservationSnapshot> findByFormTargetApplicationIdOrderBySequenceNumberDesc(Long applicationId);
    Optional<FormObservationSnapshot> findByIdAndFormTargetApplicationId(Long id, Long applicationId);
}

interface ObservedQuestionRepository extends JpaRepository<ObservedQuestion, Long> {
    List<ObservedQuestion> findBySnapshotIdOrderByDisplayOrderAscExternalQuestionIdAsc(Long snapshotId);
}

interface ApprovedFieldPlanRepository extends JpaRepository<ApprovedFieldPlan, Long> {
    Optional<ApprovedFieldPlan> findBySessionId(Long sessionId);
}

interface ApprovedFieldPlanItemRepository extends JpaRepository<ApprovedFieldPlanItem, Long> {
    List<ApprovedFieldPlanItem> findByPlanIdOrderByDisplayOrderAscIdAsc(Long planId);
}

interface FieldPreparationResultRepository extends JpaRepository<FieldPreparationResult, Long> {
    List<FieldPreparationResult> findByPlanItemPlanIdOrderByPlanItemDisplayOrderAscIdAsc(Long planId);
    boolean existsByPlanItemId(Long planItemId);
}

interface PreparationReviewRepository extends JpaRepository<PreparationReview, Long> {
    Optional<PreparationReview> findBySessionId(Long sessionId);
}
