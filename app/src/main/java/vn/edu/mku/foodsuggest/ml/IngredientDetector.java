package vn.edu.mku.foodsuggest.ml;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Nhận dạng nhiều nguyên liệu đồng thời trên một khung hình.
 * Sử dụng model SSD MobileNet V2 (EfficientDet Lite0).
 */
public class IngredientDetector {

    private static final String TAG = "IngredientDetector";
    private ObjectDetector detector;

    public IngredientDetector(Context context) throws IOException {
        initializeDetector(context);
    }

    private void initializeDetector(Context context) {
        detector = null;
        try {
            ObjectDetector.ObjectDetectorOptions options =
                    ObjectDetector.ObjectDetectorOptions.builder()
                            .setMaxResults(10)          // Tối đa 10 vật thể
                            .setScoreThreshold(0.35f)   // Hạ ngưỡng xuống 35% để bắt được nhiều vật thể hơn
                            .build();

            // ĐỔI TÊN MODEL TẠI ĐÂY (Khớp với file bạn tải từ Colab)
            detector = ObjectDetector.createFromFileAndOptions(
                    context,
                    "ssd_mobilenet_v2.tflite",
                    options
            );

            Log.d(TAG, "TFLite ObjectDetector (MobileNetV2) đã khởi tạo thành công.");

        } catch (Exception e) {
            detector = null;
            Log.e(TAG, "Không thể khởi tạo TFLite ObjectDetector. Sẽ dùng chế độ demo.", e);
        }
    }

    public List<Detection> detect(Bitmap bitmap) {
        if (bitmap == null) {
            return new ArrayList<>();
        }

        if (detector != null) {
            try {
                TensorImage tensorImage = TensorImage.fromBitmap(bitmap);
                List<Detection> detections = detector.detect(tensorImage);

                if (detections != null) {
                    Log.d(TAG, "TFLite phát hiện " + detections.size() + " vật thể.");
                    return detections;
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi chạy TFLite ObjectDetector. Chuyển sang chế độ demo.", e);
                try { detector.close(); } catch (Exception ignored) {}
                detector = null;
            }
        }

        // Chế độ demo (chỉ chạy nếu file model bị lỗi hoặc chưa copy vào assets)
        return generateDemoDetections(bitmap);
    }

    private List<Detection> generateDemoDetections(Bitmap bitmap) {
        List<Detection> list = new ArrayList<>();
        if (bitmap == null) return list;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width <= 0 || height <= 0) return list;

        RectF box1 = new RectF(width * 0.15f, height * 0.25f, width * 0.48f, height * 0.65f);
        RectF box2 = new RectF(width * 0.52f, height * 0.28f, width * 0.85f, height * 0.68f);

        // Tạo box rỗng (không có label) để ScanFragment xử lý
        list.add(Detection.create(box1, new ArrayList<>()));
        list.add(Detection.create(box2, new ArrayList<>()));

        Log.d(TAG, "Đang sử dụng chế độ demo: " + list.size() + " bounding boxes.");
        return list;
    }

    public boolean isModelAvailable() {
        return detector != null;
    }

    public void close() {
        if (detector != null) {
            try { detector.close(); } catch (Exception e) { Log.w(TAG, "Lỗi khi đóng ObjectDetector.", e); }
            detector = null;
        }
    }
}