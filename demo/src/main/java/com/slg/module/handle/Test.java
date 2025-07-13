package com.slg.module.handle;

import com.slg.module.annotation.ToMethod;
import com.slg.module.annotation.ToServer;
import com.slg.module.message.MsgResponse;
import com.slg.module.util.SentMsgUtil;
import io.netty.channel.ChannelHandlerContext;
import message.Friend;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@ToServer
public class Test {
    private static final Logger logger = LogManager.getLogger(Test.class);
    private SentMsgUtil sentUtil = SentMsgUtil.getInstance();

    @ToMethod(value = 101)
    public MsgResponse ffff(ChannelHandlerContext ctx, Friend.FriendRequest request, long userId) throws ExecutionException, InterruptedException, TimeoutException {
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



//        Friend.TestRequest.Builder testReq = Friend.TestRequest.newBuilder().setData("1233");
//        CompletableFuture<Friend.TestResponse> testResponse = sentUtil.sentMsgAsync(102, testReq, Friend.TestResponse.class);
//
//        if (testResponse!=null){
//            //异步
//            testResponse.thenAccept(resq->{
//                String result = resq.getResult();
//            });
//            //同步
//            Friend.TestResponse testResponse1 = testResponse.get(5, TimeUnit.SECONDS);
//        }



        // 使用异步调用获取响应
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
//        System.err.println("222222222222222");

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
