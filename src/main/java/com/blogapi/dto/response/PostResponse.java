package com.blogapi.dto.response;

import com.blogapi.entity.Post;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PostResponse {
    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String content;
    private Post.PostStatus status;
    private String coverImageUrl;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private UserSummary author;
    private CategoryResponse category;
    private List<TagResponse> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
