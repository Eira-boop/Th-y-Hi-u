package vn.edu.mku.foodsuggest.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * Data Access Object cho danh sách món ăn & công thức
 */
@Dao
public interface RecipeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Recipe> recipes);

    @Query("SELECT * FROM recipes")
    List<Recipe> getAll();

    @Query("SELECT * FROM recipes WHERE id = :id LIMIT 1")
    Recipe getById(int id);

    @Query("SELECT * FROM recipes WHERE title LIKE '%' || :keyword || '%' OR requiredIngredients LIKE '%' || :keyword || '%' OR allIngredients LIKE '%' || :keyword || '%'")
    List<Recipe> search(String keyword);

    @Query("SELECT COUNT(*) FROM recipes")
    int count();
}
