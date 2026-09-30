package vn.edu.mku.foodsuggest.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import vn.edu.mku.foodsuggest.R;
import vn.edu.mku.foodsuggest.data.AppDatabase;
import vn.edu.mku.foodsuggest.data.ScanResult;
import vn.edu.mku.foodsuggest.ui.MainActivity;

/**
 * Fragment hiển thị lịch sử các lần quét và nhận diện nguyên liệu (Chương 3)
 */
public class HistoryFragment extends Fragment {

    private RecyclerView recyclerHistory;
    private LinearLayout layoutEmpty;
    private HistoryRvAdapter adapter;
    private final List<ScanResult> historyList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);

        findViews(root);
        setupRecyclerView();
        loadHistory();

        return root;
    }

    private void findViews(View root) {
        recyclerHistory = root.findViewById(R.id.recyclerHistory);
        layoutEmpty = root.findViewById(R.id.layoutEmpty);
    }

    private void setupRecyclerView() {
        recyclerHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new HistoryRvAdapter(R.layout.layout_item_history_rv, historyList);
        recyclerHistory.setAdapter(adapter);

        // Chạm vào 1 mục lịch sử để mở danh sách món ăn gợi ý tương ứng
        adapter.setOnItemClickListener(item -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openRecipesWithFilter(item.label);
            }
        });

        // Hỗ trợ vuốt sang trái hoặc phải để xóa bản ghi khỏi CSDL Room (Chương 3.2.3)
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                ScanResult toDelete = adapter.getItemAt(position);
                adapter.removeAt(position);

                Executors.newSingleThreadExecutor().execute(() -> {
                    AppDatabase.getInstance(requireContext()).scanResultDao().deleteById(toDelete.id);
                });

                if (historyList.isEmpty()) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                }

                Toast.makeText(requireContext(), "Đã xóa lịch sử quét " + toDelete.label, Toast.LENGTH_SHORT).show();
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerHistory);
    }

    private void loadHistory() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ScanResult> list = AppDatabase.getInstance(requireContext()).scanResultDao().getAll();

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    historyList.clear();
                    if (list != null && !list.isEmpty()) {
                        historyList.addAll(list);
                        layoutEmpty.setVisibility(View.GONE);
                    } else {
                        // Thêm dữ liệu mẫu thử nghiệm theo Chương 3.2.4 nếu chưa có dữ liệu quét nào
                        seedSampleHistoryIfEmpty();
                    }
                    adapter.notifyDataSetChanged();
                });
            }
        });
    }

    /**
     * Dữ liệu mẫu ban đầu theo yêu cầu Chương 3.2.4 "Thêm dữ liệu mẫu"
     */
    private void seedSampleHistoryIfEmpty() {
        if (!historyList.isEmpty()) return;

        ScanResult sample1 = new ScanResult("Cà chua", 0.96f, "Rau củ", null,
                System.currentTimeMillis() - 1000 * 60 * 30,
                "• Canh Cà Chua Trứng\n• Đậu Phụ Sốt Cà Chua",
                "Giàu Lycopene tốt cho sức khỏe tim mạch.");
        ScanResult sample2 = new ScanResult("Trứng gà", 0.92f, "Trứng", null,
                System.currentTimeMillis() - 1000 * 60 * 120,
                "• Trứng Chiên Cà Chua\n• Canh Cà Chua Trứng",
                "Cung cấp Protein chất lượng cao và Choline cho não bộ.");

        historyList.add(sample1);
        historyList.add(sample2);
        layoutEmpty.setVisibility(View.GONE);

        // Lưu vào Room để lần sau vẫn còn
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            db.scanResultDao().insert(sample1);
            db.scanResultDao().insert(sample2);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistory();
    }
}
