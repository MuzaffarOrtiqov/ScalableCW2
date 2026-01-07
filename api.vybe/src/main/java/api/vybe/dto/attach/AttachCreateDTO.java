package api.vybe.dto.attach;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttachCreateDTO {
    @NotBlank(message = "photoId is required")
    private String photoId;
}
