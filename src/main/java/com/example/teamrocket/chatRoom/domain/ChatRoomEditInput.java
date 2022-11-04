package com.example.teamrocket.chatRoom.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoomEditInput {

    @Size(max = 50)
    @NotBlank
    private String title;
    @Future(message = "여행 시작날짜는 현재 이후여야합니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate startDate;
    @Future(message = "여행 끝 날짜는 현재 이후여야합니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate endDate;

    @Min(2)
    @Max(10)
    private int maxParticipant;

}
