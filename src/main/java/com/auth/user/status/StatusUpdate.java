package com.auth.user.status;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatusUpdate {
    private String username;
    private boolean online;
}

