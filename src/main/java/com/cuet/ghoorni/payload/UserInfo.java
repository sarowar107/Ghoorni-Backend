package com.cuet.ghoorni.payload;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class UserInfo {
    private String userId;
    private String name;
    private String role;
    private String deptName;
    private String batch;
}
