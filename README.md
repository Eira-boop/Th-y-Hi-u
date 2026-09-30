# 🍳 SmartCook AI - Ứng Dụng Gợi Ý Món Ăn Thông Minh Dựa Trên Nguyên Liệu

**Đồ án:** Chuyên đề thiết kế ứng dụng thiết bị số  
**Đơn vị:** Trường Đại học Cửu Long (MKU)  
**Giảng viên hướng dẫn:** Thầy Hiếu  
**Sinh viên thực hiện:** Như Ái và nhóm cộng sự  
**Nền tảng:** Android (Java) | Target SDK: 34 | Min SDK: 26 (Android 8.0+)  

---

## 🌟 Giới thiệu tổng quan
**SmartCook AI** là ứng dụng di động thông minh giúp người dùng giải quyết câu hỏi quen thuộc *"Hôm nay ăn gì?"*. Bằng việc kết hợp sức mạnh của **CameraX**, thư viện thị giác máy tính **OpenCV** và mô hình học sâu **TensorFlow Lite (Edge AI)**, ứng dụng có thể nhận diện chính xác nguyên liệu nấu ăn (rau, củ, thịt, trứng, hải sản...) và đề xuất ngay các công thức nấu ăn thơm ngon, bổ dưỡng cùng hướng dẫn chi tiết từng bước.

Dự án được xây dựng bám sát 100% theo tài liệu hướng dẫn thực hành và **Kế hoạch thực hiện đồ án sinh viên (từ Buổi 6 đến Buổi 12)**.

---

## 📱 Các tính năng chính

1. **Quét và Nhận diện nguyên liệu (CameraX + OpenCV + TFLite):**
   - **Chế độ Chụp đơn (Single Mode):** Tự động phát hiện biên Canny, tăng tương phản, cắt sát vật thể trung tâm và nhận diện nguyên liệu.
   - **Chế độ Nhiều nguyên liệu (Multi Mode):** Sử dụng Object Detection phát hiện nhiều nguyên liệu cùng lúc trong đĩa/bàn ăn, đánh số thứ tự (1, 2, 3...) và cho phép người dùng chạm vào từng ô để nhận diện và xem món gợi ý riêng biệt.
2. **Gợi ý món ăn thông minh & Mẹo dinh dưỡng:**
   - Động cơ đối sánh (`RecipeRecommendationEngine`) tự động liên kết nguyên liệu với kho công thức món Việt (Canh cà chua trứng, Bò xào hành tây, Đậu sốt cà chua, Bò lúc lắc, Gà xào nấm...).
   - Cung cấp mẹo dinh dưỡng khoa học (hấp thụ Lycopene, bảo quản tươi ngon, giữ giòn vitamin...).
3. **Quản lý Lịch sử quét (Room Database):**
   - Tự động lưu bản ghi quét khi đạt độ tin cậy $\ge 50\%$.
   - Hiển thị danh sách thẻ CardView có ảnh thumbnail.
   - Hỗ trợ thao tác vuốt sang trái/phải để xóa (`Swipe to delete`).
4. **Kho Công thức & Tra cứu chi tiết:**
   - Tìm kiếm món ăn và nguyên liệu theo thời gian thực.
   - Hộp thoại `BottomSheetDialog` xem chi tiết định lượng nguyên liệu, thời gian nấu, lượng Calo và các bước nấu ăn.
5. **Cài đặt & Cá nhân hóa:**
   - Tùy chỉnh thanh trượt ngưỡng tin cậy AI (Confidence Threshold từ 10% đến 90%).
   - Tùy chọn ưu tiên món ăn thanh đạm / Ăn chay.
   - Quản lý và làm sạch dữ liệu quét.

---

## 📂 Cấu trúc thư mục dự án

