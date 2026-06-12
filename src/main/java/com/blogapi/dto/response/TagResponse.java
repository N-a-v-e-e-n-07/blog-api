package com.blogapi.dto.response;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TagResponse {
    private Long id;
    private String name;
    private String slug;
}
