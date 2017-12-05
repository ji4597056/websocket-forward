package com.github.ji4597056;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.springframework.web.socket.AbstractWebSocketMessage;

/**
 * message forward filter
 *
 * @author Jeffrey
 * @since 2017/12/04 11:00
 */
public interface MessageFilter {

    /**
     * convert from message
     * if null,not forward
     *
     * @param fromMessage from  message
     * @return forward message
     */
    Object fromMessage(AbstractWebSocketMessage fromMessage);

    /**
     * convert to message
     * if null,not foward
     *
     * @param toMessage to message
     * @return forward message
     */
    Object toMessage(WebSocketFrame toMessage);
}
