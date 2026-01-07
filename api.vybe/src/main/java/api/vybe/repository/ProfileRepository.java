package api.vybe.repository;

import api.vybe.entity.ProfileEntity;
import api.vybe.enums.GeneralStatus;
import api.vybe.mapper.ProfileDetailMapper;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<ProfileEntity, String> {

    Optional<ProfileEntity> findByIdAndVisibleTrue(String id);

    @Transactional
    @Modifying
    @Query("UPDATE ProfileEntity AS p SET p.status=?2 WHERE p.id=?1")
    void updateStatus(String profileId, GeneralStatus generalStatus);

    @Query("FROM ProfileEntity AS p WHERE p.username=?1 AND p.visible=TRUE")
    Optional<ProfileEntity> findByUsernameAndVisibleTrue(String username);


    @Modifying
    @Transactional
    @Query("UPDATE ProfileEntity p SET p.password=?1 WHERE p.id=?2")
    void updatePassword(String password, String id);

    @Modifying
    @Transactional
    @Query("UPDATE ProfileEntity SET name=?1 WHERE id=?2")
    void updateProfileName(String name, String id);

    @Modifying
    @Transactional
    @Query("UPDATE ProfileEntity SET tempUsername=?1 WHERE id=?2")
    void updateTempUsername(String username, String userId);

    @Modifying
    @Transactional
    @Query("UPDATE ProfileEntity SET username=?2 WHERE id=?1")
    void updateUsername(String userId, String tempUsername);


    @Modifying
    @Transactional
    @Query("UPDATE ProfileEntity SET photoId=?2 WHERE id=?1")
    void updateProfilePhoto(String userId, String photoId);

    @Query("SELECT p.id as id, p.name as name, p.username as username, p.photoId as photoId, p.status as status, p.createdDate as createdDate,  " +
            "(select count (post) FROM PostEntity AS post WHERE p.id=post.profileId ) as postCount, " +
            "(select string_agg(pr.roles,',') FROM ProfileRoleEntity pr WHERE p.id= pr.profileId) as profileRole   " +
            "FROM ProfileEntity p  " +
            "WHERE p.visible = true ORDER BY p.createdDate DESC")
    Page<ProfileDetailMapper> filterProfile(Pageable pageable);

    /*  @Query("SELECT p FROM ProfileEntity p INNER JOIN FETCH p.roles  WHERE p.visible = true ORDER BY p.createdDate DESC")
    Page<ProfileEntity> filterProfile(Pageable pageable);*/

    @Query("SELECT p.id as id, p.name as name, p.username as username, p.photoId as photoId, p.status as status, p.createdDate as createdDate,  " +
            "(select count (post) FROM PostEntity AS post WHERE p.id=post.profileId ) as postCount, " +
            "(select string_agg(pr.roles,',') FROM ProfileRoleEntity pr WHERE p.id= pr.profileId) as profileRole   " +
            "FROM ProfileEntity p  " +
            "WHERE (p.id = ?1 OR lower(p.username) LIKE ?1 OR lower(p.name) LIKE ?1) AND p.visible = true ORDER BY p.createdDate DESC")
    Page<ProfileDetailMapper> filterProfile(String search, Pageable pageable);


}
