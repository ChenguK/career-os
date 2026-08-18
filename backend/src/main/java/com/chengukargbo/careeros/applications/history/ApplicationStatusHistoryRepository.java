package com.chengukargbo.careeros.applications.history;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationStatusHistoryRepository
    extends JpaRepository<ApplicationStatusHistory, Long> {
    List<ApplicationStatusHistory>
        findByApplicationIdOrderByOccurredAtAscIdAsc(Long applicationId);
}
