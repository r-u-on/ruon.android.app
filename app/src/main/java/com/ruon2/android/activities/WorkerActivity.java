package com.ruon2.android.activities;

import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.ruon2.app.R;

/**
 * Created by Ivan on 6/26/2015.
 */
public abstract class WorkerActivity extends AppCompatActivity {

    public void showProgress() {
        LinearLayout progress = findViewById(R.id.progress_bar);
        if (progress != null) {
            progress.setVisibility(View.VISIBLE);
        }
    }

    public void hideProgress() {
        LinearLayout progress = findViewById(R.id.progress_bar);
        if (progress != null) {
            progress.setVisibility(View.GONE);
        }
    }
}
