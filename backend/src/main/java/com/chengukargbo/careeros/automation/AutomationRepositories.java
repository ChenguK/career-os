package com.chengukargbo.careeros.automation;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
interface ApplicationAutomationRepository extends JpaRepository<ApplicationAutomation,Long>{Optional<ApplicationAutomation> findByApplicationId(Long id);}
interface ApplicationAutomationHistoryRepository extends JpaRepository<ApplicationAutomationHistory,Long>{List<ApplicationAutomationHistory> findByApplicationIdOrderByOccurredAtAscIdAsc(Long id);}
