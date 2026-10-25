package com.ruon2.android.net;

//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.util.EntityUtils;

import android.text.format.DateUtils;

import com.ruon2.android.model.NetworkResult;
import com.ruon2.android.util.UserLog;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Ivan on 6/26/2015.
 */
public class FcmRegisterWS extends NetworkTask {

    public static final String TAG = FcmRegisterWS.class.getSimpleName();
    String email;
    String password;
    String mName;
    String mToken;
    NetworkTaskListener mListener;
    NetworkResult result;
    private static final String UNKNOWN_ERROR = "Unknown error";

    public FcmRegisterWS(String email, String password, String name, String token, NetworkTaskListener listener) {
        this.email = email;
        this.password = password;
        this.mName = encode(name);
        this.mToken = token;
        mListener = listener;
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 is always supported", e);
        }
    }

//    @Override
//    protected Object doInBackground(Object[] objects) {
//        HttpClient httpClient = new DefaultHttpClient();
//        HttpParams httpParameters = httpClient.getParams();
//        HttpConnectionParams.setConnectionTimeout(httpParameters, (int) (20 * DateUtils.SECOND_IN_MILLIS));
//        HttpConnectionParams.setSoTimeout        (httpParameters, (int) (20 * DateUtils.SECOND_IN_MILLIS));
//        try {
//            HttpGet httpost = new HttpGet(String.format("https://www.r-u-on.com/androidNotif?email=%s&password=%s&name=%s&device=%s", email, password, mName, mToken));
//            UserLog.i(TAG, "New WS URL - " + httpost.getURI());
//            httpost.setHeader("Accept", "application/json");
//            HttpResponse httpResponse = httpClient.execute(httpost);
//            String line = EntityUtils.toString(httpResponse.getEntity());
//            UserLog.i(TAG, "Response - " + line);
//            UserLog.i(TAG, "Response code - " + httpResponse.getStatusLine().getStatusCode());
//            if(httpResponse.getStatusLine().getStatusCode() == 200){
//                result = NetworkResult.OK;
//            }else{
//                result = NetworkResult.ERROR;
//            }
//            return line;
//            // handle response here...
//        }catch (Exception ex) {
//            // handle exception here
//            ex.printStackTrace();
//            result = NetworkResult.ERROR;
//            return UNKNOWN_ERROR;
//        }
//    }

    @Override
    protected Object doInBackground() {
        HttpURLConnection urlConnection = null;
        try {
            // Construct the URL
            String urlString = String.format("https://www.r-u-on.com/androidNotif?email=%s&password=%s&name=%s&device=%s", email, password, mName,
                    mToken);
            URL url = new URL(urlString);
            UserLog.i(TAG, "New WS URL - " + urlString);

            // Open connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(20 * (int) DateUtils.SECOND_IN_MILLIS);
            urlConnection.setReadTimeout(20 * (int) DateUtils.SECOND_IN_MILLIS);
            urlConnection.setRequestProperty("Accept", "application/json");

            // Initiate the connection
            int responseCode = urlConnection.getResponseCode();
            UserLog.i(TAG, "Response code - " + responseCode);

            // Read the response
            InputStream inputStream;
            if (responseCode == 200) {
                inputStream = urlConnection.getInputStream();
                result = NetworkResult.OK;
            } else {
                inputStream = urlConnection.getErrorStream();
                result = NetworkResult.ERROR;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            String responseStr = response.toString();
            UserLog.i(TAG, "Response - " + responseStr);

            return responseStr;

        } catch (Exception e) {
            UserLog.e(TAG, "Exception in network call" + e.getMessage());
            result = NetworkResult.ERROR;
            return UNKNOWN_ERROR;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }


    @Override
    protected void onPostExecute(Object o) {
        if (mListener != null) {
            mListener.OnResult(result, o);
        }
    }
}
