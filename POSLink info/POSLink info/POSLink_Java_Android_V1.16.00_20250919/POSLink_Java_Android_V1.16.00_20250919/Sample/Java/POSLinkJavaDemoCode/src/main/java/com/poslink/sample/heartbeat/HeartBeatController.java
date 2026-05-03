package com.poslink.sample.heartbeat;

import com.poslink.sample.common.model.BaseResponse;


public class HeartBeatController {


    private static int diedCount;

    public static BaseResponse checkHeartBeat() {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setCode(BaseResponse.CODE_OK);
        baseResponse.setMessage(BaseResponse.MESSAGE_OK);
        diedCount = 0;
        return baseResponse;
    }

    public static void dieLoop() {
        new Thread(() -> {
            while (true) {
                if (diedCount >= 2) {
                    System.out.print("Ready to exit");
                    System.exit(0);
                }
                diedCount++;
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}