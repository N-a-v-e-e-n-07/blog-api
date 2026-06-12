package com.blogapi.service.impl;

import com.blogapi.dto.request.CommentRequest;
import com.blogapi.dto.response.CommentResponse;
import com.blogapi.dto.response.UserSummary;
import com.blogapi.entity.*;
import com.blogapi.exception.ResourceNotFoundException;
import com.blogapi.exception.UnauthorizedException;
import com.blogapi.repository.*;
import com.blogapi.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CommentResponse addComment(Long postId, CommentRequest request, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .post(post)
                .author(author)
                .approved(true)
                .build();

        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", request.getParentId()));
            comment.setParent(parent);
        }

        return toResponse(commentRepository.save(comment));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByPost(Long postId, Pageable pageable) {
        return commentRepository.findTopLevelByPostId(postId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, CommentRequest request, String username) {
        Comment comment = getComment(commentId);
        checkOwnershipOrAdmin(comment.getAuthor().getUsername(), username);
        comment.setContent(request.getContent());
        return toResponse(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, String username) {
        Comment comment = getComment(commentId);
        checkOwnershipOrAdmin(comment.getAuthor().getUsername(), username);
        commentRepository.delete(comment);
    }

    private Comment getComment(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));
    }

    private void checkOwnershipOrAdmin(String ownerUsername, String requestUsername) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (!ownerUsername.equals(requestUsername) && !isAdmin) {
            throw new UnauthorizedException("You do not have permission to modify this comment");
        }
    }

    private CommentResponse toResponse(Comment comment) {
        List<CommentResponse> replies = comment.getReplies() == null ? List.of() :
                comment.getReplies().stream()
                        .filter(Comment::isApproved)
                        .map(this::toResponse)
                        .collect(Collectors.toList());

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .approved(comment.isApproved())
                .author(UserSummary.builder()
                        .id(comment.getAuthor().getId())
                        .username(comment.getAuthor().getUsername())
                        .fullName(comment.getAuthor().getFullName())
                        .role(comment.getAuthor().getRole())
                        .build())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .replies(replies)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
