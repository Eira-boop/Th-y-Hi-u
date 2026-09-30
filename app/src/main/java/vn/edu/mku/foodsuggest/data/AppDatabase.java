package vn.edu.mku.foodsuggest.data;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * CSDL Room chính của ứng dụng gợi ý món ăn (Chương 3)
 */
@Database(entities = {ScanResult.class, Recipe.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String TAG = "AppDatabase";
    private static final String DATABASE_NAME = "food_suggest_db";

    public abstract ScanResultDao scanResultDao();
    public abstract RecipeDao recipeDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    )
                    .addCallback(new Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            // Nạp trước dữ liệu món ăn mẫu từ assets/recipes.json
                            Executors.newSingleThreadExecutor().execute(() -> {
                                prepopulateRecipes(context.getApplicationContext(), getInstance(context.getApplicationContext()));
                            });
                        }
                    })
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    private static void prepopulateRecipes(Context context, AppDatabase database) {
        try {
            InputStream is = context.getAssets().open("recipes.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String jsonString = new String(buffer, StandardCharsets.UTF_8);
            JSONArray array = new JSONArray(jsonString);
            List<Recipe> recipeList = new ArrayList<>();

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Recipe recipe = new Recipe();
                recipe.id = obj.getInt("id");
                recipe.title = obj.getString("title");
                recipe.category = obj.getString("category");

                JSONArray reqArray = obj.getJSONArray("requiredIngredients");
                StringBuilder reqBuilder = new StringBuilder();
                for (int j = 0; j < reqArray.length(); j++) {
                    reqBuilder.append(reqArray.getString(j));
                    if (j < reqArray.length() - 1) reqBuilder.append(", ");
                }
                recipe.requiredIngredients = reqBuilder.toString();
                recipe.allIngredients = obj.getString("allIngredients");
                recipe.instructions = obj.getString("instructions");
                recipe.cookingTime = obj.getInt("cookingTime");
                recipe.difficulty = obj.getString("difficulty");
                recipe.calories = obj.getInt("calories");
                recipe.nutritionNote = obj.optString("nutritionNote", "");

                recipeList.add(recipe);
            }

            database.recipeDao().insertAll(recipeList);
            Log.d(TAG, "Đã nạp thành công " + recipeList.size() + " công thức món ăn vào Room DB.");
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi nạp công thức món ăn mẫu: ", e);
        }
    }
}
