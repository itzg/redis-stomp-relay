package me.itzg.stomprelay.helpers;

import io.netty.channel.ChannelHandlerContext;

/**
 * @author @albertocsm
 */
public class Listner {

  public static String getListenerUniqueId(ChannelHandlerContext context, String subId){

    return String.format("%s@%s", subId, context.channel().remoteAddress());
  }
}
