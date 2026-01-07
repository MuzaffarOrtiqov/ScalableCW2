package api.vybe.dto.post;

import api.vybe.dto.attach.AttachDTO;
import api.vybe.dto.profile.ProfileDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostDTO {
    private String id;
    private String title;
    private String content;
    private AttachDTO attachDTO;
    private LocalDateTime createdDate;
    private ProfileDTO profile;



}
