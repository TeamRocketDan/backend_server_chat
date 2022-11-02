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
public class ChatRoomCreateInput {

    @NotBlank(message = "제목은 필수 입력값입니다.")
    private String title;
    @Future(message = "여행 시작날짜는 현재 이후여야합니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate startDate;
    @Future(message = "여행 끝날짜는 현재 이후여야합니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate endDate;

    @Min(2)
    @Max(10)
    private int maxParticipant;
    private boolean privateRoom;
    private String password;

    @NotNull
    private String rcate1;
    @NotNull
    private String rcate2;

    @NotNull
    private String longitude;
    @NotNull
    private String latitude;

}
