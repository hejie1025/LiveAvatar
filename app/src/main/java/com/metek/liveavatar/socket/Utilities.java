package com.metek.liveavatar.socket;

public class Utilities {

    public interface SocketInterface {
        final String TAG = "Message P2P Socket";
        final String SERVER_IP = "172.168.11.137";
        final int SERVER_PORT = 80;
    }

    public interface ActivityInterface {
        final String TAG ="Message P2P Activity";
        final String DISPLAY_MESSAGE_ACTION = "com.theo.p2ptest.DISPLAY_MESSAGE";
        final String EXTRA_MESSAGE = "message";
    }
}
