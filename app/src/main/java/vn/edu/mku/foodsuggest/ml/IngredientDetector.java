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
 *
 * Nếu model TFLite hợp lệ:
 * - Sử dụng TensorFlow Lite ObjectDetector.
 *
 * Nếu model không hợp lệ/chưa có:
 * - Không làm crash ứng dụng.
 * - Tự động chuyển sang chế độ demo bounding box.
 */
public class IngredientDetector {

    private static final String TAG = "IngredientDetector";

    private ObjectDetector detector;

    public IngredientDetector(Context context) throws IOException {
        initializeDetector(context);
    }

    /**
     * Khởi tạo TensorFlow Lite ObjectDetector.
     *
     * Model được lấy từ:
     * app/src/main/assets/ssd_mobilenet_v1.tflite
     *
     * Nếu model không hợp lệ, detector sẽ được đặt là null
     * và ứng dụng sẽ sử dụng chế độ demo.
     */
    private void initializeDetector(Context context) {

        detector = null;

        try {

            ObjectDetector.ObjectDetectorOptions options =
                    ObjectDetector.ObjectDetectorOptions.builder()
                            .setMaxResults(8)
                            .setScoreThreshold(0.5f)
                            .build();

            detector = ObjectDetector.createFromFileAndOptions(
                    context,
                    "ssd_mobilenet_v1.tflite",
                    options
            );

            Log.d(
                    TAG,
                    "TFLite ObjectDetector đã khởi tạo thành công."
            );

        } catch (Exception e) {

            /*
             * Không throw lỗi ra ngoài.
             *
             * Lỗi hiện tại:
             * Invalid model version 0
             *
             * nghĩa là model không hợp lệ hoặc không tương thích
             * với TensorFlow Lite Task Vision.
             *
             * Khi đó detector = null và detect() sẽ dùng
             * bounding box demo.
             */
            detector = null;

            Log.e(
                    TAG,
                    "Không thể khởi tạo TFLite ObjectDetector. " +
                            "Ứng dụng sẽ chuyển sang chế độ demo.",
                    e
            );
        }
    }

    /**
     * Nhận dạng nhiều vật thể trên ảnh.
     *
     * Nếu model hoạt động:
     * -> trả về kết quả thật từ TFLite.
     *
     * Nếu model lỗi/chưa có:
     * -> trả về bounding box demo.
     */
    public List<Detection> detect(Bitmap bitmap) {

        if (bitmap == null) {
            return new ArrayList<>();
        }

        /*
         * Nếu model TFLite đã khởi tạo thành công
         * thì sử dụng model thật.
         */
        if (detector != null) {

            try {

                TensorImage tensorImage =
                        TensorImage.fromBitmap(bitmap);

                List<Detection> detections =
                        detector.detect(tensorImage);

                if (detections != null) {

                    Log.d(
                            TAG,
                            "TFLite phát hiện "
                                    + detections.size()
                                    + " vật thể."
                    );

                    return detections;
                }

            } catch (Exception e) {

                /*
                 * Nếu model bị lỗi trong lúc detect,
                 * không để ứng dụng crash.
                 */
                Log.e(
                        TAG,
                        "Lỗi khi chạy TFLite ObjectDetector. " +
                                "Chuyển sang chế độ demo.",
                        e
                );

                try {
                    detector.close();
                } catch (Exception closeException) {
                    Log.w(
                            TAG,
                            "Không thể đóng ObjectDetector.",
                            closeException
                    );
                }

                detector = null;
            }
        }

        /*
         * Không có model hợp lệ:
         * sử dụng bounding box demo.
         */
        return generateDemoDetections(bitmap);
    }

    /**
     * Tạo bounding box demo để kiểm thử giao diện
     * và chức năng chạm vào từng vật thể.
     *
     * Đây KHÔNG phải kết quả nhận diện AI thật.
     */
    private List<Detection> generateDemoDetections(Bitmap bitmap) {

        List<Detection> list = new ArrayList<>();

        if (bitmap == null) {
            return list;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= 0 || height <= 0) {
            return list;
        }

        /*
         * Vùng demo số 1:
         * phía bên trái, khu vực giữa ảnh.
         */
        RectF box1 = new RectF(
                width * 0.15f,
                height * 0.25f,
                width * 0.48f,
                height * 0.65f
        );

        /*
         * Vùng demo số 2:
         * phía bên phải, khu vực giữa ảnh.
         */
        RectF box2 = new RectF(
                width * 0.52f,
                height * 0.28f,
                width * 0.85f,
                height * 0.68f
        );

        list.add(
                Detection.create(
                        box1,
                        new ArrayList<>()
                )
        );

        list.add(
                Detection.create(
                        box2,
                        new ArrayList<>()
                )
        );

        Log.d(
                TAG,
                "Đang sử dụng chế độ demo: "
                        + list.size()
                        + " bounding boxes."
        );

        return list;
    }

    /**
     * Kiểm tra model TFLite có được khởi tạo thành công
     * hay không.
     */
    public boolean isModelAvailable() {
        return detector != null;
    }

    /**
     * Đóng ObjectDetector và giải phóng tài nguyên.
     */
    public void close() {

        if (detector != null) {

            try {
                detector.close();
            } catch (Exception e) {

                Log.w(
                        TAG,
                        "Lỗi khi đóng ObjectDetector.",
                        e
                );
            }

            detector = null;
        }
    }
}
