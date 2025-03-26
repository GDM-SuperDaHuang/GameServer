package com.slg.module.handle;

import account.Account;
import com.slg.module.annotation.ToMethod;
import com.slg.module.annotation.ToServer;
import com.slg.module.message.ByteBufferMessage;

import com.slg.module.message.MsgResponse;
import com.slg.module.rpc.client.SentUtil;
import friend.Friend;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@ToServer
public class Test {
    private static final Logger logger = LogManager.getLogger(Test.class);

    @Autowired
    private SentUtil sentUtil;

    @ToMethod(value = 1)
    public MsgResponse diy(ChannelHandlerContext ctx, Account.LoginRequest request, long userId) throws IOException, InterruptedException {
        Account.LoginResponse.Builder builder = Account.LoginResponse.newBuilder()
                .setAaa(999999999)
                .setBbb(777777777);
        MsgResponse msgResponse = new MsgResponse();
        msgResponse.setBody(builder);
        msgResponse.setErrorCode(0);
        return msgResponse;
    }


    @ToMethod(value = 2)
    public MsgResponse ffff(ChannelHandlerContext ctx, Friend.FriendRequest request, long userId) throws IOException, ExecutionException, InterruptedException, TimeoutException {
        ArrayList<Long> longs = new ArrayList<>();
        longs.add(110L);
        longs.add(211L);
        Friend.FriendRequest.Builder sendMsg = Friend.FriendRequest.newBuilder().setUserId(778899L);


        Friend.FriendsResponse.Builder friendsResponse = Friend.FriendsResponse.newBuilder()
                .addAllUserIdList(longs);
        MsgResponse msgResponse = new MsgResponse();
        msgResponse.setBody(friendsResponse);
        msgResponse.setErrorCode(0);

        // 使用异步调用获取响应
        CompletableFuture<ByteBufferMessage> future = sentUtil.sentMsgAsync(2, sendMsg);

//        future.thenAccept(response -> {
//            byte[] body = response.getBody();
//            try {
//                MSG.FriendsResponse friendsResponse1 = MSG.FriendsResponse.parseFrom(body);
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
        ByteBufferMessage response = future.get(5, TimeUnit.SECONDS);
        System.err.println("Request failed0000: " + response);

        return msgResponse;
    }

}
