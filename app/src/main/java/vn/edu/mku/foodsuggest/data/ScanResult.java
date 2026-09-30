package vn.edu.mku.foodsuggest.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity lưu kết quả quét nguyên liệu và gợi ý món ăn vào Room Database (Chương 3)
 */
@Entity(tableName = "scan_results")
public class ScanResult {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String label;             // Tên nguyên liệu: Cà chua, Trứng gà, Thịt bò,...
    public float confidence;          // Độ tin cậy (0.0 -> 1.0)
    public String category;          // Phân nhóm: Rau củ, Thịt, Trứng, Hải sản,...
    public String imagePath;         // Đường dẫn ảnh đã chụp / cắt
    public long timestamp;           // Thời điểm quét (epoch millis)
    public String suggestedDishes;   // Danh sách các món ăn được gợi ý
    public String nutritionTip;      // Mẹo bảo quản hoặc giá trị dinh dưỡng

    public ScanResult() {
    }

    public ScanResult(String label, float confidence, String category, String imagePath, long timestamp, String suggestedDishes, String nutritionTip) {
        this.label = label;
        this.confidence = confidence;
        this.category = category;
        this.imagePath = imagePath;
        this.timestamp = timestamp;
        this.suggestedDishes = suggestedDishes;
        this.nutritionTip = nutritionTip;
    }
}
