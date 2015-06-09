//Albert Tate, 9 June 2015

public class directionIdentifier {
    int[] READINGS = new int[361];
    private int READ_COUNT;
    
    public directionIdentifier() {
        READ_COUNT = 0;
        for (int i = 0; i < 361; i++) {
            READINGS[i] = 0;
        }
    }
    
    public void update(int RSSI, int Azimuth) {
         READINGS[Azimuth+180] = RSSI;
         READ_COUNT++;
    }
    public void clear() {
        for(int i = 0; i < 361; i++) {
            READINGS[i] = 0;
        }
    }
    
    public int FindAngle() {
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
