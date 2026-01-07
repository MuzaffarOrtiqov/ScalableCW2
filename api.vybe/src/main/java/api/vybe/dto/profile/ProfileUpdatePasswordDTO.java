package api.vybe.dto.profile;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUpdatePasswordDTO {
    @NotBlank(message = "CurrentPassword required")
    private String currentPassword;
    @NotBlank(message = "NewPassword required")
    private String newPassword;

}
