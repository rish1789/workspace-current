package com.example.myapp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.myapp.dto.request.CreateAppUserRequest;
import com.example.myapp.dto.request.UpdateEmailRequest;
import com.example.myapp.dto.request.UpdateUserPassword;
import com.example.myapp.dto.request.UpdateUsernameRequest;
import com.example.myapp.dto.respond.AppUserPublicResponse;
import com.example.myapp.dto.respond.AppUserResponse;
import com.example.myapp.service.AppUserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/users")
@Validated
public class AppUserController {

    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    // ─── READ ─────────────────────────────────────────────────────────────────

    @GetMapping("/find")
    public ResponseEntity<List<AppUserPublicResponse>> getAllUsers(@RequestParam(required = false) String username) {
        return ResponseEntity.ok(appUserService.getAllAppUsers(username));
    }

    @GetMapping("/find/{id}")
    public ResponseEntity<AppUserPublicResponse> getAppUserById(@Positive(message = "Invalid Id") @PathVariable Integer id) {
        return ResponseEntity.ok(appUserService.getAppUserById(id));
    }
// not required now
/* 
    @GetMapping("/email/{email}")
    public ResponseEntity<AppUserResponse> getAppUserByEmail(@NotBlank(message = "Email cannot be empty") @PathVariable String email) {
        return ResponseEntity.ok(appUserService.getAppUserByEmail(email));
    }
*/   
    @GetMapping("/me")
    public ResponseEntity<AppUserResponse> getMyProfile() {
       return ResponseEntity.ok(appUserService.getMyProfile());
    }
    // ─── CREATE ──────────────────────────────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<AppUserResponse> createAppUser(@Valid @RequestBody CreateAppUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appUserService.createAppUser(request));
    }

    // ─── UPDATE ──────────────────────────────────────────────────────────────

    @PatchMapping("/{id}/username")
    public ResponseEntity<AppUserResponse> updateUsername(@Positive(message = "Invalid Id") @PathVariable Integer id,
                                                         @Valid @RequestBody UpdateUsernameRequest request) {
        return ResponseEntity.ok(appUserService.updateAppUserName(id, request));
    }

    @PatchMapping("/email/{email}/update-username")
    public ResponseEntity<AppUserResponse> updateUsername(@NotBlank(message="Email cannot be empty") @PathVariable String email,
                                                         @Valid @RequestBody UpdateUsernameRequest request) {
        return ResponseEntity.ok(appUserService.updateAppUserName(email, request));
    }

    @PatchMapping("/{id}/email")
    public ResponseEntity<AppUserResponse> updateEmail(@Positive(message = "Invalid Id") @PathVariable Integer id,
                                                      @Valid @RequestBody UpdateEmailRequest request) {
        return ResponseEntity.ok(appUserService.updateEmail(id, request));
    }

    @PatchMapping("/email/{email}/update")
    public ResponseEntity<AppUserResponse> updateEmail(@NotBlank(message = "Invalid Id") @PathVariable String email,
                                                      @Valid @RequestBody UpdateEmailRequest request) {
        return ResponseEntity.ok(appUserService.updateEmail(email, request));
    }

    @PatchMapping("/{id}/update-password")
    public ResponseEntity<Boolean> updatePassword(@Positive(message = "Invalid Id") @PathVariable Integer id,
                                                  @Valid @RequestBody UpdateUserPassword request) {
        return ResponseEntity.ok(appUserService.updatePassword(id, request));
    }

    @PatchMapping("/email/{email}/update-password")
    public ResponseEntity<Boolean> updatePassword(@NotBlank(message = "Email cannot be empty") @PathVariable String email,
                                                  @Valid @RequestBody UpdateUserPassword request) {
        return ResponseEntity.ok(appUserService.updatePassword(email, request));
    }

    // ─── DELETE ──────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteAppUser(@Positive(message = "Invalid Id") @PathVariable Integer id) {
        return ResponseEntity.ok(appUserService.deleteAppUser(id));
    }

    @DeleteMapping("/email/{email}")
    public ResponseEntity<Boolean> deleteAppUser(@NotBlank(message = "Email cannot be empty") @PathVariable String email) {
        return ResponseEntity.ok(appUserService.deleteAppUser(email));
    }
}