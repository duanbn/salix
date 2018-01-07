package org.duanbn.salix.network.util;

public class Sleep {

    public static void doSleep(long time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
        }
    }

}
