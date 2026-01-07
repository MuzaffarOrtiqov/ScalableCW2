package api.vybe.dto.profile;

import api.vybe.enums.GeneralStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileStatusDTO {
    @NotNull(message = "Status is required")
    private GeneralStatus status;
}
