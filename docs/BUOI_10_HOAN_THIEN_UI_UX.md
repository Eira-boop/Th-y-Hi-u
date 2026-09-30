# BÁO CÁO BUỔI 10: HOÀN THIỆN GIAO DIỆN UI/UX VÀ TÍNH NĂNG GỢI Ý MÓN ĂN THÔNG MINH
**Học phần:** Chuyên đề thiết kế ứng dụng thiết bị số  
**Đơn vị:** Trường Đại học Cửu Long (MKU)  
**Tên đề tài:** Ứng dụng gợi ý món ăn thông minh dựa trên nguyên liệu (SmartCook AI)  

---

## 1. Mục tiêu
- Thiết kế hệ thống giao diện người dùng theo chuẩn Material Design 3 (M3).
- Xây dựng 4 phân hệ chính điều hướng qua BottomNavigationView: Quét ảnh (Scan), Lịch sử (History), Món gợi ý (Recipes), Cài đặt (Settings).
- Hoàn thiện động cơ đề xuất món ăn thông minh (`RecipeRecommendationEngine`) kết hợp Room Database.
- Bổ sung tính năng xem chi tiết công thức nấu ăn, định lượng và mẹo dinh dưỡng bằng BottomSheet Dialog.

---

## 2. Các thành phần giao diện chính

### 2.1. Phân hệ Quét ảnh (ScanFragment)
- Tích hợp lớp phủ hiển thị trực quan thông tin nguyên liệu:
  - Dải màu trạng thái (Xanh lá: Nhận diện thành công, Cam: Dưới ngưỡng tin cậy, Xám: Không nhận diện được).
  - Tên nguyên liệu tiếng Việt (Cà chua, Trứng gà, Thịt bò...).
  - Badge phần trăm độ tin cậy.
  - Khối danh sách các món ăn gợi ý nấu được ngay.
  - Nút bấm *"Xem Công Thức Chi Tiết"* điều hướng mượt mà sang tab Recipes.

### 2.2. Phân hệ Lịch sử (HistoryFragment)
- Hiển thị danh sách các lần quét dạng `CardView` với hình ảnh thu nhỏ tải bằng thư viện `Picasso`.
- Thao tác tương tác hiện đại: `ItemTouchHelper` hỗ trợ **Vuốt sang trái hoặc phải để xóa** bản ghi khỏi Room Database.
- Chế độ rỗng (Empty State) với minh họa tinh tế khi chưa có lịch sử quét.

### 2.3. Phân hệ Món gợi ý (RecipesFragment)
- Kho tàng công thức món ăn phong phú với các món Việt truyền thống.
- Thanh tìm kiếm tức thời (Real-time Filter) theo tên món hoặc nguyên liệu.
- Thẻ món ăn hiển thị đầy đủ: Thời gian chế biến, lượng Calo, độ khó, badge *"Khớp nguyên liệu"*.
- Hộp thoại `RecipeDetailBottomSheetDialog` mở các bước nấu ăn chi tiết từng bước 1, 2, 3...

### 2.4. Phân hệ Cài đặt (SettingsFragment)
- Thanh trượt điều chỉnh Ngưỡng tin cậy AI (10% -> 90%) lưu trữ trong `SharedPreferences`.
- Switch chuyển đổi chế độ Thực đơn chay / thanh đạm.
- Hộp thoại xác nhận xóa sạch lịch sử quét an toàn.

---

## 3. Kiến trúc Động cơ Gợi ý Món ăn (`RecipeRecommendationEngine`)
- **Nguyên lý:** Phân tích một hoặc nhiều nguyên liệu được AI nhận diện, đối sánh với trường `requiredIngredients` trong CSDL Room.
- **Tính toán:** Sắp xếp các món ăn theo mức độ tương đồng và khả dụng của nguyên liệu trong nhà bếp.
- **Dinh dưỡng & An toàn thực phẩm:** Đính kèm các mẹo khoa học như: Lycopene trong cà chua cần chất béo để hấp thu, rau muống chần ngâm đá để giòn và giữ diệp lục tố...
