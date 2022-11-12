package com.example.teamrocket.scheulde;

import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomMySql;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomParticipant;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomStatus;
import com.example.teamrocket.chatRoom.repository.mysql.ChatRoomMySqlRepository;
import com.example.teamrocket.chatRoom.repository.mysql.ChatRoomParticipantRepository;
import com.example.teamrocket.user.entity.ProviderType;
import com.example.teamrocket.user.entity.RoleType;
import com.example.teamrocket.user.entity.User;
import com.example.teamrocket.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Transactional
public class ChatDeleteTest {
    private int amount = 1001;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ChatRoomMySqlRepository chatRoomMySqlRepository;
    @Autowired
    private ChatRoomParticipantRepository chatRoomParticipantRepository;

//    @Test
    void userCreate(){
        User build = User.builder()
                .username("test")
                .email("test")
                .profileImage("test")
                .uuid("ttes")
                .password("test")
                .providerType(ProviderType.LOCAL)
                .roleType(RoleType.GUEST)
                .build();
        userRepository.save(build);
    }
// 여기는 실제 서버 로 다이렉트 벌크 인설트 할떄 사용 한다. properties 부분에서 주소 수정후 사용할것
//    @Test
//    @Commit
    void bulkInsertToRealRds10K() throws Exception{
        // 채팅방 1만개 생성
        User guiwoo = userRepository.findById(13L).get();
        List<ChatRoomMySql> chat = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            ChatRoomMySql build = ChatRoomMySql.builder()
                    .title("Chat Testing " + i)
                    .owner(guiwoo)
                    .chatRoomStatus(ChatRoomStatus.PRE_TRAVEL)
                    .startDate(LocalDate.now().minusDays(2))
                    .endDate(LocalDate.now().minusDays(3))
                    .maxParticipant(10)
                    .rcate1("미국")
                    .rcate2("실리콘밸리")
                    .longitude("미국 그 어딘가")
                    .latitude("미국 그 어딘가 2")
                    .build();

            chat.add(build);
        }
        chatRoomMySqlRepository.saveAll(chat);
        ChatRoomMySql save = getChatRoomMysqlOne();
        // 채팅 하나 만들고
        List<User> list = getUsers();
        // 유저 100 명 만들고
        createChatParticipants100(save, list);
        // 채팅 참여자 벌크 인설트
    }

    @BeforeEach
    void init(){
        User guiwoo = userRepository.findById(1L).get();
        List<ChatRoomMySql> chat = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            ChatRoomMySql build = ChatRoomMySql.builder()
                    .title("Chat Testing " + i)
                    .owner(guiwoo)
                    .chatRoomStatus(ChatRoomStatus.PRE_TRAVEL)
                    .startDate(LocalDate.now().minusDays(2))
                    .endDate(LocalDate.now().minusDays(3))
                    .maxParticipant(10)
                    .rcate1("미국")
                    .rcate2("실리콘밸리")
                    .longitude("미국 그 어딘가")
                    .latitude("미국 그 어딘가 2")
                    .build();

            chat.add(build);
        }
        chatRoomMySqlRepository.saveAll(chat);
    }

    @Test
    @DisplayName("ChatRoom Test find expired ChatRoom")
    void chatRoomSelect100ExpireDate() throws Exception{
        List<ChatRoomMySql> expiredDate = chatRoomMySqlRepository.findExpiredDate(LocalDate.now());
        Assertions.assertThat(expiredDate.size()).isEqualTo(1000);
    }

    @Test
    @DisplayName("ChatRoom Delete 100 expired ChatRoom")
    void chatRoomDelete100(){
        // 스케쥴에서 삭제할때 1000 개씩 보내주기
        List<ChatRoomMySql> expiredDate = chatRoomMySqlRepository.findExpiredDate(LocalDate.now());
        chatRoomMySqlRepository.deleteAllByChatRoomList(expiredDate);
        List<ChatRoomMySql> expiredDate2 = chatRoomMySqlRepository.findExpiredDate(LocalDate.now());
        Assertions.assertThat(expiredDate2.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("Insert 10 People Participants")
    public void insert10ToParticipants() throws Exception{
        ChatRoomMySql save = getChatRoomMysqlOne();
        // 채팅 하나 만들고
        List<User> list = getUsers();
        // 유저 100 명 만들고
        createChatParticipants100(save, list);
        // 채팅 참여자 벌크 인설트

        List<ChatRoomParticipant> allByChatRoomMySql =
                chatRoomParticipantRepository.findAllByChatRoomMySql(save);
        Assertions.assertThat(allByChatRoomMySql.size()).isEqualTo(100);
    }

    @Test
    @DisplayName("Delete all Participants by ChatRoom")
    public void deleteChatRoomParticipant() throws Exception{
        ChatRoomMySql save = getChatRoomMysqlOne();
        // 채팅 하나 만들고
        List<User> list = getUsers();
        // 유저 100 명 만들고
        List<ChatRoomParticipant> list2 = createChatParticipants100(save, list);
        // 채팅 참여자 벌크 인설트

        // 채팅 참여자 삭제
        chatRoomParticipantRepository.deleteAllByChatRoomParticipants(list2);

        // 채팅 참여자 확인
        List<ChatRoomParticipant> allByChatRoomMySql =
                chatRoomParticipantRepository.findAllByChatRoomMySql(save);
        Assertions.assertThat(allByChatRoomMySql.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("Delete all Participants First and ChatRoom test")
    public void deleteParticipantsFirstAndChatRoom() throws Exception{
        ChatRoomMySql save = getChatRoomMysqlOne();
        // 채팅 하나 만들고
        List<User> list = getUsers();
        // 유저 100 명 만들고
        createChatParticipants100(save, list);
        // 채팅 참여자 벌크 인설트

        // 기간지난 채팅 방에 먼저 다가져오고
        List<ChatRoomMySql> expiredDate = chatRoomMySqlRepository.findExpiredDate(LocalDate.now());
        // 기간지난 채팅 방에 대한 모든 채팅참여자 가져오고
        List<ChatRoomParticipant> allByChatRoomMySqlExpired =
                chatRoomParticipantRepository.findAllByChatRoomMySqlExpired(expiredDate);
        //지워주고
        chatRoomParticipantRepository.deleteAllByChatRoomParticipants(allByChatRoomMySqlExpired);
        //마지막으로 채팅지워 주고
        chatRoomMySqlRepository.deleteAllByChatRoomList(expiredDate);

        List<ChatRoomMySql> expiredDate1 = chatRoomMySqlRepository.findExpiredDate(LocalDate.now());
        Assertions.assertThat(expiredDate1.size()).isEqualTo(0);

    }
   private List<ChatRoomParticipant> createChatParticipants100(ChatRoomMySql save, List<User> list) {
        List<ChatRoomParticipant> list2 = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            ChatRoomParticipant chatRoomParticipant =
                    ChatRoomParticipant.builder()
                            .chatRoomMySql(save)
                            .user(list.get(i))
                            .build();
            list2.add(chatRoomParticipant);
        }
        chatRoomParticipantRepository.saveAll(list2);
        return list2;
    }
    private List<User> getUsers() {
        List<User> list = new ArrayList<>();
        for(int i=0;i<100;i++){
            User user = User.builder()
                    .username("test")
                    .email("test")
                    .profileImage("test")
                    .uuid("ttes")
                    .password("test")
                    .providerType(ProviderType.LOCAL)
                    .roleType(RoleType.GUEST)
                    .build();
            User user1 = userRepository.save(user);
            list.add(user1);
        }
        return list;
    }
    private ChatRoomMySql getChatRoomMysqlOne() {
        User guiwoo = userRepository.findById(1L).get();
        ChatRoomMySql chatRoom = ChatRoomMySql.builder()
                .title("Chat Testing " +1)
                .owner(guiwoo)
                .chatRoomStatus(ChatRoomStatus.PRE_TRAVEL)
                .startDate(LocalDate.now().minusDays(2))
                .endDate(LocalDate.now().minusDays(3))
                .maxParticipant(10)
                .rcate1("미국")
                .rcate2("실리콘밸리")
                .longitude("미국 그 어딘가")
                .latitude("미국 그 어딘가 2")
                .build();
        ChatRoomMySql save = chatRoomMySqlRepository.save(chatRoom);
        return save;
    }
}
