package com.ruon2.android.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.ruon2.android.model.AlarmAdapter;
import com.ruon2.android.model.MyPreferenceManager;
import com.ruon2.android.model.NetworkResult;
import com.ruon2.android.model.NotificationPermissionManager;
import com.ruon2.android.model.WindowInsetsManager;
import com.ruon2.android.net.AlarmsWS;
import com.ruon2.android.net.NetworkTask;
import com.ruon2.android.util.NetworkUtils;
import com.ruon2.android.util.TheAlarm;
import com.ruon2.android.util.UserLog;
import com.ruon2.app.R;
import com.ruon2.app.databinding.ActivityMainBinding;

import java.util.List;

public class MainActivity
        extends WorkerActivity
        implements NetworkTask.NetworkTaskListener,
        AdapterView.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener {

    private final String TAG = MainActivity.class.getSimpleName();
    public static final String NOTIFICATION_EVENT = "com.ruon2.app.NotificationEvent";

    public ActivityMainBinding binding;
    private ListView mList;
    private NetworkTask mTask;
    private AlarmAdapter mAdapter;
    private TextView mNoAlarmsLabel;
    private SwipeRefreshLayout mRefresher;
    private boolean mShouldRefresh = true;
    private BroadcastReceiver receiver;
    private IntentFilter filter;
    private NotificationPermissionManager notificationPermissionManager
            = new NotificationPermissionManager(getActivityResultRegistry());
    // Coming back from alarm details must not trigger a refresh in onResume().
    private final ActivityResultLauncher<Intent> alarmDetailsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> mShouldRefresh = false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        new WindowInsetsManager().handleWindowInsets(this, binding.container);

        updateViews();
        createReceiver();
        checkNotificationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Internal-only broadcast: always register as NOT_EXPORTED. ContextCompat
        // applies the correct flag across all API levels (required for target SDK 36).
        ContextCompat.registerReceiver(this, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);

        if (mShouldRefresh) {
            showProgress();
            mList.setAdapter(null);
            refresh();
            mShouldRefresh = false;
        }
    }

    @Override
    public void onRefresh() {
        mRefresher.setRefreshing(true);
        refresh();
    }

    @Override
    protected void onPause() {
        if (mTask != null) {
            hideProgress();
            mTask.cancel();
        }
        mShouldRefresh = true;
        unregisterReceiver(receiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Intent alarmDetails = new Intent(this, AlarmDetailsActivity.class);
        TheAlarm alarm = getAdapter().getItem(position);
        UserLog.i(TAG, "OnAlarm click - " + alarm);
        alarmDetails.putExtra(TheAlarm.TAG, new Gson().toJson(alarm));
        alarmDetailsLauncher.launch(alarmDetails);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            UserLog.i(TAG, "refresh");
            refresh();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void OnResult(NetworkResult result, Object o) {
        mRefresher.setRefreshing(false);
        hideProgress();
        if (result == NetworkResult.OK) {
            if (o != null) {
                // AlarmsWS delivers the List<TheAlarm> built by TheAlarm.convert().
                @SuppressWarnings("unchecked")
                List<TheAlarm> items = (List<TheAlarm>) o;
                UserLog.i(TAG, "result - " + items.size());
                if (items.size() == 0) {
                    // if(true){
                    mNoAlarmsLabel.setText(getString(R.string.no_alarms_title));
                    mNoAlarmsLabel.setBackgroundColor(ContextCompat.getColor(this, R.color.app_green));
                    mNoAlarmsLabel.setVisibility(View.VISIBLE);
                } else {
                    getAdapter().swapData(items);
                    getAdapter().notifyDataSetChanged();
                    mList.setAdapter(getAdapter());
                }
            }
        } else {
            String message = (String) o;
            UserLog.i(TAG, "Error message - " + message);
            mNoAlarmsLabel.setText(message);
            mNoAlarmsLabel.setBackgroundColor(ContextCompat.getColor(this, R.color.app_critical));
            mNoAlarmsLabel.setVisibility(View.VISIBLE);
        }
    }

    private void checkNotificationPermission() {
        notificationPermissionManager.handleNotificationPermission(this);
    }

    private void createReceiver() {
        filter = new IntentFilter();
        filter.addAction(NOTIFICATION_EVENT);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                UserLog.i(TAG, "OnNotificationRefresh");
                if (NetworkUtils.isNetworkAvailable(MainActivity.this)) {
                    refresh();
                }
            }
        };
    }

    private void refresh() {
        if (NetworkUtils.isNetworkAvailable(this)) {
            mTask = new AlarmsWS(MyPreferenceManager.getToken(this), this);
            mTask.execute();
            mNoAlarmsLabel.setVisibility(View.GONE);
        } else {
            mList.setAdapter(null);
            mNoAlarmsLabel.setText(getString(R.string.network_error));
            mNoAlarmsLabel.setBackgroundColor(ContextCompat.getColor(this, R.color.app_critical));
            mNoAlarmsLabel.setVisibility(View.VISIBLE);
            hideProgress();
        }
    }

    private void updateViews() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        getSupportActionBar().setIcon(R.drawable.logo_padding);
        mRefresher = findViewById(R.id.refresher);
        mRefresher.setOnRefreshListener(this);
        mList = findViewById(R.id.mList);
        mList.setAdapter(getAdapter());
        mList.setOnItemClickListener(this);
        mList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                    int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition =
                        (mList == null || mList.getChildCount() == 0) ?
                                0 : mList.getChildAt(0).getTop();
                mRefresher.setEnabled(firstVisibleItem == 0 &&
                        topRowVerticalPosition >= 0);
            }
        });
        mNoAlarmsLabel = findViewById(R.id.warning_label);
    }

    private AlarmAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new AlarmAdapter();
        }
        return mAdapter;
    }
}
