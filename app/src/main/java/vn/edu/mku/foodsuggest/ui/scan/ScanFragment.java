package vn.edu.mku.foodsuggest.ui.scan;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.task.vision.detector.Detection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vn.edu.mku.foodsuggest.R;
import vn.edu.mku.foodsuggest.data.AppDatabase;
import vn.edu.mku.foodsuggest.data.ScanResult;
import vn.edu.mku.foodsuggest.ml.IngredientClassifier;
import vn.edu.mku.foodsuggest.ml.IngredientDetector;
import vn.edu.mku.foodsuggest.ui.MainActivity;
import vn.edu.mku.foodsuggest.utils.ImageUtils;
import vn.edu.mku.foodsuggest.utils.RecipeRecommendationEngine;

/**
 * ScanFragment:
 *
 * - Chụp ảnh bằng CameraX.
 * - Tiền xử lý ảnh bằng OpenCV.
 * - Phân loại nguyên liệu bằng TFLite.
 * - Nhận dạng nhiều vật thể bằng TFLite ObjectDetector.
 * - Gợi ý món ăn.
 *
 * Nếu ObjectDetector chưa có model hợp lệ,
 * ứng dụng vẫn chạy bằng chế độ demo bounding box.
 */
public class ScanFragment extends Fragment {

    private static final String TAG = "ScanFragment";

    private static final float DEFAULT_CONFIDENCE_THRESHOLD = 0.50f;

    private PreviewView previewView;
    private MaterialButton buttonCapture;
    private MaterialButton buttonMultiMode;
    private ImageView imageResult;
    private TextView textMultiHint;
    private LinearLayout resultInfoPanel;
    private View colorBadge;
    private TextView textLabel;
    private TextView textConfidence;
    private TextView textGuide;
    private TextView textCloseHint;
    private MaterialButton buttonViewRecipes;

    private ImageCapture imageCapture;

    private IngredientClassifier classifier;
    private IngredientDetector detector;

    private boolean multiMode = false;

    private Bitmap multiSourceBitmap;

    private List<Detection> multiDetections =
            Collections.emptyList();

    private String currentDetectedIngredient = "";

    private ExecutorService executorService;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(
                R.layout.fragment_scan,
                container,
                false
        );

        executorService = Executors.newSingleThreadExecutor();

        findViews(root);

        initMachineLearning();

        startCamera();

        setupListeners();

