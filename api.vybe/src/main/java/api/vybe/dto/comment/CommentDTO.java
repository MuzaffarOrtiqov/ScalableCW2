package api.vybe.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private String id;
    private String videoId;
    private String username;
    private String content;
    private LocalDateTime createdAt;
    private Integer likes;
}
