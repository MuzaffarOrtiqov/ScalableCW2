package api.vybe.mapper;

import java.time.LocalDateTime;

public interface PostDetailMapper {
    String getPostId();
    String getPostTitle();
    String getPostPhotoId();
    LocalDateTime getPhotoCreatedDate();
    String getProfileId();
    String getProfileName();
    String getProfileUsername();
}
