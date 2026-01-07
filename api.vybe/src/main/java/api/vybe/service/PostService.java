package api.vybe.service;

import api.vybe.dto.post.*;
import api.vybe.dto.profile.ProfileDTO;
import api.vybe.entity.PostEntity;
import api.vybe.enums.AppLanguage;
import api.vybe.enums.ProfileRole;
import api.vybe.exps.AppBadException;
import api.vybe.repository.CustomPostRepository;
import api.vybe.repository.PostRepository;
import api.vybe.util.SpringSecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PostService {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private ResourceBundleMessageService resourceBundleMessageService;
    @Autowired
    private AttachService attachService;
    @Autowired
    private CustomPostRepository customRepository;

    public PostDTO create(PostCreateDTO dto, AppLanguage lang) {
        PostEntity post = new PostEntity();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setProfileId(SpringSecurityUtil.getCurrentUserId());
        post.setPhotoId(dto.getPhoto().getPhotoId());
        postRepository.save(post);
        return toDTO(post);
    }


    public Page<PostDTO> getProfilePost(Integer page, Integer size, AppLanguage lang) {

        Pageable pageable = PageRequest.of(page, size);
        String userId = SpringSecurityUtil.getCurrentUserId();
        Page<PostEntity> postEntityList = postRepository.findAllByProfileIdAndVisibleTrueOrderByCreatedDateDesc(userId, pageable);
        List<PostDTO> postDTOList = postEntityList
                .stream()
                .map(postEntity -> toDTO(postEntity))
                .collect(Collectors.toList());
        long totalCount = postEntityList.getTotalElements();

        return new PageImpl<>(postDTOList, pageable, totalCount);
    }

    public PostDTO getFullPostDetails(String postId, AppLanguage lang) {
        PostEntity post = getPostById(postId, lang);
        PostDTO postDTO = toDTO(post);
        postDTO.setContent(post.getContent());
        return postDTO;
    }

    public PostDTO updatePost(String postId, PostUpdateDTO postUpdateDTO, AppLanguage lang) {
        PostEntity post = getPostById(postId, lang);
        String deletedPhotoId = null;
        String userId = SpringSecurityUtil.getCurrentUserId();

        //check for admin
        if (!SpringSecurityUtil.hasRole(ProfileRole.ROLE_ADMIN) && !post.getProfileId().equals(userId)) {
            log.warn("User {} trying to update other's post", userId);
            throw new AppBadException(resourceBundleMessageService.getMessage("no.post.update", lang));
        }

        post.setTitle(postUpdateDTO.getTitle());
        post.setContent(postUpdateDTO.getContent());
        if (!post.getPhotoId().equals(postUpdateDTO.getPhoto().getPhotoId())) {
            deletedPhotoId = post.getPhotoId();
        }
        post.setPhotoId(postUpdateDTO.getPhoto().getPhotoId());
        postRepository.save(post);
        if (deletedPhotoId != null) {
            attachService.delete(deletedPhotoId);
        }
        return toDTO(post);
    }

    public Boolean deletePostById(String postId, AppLanguage lang) {
        //receiving profileID of a post
        String profileEntityId = postRepository.getProfileId(postId);

        String userId = SpringSecurityUtil.getCurrentUserId();
        if (!SpringSecurityUtil.hasRole(ProfileRole.ROLE_ADMIN) && !profileEntityId.equals(userId)) {
            log.warn("User {} trying to update other's post", userId);
            throw new AppBadException(resourceBundleMessageService.getMessage("no.post.delete", lang));
        }
        postRepository.delete(postId);
        return true;
    }

    public PageImpl<PostDTO> filter(PostFilterDTO filterDTO, int page, int size) {
        FilterResultDTO<PostEntity> result = customRepository.filter(filterDTO, page, size);
        List<PostDTO> postList = result.getList()
                .stream()
                .map(postEntity -> toDTO(postEntity)).toList();
        return new PageImpl<>(postList, PageRequest.of(page, size), result.getCount());
    }

    public List<PostDTO> getSimilarPosts(SimilarPostDTO similarPostDTO) {
        List<PostEntity> similarPostList = postRepository.getPostsExcept(similarPostDTO.getExceptId());
        return similarPostList
                .stream()
                .map(postEntity -> toDTO(postEntity)).toList();
    }


    public PageImpl<PostDTO> adminFilter(PostAdminFilterDTO postAdminFilterDTO, int page, int size) {
        FilterResultDTO<Object[]> result = customRepository.filter(postAdminFilterDTO, page, size);
        List<PostDTO> postList = result.getList()
                .stream()
                .map(this::toDTO).toList();
        return new PageImpl<>(postList, PageRequest.of(page, size), result.getCount());
    }


    //util methods
    private PostDTO toDTO(PostEntity postEntity) {
        //id, title, content, photo{id,url}, createdDate}
        PostDTO dto = new PostDTO();
        dto.setId(postEntity.getId());
        dto.setTitle(postEntity.getTitle());
        dto.setAttachDTO(attachService.attachDTO(postEntity.getPhotoId()));
        dto.setCreatedDate(postEntity.getCreatedDate());
        return dto;

    }

    private PostDTO toDTO(Object[] obj) {
        PostDTO dto = new PostDTO();
        dto.setId((String) obj[0]);
        dto.setTitle(String.valueOf(obj[1]));
        if (obj[2] != null) {
            dto.setAttachDTO(attachService.attachDTO(String.valueOf(obj[2])));
        }
        dto.setCreatedDate((LocalDateTime) obj[3]);
        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setId(String.valueOf(obj[4]));
        profileDTO.setName(String.valueOf(obj[5]));
        profileDTO.setUsername(String.valueOf(obj[6]));

        dto.setProfile(profileDTO);
        return dto;
    }

    public PostEntity getPostById(String postId, AppLanguage lang) {
        return postRepository.findByIdAndVisibleTrue(postId)
                .orElseThrow(() -> new AppBadException(resourceBundleMessageService.getMessage("post.not.found", lang)));
    }


}
