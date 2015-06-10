package com.mikaljrue.tcptesting;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by mikaljrue on 8/06/2015.
 */
public class UDPClient {

    private DatagramSocket socketChannel = null;
    private String UDP_SERVER_IP = /*"192.168.0.23";//*/"118.208.13.248";
    private int UDP_SERVER_PORT = 4011;
    private Thread thread;

    public UDPClient() {

    }

    public void sendHeartbeat(String address)
    {
        UDP_SERVER_IP = address;
        thread = new Thread(new Runnable() {
            public void run ()
            {
                threadRun("0000000\n");
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void threadRun(String sendString) {
        try {
            if (socketChannel == null) {
                socketChannel = new DatagramSocket();
                socketChannel.connect(new InetSocketAddress(UDP_SERVER_IP, UDP_SERVER_PORT));
                socketChannel.setSoTimeout(500);
                Log.v("UDP", "Connected");
            }
            byte[] barray = sendString.getBytes();
            DatagramPacket outpack = new DatagramPacket(barray,barray.length);
            DatagramPacket inpack = new DatagramPacket(new byte[32],32);

            try {
                socketChannel.send(outpack);

                socketChannel.receive(inpack);
                if (inpack.getLength() > 0) {

                    Log.v("UDP ShortDistClient: ", new String(inpack.getData()).substring(0,inpack.getLength()));
                }

            } catch (IOException e) {
                e.printStackTrace();
                Log.v("sockets: ", "Connection Lost");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
