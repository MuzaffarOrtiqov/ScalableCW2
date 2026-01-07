package api.vybe.dto.post;


import api.vybe.dto.attach.AttachCreateDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;


@Getter
@Setter
public class PostCreateDTO {
    //title, content, photo{id}
    @NotBlank(message = "Title is required")
    @Length(min = 5, max = 255, message = "Title should be between 5 and 255 characters")
    private String title;
    @NotBlank(message = "Content is required")
    private String content;
    @NotNull(message = "Photo is required")
    private AttachCreateDTO photo;
}
