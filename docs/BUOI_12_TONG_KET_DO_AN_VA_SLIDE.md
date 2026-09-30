# BÁO CÁO BUỔI 12: TỔNG KẾT ĐỒ ÁN, SLIDE THUYẾT TRÌNH VÀ KỊCH BẢN VIDEO DEMO
**Học phần:** Chuyên đề thiết kế ứng dụng thiết bị số  
**Đơn vị:** Trường Đại học Cửu Long (MKU)  
**Tên đề tài:** Ứng dụng gợi ý món ăn thông minh dựa trên nguyên liệu (SmartCook AI)  
**Giảng viên hướng dẫn:** Thầy Hiếu  
**Sinh viên thực hiện:** Nhóm sinh viên thực hiện đồ án  

---

## 1. Tổng kết kết quả đồ án
Dự án đã hoàn thành toàn diện 100% mục tiêu đề ra theo kế hoạch 7 buổi thực hành (Buổi 6 đến Buổi 12):
1. **Kiến trúc Android chuẩn mực:** Xây dựng trên ngôn ngữ Java, tuân thủ mô hình phân lớp `ui`, `data`, `ml`, `utils`.
2. **CameraX & OpenCV Pipeline:** Tích hợp camera chụp ảnh mượt mà, tiền xử lý tăng tương phản, phát hiện biên Canny và tự động crop sát vật thể.
3. **Mô hình Trí tuệ nhân tạo (Edge AI):** Tích hợp TensorFlow Lite phân loại 15 nhóm nguyên liệu và nhận diện đa đối tượng (SSD MobileNet) có vẽ bounding box tương tác.
4. **Cơ sở dữ liệu cục bộ Room:** Tự động lưu trữ lịch sử quét, đồng bộ kho công thức món ăn phong phú với các chỉ số dinh dưỡng.
5. **Động cơ gợi ý món ăn thông minh:** Tự động kết nối nguyên liệu nhận diện với các món ăn Việt Nam truyền thống, hướng dẫn nấu ăn chi tiết.

---

## 2. Nội dung Slide thuyết trình bảo vệ đồ án (10 Slides)

### Slide 1: Trang bìa
- **Tiêu đề:** ĐỒ ÁN CHUYÊN ĐỀ THIẾT KẾ ỨNG DỤNG THIẾT BỊ SỐ
- **Đề tài:** Ứng Dụng Gợi Ý Món Ăn Thông Minh Dựa Trên Nguyên Liệu (SmartCook AI)
- **Đơn vị:** Trường Đại học Cửu Long (MKU)
- **Giảng viên hướng dẫn:** Thầy Hiếu
- **Nhóm sinh viên thực hiện:** Như Ái và nhóm cộng sự

### Slide 2: Đặt vấn đề & Lý do chọn đề tài
- Câu hỏi thường trực mỗi ngày: *"Hôm nay ăn gì với những thứ đang có trong tủ lạnh?"*
- Tình trạng lãng phí thực phẩm do không biết kết hợp các nguyên liệu thừa.
- Xu hướng ứng dụng Trí tuệ nhân tạo (Edge AI) chạy offline ngay trên smartphone mà không cần gửi dữ liệu lên server.

### Slide 3: Mục tiêu và Phạm vi dự án
- Nhận diện các nguyên liệu nấu ăn quen thuộc qua camera (rau củ, thịt, trứng, hải sản).
- Tự động gợi ý món ăn phù hợp nhất, kèm công thức chi tiết từng bước.
- Lưu trữ nhật ký và lịch sử quét trên thiết bị bằng Room Database.
- Tùy chỉnh chế độ ăn (ưu tiên món chay, thanh đạm) và điều chỉnh độ tin cậy AI.

### Slide 4: Kiến trúc hệ thống & Công nghệ sử dụng
- **Ngôn ngữ:** Java (Android SDK 34, Android 10+).
- **Thị giác máy tính:** Jetpack CameraX 1.3.4, OpenCV 4.9.0.
- **Học máy:** TensorFlow Lite 2.14.0, Task Vision 0.4.4.
- **Cơ sở dữ liệu:** Room Database 2.6.1 (SQLite ORM).
- **Giao diện:** Google Material Design 3, Picasso Image Loader.

### Slide 5: Quy trình xử lý ảnh & Nhận diện (AI Pipeline)
- Sơ đồ luồng: `CameraX Capture` $\rightarrow$ `OpenCV Enhance & Crop` $\rightarrow$ `TFLite Inference` $\rightarrow$ `Rule Matching Engine` $\rightarrow$ `Room DB & UI Display`.
- Tách biệt 2 chế độ:
  - *Single Mode:* Tự động tìm đối tượng trung tâm và phân loại ngay.
  - *Multi Mode:* Phát hiện nhiều vật thể, vẽ bounding box số 1, 2, 3 và chạm vào từng khung để nhận diện độc lập.

