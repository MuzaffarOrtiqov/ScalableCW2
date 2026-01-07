package api.vybe.mapper;

import api.vybe.enums.GeneralStatus;

import java.time.LocalDateTime;

public interface ProfileDetailMapper {
    String getId();
    String getName();
    String getUsername();
    String getPhotoId();
    GeneralStatus getStatus();
    LocalDateTime getCreatedDate();
    Long getPostCount();
    String getProfileRole();
}
