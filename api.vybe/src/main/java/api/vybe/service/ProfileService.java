package api.vybe.service;

import api.vybe.dto.AppResponse;
import api.vybe.dto.CodeConfirmDTO;
import api.vybe.dto.ProfilePhotoUpdateDTO;
import api.vybe.dto.profile.*;
import api.vybe.entity.ProfileEntity;
import api.vybe.entity.ProfileRoleEntity;
import api.vybe.enums.AppLanguage;
import api.vybe.enums.ProfileRole;
import api.vybe.exps.AppBadException;
import api.vybe.mapper.ProfileDetailMapper;
import api.vybe.repository.ProfileRepository;
import api.vybe.repository.ProfileRoleRepository;
import api.vybe.util.JwtUtil;
import api.vybe.util.SpringSecurityUtil;
import api.vybe.util.ValidityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Service
@Slf4j
public class ProfileService {
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private ResourceBundleMessageService resourceBundleMessageService;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private EmailSendingService emailSendingService;
    @Autowired
    private EmailHistoryService emailHistoryService;
    @Autowired
    private ProfileRoleRepository profileRoleRepository;
    @Autowired
    private AttachService attachService;

    public ProfileEntity findProfileById(String id, AppLanguage lang) {
        log.error("No profile found with id: {}", id);
        return profileRepository.findByIdAndVisibleTrue(id).orElseThrow(() -> new AppBadException(resourceBundleMessageService.getMessage("profile.not.found", lang)));

    }

    public AppResponse<String> updateDetail(ProfileDetailUpdateDTO profile, AppLanguage lang) {
        String userId = SpringSecurityUtil.getCurrentUserId();
        profileRepository.updateProfileName(profile.getName(), userId);
        return new AppResponse<>(resourceBundleMessageService.getMessage("profile.name.updated", lang));
    }

    public AppResponse<String> updatePassword(ProfileUpdatePasswordDTO profileDTO, AppLanguage lang) {
        String userId = SpringSecurityUtil.getCurrentUserId();
        ProfileEntity profileEntity = findProfileById(userId, lang);
        if (!bCryptPasswordEncoder.matches(profileDTO.getCurrentPassword(), profileEntity.getPassword())) {
            log.warn("Password mismatch: userId:{}", userId);
            throw new AppBadException(resourceBundleMessageService.getMessage("password.not.match", lang));
        }
        profileRepository.updatePassword(bCryptPasswordEncoder.encode(profileDTO.getNewPassword()), userId);
        return new AppResponse<>(resourceBundleMessageService.getMessage("password.update.success", lang));
    }

    public AppResponse<String> updateUsername(ProfileUpdateUsernameDTO profileDTO, AppLanguage lang) {
        //check
        Optional<ProfileEntity> optional = profileRepository.findByUsernameAndVisibleTrue(profileDTO.getUsername());
        if (optional.isPresent()) {
            log.info("Username already in use: {}", profileDTO.getUsername());
            throw new AppBadException(resourceBundleMessageService.getMessage("email.phone.exists", lang));
        }

        //send confirm code
        if (ValidityUtil.isValidEmail(profileDTO.getUsername())) {
            emailSendingService.sendUsernameChangeConfirmEmail(profileDTO.getUsername(), lang);
        }

        String userId = SpringSecurityUtil.getCurrentUserId();
        profileRepository.updateTempUsername(profileDTO.getUsername(), userId);

        return new AppResponse<>(resourceBundleMessageService.getMessage("confirm.code.sent", lang, profileDTO.getUsername()));
    }

    public AppResponse<String> updateUsernameConfirm(CodeConfirmDTO dto, AppLanguage lang) {
        String userId = SpringSecurityUtil.getCurrentUserId();
        ProfileEntity profileEntity = findProfileById(userId, lang);
        String tempUsername = profileEntity.getTempUsername();
        // check if email valid
        if (ValidityUtil.isValidEmail(tempUsername)) {
            emailHistoryService.check(tempUsername, dto.getCode(), lang);
        }
        //update username after checking
        profileRepository.updateUsername(userId, tempUsername);

        List<ProfileRole> roleList = profileRoleRepository.getAllRoles(profileEntity.getId());
        String jwt = JwtUtil.encode(userId, tempUsername, roleList);
        return new AppResponse<>(jwt, resourceBundleMessageService.getMessage("username.update.success", lang));
    }

