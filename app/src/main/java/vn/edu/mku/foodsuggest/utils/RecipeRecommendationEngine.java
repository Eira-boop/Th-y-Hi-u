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

        DISH_SUGGESTIONS.put("Đậu cô ve", Arrays.asList(
                "Đậu Cô Ve Xào Tỏi", "Đậu Cô Ve Xào Thịt Bò", "Đậu Cô Ve Luộc Chấm Muối Mè"));
        NUTRITION_TIPS.put("Đậu cô ve", "Giàu chất xơ và vitamin K, nên xào nhanh lửa lớn để giữ màu xanh và độ giòn.");
        CATEGORY_MAP.put("Đậu cô ve", "Rau củ");

        DISH_SUGGESTIONS.put("Khổ qua", Arrays.asList(
                "Khổ Qua Xào Trứng", "Canh Khổ Qua Nhồi Thịt", "Khổ Qua Xào Thịt Bò"));
        NUTRITION_TIPS.put("Khổ qua", "Vị đắng giúp hỗ trợ ổn định đường huyết, ngâm nước muối loãng trước khi nấu để bớt đắng.");
        CATEGORY_MAP.put("Khổ qua", "Rau củ");

        DISH_SUGGESTIONS.put("Bầu", Arrays.asList(
                "Canh Bầu Nấu Tôm", "Bầu Xào Trứng", "Bầu Luộc Chấm Kho Quẹt"));
        NUTRITION_TIPS.put("Bầu", "Mát, nhiều nước và ít calo, hợp nấu canh giải nhiệt mùa nóng.");
        CATEGORY_MAP.put("Bầu", "Rau củ");

        DISH_SUGGESTIONS.put("Cà tím", Arrays.asList(
                "Cà Tím Nướng Mỡ Hành", "Cà Tím Xào Thịt Băm", "Cà Tím Kho Tộ"));
        NUTRITION_TIPS.put("Cà tím", "Vỏ tím chứa Anthocyanin chống oxy hóa, nên giữ vỏ khi chế biến.");
        CATEGORY_MAP.put("Cà tím", "Rau củ");

        DISH_SUGGESTIONS.put("Bông cải xanh", Arrays.asList(
                "Bông Cải Xanh Xào Tỏi", "Súp Bông Cải Xanh", "Bông Cải Xanh Xào Thịt Bò"));
        NUTRITION_TIPS.put("Bông cải xanh", "Giàu vitamin C và Sulforaphane, nên hấp hoặc chần nhanh để giữ dưỡng chất.");
        CATEGORY_MAP.put("Bông cải xanh", "Rau củ");

        DISH_SUGGESTIONS.put("Súp lơ trắng", Arrays.asList(
                "Súp Lơ Xào Tỏi", "Súp Lơ Xào Thịt Bò", "Canh Súp Lơ Hầm Xương"));
        NUTRITION_TIPS.put("Súp lơ trắng", "Ít tinh bột, giàu vitamin C và chất xơ, hợp chế độ ăn giảm cân.");
        CATEGORY_MAP.put("Súp lơ trắng", "Rau củ");

        DISH_SUGGESTIONS.put("Dưa leo", Arrays.asList(
                "Gỏi Dưa Leo Tôm Thịt", "Dưa Leo Muối Chua", "Canh Dưa Leo Nhồi Thịt"));
        NUTRITION_TIPS.put("Dưa leo", "Chứa hơn 90% nước, giải nhiệt tốt, nên ăn cả vỏ đã rửa sạch.");
        CATEGORY_MAP.put("Dưa leo", "Rau củ");

        DISH_SUGGESTIONS.put("Đu đủ", Arrays.asList(
                "Gỏi Đu Đủ Khô Bò", "Canh Đu Đủ Hầm Xương", "Đu Đủ Xào Tôm"));
        NUTRITION_TIPS.put("Đu đủ", "Chứa enzyme Papain hỗ trợ tiêu hóa, đu đủ xanh thường dùng làm gỏi và nấu canh.");
        CATEGORY_MAP.put("Đu đủ", "Trái cây / Rau củ");

        DISH_SUGGESTIONS.put("Bí đỏ", Arrays.asList(
                "Canh Bí Đỏ Nấu Tôm", "Bí Đỏ Xào Tỏi", "Súp Bí Đỏ"));
        NUTRITION_TIPS.put("Bí đỏ", "Giàu Beta-carotene tốt cho mắt, nấu chín mềm để dễ hấp thu.");
        CATEGORY_MAP.put("Bí đỏ", "Rau củ");

        DISH_SUGGESTIONS.put("Củ cải", Arrays.asList(
                "Canh Củ Cải Hầm Xương", "Củ Cải Kho Thịt", "Củ Cải Muối Chua"));
        NUTRITION_TIPS.put("Củ cải", "Hỗ trợ tiêu hóa, nên nấu chín kỹ để bớt hăng.");
        CATEGORY_MAP.put("Củ cải", "Rau củ");

        DISH_SUGGESTIONS.put("Cua", Arrays.asList(
                "Cua Hấp Bia", "Canh Cua Rau Đay", "Cua Rang Muối"));
        NUTRITION_TIPS.put("Cua", "Giàu đạm, canxi và kẽm, cần nấu chín kỹ, người dị ứng hải sản nên thận trọng.");
        CATEGORY_MAP.put("Cua", "Hải sản");

        DISH_SUGGESTIONS.put("Mực", Arrays.asList(
                "Mực Xào Sa Tế", "Mực Hấp Gừng", "Mực Nhồi Thịt Hấp"));
        NUTRITION_TIPS.put("Mực", "Giàu đạm và Taurine, không nên nấu quá lâu vì mực sẽ dai.");
        CATEGORY_MAP.put("Mực", "Hải sản");

        DISH_SUGGESTIONS.put("Bạch tuộc", Arrays.asList(
                "Bạch Tuộc Nướng Sa Tế", "Bạch Tuộc Xào Rau Muống", "Lẩu Bạch Tuộc"));
        NUTRITION_TIPS.put("Bạch tuộc", "Ít béo, nhiều đạm, chần sơ qua nước sôi trước khi chế biến để giòn.");
        CATEGORY_MAP.put("Bạch tuộc", "Hải sản");

        DISH_SUGGESTIONS.put("Nghêu", Arrays.asList(
                "Nghêu Hấp Sả", "Canh Nghêu Nấu Rau Muống", "Nghêu Xào Tỏi"));
        NUTRITION_TIPS.put("Nghêu", "Ngâm nước muối loãng 1–2 giờ cho nhả sạch cát trước khi nấu.");
        CATEGORY_MAP.put("Nghêu", "Hải sản");

        DISH_SUGGESTIONS.put("Lươn", Arrays.asList(
                "Lươn Xào Sả Ớt", "Cháo Lươn", "Lươn Um Chuối Đậu"));
        NUTRITION_TIPS.put("Lươn", "Giàu đạm và vitamin A, cần làm sạch nhớt kỹ bằng muối và nước nóng.");
        CATEGORY_MAP.put("Lươn", "Hải sản");

        DISH_SUGGESTIONS.put("Tôm hùm", Arrays.asList(
                "Tôm Hùm Hấp Bia", "Tôm Hùm Nướng Phô Mai", "Cháo Tôm Hùm"));
        NUTRITION_TIPS.put("Tôm hùm", "Thịt giàu đạm, ít béo, hấp hoặc nướng để giữ vị ngọt tự nhiên.");
        CATEGORY_MAP.put("Tôm hùm", "Hải sản");
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