package com.ruon2.android.net;

import android.os.Handler;
import android.os.Looper;

import com.ruon2.android.model.NetworkResult;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Runs a blocking network call off the main thread and delivers its result back on the
 * main thread. Replaces the deprecated {@code AsyncTask} with the same lifecycle:
 * {@link #onPreExecute()} on the caller's thread, {@link #doInBackground()} on a worker
 * thread, then {@link #onPostExecute(Object)} on the main thread unless {@link #cancel()}
 * was called first.
 *
 * <p>Tasks run one at a time on a single background thread, matching AsyncTask's default
 * serial executor.
 *
 * Created by Ivan on 6/26/2015.
 */
public abstract class NetworkTask {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "ruon-network");
        thread.setDaemon(true);
        return thread;
    });
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private volatile boolean mCancelled;
    private Future<?> mFuture;

    public interface NetworkTaskListener {
        void OnResult(NetworkResult result, Object o);
    }

    /**
     * Starts the task. Must be called on the main thread.
     */
    public final void execute() {
        onPreExecute();
        mFuture = EXECUTOR.submit(() -> {
            if (mCancelled) {
                return;
            }
            Object result;
            try {
                result = doInBackground();
            } catch (RuntimeException e) {
                // Surface bugs as a crash, like AsyncTask did, instead of losing them in the Future.
                MAIN_HANDLER.post(() -> {
                    throw e;
                });
                return;
            }
            MAIN_HANDLER.post(() -> {
                if (!mCancelled) {
                    onPostExecute(result);
                }
            });
        });
    }

    /**
     * Cancels the task. {@link #onPostExecute(Object)} will not be called afterwards, and a
     * running {@link #doInBackground()} is interrupted.
     */
    public final void cancel() {
        mCancelled = true;
        if (mFuture != null) {
            mFuture.cancel(true);
        }
    }

    public final boolean isCancelled() {
        return mCancelled;
    }

    protected void onPreExecute() {
    }

    protected abstract Object doInBackground();

    protected void onPostExecute(Object result) {
    }
}
