package com.ruon2.android.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.os.Looper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Robolectric tests for {@link NetworkTask}: background execution, main-thread delivery
 * and cancellation.
 */
@RunWith(RobolectricTestRunner.class)
public class NetworkTaskRobolectricTest {

    @Test
    public void runsInBackground_andDeliversResultOnMainThread() throws Exception {
        Thread mainThread = Thread.currentThread();
        AtomicReference<Thread> backgroundThread = new AtomicReference<>();
        AtomicReference<Thread> postThread = new AtomicReference<>();
        AtomicReference<Object> delivered = new AtomicReference<>();
        CountDownLatch finished = new CountDownLatch(1);

        NetworkTask task = new NetworkTask() {
            @Override
            protected Object doInBackground() {
                backgroundThread.set(Thread.currentThread());
                return "result";
            }

            @Override
            protected void onPostExecute(Object result) {
                postThread.set(Thread.currentThread());
                delivered.set(result);
                finished.countDown();
            }
        };
        task.execute();

        idleMainLooperUntil(finished);
        assertEquals("result", delivered.get());
        assertNotSame(mainThread, backgroundThread.get());
        assertSame(mainThread, postThread.get());
    }

    @Test
    public void cancel_skipsOnPostExecute() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch backgroundDone = new CountDownLatch(1);
        AtomicReference<Object> delivered = new AtomicReference<>();

        NetworkTask task = new NetworkTask() {
            @Override
            protected Object doInBackground() {
                started.countDown();
                try {
                    release.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException ignored) {
                    // cancel() interrupts the worker, like AsyncTask.cancel(true)
                }
                backgroundDone.countDown();
                return "result";
            }

            @Override
            protected void onPostExecute(Object result) {
                delivered.set(result);
            }
        };
        task.execute();
        assertTrue(started.await(5, TimeUnit.SECONDS));

        task.cancel();
        release.countDown();
        assertTrue(backgroundDone.await(5, TimeUnit.SECONDS));
        // Let any result the worker posted run before checking it was dropped.
        Thread.sleep(100);
        shadowOf(Looper.getMainLooper()).idle();

        assertTrue(task.isCancelled());
        assertNull(delivered.get());
    }

    @Test
    public void isCancelled_isFalseUntilCancelled() {
        NetworkTask task = new NetworkTask() {
            @Override
            protected Object doInBackground() {
                return null;
            }
        };
        assertFalse(task.isCancelled());
        task.cancel();
        assertTrue(task.isCancelled());
    }

    private static void idleMainLooperUntil(CountDownLatch latch) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5_000;
        while (latch.getCount() > 0 && System.currentTimeMillis() < deadline) {
            shadowOf(Looper.getMainLooper()).idle();
            Thread.sleep(10);
        }
        assertEquals("onPostExecute was not delivered", 0, latch.getCount());
    }
}
