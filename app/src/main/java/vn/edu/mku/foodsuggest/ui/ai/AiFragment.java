package vn.edu.mku.foodsuggest.ui.ai;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

import vn.edu.mku.foodsuggest.R;
import vn.edu.mku.foodsuggest.data.AppDatabase;
import vn.edu.mku.foodsuggest.data.Recipe;
import vn.edu.mku.foodsuggest.utils.RecipeRecommendationEngine;

/**
 * Fragment Phần AI: Lên thực đơn thông minh từ tủ lạnh và Trợ lý Bếp Trưởng AI
 */
public class AiFragment extends Fragment {

    private static final String[] POPULAR_INGREDIENTS = {
            "Cà chua", "Trứng gà", "Thịt bò", "Thịt heo", "Thịt gà",
            "Đậu phụ", "Nấm rơm", "Hành tây", "Cà rốt", "Khoai tây",
            "Bắp cải", "Ớt chuông", "Tôm tươi", "Rau muống", "Cá tươi"
    };

    private ChipGroup chipGroupIngredients;
    private MaterialButton btnGenerateMenu;
    private LinearLayout layoutMenuResult;
    private TextView textMenuTitle;
    private TextView textMenuContent;
    private TextView textNutritionAdvice;

    private LinearLayout layoutChatMessages;
    private EditText editChatInput;
    private MaterialButton btnSendMessage;

