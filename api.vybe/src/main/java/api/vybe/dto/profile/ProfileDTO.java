package api.vybe.dto.profile;


import api.vybe.enums.GeneralStatus;
import api.vybe.enums.ProfileRole;
import api.vybe.dto.attach.AttachDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileDTO {
    private String id;
    private String name;
    private String surname;
    private String username;
    private String password;
    private GeneralStatus status;
    private List<ProfileRole> role;
    private Boolean visible;
    private LocalDateTime createdDate;
    private AttachDTO attachDTO;
    private Long postCount;
    private String roles;

}
