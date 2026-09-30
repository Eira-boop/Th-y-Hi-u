# BÁO CÁO BUỔI 11: KIỂM THỬ ỨNG DỤNG VÀ HƯỚNG DẪN XUẤT TẬP TIN APK
**Học phần:** Chuyên đề thiết kế ứng dụng thiết bị số  
**Đơn vị:** Trường Đại học Cửu Long (MKU)  
**Tên đề tài:** Ứng dụng gợi ý món ăn thông minh dựa trên nguyên liệu (SmartCook AI)  

---

## 1. Kế hoạch và Ma trận kiểm thử (Test Matrix)

| Mã kiểm thử | Phân hệ kiểm thử | Điều kiện kiểm thử | Kết quả mong đợi | Trạng thái |
| :--- | :--- | :--- | :--- | :--- |
| **TC01** | Quyền Camera | Mở app lần đầu trên Android 10+ | Hiển thị hộp thoại xin cấp quyền Camera | **ĐẠT (PASS)** |
| **TC02** | Xem trước CameraX | Cấp quyền Camera thành công | PreviewView hiển thị hình ảnh mượt mà, đúng tỷ lệ | **ĐẠT (PASS)** |
| **TC03** | Chụp ảnh đơn (Single Mode) | Bấm nút "Chụp ảnh" khi hướng vào Cà chua | OpenCV vẽ khung, TFLite nhận diện "Cà chua", gợi ý món ăn | **ĐẠT (PASS)** |
| **TC04** | Tự động lưu Room DB | Nhận diện có độ tin cậy $\ge 50\%$ | Bản ghi mới xuất hiện trong `scan_results` kèm thời gian và ảnh | **ĐẠT (PASS)** |
| **TC05** | Nhận diện đa vật thể (Multi Mode)| Bật "Nhiều nguyên liệu: BẬT" và bấm chụp | Vẽ các bounding box số 1, 2 kèm đường viền xanh | **ĐẠT (PASS)** |
| **TC06** | Chạm Bounding Box | Chạm vào khung số 1 trên ảnh | Cắt đúng vật thể, chạy phân loại và hiển thị gợi ý món ăn | **ĐẠT (PASS)** |
| **TC07** | Xem lịch sử & Vuốt xóa | Vuốt một item sang trái trong History | Item bị xóa, CSDL Room cập nhật, hiển thị Toast xác nhận | **ĐẠT (PASS)** |
| **TC08** | Lọc và Tìm kiếm món ăn | Gõ từ khóa "Bò" trong tab Món gợi ý | Hiển thị chính xác các món bò: Bò xào hành tây, Bò lúc lắc... | **ĐẠT (PASS)** |
| **TC09** | Xem chi tiết công thức | Nhấn "Xem Hướng Dẫn Nấu" trên thẻ món | BottomSheet trượt lên hiển thị đầy đủ nguyên liệu và cách nấu | **ĐẠT (PASS)** |
| **TC10** | Cấu hình ngưỡng tin cậy | Kéo thanh trượt trong Cài đặt lên 80% | Lưu vào SharedPreferences, nhận diện dưới 80% không tự lưu | **ĐẠT (PASS)** |

---

## 2. Kiểm thử hiệu năng và bộ nhớ
- **Thời gian khởi động ứng dụng:** < 1.2 giây.
- **Tốc độ suy luận TFLite trên thiết bị:** ~35ms - 65ms / ảnh.
- **Mức chiếm dụng RAM:** Trung bình 95MB - 140MB, không bị tràn bộ nhớ (Memory Leak) nhờ giải phóng `Bitmap.recycle()` và gọi `classifier.close()` trong `onDestroyView()`.

---

## 3. Hướng dẫn đóng gói và xuất tập tin APK (Build APK)

### Cách 1: Sử dụng Gradle Wrapper trong Terminal
Mở terminal tại thư mục gốc của dự án và chạy lệnh:
```bash
./gradlew assembleDebug
```
Sau khi build hoàn tất, file APK sẽ được tạo tại đường dẫn:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Cách 2: Sử dụng menu Android Studio
1. Mở dự án trong Android Studio.
2. Chọn menu: **Build** $\rightarrow$ **Build Bundle(s) / APK(s)** $\rightarrow$ **Build APK(s)**.
3. Khi thông báo *"APK(s) generated successfully"* hiển thị ở góc phải dưới, nhấn **locate** để lấy file APK cài lên điện thoại.
