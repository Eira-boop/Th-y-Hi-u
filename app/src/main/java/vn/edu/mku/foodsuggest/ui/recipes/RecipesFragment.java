package vn.edu.mku.foodsuggest.ui.recipes;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import vn.edu.mku.foodsuggest.R;
import vn.edu.mku.foodsuggest.data.AppDatabase;
import vn.edu.mku.foodsuggest.data.Recipe;

/**
 * Fragment hiển thị kho danh sách công thức và lọc theo danh mục / nguyên liệu
 */
public class RecipesFragment extends Fragment {

    private static final String ARG_FILTER = "ingredient_filter";

    private EditText editSearchRecipe;
    private TextView textCurrentIngredientFilter;
    private RecyclerView recyclerRecipes;
    private ChipGroup chipGroupCategories;

    private RecipeRvAdapter adapter;
    private final List<Recipe> allRecipesList = new ArrayList<>();
    private final List<Recipe> displayedRecipesList = new ArrayList<>();
    private String initialFilter = "";
    private String selectedCategory = "Tất cả";

    public static RecipesFragment newInstance(String ingredientFilter) {
        RecipesFragment fragment = new RecipesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILTER, ingredientFilter);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            initialFilter = getArguments().getString(ARG_FILTER, "");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_recipes, container, false);

        findViews(root);
        setupRecyclerView();
        setupSearch();
        setupCategoryChips();
        loadRecipes();

        return root;
    }

    private void findViews(View root) {
        editSearchRecipe = root.findViewById(R.id.editSearchRecipe);
        textCurrentIngredientFilter = root.findViewById(R.id.textCurrentIngredientFilter);
        recyclerRecipes = root.findViewById(R.id.recyclerRecipes);
        chipGroupCategories = root.findViewById(R.id.chipGroupCategories);
    }

    private void setupRecyclerView() {
        recyclerRecipes.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RecipeRvAdapter(displayedRecipesList, recipe -> {
            RecipeDetailBottomSheetDialog dialog = new RecipeDetailBottomSheetDialog(recipe);
            dialog.show(getChildFragmentManager(), "recipe_detail");
        });
        recyclerRecipes.setAdapter(adapter);

        if (!initialFilter.isEmpty()) {
            textCurrentIngredientFilter.setText("Gợi ý các món ăn có nguyên liệu: " + initialFilter);
            adapter.setIngredientFilter(initialFilter);
        }
    }

    private void setupSearch() {
        editSearchRecipe.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRecipes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupCategoryChips() {
        if (chipGroupCategories != null) {
            chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.isEmpty()) {
                    selectedCategory = "Tất cả";
                } else {
                    int checkedId = checkedIds.get(0);
                    Chip chip = group.findViewById(checkedId);
                    if (chip != null) {
                        selectedCategory = chip.getText().toString();
                    } else {
                        selectedCategory = "Tất cả";
                    }
                }
                filterRecipes(editSearchRecipe.getText().toString());
            });
        }
    }

    private void filterRecipes(String query) {
        displayedRecipesList.clear();
        String q = query.trim().toLowerCase();

        for (Recipe r : allRecipesList) {
            boolean matchesQuery = q.isEmpty()
                    || r.title.toLowerCase().contains(q)
                    || r.requiredIngredients.toLowerCase().contains(q)
                    || r.category.toLowerCase().contains(q);

            boolean matchesInitial = initialFilter.isEmpty()
                    || r.requiredIngredients.toLowerCase().contains(initialFilter.toLowerCase())
                    || r.title.toLowerCase().contains(initialFilter.toLowerCase());

            boolean matchesCategory = selectedCategory.equals("Tất cả")
                    || r.category.toLowerCase().contains(selectedCategory.toLowerCase())
                    || (selectedCategory.equals("Món Mặn") && !r.category.toLowerCase().contains("canh") && !r.category.toLowerCase().contains("chay"));

            if (matchesQuery && matchesCategory && (q.length() > 0 || matchesInitial)) {
                displayedRecipesList.add(r);
            }
        }

        // Nếu không có món nào thỏa mãn và query rỗng
        if (displayedRecipesList.isEmpty() && !allRecipesList.isEmpty() && q.isEmpty() && selectedCategory.equals("Tất cả")) {
            displayedRecipesList.addAll(allRecipesList);
            textCurrentIngredientFilter.setText("Hiển thị tất cả món ăn trong kho công thức");
        }

        adapter.notifyDataSetChanged();
    }

    private void loadRecipes() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Recipe> list = AppDatabase.getInstance(requireContext()).recipeDao().getAll();

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    allRecipesList.clear();
                    if (list != null) {
                        allRecipesList.addAll(list);
                    }
                    filterRecipes(editSearchRecipe.getText().toString());
                });
            }
        });
    }
}
