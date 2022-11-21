package com.example.teamrocket.utils;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagingResponse<T> {

    private boolean firstPage;
    private boolean lastPage;
    private int totalPage;
    private long totalElements;
    private int size;
    private int currentPage;
    private List<T> content;

    public static PagingResponse fromEntity(Page page) {
        return PagingResponse.builder()
                .firstPage(page.isFirst())
                .lastPage(page.isLast())
                .totalPage(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .size(page.getSize())
                .currentPage(page.getNumber())
                .content(page.getContent())
                .build();
    }
}