package api.vybe.repository;


import api.vybe.entity.PostEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends CrudRepository<PostEntity, String>, PagingAndSortingRepository<PostEntity, String> {


    //List<PostEntity> findAllByProfileIdAndVisibleTrue(String profileId);
    Page<PostEntity> findAllByProfileIdAndVisibleTrueOrderByCreatedDateDesc(String profileId, Pageable pageable);

    Optional<PostEntity> findByIdAndVisibleTrue(String postId);

    @Transactional
    @Modifying
    @Query("UPDATE PostEntity AS p SET p.visible=false WHERE p.id=?1")
    void delete(String postId);

    @Query("SELECT p.profileId FROM PostEntity AS p WHERE p.id=?1")
    String getProfileId(String postId);

    @Query("from PostEntity where id !=?1 and visible =true order by createdDate desc limit 3")
    List<PostEntity> getPostsExcept(String id);
}
