package me.itzg.stomprelay.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.stomp.StompCommand;
import io.netty.handler.codec.stomp.StompHeaders;

/**
 * @author Geoff Bourne
 */
public interface StompFrameHandlerFactory {
    StompCommand getCommand();

    StompFrameHandler create(ChannelHandlerContext context, StompHeaders headers);
}
