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

    private static int getCircularDiff(int first, int second) {
        if (first <= second) {
            return second - first;
        } else {
            return 360 - first + second;
        }
    }

    public void add(int Azimuth, int RSSI) {
        if(READINGS[Azimuth + 180] == 0) {
            READ_COUNT++;
        }
        READINGS[Azimuth+180] = RSSI;
    }

    public int process() {
        int[][] B_DATA = new int[READ_COUNT][2]; //Angle, RSSI
        int[][] SMA = new int[READ_COUNT][2]; //Angle, RSSI
        int j = 0;
        int lastAngle = 0;
        //Sort
        for (int i = 0; i < 361; i++) {
            if(READINGS[i] != 0) {
                B_DATA[j][0] = i;
                B_DATA[j][1] = READINGS[i];
                if (getCircularDiff(lastAngle, i) > 100)
                    return -181;
                lastAngle = i;
                j++;
            }
        }
        //Calculate SMA
        int SMA_WIDTH = 6; //User Var
        int SMA_VAL = 0;
        if (READ_COUNT <= SMA_WIDTH) return -181;
        for(int i = SMA_WIDTH-1; i < READ_COUNT; i++) {//Maybe start at SMA_WIDTH?
            for(int k = 0; k < SMA_WIDTH; k++) {
                SMA_VAL += B_DATA[i - k][1];
            }
            SMA_VAL = SMA_VAL/SMA_WIDTH;
            SMA[i][0] = B_DATA[i - SMA_WIDTH/2][0];
            SMA[i][1] = SMA_VAL;
            SMA_VAL = 0;
        }
        //Find max
        int cur_max = -200;
        int cur_ang = 0;
        for(int i = 0; i < READ_COUNT; i++) {
            if( (SMA[i][1] > cur_max) && (SMA[i][1] != 0)) {
                cur_max = SMA[i][1];
                cur_ang = SMA[i][0];
            }
        }

        return addAngle(cur_ang - 180, 90); //Offset from data rep + antenna directivity offset

    }

    static int addAngle(int angle1, int angle2) {
        int retVal = angle1+angle2;
        if (retVal < -180) return retVal +360;
        else if (retVal > 180) return retVal - 360;
        return retVal;
    }

}
