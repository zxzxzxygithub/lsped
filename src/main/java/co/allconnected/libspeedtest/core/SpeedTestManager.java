package co.allconnected.libspeedtest.core;

/**
 * Created by michael on 16/11/28.
 */

public interface SpeedTestManager {


    void  init();

    void startTest( String ip);

    void cancelTest();

    String getAvgSpeed();

    void showSpeed(OnDetectSpeedListener listener);


}
