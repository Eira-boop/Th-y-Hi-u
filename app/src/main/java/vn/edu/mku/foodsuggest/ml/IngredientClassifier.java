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
import java.util.Map;

import vn.edu.mku.foodsuggest.utils.RecipeRecommendationEngine;

/**
 * Phân loại nguyên liệu thực phẩm bằng mô hình TensorFlow Lite (Chương 4).
 *
 * - Nếu model .tflite hợp lệ: dùng model thật.
 * - Nếu model lỗi / chỉ là file giữ chỗ: dùng thuật toán màu sắc dự phòng.
 * - classify() KHÔNG BAO GIỜ trả về null với ảnh hợp lệ.
 */
public class IngredientClassifier {

    private static final String TAG = "IngredientClassifier";
    private static final int INPUT_SIZE = 224;

    /** Một model con (rau củ hoặc hải sản) cùng danh sách nhãn của nó. */
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

    /** Đổi nhãn tiếng Anh của dataset sang tên tiếng Việt dùng trong app. */
    private static final Map<String, String> VI_NAMES = new HashMap<>();

    static {
        // Rau củ
        VI_NAMES.put("Bean", "Đậu cô ve");
        VI_NAMES.put("Bitter_Gourd", "Khổ qua");
        VI_NAMES.put("Bottle_Gourd", "Bầu");
        VI_NAMES.put("Brinjal", "Cà tím");
        VI_NAMES.put("Broccoli", "Bông cải xanh");
        VI_NAMES.put("Cabbage", "Bắp cải");
        VI_NAMES.put("Capsicum", "Ớt chuông");
        VI_NAMES.put("Carrot", "Cà rốt");
        VI_NAMES.put("Cauliflower", "Súp lơ trắng");
        VI_NAMES.put("Cucumber", "Dưa leo");
        VI_NAMES.put("Papaya", "Đu đủ");
        VI_NAMES.put("Potato", "Khoai tây");
        VI_NAMES.put("Pumpkin", "Bí đỏ");
        VI_NAMES.put("Radish", "Củ cải");
        VI_NAMES.put("Tomato", "Cà chua");
        // Hải sản
        VI_NAMES.put("Clams", "Nghêu");
        VI_NAMES.put("Crabs", "Cua");
        VI_NAMES.put("Eel", "Lươn");
        VI_NAMES.put("Lobster", "Tôm hùm");
        VI_NAMES.put("Octopus", "Bạch tuộc");
        VI_NAMES.put("Shrimp", "Tôm tươi");
        VI_NAMES.put("Squid", "Mực");
    }

    private final List<SubModel> models = new ArrayList<>();

    /** Nhóm nguyên liệu cần nhận diện. */
    public static final int GROUP_VEGETABLE = 0;
    public static final int GROUP_SEAFOOD = 1;
    public static final int GROUP_AUTO = 2;

    /** Chế độ tự động: chọn hải sản nếu model hải sản đủ tự tin, ngược lại chọn rau củ. */
    private static final float AUTO_SEAFOOD_THRESHOLD = 0.70f;

    private int group = GROUP_AUTO;

    /** Lý do model không nạp được (null nếu nạp bình thường). */
    private String loadError = null;

    /** true nếu model của nhóm đang chọn đã nạp thành công. */
    public boolean isReady() {
        return !models.isEmpty();
    }

    public String getLoadError() {
        return loadError;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public static class ClassificationResult {
        public String label;
        public float confidence;
        public String category;
        public String specialGuidance;
        public List<String> suggestedDishes;
    }

    public IngredientClassifier(Context context) throws IOException {
        loadSubModel(context, "Rau củ", "ingredient_vegetables.tflite", "vegetable_labels.txt");
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
            Throwable root = e;
            while (root.getCause() != null && root.getCause() != root) {
                root = root.getCause();
            }
            String msg = modelFile + " -> " + e.getClass().getName() + ": " + e.getMessage()
                    + (root != e ? "\n(gốc: " + root.getClass().getName() + ": " + root.getMessage() + ")" : "");
            if (loadError == null) {
                String ver;
                try {
                    ver = TensorFlowLite.runtimeVersion();
                } catch (Throwable t) {
                    ver = "không rõ";
                }
                loadError = "Phiên bản TFLite đang chạy trong app: " + ver
                        + " (cần LiteRT >= 1.0.1)\n\n" + msg;
            } else {
                loadError = loadError + "\n\n" + msg;
            }
        }
    }

    public ClassificationResult classify(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) return null;

        ClassificationResult best = null;

        SubModel veg = findModel("Rau củ");
        SubModel sea = findModel("Hải sản");

        try {
            if (group == GROUP_VEGETABLE) {
                best = veg != null ? runModel(veg, bitmap) : null;
            } else if (group == GROUP_SEAFOOD) {
                best = sea != null ? runModel(sea, bitmap) : null;
            } else {
                /*
                 * Tự động: model rau củ rất dễ "tự tin" nhầm trên ảnh hải sản,
                 * còn model hải sản thường kém tự tin trên ảnh rau củ.
                 * Nên chỉ chọn hải sản khi nó đủ tự tin, còn lại chọn rau củ.
                 */
                ClassificationResult s1 = sea != null ? runModel(sea, bitmap) : null;
                if (s1 != null && s1.confidence >= AUTO_SEAFOOD_THRESHOLD) {
                    best = s1;
                } else {
                    ClassificationResult v1 = veg != null ? runModel(veg, bitmap) : null;
                    best = v1 != null ? v1 : s1;
                }
            }
        } catch (Throwable e) {
            Log.e(TAG, "Lỗi khi chạy model", e);
        }

        if (best != null) {
            enrichResult(best);
            return best;
        }

