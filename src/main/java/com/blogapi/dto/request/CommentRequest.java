package com.blogapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentRequest {
    @NotBlank @Size(min = 1, max = 2000)
    private String content;

    private Long parentId; // for threaded replies
}
