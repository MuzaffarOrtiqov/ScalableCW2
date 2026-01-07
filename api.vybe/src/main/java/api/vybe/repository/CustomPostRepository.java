package api.vybe.repository;

import api.vybe.dto.post.FilterResultDTO;
import api.vybe.dto.post.PostAdminFilterDTO;
import api.vybe.dto.post.PostFilterDTO;
import api.vybe.entity.PostEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CustomPostRepository {
    @Autowired
    private EntityManager entityManager;

    public FilterResultDTO<PostEntity> filter(PostFilterDTO filterDTO, int page, int size) {
        StringBuilder builder = new StringBuilder(" WHERE p.visible=true");

        HashMap<String, Object> params = new HashMap<>();
        if (filterDTO.getQuery() != null) {
            builder.append(" AND lower(p.title) like :query");
            params.put("query", "%" + filterDTO.getQuery().toLowerCase() + "%");
        }
        if (filterDTO.getExceptId() != null) {
            builder.append(" AND id !=:exceptId");
            params.put("exceptId", filterDTO.getExceptId());
        }
        StringBuilder selectBuilder = new StringBuilder("SELECT p FROM PostEntity p")
                .append(builder).append(" order by p.createdDate desc");
        ;
        StringBuilder countBuilder = new StringBuilder("SELECT COUNT(p.id) FROM PostEntity p")
                .append(builder);
        // select query
        Query selectQuery = entityManager.createQuery(selectBuilder.toString());
        selectQuery.setFirstResult(page * size);
        selectQuery.setMaxResults(size);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            selectQuery.setParameter(key, value);
        }
        List<PostEntity> postEntityList = selectQuery.getResultList();

        // count query
        Query countQuery = entityManager.createQuery(countBuilder.toString());
        params.forEach(countQuery::setParameter);
        Long totalCount = (Long) countQuery.getSingleResult();

        return new FilterResultDTO<>(postEntityList, totalCount);
    }

    public FilterResultDTO<Object[]> filter(PostAdminFilterDTO filterDTO, int page, int size) {
        StringBuilder builder = new StringBuilder(" WHERE p.visible=true");

        HashMap<String, Object> params = new HashMap<>();
        if (filterDTO.getProfileQuery() != null && !filterDTO.getProfileQuery().isBlank()) {
            builder.append(" AND (lower(pr.name) like :profileQuery or lower(pr.username) like :profileQuery)");
            params.put("profileQuery", "%" + filterDTO.getProfileQuery().toLowerCase() + "%");
        }
        if (filterDTO.getPostQuery() != null && !filterDTO.getPostQuery().isBlank()) {
            builder.append(" AND (lower(p.title) like :postQuery or p.id =:postId)");
            params.put("postQuery", "%" + filterDTO.getPostQuery().toLowerCase() + "%");
            params.put("postId", filterDTO.getPostQuery());
        }
        StringBuilder selectBuilder = new StringBuilder("SELECT p.id as postId, p.title as postTitle, p.photoId as postPhotoId, p.createdDate as postCreatedDate, " +
                " pr.id as profileId, pr.name as profileName, pr.username as profileUsername" +
                " FROM PostEntity p  inner join ProfileEntity pr on pr.id = p.profileId")
                .append(builder).append(" order by p.createdDate desc");
        StringBuilder countBuilder = new StringBuilder("SELECT COUNT(p) FROM PostEntity p inner join ProfileEntity pr on pr.id = p.profileId")
                .append(builder);
        // select query
        Query selectQuery = entityManager.createQuery(selectBuilder.toString());
        selectQuery.setFirstResult(page * size);
        selectQuery.setMaxResults(size);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            selectQuery.setParameter(key, value);
        }
        List<Object[]> postDetailList = selectQuery.getResultList();

        // count query
        Query countQuery = entityManager.createQuery(countBuilder.toString());
        params.forEach(countQuery::setParameter);
        Long totalCount = (Long) countQuery.getSingleResult();

        return new FilterResultDTO<>(postDetailList, totalCount);
    }
}
