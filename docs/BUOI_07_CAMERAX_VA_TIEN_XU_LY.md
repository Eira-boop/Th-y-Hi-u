# BÁO CÁO BUỔI 7: TÍCH HỢP CAMERAX VÀ TIỀN XỬ LÝ ẢNH VỚI OPENCV
**Học phần:** Chuyên đề thiết kế ứng dụng thiết bị số  
**Đơn vị:** Trường Đại học Cửu Long (MKU)  
**Tên đề tài:** Ứng dụng gợi ý món ăn thông minh dựa trên nguyên liệu (SmartCook AI)  

---

## 1. Mục tiêu thực hành
- Tích hợp thư viện Jetpack CameraX (`androidx.camera:1.3.4`) vào ứng dụng Android.
- Xây dựng quy trình xin cấp quyền `CAMERA` trong `SplashActivity`.
- Thiết kế luồng hiển thị video thời gian thực bằng `PreviewView` và bắt sự kiện chụp ảnh chất lượng cao bằng `ImageCapture`.
- Tích hợp OpenCV (`org.opencv:opencv:4.9.0`) để tiền xử lý ảnh (tăng tương phản, khử nhiễu, phát hiện biên Canny, cắt sát vật thể chính) trước khi đưa vào mô hình AI.

---

## 2. Cấu hình dependencies
Trong `app/build.gradle.kts`:
```kotlin
implementation("androidx.camera:camera-core:1.3.4")
implementation("androidx.camera:camera-camera2:1.3.4")
implementation("androidx.camera:camera-lifecycle:1.3.4")
implementation("androidx.camera:camera-view:1.3.4")
implementation("org.opencv:opencv:4.9.0")
```

---

## 3. Khởi tạo CameraX bất đồng bộ
Trong `ScanFragment.java`, sử dụng `ProcessCameraProvider` gắn vào vòng đời `LifecycleOwner` của Fragment:
```java
ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(requireContext());
future.addListener(() -> {
    try {
        ProcessCameraProvider cameraProvider = future.get();
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture
        );
    } catch (Exception e) {
        Log.e("ScanFragment", "Camera bind error", e);
    }
}, ContextCompat.getMainExecutor(requireContext()));
```

---

## 4. Kỹ thuật tiền xử lý ảnh với OpenCV (`ImageUtils.java`)

1. **Khởi tạo Native Library:**
   ```java
   static {
       OpenCVLoader.initLocal();
   }
   ```
2. **Tăng cường tương phản và độ sáng (`enhance`):**
   ```java
   // alpha = 1.15 (tương phản), beta = 10 (độ sáng)
   src.convertTo(enhanced, -1, 1.15, 10);
   ```
3. **Phát hiện vùng đối tượng trung tâm (`findMainObjectRect`):**
   - Chuyển RGB sang Grayscale: `Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGBA2GRAY)`.
   - Làm mờ giảm nhiễu: `Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0)`.
   - Lọc biên Canny: `Imgproc.Canny(gray, edges, 50, 150)`.
   - Giãn nở biên khép kín: `Imgproc.dilate(edges, edges, Mat.ones(5, 5, CvType.CV_8U))`.
   - Tìm contour và tính điểm ưu tiên vật thể gần tâm khung hình nhất với diện tích $> 5\%$.
4. **Cắt đối tượng có lề dự phòng (Padding 10%):**
   - Đảm bảo trích xuất trọn vẹn nguyên liệu trước khi nạp vào mạng nơ-ron nhận dạng 224x224.

---

## 5. Kết quả đạt được
- Ứng dụng khởi động mượt mà, yêu cầu cấp quyền hợp lệ theo tiêu chuẩn Android 10+.
- Khung hình xem trước từ camera hiển thị ổn định 30fps.
- Ảnh chụp được lưu vào thư mục hệ thống `Pictures/SmartCook/scan_<timestamp>.jpg` và xử lý tiền xử lý OpenCV ngay trên Background Thread.