    private final List<Recipe> cachedRecipes = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_ai, container, false);

        findViews(root);
        setupIngredientChips();
        setupMenuGenerator();
        setupChatbot();
        loadRecipesForEngine();

        return root;
    }

    private void findViews(View root) {
        chipGroupIngredients = root.findViewById(R.id.chipGroupIngredients);
        btnGenerateMenu = root.findViewById(R.id.btnGenerateMenu);
        layoutMenuResult = root.findViewById(R.id.layoutMenuResult);
        textMenuTitle = root.findViewById(R.id.textMenuTitle);
        textMenuContent = root.findViewById(R.id.textMenuContent);
        textNutritionAdvice = root.findViewById(R.id.textNutritionAdvice);

        layoutChatMessages = root.findViewById(R.id.layoutChatMessages);
        editChatInput = root.findViewById(R.id.editChatInput);
        btnSendMessage = root.findViewById(R.id.btnSendMessage);
    }

    private void loadRecipesForEngine() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Recipe> list = AppDatabase.getInstance(requireContext()).recipeDao().getAll();
            if (list != null) {
                cachedRecipes.addAll(list);
            }
        });
    }

    private void setupIngredientChips() {
        for (String ing : POPULAR_INGREDIENTS) {
            Chip chip = new Chip(requireContext());
            chip.setText(ing);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(true);
            chipGroupIngredients.addView(chip);
        }
    }

    private void setupMenuGenerator() {
        btnGenerateMenu.setOnClickListener(v -> {
            List<String> selected = new ArrayList<>();
            for (int i = 0; i < chipGroupIngredients.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupIngredients.getChildAt(i);
                if (chip.isChecked()) {
                    selected.add(chip.getText().toString());
                }
            }

            if (selected.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng chọn ít nhất 1 hoặc 2 nguyên liệu có sẵn!", Toast.LENGTH_SHORT).show();
                return;
            }

            generateSmartMenu(selected);
        });
    }

    private void generateSmartMenu(List<String> ingredients) {
        layoutMenuResult.setVisibility(View.VISIBLE);

        StringBuilder selectedStr = new StringBuilder();
        for (int i = 0; i < ingredients.size(); i++) {
            selectedStr.append(ingredients.get(i));
            if (i < ingredients.size() - 1) selectedStr.append(", ");
        }

        textMenuTitle.setText("🍱 Thực Đơn AI Cho: " + selectedStr);

        // Lọc các món ăn phù hợp với nguyên liệu đã chọn
        List<String> mainDishes = new ArrayList<>();
        List<String> soups = new ArrayList<>();
        List<String> stirFries = new ArrayList<>();

        for (String ing : ingredients) {
            List<String> suggested = RecipeRecommendationEngine.getSuggestedDishes(ing);
            for (String dish : suggested) {
                if (dish.toLowerCase().contains("canh") || dish.toLowerCase().contains("súp")) {
                    if (!soups.contains(dish)) soups.add(dish);
                } else if (dish.toLowerCase().contains("xào") || dish.toLowerCase().contains("luộc") || dish.toLowerCase().contains("salad")) {
                    if (!stirFries.contains(dish)) stirFries.add(dish);
                } else {
                    if (!mainDishes.contains(dish)) mainDishes.add(dish);
                }
            }
        }

        // Đảm bảo có gợi ý phong phú
        if (mainDishes.isEmpty()) mainDishes.add("Trứng Chiên Cà Chua / Thịt Rang Cháy Cạnh");
        if (soups.isEmpty()) soups.add("Canh Cà Chua Trứng Thanh Nhiệt");
        if (stirFries.isEmpty()) stirFries.add("Rau Muống Xào Tỏi Giòn Ngon");

        StringBuilder sb = new StringBuilder();
        sb.append("🍲 Món Canh: ").append(soups.get(0)).append("\n");
        sb.append("🥩 Món Mặn: ").append(mainDishes.get(0)).append("\n");
        sb.append("🥗 Món Xào/Rau: ").append(stirFries.get(0)).append("\n\n");
        sb.append("⏱ Thời gian chuẩn bị: ~25 - 35 phút\n");
        sb.append("🔥 Năng lượng ước tính: ~550 - 680 kcal / người");

        textMenuContent.setText(sb.toString());

        // Lời khuyên dinh dưỡng tổng hợp
        StringBuilder advice = new StringBuilder("💡 Đánh giá của AI Chef: Mâm cơm cân bằng hoàn hảo giữa protein ");
        if (ingredients.contains("Thịt bò") || ingredients.contains("Thịt gà") || ingredients.contains("Trứng gà") || ingredients.contains("Tôm tươi")) {
            advice.append("từ thịt/trứng/hải sản ");
        }
        advice.append("và vitamin, chất xơ dồi dào từ rau củ quả. ");
        advice.append(RecipeRecommendationEngine.getNutritionTip(ingredients.get(0)));

        textNutritionAdvice.setText(advice.toString());
    }

    private void setupChatbot() {
        // Quick prompts
        View chipPromptDiet = getView() != null ? getView().findViewById(R.id.chipPromptDiet) : null;
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

        if (chipPromptDiet != null) {
            chipPromptDiet.setOnClickListener(v -> handleUserQuestion("Gợi ý thực đơn giảm cân lành mạnh"));
        }
        if (chipPromptBeef != null) {
            chipPromptBeef.setOnClickListener(v -> handleUserQuestion("Bí quyết xào thịt bò mềm ngọt không dai"));
        }
        if (chipPromptSoup != null) {
            chipPromptSoup.setOnClickListener(v -> handleUserQuestion("Nấu gì khi chỉ có trứng và cà chua?"));
        }
        if (chipPromptVeg != null) {
            chipPromptVeg.setOnClickListener(v -> handleUserQuestion("Cách giữ rau xanh mướt khi luộc"));
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupPromptClicks();
    }

    private void handleUserQuestion(String question) {
        addUserMessage(question);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded()) {
                String reply = answerQuestion(question);
                addBotMessage(reply);
            }
        }, 500);
    }

    private void addUserMessage(String message) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        row.setGravity(Gravity.END);
        row.setOrientation(LinearLayout.HORIZONTAL);

        TextView tv = new TextView(requireContext());
        tv.setText(message);
        tv.setTextColor(getResources().getColor(R.color.white, null));
        tv.setTextSize(13);
        tv.setPadding(28, 20, 28, 20);
        tv.setBackgroundResource(R.drawable.bg_chat_user);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = 20;
        params.leftMargin = 120;
        tv.setLayoutParams(params);

        row.addView(tv);
        layoutChatMessages.addView(row);
    }

    private void addBotMessage(String message) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
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

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = 16;
        params.bottomMargin = 20;
        params.rightMargin = 60;
        tv.setLayoutParams(params);

        row.addView(botIcon);
        row.addView(tv);
        layoutChatMessages.addView(row);
    }

    private String answerQuestion(String q) {
        String query = q.toLowerCase();

        if (query.contains("giảm cân") || query.contains("diet") || query.contains("calo")) {
            return "🥗 Gợi ý thực đơn giảm cân của AI Chef:\n" +
                    "• Bữa trưa: Ức gà áp chảo (150g) + Nấm rơm xào dầu oliu + Bắp cải luộc chấm trứng.\n" +
                    "• Bữa tối: Canh cà chua đậu phụ thanh đạm + Đĩa rau muống chần.\n" +
                    "💡 Mẹo calo: Tổng năng lượng ~420 kcal, giàu đạm và chất xơ no lâu, hạn chế tích mỡ!";
        } else if (query.contains("thịt bò") || query.contains("bò mềm") || query.contains("dai")) {
            return "🥩 Bí quyết xào thịt bò mềm ngọt chuẩn nhà hàng:\n" +
                    "1. Luôn thái thịt ngang thớ bản mỏng.\n" +
                    "2. Ướp cùng 1 thìa dầu ăn, tỏi băm và một chút bột bắp (tinh bột giúp giữ nước ngọt bên trong thịt).\n" +
                    "3. Đun chảo thật nóng, xào lửa lớn trong 90 giây đến khi bò vừa chín tới thì trút ra ngay, không để lâu trên chảo!";
        } else if (query.contains("trứng") && query.contains("cà chua")) {
            return "🍅 Trứng & Cà chua là cặp đôi vàng trong ẩm thực:\n" +
                    "1. Canh Cà Chua Trứng (15 phút, 160 kcal): Khuấy tạo xoáy nước rồi rót trứng từ từ để có vân hoa bồng bềnh đẹp mắt.\n" +
                    "2. Trứng Chiên Cà Chua (10 phút): Đậm đà, béo ngậy, giàu Lycopene và Choline rất tốt cho mắt và não bộ.";
        } else if (query.contains("rau") || query.contains("xanh") || query.contains("luộc")) {
            return "🥬 Mẹo luộc rau xanh mướt giòn ngọt:\n" +
                    "• Đun nước thật sôi bùng rồi mới cho rau vào, thêm 1/2 thìa cà phê muối.\n" +
                    "• Không đậy nắp vung để axit hữu cơ trong rau bay hơi (tránh vàng ố lá).\n" +
                    "• Vớt ngay ra âu nước đá lạnh 2 phút để hãm nhiệt, giữ màu xanh diệp lục tươi rói!";
        } else if (query.contains("chay") || query.contains("đậu phụ")) {
            return "🌿 Thực đơn thuần chay dinh dưỡng:\n" +
                    "• Đậu phụ sốt cà chua ngò rí\n" +
                    "• Canh rong biển nấm rơm đậu phụ\n" +
                    "• Cà rốt xào nấm & bắp cải thanh nhiệt\n" +
                    "Cung cấp đủ axit amin thiết yếu mà không cần thịt cá!";
        } else {
            return "👨‍🍳 Bếp Trưởng AI xin chia sẻ:\n" +
                    "Với câu hỏi của bạn: \"" + q + "\", nguyên tắc ẩm thực quan trọng nhất là chọn nguyên liệu tươi mới, nêm gia vị theo thứ tự (mặn -> ngọt -> thơm), và điều chỉnh nhiệt độ lửa chuẩn xác. Bạn có thể chọn tab 'Danh sách' để xem chi tiết từng công thức nấu ăn nhé!";
        }
    }
}
