package api.vybe.repository;

import api.vybe.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, String> {
    List<CommentEntity> findByVideoIdOrderByCreatedAtDesc(String videoId);

    Long countByVideoId(String videoId);

    void deleteByVideoId(String videoId);
}
