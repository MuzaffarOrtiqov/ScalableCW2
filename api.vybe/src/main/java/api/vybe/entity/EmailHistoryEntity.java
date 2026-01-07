package api.vybe.entity;


import api.vybe.enums.EmailType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "email_history")
public class EmailHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(name = "email")
    private String email;
    @Column(name = "subject")
    private String subject;
    @Column(name = "body", columnDefinition = "text")
    private String body;
    @Column(name = "code")
    private String code;
    @Column(name = "created_date")
    @CreationTimestamp
    private LocalDateTime createdDate;
    @Column(name = "email_type")
    @Enumerated(EnumType.STRING)
    private EmailType emailType;
    @Column(name = "attempt_count")
    private Long attemptCount=0l;

}
