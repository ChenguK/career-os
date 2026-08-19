package com.chengukargbo.careeros.applications.lock;
import java.util.List;import org.springframework.data.jpa.repository.JpaRepository;
public interface ApplicationLockHistoryRepository extends JpaRepository<ApplicationLockHistory,Long>{List<ApplicationLockHistory> findByApplicationIdOrderByOccurredAtAscIdAsc(Long applicationId);}
