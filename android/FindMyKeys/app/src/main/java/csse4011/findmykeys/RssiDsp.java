package com.mikaljrue.tcptesting;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by mikaljrue on 8/06/2015.

public class RssiDsp {
    TreeMap<Integer, Double> map;
    public RssiDsp()
    {
        map = new TreeMap<>();
    }
    public void add(int azimuth, int rssi)
    {
        if (map.containsKey(azimuth))
            map.put(azimuth, rssi/2 + map.get(azimuth)/2);
        else
            map.put(azimuth, (double)rssi);

    }
    public double process()
    {
        TreeMap<Double, Integer> smaData = processSMA(map, 60);

        return smaData.lastEntry().getValue();
    }
    private static int getCircularDiff(int first, int second) {
        if (first <= second) {
            return second - first;
        } else {
            return 360 - first + second;
        }
    }

    public static TreeMap<Double,Integer> processSMA(TreeMap<Integer,Double> sourceMap, int windowSize){
        TreeMap<Double, Integer> outMap = new TreeMap<>();
        Map.Entry<Integer, Double> earlyEntry = sourceMap.firstEntry();
        Map.Entry<Integer, Double> lateEntry = sourceMap.firstEntry();
        boolean finished = false;
        do {
            if (getCircularDiff(earlyEntry.getKey(), lateEntry.getKey()) > windowSize) {
                Map.Entry<Integer, Double> curEntry = earlyEntry;

                double average = 0;
                int count = 0;
                while (curEntry.getKey() != lateEntry.getKey()) {
                    average += curEntry.getValue();
                    count++;
                    curEntry = sourceMap.higherEntry(curEntry.getKey());
                    if (curEntry == null) curEntry = sourceMap.firstEntry();
                }
                if (count == 0)
                    average = average/count;
                else
                    average = 0;
                outMap.put(average, earlyEntry.getKey());

                earlyEntry = sourceMap.higherEntry(earlyEntry.getKey());
                if (earlyEntry == null) earlyEntry = sourceMap.firstEntry();
                if (earlyEntry.getKey() == sourceMap.firstEntry().getKey()) { finished = true; }

            } else {
                lateEntry = sourceMap.higherEntry(lateEntry.getKey());
                if (lateEntry == null) lateEntry = sourceMap.firstEntry();
            }
        } while (!finished);
        return outMap;
    }
}
*/
//Albert Tate, 9 June 2015

public class RssiDsp {
    int[] READINGS = new int[361];
    private int READ_COUNT;

    public RssiDsp() {
        READ_COUNT = 0;
        for (int i = 0; i < 361; i++) {
            READINGS[i] = 0;
        }
    }

    public void add(int RSSI, int Azimuth) {
        READINGS[Azimuth+180] = RSSI;
        READ_COUNT++;
    }

    public int process() {
        int[][] B_DATA = new int[READ_COUNT][2]; //Angle, RSSI
        int[][] SMA = new int[READ_COUNT][2]; //Angle, RSSI
        int j = 0;
        //Sort
        for (int i = 0; i < 361; i++) {
            if(READINGS[i] != 0) {
                B_DATA[j][0] = i;
                B_DATA[j][1] = READINGS[i];
                j++;
            }
        }
        //Calculate SMA
        int SMA_WIDTH = 7; //User Var
        int SMA_VAL = 0;
        for(int i = SMA_WIDTH; i < READ_COUNT; i++) {//Maybe start at SMA_WIDTH?
            for(int k = 0; k < SMA_WIDTH; k++) {
                SMA_VAL += B_DATA[i - k][1];
            }
            SMA_VAL = SMA_VAL/SMA_WIDTH;
            SMA[i][0] = B_DATA[i][0];
            SMA[i][1] = SMA_VAL;
            SMA_VAL = 0;
        }
        //Find max
        int cur_max = -200;
        int cur_ang = 0;
        for(int i = 0; i < READ_COUNT; i++) {
            if(SMA[i][1] > cur_max) {
                cur_max = SMA[i][1];
                cur_ang = SMA[i][0];
            }
        }

        return cur_ang - 180;

    }

}
