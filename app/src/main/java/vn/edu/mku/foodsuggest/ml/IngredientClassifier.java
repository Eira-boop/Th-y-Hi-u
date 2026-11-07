package vn.edu.mku.foodsuggest.ml;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.TensorFlowLite;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import vn.edu.mku.foodsuggest.utils.RecipeRecommendationEngine;

public class IngredientClassifier {

    private static final String TAG = "IngredientClassifier";
    private static final int INPUT_SIZE = 224;

    // Các hằng số nhóm
    public static final int GROUP_VEGETABLE = 0;
    public static final int GROUP_SEAFOOD = 1;
    public static final int GROUP_AUTO = 2;
    private int group = GROUP_AUTO;

    public void setGroup(int group) {
        this.group = group;
    }

    private static class SubModel {
        final String name;
        Interpreter interpreter;
        final List<String> labels;

        SubModel(String name, Interpreter interpreter, List<String> labels) {
            this.name = name;
            this.interpreter = interpreter;
            this.labels = labels;
        }
    }

    private static final Map<String, String> VI_NAMES = new HashMap<>();

    static {
        // ================= 29 LỚP CƠ BẢN =================
        VI_NAMES.put("beef_mince", "Thịt bò xay");
        VI_NAMES.put("chicken_breast", "Ức gà");
        VI_NAMES.put("chicken_thighs", "Đùi gà");
        VI_NAMES.put("meat", "Thịt");
        VI_NAMES.put("mutton", "Thịt cừu");
        VI_NAMES.put("pork", "Thịt heo");
        VI_NAMES.put("steak", "Bít tết");
        VI_NAMES.put("ham", "Thịt nguội");
        VI_NAMES.put("sausage", "Xúc xích");
        VI_NAMES.put("fish", "Cá");
        VI_NAMES.put("bellpepper", "Ớt chuông");
        VI_NAMES.put("carrot", "Cà rốt");
        VI_NAMES.put("cucumber", "Dưa leo");
        VI_NAMES.put("garlic", "Tỏi");
        VI_NAMES.put("ginger", "Gừng");
        VI_NAMES.put("lettuce", "Xà lách");
        VI_NAMES.put("mushroom", "Nấm");
        VI_NAMES.put("onion", "Hành tây");
        VI_NAMES.put("potato", "Khoai tây");
        VI_NAMES.put("tomato", "Cà chua");
        VI_NAMES.put("beetroot", "Củ dền");
        VI_NAMES.put("cabbage", "Bắp cải");
        VI_NAMES.put("cauliflower", "Súp lơ");
        VI_NAMES.put("peas", "Đậu Hà Lan");
        VI_NAMES.put("apple", "Táo");
        VI_NAMES.put("avocado", "Bơ");
        VI_NAMES.put("banana", "Chuối");
        VI_NAMES.put("lemon", "Chanh");
        VI_NAMES.put("mango", "Xoài");
        VI_NAMES.put("orange", "Cam");
        VI_NAMES.put("strawberry", "Dâu tây");
        VI_NAMES.put("watermelon", "Dưa hấu");
        VI_NAMES.put("egg", "Trứng");
        VI_NAMES.put("iceCream", "Kem");
        VI_NAMES.put("yogurt", "Sữa chua");

        // ================= HẢI SẢN CŨ =================
        VI_NAMES.put("Clams", "Nghêu");
        VI_NAMES.put("Crabs", "Cua");
        VI_NAMES.put("Eel", "Lươn");
        VI_NAMES.put("Lobster", "Tôm hùm");
        VI_NAMES.put("Octopus", "Bạch tuộc");
        VI_NAMES.put("Shrimp", "Tôm");
        VI_NAMES.put("Squid", "Mực");
        VI_NAMES.put("Fish", "Cá");
    }

    private final List<SubModel> models = new ArrayList<>();
    private String loadError = null;

    public boolean isReady() {
        return !models.isEmpty();
    }

    public String getLoadError() {
        return loadError;
    }

