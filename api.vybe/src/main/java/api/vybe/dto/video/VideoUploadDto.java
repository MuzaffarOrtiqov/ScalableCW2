package api.vybe.dto.video;

import api.vybe.enums.VideoStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NonNull;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VideoUploadDto {
    private String title;
    private String caption;
    private String location;
    private String category;
    private String tags;
    private VideoStatus status=VideoStatus.PUBLISHED;
}
