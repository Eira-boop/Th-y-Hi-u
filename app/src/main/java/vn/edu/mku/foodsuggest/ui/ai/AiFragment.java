package vn.edu.mku.foodsuggest.ui.ai;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import vn.edu.mku.foodsuggest.R;
import vn.edu.mku.foodsuggest.data.AppDatabase;
import vn.edu.mku.foodsuggest.data.ScanResult;
import vn.edu.mku.foodsuggest.utils.GeminiChatHelper;

public class AiFragment extends Fragment {

    private RecyclerView rvPantry;
    private PantryAdapter pantryAdapter;

    private MaterialButton btnGenerateMenu;
    private LinearLayout layoutMenuResult;
    private TextView textMenuTitle;
    private TextView textMenuContent;
    private TextView textNutritionAdvice;

    private LinearLayout layoutChatMessages;
    private EditText editChatInput;
    private MaterialButton btnSendMessage;

    // Thêm biến cho ScrollView tổng
    private ScrollView mainScrollView;

    private GeminiChatHelper chatHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_ai, container, false);

        findViews(root);
        setupPantryRecyclerView();
        setupMenuGenerator();
        setupChatbot();

        chatHelper = new GeminiChatHelper();

        loadPantryData();

        return root;
    }

    private void findViews(View root) {
        rvPantry = root.findViewById(R.id.rvPantry);
        btnGenerateMenu = root.findViewById(R.id.btnGenerateMenu);
        layoutMenuResult = root.findViewById(R.id.layoutMenuResult);
        textMenuTitle = root.findViewById(R.id.textMenuTitle);
        textMenuContent = root.findViewById(R.id.textMenuContent);
        textNutritionAdvice = root.findViewById(R.id.textNutritionAdvice);

        layoutChatMessages = root.findViewById(R.id.layoutChatMessages);
        editChatInput = root.findViewById(R.id.editChatInput);
        btnSendMessage = root.findViewById(R.id.btnSendMessage);

        // Ánh xạ ScrollView tổng
        mainScrollView = root.findViewById(R.id.mainScrollView);
    }

    private void setupPantryRecyclerView() {
        pantryAdapter = new PantryAdapter();
        rvPantry.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvPantry.setAdapter(pantryAdapter);
    }

    private void loadPantryData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Lấy dữ liệu từ Room DB (những nguyên liệu đã từng chụp)
            List<ScanResult> scanResults = AppDatabase.getInstance(requireContext()).scanResultDao().getAll();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (scanResults != null) {
                        pantryAdapter.setItems(scanResults);
                    }
                });
            }
        });
    }

    private void setupMenuGenerator() {
        btnGenerateMenu.setOnClickListener(v -> {
            List<ScanResult> selectedItems = pantryAdapter.getSelectedItems();

            if (selectedItems.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng chạm chọn ít nhất 1 nguyên liệu trong tủ lạnh!", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> ingredientNames = new ArrayList<>();
            for (ScanResult item : selectedItems) {
                ingredientNames.add(item.label);
            }

            generateSmartMenuWithAI(ingredientNames);
        });
    }

    private void generateSmartMenuWithAI(List<String> ingredients) {
        layoutMenuResult.setVisibility(View.VISIBLE);

        String selectedStr = String.join(", ", ingredients);
        textMenuTitle.setText("🍱 Thực Đơn AI Cho: " + selectedStr);
        textMenuContent.setText("🤖 Bếp trưởng AI đang phân tích và lên thực đơn...");

        // Ẩn lời khuyên cũ đi (vì AI đã viết toàn bộ)
        if (textNutritionAdvice != null) {
            textNutritionAdvice.setVisibility(View.GONE);
        }

        // Gửi prompt cho Gemini AI
        String prompt = "Tôi có các nguyên liệu sau: " + selectedStr +
                ". Hãy lên một thực đơn gồm 1 món canh, 1 món mặn, 1 món xào. Trình bày ngắn gọn, có gạch đầu dòng.";

        chatHelper.getRecipeSuggestion(prompt).thenAccept(response -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    textMenuContent.setText(response);
                });
            }
        }).exceptionally(throwable -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    textMenuContent.setText("❌ Lỗi kết nối AI: " + throwable.getMessage());
                });
            }
            return null;
        });
    }

    private void setupChatbot() {
        setupPromptClicks();

        btnSendMessage.setOnClickListener(v -> {
            String question = editChatInput.getText().toString().trim();
            if (!question.isEmpty()) {
                handleUserQuestion(question);
                editChatInput.setText("");
            }
        });
    }

    private void setupPromptClicks() {
        View root = getView();
        if (root == null) return;

        Chip chipPromptDiet = root.findViewById(R.id.chipPromptDiet);
        Chip chipPromptBeef = root.findViewById(R.id.chipPromptBeef);
        Chip chipPromptSoup = root.findViewById(R.id.chipPromptSoup);
        Chip chipPromptVeg = root.findViewById(R.id.chipPromptVeg);

        if (chipPromptDiet != null) chipPromptDiet.setOnClickListener(v -> handleUserQuestion("Gợi ý thực đơn giảm cân lành mạnh"));
        if (chipPromptBeef != null) chipPromptBeef.setOnClickListener(v -> handleUserQuestion("Bí quyết xào thịt bò mềm ngọt không dai"));
        if (chipPromptSoup != null) chipPromptSoup.setOnClickListener(v -> handleUserQuestion("Nấu gì khi chỉ có trứng và cà chua?"));
        if (chipPromptVeg != null) chipPromptVeg.setOnClickListener(v -> handleUserQuestion("Cách giữ rau xanh mướt khi luộc"));
    }

    private void handleUserQuestion(String question) {
        addUserMessage(question);

        LinearLayout loadingRow = addBotMessage("🤖 AI Bếp Trưởng đang suy nghĩ...");

        chatHelper.getRecipeSuggestion(question).thenAccept(response -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    layoutChatMessages.removeView(loadingRow);
                    addBotMessage(response);
                });
            }
        }).exceptionally(throwable -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    layoutChatMessages.removeView(loadingRow);
                    addBotMessage("❌ Lỗi kết nối AI: " + throwable.getMessage());
                });
            }
            return null;
        });
    }

    private void addUserMessage(String message) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        row.setGravity(Gravity.END);
        row.setOrientation(LinearLayout.HORIZONTAL);

        TextView tv = new TextView(requireContext());
        tv.setText(message);
        tv.setTextColor(getResources().getColor(R.color.white, null));
        tv.setTextSize(13);
        tv.setPadding(28, 20, 28, 20);
        tv.setBackgroundResource(R.drawable.bg_chat_user);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = 20;
        params.leftMargin = 120;
        tv.setLayoutParams(params);

        row.addView(tv);
        layoutChatMessages.addView(row);
        scrollToBottom();
    }

    private LinearLayout addBotMessage(String message) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        row.setGravity(Gravity.START | Gravity.TOP);
        row.setOrientation(LinearLayout.HORIZONTAL);

        ImageView botIcon = new ImageView(requireContext());
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(64, 64);
        iconParams.topMargin = 8;
        botIcon.setLayoutParams(iconParams);
        botIcon.setImageResource(R.drawable.ic_bot_avatar);

        TextView tv = new TextView(requireContext());
        tv.setText(message);
        tv.setTextColor(getResources().getColor(R.color.text_primary, null));
        tv.setTextSize(13);
        tv.setLineSpacing(6f, 1f);
        tv.setPadding(28, 24, 28, 24);
        tv.setBackgroundResource(R.drawable.bg_chat_bot);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 16;
        params.bottomMargin = 20;
        params.rightMargin = 60;
        tv.setLayoutParams(params);

        row.addView(botIcon);
        row.addView(tv);
        layoutChatMessages.addView(row);

        scrollToBottom();
        return row;
    }

    // Hàm cuộn xuống dưới cùng - Đã sửa để dùng mainScrollView
    private void scrollToBottom() {
        if (mainScrollView != null) {
            mainScrollView.post(() -> mainScrollView.fullScroll(View.FOCUS_DOWN));
        }
    }
}