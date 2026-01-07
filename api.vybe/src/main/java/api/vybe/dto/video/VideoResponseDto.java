package api.vybe.dto.video;

import api.vybe.enums.VideoStatus;
import lombok.Data;

@Data
public class VideoResponseDto {
    private String id;
    private String title;
    private String caption;
    private String location;
    private String videoUrl;
    private String originalFilename;
    private String thumbnailUrl;
    private String category;
    private String tags;
    private VideoStatus status;
    private Long views;
    private Long likes;
    private String uploadedAt;
    private Long fileSize;
}
