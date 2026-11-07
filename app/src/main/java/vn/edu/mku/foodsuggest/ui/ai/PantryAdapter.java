package vn.edu.mku.foodsuggest.ui.ai;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import vn.edu.mku.foodsuggest.R;
import vn.edu.mku.foodsuggest.data.ScanResult;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {
    private List<ScanResult> items = new ArrayList<>();
    private Set<Integer> selectedIds = new HashSet<>();

    public void setItems(List<ScanResult> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    // Lấy danh sách các nguyên liệu đang được chọn
    public List<ScanResult> getSelectedItems() {
        List<ScanResult> selected = new ArrayList<>();
        for (ScanResult item : items) {
            if (selectedIds.contains(item.id)) {
                selected.add(item);
            }
        }
        return selected;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScanResult item = items.get(position);
        holder.tvName.setText(item.label);

        if (item.imagePath != null && !item.imagePath.isEmpty()) {
            Picasso.get().load(new File(item.imagePath)).into(holder.imgIngredient);
        }

        // Đổi màu nếu được chọn
        boolean isSelected = selectedIds.contains(item.id);
        holder.layoutItem.setBackgroundColor(isSelected ? Color.parseColor("#A5D6A7") : Color.TRANSPARENT);

        holder.itemView.setOnClickListener(v -> {
            if (isSelected) {
                selectedIds.remove(item.id);
            } else {
                selectedIds.add(item.id);
            }
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutItem;
        ImageView imgIngredient;
        TextView tvName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutItem = itemView.findViewById(R.id.layoutItem);
            imgIngredient = itemView.findViewById(R.id.imgIngredient);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }
}