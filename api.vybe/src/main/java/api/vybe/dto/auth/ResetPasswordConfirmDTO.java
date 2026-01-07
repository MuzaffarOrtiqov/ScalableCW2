package api.vybe.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordConfirmDTO {

    @NotBlank(message = "Username is required")
    private String username;
    @NotBlank(message = "ConfirmCode is required")
    private String confirmCode;
    @NotBlank(message = "Password is required")
    private String password;
}
