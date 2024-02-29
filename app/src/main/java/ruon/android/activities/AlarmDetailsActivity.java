package ruon.android.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.ruon.app.R;
import com.ruon.app.databinding.AlarmDetailsActivityBinding;

import ruon.android.util.TheAlarm;
import ruon.android.util.UserLog;

/**
 * Created by Ivan on 6/29/2015.
 */
public class AlarmDetailsActivity extends AppCompatActivity {

    private static final String TAG = AlarmDetailsActivity.class.getSimpleName();

    private TheAlarm mAlarm;

    private AlarmDetailsActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AlarmDetailsActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getAlarmInfo();
        updateViews();
    }

    private void getAlarmInfo() {
        mAlarm = new Gson().fromJson(getIntent().getStringExtra(TheAlarm.TAG), TheAlarm.class);
        UserLog.i(TAG, "Alarm - " + mAlarm);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateValues();
    }

    private void updateValues() {
        if (mAlarm == null) {
            finish();
        } else {
            getSupportActionBar().setTitle(mAlarm.getAgent());
            binding.alarmSeverity.setText(mAlarm.getSeverity().toString());
            binding.alarmTimestamp.setText(mAlarm.getDate());
            binding.alarmAgent.setText(mAlarm.getResource());
            binding.alarmDetail.setText(mAlarm.getGuid() + "\n\n" + mAlarm.getDescription());
            switch (mAlarm.getSeverity()) {
                case Minor:
                    binding.severityContainer.setBackgroundColor(getResources().getColor(R.color.app_minor));
                    break;
                case Major:
                    binding.severityContainer.setBackgroundColor(getResources().getColor(R.color.app_major));
                    break;
                case Critical:
                    binding.severityContainer.setBackgroundColor(getResources().getColor(R.color.app_critical));
                    break;
            }
        }
    }

    private void updateViews() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
