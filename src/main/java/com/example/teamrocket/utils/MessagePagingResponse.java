package com.example.teamrocket.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessagePagingResponse<MessageDto> {

    private boolean lastDay;
    private LocalDate targetDay;

    private boolean firstPage;
    private boolean lastPage;
    private int targetDayTotalPage;
    private long targetDayTotalElements;
    private int size;
    private int targetDayCurrentPage;
    private List<MessageDto> content;

    public void setFromPage(Page<MessageDto> page,LocalDate targetDay) {

        this.targetDay = targetDay;

        this.firstPage = page.isFirst();
        this.lastPage = page.isLast();
        this.targetDayTotalPage = page.getTotalPages();
        this.targetDayTotalElements = page.getTotalElements();
        this.size = page.getSize();
        this.targetDayCurrentPage = page.getNumber();
        this.content = page.getContent();
    }

    public void setLastDay(boolean isLastDay){
        this.lastDay = isLastDay;
    }

    public void setFromList(List<MessageDto> list, LocalDate targetDay,boolean isFirstPage,Integer size, int totalPage,
                            int totalElements, int currentPage){
        this.targetDay = targetDay;
        this.firstPage = isFirstPage;
        this.lastPage = list.size() != size;
        this.targetDayTotalPage = totalPage;
        this.targetDayTotalElements = totalElements;
        this.size = size;
        this.targetDayCurrentPage = currentPage;
        this.content = list;
    }
}