package ruon.android.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
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
import com.ruon.app.R;

import java.util.ArrayList;

import ruon.android.model.AlarmAdapter;
import ruon.android.model.MyPreferenceManager;
import ruon.android.model.NetworkResult;
import ruon.android.net.AlarmsWS;
import ruon.android.net.NetworkTask;
import ruon.android.util.NetworkUtils;
import ruon.android.util.TheAlarm;
import ruon.android.util.UserLog;

public class MainActivity extends WorkerActivity implements NetworkTask.NetworkTaskListener, AdapterView.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener{

    private final String TAG = MainActivity.class.getSimpleName();
    public static final String NOTIFICATION_EVENT = "com.ruon.app.NotificationEvent";

    private ListView mList;
    private NetworkTask mTask;
    private AlarmAdapter mAdapter;
    private TextView mNoAlarmsLabel;
    private SwipeRefreshLayout mRefresher;
    private boolean mShouldRefresh = true;
    private BroadcastReceiver receiver;
    private IntentFilter filter;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    UserLog.i(TAG, "notifications permission granted");
                } else {
                    UserLog.i(TAG, "notifications permission not granted");
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        updateViews();
        createReceiver();
        askNotificationPermission();
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void createReceiver() {
        filter = new IntentFilter();
        filter.addAction(NOTIFICATION_EVENT);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                UserLog.i(TAG, "OnNotificationRefresh");
                if(NetworkUtils.isNetworkAvailable(MainActivity.this)) {
                    refresh();
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(receiver, filter);
        }
        if(mShouldRefresh){
            showProgress();
            mList.setAdapter(null);
            refresh();
            mShouldRefresh = false;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Intent alarmDetails = new Intent(this, AlarmDetailsActivity.class);
        TheAlarm alarm = getAdapter().getItem(position);
        UserLog.i(TAG, "OnAlarm click - " + alarm);
        alarmDetails.putExtra(TheAlarm.TAG, new Gson().toJson(alarm));
        startActivityForResult(alarmDetails, 11);
    }

    private void refresh(){
        if(NetworkUtils.isNetworkAvailable(this)){
            mTask = new AlarmsWS(MyPreferenceManager.getToken(this), this);
            mTask.execute();
            mNoAlarmsLabel.setVisibility(View.GONE);
        }else{
            mList.setAdapter(null);
            mNoAlarmsLabel.setText(getString(R.string.network_error));
            mNoAlarmsLabel.setBackgroundColor(getResources().getColor(R.color.app_critical));
            mNoAlarmsLabel.setVisibility(View.VISIBLE);
            hideProgress();
        }
    }

    private void updateViews() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        getSupportActionBar().setIcon(R.drawable.logo_padding);
        mRefresher = (SwipeRefreshLayout) findViewById(R.id.refresher);
        mRefresher.setOnRefreshListener(this);
        mList = (ListView) findViewById(R.id.mList);
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
        mNoAlarmsLabel = (TextView) findViewById(R.id.warning_label);
    }

    @Override
    protected void onPause() {
        if(mTask != null){
            hideProgress();
            mTask.cancel(true);
        }
        mShouldRefresh = true;
        unregisterReceiver(receiver);
        super.onPause();
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
        if(result == NetworkResult.OK) {
            if (o != null) {
                ArrayList<TheAlarm> items = (ArrayList<TheAlarm>) o;
                UserLog.i(TAG, "result - " + items.size());
                if (items.size() == 0) {
                    // if(true){
                    mNoAlarmsLabel.setText(getString(R.string.no_alarms_title));
                    mNoAlarmsLabel.setBackgroundColor(getResources().getColor(R.color.app_green));
                    mNoAlarmsLabel.setVisibility(View.VISIBLE);
                } else {
                    getAdapter().swapData(items);
                    getAdapter().notifyDataSetChanged();
                    mList.setAdapter(getAdapter());
                }
            }
        }else{
            String message = (String)o;
            UserLog.i(TAG, "Error message - " + message);
            mNoAlarmsLabel.setText(message);
            mNoAlarmsLabel.setBackgroundColor(getResources().getColor(R.color.app_critical));
            mNoAlarmsLabel.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mShouldRefresh = false;
    }

    private AlarmAdapter getAdapter(){
        if(mAdapter == null){
            mAdapter = new AlarmAdapter();
        }
        return mAdapter;
    }

    @Override
    public void onRefresh() {
        mRefresher.setRefreshing(true);
        refresh();
    }
}
