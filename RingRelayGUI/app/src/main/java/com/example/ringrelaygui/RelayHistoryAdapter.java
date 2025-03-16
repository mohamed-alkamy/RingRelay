
package com.example.ringrelaygui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RelayHistoryAdapter extends RecyclerView.Adapter<RelayHistoryAdapter.RelayViewHolder> {
    private List<AlarmEntity> relayHistory;
    private Context context;

    public RelayHistoryAdapter(Context context, List<AlarmEntity> relayHistory) {
        this.context = context;
        this.relayHistory = relayHistory;
    }

    @NonNull
    @Override
    public RelayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_relay_history, parent, false);
        return new RelayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RelayViewHolder holder, int position) {
        AlarmEntity relay = relayHistory.get(position);
        holder.startTimeTextView.setText("Start: " + relay.startTime);
        holder.endTimeTextView.setText("End: " + relay.endTime);
    }

    @Override
    public int getItemCount() {
        return relayHistory.size();
    }

    public static class RelayViewHolder extends RecyclerView.ViewHolder {
        TextView startTimeTextView, endTimeTextView;

        public RelayViewHolder(@NonNull View itemView) {
            super(itemView);
            startTimeTextView = itemView.findViewById(R.id.startTimeTextView);
            endTimeTextView = itemView.findViewById(R.id.endTimeTextView);
        }
    }
}
