package co.allconnected.libspeedtest.core;

import android.os.SystemClock;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadTask implements Runnable {


    private OnDetectSpeedListener mListener;
    private String mUrl;
    private URL downloadUrl;
    private InputStream is;
    private HttpURLConnection connection;
    private boolean stopDownload = false;

    public DownloadTask(String url, OnDetectSpeedListener listener) {
        mUrl = url;
        mListener = listener;
    }

    @Override
    public void run() {
        if (!startDefaultDownLoad() && !stopDownload) {
            mListener.onError(mUrl);
        }
    }

    private boolean startDefaultDownLoad() {
        try {
            downloadUrl = new URL(mUrl);
        } catch (MalformedURLException e) {
            this.mListener.onError(mUrl);
        }
        return startDown();
    }

    private boolean startDown() {
        for (int retry = 3; retry > 0 && !stopDownload; --retry) {
            try {
                connect();
                down();
                return true;
            } catch (Exception e) {
                SystemClock.sleep(100);
            }
        }
        return false;
    }

    public void disconnect() {
        stopDownload = true;
        try {
            if (connection != null) {
                connection.disconnect();
            }
            if (is != null) {
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connect() throws IOException, IllegalArgumentException {
        connection = (HttpURLConnection) downloadUrl.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(false);
        connection.setConnectTimeout(5000);
        connection.setRequestProperty("Accept-Encoding", "identity");
        connection.setRequestProperty(
                "User-Agent",
                "Mozilla/4.0(compatible;MSIE7.0;Windows NT 5.2;Trident/4.0;.NET CLR 1.1.4322;.NET CLR 2.0.50727;.NET CLR 3.0.04506.30;.NET CLR 3.0.4506.2152;.NET CLR 3.5.30729)");
        connection.connect();
        is = connection.getInputStream();
    }

    private void down() throws Exception {
        synchronized (this) {
            byte[] buffer = new byte[1024];
            while (is != null && (is.read(buffer, 0, 1024)) != -1 && !stopDownload) {
            }
            if (is != null) {
                is.close();
            }

        }

    }

}
