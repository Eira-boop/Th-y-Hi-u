package vn.edu.mku.foodsuggest.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;
import java.util.concurrent.Executors;

import vn.edu.mku.foodsuggest.R;
import vn.edu.mku.foodsuggest.data.AppDatabase;
import vn.edu.mku.foodsuggest.data.Recipe;
import vn.edu.mku.foodsuggest.ui.MainActivity;
import vn.edu.mku.foodsuggest.ui.recipes.RecipeDetailBottomSheetDialog;

/**
 * Fragment Trang chủ: Tổng quan các tính năng, gợi ý món ngon hôm nay và mẹo vặt nhà bếp
 */
public class HomeFragment extends Fragment {

    private LinearLayout containerFeaturedRecipes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        findViews(root);
        setupNavigationListeners(root);
        loadFeaturedRecipes();

        return root;
    }

    private void findViews(View root) {
        containerFeaturedRecipes = root.findViewById(R.id.containerFeaturedRecipes);
    }

    private void setupNavigationListeners(View root) {
        View btnHeroScan = root.findViewById(R.id.btnHeroScan);
        View cardActionScan = root.findViewById(R.id.cardActionScan);
        View btnQuickAiChat = root.findViewById(R.id.btnQuickAiChat);
        View cardActionAi = root.findViewById(R.id.cardActionAi);
        View cardActionRecipes = root.findViewById(R.id.cardActionRecipes);
        View textHomeViewAllRecipes = root.findViewById(R.id.textHomeViewAllRecipes);

        View.OnClickListener goToScan = v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab(R.id.nav_scan);
            }
        };

        View.OnClickListener goToAi = v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab(R.id.nav_ai);
            }
        };

        View.OnClickListener goToRecipes = v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab(R.id.nav_recipes);
            }
        };

        if (btnHeroScan != null) btnHeroScan.setOnClickListener(goToScan);
        if (cardActionScan != null) cardActionScan.setOnClickListener(goToScan);

        if (btnQuickAiChat != null) btnQuickAiChat.setOnClickListener(goToAi);
        if (cardActionAi != null) cardActionAi.setOnClickListener(goToAi);

        if (cardActionRecipes != null) cardActionRecipes.setOnClickListener(goToRecipes);
        if (textHomeViewAllRecipes != null) textHomeViewAllRecipes.setOnClickListener(goToRecipes);
    }

    private void loadFeaturedRecipes() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Recipe> recipes = AppDatabase.getInstance(requireContext()).recipeDao().getAll();
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    if (recipes != null && !recipes.isEmpty()) {
                        displayFeaturedRecipes(recipes);
                    }
                });
            }
        });
    }

    private void displayFeaturedRecipes(List<Recipe> recipes) {
        containerFeaturedRecipes.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        int count = Math.min(4, recipes.size());
        for (int i = 0; i < count; i++) {
            Recipe recipe = recipes.get(i);
            View cardView = inflater.inflate(R.layout.layout_item_recipe_card, containerFeaturedRecipes, false);

            TextView textRecipeTitle = cardView.findViewById(R.id.textRecipeTitle);
            TextView textRecipeCategory = cardView.findViewById(R.id.textRecipeCategory);
            TextView textCookingTime = cardView.findViewById(R.id.textCookingTime);
            TextView textCalories = cardView.findViewById(R.id.textCalories);
            TextView textIngredients = cardView.findViewById(R.id.textIngredients);
            View buttonViewDetail = cardView.findViewById(R.id.buttonViewDetail);

            textRecipeTitle.setText(recipe.title);
            textRecipeCategory.setText(recipe.category);
            textCookingTime.setText(recipe.cookingTime + " phút");
            textCalories.setText(recipe.calories + " kcal");
            textIngredients.setText("Nguyên liệu chính: " + recipe.requiredIngredients);

            View.OnClickListener clickListener = v -> {
                RecipeDetailBottomSheetDialog dialog = new RecipeDetailBottomSheetDialog(recipe);
                dialog.show(getChildFragmentManager(), "home_recipe_detail");
            };

            cardView.setOnClickListener(clickListener);
            if (buttonViewDetail != null) {
                buttonViewDetail.setOnClickListener(clickListener);
            }

            containerFeaturedRecipes.addView(cardView);
        }
    }
}
