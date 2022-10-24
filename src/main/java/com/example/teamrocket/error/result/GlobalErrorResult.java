package com.example.teamrocket.error.result;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalErrorResult {
    private boolean success;
    private String errorMessage;
    private String result;

    public static GlobalErrorResult of(String message) {
        return GlobalErrorResult.builder()
                .success(false)
                .errorMessage(message)
                .build();
    }
}