    public static class ClassificationResult {
        public String label;
        public float confidence;
        public String category;
        public String specialGuidance;
        public List<String> suggestedDishes;
    }

    public IngredientClassifier(Context context) throws IOException {
        // Load model 29 lớp cũ
        loadSubModel(context, "Tất cả nguyên liệu", "ingredient_classifier.tflite", "labels.txt");
        // Load model hải sản cũ
        loadSubModel(context, "Hải sản", "ingredient_seafood.tflite", "seafood_labels.txt");
        Log.d(TAG, "Đã nạp " + models.size() + " model AI.");
    }

    private void loadSubModel(Context context, String name, String modelFile, String labelFile) {
        try {
            List<String> labels = loadLabels(context, labelFile);
            MappedByteBuffer buffer = loadModelFile(context, modelFile);
            Interpreter it = new Interpreter(buffer);
            models.add(new SubModel(name, it, labels));
            Log.d(TAG, "Nạp thành công " + modelFile + " (" + labels.size() + " nhãn)");
        } catch (Throwable e) {
            Log.e(TAG, "Không nạp được " + modelFile + ": " + e.getMessage(), e);
            if (loadError == null) loadError = modelFile + ": " + e.getMessage();
            else loadError += "\n" + modelFile + ": " + e.getMessage();
        }
    }

    private SubModel findModel(String name) {
        for (SubModel m : models) {
            if (m.name.equals(name)) return m;
        }
        return null;
    }

    public ClassificationResult classify(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled() || models.isEmpty()) {
            return null;
        }

        // 1. Xác định xem sẽ dùng model nào
        SubModel targetModel = null;
        if (group == GROUP_SEAFOOD) {
            targetModel = findModel("Hải sản");
            if (targetModel == null) {
                Log.e(TAG, "Model hải sản lỗi, tự động chuyển về model 29 lớp.");
            }
        }

        // Mặc định dùng model 29 lớp nếu không phải chế độ Hải sản hoặc model Hải sản bị lỗi
        if (targetModel == null) {
            targetModel = findModel("Tất cả nguyên liệu");
        }

        if (targetModel == null) return null;

