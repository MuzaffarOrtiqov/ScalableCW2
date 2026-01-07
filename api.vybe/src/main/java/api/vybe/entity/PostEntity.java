package api.vybe.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "post")
public class PostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;  //title, content, photo{id}
    @Column(name = "title")
    private String title;
    @Column(name = "content", columnDefinition = "text")
    private String content;

    @Column(name = "photo_id")
    private String photoId;
    @JoinColumn(name = "photo_id",insertable = false, updatable = false)
    @OneToOne(fetch = FetchType.LAZY)
    private AttachEntity photo;

    @Column(name = "profile_id")
    private String profileId;
    @JoinColumn(name = "profile_id",insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private ProfileEntity profile;

    @Column(name = "visible")
    private boolean visible =true;
    @Column(name = "created_date")
    private LocalDateTime createdDate=LocalDateTime.now();
}