    public AppResponse<String> updateProfilePhoto(ProfilePhotoUpdateDTO profileUpdateDTO, AppLanguage lang) {
        String userId = SpringSecurityUtil.getCurrentUserId();
        ProfileEntity profileEntity = findProfileById(userId, lang);
        String currentPhotoId = profileEntity.getPhotoId();
        if (profileUpdateDTO.getPhotoId() != null && profileUpdateDTO.getPhotoId() != profileEntity.getPhotoId()) { // check if profile photo is being renewed
            if (currentPhotoId != null) { // check if current photo exists before deleting
                attachService.delete(currentPhotoId);
            }
        }
        profileRepository.updateProfilePhoto(userId, profileUpdateDTO.getPhotoId());
        return new AppResponse<>(resourceBundleMessageService.getMessage("profile.photo.updated", lang));
    }

    public Page<ProfileDTO> filterProfile(ProfileFilterDTO profileFilterDTO, AppLanguage lang, int page, Integer size) {
        Page<ProfileDetailMapper> profileDetailMapperPage = null;
        Pageable pageable = PageRequest.of(page, size);
        if (profileFilterDTO.getQuery() == null) {
            profileDetailMapperPage = profileRepository.filterProfile(pageable);
        } else {
            profileDetailMapperPage = profileRepository.filterProfile("%" + profileFilterDTO.getQuery().toLowerCase() + "%", pageable);
        }
        List<ProfileDTO> profileDTOList = Objects.requireNonNull(profileDetailMapperPage).stream().map(this::toDTO).toList();
        return new PageImpl<>(profileDTOList, pageable, profileDetailMapperPage.getTotalElements());
    }

    public AppResponse<String> changeProfileStatus(String userId, ProfileStatusDTO profileStatusDTO, AppLanguage lang) {
        profileRepository.updateStatus(userId, profileStatusDTO.getStatus());
        return new AppResponse<>(resourceBundleMessageService.getMessage("profile.update.success", lang));
    }

    public AppResponse<String> deleteProfile(String userId, AppLanguage lang) {
        profileRoleRepository.deleteProfile(userId);
        return new AppResponse<>(resourceBundleMessageService.getMessage("profile.delete.success", lang));
    }

    // util methods
    private ProfileDTO toDTO(ProfileEntity profileEntity) {
        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setId(profileEntity.getId());
        profileDTO.setName(profileEntity.getName());
        profileDTO.setUsername(profileEntity.getUsername());
        if (profileEntity.getRoles() != null) {
            List<ProfileRole> profileRoleList = profileEntity.getRoles().stream().map(ProfileRoleEntity::getRoles).toList();
            profileDTO.setRole(profileRoleList);
        }
        profileDTO.setAttachDTO(attachService.attachDTO(profileEntity.getPhotoId()));
        profileDTO.setStatus(profileEntity.getStatus());
        profileDTO.setCreatedDate(profileEntity.getCreatedDate());
        return profileDTO;
    }

    private ProfileDTO toDTO(ProfileDetailMapper mapper) {
        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setId(mapper.getId());
        profileDTO.setName(mapper.getName());
        profileDTO.setPostCount(mapper.getPostCount());
        profileDTO.setUsername(mapper.getUsername());
        if (mapper.getProfileRole() != null) {
            String roles = mapper.getProfileRole();
            String[] rolesArray=  roles.split(",");
            List<ProfileRole> profileRoleList = new ArrayList<>();
            for (String role : rolesArray) {
                ProfileRole profileRole = ProfileRole.valueOf(role);
                profileRoleList.add(profileRole);
            }
            profileDTO.setRole(profileRoleList);
        }
        profileDTO.setAttachDTO(attachService.attachDTO(mapper.getPhotoId()));
        profileDTO.setStatus(mapper.getStatus());
        profileDTO.setCreatedDate(mapper.getCreatedDate());
        return profileDTO;
    }


}
