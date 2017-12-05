/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
//The MIT License
//
//Copyright (c) 2009 Carl Bystršm
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in
//all copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//THE SOFTWARE.

package com.github.ji4597056.client;

import com.github.ji4597056.MessageFilter;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.util.CharsetUtil;
import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * netty handler(websocket forward client handler)
 *
 * @author Jeffrey
 * @since 2017/11/28 17:14
 */
public class WsForwardClientHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(WsForwardClientHandler.class);

    /**
     * WebSocketClientHandshaker
     */
    private final WebSocketClientHandshaker handshaker;

    /**
     * WebSocketSession
     */
    private final WebSocketSession webSocketSession;

    /**
     * MessageFilter
     */
    private final MessageFilter messageFilter;

    /**
     * ChannelPromise
     */
    private ChannelPromise handshakeFuture;

    public WsForwardClientHandler(WebSocketClientHandshaker handshaker,
        WebSocketSession webSocketSession, MessageFilter messageFilter) {
        this.handshaker = handshaker;
        this.webSocketSession = webSocketSession;
        this.messageFilter = messageFilter;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        LOGGER.info("WebSocket Client disconnected!");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            try {
                handshaker.finishHandshake(ch, (FullHttpResponse) msg);
                handshakeFuture.setSuccess();
            } catch (WebSocketHandshakeException e) {
                LOGGER.info("WebSocket Client failed to connect");
                handshakeFuture.setFailure(e);
            }
            return;
        }
        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException(
                "Unexpected FullHttpResponse (getStatus=" + response.status() +
                    ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }
        // handler websocket message
        Optional.ofNullable(msg).map(frame -> messageFilter.toMessage((WebSocketFrame) frame))
            .filter(message -> message instanceof WebSocketMessage)
            .ifPresent(message -> {
                try {
                    webSocketSession.sendMessage((WebSocketMessage<?>) message);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("Websocket forward client caught exception!exception: {}", cause);
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
        if (webSocketSession.isOpen()) {
            try {
                webSocketSession.close();
            } catch (IOException e) {
                LOGGER.error("Close websocket session error!error: {}", e);
            }
        }
    }
}
