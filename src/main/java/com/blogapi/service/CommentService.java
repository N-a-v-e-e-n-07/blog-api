package com.blogapi.service;

import com.blogapi.dto.request.CommentRequest;
import com.blogapi.dto.response.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    CommentResponse addComment(Long postId, CommentRequest request, String username);
    Page<CommentResponse> getCommentsByPost(Long postId, Pageable pageable);
    CommentResponse updateComment(Long commentId, CommentRequest request, String username);
    void deleteComment(Long commentId, String username);
}