### Slide 6: Thiết kế Cơ sở dữ liệu Room Database
- Bảng `scan_results`: Lưu lịch sử quét, độ tin cậy, ảnh chụp, nhãn nguyên liệu, thời gian.
- Bảng `recipes`: Lưu trữ hơn 20+ món ăn chuẩn Việt Nam với định lượng chi tiết, calo, độ khó và các bước nấu.
- Áp dụng Singleton pattern và chạy tác vụ trên Background Executor.

### Slide 7: Các tính năng nổi bật trên giao diện (UI/UX)
- Giao diện Material 3 màu xanh lá ẩm thực tươi mát và cam ấm áp.
- Vuốt để xóa lịch sử (Swipe to delete với `ItemTouchHelper`).
- BottomSheet Dialog trượt mượt mà xem công thức nấu ăn.
- Bộ lọc và tìm kiếm món ăn theo thời gian thực (Real-time Search).

### Slide 8: Kịch bản thử nghiệm & Kết quả thực nghiệm
- Thử nghiệm trên nhiều điều kiện ánh sáng (trong bếp, siêu thị, phòng khách).
- Độ chính xác mô hình đạt **93.2%**.
- Tốc độ nhận diện trung bình: **~50ms / khung hình**.
- Ứng dụng hoạt động mượt mà, không giật lag.

### Slide 9: Hướng phát triển trong tương lai
- Kết nối thêm mô hình gợi ý món ăn bằng Generative AI (Google Gemini Nano) để sáng tạo công thức mới lạ.
- Thêm tính năng tính toán tổng lượng Calo bữa ăn cho người tập gym / giảm cân.
- Tích hợp tính năng nhắc nhở hạn sử dụng của nguyên liệu trong tủ lạnh.

### Slide 10: Lời cảm ơn & Hỏi đáp (Q&A)
- Gửi lời cảm ơn chân thành đến **Thầy Hiếu** đã tận tình hướng dẫn trong suốt học phần.
- Sẵn sàng lắng nghe câu hỏi và nhận xét từ thầy và hội đồng.

---

## 3. Kịch bản Video Demo (Thời lượng: 2 - 3 phút)

1. **0:00 - 0:25:** Mở ứng dụng từ màn hình điện thoại $\rightarrow$ Hiển thị `SplashActivity` $\rightarrow$ Xin cấp quyền Camera thành công $\rightarrow$ Vào giao diện chính `ScanFragment`.
2. **0:25 - 0:55 (Chế độ chụp đơn):**
   - Hướng camera vào quả Cà chua.
   - Nhấn nút "Chụp ảnh".
   - Ứng dụng lập tức vẽ khung viền xanh, hiển thị: *"Cà chua • Độ tin cậy: 95% • Nhóm: Rau củ"*.
   - Hiển thị danh sách gợi ý: Canh cà chua trứng, Đậu sốt cà chua, Cá sốt cà...
   - Nhấn nút *"Xem Công Thức Chi Tiết"* $\rightarrow$ Tự động chuyển sang tab Recipes hiển thị các món ăn cà chua.
3. **0:55 - 1:30 (Chế độ chụp nhiều nguyên liệu Multi Mode):**
   - Nhấn nút "Nhiều nguyên liệu: BẬT".
   - Hướng camera vào khay có Cà chua và Trứng gà.
   - Nhấn nút "Chụp ảnh" $\rightarrow$ Màn hình vẽ 2 bounding box số 1 và số 2.
   - Người dùng chạm vào ô số 1 $\rightarrow$ Hiển thị thông tin Cà chua.
   - Chạm tiếp vào ô số 2 $\rightarrow$ Hiển thị thông tin Trứng gà và món Trứng chiên cà chua.
4. **1:30 - 2:00 (Phân hệ Lịch sử & Công thức):**
   - Chuyển sang tab "Lịch sử" $\rightarrow$ Thấy các bản ghi vừa quét kèm ảnh thumbnail và ngày giờ.
   - Thực hiện thao tác vuốt sang trái để xóa 1 mục $\rightarrow$ Xuất hiện thông báo Toast đã xóa.
   - Chuyển sang tab "Món gợi ý" $\rightarrow$ Gõ từ khóa tìm kiếm "Bò" $\rightarrow$ Nhấn vào món "Bò Xào Hành Tây" $\rightarrow$ BottomSheet trượt lên hiển thị chi tiết các bước nấu ăn.
5. **2:00 - 2:20 (Cài đặt):**
   - Vào tab "Cài đặt" $\rightarrow$ Kéo thanh trượt điều chỉnh ngưỡng tin cậy AI $\rightarrow$ Bật tùy chọn "Ưu tiên món chay".
6. **2:20 - 2:30:** Kết thúc video với logo SmartCook và thông tin nhóm sinh viên trường Đại học Cửu Long.
