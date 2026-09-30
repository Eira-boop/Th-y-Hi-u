
package vn.edu.mku.foodsuggest.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import vn.edu.mku.foodsuggest.R;

/**
 * Màn hình chào (SplashActivity)
 */
public class SplashActivity extends AppCompatActivity {

    // Thời gian hiển thị Splash
    private static final long SPLASH_DELAY = 1000L;

    private Handler handler;
    private Runnable startMainRunnable;

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

        handler.postDelayed(startMainRunnable, SPLASH_DELAY);
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

