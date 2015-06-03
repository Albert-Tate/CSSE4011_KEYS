package com.mikaljrue.tcptesting;

import android.location.Location;
import android.net.wifi.ScanResult;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

/**
 * Created by mikaljrue on 17/05/15.
 */
public class TCPClient {
    private SocketChannel socketChannel = null;
    private String TCP_SERVER_IP = /*"192.168.0.23";//*/"118.208.13.248";
    private int TCP_SERVER_PORT = 4011;
    private Thread thread;
    private Location loc;


    TCPClient()
    {

    }
    private void initThread()
    {

    }

    public Location getKeyLocationEstimate()
    {
        thread = new Thread(new Runnable() {
            public void run ()
            {
                loc = threadrun("request\n", true);
            }
        });
        thread.start();
        try {
            thread.join();
            return loc;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    public Location getMyLocationEstimate(final List<ScanResult> nodes)
    {

        thread = new Thread(new Runnable() {
            public void run ()
            {
                if (nodes != null) {
                    StringBuilder request = new StringBuilder("location_p ");
                    StringBuilder macList = new StringBuilder("");
                    StringBuilder channelList = new StringBuilder("");
                    StringBuilder rssiList = new StringBuilder("");
                    for (int i = 0; i < nodes.size(); i++) {
                        macList.append('\"').append(nodes.get(i).BSSID).append('\"');
                        channelList.append(nodes.get(i).frequency/5 - 481);
                        rssiList.append(nodes.get(i).level);
                        if (i < nodes.size() - 1) {
                            macList.append(',');
                            channelList.append(',');
                            rssiList.append(',');
                        }
                    }
                    loc = threadrun("location_p " + macList + ' ' + channelList + ' ' + rssiList + '\n', false);
                } else
                    loc = null;
            }
        });
        thread.start();
        try {
            thread.join();
            return loc;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    TCPClient(String ip, int port)
    {
        TCP_SERVER_IP = ip;
        TCP_SERVER_PORT = port;
    }
    private Location threadrun(String sendString, Boolean getTime) {
        Location curLoc = new Location("");
        try {
            if (socketChannel == null || !socketChannel.isOpen())
                socketChannel = SocketChannel.open();
            if (!socketChannel.isConnected())
                socketChannel.connect(new InetSocketAddress(TCP_SERVER_IP, TCP_SERVER_PORT));
            ByteBuffer inBuff = ByteBuffer.allocate(1024);
            ByteBuffer outBuf = ByteBuffer.allocate(1024);
            outBuf.clear();
            outBuf.put(sendString.getBytes());

            outBuf.flip();
            try {
                while(outBuf.hasRemaining()) {
                    socketChannel.write(outBuf);
                }
                int readbytes = socketChannel.read(inBuff);
                socketChannel.close();
                if (readbytes > 0)
                {
                    curLoc = new LocationParser(new String(inBuff.array()), getTime).getloc();
                    curLoc.setProvider(sendString);
                }
                return curLoc;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        curLoc.setProvider("returning Null");
        return curLoc;
    }



}
