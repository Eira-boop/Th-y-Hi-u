# BÁO CÁO BUỔI 8: THU THẬP DỮ LIỆU VÀ HUẤN LUYỆN MÔ HÌNH HỌC MÁY (TFLITE)
**Học phần:** Chuyên đề thiết kế ứng dụng thiết bị số  
**Đơn vị:** Trường Đại học Cửu Long (MKU)  
**Tên đề tài:** Ứng dụng gợi ý món ăn thông minh dựa trên nguyên liệu (SmartCook AI)  

---

## 1. Mục tiêu
- Thu thập và phân loại tập dữ liệu hình ảnh nguyên liệu thực phẩm đặc trưng trong ẩm thực Việt Nam.
- Áp dụng kỹ thuật học chuyển giao (Transfer Learning) với mạng nơ-ron tích chập MobileNetV2 tối ưu cho thiết bị di động.
- Xuất mô hình sang định dạng TensorFlow Lite (`.tflite`) tối ưu kích thước dưới 5MB.

---

## 2. Thu thập và Tiền xử lý dữ liệu
- **Nguồn dữ liệu:**
  - Kaggle Food Ingredients Dataset & Fruit 360 Dataset.
  - Hình ảnh thực tế chụp tại chợ/siêu thị Việt Nam (cà chua, đậu phụ, hành tây, trứng gà, thịt heo, thịt bò, tôm tươi...).
- **Quy mô tập dữ liệu:**
  - Tổng số lớp (classes): 15 loại nguyên liệu.
  - Tổng số lượng ảnh: ~4,500 ảnh (khoảng 300 ảnh/lớp).
  - Tỉ lệ phân chia: 80% tập Train (3,600 ảnh), 20% tập Validation (900 ảnh).
- **Kỹ thuật tăng cường dữ liệu (Data Augmentation):**
  - Random Flip (lật ngang).
  - Random Rotation (xoay góc 15 độ).
  - Random Zoom (phóng to/thu nhỏ 10%).
  - Random Contrast (thay đổi độ tương phản giả lập môi trường thiếu sáng/quá sáng).

---

## 3. Kiến trúc mô hình mạng
- **Backbone:** MobileNetV2 (pretrained weights từ ImageNet).
- **Input size:** `(224, 224, 3)`.
- **Top Layers:**
  - `GlobalAveragePooling2D()`
  - `Dropout(0.2)`
  - `Dense(15, activation='softmax')`
- **Hàm mất mát (Loss):** `sparse_categorical_crossentropy`
- **Thuật toán tối ưu (Optimizer):** `Adam(learning_rate=0.001)`

---

## 4. Kết quả huấn luyện
- Số vòng lặp (Epochs): 15 epochs.
- Độ chính xác trên tập Train (Training Accuracy): **95.8%**
- Độ chính xác trên tập Validation (Validation Accuracy): **93.2%**
- Mô hình hội tụ tốt, không xảy ra hiện tượng Overfitting nhờ tầng Dropout và Data Augmentation.

---

## 5. Xuất mô hình sang TensorFlow Lite
- Sử dụng `tf.lite.TFLiteConverter`:
  ```python
  converter = tf.lite.TFLiteConverter.from_keras_model(model)
  converter.optimizations = [tf.lite.Optimize.DEFAULT]
  tflite_model = converter.convert()
  ```
- File xuất: `ingredient_classifier.tflite` (dung lượng ~2.8 MB).
- File nhãn: `ingredient_labels.txt` chứa danh sách 15 nguyên liệu chuẩn tiếng Việt.
- File mã nguồn huấn luyện: `ai_training/train_ingredient_classifier.ipynb` có thể chạy trên Google Colab.
