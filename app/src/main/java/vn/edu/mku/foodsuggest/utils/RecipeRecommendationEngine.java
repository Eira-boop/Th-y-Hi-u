package vn.edu.mku.foodsuggest.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Công cụ gợi ý món ăn thông minh dựa trên nguyên liệu (Rule-based & Matching Engine)
 */
public class RecipeRecommendationEngine {

    private static final Map<String, List<String>> DISH_SUGGESTIONS = new HashMap<>();
    private static final Map<String, String> NUTRITION_TIPS = new HashMap<>();
    private static final Map<String, String> CATEGORY_MAP = new HashMap<>();

    static {
        // Cà chua
        DISH_SUGGESTIONS.put("Cà chua", Arrays.asList(
                "Canh Cà Chua Trứng", "Trứng Chiên Cà Chua", "Đậu Phụ Sốt Cà Chua", "Cá Sốt Cà Chua Cay Ngọt", "Canh Chua Tôm Cà Chua"
        ));
        NUTRITION_TIPS.put("Cà chua", "Cà chua giàu Lycopene, nên xào cùng một ít dầu mỡ để tăng khả năng hấp thụ dưỡng chất chống oxy hóa.");
        CATEGORY_MAP.put("Cà chua", "Rau củ");

        // Trứng gà
        DISH_SUGGESTIONS.put("Trứng gà", Arrays.asList(
                "Canh Cà Chua Trứng", "Trứng Chiên Cà Chua", "Trứng Cuộn Rau Củ", "Trứng Hấp Nấm Thịt Bằm"
        ));
        NUTRITION_TIPS.put("Trứng gà", "Nguồn đạm chuẩn sinh học dồi dào, lòng đỏ chứa nhiều Lutein và Choline tốt cho não bộ.");
        CATEGORY_MAP.put("Trứng gà", "Trứng");

        // Thịt bò
        DISH_SUGGESTIONS.put("Thịt bò", Arrays.asList(
                "Bò Xào Hành Tây Cần Tây", "Bò Lúc Lắc Ớt Chuông", "Rau Muống Xào Tỏi Bò", "Bò Sốt Tiêu Đen"
        ));
        NUTRITION_TIPS.put("Thịt bò", "Giàu chất sắt Heme và vitamin B12, nên xào nhanh trên lửa lớn để giữ độ mềm ngọt.");
        CATEGORY_MAP.put("Thịt bò", "Thịt tươi");

        // Thịt heo
        DISH_SUGGESTIONS.put("Thịt heo", Arrays.asList(
                "Canh Súp Thịt Heo Cà Rốt Khoai Tây", "Khoai Tây Xào Thịt Heo", "Thịt Heo Kho Trứng", "Thịt Nạc Xào Nấm Rơm"
        ));
        NUTRITION_TIPS.put("Thịt heo", "Nguồn bổ sung vitamin B1 thiết yếu, nên luộc chần sơ để khử mùi và làm sạch.");
        CATEGORY_MAP.put("Thịt heo", "Thịt tươi");

        // Thịt gà
        DISH_SUGGESTIONS.put("Thịt gà", Arrays.asList(
                "Gà Xào Nấm Rơm", "Gà Hầm Cà Rốt Khoai Tây", "Gà Xào Sả Ớt", "Canh Gà Nấu Nấm"
        ));
        NUTRITION_TIPS.put("Thịt gà", "Ức và đùi gà ít chất béo bão hòa, thích hợp cho chế độ ăn lành mạnh và tăng cơ.");
        CATEGORY_MAP.put("Thịt gà", "Thịt tươi");

        // Hành tây
        DISH_SUGGESTIONS.put("Hành tây", Arrays.asList(
                "Bò Xào Hành Tây Cần Tây", "Bò Lúc Lắc Ớt Chuông", "Khoai Tây Xào Thịt Heo", "Hành Tây Xào Trứng"
        ));
        NUTRITION_TIPS.put("Hành tây", "Chứa nhiều Quercetin giúp kháng viêm, hạ mỡ máu và tăng sức đề kháng.");
        CATEGORY_MAP.put("Hành tây", "Rau củ");

        // Cà rốt
        DISH_SUGGESTIONS.put("Cà rốt", Arrays.asList(
                "Canh Súp Thịt Heo Cà Rốt Khoai Tây", "Tôm Xào Bông Cải Bắp Cải", "Bò Kho Cà Rốt", "Nộm Cà Rốt Tai Heo"
        ));
        NUTRITION_TIPS.put("Cà rốt", "Rất giàu Beta-carotene (tiền chất vitamin A), bảo vệ thị lực và làm đẹp da.");
        CATEGORY_MAP.put("Cà rốt", "Rau củ");

        // Khoai tây
        DISH_SUGGESTIONS.put("Khoai tây", Arrays.asList(
                "Canh Súp Thịt Heo Cà Rốt Khoai Tây", "Khoai Tây Xào Thịt Heo", "Khoai Tây Chiên Bơ Tỏi", "Súp Kem Khoai Tây"
        ));
        NUTRITION_TIPS.put("Khoai tây", "Giàu kali và tinh bột kháng, sau khi gọt nên ngâm nước muối loãng để không bị thâm.");
        CATEGORY_MAP.put("Khoai tây", "Rau củ");

        // Nấm rơm
        DISH_SUGGESTIONS.put("Nấm rơm", Arrays.asList(
                "Gà Xào Nấm Rơm", "Đậu Phụ Kho Nấm Rơm", "Canh Nấm Rơm Đậu Phụ", "Trứng Đúc Nấm Rơm"
        ));
        NUTRITION_TIPS.put("Nấm rơm", "Chứa 18 loại axit amin, là nguyên liệu tuyệt vời thay thế thịt cho người ăn chay.");
        CATEGORY_MAP.put("Nấm rơm", "Nấm / Rau củ");

        // Tôm tươi
        DISH_SUGGESTIONS.put("Tôm tươi", Arrays.asList(
                "Tôm Xào Bông Cải Bắp Cải", "Canh Chua Tôm Cà Chua Bắp Cải", "Tôm Rim Mặn Ngọt", "Tôm Hấp Bia"
        ));
        NUTRITION_TIPS.put("Tôm tươi", "Nguồn canxi và phốt pho cao, cung cấp i-ốt và kẽm tăng cường miễn dịch.");
        CATEGORY_MAP.put("Tôm tươi", "Hải sản");

        // Đậu phụ
        DISH_SUGGESTIONS.put("Đậu phụ", Arrays.asList(
                "Đậu Phụ Sốt Cà Chua", "Đậu Phụ Nhồi Thịt", "Canh Rong Biển Đậu Phụ", "Đậu Phụ Chiên Giòn Lắc Phô Mai"
        ));
        NUTRITION_TIPS.put("Đậu phụ", "Giàu protein thực vật, không chứa cholesterol, rất tốt cho tim mạch.");
        CATEGORY_MAP.put("Đậu phụ", "Món chay / Chế phẩm");

        // Bắp cải
        DISH_SUGGESTIONS.put("Bắp cải", Arrays.asList(
                "Tôm Xào Bông Cải Bắp Cải", "Canh Chua Tôm Cà Chua Bắp Cải", "Bắp Cải Luộc Chấm Trứng Dầm Nước Mắm", "Bắp Cải Xào Thịt Bò"
        ));
        NUTRITION_TIPS.put("Bắp cải", "Giàu vitamin U hỗ trợ niêm mạc dạ dày, luộc giữ nước làm canh giải nhiệt rất tốt.");
        CATEGORY_MAP.put("Bắp cải", "Rau củ");

        // Ớt chuông
        DISH_SUGGESTIONS.put("Ớt chuông", Arrays.asList(
                "Bò Lúc Lắc Ớt Chuông", "Ớt Chuông Xào Mực", "Salad Ớt Chuông Dầu Giấm", "Ớt Chuông Nhồi Thịt Nướng"
        ));
        NUTRITION_TIPS.put("Ớt chuông", "Hàm lượng vitamin C cực cao, ăn tái chín tới để giữ độ giòn và dưỡng chất.");
        CATEGORY_MAP.put("Ớt chuông", "Rau củ");

        // Rau muống
        DISH_SUGGESTIONS.put("Rau muống", Arrays.asList(
                "Rau Muống Xào Tỏi Bò", "Canh Rau Muống Nấu Tôm", "Rau Muống Luộc Chanh", "Nộm Rau Muống Tép Đồng"
        ));
        NUTRITION_TIPS.put("Rau muống", "Chứa nhiều chất xơ hòa tan và diệp lục tố, nên chần nước sôi rồi ngâm nước đá để giữ độ xanh giòn.");
        CATEGORY_MAP.put("Rau muống", "Rau củ");

        // Cá tươi
        DISH_SUGGESTIONS.put("Cá tươi", Arrays.asList(
                "Cá Sốt Cà Chua Cay Ngọt", "Canh Chua Cá Lóc", "Cá Kho Tộ", "Cá Hấp Xì Dầu Gừng Hành"
        ));
        NUTRITION_TIPS.put("Cá tươi", "Cung cấp Omega-3 EPA/DHA tự nhiên tốt cho tim mạch và phát triển trí não.");
        CATEGORY_MAP.put("Cá tươi", "Thủy hải sản");
    }

