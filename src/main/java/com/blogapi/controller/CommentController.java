package com.blogapi.controller;

import com.blogapi.dto.request.CommentRequest;
import com.blogapi.dto.response.ApiResponse;
import com.blogapi.dto.response.CommentResponse;
import com.blogapi.service.CommentService;
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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Nested comment threads per post")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/posts/{postId}/comments")
    @Operation(summary = "Get top-level comments for a post (with nested replies)")
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        return ResponseEntity.ok(ApiResponse.ok(commentService.getCommentsByPost(postId, pageable)));
    }

    @PostMapping("/posts/{postId}/comments")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add a comment (or reply) to a post", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Comment added", commentService.addComment(postId, request, auth.getName())));
    }

    @PutMapping("/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update a comment (owner or ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("Comment updated",
                commentService.updateComment(commentId, request, auth.getName())));
    }

    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete a comment (owner or ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long commentId,
            Authentication auth) {
        commentService.deleteComment(commentId, auth.getName());
        return ResponseEntity.ok(ApiResponse.ok("Comment deleted", null));
    }
}
