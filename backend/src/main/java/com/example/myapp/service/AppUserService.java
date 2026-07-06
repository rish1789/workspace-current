package com.example.myapp.service;

import com.example.myapp.ErrorException.DuplicateResponseException;
import com.example.myapp.ErrorException.ResourceNotFoundException;
import com.example.myapp.HelperFiles.AccessControl;
import com.example.myapp.HelperFiles.Helper;
import com.example.myapp.dto.request.CreateAppUserRequest;
import com.example.myapp.dto.request.UpdateEmailRequest;
import com.example.myapp.dto.request.UpdateUserPassword;
import com.example.myapp.dto.request.UpdateUsernameRequest;
import com.example.myapp.dto.respond.AppUserPublicResponse;
import com.example.myapp.dto.respond.AppUserResponse;
import com.example.myapp.entity.AppUser;
import com.example.myapp.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder   passwordEncoder;  // ← add this
    private final AccessControl     accessControl;
    private final Helper            helper;
    public AppUserService(AppUserRepository appUserRepository,
                          PasswordEncoder passwordEncoder,
                          AccessControl accessControl,
                          Helper helper) {  // ← add this
        this.appUserRepository = appUserRepository;
        this.passwordEncoder   = passwordEncoder;
        this.accessControl     = accessControl;
        this.helper            = helper;
    }

    // ─── PRIVATE HELPER ──────────────────────────────────────────────────────

    private AppUserResponse toRespond(AppUser user) {
        return new AppUserResponse(user.getId(), user.getUsername(), user.getEmail());
    }

    private AppUserPublicResponse toPublicRespond(AppUser user){
        return new AppUserPublicResponse(user.getId(),user.getUsername());
    }

    // ─── CREATE ──────────────────────────────────────────────────────────────

    public AppUserResponse createAppUser(CreateAppUserRequest request) {
        if (appUserRepository.existsByEmail(request.getEmail()))
            throw new DuplicateResponseException("Email is already in use");
        String encoded = passwordEncoder.encode(request.getPassword());    
        AppUser user = appUserRepository.save(
            new AppUser(request.getUsername(), request.getEmail(),encoded)
        );
        return toRespond(user);
    }

    // ─── READ ────────────────────────────────────────────────────────────────
    
    public List<AppUserPublicResponse> getAllAppUsers(String username) {
        if(username == null || username.isBlank()){
            throw new IllegalArgumentException("At least provide one parameter for search");
        }
        return appUserRepository.findByUsernameContainingIgnoreCase(username)
                .stream()
                .map(this::toPublicRespond)
                .toList();
    }

    public AppUserPublicResponse getAppUserById(Integer id) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser not found"));

        return toPublicRespond(user);
    }
//Not required now 
/* 
    public AppUserResponse getAppUserByEmail(String email) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser not found"));
        accessControl.UserAccessAuthentication(user,"");        
        return toRespond(user);
    }
*/   
    public AppUserResponse getMyProfile() {
       AppUser user = helper.getCurrentUser();
       return toRespond(user);
    }
    // ─── UPDATE ──────────────────────────────────────────────────────────────

    public AppUserResponse updateAppUserName(Integer id, UpdateUsernameRequest request) {

        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser not found"));
        accessControl.UserAccessAuthentication(user);        
        user.setUsername(request.getUsername());
        appUserRepository.save(user);
        return toRespond(user);
    }

    public AppUserResponse updateAppUserName(String email, UpdateUsernameRequest request) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser not found"));
        accessControl.UserAccessAuthentication(user);        
        user.setUsername(request.getUsername());
        appUserRepository.save(user);
        return toRespond(user);
    }

    public AppUserResponse updateEmail(Integer id, UpdateEmailRequest request) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser not found"));
        accessControl.UserAccessAuthentication(user);        
        if (appUserRepository.existsByEmail(request.getEmail()))
            throw new DuplicateResponseException("Email is already in use");

        user.setEmail(request.getEmail());
        appUserRepository.save(user);
        return toRespond(user);
    }

    public AppUserResponse updateEmail(String email, UpdateEmailRequest request) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser not found"));
        accessControl.UserAccessAuthentication(user);        
        if (appUserRepository.existsByEmail(request.getEmail()))
            throw new DuplicateResponseException("Email is already in use");
        user.setEmail(request.getEmail());
        appUserRepository.save(user);
        return toRespond(user);
    }

    public boolean updatePassword(Integer id, UpdateUserPassword request) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser not found"));
        accessControl.UserAccessAuthentication(user);        
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        appUserRepository.save(user);
        return true;
    }

    public boolean updatePassword(String email, UpdateUserPassword request) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser not found"));
        accessControl.UserAccessAuthentication(user);        
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        appUserRepository.save(user);
        return true;
    }

    // ─── DELETE ──────────────────────────────────────────────────────────────

    public boolean deleteAppUser(Integer id) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser not found with id " + id));
        accessControl.UserAccessAuthentication(user);   
        appUserRepository.deleteById(id);
        return true;
    }

    public boolean deleteAppUser(String email) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser not found"));
        accessControl.UserAccessAuthentication(user);          
        appUserRepository.delete(user);
        return true;
    }
}