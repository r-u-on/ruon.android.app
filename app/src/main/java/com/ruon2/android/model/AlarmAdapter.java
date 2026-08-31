package com.ruon2.android.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ruon2.android.util.TheAlarm;
import com.ruon2.app.R;
import com.ruon2.rssapi.Alarm;

import java.util.ArrayList;

/**
 * Created by Ivan on 6/26/2015.
 */
public class AlarmAdapter extends BaseAdapter {

    private ArrayList<TheAlarm> items = new ArrayList<>();

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        TheAlarm alarm = getItem(position);
        if (view == null) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.alarm_list_item, null, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.cause.setText(alarm.getResource());
        holder.title.setText(alarm.getAgent());
        holder.snippet.setText(alarm.getDescription());
        holder.updateBG(alarm.getSeverity());
        return view;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public TheAlarm getItem(int i) {
        return items.get(i);
    }

    public void swapData(ArrayList<TheAlarm> items) {
        this.items = items;
    }

    static class ViewHolder {
        TextView title;
        TextView cause;
        TextView snippet;
        LinearLayout root;

        public ViewHolder(View view) {
            title = view.findViewById(R.id.alarm_title);
            cause = view.findViewById(R.id.alarm_cause);
            snippet = view.findViewById(R.id.alarm_short_report);
            root = (LinearLayout) view;
        }

        public void updateBG(Alarm.Severity severity) {
            if (severity == null) {
                return;
            }
            switch (severity) {
                case Minor:
                    root.setBackgroundColor(root.getContext().getResources().getColor(R.color.app_minor));
                    break;
                case Major:
                    root.setBackgroundColor(root.getContext().getResources().getColor(R.color.app_major));
                    break;
                case Critical:
                    root.setBackgroundColor(root.getContext().getResources().getColor(R.color.app_critical));
                    break;
            }
        }
    }

}
