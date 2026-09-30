# BÁO CÁO BUỔI 9: TÍCH HỢP MÔ HÌNH TFLITE VÀO ỨNG DỤNG ANDROID
**Học phần:** Chuyên đề thiết kế ứng dụng thiết bị số  
**Đơn vị:** Trường Đại học Cửu Long (MKU)  
**Tên đề tài:** Ứng dụng gợi ý món ăn thông minh dựa trên nguyên liệu (SmartCook AI)  

---

## 1. Mục tiêu
- Nạp tập tin mô hình `ingredient_classifier.tflite` và nhãn `ingredient_labels.txt` từ thư mục `assets`.
- Khởi tạo đối tượng `org.tensorflow.lite.Interpreter`.
- Chuyển đổi và chuẩn hóa ảnh Bitmap thành `ByteBuffer` (chuẩn hóa giá trị pixel về dải `[-1, 1]`).
- Tích hợp lớp nhận diện đa đối tượng `IngredientDetector` bằng TensorFlow Lite Task Vision (`ObjectDetector`).
- Hiển thị kết quả nhận diện (nhãn, % tin cậy, phân loại thực phẩm) trực tiếp lên giao diện của `ScanFragment`.

---

## 2. Nạp mô hình từ Assets vào bộ nhớ đệm
Trong `IngredientClassifier.java`:
```java
private MappedByteBuffer loadModelFile(Context context, String modelName) throws IOException {
    AssetFileDescriptor fd = context.getAssets().openFd(modelName);
    FileInputStream inputStream = new FileInputStream(fd.getFileDescriptor());
    FileChannel channel = inputStream.getChannel();
    return channel.map(FileChannel.MapMode.READ_ONLY, fd.getStartOffset(), fd.getDeclaredLength());
}
```

---

## 3. Tiền xử lý Tensor đầu vào (`preprocess`)
- Resize ảnh chụp về kích thước chuẩn của mô hình `224x224`.
- Phân tách 3 kênh màu Red, Green, Blue và chuẩn hóa giá trị từ `[0, 255]` về `[-1.0, 1.0]`:
```java
buffer.putFloat(((pixel >> 16) & 0xFF) / 127.5f - 1.0f); // Red
buffer.putFloat(((pixel >> 8) & 0xFF) / 127.5f - 1.0f);  // Green
buffer.putFloat((pixel & 0xFF) / 127.5f - 1.0f);         // Blue
```

---

## 4. Chạy suy luận (Inference Pipeline)
```java
float[][] output = new float[1][labels.size()];
interpreter.run(input, output);

// Tìm index có xác suất lớn nhất (ArgMax)
int bestIdx = 0;
for (int i = 1; i < output[0].length; i++) {
    if (output[0][i] > output[0][bestIdx]) bestIdx = i;
}
String detectedLabel = labels.get(bestIdx);
float confidence = output[0][bestIdx];
```

---

## 5. Tự động liên kết cơ sở dữ liệu Room
Khi độ tin cậy $\ge 50\%$ (hoặc ngưỡng do người dùng thiết lập trong Settings):
1. Bản ghi `ScanResult` được tự động ghi vào Room Database.
2. Dữ liệu bao gồm: Tên nguyên liệu, nhóm thực phẩm, ảnh chụp, thời gian quét, các món ăn gợi ý.
3. Người dùng không cần bấm lưu thủ công, nâng cao trải nghiệm người dùng (UX).
