package api.vybe.service;

import api.vybe.entity.ProfileRoleEntity;
import api.vybe.enums.ProfileRole;
import api.vybe.repository.ProfileRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileRoleService {
    @Autowired
    private ProfileRoleRepository profileRoleRepository;

    public void create (String id, ProfileRole role) {
        ProfileRoleEntity profileRoleEntity = new ProfileRoleEntity();
        profileRoleEntity.setProfileId(id);
        profileRoleEntity.setRoles(role);
        profileRoleRepository.save(profileRoleEntity);
    }

    public void deleteRoles(String profileId) {
        profileRoleRepository.deleteRole(profileId);
    }


}
