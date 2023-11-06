package com.mcnc.bizmob.util;


import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.TypedValue;
import android.view.Window;
import android.view.WindowManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author phanha_uy
 * @version 2017-02-13
 */
public class Utils {

    /**
     * Get Pixel of DPI, SPI, DP
     *
     * @param context the context for getting resource {@Link context.getResources.getDisplayMetrics}
     * @param dp      the size of dpi, spi, dp to convert to pixels
     * @return {float} of pixels
     */
    public static float getPixelOfDpi(Context context, int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    /**
     * Convert from one date format to another
     * <p>
     * {@link SimpleDateFormat}
     *
     * @param dateTimeString the date time string that to be converted
     * @param fromFormat     the original format of {@Link dateTimeString}
     */
    public static String convertDateFormat(String dateTimeString, String fromFormat, String toFormat) {
        if (dateTimeString == null) {
            return null;
        }

        String dateString = dateTimeString;
        try {
            DateFormat format = new SimpleDateFormat(fromFormat);
            Date date = format.parse(dateTimeString);

            format = new SimpleDateFormat(toFormat);
            dateString = format.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateString;
    }


    /**
     * Prevent the screen to be captured
     *
     * @param activity {@link Activity} the activity to prevent
     */
    public static void preventScreenShot(Activity activity) {
        if (activity != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
            } else {
                activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
//                Window window = activity.getWindow();
//                WindowManager wm = activity.getWindowManager();
//                wm.removeViewImmediate(window.getDecorView());
//                wm.addView(window.getDecorView(), window.getAttributes());
            }
        }
    }

    /**
     * Allow Screen Shot after being prevented
     *
     * @param activity {@link Activity} the activity to allow
     */
    public static void allowScreenShot(Activity activity) {
        if (activity != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
            } else {
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
            }
        }
    }

    /**
     * Check whether if the device is connected to proxy server or not
     * <p>
     * if connected, @return true
     * else @return false
     *
     * @see #getProxyDetails()
     */
    public static boolean isConnectedProxy(Context context) {
        String proxyDetails = getProxyDetails();
        if (proxyDetails == null || proxyDetails.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * Get Proxy Details
     * <p>
     * if return empty text, it means it's not connected to proxy
     * else, it's connected to proxy server
     *
     * @return object of {@link String} proxy ip : proxy port
     */
    public static String getProxyDetails() {
        String proxyAddress = new String();
        try {
            proxyAddress = System.getProperty("http.proxyHost");
            if (proxyAddress == null || proxyAddress.isEmpty()) {
                return proxyAddress;
            }
            proxyAddress += ":" + System.getProperty("http.proxyPort");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return proxyAddress;
    }

    /**
     * Allow to access untrusted certificates
     */
//    public static void allowToAccessUntrustedCertificate() {
//        TrustManager[] trustManagers = new TrustManager[]{
//                new X509TrustManager() {
//                    @Override
//                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//                    }
//
//                    @Override
//                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//                    }
//
//                    @Override
//                    public X509Certificate[] getAcceptedIssuers() {
//                        return null;
//                    }
//                }
//        };
//
//        // Install the all-trusting trust manager
//        try {
//            SSLContext sc = SSLContext.getInstance("SSL");
//            sc.init(null, trustManagers, new java.security.SecureRandom());
//            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
//        } catch (Exception e) {
//        }
//
//        // Do not do this in production!!!
//        HostnameVerifier hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
//
//        SchemeRegistry registry = new SchemeRegistry();
//        SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
//        socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
//        registry.register(new Scheme("https", socketFactory, 443));
//        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
//    }
    public static void setIndicatorBgColor(Activity activity, int colorResourceId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(colorResourceId);
        }
    }

}
