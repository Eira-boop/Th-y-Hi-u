
package vn.edu.mku.foodsuggest.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import vn.edu.mku.foodsuggest.R;

/**
 * Màn hình chào (SplashActivity)
 * Khởi động đầu tiên để xin cấp quyền Camera
 */
public class SplashActivity extends AppCompatActivity {

    // Thời gian hiển thị Splash
    private static final long SPLASH_DELAY = 1000L;

    private Handler handler;
    private Runnable startMainRunnable;

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        startMain();
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        handler = new Handler(Looper.getMainLooper());

        startMainRunnable = new Runnable() {
            @Override
            public void run() {
                startMain();
            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            handler.postDelayed(startMainRunnable, SPLASH_DELAY);
        } else {
            // Xin cấp quyền Camera ngay tại màn hình khởi động
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startMain() {
        // Không thực hiện nếu Activity đã bị đóng
        if (isFinishing() || isDestroyed()) {
            return;
        }

        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Hủy callback của Handler để tránh chạy sau khi Activity bị destroy
        if (handler != null && startMainRunnable != null) {
            handler.removeCallbacks(startMainRunnable);
        }
    }
}