        try {
            int numClasses = targetModel.labels.size();
            float[] avgProbs = new float[numClasses];

            // KỸ THUẬT TTA: Quét 3 vùng (Trái, Giữa, Phải) để tránh bị lừa bởi nền
            Bitmap leftCrop = getCropBitmap(bitmap, -1);
            runSingleInterpreter(leftCrop, targetModel, avgProbs);

            Bitmap centerCrop = getCropBitmap(bitmap, 0);
            runSingleInterpreter(centerCrop, targetModel, avgProbs);

            Bitmap rightCrop = getCropBitmap(bitmap, 1);
            runSingleInterpreter(rightCrop, targetModel, avgProbs);

            // Trung bình cộng xác suất
            for (int i = 0; i < numClasses; i++) {
                avgProbs[i] /= 3.0f;
            }

            // Tìm Top 3 nhãn có xác suất cao nhất
            int[] topIndices = new int[3];
            float[] topProbs = new float[3];
            boolean[] used = new boolean[numClasses];
            for (int rank = 0; rank < 3; rank++) {
                int bestIdx = -1;
                for (int i = 0; i < numClasses; i++) {
                    if (!used[i] && (bestIdx == -1 || avgProbs[i] > avgProbs[bestIdx])) {
                        bestIdx = i;
                    }
                }
                used[bestIdx] = true;
                topIndices[rank] = bestIdx;
                topProbs[rank] = avgProbs[bestIdx];
            }

            // LOG TOP 3 ĐỂ DEBUG
            for (int i = 0; i < 3; i++) {
                Log.d(TAG, "TOP " + (i+1) + " (" + targetModel.name + "): " + targetModel.labels.get(topIndices[i]) + " = " + String.format(Locale.US, "%.1f%%", topProbs[i] * 100));
            }

            int finalBestIdx = topIndices[0];
            float finalConf = topProbs[0];

            // TUYỆT CHIÊU CHỐNG ẢO GIÁC (Chỉ áp dụng cho model 29 lớp)
            if (targetModel.name.equals("Tất cả nguyên liệu")) {
                String top1Raw = targetModel.labels.get(topIndices[0]);
                if (top1Raw.equals("iceCream") || top1Raw.equals("beverage") || top1Raw.equals("dairy") || top1Raw.equals("yogurt")) {
                    if (topProbs[1] > 0.15f) {
                        finalBestIdx = topIndices[1];
                        finalConf = topProbs[1];
                        Log.d(TAG, "Đã chặn Top 1 (" + top1Raw + "), chuyển sang Top 2: " + targetModel.labels.get(finalBestIdx));
                    }
                }
            }

            String raw = targetModel.labels.get(finalBestIdx);
            String vi = VI_NAMES.get(raw);

            ClassificationResult result = new ClassificationResult();
            result.label = vi != null ? vi : raw;
            result.confidence = finalConf;
            enrichResult(result);

            return result;

        } catch (Exception e) {
            Log.e(TAG, "Lỗi phân loại AI", e);
            return null;
        }
    }

    private void runSingleInterpreter(Bitmap bmp, SubModel model, float[] probAccumulator) {
        ByteBuffer input = preprocess(bmp);
        float[][] output = new float[1][model.labels.size()];
        model.interpreter.run(input, output);

        for (int i = 0; i < output[0].length; i++) {
            probAccumulator[i] += output[0][i];
        }
        if (!bmp.isRecycled()) bmp.recycle();
    }

    private Bitmap getCropBitmap(Bitmap bitmap, int position) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int size = Math.min(w, h);

        int x;
        if (position == -1) {
            x = 0; // Cắt bên trái
        } else if (position == 1) {
            x = w - size; // Cắt bên phải
        } else {
            x = (w - size) / 2; // Cắt ở giữa
        }
        int y = (h - size) / 2; // Luôn cắt ở giữa theo chiều dọc

        Bitmap cropped = Bitmap.createBitmap(bitmap, x, y, size, size);
        Bitmap resized = Bitmap.createScaledBitmap(cropped, INPUT_SIZE, INPUT_SIZE, true);
        if (cropped != resized && cropped != bitmap) cropped.recycle();
        return resized;
    }

    private void enrichResult(ClassificationResult result) {
        result.category = RecipeRecommendationEngine.getCategory(result.label);
        result.suggestedDishes = RecipeRecommendationEngine.getSuggestedDishes(result.label);
        result.specialGuidance = RecipeRecommendationEngine.getNutritionTip(result.label);
    }

    private ByteBuffer preprocess(Bitmap resized) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * 3);
        buffer.order(ByteOrder.nativeOrder());

        int[] pixels = new int[INPUT_SIZE * INPUT_SIZE];
        resized.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE);

        for (int pixel : pixels) {
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;
            buffer.putFloat((r / 127.5f) - 1.0f);
            buffer.putFloat((g / 127.5f) - 1.0f);
            buffer.putFloat((b / 127.5f) - 1.0f);
        }
        buffer.rewind();
        return buffer;
    }

    private MappedByteBuffer loadModelFile(Context context, String modelName) throws IOException {
        AssetFileDescriptor fd = context.getAssets().openFd(modelName);
        FileInputStream inputStream = new FileInputStream(fd.getFileDescriptor());
        FileChannel channel = inputStream.getChannel();
        return channel.map(FileChannel.MapMode.READ_ONLY, fd.getStartOffset(), fd.getDeclaredLength());
    }

    private List<String> loadLabels(Context context, String fileName) throws IOException {
        List<String> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(context.getAssets().open(fileName), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) list.add(trimmed);
            }
        }
        return list;
    }

    public void close() {
        for (SubModel m : models) {
            if (m.interpreter != null) {
                m.interpreter.close();
                m.interpreter = null;
            }
        }
        models.clear();
    }
}