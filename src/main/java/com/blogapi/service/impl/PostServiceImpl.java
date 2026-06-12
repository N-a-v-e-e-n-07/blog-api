package com.blogapi.service.impl;

import com.blogapi.dto.request.PostRequest;
import com.blogapi.dto.response.*;
import com.blogapi.entity.*;
import com.blogapi.exception.ResourceNotFoundException;
import com.blogapi.exception.UnauthorizedException;
import com.blogapi.repository.*;
import com.blogapi.service.PostService;
import com.blogapi.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public PostResponse createPost(PostRequest request, String username) {
        User author = getUser(username);

        Post post = Post.builder()
                .title(request.getTitle())
                .slug(generateUniqueSlug(request.getTitle()))
                .summary(request.getSummary())
                .content(request.getContent())
                .status(request.getStatus())
                .coverImageUrl(request.getCoverImageUrl())
                .author(author)
                .build();

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            post.setCategory(category);
        }

        if (request.getTagNames() != null && !request.getTagNames().isEmpty()) {
            post.setTags(resolveOrCreateTags(request.getTagNames()));
        }

        return toResponse(postRepository.save(post));
    }

    @Override
    @Transactional
    public PostResponse updatePost(Long id, PostRequest request, String username) {
        Post post = getPost(id);
        checkOwnershipOrAdmin(post.getAuthor().getUsername(), username);

        post.setTitle(request.getTitle());
        post.setSummary(request.getSummary());
        post.setContent(request.getContent());
        post.setStatus(request.getStatus());
        post.setCoverImageUrl(request.getCoverImageUrl());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            post.setCategory(category);
        } else {
            post.setCategory(null);
        }

        if (request.getTagNames() != null) {
            post.setTags(resolveOrCreateTags(request.getTagNames()));
        }

        return toResponse(postRepository.save(post));
    }

    @Override
    @Transactional
    public void deletePost(Long id, String username) {
        Post post = getPost(id);
        checkOwnershipOrAdmin(post.getAuthor().getUsername(), username);
        postRepository.delete(post);
    }

    @Override
    @Transactional
    public PostResponse getPostById(Long id) {
        Post post = getPost(id);
        postRepository.incrementViewCount(id);
        return toResponse(post);
    }

    @Override
    @Transactional
    public PostResponse getPostBySlug(String slug) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "slug", slug));
        postRepository.incrementViewCount(post.getId());
        return toResponse(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPublishedPosts(Pageable pageable) {
        return postRepository.findAllPublished(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> searchPosts(String keyword, Pageable pageable) {
        return postRepository.searchByKeyword(keyword, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByCategory(Long categoryId, Pageable pageable) {
        return postRepository.findByCategoryId(categoryId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByTag(Long tagId, Pageable pageable) {
        return postRepository.findByTagId(tagId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByAuthor(Long authorId, Pageable pageable) {
        return postRepository.findByAuthorId(authorId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public PostResponse likePost(Long id) {
        getPost(id); // validate exists
        postRepository.incrementLikeCount(id);
        return toResponse(getPost(id));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Post getPost(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private String generateUniqueSlug(String title) {
        String base = SlugUtils.toSlug(title);
        if (!postRepository.findBySlug(base).isPresent()) return base;
        return SlugUtils.toUniqueSlug(title);
    }

    private List<Tag> resolveOrCreateTags(List<String> tagNames) {
        return tagNames.stream().map(name -> {
            String slug = SlugUtils.toSlug(name);
            return tagRepository.findByName(name).orElseGet(() ->
                    tagRepository.save(Tag.builder().name(name).slug(slug).build()));
        }).collect(Collectors.toList());
    }

    private void checkOwnershipOrAdmin(String ownerUsername, String requestUsername) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (!ownerUsername.equals(requestUsername) && !isAdmin) {
            throw new UnauthorizedException("You do not have permission to modify this post");
        }
    }

    private PostResponse toResponse(Post post) {
        long commentCount = commentRepository.countApprovedByPostId(post.getId());

        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .summary(post.getSummary())
                .content(post.getContent())
                .status(post.getStatus())
                .coverImageUrl(post.getCoverImageUrl())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(commentCount)
                .author(toUserSummary(post.getAuthor()))
                .category(post.getCategory() != null ? toCategoryResponse(post.getCategory()) : null)
                .tags(post.getTags().stream().map(this::toTagResponse).collect(Collectors.toList()))
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    private UserSummary toUserSummary(User user) {
        return UserSummary.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    private CategoryResponse toCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .build();
    }

    private TagResponse toTagResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .slug(tag.getSlug())
                .build();
    }
}
