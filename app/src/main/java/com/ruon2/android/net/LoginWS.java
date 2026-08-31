package com.ruon2.android.net;

import com.ruon2.android.model.NetworkResult;
import com.ruon2.android.util.UserLog;
import com.ruon2.rssapi.API;

/**
 * Created by Ivan on 6/26/2015.
 */
public class LoginWS extends NetworkTask {

    private static final String UNKNOWN_ERROR = "Unknown error";
    public static final String TAG = LoginWS.class.getSimpleName();

    String email;
    String password;
    String rawResult;
    NetworkTaskListener mListener;
    NetworkResult result;

    public LoginWS(String email, String password, NetworkTaskListener listener) {
        this.email = email;
        this.password = password;
        mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        rawResult = UNKNOWN_ERROR;
        API.authenticate(email, password, new API.AuthenticateListener() {

            @Override
            public void error(Exception e) {
                // Handle authentication error
                System.err.println(e);
                result = NetworkResult.ERROR;
                rawResult = e.getMessage();
            }

            @Override
            protected void authenticated(String guid) {
                // On successful authentication, you might want to
                // save the guid in a secure place for future executions.
                result = NetworkResult.OK;
                rawResult = guid;
            }
        });
        UserLog.i(TAG, "result - " + result);

        return rawResult;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        if (!isCancelled()) {
            mListener.OnResult(result, o);
        }
    }
}
