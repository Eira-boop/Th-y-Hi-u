package vn.edu.mku.foodsuggest.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.edu.mku.foodsuggest.R;
import vn.edu.mku.foodsuggest.data.ScanResult;

/**
 * Adapter hiển thị danh sách lịch sử quét nguyên liệu (Chương 3.2.3)
 */
public class HistoryRvAdapter extends RecyclerView.Adapter<HistoryRvAdapter.ViewHolder> {

    private final int LAYOUT_ID;
    private final List<ScanResult> data;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ScanResult item);
    }

    public HistoryRvAdapter(int layoutId, List<ScanResult> data) {
        this.LAYOUT_ID = layoutId;
        this.data = data;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(LAYOUT_ID, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScanResult item = data.get(holder.getAdapterPosition());

        holder.textLabel.setText(item.label);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String dateStr = sdf.format(new Date(item.timestamp));
        String infoText = String.format("%s • %s", item.category != null ? item.category : "Nguyên liệu", dateStr);
        holder.textInfo.setText(infoText);

        holder.textConfidence.setText(String.format(Locale.getDefault(), "%.0f%%", item.confidence * 100));

        if (item.imagePath != null && new File(item.imagePath).exists()) {
            Picasso.get()
                    .load(new File(item.imagePath))
                    .placeholder(R.drawable.background_thumb_placeholder)
                    .error(R.drawable.background_thumb_placeholder)
                    .into(holder.imageThumb);
        } else {
            holder.imageThumb.setImageResource(R.drawable.background_thumb_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public ScanResult getItemAt(int position) {
        return data.get(position);
    }

    public void removeAt(int position) {
        data.remove(position);
        notifyItemRemoved(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageThumb;
        TextView textLabel, textInfo, textConfidence;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageThumb = itemView.findViewById(R.id.imageThumb);
            textLabel = itemView.findViewById(R.id.textLabel);
            textInfo = itemView.findViewById(R.id.textInfo);
            textConfidence = itemView.findViewById(R.id.textConfidence);
        }
    }
}
