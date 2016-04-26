package com.metek.liveavatar.socket;

import android.util.Log;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioDatagramConnector;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class UDPManager {
    private static final String TAG = UDPManager.class.getSimpleName();

    private static UDPManager manager;
    private ExecutorService executor;
    private NioDatagramConnector connector;
    private ConnectFuture future;
    private ConnectListener listener;

    private UDPManager() {
        executor = Executors.newFixedThreadPool(1);
        connector = new NioDatagramConnector();
        connector.setConnectTimeoutMillis(5000);
        connector.getSessionConfig().setBothIdleTime(120);
        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new CodecFactory()));
        connector.setHandler(new ConnectHandler());
    }

    public synchronized static UDPManager getManager() {
        if (manager == null) {
            manager = new UDPManager();
        }
        return manager;
    }

    public void setConnectListener(ConnectListener listener) {
        this.listener = listener;
    }

    public void connect() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isConnected()) {
                        return;
                    }
                    InetSocketAddress remoteSocketAddress = new InetSocketAddress(NetConst.Host, NetConst.UdpPort);
                    InetSocketAddress localSocketAddress = new InetSocketAddress(NetUtils.getLocalIPAddress(), 3000);
                    future = connector.connect(remoteSocketAddress, localSocketAddress);
                    future.awaitUninterruptibly();
                    future.getSession();
                } catch (Exception e) {
                    e.printStackTrace();
                    listener.onConnect(ConnectListener.CONN_ERR, null);
                }
            }
        });
    }

    public void send(final MsgData data) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                IoSession session = getCurrentSession();
                if (session != null && session.isConnected()) {
                    WriteFuture writeFuture = session.write(data);
                    writeFuture.awaitUninterruptibly(5, TimeUnit.SECONDS);
                    if (!writeFuture.isWritten()) {
                        listener.onConnect(ConnectListener.SENT_FAILED, null);
                    } else {
                        listener.onConnect(ConnectListener.SENT_FAILED, null);
                    }
                }
            }
        });

    }

    public boolean isConnected() {
        IoSession session = getCurrentSession();
        return session != null && session.isConnected();
    }

    public void closeSession() {
        IoSession session = getCurrentSession();
        if (session != null) {
            session.close(true);
        }
    }

    public IoSession getCurrentSession() {
        if (connector.getManagedSessionCount() > 0) {
            //noinspection LoopStatementThatDoesntLoop
            for (Long key : connector.getManagedSessions().keySet()) {
                return connector.getManagedSessions().get(key);
            }
        }
        return null;
    }

    public void destroy() {
        closeSession();
        if (connector != null && !connector.isDisposed()) {
            connector.dispose();
        }
        manager = null;
    }

    public interface ConnectListener {
        int CONN_ERR = -1;
        int SENT_FAILED = -2;
        int CONN_OK = 0;

        void onConnect(int state, MsgData data);
    }

    private class ConnectHandler implements IoHandler {
        @Override
        public void sessionCreated(IoSession ioSession) throws Exception {
            Log.v(TAG, "创建连接 sessionCreated: " + ioSession.getLocalAddress());
        }

        @Override
        public void sessionOpened(IoSession ioSession) throws Exception {
            Log.v(TAG, "连接开启 sessionOpened: " + ioSession.getLocalAddress());
            connector.getSessionConfig().setBothIdleTime(180);
        }

        @Override
        public void sessionClosed(IoSession ioSession) throws Exception {
            Log.v(TAG, "断开连接 sessionClosed: " + ioSession.getLocalAddress());
        }

        @Override
        public void sessionIdle(IoSession ioSession, IdleStatus idleStatus) throws Exception {
            Log.v(TAG, "连接空闲 sessionIdle: " + ioSession.getLocalAddress());
        }

        @Override
        public void exceptionCaught(IoSession ioSession, Throwable throwable) throws Exception {
            Log.e(TAG, "连接异常 exceptionCaught: " + throwable);
            listener.onConnect(ConnectListener.CONN_ERR, null);
            ioSession.close(true);
        }

        @Override
        public void messageReceived(IoSession ioSession, Object object) throws Exception {
            MsgData data = (MsgData) object;
            listener.onConnect(ConnectListener.CONN_OK, data);
            Log.i(TAG, "收到数据: " + data.toLogString() + " " + ioSession.getLocalAddress());
        }

        @Override
        public void messageSent(IoSession ioSession, Object object) throws Exception {
            MsgData data = (MsgData) object;
            Log.i(TAG, "发送数据: " + data.toLogString() + " " + ioSession.getLocalAddress());
        }
    }
}
