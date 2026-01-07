package api.vybe.controller;


import api.vybe.dto.AppResponse;
import api.vybe.dto.auth.AuthDTO;
import api.vybe.dto.auth.RegistrationDTO;
import api.vybe.dto.auth.ResetPasswordConfirmDTO;
import api.vybe.dto.auth.ResetPasswordDTO;
import api.vybe.dto.profile.ProfileResponseDTO;
import api.vybe.enums.AppLanguage;
import api.vybe.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/")
@Tag(name = "AuthController", description = "A set of APIs to work with Authentication and Authorization")
@Slf4j
public class AuthController {
    private AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registration")
    @Operation(summary = "Registration",description ="Method used to register a new user" )
    public ResponseEntity<AppResponse<String>> registration(@Valid @RequestBody RegistrationDTO dto,
                                                            @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage lang) {
        log.info("Registration: name: {}, username: {}",dto.getName(),dto.getUsername());
        return ResponseEntity.ok().body(authService.registration(dto, lang));
    }

    @GetMapping("/registration/email-verification/{token}")
    @Operation(summary = "Registration Verification email",description ="Method used to verify the user getting registered through email" )
    public ResponseEntity<ProfileResponseDTO> regVerificationEmail(@PathVariable String token,
                                                                   @RequestParam(name = "lang", defaultValue = "UZ") AppLanguage lang) {
        log.info("Registration Verification email: token: {}", token);
        return ResponseEntity.ok().body(authService.regVerificationEmail(token, lang));
    }



    @PostMapping("/login")
    @Operation(summary = "Login",description ="Method used to log in to the website" )
    public ResponseEntity<ProfileResponseDTO> login(@Valid @RequestBody AuthDTO authDTO,
                                                    @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage lang) {
        log.info("Login: username: {}", authDTO.getUsername());
        return ResponseEntity.ok(authService.login(authDTO, lang));
    }

    @PostMapping("/password-reset")
    @Operation(summary = "Reset Password",description ="Method used to reset the forgotten password" )
    public ResponseEntity<AppResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordDTO dto,
                                                             @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage lang) {
        log.info("Reset Password: username: {}", dto.getUsername());
        return ResponseEntity.ok(authService.resetPassword(dto, lang));
    }

    @PostMapping("/password-reset-confirm")
    @Operation(summary = "Password Reset Confirm",description ="Method used to confirm whether the new username belongs to him/her" )
    public ResponseEntity<AppResponse<String>> resetPasswordConfirm(@Valid @RequestBody ResetPasswordConfirmDTO dto,
                                                                        @RequestHeader(name = "Accept-Language", defaultValue = "UZ") AppLanguage lang) {
        log.info("Password Reset Confirm: username: {}", dto.getUsername());
        return ResponseEntity.ok(authService.resetPasswordConfirm(dto, lang));

    }
}
