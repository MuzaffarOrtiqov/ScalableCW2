package api.vybe.entity;

import api.vybe.enums.VideoStatus;
import jakarta.persistence.*;
import lombok.Data;


import java.time.LocalDateTime;

@Entity
@Table(name = "videos")
@Data
public class VideoEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String caption;

    private String location;

    @Column(nullable = false)
    private String videoUrl;

    private String thumbnailUrl;

    @Column(nullable = false)
    private String category;

    private String tags;

    @Column(nullable = false)
    private VideoStatus status;

    private Long views = 0L;
    private Long likes = 0L;

    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    private Long fileSize;

    private String originalFilename;
}
