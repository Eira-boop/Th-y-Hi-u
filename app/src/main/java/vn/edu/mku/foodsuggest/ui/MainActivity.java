package vn.edu.mku.foodsuggest.ui;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import vn.edu.mku.foodsuggest.R;
import vn.edu.mku.foodsuggest.ui.history.HistoryFragment;
import vn.edu.mku.foodsuggest.ui.recipes.RecipesFragment;
import vn.edu.mku.foodsuggest.ui.scan.ScanFragment;
import vn.edu.mku.foodsuggest.ui.settings.SettingsFragment;

/**
 * Activity chính chứa BottomNavigationView điều hướng 4 Fragment (Chương 1.2.4)
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        setupListeners();

        // Mặc định chọn màn hình Quét ảnh (ScanFragment)
        if (savedInstanceState == null) {
            replaceFragment(new ScanFragment());
        }
    }

    private void findViews() {
        bottomNav = findViewById(R.id.bottomNav);
    }

    private void setupListeners() {
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.nav_scan) {
                    replaceFragment(new ScanFragment());
                    return true;
                } else if (id == R.id.nav_history) {
                    replaceFragment(new HistoryFragment());
                    return true;
                } else if (id == R.id.nav_recipes) {
                    replaceFragment(new RecipesFragment());
                    return true;
                } else if (id == R.id.nav_settings) {
                    replaceFragment(new SettingsFragment());
                    return true;
                }
                return false;
            }
        });
    }

    public void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    /**
     * Mở tab gợi ý món ăn và tự động lọc theo nguyên liệu vừa nhận diện
     */
    public void openRecipesWithFilter(String ingredient) {
        bottomNav.setSelectedItemId(R.id.nav_recipes);
        RecipesFragment fragment = RecipesFragment.newInstance(ingredient);
        replaceFragment(fragment);
    }
}
