package com.metek.liveavatar.socket;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UDPClient implements Utilities.SocketInterface {
    private static final String TAG = UDPClient.class.getSimpleName();
    private boolean isReceive = false;
    private Context context;
    private DatagramSocket datagramSocket;
    private DatagramPacket datagramPacket;
    private SocketAddress remoteAddress;
    private ExecutorService executor;

    public UDPClient(Context context) {
        this.context = context;
        this.executor = Executors.newFixedThreadPool(3);
    }

    public void start(final String remoteIP, final int remotePort, final int port) {
        Log.i(TAG, "连接开启");
        if (datagramSocket != null && datagramSocket.getLocalPort() == port) {
            Log.w(TAG, "端口重复连接");
            return;
        }
        try {
            datagramSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        remoteAddress = new InetSocketAddress(remoteIP, remotePort);
        isReceive = true;

        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (isReceive) {
                    try {
                        byte[] buffer = new byte[1024];
                        datagramPacket = new DatagramPacket(buffer, buffer.length);
                        datagramSocket.receive(datagramPacket);

                        ByteBuffer byteBuffer = ByteBuffer.wrap(
                                datagramPacket.getData(),
                                datagramPacket.getOffset(),
                                datagramPacket.getLength());
                        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                        int code = byteBuffer.getInt();
                        int size = byteBuffer.getInt();
                        byte[] body = new byte[size];
                        byteBuffer.get(body);
                        MsgData data = new MsgData(code, size, body);
                        Log.i(TAG, "接受数据 " + data.toLogString());

                        Intent intent = new Intent(Utilities.ActivityInterface.DISPLAY_MESSAGE_ACTION);
                        intent.putExtra(Utilities.ActivityInterface.EXTRA_MESSAGE, data);
                        context.sendBroadcast(intent);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void stop() {
        if (datagramSocket != null) {
            datagramSocket.close();
        }
        isReceive = false;
        remoteAddress = null;
    }

    public void doSend(MsgData data) {
        doSend(remoteAddress, data);
    }

    public void doSend(final SocketAddress address, final MsgData data) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "发送消息 " + data.toLogString());
                try {
                    DatagramPacket datagramPacket = new DatagramPacket(data.toByte(), data.toByte().length, address);
                    datagramSocket.send(datagramPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
