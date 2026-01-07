package api.vybe.controller;


import api.vybe.dto.AppResponse;
import api.vybe.dto.CodeConfirmDTO;
import api.vybe.dto.ProfilePhotoUpdateDTO;
import api.vybe.dto.profile.*;
import api.vybe.enums.AppLanguage;
import api.vybe.service.ProfileService;
import api.vybe.util.PageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile/")
@Tag(name = "ProfileController", description = "A set of APIs to work with profile")
@Slf4j
public class ProfileController {
    @Autowired
    private ProfileService profileService;

    @PutMapping("/detail")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Update profile detail", description = "Method used to update details of a profile")
    public ResponseEntity<AppResponse<String>> updateDetail(
            @Valid @RequestBody ProfileDetailUpdateDTO profile,
            @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage lang) {

        AppResponse<String> response = profileService.updateDetail(profile, lang);
        log.info("Update profile detail name: {}", profile.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/password")
    @Operation(summary = "Update profile password", description = "Method used to update password of a profile")
    public ResponseEntity<AppResponse<String>> updatePassword(
            @Valid @RequestBody ProfileUpdatePasswordDTO profile,
            @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage lang) {

        AppResponse<String> response = profileService.updatePassword(profile, lang);
        log.info("Password is being updated");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/username")
    @Operation(summary = "Update profile username", description = "Method used to update username of a profile")
    public ResponseEntity<AppResponse<String>> updateUsername(
            @Valid @RequestBody ProfileUpdateUsernameDTO profileDTO,
            @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage lang) {

        AppResponse<String> response = profileService.updateUsername(profileDTO, lang);
        log.info("Old username is being updated to username: {}", profileDTO.getUsername());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/username-confirmation")
    @Operation(summary = "Confirm profile's username", description = "Method used to confirm username of a profile")
    public ResponseEntity<AppResponse<String>> updateUsernameConfirm(
            @Valid @RequestBody CodeConfirmDTO codeConfirmDTO,
            @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage lang) {

        AppResponse<String> response = profileService.updateUsernameConfirm(codeConfirmDTO, lang);
        log.info("Username confirmation");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/photo")
    @Operation(summary = "Update profile photo", description = "Method used to update photo of a profile")
    public ResponseEntity<AppResponse<String>> updateProfilePhoto(@Valid @RequestBody ProfilePhotoUpdateDTO profileUpdateDTO,
                                                                  @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage lang) {
        AppResponse<String> response = profileService.updateProfilePhoto(profileUpdateDTO, lang);
        log.info("Profile photo updated");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/filter")
    @Operation(summary = "Filter profile", description = "Method used to filter profile list")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Page<ProfileDTO>> filterProfile(@RequestBody ProfileFilterDTO profileFilterDTO,
                                                          @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage lang,
                                                          @RequestParam(name = "page", defaultValue = "1") Integer page,
                                                          @RequestParam(name = "size", defaultValue = "5") Integer size) {
        Page<ProfileDTO> response = profileService.filterProfile(profileFilterDTO, lang, PageUtil.giveProperPageNumbering(page), size);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/status/{userId}")
    @Operation(summary = "Change profile status", description = "Method used to change status of profile")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<AppResponse<String>> changeProfileStatus(@PathVariable(name = "userId") String userId,
                                                                   @Valid @RequestBody ProfileStatusDTO profileStatusDTO,
                                                                   @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage lang) {
        AppResponse<String> response = profileService.changeProfileStatus(userId, profileStatusDTO, lang);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{userId}")
    @Operation(summary = "Delete profile", description = "Method used to delete profile")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<AppResponse<String>> deleteProfile(@PathVariable(name = "userId") String userId,
                                                             @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage lang) {
        AppResponse<String> response = profileService.deleteProfile(userId, lang);
        return ResponseEntity.ok(response);
    }

}
