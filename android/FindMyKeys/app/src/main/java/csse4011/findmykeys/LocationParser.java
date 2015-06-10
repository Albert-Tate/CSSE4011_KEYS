package csse4011.findmykeys;

import android.location.Location;

import java.util.Scanner;

/**
 * Created by mikaljrue on 20/05/15.
 */
public class LocationParser {
    private Location loc;

    LocationParser (String locString, Boolean hasTime) {
        loc = new Location(locString);
        Scanner scan = new Scanner(locString);
        scan.useDelimiter(",\\s");
        scan.skip("\\[");
        loc.setLatitude(Double.parseDouble(scan.next()));
        loc.setLongitude(Double.parseDouble(scan.next()));
        if (hasTime) {
            loc.setAccuracy((float) Double.parseDouble(scan.next()));
            String time = scan.useDelimiter(",\\s|\\]").next();
            loc.setTime((long) Double.parseDouble(time));
        } else {
            scan.useDelimiter(",\\s|\\]");
            loc.setAccuracy((float) Double.parseDouble(scan.next()));
        }
    }
    public Location getloc() {
        return loc;
    }
}
