package com.blogapi.controller;

import com.blogapi.dto.request.PostRequest;
import com.blogapi.dto.response.ApiResponse;
import com.blogapi.dto.response.PostResponse;
import com.blogapi.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "Blog post CRUD + search, filter, pagination")
public class PostController {

    private final PostService postService;

    @GetMapping
    @Operation(summary = "Get all published posts (paginated, sortable)")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) Long authorId) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PostResponse> result;
        if (keyword != null && !keyword.isBlank()) {
            result = postService.searchPosts(keyword, pageable);
        } else if (categoryId != null) {
            result = postService.getPostsByCategory(categoryId, pageable);
        } else if (tagId != null) {
            result = postService.getPostsByTag(tagId, pageable);
        } else if (authorId != null) {
            result = postService.getPostsByAuthor(authorId, pageable);
        } else {
            result = postService.getAllPublishedPosts(pageable);
        }

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a post by ID (increments view count)")
    public ResponseEntity<ApiResponse<PostResponse>> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(postService.getPostById(id)));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get a post by SEO-friendly slug")
    public ResponseEntity<ApiResponse<PostResponse>> getPostBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.ok(postService.getPostBySlug(slug)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new post", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @Valid @RequestBody PostRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Post created", postService.createPost(request, auth.getName())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update a post (owner or ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest request,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("Post updated", postService.updatePost(id, request, auth.getName())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete a post (owner or ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id, Authentication auth) {
        postService.deletePost(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.ok("Post deleted", null));
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "Like a post (no auth required)")
    public ResponseEntity<ApiResponse<PostResponse>> likePost(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Post liked", postService.likePost(id)));
    }
}
