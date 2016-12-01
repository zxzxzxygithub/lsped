package co.allconnected.libspeedtest.core;

/**
 * Created by michael on 16/11/28.
 */

public interface SpeedTestManager {


    void startTest(OnDetectSpeedListener listener);

    void cancelTest();

    String getAvgSpeed();


}
