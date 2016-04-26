package com.metek.liveavatar.socket;

import android.support.annotation.Nullable;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class Utils {

    /**
     * 获取本机设备的IP
     * @return 返回数字IP地址（如"127.0.0.1"），否则返回null
     */
    @Nullable
    public static String getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> eni = NetworkInterface.getNetworkInterfaces(); eni.hasMoreElements(); ) {
                NetworkInterface ni = eni.nextElement();
                for (Enumeration<InetAddress> eia = ni.getInetAddresses(); eia.hasMoreElements(); ) {
                    InetAddress ia = eia.nextElement();
                    if (!ia.isLoopbackAddress() && (ia instanceof Inet4Address)) {
                        return ia.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }
}
