package me.itzg.stomprelay.handlers;

import io.netty.handler.codec.stomp.DefaultStompFrame;

/**
 * @author Geoff Bourne
 */
public interface StompFrameHandler {
    DefaultStompFrame getResponse();

    boolean isCloseAfterResponse();

    StompFrameHandler invoke();
}
