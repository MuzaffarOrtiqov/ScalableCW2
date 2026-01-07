package api.vybe.repository;


import api.vybe.entity.AttachEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface AttachRepository extends CrudRepository<AttachEntity, String> {

    @Transactional
    @Modifying
    @Query("UPDATE AttachEntity SET visible=false WHERE id=?1")
    void delete(String id);

}