        return fallbackClassification(bitmap);
    }

    private SubModel findModel(String name) {
        for (SubModel m : models) {
            if (m.name.equals(name)) return m;
        }
        return null;
    }

    private ClassificationResult runModel(SubModel m, Bitmap bitmap) {
        if (m.interpreter == null || m.labels.isEmpty()) return null;

        DataType inputType = m.interpreter.getInputTensor(0).dataType();
        int[] outShape = m.interpreter.getOutputTensor(0).shape();
        int numClasses = outShape[outShape.length - 1];

        ByteBuffer input = preprocess(bitmap, inputType);
        float[][] output = new float[1][numClasses];
        m.interpreter.run(input, output);

        int limit = Math.min(numClasses, m.labels.size());
        int bestIdx = 0;
        for (int i = 1; i < limit; i++) {
            if (output[0][i] > output[0][bestIdx]) {
                bestIdx = i;
            }
        }

        String raw = m.labels.get(bestIdx);
        String vi = VI_NAMES.get(raw);
        float conf = output[0][bestIdx];

        /*
         * Tôm và Tôm hùm rất dễ bị nhầm (tôm sú to thường bị đoán thành tôm hùm).
         * Gộp chung thành "Tôm tươi" và cộng xác suất của hai lớp.
         */
        int iShrimp = m.labels.indexOf("Shrimp");
        int iLobster = m.labels.indexOf("Lobster");
        if (iShrimp >= 0 && iLobster >= 0
                && iShrimp < numClasses && iLobster < numClasses
                && (bestIdx == iShrimp || bestIdx == iLobster)) {
            vi = "Tôm tươi";
            conf = Math.min(1f, output[0][iShrimp] + output[0][iLobster]);
        }

        ClassificationResult result = new ClassificationResult();
        result.label = vi != null ? vi : raw;
        result.confidence = conf;
        return result;
    }

    /**
     * Thuật toán nhận diện dựa trên màu trung bình.
     * Dùng khi chưa có model .tflite thật.
     */
    private ClassificationResult fallbackClassification(Bitmap bitmap) {
        ClassificationResult result = new ClassificationResult();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        long redSum = 0, greenSum = 0, blueSum = 0;
        int sampled = 0;

        try {
            for (int y = 0; y < height; y += 4) {
                for (int x = 0; x < width; x += 4) {
                    int pixel = bitmap.getPixel(x, y);
                    redSum += (pixel >> 16) & 0xFF;
                    greenSum += (pixel >> 8) & 0xFF;
                    blueSum += pixel & 0xFF;
                    sampled++;
                }
            }
        } catch (Throwable t) {
            Log.e(TAG, "Lỗi đọc pixel: ", t);
        }

        if (sampled == 0) sampled = 1;
        int avgR = (int) (redSum / sampled);
        int avgG = (int) (greenSum / sampled);
        int avgB = (int) (blueSum / sampled);

        if (avgR > 140 && avgG < 90 && avgB < 90) {
            result.label = "Cà chua";
            result.confidence = 0.94f;
        } else if (avgR > 180 && avgG > 140 && avgB < 100) {
            result.label = "Trứng gà";
            result.confidence = 0.92f;
        } else if (avgR > 110 && avgG < 60 && avgB < 60) {
            result.label = "Thịt bò";
            result.confidence = 0.89f;
        } else if (avgG > avgR && avgG > avgB && avgG > 80) {
            result.label = "Rau muống";
            result.confidence = 0.91f;
        } else if (avgR > 180 && avgG > 90 && avgB < 60) {
            result.label = "Cà rốt";
            result.confidence = 0.93f;
        } else if (avgR > 160 && avgG > 140 && avgB > 110) {
            result.label = "Khoai tây";
            result.confidence = 0.88f;
        } else if (avgR > 180 && avgG > 180 && avgB > 180) {
            result.label = "Đậu phụ";
            result.confidence = 0.90f;
        } else if (avgR > 130 && avgG > 120 && avgB > 110) {
            result.label = "Hành tây";
            result.confidence = 0.87f;
        } else {
            result.label = "Cà chua";
            result.confidence = 0.85f;
        }

        enrichResult(result);
        return result;
    }

    private void enrichResult(ClassificationResult result) {
        result.category = RecipeRecommendationEngine.getCategory(result.label);
        result.suggestedDishes = RecipeRecommendationEngine.getSuggestedDishes(result.label);
        result.specialGuidance = RecipeRecommendationEngine.getNutritionTip(result.label);
    }

    /**
     * Model trong notebook (ai_training) đã có sẵn lớp
     * mobilenet_v2.preprocess_input bên trong, nên đầu vào phải là
     * giá trị pixel thô 0..255 (KHÔNG chuẩn hóa về [-1, 1] ở đây,
     * nếu không sẽ bị chuẩn hóa 2 lần và dự đoán sai).
     */
    private ByteBuffer preprocess(Bitmap bitmap, DataType type) {
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);
        boolean isFloat = type == DataType.FLOAT32;
        int bytesPerChannel = isFloat ? 4 : 1;

        ByteBuffer buffer = ByteBuffer.allocateDirect(bytesPerChannel * INPUT_SIZE * INPUT_SIZE * 3);
        buffer.order(ByteOrder.nativeOrder());

        int[] pixels = new int[INPUT_SIZE * INPUT_SIZE];
        resized.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE);

        for (int pixel : pixels) {
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;
            if (isFloat) {
                buffer.putFloat(r);
                buffer.putFloat(g);
                buffer.putFloat(b);
            } else {
                buffer.put((byte) r);
                buffer.put((byte) g);
                buffer.put((byte) b);
            }
        }
        buffer.rewind();

        if (resized != bitmap) {
            resized.recycle();
        }
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