```
d:/Thầy Hiếu and Như Ái/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── assets/
│   │   │   ├── ingredient_classifier.tflite   # Mô hình AI phân loại nguyên liệu
│   │   │   ├── ssd_mobilenet_v1.tflite        # Mô hình phát hiện đa đối tượng
│   │   │   ├── ingredient_labels.txt          # Nhãn 15 loại nguyên liệu tiếng Việt
│   │   │   ├── labelmap.txt                   # Nhãn phát hiện vật thể
│   │   │   └── recipes.json                   # CSDL công thức món ăn khởi tạo
│   │   ├── java/vn/edu/mku/foodsuggest/
│   │   │   ├── data/                          # Kiến trúc Room Database (Chương 3)
│   │   │   │   ├── AppDatabase.java
│   │   │   │   ├── ScanResult.java
│   │   │   │   ├── ScanResultDao.java
│   │   │   │   ├── Recipe.java
│   │   │   │   └── RecipeDao.java
│   │   │   ├── ml/                            # Xử lý mô hình AI (Chương 4 & 5)
│   │   │   │   ├── IngredientClassifier.java
│   │   │   │   └── IngredientDetector.java
│   │   │   ├── ui/                            # Giao diện người dùng
│   │   │   │   ├── SplashActivity.java        # Xin cấp quyền Camera (Chương 1)
│   │   │   │   ├── MainActivity.java          # BottomNavigationView
│   │   │   │   ├── scan/ScanFragment.java     # CameraX + OpenCV + TFLite
│   │   │   │   ├── history/                   # Lịch sử quét & Swipe Delete
│   │   │   │   │   ├── HistoryFragment.java
│   │   │   │   │   └── HistoryRvAdapter.java
│   │   │   │   ├── recipes/                   # Danh sách công thức & BottomSheet
│   │   │   │   │   ├── RecipesFragment.java
│   │   │   │   │   ├── RecipeRvAdapter.java
│   │   │   │   │   └── RecipeDetailBottomSheetDialog.java
│   │   │   │   └── settings/SettingsFragment.java
│   │   │   └── utils/                         # Tiện ích tiền xử lý ảnh & gợi ý
│   │   │       ├── ImageUtils.java            # OpenCV enhance & contour crop (Chương 2)
│   │   │       └── RecipeRecommendationEngine.java
│   │   └── res/                               # Layouts, Drawable, Colors, Values M3
│   └── build.gradle.kts                       # Cấu hình CameraX, OpenCV, Room, TFLite
├── ai_training/
│   └── train_ingredient_classifier.ipynb      # File Jupyter Notebook train model (Buổi 8)
├── docs/                                      # Báo cáo theo kế hoạch 7 buổi
│   ├── BUOI_06_KHAO_SAT_VA_THIET_KE.md        # Use Case & Wireframe
│   ├── BUOI_07_CAMERAX_VA_TIEN_XU_LY.md       # CameraX & OpenCV tiền xử lý
│   ├── BUOI_08_HUAN_LUYEN_MO_HINH_AI.md       # Huấn luyện MobileNetV2
│   ├── BUOI_09_TICH_HOP_TFLITE.md             # Nạp model TFLite vào Android
│   ├── BUOI_10_HOAN_THIEN_UI_UX.md            # Giao diện & Room gợi ý món
│   ├── BUOI_11_KIEM_THU_VA_BAO_CAO.md         # Kịch bản Test & Xuất APK
│   └── BUOI_12_TONG_KET_DO_AN_VA_SLIDE.md     # Báo cáo tổng kết, Slide & Video Demo
├── settings.gradle.kts
├── build.gradle.kts
└── gradlew / gradlew.bat
```

---

## 🚀 Hướng dẫn mở và chạy dự án trong Android Studio

1. **Mở dự án:**
   - Mở **Android Studio**.
   - Chọn **File $\rightarrow$ Open** (hoặc Open Project).
   - Trỏ tới thư mục: `d:\Thầy Hiếu and Như Ái`.
2. **Đồng bộ Gradle (Sync Project):**
   - Chờ Android Studio đồng bộ các thư viện (Gradle Sync).
   - Kiểm tra JDK trong Android Studio: khuyến nghị JDK 17 hoặc JDK 21 (mặc định đi kèm Android Studio JBR).