    public static List<String> getSuggestedDishes(String ingredient) {
        if (ingredient == null) return new ArrayList<>();
        for (String key : DISH_SUGGESTIONS.keySet()) {
            if (ingredient.toLowerCase().contains(key.toLowerCase()) || key.toLowerCase().contains(ingredient.toLowerCase())) {
                return DISH_SUGGESTIONS.get(key);
            }
        }
        return Arrays.asList("Món Xào Thập Cẩm", "Canh Rau Củ Hầm Xương", "Món Nấu Gia Đình");
    }

    public static String getSuggestedDishesFormatted(String ingredient) {
        List<String> list = getSuggestedDishes(ingredient);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(3, list.size()); i++) {
            sb.append("• ").append(list.get(i));
            if (i < Math.min(3, list.size()) - 1) sb.append("\n");
        }
        return sb.toString();
    }

    public static String getNutritionTip(String ingredient) {
        if (ingredient == null) return "Bảo quản nơi khô ráo, thoáng mát hoặc ngăn mát tủ lạnh.";
        for (String key : NUTRITION_TIPS.keySet()) {
            if (ingredient.toLowerCase().contains(key.toLowerCase()) || key.toLowerCase().contains(ingredient.toLowerCase())) {
                return NUTRITION_TIPS.get(key);
            }
        }
        return "Nguyên liệu tươi ngon, giàu giá trị dinh dưỡng cho bữa cơm gia đình.";
    }

    public static String getCategory(String ingredient) {
        if (ingredient == null) return "Thực phẩm";
        for (String key : CATEGORY_MAP.keySet()) {
            if (ingredient.toLowerCase().contains(key.toLowerCase()) || key.toLowerCase().contains(ingredient.toLowerCase())) {
                return CATEGORY_MAP.get(key);
            }
        }
        return "Rau củ quả";
    }
}
