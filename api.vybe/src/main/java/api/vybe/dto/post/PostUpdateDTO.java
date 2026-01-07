package api.vybe.dto.post;

import api.vybe.dto.attach.AttachCreateDTO;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class PostUpdateDTO {
    @Length(min = 5, max = 255, message = "Title should be between 5 and 255 characters")
    private String title;
    private String content;
    private AttachCreateDTO photo;
}
