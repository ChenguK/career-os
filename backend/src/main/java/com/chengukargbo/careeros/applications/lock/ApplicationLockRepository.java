package com.chengukargbo.careeros.applications.lock;
import java.util.Optional;import org.springframework.data.jpa.repository.JpaRepository;
public interface ApplicationLockRepository extends JpaRepository<ApplicationLock,Long>{Optional<ApplicationLock> findByApplicationId(Long applicationId);}
