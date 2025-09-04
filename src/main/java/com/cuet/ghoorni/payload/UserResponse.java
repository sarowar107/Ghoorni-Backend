package com.cuet.ghoorni.payload;

import com.cuet.ghoorni.model.User;
import lombok.Data;

@Data
public class UserResponse {
    private String userId;
    private String name;
    private String email;
    private String deptName;
    private String batch;
    private String role;

    public static UserResponse fromEntity(User user) {
        UserResponse response = new UserResponse();
        response.setUserId(user.getUserId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setDeptName(user.getDeptName());
        response.setBatch(user.getBatch());
        response.setRole(user.getRole());
        return response;
    }
}
