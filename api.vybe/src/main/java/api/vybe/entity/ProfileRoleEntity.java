package api.vybe.entity;


import api.vybe.enums.ProfileRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Table(name = "profile_role")
@Entity
@Getter
@Setter
public class ProfileRoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "profile_id")
    private String profileId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id",insertable = false, updatable = false)
    private ProfileEntity profileEntity;

    @Column(name = "roles")
    @Enumerated(EnumType.STRING)
    private ProfileRole roles;

    @Column(name = "created_date")
    @CreationTimestamp
    private LocalDateTime createdDate;
}
