package com.blogapi.controller;

import com.blogapi.dto.response.ApiResponse;
import com.blogapi.dto.response.CategoryResponse;
import com.blogapi.entity.Category;
import com.blogapi.exception.BadRequestException;
import com.blogapi.exception.ResourceNotFoundException;
import com.blogapi.repository.CategoryRepository;
import com.blogapi.util.SlugUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Blog post categories (ADMIN managed)")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    @Operation(summary = "List all categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAll() {
        List<CategoryResponse> categories = categoryRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(categories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(@PathVariable Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return ResponseEntity.ok(ApiResponse.ok(toResponse(category)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a category (ADMIN only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@RequestBody Category request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException("Category '" + request.getName() + "' already exists");
        }
        request.setSlug(SlugUtils.toSlug(request.getName()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Category created", toResponse(categoryRepository.save(request))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a category (ADMIN only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<CategoryResponse>> update(@PathVariable Long id, @RequestBody Category request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSlug(SlugUtils.toSlug(request.getName()));
        return ResponseEntity.ok(ApiResponse.ok("Category updated", toResponse(categoryRepository.save(category))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a category (ADMIN only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        categoryRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Category deleted", null));
    }

    private CategoryResponse toResponse(Category c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .slug(c.getSlug())
                .description(c.getDescription())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
