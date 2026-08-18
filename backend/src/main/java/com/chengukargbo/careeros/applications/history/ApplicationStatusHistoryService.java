package com.chengukargbo.careeros.applications.history;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.chengukargbo.careeros.applications.*;

@Service
@Transactional
public class ApplicationStatusHistoryService {
    private final ApplicationStatusHistoryRepository historyRepository;
    private final ApplicationRepository applicationRepository;

    public ApplicationStatusHistoryService(ApplicationStatusHistoryRepository historyRepository,
        ApplicationRepository applicationRepository) {
        this.historyRepository = historyRepository;
        this.applicationRepository = applicationRepository;
    }

    public void recordInitial(Application application, ApplicationTransitionSource source) {
        historyRepository.save(new ApplicationStatusHistory(application, null,
            application.getStatus(), source, null));
    }

    public void recordTransition(Application application, ApplicationStatus previous,
        ApplicationTransitionSource source) {
        if (previous != application.getStatus()) {
            historyRepository.save(new ApplicationStatusHistory(application, previous,
                application.getStatus(), source, null));
        }
    }

    @Transactional(readOnly = true)
    public List<ApplicationStatusHistoryResponse> findForApplication(Long applicationId) {
        if (!applicationRepository.existsById(applicationId)) {
            throw new ApplicationNotFoundException(applicationId);
        }
        return historyRepository.findByApplicationIdOrderByOccurredAtAscIdAsc(applicationId)
            .stream().map(ApplicationStatusHistoryResponse::from).toList();
    }
}
