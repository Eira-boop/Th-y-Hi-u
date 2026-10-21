package vn.edu.mku.foodsuggest.ui.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.concurrent.Executors;

import vn.edu.mku.foodsuggest.R;
import vn.edu.mku.foodsuggest.data.AppDatabase;
import vn.edu.mku.foodsuggest.ui.MainActivity;
import vn.edu.mku.foodsuggest.ui.history.HistoryFragment;

/**
 * Fragment cài đặt: cấu hình ngưỡng tin cậy AI, chế độ ăn và quản lý dữ liệu (Chương 1.2.4 & 4.2.4)
 */
public class SettingsFragment extends Fragment {

    private static final String PREFS_NAME = "smartcook_prefs";
    private static final String KEY_THRESHOLD = "confidence_threshold";
    private static final String KEY_VEGETARIAN = "pref_vegetarian";
    private static final String KEY_AUTOSAVE = "pref_autosave";

    private TextView textThresholdValue;
    private SeekBar seekBarThreshold;
    private MaterialSwitch switchVegetarian;
    private MaterialSwitch switchAutoSave;
    private MaterialButton buttonViewHistory;
    private MaterialButton buttonClearHistory;

    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        findViews(root);
        loadPreferences();
        setupListeners();

        return root;
    }

    private void findViews(View root) {
        textThresholdValue = root.findViewById(R.id.textThresholdValue);
        seekBarThreshold = root.findViewById(R.id.seekBarThreshold);
        switchVegetarian = root.findViewById(R.id.switchVegetarian);
        switchAutoSave = root.findViewById(R.id.switchAutoSave);
        buttonViewHistory = root.findViewById(R.id.buttonViewHistory);
        buttonClearHistory = root.findViewById(R.id.buttonClearHistory);
    }

    private void loadPreferences() {
        int threshold = prefs.getInt(KEY_THRESHOLD, 50);
        seekBarThreshold.setProgress(threshold);
        textThresholdValue.setText("Ngưỡng hiện tại: " + threshold + "%");

        switchVegetarian.setChecked(prefs.getBoolean(KEY_VEGETARIAN, false));
        switchAutoSave.setChecked(prefs.getBoolean(KEY_AUTOSAVE, true));
    }

    private void setupListeners() {
        seekBarThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int val = Math.max(10, progress);
                textThresholdValue.setText("Ngưỡng hiện tại: " + val + "%");
                prefs.edit().putInt(KEY_THRESHOLD, val).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        switchVegetarian.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_VEGETARIAN, isChecked).apply();
            Toast.makeText(requireContext(), isChecked ? "Đã bật ưu tiên món chay" : "Chế độ bình thường", Toast.LENGTH_SHORT).show();
        });

        switchAutoSave.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_AUTOSAVE, isChecked).apply();
        });

        if (buttonViewHistory != null) {
            buttonViewHistory.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).replaceFragment(new HistoryFragment());
                }
            });
        }

        buttonClearHistory.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa toàn bộ lịch sử quét nguyên liệu không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        Executors.newSingleThreadExecutor().execute(() -> {
                            AppDatabase.getInstance(requireContext()).scanResultDao().deleteAll();
                            if (isAdded()) {
                                requireActivity().runOnUiThread(() -> {
                                    Toast.makeText(requireContext(), "Đã xóa toàn bộ lịch sử quét thành công.", Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }
}
