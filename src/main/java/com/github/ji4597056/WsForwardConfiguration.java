package com.github.ji4597056;

import com.github.ji4597056.WsForwardProperties.ForwardHandler;
import com.github.ji4597056.server.AbstractWsServerHandler;
import com.github.ji4597056.server.DiscoveryForwardHandler;
import com.github.ji4597056.utils.CommonUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * websocket forward configuration
 *
 * @author Jeffrey
 * @since 2017/12/04 10:37
 */
@Configuration
@EnableWebSocket
@ConditionalOnProperty(
    prefix = "ws.forward",
    name = {"enabled"},
    havingValue = "true"
)
@EnableConfigurationProperties({WsForwardProperties.class})
public class WsForwardConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(WsForwardConfiguration.class);

    @Autowired
    private WsForwardProperties wsForwardProperties;

    @Autowired(required = false)
    private DiscoveryClient discoveryClient;

    @Bean
    @ConditionalOnMissingBean(AbstractWsServerHandler.class)
    public AbstractWsServerHandler discoveryForwardHandler() {
        return new DiscoveryForwardHandler(discoveryClient, wsForwardProperties);
    }

    @Bean
    @ConditionalOnMissingBean(WsHandlerRegistration.class)
    public WsHandlerRegistration wsHandlerRegistration() {
        return new DefaultWsHandlerRegistration();
    }

    @Configuration
    @AutoConfigureAfter({WsHandlerRegistration.class, AbstractWsServerHandler.class})
    protected class WebsocketConfig implements WebSocketConfigurer {

        @Autowired
        private WsHandlerRegistration handlerRegistration;

        @Autowired(required = false)
        private List<HandshakeInterceptor> defaultInterceptors;

        @Autowired
        private AbstractWsServerHandler defaultHandler;

        @Override
        public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
            Map<String, ForwardHandler> handlers = wsForwardProperties.getHandlers();
            if (handlers != null && !handlers.isEmpty()) {
                wsForwardProperties.getHandlers()
                    .forEach((id, handler) -> {
                        if (handler.isEnabled()) {
                            registryHandler(webSocketHandlerRegistry, handler);
                        }
                    });
            }
        }

        /**
         * register handler
         *
         * @param registry WebSocketHandlerRegistry
         * @param handler ForwardHandler
         */
        private void registryHandler(WebSocketHandlerRegistry registry, ForwardHandler handler) {
            WebSocketHandlerRegistration registration = getRegistration(registry, handler);
            // set allowedOrigins
            if (handler.getAllowedOrigins() == null) {
                registration.setAllowedOrigins("*");
            } else {
                registration.setAllowedOrigins(handler.getAllowedOrigins());
            }
            // set interceptors
            String[] interceptorClasses = handler.getInterceptorClasses();
            if (interceptorClasses != null) {
                HandshakeInterceptor[] interceptors = Arrays.stream(interceptorClasses)
                    .map(className -> handlerRegistration.getInterceptor(className))
                    .toArray(HandshakeInterceptor[]::new);
                registration.addInterceptors(interceptors);
            } else {
                if (defaultInterceptors != null) {
                    registration.addInterceptors(defaultInterceptors
                        .toArray(new HandshakeInterceptor[defaultInterceptors.size()]));
                }
            }
            // set withSocketJs
            if (handler.isWithJs()) {
                registration.withSockJS();
            }
        }

        /**
         * get WebSocketHandlerRegistration
         *
         * @param registry WebSocketHandlerRegistry
         * @param handler ForwardHandler
         * @return WebSocketHandlerRegistration
         */
        private WebSocketHandlerRegistration getRegistration(WebSocketHandlerRegistry registry,
            ForwardHandler handler) {
            // set handler
            String className = handler.getHandlerClass();
            if (className == null) {
                return registry.addHandler(defaultHandler, CommonUtils.getWsPattern(handler));
            } else {
                try {
                    return registry
                        .addHandler(handlerRegistration.getHandler(handler.getHandlerClass()),
                            CommonUtils.getWsPattern(handler));
                } catch (Exception e) {
                    LOGGER.error("Set webosocket handler error!error: {}", e);
                    throw new IllegalArgumentException("Set websocket handler error!");
                }
            }
        }
    }
}
