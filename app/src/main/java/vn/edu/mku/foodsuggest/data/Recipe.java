package vn.edu.mku.foodsuggest.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity lưu kho công thức món ăn vào Room Database
 */
@Entity(tableName = "recipes")
public class Recipe {

    @PrimaryKey
    public int id;

    public String title;                // Tên món ăn
    public String category;             // Món canh, món xào, món mặn, món chay...
    public String requiredIngredients;  // Chuỗi các nguyên liệu bắt buộc (cách nhau dấu phẩy)
    public String allIngredients;       // Toàn bộ nguyên liệu định lượng chi tiết
    public String instructions;         // Các bước nấu chi tiết
    public int cookingTime;             // Thời gian nấu (phút)
    public String difficulty;           // Dễ, Trung bình, Nâng cao
    public int calories;                // Calo (kcal)
    public String nutritionNote;        // Lợi ích dinh dưỡng

    public Recipe() {
    }

    public Recipe(int id, String title, String category, String requiredIngredients, String allIngredients, String instructions, int cookingTime, String difficulty, int calories, String nutritionNote) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.requiredIngredients = requiredIngredients;
        this.allIngredients = allIngredients;
        this.instructions = instructions;
        this.cookingTime = cookingTime;
        this.difficulty = difficulty;
        this.calories = calories;
        this.nutritionNote = nutritionNote;
    }
}
