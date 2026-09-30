package vn.edu.mku.foodsuggest.ui.recipes;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import vn.edu.mku.foodsuggest.R;
import vn.edu.mku.foodsuggest.data.Recipe;

/**
 * Bottom Sheet hiển thị chi tiết công thức nấu ăn, định lượng và mẹo dinh dưỡng
 */
public class RecipeDetailBottomSheetDialog extends BottomSheetDialogFragment {

    private final Recipe recipe;

    public RecipeDetailBottomSheetDialog(Recipe recipe) {
        this.recipe = recipe;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_recipe_detail, container, false);

        TextView title = view.findViewById(R.id.dialogTitle);
        TextView meta = view.findViewById(R.id.dialogMeta);
        TextView ingredients = view.findViewById(R.id.dialogIngredients);
        TextView instructions = view.findViewById(R.id.dialogInstructions);
        MaterialButton closeBtn = view.findViewById(R.id.dialogButtonClose);

        if (recipe != null) {
            title.setText(recipe.title);
            meta.setText(String.format("%s • %d phút • Độ khó: %s • %d kcal",
                    recipe.category, recipe.cookingTime, recipe.difficulty, recipe.calories));
            ingredients.setText(recipe.allIngredients);
            instructions.setText(recipe.instructions + (recipe.nutritionNote != null && !recipe.nutritionNote.isEmpty()
                    ? "\n\n💡 Lợi ích sức khỏe: " + recipe.nutritionNote
                    : ""));
        }

        closeBtn.setOnClickListener(v -> dismiss());

        return view;
    }
}
