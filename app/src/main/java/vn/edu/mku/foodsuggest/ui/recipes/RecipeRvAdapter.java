package vn.edu.mku.foodsuggest.ui.recipes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

import vn.edu.mku.foodsuggest.R;
import vn.edu.mku.foodsuggest.data.Recipe;

/**
 * Adapter hiển thị danh sách các món ăn gợi ý
 */
public class RecipeRvAdapter extends RecyclerView.Adapter<RecipeRvAdapter.ViewHolder> {

    private final List<Recipe> recipes;
    private final OnRecipeClickListener listener;
    private String currentIngredientFilter = "";

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    public RecipeRvAdapter(List<Recipe> recipes, OnRecipeClickListener listener) {
        this.recipes = recipes;
        this.listener = listener;
    }

    public void setIngredientFilter(String filter) {
        this.currentIngredientFilter = filter;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_recipe_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);

        holder.textTitle.setText(recipe.title);
        holder.textCategory.setText(recipe.category);
        holder.textCookingTime.setText(recipe.cookingTime + " phút");
        holder.textCalories.setText(recipe.calories + " kcal");
        holder.textIngredients.setText("Nguyên liệu chính: " + recipe.requiredIngredients);

        // Hiển thị badge độ phù hợp
        if (!currentIngredientFilter.isEmpty() && recipe.requiredIngredients.toLowerCase().contains(currentIngredientFilter.toLowerCase())) {
            holder.textMatchBadge.setText("Khớp nguyên liệu");
            holder.textMatchBadge.setVisibility(View.VISIBLE);
        } else {
            holder.textMatchBadge.setVisibility(View.GONE);
        }

        View.OnClickListener clickAction = v -> {
            if (listener != null) {
                listener.onRecipeClick(recipe);
            }
        };

        holder.itemView.setOnClickListener(clickAction);
        holder.buttonViewDetail.setOnClickListener(clickAction);
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textCategory, textCookingTime, textCalories, textIngredients, textMatchBadge;
        MaterialButton buttonViewDetail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textRecipeTitle);
            textCategory = itemView.findViewById(R.id.textRecipeCategory);
            textCookingTime = itemView.findViewById(R.id.textCookingTime);
            textCalories = itemView.findViewById(R.id.textCalories);
            textIngredients = itemView.findViewById(R.id.textIngredients);
            textMatchBadge = itemView.findViewById(R.id.textMatchBadge);
            buttonViewDetail = itemView.findViewById(R.id.buttonViewDetail);
        }
    }
}
