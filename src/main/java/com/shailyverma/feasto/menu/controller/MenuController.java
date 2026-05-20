package com.shailyverma.feasto.menu.controller;

import com.shailyverma.feasto.menu.dtos.MenuDTO;
import com.shailyverma.feasto.menu.services.MenuServiceImpl;
import com.shailyverma.feasto.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuServiceImpl menuService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response<MenuDTO>> createMenu(
            @ModelAttribute @Valid MenuDTO menuDTO
    ) {
        return ResponseEntity.ok(menuService.createMenu(menuDTO));
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response<MenuDTO>> updateMenu(
            @ModelAttribute @Valid MenuDTO menuDTO
    ) {
        return ResponseEntity.ok(menuService.updateMenu(menuDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<MenuDTO>> getMenuById(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.getMenuById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response<?>> deleteMenu(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.deleteMenu(id));
    }

    @GetMapping
    public ResponseEntity<Response<List<MenuDTO>>> getMenus(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(menuService.getMenus(categoryId, search));
    }
}
