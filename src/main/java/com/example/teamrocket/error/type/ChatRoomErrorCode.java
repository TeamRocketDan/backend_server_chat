package com.example.teamrocket.error.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ChatRoomErrorCode {
    NOT_PARTICIPATED_USER(HttpStatus.BAD_REQUEST,"방에 참가한 이력이 없습니다."),
    EXCEED_MAX_PARTICIPANTS(HttpStatus.BAD_REQUEST,"정원을 넘어 들어갈 수 없습니다."),
    PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST,"비밀번호가 일치하지 않습니다."),
    ALREADY_PARTICIPATE(HttpStatus.BAD_REQUEST,"이미 방에 참가한 사람입니다."),
    MAX_PARTICIPANT_IS_TOO_SMALL(HttpStatus.BAD_REQUEST,"현재 채팅방 인원보다 채팅방 인원을 적게 수정할 수 없습니다."),
    START_DATE_MUST_BE_AFTER_TODAY(HttpStatus.BAD_REQUEST,"여행 시작날짜는 현재 날짜 이후여야합니다."),
    NOT_CHAT_ROOM_OWNER(HttpStatus.BAD_REQUEST,"방장이 아닙니다."),
    CHAT_ROOM_NOT_FOUND(HttpStatus.BAD_REQUEST,"방을 찾을 수 없습니다."),
    TRAVEL_START_DATE_MUST_BE_BEFORE_END_DATE(HttpStatus.BAD_REQUEST,"여행 시작 날짜는 여행 끝 날짜 이전이여야 합니다.");

    private final HttpStatus httpStatus;
    private final String message;
}