package com.blogapi.service;

import com.blogapi.dto.request.PostRequest;
import com.blogapi.dto.response.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {
    PostResponse createPost(PostRequest request, String username);
    PostResponse updatePost(Long id, PostRequest request, String username);
    void deletePost(Long id, String username);
    PostResponse getPostById(Long id);
    PostResponse getPostBySlug(String slug);
    Page<PostResponse> getAllPublishedPosts(Pageable pageable);
    Page<PostResponse> searchPosts(String keyword, Pageable pageable);
    Page<PostResponse> getPostsByCategory(Long categoryId, Pageable pageable);
    Page<PostResponse> getPostsByTag(Long tagId, Pageable pageable);
    Page<PostResponse> getPostsByAuthor(Long authorId, Pageable pageable);
    PostResponse likePost(Long id);
}
