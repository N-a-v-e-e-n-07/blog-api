package com.blogapi.dto.response;

import com.blogapi.entity.User;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserSummary {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private User.Role role;
}
