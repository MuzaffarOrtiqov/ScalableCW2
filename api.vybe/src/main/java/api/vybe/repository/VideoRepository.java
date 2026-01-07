package api.vybe.repository;

import api.vybe.entity.VideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface VideoRepository extends JpaRepository<VideoEntity,String> {

    List<VideoEntity> findByStatus(String status);
    List<VideoEntity> findAllByOrderByUploadedAtDesc();
}