        return root;
    }

    /**
     * Tìm các View trong fragment_scan.xml.
     */
    private void findViews(View root) {

        previewView =
                root.findViewById(R.id.previewView);

        buttonCapture =
                root.findViewById(R.id.buttonCapture);

        buttonMultiMode =
                root.findViewById(R.id.buttonMultiMode);

        imageResult =
                root.findViewById(R.id.imageResult);

        textMultiHint =
                root.findViewById(R.id.textMultiHint);

        resultInfoPanel =
                root.findViewById(R.id.resultInfoPanel);

        colorBadge =
                root.findViewById(R.id.colorBadge);

        textLabel =
                root.findViewById(R.id.textLabel);

        textConfidence =
                root.findViewById(R.id.textConfidence);

        textGuide =
                root.findViewById(R.id.textGuide);

        textCloseHint =
                root.findViewById(R.id.textCloseHint);

        buttonViewRecipes =
                root.findViewById(R.id.buttonViewRecipes);
    }

    /**
     * Khởi tạo các model AI.
     *
     * IngredientClassifier:
     * - Khởi tạo ngay vì dùng cho chế độ nhận diện đơn.
     *
     * IngredientDetector:
     * - KHÔNG khởi tạo ở đây.
     * - Chỉ khởi tạo khi người dùng bật chế độ nhiều vật thể.
     *
     * Điều này tránh lỗi model ObjectDetector làm crash
     * ScanFragment ngay khi mở màn hình.
     */
    private void initMachineLearning() {

        try {

            classifier =
                    new IngredientClassifier(
                            requireContext()
                    );

            Log.d(
                    TAG,
                    "IngredientClassifier đã khởi tạo."
            );

        } catch (IOException e) {

            Log.e(
                    TAG,
                    "Không thể khởi tạo IngredientClassifier.",
                    e
            );

            classifier = null;
        }

        /*
         * Không khởi tạo IngredientDetector tại đây.
         *
         * Model:
         * ssd_mobilenet_v1.tflite
         *
         * sẽ chỉ được mở khi bật Multi Mode.
         */
        detector = null;
    }

    /**
     * Lấy ngưỡng confidence được lưu trong SharedPreferences.
     */
    private float getConfidenceThreshold() {

        if (getContext() == null) {
            return DEFAULT_CONFIDENCE_THRESHOLD;
        }

        SharedPreferences prefs =
                getContext().getSharedPreferences(
                        "smartcook_prefs",
                        Context.MODE_PRIVATE
                );

        return prefs.getInt(
                "confidence_threshold",
                50
        ) / 100.0f;
    }

    /**
     * Thiết lập sự kiện cho các nút.
     */
    private void setupListeners() {

        /*
         * Nút chụp ảnh.
         */
        buttonCapture.setOnClickListener(
                v -> takePhoto()
        );

        /*
         * Nút bật/tắt chế độ nhiều vật thể.
         */
        buttonMultiMode.setOnClickListener(v -> {

            multiMode = !multiMode;

            if (multiMode) {

                buttonMultiMode.setText(
                        R.string.multi_mode_on
                );

                buttonMultiMode.setBackgroundTintList(
                        ContextCompat.getColorStateList(
                                requireContext(),
                                R.color.secondary
                        )
                );

                /*
                 * Chỉ khởi tạo ObjectDetector khi
                 * người dùng thực sự bật Multi Mode.
                 */
                initializeMultiObjectDetector();

            } else {

                buttonMultiMode.setText(
                        R.string.multi_mode_off
                );

                buttonMultiMode.setBackgroundTintList(
                        ContextCompat.getColorStateList(
                                requireContext(),
                                R.color.black
                        )
                );
            }
        });

        /*
         * Chạm vào ảnh kết quả:
         * nếu không ở Multi Mode thì quay lại camera.
         */
        imageResult.setOnClickListener(v -> {

            if (!multiMode) {
                hideResult();
            }
        });

        /*
         * Chạm vào từng bounding box ở Multi Mode.
         */

        imageResult.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(
                            View v,
                            MotionEvent event) {

                        if (multiMode
                                && multiDetections != null
                                && event.getAction()
                                == MotionEvent.ACTION_DOWN) {

                            boolean handled =
                                    handleMultiTap(
                                            event.getX(),
                                            event.getY()
                                    );

                            if (!handled) {
                                hideResult();
                            }

                            return true;
                        }

                        return false;
                    }
                }
        );


        /*
         * Nút xem công thức món ăn.
         */
        buttonViewRecipes.setOnClickListener(v -> {

            if (!currentDetectedIngredient.isEmpty()
                    && getActivity() instanceof MainActivity) {

                ((MainActivity) getActivity())
                        .openRecipesWithFilter(
                                currentDetectedIngredient
                        );
            }
        });
    }

    /**
     * Khởi tạo ObjectDetector khi bật Multi Mode.
     *
     * Nếu model lỗi:
     * IngredientDetector tự động chuyển sang demo.
     */
    private void initializeMultiObjectDetector() {

        if (detector != null) {
            return;
        }

        try {

            detector =
                    new IngredientDetector(
                            requireContext()
                    );

            Log.d(
                    TAG,
                    "IngredientDetector đã được khởi tạo."
            );

            /*
             * Nếu model không khả dụng,
             * vẫn cho phép chạy bằng demo.
             */
            if (!detector.isModelAvailable()) {

                Log.w(
                        TAG,
                        "Model ObjectDetector không khả dụng. " +
                                "Multi Mode sẽ sử dụng bounding box demo."
                );

                if (isAdded()) {

                    Toast.makeText(
                            requireContext(),
                            "Model nhận diện nhiều vật thể chưa hợp lệ. Đang sử dụng chế độ demo.",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

        } catch (IOException e) {

            Log.e(
                    TAG,
                    "Không thể khởi tạo IngredientDetector.",
                    e
            );

            detector = null;

            if (isAdded()) {

                Toast.makeText(
                        requireContext(),
                        "Không thể khởi tạo nhận diện nhiều vật thể.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    /**
     * Khởi động CameraX.
     */
    private void startCamera() {

        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(
                        requireContext()
                );

        future.addListener(
                () -> {

                    try {

                        ProcessCameraProvider cameraProvider =
                                future.get();

                        Preview preview =
                                new Preview.Builder()
                                        .build();

                        preview.setSurfaceProvider(
                                previewView.getSurfaceProvider()
                        );

                        imageCapture =
                                new ImageCapture.Builder()
                                        .setCaptureMode(
                                                ImageCapture
                                                        .CAPTURE_MODE_MINIMIZE_LATENCY
                                        )
                                        .build();

                        cameraProvider.unbindAll();

                        cameraProvider.bindToLifecycle(
                                this,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageCapture
                        );

                    } catch (
                            ExecutionException
                            | InterruptedException e) {

                        Log.e(
                                TAG,
                                "Lỗi kết nối camera.",
                                e
                        );
                    }
                },
                ContextCompat.getMainExecutor(
                        requireContext()
                )
        );
    }

    /**
     * Chụp ảnh.
     */
    private void takePhoto() {

        if (imageCapture == null) {

            Toast.makeText(
                    requireContext(),
                    "Camera chưa sẵn sàng.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        File picturesDir =
                new File(
                        Environment
                                .getExternalStoragePublicDirectory(
                                        Environment.DIRECTORY_PICTURES
                                ),
                        "SmartCook"
                );

        if (!picturesDir.exists()) {
            picturesDir.mkdirs();
        }

        File photoFile =
                new File(
                        picturesDir,
                        "scan_"
                                + System.currentTimeMillis()
                                + ".jpg"
                );

        ImageCapture.OutputFileOptions options =
                new ImageCapture.OutputFileOptions.Builder(
                        photoFile
                ).build();

        imageCapture.takePicture(
                options,
                ContextCompat.getMainExecutor(
                        requireContext()
                ),
                new ImageCapture.OnImageSavedCallback() {

                    @Override
                    public void onImageSaved(
                            @NonNull ImageCapture.OutputFileResults outputFileResults) {

                        if (multiMode) {

                            processMultiMode(
                                    photoFile
                            );

                        } else {

                            processAndSave(
                                    photoFile
                            );
                        }
                    }

                    @Override
                    public void onError(
                            @NonNull ImageCaptureException exception) {

                        Log.e(
                                TAG,
                                "Lỗi khi chụp ảnh.",
                                exception
                        );

                        if (isAdded()) {

                            Toast.makeText(
                                    requireContext(),
                                    "Lỗi chụp ảnh: "
                                            + exception.getMessage(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                }
        );
    }

    /**
     * Luồng xử lý một vật thể.
     */
    private void processAndSave(File photoFile) {

        if (executorService == null
                || executorService.isShutdown()) {
            return;
        }

        executorService.execute(() -> {

            Bitmap original =
                    BitmapFactory.decodeFile(
                            photoFile.getAbsolutePath()
                    );

            if (original == null) {
                return;
            }

            /*
             * Bước 1:
             * Tăng cường chất lượng ảnh.
             */
            Bitmap enhanced =
                    ImageUtils.enhance(original);

            /*
             * Bước 2:
             * Vẽ khung nhận diện.
             */
            Bitmap debugBox =
                    ImageUtils.drawMainObjectBox(
                            enhanced
                    );

            /*
             * Bước 3:
             * Cắt nguyên liệu chính.
             */
            Bitmap processed =
                    ImageUtils.cropMainObject(
                            enhanced
                    );

            /*
             * Bước 4:
             * Lưu ảnh processed.
             */
            try (
                    FileOutputStream out =
                            new FileOutputStream(
                                    photoFile
                            )
            ) {

                processed.compress(
                        Bitmap.CompressFormat.JPEG,
                        90,
                        out
                );

            } catch (IOException e) {

                Log.e(
                        TAG,
                        "Lỗi lưu ảnh processed.",
                        e
                );
            }

            /*
             * Bước 5:
             * Phân loại bằng AI.
             */
            IngredientClassifier.ClassificationResult result =
                    null;

            if (classifier != null) {

                result =
                        classifier.classify(
                                processed
                        );

                float threshold =
                        getConfidenceThreshold();

                if (result != null
                        && result.confidence >= threshold) {

                    saveScanResult(
                            result,
                            photoFile.getAbsolutePath()
                    );
                }
            }

            /*
             * Hiển thị kết quả.
             */
            showResult(
                    debugBox,
                    result
            );
        });
    }

    /**
     * Lưu kết quả nhận diện vào Room Database.
     */
    private void saveScanResult(
            IngredientClassifier.ClassificationResult result,
            String imagePath) {

        if (result == null
                || getContext() == null) {
            return;
        }

        try {

            ScanResult record =
                    new ScanResult();

            record.label =
                    result.label;

            record.confidence =
                    result.confidence;

            record.category =
                    result.category;

            record.imagePath =
                    imagePath;

            record.timestamp =
                    System.currentTimeMillis();

            record.suggestedDishes =
                    RecipeRecommendationEngine
                            .getSuggestedDishesFormatted(
                                    result.label
                            );

            record.nutritionTip =
                    result.specialGuidance;

            AppDatabase
                    .getInstance(requireContext())
                    .scanResultDao()
                    .insert(record);

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Lỗi lưu kết quả ScanResult.",
                    e
            );
        }
    }

    /**
     * Luồng xử lý nhiều vật thể.
     */
    private void processMultiMode(File photoFile) {

        if (executorService == null
                || executorService.isShutdown()) {
            return;
        }

        executorService.execute(() -> {

            Bitmap original =
                    BitmapFactory.decodeFile(
                            photoFile.getAbsolutePath()
                    );

            if (original == null) {
                return;
            }

            /*
             * Tăng cường chất lượng ảnh.
             */
            Bitmap enhanced =
                    ImageUtils.enhance(
                            original
                    );

            /*
             * Nếu detector có sẵn thì chạy.
             * Nếu không thì danh sách rỗng.
             */
            List<Detection> detections;

            if (detector != null) {

                detections =
                        detector.detect(
                                enhanced
                        );

            } else {

                detections =
                        Collections.emptyList();
            }

            /*
             * Vẽ bounding box.
             */
            Bitmap withBoxes =
                    drawNumberedBoxes(
                            enhanced,
                            detections
                    );

            multiSourceBitmap =
                    enhanced;

            multiDetections =
                    detections;

            /*
             * Nếu không phát hiện vật thể.
             */
            if (isAdded()) {

                requireActivity().runOnUiThread(() -> {

                    if (detections.isEmpty()) {

                        Toast.makeText(
                                requireContext(),
                                R.string.no_items_detected,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
            }

            /*
             * Hiển thị ảnh kết quả.
             */
            showResult(
                    withBoxes,
                    null
            );
        });
    }

    /**
     * Vẽ số thứ tự lên bounding box.
     */
    private Bitmap drawNumberedBoxes(
            Bitmap source,
            List<Detection> detections) {

        if (source == null) {
            return null;
        }

        Bitmap mutable =
                source.copy(
                        Bitmap.Config.ARGB_8888,
                        true
                );

        Canvas canvas =
                new Canvas(mutable);

        Paint boxPaint =
                new Paint();

        boxPaint.setColor(
                Color.parseColor("#2E7D32")
        );

        boxPaint.setStyle(
                Paint.Style.STROKE
        );

        boxPaint.setStrokeWidth(
                6f
        );

        Paint textPaint =
                new Paint();

        textPaint.setColor(
                Color.WHITE
        );

        textPaint.setTextSize(
                40f
        );

        textPaint.setFakeBoldText(
                true
        );

        Paint textBgPaint =
                new Paint();

        textBgPaint.setColor(
                Color.parseColor("#2E7D32")
        );

        if (detections == null) {
            return mutable;
        }

        for (
                int i = 0;
                i < detections.size();
                i++
        ) {

            RectF box =
                    detections
                            .get(i)
                            .getBoundingBox();

            canvas.drawRect(
                    box,
                    boxPaint
            );

            canvas.drawRect(
                    box.left,
                    Math.max(
                            0,
                            box.top - 46
                    ),
                    box.left + 50,
                    box.top,
                    textBgPaint
            );

            canvas.drawText(
                    String.valueOf(i + 1),
                    box.left + 14,
                    box.top - 10,
                    textPaint
            );
        }

        return mutable;
    }

    /**
     * Lưu ảnh vật thể đã crop.
     */
    private File saveCroppedObject(
            Bitmap bitmap,
            int index) throws IOException {

        File dir =
                new File(
                        requireContext()
                                .getExternalFilesDir(
                                        Environment
                                                .DIRECTORY_PICTURES
                                ),
                        "SmartCook/items"
                );

        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file =
                new File(
                        dir,
                        "item_"
                                + System.currentTimeMillis()
                                + "_"
                                + index
                                + ".jpg"
                );

        try (
                FileOutputStream out =
                        new FileOutputStream(file)
        ) {

            bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    90,
                    out
            );
        }

        return file;
    }

    /**
     * Chuyển tọa độ chạm trên ImageView
     * về tọa độ Bitmap.
     */
    private PointF mapTouchToBitmap(
            float x,
            float y) {

        Matrix inverse =
                new Matrix();

        if (!imageResult
                .getImageMatrix()
                .invert(inverse)) {

            return null;
        }

        float[] points = {
                x,
                y
        };

        inverse.mapPoints(points);

        return new PointF(
                points[0],
                points[1]
        );
    }

    /**
     * Xử lý khi người dùng chạm vào
     * một bounding box.
     */
    private boolean handleMultiTap(
            float x,
            float y) {

        if (multiSourceBitmap == null
                || multiDetections == null) {

            return false;
        }

        PointF point =
                mapTouchToBitmap(
                        x,
                        y
                );

        if (point == null) {
            return false;
        }

        for (
                int i = 0;
                i < multiDetections.size();
                i++
        ) {

            RectF box =
                    multiDetections
                            .get(i)
                            .getBoundingBox();

            if (!box.contains(
                    point.x,
                    point.y
            )) {

                continue;
            }

            int left =
                    Math.max(
                            0,
                            (int) box.left
                    );

            int top =
                    Math.max(
                            0,
                            (int) box.top
                    );

            int width =
                    Math.min(
                            (int) box.width(),
                            multiSourceBitmap.getWidth()
                                    - left
                    );

            int height =
                    Math.min(
                            (int) box.height(),
                            multiSourceBitmap.getHeight()
                                    - top
                    );

            if (width <= 0
                    || height <= 0) {

                return false;
            }

            Bitmap cropped;

            try {

                cropped =
                        Bitmap.createBitmap(
                                multiSourceBitmap,
                                left,
                                top,
                                width,
                                height
                        );

            } catch (Exception e) {

                Log.e(
                        TAG,
                        "Không thể crop vật thể.",
                        e
                );

                return false;
            }

            IngredientClassifier.ClassificationResult result =
                    classifier != null
                            ? classifier.classify(cropped)
                            : null;

            textMultiHint.setVisibility(
                    View.GONE
            );

            resultInfoPanel.setVisibility(
                    View.VISIBLE
            );

            renderClassificationResult(
                    result
            );

            /*
             * Nếu confidence đạt ngưỡng thì lưu.
             */
            if (result != null
                    && result.confidence
                    >= getConfidenceThreshold()) {

                final int itemIndex =
                        i + 1;

                if (executorService != null
                        && !executorService.isShutdown()) {

                    executorService.execute(() -> {

                        try {

                            File itemFile =
                                    saveCroppedObject(
                                            cropped,
                                            itemIndex
                                    );

                            saveScanResult(
                                    result,
                                    itemFile.getAbsolutePath()
                            );

                        } catch (IOException e) {

                            Log.e(
                                    TAG,
                                    "Lỗi lưu cropped object.",
                                    e
                            );
                        }
                    });
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Hiển thị ảnh kết quả.
     */
    private void showResult(
            Bitmap bitmap,
            IngredientClassifier.ClassificationResult result) {

        if (!isAdded()
                || bitmap == null) {
            return;
        }

        requireActivity().runOnUiThread(() -> {

            if (!isAdded()) {
                return;
            }

            previewView.setVisibility(
                    View.INVISIBLE
            );

            imageResult.setImageBitmap(
                    bitmap
            );

            imageResult.setVisibility(
                    View.VISIBLE
            );

            textCloseHint.setVisibility(
                    View.VISIBLE
            );

            if (multiMode) {

                textMultiHint.setVisibility(
                        View.VISIBLE
                );

                resultInfoPanel.setVisibility(
                        View.GONE
                );

            } else {

                resultInfoPanel.setVisibility(
                        View.VISIBLE
                );

                renderClassificationResult(
                        result
                );
            }
        });
    }

    /**
     * Hiển thị kết quả phân loại.
     */
    private void renderClassificationResult(
            IngredientClassifier.ClassificationResult result) {

        if (result == null) {

            textLabel.setText(
                    "Không thể phân loại"
            );

            textConfidence.setText(
                    ""
            );

            textGuide.setText(
                    "Vui lòng chụp lại với ánh sáng rõ hơn."
            );

            colorBadge.setBackgroundColor(
                    Color.parseColor("#888888")
            );

            currentDetectedIngredient =
                    "";

            buttonViewRecipes.setVisibility(
                    View.GONE
            );

        } else if (
                result.confidence
                        < getConfidenceThreshold()
        ) {

            textLabel.setText(
                    "Chưa xác định chắc chắn ("
                            + result.label
                            + ")"
            );

            textConfidence.setText(
                    String.format(
                            Locale.getDefault(),
                            "Độ tin cậy: %.0f%% (Dưới ngưỡng lưu)",
                            result.confidence * 100
                    )
            );

            textGuide.setText(
                    "Hãy đưa camera lại gần nguyên liệu hơn " +
                            "để nhận diện chính xác."
            );

            colorBadge.setBackgroundColor(
                    Color.parseColor("#FB8C00")
            );

            currentDetectedIngredient =
                    result.label;

            buttonViewRecipes.setVisibility(
                    View.VISIBLE
            );

        } else {

            textLabel.setText(
                    result.label
            );

            textConfidence.setText(
                    String.format(
                            Locale.getDefault(),
                            "Độ tin cậy: %.0f%% • Nhóm: %s",
                            result.confidence * 100,
                            result.category
                    )
            );

            textGuide.setText(
                    String.format(
                            Locale.getDefault(),
                            "Gợi ý món ăn:\n%s\n\n" +
                                    "💡 Mẹo dinh dưỡng: %s",
                            RecipeRecommendationEngine
                                    .getSuggestedDishesFormatted(
                                            result.label
                                    ),
                            result.specialGuidance
                    )
            );

            colorBadge.setBackgroundColor(
                    Color.parseColor("#2E7D32")
            );

            currentDetectedIngredient =
                    result.label;

            buttonViewRecipes.setVisibility(
                    View.VISIBLE
            );
        }
    }

    /**
     * Ẩn kết quả và quay lại Camera.
     */
    private void hideResult() {

        imageResult.setVisibility(
                View.GONE
        );

        textCloseHint.setVisibility(
                View.GONE
        );

        textMultiHint.setVisibility(
                View.GONE
        );

        resultInfoPanel.setVisibility(
                View.GONE
        );

        previewView.setVisibility(
                View.VISIBLE
        );

        currentDetectedIngredient =
                "";
    }

    /**
     * Giải phóng tài nguyên.
     */
    @Override
    public void onDestroyView() {

        /*
         * Dừng executor.
         */
        if (executorService != null) {

            executorService.shutdownNow();
            executorService = null;
        }

        /*
         * Đóng classifier.
         */
        if (classifier != null) {

            try {
                classifier.close();
            } catch (Exception e) {

                Log.w(
                        TAG,
                        "Lỗi khi đóng IngredientClassifier.",
                        e
                );
            }

            classifier = null;
        }

        /*
         * Đóng object detector.
         */
        if (detector != null) {

            try {
                detector.close();
            } catch (Exception e) {

                Log.w(
                        TAG,
                        "Lỗi khi đóng IngredientDetector.",
                        e
                );
            }

            detector = null;
        }

        multiSourceBitmap = null;

        multiDetections =
                Collections.emptyList();

        super.onDestroyView();
    }
}
