package api.vybe.repository;

import api.vybe.entity.EmailHistoryEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailHistoryRepository extends CrudRepository<EmailHistoryEntity, String> {
    Long countByEmailAndCreatedDateBetween(String email, LocalDateTime localDateTime, LocalDateTime now);

    Optional<EmailHistoryEntity> findTop1ByEmailOrderByCreatedDateDesc(String email);

    @Modifying
    @Transactional
    @Query("UPDATE EmailHistoryEntity SET attemptCount=COALESCE(attemptCount,0) +1")
    void updateAttemptCount(String id);
}
