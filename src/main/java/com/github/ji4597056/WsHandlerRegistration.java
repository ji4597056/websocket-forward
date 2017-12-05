package com.github.ji4597056;

import com.github.ji4597056.server.AbstractWsServerHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * websocket handler registration
 *
 * @author Jeffrey
 * @since 2017/12/04 17:02
 */
public interface WsHandlerRegistration {

    /**
     * add AbstractWsServerHandler
     *
     * @param handlers AbstractWsServerHandler
     */
    void addHandler(AbstractWsServerHandler... handlers);

    /**
     * add HandshakeInterceptor
     *
     * @param interceptors HandshakeInterceptor
     */
    void addInterceptor(HandshakeInterceptor... interceptors);

    /**
     * get AbstractWsServerHandler
     *
     * @param handlerClassName handler name
     * @return AbstractWsServerHandler
     */
    AbstractWsServerHandler getHandler(String handlerClassName);

    /**
     * get HandshakeInterceptor
     *
     * @param interceptorClassName interceptor name
     * @return HandshakeInterceptor
     */
    HandshakeInterceptor getInterceptor(String interceptorClassName);
}