3. **Chạy ứng dụng (Run App):**
   - Kết nối điện thoại thật qua cáp USB (đã bật USB Debugging) hoặc mở máy ảo Android Emulator (Android 10 trở lên).
   - Nhấn nút **Run 'app'** (phím tắt `Shift + F10`).
   - Cấp quyền Camera khi ứng dụng hiển thị thông báo.

---

## 📝 Bảng đối chiếu tiến độ kế hoạch đồ án (Buổi 6 - Buổi 12)

| STT | Buổi | Nội dung chính | Sản phẩm hoàn thành trong đồ án |
| :---: | :--- | :--- | :--- |
| 1 | **Buổi 6** | Khảo sát và Thiết kế | Sơ đồ Use Case + Wireframe tại [`docs/BUOI_06_KHAO_SAT_VA_THIET_KE.md`](file:///d:/Thầy%20Hiếu%20and%20Như%20Ái/docs/BUOI_06_KHAO_SAT_VA_THIET_KE.md) |
| 2 | **Buổi 7** | Tích hợp CameraX & Tiền xử lý | CameraX chụp ảnh, tiền xử lý OpenCV 224x224 tại [`ScanFragment.java`](file:///d:/Thầy%20Hiếu%20and%20Như%20Ái/app/src/main/java/vn/edu/mku/foodsuggest/ui/scan/ScanFragment.java) & [`ImageUtils.java`](file:///d:/Thầy%20Hiếu%20and%20Như%20Ái/app/src/main/java/vn/edu/mku/foodsuggest/utils/ImageUtils.java) |
| 3 | **Buổi 8** | Huấn luyện mô hình AI | File notebook training Google Colab tại [`train_ingredient_classifier.ipynb`](file:///d:/Thầy%20Hiếu%20and%20Như%20Ái/ai_training/train_ingredient_classifier.ipynb) & xuất `.tflite` |
| 4 | **Buổi 9** | Tích hợp model TFLite | Nạp `.tflite`, chạy dự đoán tại [`IngredientClassifier.java`](file:///d:/Thầy%20Hiếu%20and%20Như%20Ái/app/src/main/java/vn/edu/mku/foodsuggest/ml/IngredientClassifier.java) & [`IngredientDetector.java`](file:///d:/Thầy%20Hiếu%20and%20Như%20Ái/app/src/main/java/vn/edu/mku/foodsuggest/ml/IngredientDetector.java) |
| 5 | **Buổi 10** | Hoàn thiện UI/UX | Giao diện Material 3, CSDL Room, gợi ý món ăn chi tiết tại [`RecipesFragment.java`](file:///d:/Thầy%20Hiếu%20and%20Như%20Ái/app/src/main/java/vn/edu/mku/foodsuggest/ui/recipes/RecipesFragment.java) & [`HistoryFragment.java`](file:///d:/Thầy%20Hiếu%20and%20Như%20Ái/app/src/main/java/vn/edu/mku/foodsuggest/ui/history/HistoryFragment.java) |
| 6 | **Buổi 11** | Kiểm thử, viết báo cáo | Kế hoạch kiểm thử, ma trận Test Cases, hướng dẫn xuất APK tại [`docs/BUOI_11_KIEM_THU_VA_BAO_CAO.md`](file:///d:/Thầy%20Hiếu%20and%20Như%20Ái/docs/BUOI_11_KIEM_THU_VA_BAO_CAO.md) |
| 7 | **Buổi 12** | Hoàn thành đồ án & Thuyết trình | Mã nguồn hoàn chỉnh, Báo cáo tổng kết, Nội dung 10 Slide thuyết trình & Kịch bản Video Demo tại [`docs/BUOI_12_TONG_KET_DO_AN_VA_SLIDE.md`](file:///d:/Thầy%20Hiếu%20and%20Như%20Ái/docs/BUOI_12_TONG_KET_DO_AN_VA_SLIDE.md) |
