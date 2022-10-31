package com.example.teamrocket.config.socket;

import com.example.teamrocket.config.jwt.AuthToken;
import com.example.teamrocket.config.jwt.AuthTokenProvider;
import com.example.teamrocket.user.entity.User;
import com.example.teamrocket.user.repository.UserRepository;
import com.sun.security.auth.UserPrincipal;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@Component
public class StompHandler implements ChannelInterceptor {
    private final AuthTokenProvider authTokenProvider;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,
                StompHeaderAccessor.class);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            try {
                AuthToken authToken1 = authTokenProvider.convertAuthToken(accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION).substring(7));
                boolean validate = authToken1.validate();
                if(!validate) throw new RuntimeException("Token is not valid");

                String uuid = authToken1.getUuid();
                User user = userRepository.findByUuid(uuid)
                        .orElseThrow(() -> new RuntimeException("User does not exist"));
                accessor.setUser(new UserPrincipal(user.getUuid()));
            } catch (Exception e) {
                throw new RuntimeException("Token is not valid");
            }
        }
        return message;
    }
}
