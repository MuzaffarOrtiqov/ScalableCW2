package api.vybe.dto;


import api.vybe.enums.ProfileRole;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class JwtDTO {
    private String id;
    private String username;
    private List<ProfileRole> role;


    public JwtDTO(String id, String username, List<ProfileRole> role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }
}
