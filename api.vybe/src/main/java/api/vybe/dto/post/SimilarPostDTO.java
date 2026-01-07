package api.vybe.dto.post;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimilarPostDTO {
    @NotBlank(message = "exceptId is required")
    private String exceptId;
}
