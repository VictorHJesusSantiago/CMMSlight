package com.cmmslight.cmmsapi.web;

import com.cmmslight.cmmsapi.dto.AppUserRequest;
import com.cmmslight.cmmsapi.dto.AppUserResponse;
import com.cmmslight.cmmsapi.service.AppUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class AppUserController {

    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping
    public List<AppUserResponse> findAll() {
        return appUserService.findAll();
    }

    @GetMapping("/{id}")
    public AppUserResponse findById(@PathVariable Long id) {
        return appUserService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppUserResponse create(@Valid @RequestBody AppUserRequest request) {
        return appUserService.create(request);
    }

    @PutMapping("/{id}")
    public AppUserResponse update(@PathVariable Long id, @Valid @RequestBody AppUserRequest request) {
        return appUserService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        appUserService.delete(id);
    }
}
