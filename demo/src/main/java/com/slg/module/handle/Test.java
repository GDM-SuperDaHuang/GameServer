package com.slg.module.handle;

import com.slg.module.annotation.ToMethod;
import com.slg.module.annotation.ToServer;
import com.slg.module.message.MsgResponse;
import io.netty.channel.ChannelHandlerContext;
import message.Friend;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

@ToServer
public class Test {
    private static final Logger logger = LogManager.getLogger(Test.class);

    @ToMethod(value = 101)
    public MsgResponse ffff(ChannelHandlerContext ctx, Friend.FriendRequest request, long userId) {
        ArrayList<Long> longs = new ArrayList<>();
        longs.add(110L);
        longs.add(211L);
        longs.add(211L);
        longs.add(211L);
        longs.add(211L);
        longs.add(211L);
        longs.add(211L);
        longs.add(211L);
        longs.add(211L);
        longs.add(211L);
        longs.add(211L);
        longs.add(211L);
        longs.add(211L);
        longs.add(211L);
        longs.add(211L);
        longs.add(211L);
        longs.add(211L);
        longs.add(211L);
        longs.add(211L);
        longs.add(211L);
        longs.add(211L);
        longs.add(211L);
        longs.add(211L);
        longs.add(211L);
        longs.add(211L);
        longs.add(211L);
        longs.add(211L);
        longs.add(211L);
        longs.add(211L);
        longs.add(211L);
        longs.add(211L);

        Friend.FriendsResponse.Builder friendsResponse = Friend.FriendsResponse.newBuilder()
                .addAllUserIdList(longs);
        MsgResponse msgResponse = MsgResponse.newInstance(friendsResponse);

//        // 使用异步调用获取响应
//        CompletableFuture<ByteBufferMessage> future = sentUtil.sentMsgAsync(2, sendMsg);
//
//        future.thenAccept(response -> {
//            byte[] body = response.getBody();
//            try {
//                Friend.FriendsResponse friendsResponse1 = Friend.FriendsResponse.parseFrom(body);
//                System.out.println(friendsResponse1);
//            } catch (InvalidProtocolBufferException e) {
//                throw new RuntimeException(e);
//            }
//            System.out.println("Received response: " + response);
//        }).exceptionally(ex -> {
//            System.err.println("Request failed: " + ex.getMessage());
//            return null;
//        });
        System.err.println("222222222222222");

        // 同步等待（如果需要）
//        ByteBufferMessage response = null;
//        try {
//            response = future.get(5, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        } catch (ExecutionException e) {
//            throw new RuntimeException(e);
//        } catch (TimeoutException e) {
//            throw new RuntimeException(e);
//        }
//        System.err.println("Request failed0000: " + response);

        return msgResponse;
    }

}
