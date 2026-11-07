package vn.edu.mku.foodsuggest.ui.scan;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

public class ScanFragment extends Fragment {

    private static final String TAG = "ScanFragment";
    private static final float DEFAULT_CONFIDENCE_THRESHOLD = 0.50f;

    private PreviewView previewView;
    private MaterialButton buttonCapture;
    private MaterialButton buttonMultiMode;
    private MaterialButton buttonGroupMode;

    private int ingredientGroup = IngredientClassifier.GROUP_VEGETABLE;

    private ImageView imageResult;
    private TextView textMultiHint;
    private LinearLayout resultInfoPanel;
    private View colorBadge;
    private TextView textLabel;
    private TextView textConfidence;
    private TextView textGuide;
    private TextView textCloseHint;
    private TextView textCameraHint;
    private MaterialButton buttonViewRecipes;

    private ImageCapture imageCapture;
    private IngredientClassifier classifier;
    private IngredientDetector detector;

    private boolean multiMode = false;
    private Bitmap multiSourceBitmap;
    private List<Detection> multiDetections = Collections.emptyList();
    private String currentDetectedIngredient = "";
    private ExecutorService executorService;

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            startCamera();
                        } else {
                            if (isAdded()) {
                                Toast.makeText(requireContext(), R.string.permission_required, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
            );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_scan, container, false);
        executorService = Executors.newSingleThreadExecutor();
        findViews(root);
        initMachineLearning();
        checkCameraPermissionAndStart();
        setupListeners();
        return root;
    }

    private void checkCameraPermissionAndStart() {
        if (!isAdded()) return;
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded() && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (imageCapture == null) startCamera();
        }
    }

    private void findViews(View root) {
        previewView = root.findViewById(R.id.previewView);
        buttonCapture = root.findViewById(R.id.buttonCapture);
        buttonGroupMode = root.findViewById(R.id.buttonGroupMode);
        buttonMultiMode = root.findViewById(R.id.buttonMultiMode);
        imageResult = root.findViewById(R.id.imageResult);
        textMultiHint = root.findViewById(R.id.textMultiHint);
        textCameraHint = root.findViewById(R.id.textCameraHint);
        resultInfoPanel = root.findViewById(R.id.resultInfoPanel);
        colorBadge = root.findViewById(R.id.colorBadge);
        textLabel = root.findViewById(R.id.textLabel);
        textConfidence = root.findViewById(R.id.textConfidence);
        textGuide = root.findViewById(R.id.textGuide);
        textCloseHint = root.findViewById(R.id.textCloseHint);
        buttonViewRecipes = root.findViewById(R.id.buttonViewRecipes);
    }

    private void initMachineLearning() {
        try {
            classifier = new IngredientClassifier(requireContext());
            Log.d(TAG, "IngredientClassifier đã khởi tạo.");
        } catch (IOException e) {
            Log.e(TAG, "Không thể khởi tạo IngredientClassifier.", e);
            classifier = null;
        }
        detector = null;
    }

    private float getConfidenceThreshold() {
        if (getContext() == null) return DEFAULT_CONFIDENCE_THRESHOLD;
        SharedPreferences prefs = getContext().getSharedPreferences("smartcook_prefs", Context.MODE_PRIVATE);
        return prefs.getInt("confidence_threshold", 50) / 100.0f;
    }

    private void setupListeners() {
        // 1. Nút đổi nhóm nguyên liệu
        buttonGroupMode.setOnClickListener(v -> {
            if (ingredientGroup == IngredientClassifier.GROUP_AUTO) {
                ingredientGroup = IngredientClassifier.GROUP_VEGETABLE;
                buttonGroupMode.setText("Nhóm: Thịt, Rau & Quả");
            } else if (ingredientGroup == IngredientClassifier.GROUP_VEGETABLE) {
                ingredientGroup = IngredientClassifier.GROUP_SEAFOOD;
                buttonGroupMode.setText("Nhóm: Hải sản");
            } else {
                ingredientGroup = IngredientClassifier.GROUP_AUTO;
                buttonGroupMode.setText("Nhóm: Tự động");
            }
        });

        // Nút chụp
        buttonCapture.setOnClickListener(v -> takePhoto());

        // 2. Nút bật/tắt nhiều vật thể
        buttonMultiMode.setOnClickListener(v -> {
            multiMode = !multiMode;
            if (multiMode) {
                buttonMultiMode.setText(R.string.multi_mode_on);
                buttonMultiMode.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.secondary));

                // Ẩn nút nhóm đi khi bật chế độ nhiều nguyên liệu
                if (buttonGroupMode != null) {
                    buttonGroupMode.setVisibility(View.GONE);
                }

                if (textCameraHint != null) {
                    textCameraHint.setText("⚡ Chế độ nhiều nguyên liệu đang BẬT: Hướng camera và bấm 'Chụp ảnh'");
                    textCameraHint.setBackgroundColor(Color.parseColor("#D0E65100"));
                }
                initializeMultiObjectDetector();
            } else {
                buttonMultiMode.setText(R.string.multi_mode_off);
                buttonMultiMode.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.black));

                // Hiện lại nút nhóm khi tắt chế độ nhiều nguyên liệu
                if (buttonGroupMode != null) {
                    buttonGroupMode.setVisibility(View.VISIBLE);
                }

                if (textCameraHint != null) {
                    textCameraHint.setText(R.string.camera_hint);
                    textCameraHint.setBackgroundColor(Color.parseColor("#D01B5E20"));
                }
            }
        });

        // Ẩn bảng thông tin khi chạm vào
        resultInfoPanel.setOnClickListener(v -> {
            resultInfoPanel.setVisibility(View.GONE);
            if (multiMode) textMultiHint.setVisibility(View.VISIBLE);
        });

        // Sự kiện chạm ảnh
        imageResult.setOnTouchListener((v, event) -> {
            if (multiMode && multiDetections != null && event.getAction() == MotionEvent.ACTION_DOWN) {
                // Chỉ xử lý khi chạm trúng khung. Nếu trượt ra ngoài sẽ KHÔNG tự động đóng
                handleMultiTap(event.getX(), event.getY());
                return true;
            }
            return false;
        });

        // Bấm vào dòng chữ dưới cùng để quay lại camera
        textCloseHint.setOnClickListener(v -> hideResult());

        // Xem công thức
        buttonViewRecipes.setOnClickListener(v -> {
            if (!currentDetectedIngredient.isEmpty() && getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openRecipesWithFilter(currentDetectedIngredient);
            }
        });
    }

    private void initializeMultiObjectDetector() {
        if (detector != null) return;
        try {
            detector = new IngredientDetector(requireContext());
            Log.d(TAG, "IngredientDetector đã được khởi tạo.");
            if (isAdded()) Toast.makeText(requireContext(), "Đã BẬT chế độ nhiều nguyên liệu!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "Không thể khởi tạo IngredientDetector.", e);
            detector = null;
            if (isAdded()) Toast.makeText(requireContext(), "Không thể khởi tạo nhận diện nhiều vật thể.", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCamera() {
        if (!isAdded()) return;
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(requireContext());
        future.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = future.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build();
                cameraProvider.unbindAll();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                try {
                    if (!cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) && cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                        cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                    }
                } catch (Exception ignored) {}
                cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview, imageCapture);
            } catch (Exception e) {
                Log.e(TAG, "Lỗi kết nối camera: " + e.getMessage(), e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void takePhoto() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            checkCameraPermissionAndStart();
            return;
        }
        if (imageCapture == null) {
            Toast.makeText(requireContext(), "Camera chưa sẵn sàng.", Toast.LENGTH_SHORT).show();
            return;
        }

        File picturesDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (picturesDir == null) picturesDir = requireContext().getFilesDir();
        File smartCookDir = new File(picturesDir, "SmartCook");
        if (!smartCookDir.exists()) smartCookDir.mkdirs();

        File photoFile = new File(smartCookDir, "scan_" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(options, ContextCompat.getMainExecutor(requireContext()), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                if (multiMode) {
                    processMultiMode(photoFile);
                } else {
                    processAndSave(photoFile);
                }
            }
            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Lỗi khi chụp ảnh.", exception);
            }
        });
    }

    private void processAndSave(File photoFile) {
        if (executorService == null || executorService.isShutdown()) return;
        executorService.execute(() -> {
            IngredientClassifier.ClassificationResult result = null;
            Bitmap preview = null;
            try {
                Bitmap original = ImageUtils.decodeFileWithExif(photoFile.getAbsolutePath());
                if (original == null) return;

                Bitmap processed = original;
                preview = original;

                if (classifier != null) {
                    classifier.setGroup(ingredientGroup); // Chọn model trước khi phân loại
                    result = classifier.classify(processed);

                    if (result != null) {
                        Log.d(TAG, "KẾT QUẢ AI: " + result.label + " - Tự tin: " + (result.confidence * 100) + "%");
                    }
                    if (result != null && result.confidence >= getConfidenceThreshold()) {
                        saveScanResult(result, photoFile.getAbsolutePath());
                    }
                }
            } catch (Throwable t) {
                Log.e(TAG, "Lỗi xử lý ảnh một vật thể.", t);
            }
            if (preview != null) showResult(preview, result);
        });
    }

    private void saveScanResult(IngredientClassifier.ClassificationResult result, String imagePath) {
        if (result == null || getContext() == null) return;
        try {
            ScanResult record = new ScanResult();
            record.label = result.label;
            record.confidence = result.confidence;
            record.category = result.category;
            record.imagePath = imagePath;
            record.timestamp = System.currentTimeMillis();
            record.suggestedDishes = RecipeRecommendationEngine.getSuggestedDishesFormatted(result.label);
            record.nutritionTip = result.specialGuidance;
            AppDatabase.getInstance(requireContext()).scanResultDao().insert(record);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi lưu kết quả ScanResult.", e);
        }
    }

    private void processMultiMode(File photoFile) {
        if (executorService == null || executorService.isShutdown()) return;
        executorService.execute(() -> {
            Bitmap original = ImageUtils.decodeFileWithExif(photoFile.getAbsolutePath());
            if (original == null) return;

            Bitmap enhanced = original;
            List<Detection> detections = (detector != null) ? detector.detect(enhanced) : Collections.emptyList();
            Bitmap withBoxes = drawNumberedBoxes(enhanced, detections);

            multiSourceBitmap = enhanced;
            multiDetections = detections;

            if (isAdded() && detections.isEmpty()) {
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), R.string.no_items_detected, Toast.LENGTH_LONG).show());
            }
            showResult(withBoxes, null);
        });
    }

    private Bitmap drawNumberedBoxes(Bitmap source, List<Detection> detections) {
        if (source == null) return null;
        Bitmap mutable = source.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutable);
        Paint boxPaint = new Paint();
        boxPaint.setColor(Color.parseColor("#2E7D32"));
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(6f);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40f);
        textPaint.setFakeBoldText(true);

        Paint textBgPaint = new Paint();
        textBgPaint.setColor(Color.parseColor("#2E7D32"));

        if (detections == null) return mutable;
        for (int i = 0; i < detections.size(); i++) {
            RectF box = detections.get(i).getBoundingBox();
            canvas.drawRect(box, boxPaint);
            canvas.drawRect(box.left, Math.max(0, box.top - 46), box.left + 50, box.top, textBgPaint);
            canvas.drawText(String.valueOf(i + 1), box.left + 14, box.top - 10, textPaint);
        }
        return mutable;
    }

    private File saveCroppedObject(Bitmap bitmap, int index) throws IOException {
        File dir = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "SmartCook/items");
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, "item_" + System.currentTimeMillis() + "_" + index + ".jpg");
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
        }
        return file;
    }

    private PointF mapTouchToBitmap(float x, float y) {
        Matrix inverse = new Matrix();
        if (!imageResult.getImageMatrix().invert(inverse)) return null;
        float[] points = {x, y};
        inverse.mapPoints(points);
        return new PointF(points[0], points[1]);
    }

    private boolean handleMultiTap(float x, float y) {
        if (multiSourceBitmap == null || multiDetections == null) return false;
        PointF point = mapTouchToBitmap(x, y);
        if (point == null) return false;

        for (int i = 0; i < multiDetections.size(); i++) {
            RectF box = multiDetections.get(i).getBoundingBox();

            // MỞ RỘNG VÙNG CHẠM RA 40 PIXEL XUNG QUANH
            RectF touchBox = new RectF(box.left - 40f, box.top - 40f, box.right + 40f, box.bottom + 40f);

            if (!touchBox.contains(point.x, point.y)) continue;

            int left = Math.max(0, (int) box.left);
            int top = Math.max(0, (int) box.top);
            int width = Math.min((int) box.width(), multiSourceBitmap.getWidth() - left);
            int height = Math.min((int) box.height(), multiSourceBitmap.getHeight() - top);

            if (width <= 0 || height <= 0) return false;

            Bitmap cropped;
            try {
                cropped = Bitmap.createBitmap(multiSourceBitmap, left, top, width, height);
            } catch (Exception e) {
                return false;
            }

            if (classifier != null) {
                classifier.setGroup(ingredientGroup);
            }

            IngredientClassifier.ClassificationResult result = (classifier != null) ? classifier.classify(cropped) : null;

            textMultiHint.setVisibility(View.GONE);
            resultInfoPanel.setVisibility(View.VISIBLE);
            renderClassificationResult(result);

            if (result != null && result.confidence >= getConfidenceThreshold()) {
                final int itemIndex = i + 1;
                if (executorService != null && !executorService.isShutdown()) {
                    executorService.execute(() -> {
                        try {
                            File itemFile = saveCroppedObject(cropped, itemIndex);
                            saveScanResult(result, itemFile.getAbsolutePath());
                        } catch (IOException e) {
                            Log.e(TAG, "Lỗi lưu cropped object.", e);
                        }
                    });
                }
            }
            return true;
        }
        return false;
    }

    private void showResult(Bitmap bitmap, IngredientClassifier.ClassificationResult result) {
        if (!isAdded() || bitmap == null) return;
        requireActivity().runOnUiThread(() -> {
            if (!isAdded()) return;
            previewView.setVisibility(View.INVISIBLE);
            imageResult.setImageBitmap(bitmap);
            imageResult.setVisibility(View.VISIBLE);
            textCloseHint.setVisibility(View.VISIBLE);

            if (multiMode) {
                textMultiHint.setVisibility(View.VISIBLE);
                resultInfoPanel.setVisibility(View.GONE);
            } else {
                resultInfoPanel.setVisibility(View.VISIBLE);
                renderClassificationResult(result);
            }
        });
    }

    private void renderClassificationResult(IngredientClassifier.ClassificationResult result) {
        if (result == null) {
            textLabel.setText("Không thể phân loại");
            textConfidence.setText("");
            textGuide.setText("Vui lòng chụp lại với ánh sáng rõ hơn.");
            colorBadge.setBackgroundColor(Color.parseColor("#888888"));
            currentDetectedIngredient = "";
            buttonViewRecipes.setVisibility(View.GONE);
        } else if (result.confidence < getConfidenceThreshold()) {
            textLabel.setText("Chưa xác định chắc chắn (" + result.label + ")");
            textConfidence.setText(String.format(Locale.getDefault(), "Độ tin cậy: %.0f%% (Dưới ngưỡng lưu)", result.confidence * 100));
            textGuide.setText("Hãy đưa camera lại gần nguyên liệu hơn để nhận diện chính xác.");
            colorBadge.setBackgroundColor(Color.parseColor("#FB8C00"));
            currentDetectedIngredient = result.label;
            buttonViewRecipes.setVisibility(View.VISIBLE);
        } else {
            textLabel.setText(result.label);
            textConfidence.setText(String.format(Locale.getDefault(), "Độ tin cậy: %.0f%% • Nhóm: %s", result.confidence * 100, result.category));
            textGuide.setText(String.format(Locale.getDefault(), "Gợi ý món ăn:\n%s\n\n💡 Mẹo: %s", RecipeRecommendationEngine.getSuggestedDishesFormatted(result.label), result.specialGuidance));
            colorBadge.setBackgroundColor(Color.parseColor("#2E7D32"));
            currentDetectedIngredient = result.label;
            buttonViewRecipes.setVisibility(View.VISIBLE);
        }
    }

    private void hideResult() {
        imageResult.setVisibility(View.GONE);
        textCloseHint.setVisibility(View.GONE);
        textMultiHint.setVisibility(View.GONE);
        resultInfoPanel.setVisibility(View.GONE);
        previewView.setVisibility(View.VISIBLE);

        if (textCameraHint != null) {
            if (multiMode) {
                textCameraHint.setText("⚡ Chế độ nhiều nguyên liệu đang BẬT: Hướng camera và bấm 'Chụp ảnh'");
                textCameraHint.setBackgroundColor(Color.parseColor("#D0E65100"));
            } else {
                textCameraHint.setText(R.string.camera_hint);
                textCameraHint.setBackgroundColor(Color.parseColor("#D01B5E20"));
            }
        }
        currentDetectedIngredient = "";
    }

    @Override
    public void onDestroyView() {
        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }
        if (classifier != null) {
            try { classifier.close(); } catch (Exception e) { Log.w(TAG, "Lỗi đóng classifier.", e); }
            classifier = null;
        }
        if (detector != null) {
            try { detector.close(); } catch (Exception e) { Log.w(TAG, "Lỗi đóng detector.", e); }
            detector = null;
        }
        multiSourceBitmap = null;
        multiDetections = Collections.emptyList();
        imageCapture = null;
        super.onDestroyView();
    }
}