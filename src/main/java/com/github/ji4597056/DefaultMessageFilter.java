package com.github.ji4597056;

import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.springframework.web.socket.AbstractWebSocketMessage;
import org.springframework.web.socket.TextMessage;

/**
 * default message filter
 *
 * @author Jeffrey
 * @since 2017/12/04 11:22
 */
public class DefaultMessageFilter implements MessageFilter {

    @Override
    public Object fromMessage(AbstractWebSocketMessage fromMessage) {
        if (fromMessage != null && fromMessage.getPayload() instanceof String) {
            return new TextWebSocketFrame((String) fromMessage.getPayload());
        }
        return null;
    }

    @Override
    public Object toMessage(WebSocketFrame toMessage) {
        if (toMessage != null) {
            return new TextMessage(ByteBufUtil.getBytes(toMessage.content()));
        }
        return null;
    }
}
