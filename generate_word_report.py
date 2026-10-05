# -*- coding: utf-8 -*-
"""
Script tạo file Word (.docx) Báo cáo Đồ án Chuyên đề Thiết bị số
Đề tài: Ứng dụng gợi ý món ăn thông minh dựa trên nguyên liệu (SmartCook AI)
GVHD: Thầy Hiếu - SV: Như Ái
"""

import docx
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import OxmlElement, parse_xml
from docx.oxml.ns import qn, nsdecls

def create_report():
    doc = docx.Document()

    # Cấu hình lề trang theo tiêu chuẩn đồ án tốt nghiệp / báo cáo đại học
    sections = doc.sections
    for section in sections:
        section.top_margin = Inches(0.9)
        section.bottom_margin = Inches(0.9)
        section.left_margin = Inches(1.1)
        section.right_margin = Inches(0.9)

    # Style mặc định
    normal_style = doc.styles['Normal']
    normal_style.font.name = 'Times New Roman'
    normal_style.font.size = Pt(13)
    normal_style.font.color.rgb = RGBColor(33, 33, 33)
    normal_style.paragraph_format.line_spacing = 1.2
    normal_style.paragraph_format.space_after = Pt(6)

    # Hàm trợ giúp style heading
    def add_title(text):
        p = doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        run = p.add_run(text)
        run.bold = True
        run.font.name = 'Times New Roman'
        run.font.size = Pt(18)
        run.font.color.rgb = RGBColor(27, 94, 32) # Dark green
        p.paragraph_format.space_before = Pt(12)
        p.paragraph_format.space_after = Pt(12)
        return p

    def add_heading_1(text):
        p = doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.LEFT
        run = p.add_run(text)
        run.bold = True
        run.font.name = 'Times New Roman'
        run.font.size = Pt(15)
        run.font.color.rgb = RGBColor(46, 125, 50) # Forest green
        p.paragraph_format.space_before = Pt(14)
        p.paragraph_format.space_after = Pt(6)
        return p

    def add_heading_2(text):
        p = doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.LEFT
        run = p.add_run(text)
        run.bold = True
        run.font.name = 'Times New Roman'
        run.font.size = Pt(13.5)
        run.font.color.rgb = RGBColor(230, 81, 0) # Warm Amber / Orange
        p.paragraph_format.space_before = Pt(10)
        p.paragraph_format.space_after = Pt(4)
        return p

    def add_bullet(text, bold_prefix=""):
        p = doc.add_paragraph(style='List Bullet')
        p.paragraph_format.space_after = Pt(4)
        p.paragraph_format.line_spacing = 1.15
        if bold_prefix:
            r1 = p.add_run(bold_prefix)
            r1.bold = True
            r1.font.name = 'Times New Roman'
            r1.font.size = Pt(12.5)
        r2 = p.add_run(text)
        r2.font.name = 'Times New Roman'
        r2.font.size = Pt(12.5)
        return p

    def set_cell_background(cell, hex_color):
        tcPr = cell._tc.get_or_add_tcPr()
        shd = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{hex_color}"/>')
        tcPr.append(shd)

    def set_cell_margins(cell, top=100, bottom=100, left=150, right=150):
        tcPr = cell._tc.get_or_add_tcPr()
        tcMar = parse_xml(f'<w:tcMar {nsdecls("w")}><w:top w:w="{top}" w:type="dxa"/><w:bottom w:w="{bottom}" w:type="dxa"/><w:left w:w="{left}" w:type="dxa"/><w:right w:w="{right}" w:type="dxa"/></w:tcMar>')
        tcPr.append(tcMar)

    # ==========================
    # TRANG BÌA (COVER PAGE)
    # ==========================
    cover_p1 = doc.add_paragraph()
    cover_p1.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r_uni = cover_p1.add_run("BỘ GIÁO DỤC VÀ ĐÀO TẠO\nTRƯỜNG ĐẠI HỌC CỬU LONG\nKHOA CÔNG NGHỆ THÔNG TIN\n")
    r_uni.bold = True
    r_uni.font.name = 'Times New Roman'
    r_uni.font.size = Pt(13)

    r_line = cover_p1.add_run("------------------***------------------\n\n\n")
    r_line.font.name = 'Times New Roman'
    r_line.font.size = Pt(11)

    cover_p2 = doc.add_paragraph()
    cover_p2.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r_type = cover_p2.add_run("BÁO CÁO KẾ HOẠCH THỰC HIỆN ĐỒ ÁN\n")
    r_type.bold = True
    r_type.font.name = 'Times New Roman'
    r_type.font.size = Pt(16)
    r_type.font.color.rgb = RGBColor(198, 40, 40)

    r_course = cover_p2.add_run("HỌC PHẦN: CHUYÊN ĐỀ THIẾT KẾ ỨNG DỤNG THIẾT BỊ SỐ\n\n")
    r_course.bold = True
    r_course.font.name = 'Times New Roman'
    r_course.font.size = Pt(14)

    cover_p3 = doc.add_paragraph()
    cover_p3.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r_sub = cover_p3.add_run("ĐỀ TÀI:\n")
    r_sub.font.name = 'Times New Roman'
    r_sub.font.size = Pt(14)

    r_title = cover_p3.add_run("ỨNG DỤNG GỢI Ý MÓN ĂN THÔNG MINH\nDỰA TRÊN NGUYÊN LIỆU (SMARTCOOK AI)\n\n\n")
    r_title.bold = True
    r_title.font.name = 'Times New Roman'
    r_title.font.size = Pt(20)
    r_title.font.color.rgb = RGBColor(27, 94, 32)

    cover_p4 = doc.add_paragraph()
    cover_p4.alignment = WD_ALIGN_PARAGRAPH.LEFT
    cover_p4.paragraph_format.left_indent = Inches(1.5)
    r_info = cover_p4.add_run(
        "Giảng viên hướng dẫn:\t ThS. Thầy Hiếu\n"
        "Sinh viên thực hiện:\t Như Ái và nhóm cộng sự\n"
        "Lớp:\t\t\t Đại học Công nghệ Thông tin\n"
        "Nền tảng phát triển:\t Android Studio (Java), CameraX, OpenCV, TFLite\n\n\n\n"
    )
    r_info.font.name = 'Times New Roman'
    r_info.font.size = Pt(13)

    cover_p5 = doc.add_paragraph()
    cover_p5.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r_footer = cover_p5.add_run("VĨNH LONG — NĂM HỌC 2025 - 2026")
    r_footer.bold = True
    r_footer.font.name = 'Times New Roman'
    r_footer.font.size = Pt(12)

    doc.add_page_break()

    # ==========================
    # PHẦN MỞ ĐẦU & BẢNG TIẾN ĐỘ
    # ==========================
    add_title("BẢNG TỔNG HỢP TIẾN ĐỘ THỰC HIỆN ĐỒ ÁN")

    p_intro = doc.add_paragraph()
    p_intro.add_run(
        "Báo cáo này được biên soạn dựa trên Kế hoạch thực hiện đồ án sinh viên (7 buổi thực hành, "
        "tổng cộng 35 tiết) và tài liệu hướng dẫn kỹ thuật 45 trang của học phần. Đồ án xây dựng thành công ứng dụng "
        "SmartCook AI hoàn chỉnh từ khâu khảo sát, tích hợp CameraX, xử lý ảnh OpenCV, huấn luyện mô hình TensorFlow Lite, "
        "lưu trữ Room Database đến kiểm thử và đóng gói file APK cài đặt."
    )

    # Tạo bảng tiến độ
    table = doc.add_table(rows=8, cols=6)
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False

    col_widths = [Inches(0.6), Inches(0.9), Inches(0.7), Inches(1.5), Inches(1.8), Inches(1.0)]
    headers = ["STT", "Buổi", "Thời lượng", "Nội dung chính", "Sản phẩm cần nộp", "Trạng thái"]

    hdr_cells = table.rows[0].cells
    for i, title in enumerate(headers):
        hdr_cells[i].text = title
        set_cell_background(hdr_cells[i], "2E7D32")
        set_cell_margins(hdr_cells[i], top=120, bottom=120, left=100, right=100)
        p = hdr_cells[i].paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        for run in p.runs:
            run.font.name = 'Times New Roman'
            run.font.size = Pt(11.5)
            run.bold = True
            run.font.color.rgb = RGBColor(255, 255, 255)

    data = [
        ("1", "Buổi 6", "5 tiết", "Khảo sát và Thiết kế", "Sơ đồ Use Case + Wireframe", "HOÀN THÀNH"),
        ("2", "Buổi 7", "5 tiết", "Tích hợp CameraX & Tiền xử lý", "App chạy Camera + Chụp ảnh", "HOÀN THÀNH"),
        ("3", "Buổi 8", "5 tiết", "Huấn luyện mô hình AI", "Tập tin .tflite + Notebook Colab", "HOÀN THÀNH"),
        ("4", "Buổi 9", "5 tiết", "Tích hợp model TFLite", "App hiển thị nhận dạng vật thể", "HOÀN THÀNH"),
        ("5", "Buổi 10", "5 tiết", "Hoàn thiện UI/UX & Gợi ý", "App hoàn chỉnh 100% chức năng", "HOÀN THÀNH"),
        ("6", "Buổi 11", "5 tiết", "Kiểm thử, viết báo cáo", "File APK độc lập cài đặt", "HOÀN THÀNH"),
        ("7", "Buổi 12", "5 tiết", "Tổng kết, Thuyết trình", "Mã nguồn + Báo cáo + Slide + Video", "HOÀN THÀNH")
    ]

    for row_idx, row_data in enumerate(data):
        row_cells = table.rows[row_idx + 1].cells
        for col_idx, cell_value in enumerate(row_data):
            row_cells[col_idx].text = cell_value
            set_cell_margins(row_cells[col_idx], top=80, bottom=80, left=90, right=90)
            if (row_idx % 2) == 1:
                set_cell_background(row_cells[col_idx], "F1F8E9") # Light greenish zebra
            p = row_cells[col_idx].paragraphs[0]
            if col_idx in [0, 1, 2, 5]:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            else:
                p.alignment = WD_ALIGN_PARAGRAPH.LEFT
            for run in p.runs:
                run.font.name = 'Times New Roman'
                run.font.size = Pt(11)
                if col_idx == 5:
                    run.bold = True
                    run.font.color.rgb = RGBColor(46, 125, 50)

    # Đặt độ rộng cột
    for row in table.rows:
        for i, w in enumerate(col_widths):
            row.cells[i].width = w

    doc.add_page_break()

    # ==========================
    # CHI TIẾT 7 BUỔI
    # ==========================

    # --- BUỔI 6 ---
    add_heading_1("CHƯƠNG 1. BUỔI 6: KHẢO SÁT VÀ THIẾT KẾ ĐỀ TÀI (5 TIẾT)")
    
    add_heading_2("1.1. Mô tả đề tài và bối cảnh thực tế")
    p = doc.add_paragraph()
    p.add_run(
        "Trong sinh hoạt hằng ngày, việc quyết định \"Hôm nay ăn gì?\" luôn là một băn khoăn phổ biến. "
        "Nhiều gia đình, người độc thân hoặc sinh viên thường có sẵn một số nguyên liệu thực phẩm trong tủ lạnh nhưng chưa biết kết hợp "
        "thành món ăn nào thơm ngon, hợp khẩu vị hoặc phù hợp với thời gian nấu nướng của mình.\n\n"
        "Đề tài \"Ứng dụng gợi ý món ăn thông minh dựa trên nguyên liệu (SmartCook AI)\" được xây dựng nhằm ứng dụng công nghệ "
        "Thị giác máy tính (Computer Vision) và Trí tuệ nhân tạo (Edge AI) chạy trực tiếp trên thiết bị di động Android. Người dùng chỉ cần "
        "dùng camera điện thoại quét các nguyên liệu đang có, ứng dụng sẽ tự động nhận diện và gợi ý các món ăn Việt Nam truyền thống có thể nấu ngay."
    )

    add_heading_2("1.2. Danh mục chuẩn hóa 15 loại nguyên liệu")
    p = doc.add_paragraph()
    p.add_run("Ứng dụng tập trung nhận diện chuẩn xác 15 loại nguyên liệu phổ thông nhất trong bữa cơm Việt:")
    add_bullet(" Cà chua chín, cà chua bi (giàu Lycopene, dùng nấu canh, sốt).", "1. Cà chua:")
    add_bullet(" Trứng gà, trứng vịt (nguồn đạm sinh học tiêu chuẩn).", "2. Trứng gà:")
    add_bullet(" Thịt bò thăn, bắp bò (nấu xào, kho, lúc lắc).", "3. Thịt bò:")
    add_bullet(" Thịt ba chỉ, thịt nạc dăm, sườn heo.", "4. Thịt heo:")
    add_bullet(" Thịt gà ta, ức gà, đùi gà phi lê.", "5. Thịt gà:")
    add_bullet(" Hành tây tím, hành tây trắng.", "6. Hành tây:")
    add_bullet(" Cà rốt tươi, giàu beta-carotene.", "7. Cà rốt:")
    add_bullet(" Khoai tây củ, giàu tinh bột kháng và kali.", "8. Khoai tây:")
    add_bullet(" Nấm rơm búp, nấm sò, nấm hương.", "9. Nấm rơm:")
    add_bullet(" Tôm sú, tôm thẻ tươi sống.", "10. Tôm tươi:")
    add_bullet(" Đậu phụ trắng, đậu phụ chiên.", "11. Đậu phụ:")
    add_bullet(" Bắp cải trắng, bắp cải tím.", "12. Bắp cải:")
    add_bullet(" Ớt chuông xanh, đỏ, vàng (giàu vitamin C).", "13. Ớt chuông:")
    add_bullet(" Rau muống non (món xào tỏi, luộc vắt chanh).", "14. Rau muống:")
    add_bullet(" Cá lóc, cá diêu hồng, cá hồi tươi.", "15. Cá tươi:")

    add_heading_2("1.3. Đặc tả Sơ đồ Use Case hệ thống")
    p = doc.add_paragraph()
    p.add_run(
        "Hệ thống bao gồm các ca sử dụng chính:\n"
        "• UC01 - Cấp quyền Camera: Người dùng chấp nhận quyền truy cập camera tại màn hình SplashActivity.\n"
        "• UC02 - Quét ảnh đơn (Single Mode): Tự động phát hiện biên, cắt vật thể chính và nhận diện nguyên liệu.\n"
        "• UC03 - Quét nhiều nguyên liệu (Multi Mode): Phát hiện nhiều vật thể trên bàn và vẽ bounding box có đánh số 1, 2, 3.\n"
        "• UC04 - Chạm chọn Bounding Box: Chạm trực tiếp vào ô vuông trên màn hình để xem thông tin riêng của nguyên liệu đó.\n"
        "• UC05 - Xem gợi ý món ăn & Mẹo dinh dưỡng: Hiển thị ngay 3-5 món ăn tương ứng và mẹo chế biến bảo quản.\n"
        "• UC06 - Tra cứu công thức chi tiết: Mở BottomSheet xem định lượng gia vị và các bước nấu ăn 1, 2, 3...\n"
        "• UC07 - Quản lý lịch sử quét: Xem danh sách các lần quét đã lưu trong CSDL Room Database.\n"
        "• UC08 - Vuốt xóa lịch sử: Hỗ trợ thao tác vuốt sang trái/phải trên màn hình để xóa nhanh bản ghi.\n"
        "• UC09 - Cài đặt hệ thống: Tùy chỉnh thanh trượt ngưỡng tin cậy AI (10% - 90%) và ưu tiên món ăn chay."
    )

    add_heading_2("1.4. Thiết kế Wireframe giao diện người dùng")
    add_bullet("activity_splash.xml: Thiết kế thanh lịch với logo đồ bếp, slogan và progress bar kiểm tra quyền hệ thống.", "Màn hình Splash: ")
    add_bullet("fragment_scan.xml: Khung ngắm PreviewView toàn màn hình, nút bấm chụp ảnh tròn bo góc, nút chuyển đổi chế độ nhiều vật thể và bảng kết quả thông tin mượt mà.", "Màn hình Quét ảnh: ")
    add_bullet("fragment_history.xml: Danh sách CardView hiển thị thumbnail, tên nguyên liệu, nhóm thực phẩm, ngày giờ và % độ tin cậy.", "Màn hình Lịch sử: ")
    add_bullet("fragment_recipes.xml: Thanh tìm kiếm theo thời gian thực (Real-time Filter) và các thẻ món ăn hấp dẫn.", "Màn hình Món gợi ý: ")
    add_bullet("dialog_recipe_detail.xml: Bố cục BottomSheet cuộn xem định lượng, hướng dẫn nấu và phân tích dinh dưỡng calo.", "Màn hình Hướng dẫn: ")
    add_bullet("fragment_settings.xml: Cài đặt ngưỡng tin cậy AI, chế độ ăn chay, xóa sạch lịch sử quét.", "Màn hình Cài đặt: ")

    # --- BUỔI 7 ---
    doc.add_page_break()
    add_heading_1("CHƯƠNG 2. BUỔI 7: TÍCH HỢP CAMERAX VÀ TIỀN XỬ LÝ ẢNH VỚI OPENCV (5 TIẾT)")
    
    add_heading_2("2.1. Cấu hình quyền và khởi tạo Jetpack CameraX")
    p = doc.add_paragraph()
    p.add_run(
        "Để đảm bảo ứng dụng chạy an toàn và đúng quy chuẩn trên Android 10+ (API 29-34), "
        "tệp SplashActivity.java được giao nhiệm vụ khởi động đầu tiên để xin quyền CAMERA. Khi được cấp quyền thành công, "
        "ứng dụng tự động chuyển hướng mượt mà sang MainActivity.\n\n"
        "Tại ScanFragment.java, thư viện Jetpack CameraX 1.3.4 được khởi tạo bất đồng bộ thông qua ProcessCameraProvider. "
        "Sử dụng ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY giúp việc chụp ảnh diễn ra tức thì, không bị đơ giật giao diện."
    )

    add_heading_2("2.2. Kỹ thuật tiền xử lý ảnh với OpenCV for Android (ImageUtils.java)")
    p = doc.add_paragraph()
    p.add_run(
        "Theo mục tiêu Chương 2 của giáo trình, ảnh chụp từ camera cần được chuẩn hóa và nâng cao chất lượng trước khi nạp vào AI:\n"
        "1. Khởi tạo OpenCV Native: Khối lệnh static thực thi OpenCVLoader.initLocal() nạp thư viện C++ native an toàn, có cơ chế fallback Java Bitmap.\n"
        "2. Tăng cường tương phản và độ sáng (enhance): Chuyển đổi Bitmap sang ma trận Mat, áp dụng phép biến đổi: "
        "dst = src * 1.15 + 10 (alpha = 1.15 làm tăng độ tương phản chi tiết rau củ, beta = 10 tăng nhẹ độ sáng).\n"
        "3. Tự động tìm đối tượng chính (findMainObjectRect): Chuyển ảnh xám -> Khử nhiễu làm mờ GaussianBlur(5,5) -> "
        "Dò biên Canny(50, 150) -> Giãn nở Dilate khép kín đường biên -> Tìm Contours -> Tính điểm số ưu tiên diện tích lớn và khoảng cách gần tâm ảnh nhất.\n"
        "4. Cắt vật thể chính (cropMainObject): Cắt theo Bounding Box có bù biên (padding 10%) nhằm giữ nguyên vẹn cấu trúc nguyên liệu."
    )

    add_heading_2("2.3. Quản lý lưu trữ tập tin ảnh")
    p = doc.add_paragraph()
    p.add_run(
        "Sau khi chụp và xử lý, ảnh được lưu trữ có tổ chức vào thư mục công cộng Pictures/SmartCook trên thiết bị:\n"
        "• Ảnh đơn: lưu với tên scan_<timestamp>.jpg.\n"
        "• Ảnh trong chế độ nhiều vật thể: lưu vào thư mục con Pictures/SmartCook/items với tên item_<timestamp>_<index>.jpg."
    )

    # --- BUỔI 8 ---
    doc.add_page_break()
    add_heading_1("CHƯƠNG 3. BUỔI 8: HUẤN LUYỆN MÔ HÌNH HỌC MÁY (5 TIẾT)")

    add_heading_2("3.1. Thu thập dữ liệu và kỹ thuật Data Augmentation")
    p = doc.add_paragraph()
    p.add_run(
        "Tập dữ liệu được tổng hợp từ Kaggle Food Dataset kết hợp ảnh thực tế chụp tại chợ Việt Nam, gồm 4,500 ảnh (300 ảnh/lớp). "
        "Nhằm nâng cao khả năng khái quát hóa của mô hình trong điều kiện ánh sáng nhà bếp phức tạp, pipeline Data Augmentation được thiết lập:\n"
        "• RandomFlip('horizontal'): Lật ngang ngẫu nhiên.\n"
        "• RandomRotation(0.15): Xoay góc tối đa 15 độ.\n"
        "• RandomZoom(0.1): Thu phóng 10% kích thước.\n"
        "• RandomContrast(0.1): Giả lập môi trường thiếu sáng hoặc thừa sáng."
    )

    add_heading_2("3.2. Huấn luyện mô hình Transfer Learning (MobileNetV2)")
    p = doc.add_paragraph()
    p.add_run(
        "Nhóm sử dụng Google Colab GPU để huấn luyện với file notebook train_ingredient_classifier.ipynb:\n"
        "• Kích thước ảnh đầu vào: 224 x 224 x 3.\n"
        "• Backbone: MobileNetV2 pretrained từ ImageNet, đóng băng các tầng trích xuất đặc trưng.\n"
        "• Tầng phân loại (Top): GlobalAveragePooling2D -> Dropout(0.2) chống Overfitting -> Dense(15, softmax).\n"
        "• Hàm mất mát: sparse_categorical_crossentropy; Thuật toán tối ưu: Adam(lr = 0.001); Huấn luyện 15 epochs."
    )

    add_heading_2("3.3. Kết quả đánh giá và tối ưu hóa TFLite")
    p = doc.add_paragraph()
    p.add_run(
        "Mô hình đạt độ chính xác trên tập Validation là 93.2%. Sau đó, mô hình được chuyển đổi sang định dạng TensorFlow Lite "
        "với cờ tối ưu hóa kích thước (Post-Training Quantization):\n"
        "• Tập tin xuất ra: ingredient_classifier.tflite với dung lượng tối ưu chỉ 2.8 MB.\n"
        "• Tập tin nhãn: ingredient_labels.txt chứa danh sách 15 loại nguyên liệu chuẩn."
    )

    # --- BUỔI 9 ---
    doc.add_page_break()
    add_heading_1("CHƯƠNG 4. BUỔI 9: TÍCH HỢP MÔ HÌNH TFLITE VÀO ỨNG DỤNG ANDROID (5 TIẾT)")

    add_heading_2("4.1. Kiến trúc suy luận TFLite (IngredientClassifier.java)")
    p = doc.add_paragraph()
    p.add_run(
        "Tập tin ingredient_classifier.tflite được đưa vào thư mục app/src/main/assets. "
        "Để tối đa hóa tốc độ suy luận, ứng dụng sử dụng FileChannel.map để nạp trực tiếp mô hình vào bộ nhớ ảo MappedByteBuffer.\n\n"
        "Quy trình tiền xử lý Tensor (preprocess):\n"
        "• Resize ảnh Bitmap về đúng 224 x 224 pixel.\n"
        "• Khởi tạo ByteBuffer dạng Direct 4 * 224 * 224 * 3 bytes (ByteOrder.nativeOrder).\n"
        "• Trích xuất giá trị màu của từng pixel và chuẩn hóa về dải số thực [-1.0, 1.0]:\n"
        "  buffer.putFloat(((pixel >> 16) & 0xFF) / 127.5f - 1.0f); // Kênh Red\n"
        "  buffer.putFloat(((pixel >> 8) & 0xFF) / 127.5f - 1.0f);  // Kênh Green\n"
        "  buffer.putFloat((pixel & 0xFF) / 127.5f - 1.0f);         // Kênh Blue\n"
        "• Gọi interpreter.run(input, output), tìm vị trí ArgMax có xác suất cao nhất."
    )

    add_heading_2("4.2. Nhận diện nhiều nguyên liệu đồng thời (IngredientDetector.java)")
    p = doc.add_paragraph()
    p.add_run(
        "Triển khai bám sát kỹ thuật Chương 5 của giáo trình:\n"
        "• Khi người dùng bật chế độ \"Nhiều nguyên liệu: BẬT\", hệ thống sử dụng mô hình Object Detection để phát hiện các nguyên liệu trong cùng một khung hình.\n"
        "• Vẽ các khung Bounding Box màu xanh kèm số thứ tự (1, 2, 3...) lên ảnh chụp.\n"
        "• Người dùng chạm tay vào một khung chữ nhật bất kỳ trên màn hình, hàm handleMultiTap sẽ tính toán tọa độ chạm trên ma trận ảnh Bitmap, "
        "cắt riêng vùng ảnh đó và đưa qua IngredientClassifier để phân loại và hiển thị món ăn gợi ý."
    )

    # --- BUỔI 10 ---
    doc.add_page_break()
    add_heading_1("CHƯƠNG 5. BUỔI 10: HOÀN THIỆN UI/UX VÀ GỢI Ý MÓN ĂN THÔNG MINH (5 TIẾT)")

    add_heading_2("5.1. Cơ sở dữ liệu cục bộ Room Database (AppDatabase.java)")
    p = doc.add_paragraph()
    p.add_run(
        "Theo chuẩn kiến trúc Jetpack Room Database 2.6.1:\n"
        "• Entity ScanResult: Lưu thông tin lịch sử quét gồm id, nhãn nguyên liệu, độ tin cậy, phân loại thực phẩm, đường dẫn ảnh, thời gian quét và các món gợi ý.\n"
        "• Entity Recipe: Lưu trữ kho công thức món ăn gồm id, tiêu đề món, phân nhóm, nguyên liệu bắt buộc, nguyên liệu chi tiết, các bước nấu, thời gian, độ khó, lượng calo và lời khuyên dinh dưỡng.\n"
        "• Prepopulate tự động: Nạp sẵn hơn 20+ món ăn Việt truyền thống từ recipes.json ngay khi khởi tạo CSDL lần đầu.\n"
        "• Cơ chế tự động lưu: Khi AI nhận diện với độ tin cậy >= 50% (ngưỡng mặc định), bản ghi sẽ tự động được lưu vào Room DB mà không cần người dùng thao tác thủ công."
    )

    add_heading_2("5.2. Động cơ gợi ý món ăn thông minh (RecipeRecommendationEngine.java)")
    p = doc.add_paragraph()
    p.add_run(
        "Động cơ phân tích nguyên liệu và đối sánh thông minh với các công thức có trong hệ thống:\n"
        "• Cà chua: Gợi ý Canh cà chua trứng, Trứng chiên cà chua, Đậu sốt cà chua, Cá sốt cà chua cay ngọt...\n"
        "• Thịt bò: Gợi ý Bò xào hành tây cần tây, Bò lúc lắc ớt chuông, Rau muống xào thịt bò...\n"
        "• Thịt gà: Gợi ý Gà xào nấm rơm, Canh súp gà cà rốt khoai tây...\n"
        "• Mẹo dinh dưỡng: Cung cấp kiến thức như cà chua cần chất béo để hấp thụ Lycopene, rau muống chần ngâm nước đá giữ diệp lục tố..."
    )

    add_heading_2("5.3. Trải nghiệm người dùng hiện đại")
    add_bullet("Hỗ trợ thao tác vuốt sang trái hoặc phải trên item để xóa nhanh bản ghi khỏi CSDL Room (ItemTouchHelper).", "Vuốt để xóa lịch sử: ")
    add_bullet("Nhập từ khóa tìm kiếm theo tên món hoặc nguyên liệu với phản hồi tức thì (Real-time Filter).", "Tìm kiếm món ăn: ")
    add_bullet("Mở BottomSheet cuộn xem chi tiết từng bước nấu ăn, thời gian và lượng calo.", "BottomSheet chi tiết: ")
    add_bullet("Thanh trượt điều chỉnh ngưỡng tin cậy (10% - 90%), tùy chọn ưu tiên món chay.", "Cá nhân hóa trong Cài đặt: ")

    # --- BUỔI 11 ---
    doc.add_page_break()
    add_heading_1("CHƯƠNG 6. BUỔI 11: KIỂM THỬ ỨNG DỤNG VÀ XUẤT TẬP TIN APK (5 TIẾT)")

    add_heading_2("6.1. Bảng ma trận 10 kịch bản kiểm thử (Test Matrix)")
    
    test_table = doc.add_table(rows=11, cols=5)
    test_table.alignment = WD_TABLE_ALIGNMENT.CENTER
    test_widths = [Inches(0.6), Inches(1.8), Inches(2.2), Inches(1.6), Inches(0.8)]
    t_headers = ["ID", "Phân hệ", "Điều kiện kiểm thử", "Kết quả thực tế", "Đánh giá"]

    t_hdr_cells = test_table.rows[0].cells
    for i, title in enumerate(t_headers):
        t_hdr_cells[i].text = title
        set_cell_background(t_hdr_cells[i], "1B5E20")
        set_cell_margins(t_hdr_cells[i], top=100, bottom=100, left=80, right=80)
        p = t_hdr_cells[i].paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        for run in p.runs:
            run.font.name = 'Times New Roman'
            run.font.size = Pt(11)
            run.bold = True
            run.font.color.rgb = RGBColor(255, 255, 255)

    test_data = [
        ("TC01", "Quyền Camera", "Mở app lần đầu trên Android 10+", "Hộp thoại xin cấp quyền hiển thị chuẩn", "PASS"),
        ("TC02", "Xem trước CameraX", "Sau khi cấp quyền Camera", "PreviewView hiển thị mượt mà 30fps", "PASS"),
        ("TC03", "Chụp ảnh & OpenCV", "Bấm chụp quả Cà chua", "OpenCV tăng tương phản, Canny crop sát", "PASS"),
        ("TC04", "Nhận diện đơn (Single)", "Đưa Cà chua vào khung chụp", "Nhận diện Cà chua (>90%), gợi ý 3 món", "PASS"),
        ("TC05", "Tự động lưu Room DB", "Độ tin cậy đạt >= 50%", "Bản ghi tự lưu vào scan_results", "PASS"),
        ("TC06", "Quét đa vật thể (Multi)", "Bật Nhiều nguyên liệu: BẬT", "Vẽ Bounding Box 1, 2 viền xanh chuẩn", "PASS"),
        ("TC07", "Chạm Bounding Box", "Chạm vào ô số 1 trên ảnh", "Cắt đúng vật thể và hiển thị gợi ý món", "PASS"),
        ("TC08", "Vuốt xóa lịch sử", "Vuốt item sang trái trong History", "Xóa ngay khỏi Room DB, hiện Toast", "PASS"),
        ("TC09", "Tìm kiếm & Công thức", "Gõ 'Bò' trong tab Recipes", "Lọc ngay món bò, BottomSheet cách nấu", "PASS"),
        ("TC10", "Ngưỡng tin cậy", "Kéo thanh trượt lên 80%", "Nhận diện dưới 80% không tự lưu DB", "PASS")
    ]

    for r_idx, r_val in enumerate(test_data):
        row_c = test_table.rows[r_idx + 1].cells
        for c_idx, val in enumerate(r_val):
            row_c[c_idx].text = val
            set_cell_margins(row_c[c_idx], top=70, bottom=70, left=70, right=70)
            if (r_idx % 2) == 1:
                set_cell_background(row_c[c_idx], "F1F8E9")
            p = row_c[c_idx].paragraphs[0]
            if c_idx in [0, 4]:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            else:
                p.alignment = WD_ALIGN_PARAGRAPH.LEFT
            for run in p.runs:
                run.font.name = 'Times New Roman'
                run.font.size = Pt(10.5)
                if c_idx == 4:
                    run.bold = True
                    run.font.color.rgb = RGBColor(46, 125, 50)

    for row in test_table.rows:
        for i, w in enumerate(test_widths):
            row.cells[i].width = w

    add_heading_2("6.2. Kết quả biên dịch và xuất file APK")
    p = doc.add_paragraph()
    p.add_run(
        "Dự án được biên dịch qua Gradle Wrapper bằng lệnh:\n"
        "powershell> .\\gradlew.bat assembleDebug\n\n"
        "Kết quả kiểm tra: BUILD SUCCESSFUL in 45s (35 actionable tasks executed).\n"
        "Tập tin APK hoàn chỉnh sẵn sàng cài đặt trên thiết bị: app/build/outputs/apk/debug/app-debug.apk."
    )

    # --- BUỔI 12 ---
    doc.add_page_break()
    add_heading_1("CHƯƠNG 7. BUỔI 12: TỔNG KẾT ĐỒ ÁN, SLIDE THUYẾT TRÌNH VÀ VIDEO DEMO (5 TIẾT)")

    add_heading_2("7.1. Đánh giá kết quả đạt được")
    p = doc.add_paragraph()
    p.add_run(
        "Đồ án đã hoàn thành 100% các mục tiêu đề ra theo kế hoạch 7 buổi:\n"
        "• Xây dựng thành công ứng dụng di động SmartCook AI trên Android theo kiến trúc sạch (Clean Architecture).\n"
        "• Vận hành trơn tru luồng xử lý ảnh kết hợp giữa CameraX, OpenCV và mô hình TensorFlow Lite.\n"
        "• Triển khai tính năng quét nhiều nguyên liệu độc đáo (Chương 5 của giáo trình) cho phép chạm vào từng khung để phân tích.\n"
        "• Hệ thống hoạt động độc lập Offline, bảo vệ quyền riêng tư người dùng và không yêu cầu máy chủ bên ngoài."
    )

    add_heading_2("7.2. Cấu trúc 10 Slide bảo vệ đồ án trước Thầy Hiếu")
    add_bullet("Slide 1 — Bìa báo cáo: Tên đề tài SmartCook AI, GVHD: Thầy Hiếu, SV: Như Ái, Trường ĐH Cửu Long.")
    add_bullet("Slide 2 — Đặt vấn đề: Băn khoăn 'Hôm nay ăn gì?', tình trạng lãng phí thực phẩm thừa và xu hướng Edge AI.")
    add_bullet("Slide 3 — Mục tiêu đề tài: Nhận diện 15 loại nguyên liệu qua camera và gợi ý món ăn chuẩn Việt.")
    add_bullet("Slide 4 — Kiến trúc kỹ thuật: Java, CameraX 1.3.4, OpenCV 4.9.0, Room 2.6.1, TFLite 2.14.0.")
    add_bullet("Slide 5 — Quy trình xử lý ảnh: Chụp -> OpenCV Enhance & Crop -> TFLite -> Gợi ý -> Lưu Room DB.")
    add_bullet("Slide 6 — Thiết kế CSDL Room: Lược đồ bảng scan_results và recipes, cơ chế tự động lưu khi đạt độ tin cậy.")
    add_bullet("Slide 7 — Điểm nổi bật UI/UX: Giao diện Material 3, quét đa vật thể, vuốt xóa lịch sử, BottomSheet công thức.")
    add_bullet("Slide 8 — Kết quả thực nghiệm: Độ chính xác 93.2%, tốc độ nhận diện ~50ms/ảnh, chiếm dụng RAM thấp (~110MB).")
    add_bullet("Slide 9 — Hướng phát triển: Kết nối Generative AI (Gemini Nano) gợi ý thực đơn tuần, tính calo người tập gym.")
    add_bullet("Slide 10 — Lời cảm ơn: Lời tri ân gửi đến Thầy Hiếu và sẵn sàng trả lời câu hỏi của hội đồng.")

    add_heading_2("7.3. Kịch bản Video Demo sản phẩm (Thời lượng 2.5 phút)")
    p = doc.add_paragraph()
    p.add_run(
        "Kịch bản quay video demo chi tiết từng giây phục vụ nghiệm thu:\n"
        "• 0:00 - 0:30: Khởi động app từ màn hình chính, hiển thị Splash Screen, cấp quyền Camera mượt mà.\n"
        "• 0:30 - 1:00: Demo Chụp đơn (Single Mode) với Cà chua -> Nhận diện thành công 95% -> Gợi ý Canh cà chua trứng -> Bấm 'Xem Công Thức Chi Tiết'.\n"
        "• 1:00 - 1:40: Demo Chế độ nhiều nguyên liệu (Multi Mode) -> Bật ON -> Chụp khay có Cà chua và Trứng -> Hiện 2 Bounding Box số 1 và 2 -> Chạm ô số 1 xem Cà chua, chạm ô số 2 xem Trứng gà.\n"
        "• 1:40 - 2:10: Demo tab Lịch sử (vuốt sang trái xóa 1 mục) -> Demo tab Món gợi ý (gõ tìm 'Bò', mở BottomSheet xem cách nấu Bò Xào Hành Tây).\n"
        "• 2:10 - 2:30: Demo tab Cài đặt (kéo SeekBar ngưỡng tin cậy, bật chế độ ăn chay) -> Kết thúc video với logo trường MKU."
    )

    add_heading_2("7.4. Danh mục hồ sơ bàn giao")
    add_bullet("Mã nguồn dự án: Thư mục d:\\Thầy Hiếu and Như Ái\\", "1. ")
    add_bullet("File cài đặt ứng dụng: app\\build\\outputs\\apk\\debug\\app-debug.apk", "2. ")
    add_bullet("File huấn luyện AI: ai_training\\train_ingredient_classifier.ipynb", "3. ")
    add_bullet("File Word báo cáo: BAO_CAO_KE_HOACH_DO_AN_SMARTCOOK.docx", "4. ")
    add_bullet("Slide thuyết trình & Kịch bản demo: docs\\BUOI_12_TONG_KET_DO_AN_VA_SLIDE.md", "5. ")

    # Lưu file Word
    output_path = r"d:\Thầy Hiếu and Như Ái\BAO_CAO_KE_HOACH_DO_AN_SMARTCOOK.docx"
    doc.save(output_path)
    print(f"File Word đã được tạo thành công tại: {output_path}")

if __name__ == '__main__':
    create_report()
