package co.allconnected.libspeedtest.core;

public interface OnDetectSpeedListener {

    void onStart();

    void onFinished(float speed);

    /**
     * @param percent 测速进度，区间为：0-100
     * @param speed   瞬时速度，单位（byte/s）
     */
    void onProgress(float percent, float speed);

    void onError(String url);

    void getDownLoadSpeed(long downSpeed);

    void getUpLoadSpeed(long upSpeed);


}