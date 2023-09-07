package br.psi.giganet.stockapi.config.websocket;

import br.psi.giganet.stockapi.config.project_property.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private ApplicationProperties properties;

    @Autowired
    private TokenStore tokenStore;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {

            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                try {
                    StompHeaderAccessor accessor =
                            MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                        String authorization = accessor.getFirstNativeHeader("Authorization");
                        String accessToken = authorization.split(" ")[1];
                        accessor.setUser(tokenStore.readAuthentication(accessToken));
                    }
                    return message;
                } catch (Exception e) {
                    return null;
                }
            }
        });
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(properties.getUiCredentials().getUrl());

        registry.addEndpoint("/ws")
                .setAllowedOrigins(properties.getUiCredentials().getUrl())
                .withSockJS();
    }
}
