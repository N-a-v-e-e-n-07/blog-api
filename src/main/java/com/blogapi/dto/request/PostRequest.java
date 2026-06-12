package com.blogapi.dto.request;

import com.blogapi.entity.Post;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class PostRequest {

    @NotBlank @Size(min = 5, max = 255)
    private String title;

    @Size(max = 500)
    private String summary;

    @NotBlank
    private String content;

    private Post.PostStatus status = Post.PostStatus.DRAFT;

    private String coverImageUrl;

    private Long categoryId;

    private List<String> tagNames;
}
