package com.ruon2.android.net;

import android.os.AsyncTask;

import com.ruon2.android.model.NetworkResult;

/**
 * Created by Ivan on 6/26/2015.
 */
public abstract class NetworkTask extends AsyncTask {

    @Override
    protected Object doInBackground(Object[] objects) {
        return null;
    }

    public interface NetworkTaskListener {
        void OnResult(NetworkResult result, Object o);
    }

}
