# Hướng Dẫn Thuyết Trình & Bảo Vệ — Nhóm 4 (`Nhom4_Milestone6_Slides.pdf`)

> **Phong cách trình bày:** Tài liệu này được biên soạn theo phong cách **Tech Conference Keynote** (gần gũi, tương tác với lớp, kể câu chuyện từ *“nỗi đau viết code hay bị lỗi”* đi tới *“nguyên lý OOP giải quyết”*, câu văn ngắn gọn, giàu nhịp điệu, **không đọc vẹt chữ trên slide**).
>
> **Tài liệu nền tảng đi kèm:** Nếu cần hiểu sâu chi tiết từng dòng code ở mức OOP Chương 6 trở về, hãy đọc thêm [Cẩm nang nền tảng & giải phẫu codebase](../docs/project-fundamentals-and-codebase-guide.md).

---

## 1. Bí quyết "Giữ Sân Khấu" & Thông điệp Chủ đạo (The Big Thesis)

Thay vì lên bảng nói một mạch *"Slide 1 là bìa, Slide 2 là lộ trình, Slide 3 là yêu cầu..."* khiến cả lớp buồn ngủ và thầy khó thấy chiều sâu, hãy bám chặt **1 luận điểm xuyên suốt (Thesis)** từ đầu tới cuối:

> **"Code dễ viết mới là thứ rẻ, còn một codebase dễ sửa mà không gãy chỗ khác mới là thứ đắt giá nhất — và chìa khóa để làm được điều đó không phải phép màu gì cao siêu, mà nằm ngay ở những kiến thức OOP nền tảng nhất từ Chương 1 đến Chương 6."**

### 4 kỹ thuật diễn đạt giúp bài nói "cuốn" ngay lập tức:
1. **Mở đầu bằng giao tiếp thật & sự đồng cảm:** Chào thầy và cả lớp thoải mái, hỏi nhanh 1 câu để làm nóng phòng học trước khi vào slide kỹ thuật.
2. **Công thức vàng cho mọi Slide — `Nỗi đau quen thuộc -> Cách làm ngây thơ bị gãy -> Viên gạch OOP sửa triệt để`:** Người nghe rất thích nghe bạn thú nhận cái bẫy nhóm từng suýt dính phải (ví dụ đặt ghế vào máy bay `Aircraft`, hay viết `if-else` dài 5 trang) trước khi tung ra giải pháp thật trong code.
3. **Dùng các nhịp dừng & câu hỏi "giơ tay / tương tác nhẹ":** Trong kịch bản có đánh dấu `[Dừng 2 giây]`, `[Nhìn lớp]`, `[Chỉ lên slide]` — nhớ thực hiện thật, đừng nói liên thanh như trả bài.
4. **Giữ sự lễ phép học thuật đúng mực PTIT:** Thoải mái, tự tin, hài hước thông minh với cả lớp nhưng vẫn xưng `em / nhóm em` và thưa `thầy` rõ ràng khi hướng về giảng viên.

---

## 2. Thông tin tổng quan & 10 Tiêu chí Chấm điểm

| Hạng mục | Chi tiết |
|---|---|
| **Học phần** | Lập trình hướng đối tượng — `INT1332_CLC` (PTIT) |
| **Giảng viên hướng dẫn** | ThS. Phạm Hoàng Việt |
| **Đề tài** | Đề tài 21 (Advanced): **Airline Reservation System** (Hệ thống Đặt vé & Điều phối Chuyến bay) |
| **Nhóm thực hiện** | Nhóm 4 · Lớp `D24CQCE05` |
| **Thành viên** | Đồng Việt Hưng (`B24DCCE117`) · Dương Kim Sang (`B24DCCE236`) · Lê Bảo Phúc (`B24DCCE215`) · Nguyễn Hữu Phi Long (`B24DCCE166`) |
| **File trình chiếu** | [Nhom4_Milestone6_Slides.pdf](./Nhom4_Milestone6_Slides.pdf) (14 slides) |
| **Thời lượng mục tiêu** | **12 – 14 phút** (Kể chuyện qua slide: ~8–9 phút; Live Demo trực tiếp: ~3.5–4.5 phút) + Hỏi đáp |

### Bảng đối chiếu 10 Tiêu chí Chấm điểm (`Introduction.pdf` tr. 13 · Tổng 100 điểm)

| # | Tiêu chí chấm điểm | Điểm | Slide phụ trách chính | Thông điệp "ăn điểm" cần nhấn mạnh |
|---|---|---:|---|---|
| 1 | Phân tích bài toán & yêu cầu | 10đ | Slide 2, Slide 3 | Tiến hóa qua 6 mốc, 3 trụ cột Passenger / Admin / Invariants |
| 2 | Mô hình hóa UML & thiết kế OOP | 10đ | Slide 4 | Ranh giới Passive View tách 38 kiểu Java thuần khỏi JavaFX |
| 3 | Đóng gói & thiết kế lớp | 10đ | Slide 5 | `private`/`protected`, chặn sửa lén từ ngoài bằng `List.copyOf` |
| 4 | Quan hệ Association / Composition | 10đ | Slide 5 | `Flight *-- Seat` tách khỏi `Flight --> Aircraft` (ADR-0001) |
| 5 | Kế thừa & Đa hình | 10đ | Slide 6 | `Person`, `Seat` tự tính giá không `if-else`, `PaymentStrategy` |
| 6 | Interfaces & Trừu tượng hóa | 10đ | Slide 6, 8, 9 | `BookingState`, `@FunctionalInterface PaymentStrategy` |
| 7 | Collections, Generics & Exceptions | 10đ | Slide 7 | `LinkedHashMap` $O(1)$, `HashSet`, `<T> findById`, 6 `RuntimeException` |
| 8 | SOLID & Design Patterns | 10đ | Slide 8, 9 | State (5 Singleton), Strategy (4), Factory (`Seat`/`Payment`), SOLID |
| 9 | Chất lượng mã nguồn & Kiểm thử | 10đ | Slide 12 | 85 JUnit 5 tests (100% pass, headless controller + CSS parser) |
| 10 | Demo thực tế & Trình bày | 10đ | Slide 10, 11, 13 | JavaFX 25 SPA, sơ đồ ghế 2-2, Demo 4 bước & cascade hoàn tiền |

---

## 3. Phân công người trình bày (2 phương án)

### Phương án A — Chia đều cả 4 thành viên (Khuyên dùng nếu thầy muốn thấy đóng góp đều)

| Vai | Slide | Câu chuyện phụ trách | Gợi ý người nhận | Thời lượng |
|---|---|---|---|---:|
| **Người mở màn (Keynote Hook + Nền tảng)** | Slide 1 → 6 | Khuấy động phòng, đặt luận điểm "Code dễ sửa mới là code tốt", kể bẫy giao diện, bẫy `Aircraft`–`Seat` và đa hình xóa `if-else` | Thành viên tự tin giao tiếp nhất | ~3.5 phút |
| **Người giải mã Lõi (Data + Patterns)** | Slide 7 → 9 | Collections $O(1)$, 6 ngoại lệ sạch, cách State Pattern thay thế ma trận `switch-case` và mở rộng SOLID | Thành viên nắm luồng logic | ~2.5 phút |
| **Người điều phối Live Demo** | Slide 10 → 11 | Giới thiệu bản đồ `2-AISLE-2`, cầm chuột bấm app JavaFX chạy thật + dẫn chuyện trực tiếp lúc "cố tình bấm lỗi thanh toán" | Thành viên thao tác máy nhanh | ~4.5 phút |
| **Người chốt hạ (Proof & Wrap-up)** | Slide 12 → 14 | Khoảnh khắc chốt bằng `85` bài test như "đèn pha ban đêm", đối chiếu minh chứng theo 10 tiêu chí & mở Q&A | Thành viên giọng chắc, dứt khoát | ~1.5 phút |

> **Mẹo chuyển người cực mượt (không bị khựng):** Đừng nói *"Em hết phần rồi ạ"*. Hãy tung hứng tự nhiên: *"Em vừa mở nắp hộp động cơ cho mọi người thấy đa hình dọn sạch `if-else` thế nào. Giờ câu hỏi tiếp theo là: dữ liệu chạy bên trong lấy gì chứa cho nhanh, và sai thì báo lỗi làm sao? Bạn [Tên] sẽ kể tiếp phần này."*

### Phương án B — 1 Người dẫn chuyện chính + 1 Người bấm máy Demo
- **Người dẫn chính:** Ôm trọn Slide 1 → 10 và chốt Slide 12 → 14 để mạch kể chuyện giữ nguyên một giọng văn liền mạch (~7.5 phút).
- **Người bấm Demo:** Chuẩn bị sẵn cửa sổ JavaFX từ trước; tới Slide 11, hai người phối hợp kiểu *"tung – hứng"*: người dẫn đọc tình huống, người bấm máy thao tác ngay trên máy chiếu (~4 phút). Cả 4 thành viên cùng đứng hỗ trợ phần Q&A.

---

## 4. Checklist chuẩn bị trước giờ G (Windows PowerShell)

Làm **ít nhất 15 phút trước khi lên bảng**:

1. Mở PowerShell tại `e:\OOP` và chạy trước toàn bộ bộ test để chắc máy không lỗi môi trường Java 25:
   ```powershell
   .\mvnw.cmd test
   ```
   Kết quả chuẩn phải là `Tests run: 85, Failures: 0, Errors: 0, Skipped: 0`.
2. Mở sẵn ứng dụng JavaFX trước để Maven nạp xong thư viện, kiểm tra khung cửa sổ vừa màn hình máy chiếu:
   ```powershell
   .\mvnw.cmd javafx:run
   ```
3. Để sẵn trên màn hình:
   - Cửa sổ trình chiếu `Nhom4_Milestone6_Slides.pdf` ở chế độ toàn màn hình.
   - Cửa sổ `Airline Reservation System` nằm ngay sau để `Alt + Tab` chuyển sang trong 1 giây ở Slide 11.
4. Ghi nhớ **3 quy tắc dữ liệu demo**:
   - Mật khẩu hành khách mẫu (`PAX-01`, `PAX-02`) là **`123456`**; Admin (`ADM-01`) là **`admin123`** *(nhưng trên màn hình Login đã có 3 chip **Quick Demo Accounts**: bấm 1 lần điền sẵn, **double-click** đăng nhập luôn!)*.
   - Nếu bấm **`Auto-Fill Sample`** trong hộp thoại đăng ký hành khách mới, mật khẩu được điền sẵn là **`pass123`**.
   - Chạy thử nháp trước lúc lên bảng xong **chỉ cần bấm nút `Reset Demo`** trên thanh Navbar góc trên bên phải (hoặc tắt mở lại app) — dữ liệu quay về nguyên trạng ban đầu ngay.
5. Nếu máy chiếu/máy trường gặp trục trặc đồ họa JavaFX hiếm gặp, chạy **Console smoke run dự phòng**:
   ```powershell
   .\mvnw.cmd compile exec:java -Dexec.mainClass="com.airlinereservation.Main"
   ```

---

## 5. CẢNH BÁO QUAN TRỌNG: 8 điểm khác nhau giữa Slide và Code thực tế

Trước khi lên bảng, cả nhóm **bắt buộc đọc bảng này** để không bị bối rối nếu giảng viên soi code đối chiếu trực tiếp với slide:

| # | Vị trí trên Slide | Slide hiển thị gì? | Code thực tế làm gì? | Xử lý khéo khi nói / bị hỏi |
|---|---|---|---|---|
| **1** | **Slide 4** (`GuiController`) | Node giữa ghi `GuiController` với các hàm `holdBooking()`, `payForBooking()`, `searchFlights()`, `cancelFlight()`. | Class thật là `ReservationGuiController`. Hàm giữ chỗ là `bookSeats(flightId, seatNumbers)` và thanh toán là `processPayment(bookingId, method, primaryDetail, secondaryDetail, simulateFailure)`. | *"Trên sơ đồ kiến trúc, nhóm viết gọn là `GuiController` cho vừa khung hình; trong mã nguồn Java tên đầy đủ là `ReservationGuiController`."* |
| **2** | **Slide 9** (`PaymentStrategy`) | Minh họa `CardPayment (12–19 digits)`, `EWalletPayment (WALLET-*)`, `BankTransferPayment (BANK-*)`, `CashPayment (front-desk)`. | Constructor của 4 lớp thanh toán kiểm tra chuỗi không rỗng (`ValidationException.requireNonBlank(...)`), `pay(amount)` kiểm tra số tiền hợp lệ (`Double.isFinite(amount) && amount > 0`). Trên UI, `PaymentDialog` điền sẵn mẫu gợi ý (`4111-2222-3333-4444`, `0901111111`/`MoMo`, `Vietcombank`/`0123456789`, `CNT-01`/`CSH-01`). | Nói gọn *"mỗi lớp đóng gói thông tin xác nhận riêng của kênh thanh toán và kiểm tra tính hợp lệ đầu vào"*; đừng tự khẳng định trong code có kiểm tra regex 12–19 số. |
| **3** | **Slide 10** (Mockup `VN101`) | Mockup minh họa Base Fare `1,200,000 VND`, nên `1B (2,400,000) + 2A (1,380,000) = 3,780,000 VND`. | Trong `DemoDataSeeder`, `VN101` có giá gốc **`1,500,000 VND`**. Chọn `1B (2.0x = 3,000,000) + 2A (1.15x = 1,725,000)` sẽ ra **`4,725,000 VND`** (`VN205` mới có giá gốc `1,200,000`). | Khi nói Slide 10 gọi rõ là *"ảnh thiết kế minh họa quy tắc nhân hệ số"*; lúc chạy app thật trên `VN101` đọc đúng **`4,725,000 VND`**. |
| **4** | **Slide 11** (Mockup KPI & Step 4) | Nền minh họa `4 Flights · 3 Bookings · 2 Passengers · 6.18M`. Bước 4 ghi gọn *"Hạ cánh VN101 `COMPLETED` · Hủy VN103 `REFUNDED`"*. | Khi mở app có **`3` Flights, `2` Bookings (`BK-1` đã trả `3,000,000 VND` trên `VN101`; `BK-2` của `PAX-01` trên `VN103` mới `PENDING` chưa trả tiền), `2` Passengers**. Ngoài ra: (a) hạ cánh `VN101` phải bấm tuần tự **`Mark Boarding` → `Mark Departed` → `Mark Arrived`**; (b) phải có ít nhất 1 đơn đã trả tiền trên `VN103` thì lúc hủy `VN103` mới hiện **`REFUNDED`**. | Thực hiện đúng *"Bước đệm 15 giây"* trong Mục 7 trước khi sang Admin: trả tiền 1 ghế trên `VN103` để lúc Admin hủy hiện trọn vẹn `REFUNDED`! |
| **5** | **Slide 12** (Số lượng test class) | Slide ghi `85` tests nhưng chú thích `18 Classes` (`32 / 22 / 7 / 24`). | `.\mvnw.cmd test` thực tế chạy **`85` tests, `0` Failures** trên **`22` test classes** (`model`: 37 tests / 9 classes; `system`: 27 tests / 7 classes; `payment + exception`: 7 tests / 3 classes; `gui + MainTest`: 14 tests / 3 classes). | Nhấn mạnh **`85` test pass 100%**. Nếu thầy mở thư mục thấy 22 file, trả lời thẳng: *"Nhóm tách thêm file test khi hoàn thiện cuối kỳ nên thành 22 class cho gọn trách nhiệm, tổng vẫn 85 test."* |
| **6** | **Slide 6** (Không `instanceof`) | Slide nhấn mạnh đa hình loại bỏ rẽ nhánh kiểm tra kiểu. | Tầng Domain (tính tiền vé, đặt vé, thanh toán, hệ thống) **0% `instanceof`**. Riêng UI `SeatSelectionDialog` có `instanceof BusinessSeat` / `EconomySeat` chỉ để tô màu badge và in nhãn `"Lounge Access"`. | Chủ động thừa nhận khéo nếu được hỏi — chứng tỏ mình hiểu rõ ranh giới tách Logic và UI. |
| **7** | **Mật khẩu Demo** (`README.md` vs code) | `README.md` có ghi mẫu `pass123`. | Code thật: `PAX-01`/`PAX-02` là **`123456`**, `ADM-01` là **`admin123`** (riêng `Auto-Fill Sample` ở đăng ký là `pass123`). | Dùng **Quick Demo Accounts** chip khi trình diễn. |
| **8** | **Slide 13** (Lệnh Console) | In gọn `.\mvnw.cmd exec:java`. | `pom.xml` chưa gắn `<mainClass>` cho `exec-maven-plugin`. | Nếu chạy Console phải gõ đủ: `.\mvnw.cmd compile exec:java -Dexec.mainClass="com.airlinereservation.Main"`. |

---

## 6. Kịch bản Thuyết trình Từng Slide (Slide 1 → Slide 14)

---

### Slide 1 — Trang bìa (`Airline Reservation System`)

- **Thời lượng:** 30–35 giây | **Mục tiêu:** Chào hỏi ấm áp, tạo kết nối với phòng học và ném ra thông điệp chủ đạo ngay phút đầu.
- **Trên màn hình có gì:** Hình mạng lưới bay bên trái; thông tin `INT1332_CLC · MILESTONE 6`, `Nhóm 4 · D24CQCE05`, tiêu đề `Airline Reservation System — Hệ thống Đặt vé & Điều phối Chuyến bay`, 4 thành viên (`Đồng Việt Hưng`, `Dương Kim Sang`, `Lê Bảo Phúc`, `Nguyễn Hữu Phi Long`), `GVHD: ThS. Phạm Hoàng Việt · Đề tài 21 (Advanced)`.
- **Lời nói gợi ý (Keynote Style):**
  > *"Em chào thầy Việt, chào cả lớp! Sáng giờ bảo vệ tới lượt nhóm mình mọi người vẫn ổn chứ ạ?"* `[Cười nhẹ, dừng 1.5 giây quét mắt cả lớp]`
  > *"Tốt quá! Nhóm em là Nhóm 4 — D24CQCE05, gồm Hưng, Sang, Phúc và Long. Hôm nay nhóm mang lên đây đề tài 21 mức Advanced: Hệ thống Đặt vé và Điều phối Chuyến bay."*
  > `[Hạ giọng một nhịp, nói chậm và chắc]` *"Trước khi bấm sang slide tiếp theo, em muốn gửi một tin vui cho những bạn giống bọn em hồi đầu kỳ: mới học qua vài chương OOP căn bản, thấy một dự án 52 file JavaFX có vẻ rất ngợp. Nhưng thực ra, cả đồ án này là câu chuyện chứng minh một điều: **những viên gạch OOP cơ bản nhất — đóng gói, kế thừa, đa hình và chia ranh giới rõ ràng — mới là thứ giữ cho một hệ thống lớn không sụp đổ khi mình liên tục thêm tính năng.**"*
- **Câu chuyển:** *"Và bọn em không có phép màu nào để làm một phát ăn ngay đâu — mọi thứ đi qua đúng 6 cột mốc tiến hóa trên Slide 2."*

---

### Slide 2 — `Hành trình phát triển qua sáu cột mốc`

- **Thời lượng:** 45–50 giây | **Mục tiêu:** Cho thầy thấy nhóm đi đúng quy trình kỹ thuật phần mềm (phân tích → UML → code → refactor SOLID → GUI & test) thay vì "code bừa từ đầu".
- **Trên màn hình có gì:** Trục 6 mốc theo đúng thứ tự `M1` → `M6 (TODAY)`:
  1. `M1 · Problem Analysis`: Yêu cầu bài toán & ca sử dụng `Passenger` / `Admin`, quy tắc không đặt trùng ghế.
  2. `M2 · OO Analysis (CRC)`: 11 lớp miền cốt lõi.
  3. `M3 · ADR-0001 UML`: Quyết định thiết kế kiến trúc trên sơ đồ UML.
  4. `M4 · Java 25`: Khối terminal `> mvnw test (M4)` → `52 TESTS`.
  5. `M5 · SOLID & Patterns`: `BookingState (5)` · `PaymentStrategy (4)` · `SeatFactory & PaymentFactory`.
  6. `M6 · Final Product (TODAY)`: JavaFX 25 SPA, Passive View MVC, `SeatSelectionDialog (2-AISLE-2)`, `85 JUnit 5 Tests · 0 Failures`.
- **Lời nói gợi ý:**
  > *"Ở đây có bạn nào từng code một đồ án mà viết thẳng một lèo từ `main()`, tới tuần cuối mới nhét vội giao diện vào rồi mọi thứ nổ bug dây chuyền chưa ạ? Gật đầu nhẹ cái cho em đỡ cô đơn..."* `[Dừng 2 giây, cười]`
  > *"Nhóm em từng suýt rơi vào cảnh đó — cái mà trong sách gọi là 'độ hỗn loạn phần mềm': sửa một chỗ là hỏng ba chỗ khác. Nên ngay từ M1–M2, bọn em khoanh lại đúng 11 lớp cốt lõi. M3 vẽ UML và ghi nhật ký kiến trúc đầu tiên ADR-0001. M4 có 52 bài test chạy trên nền Java thuần không cần giao diện. Tới M5, thấy đống `if-else` bắt đầu phình to, bọn em dừng lại dọn dẹp bằng SOLID và các Design Pattern. Và kết quả đứng trước thầy hôm nay — Milestone 6 — là một giao diện JavaFX 25 dạng Single Page Application với sơ đồ chọn ghế `2-AISLE-2`, được bảo vệ bởi 85 bài kiểm thử xanh rờn không một lỗi."*
- **Câu chuyển:** *"Vậy cụ thể, hệ thống đặt vé hàng không này phải gánh những bài toán khó chịu nào khiến code dễ bị rối? Mời thầy và các bạn sang Slide 3."*

---

### Slide 3 — `Ba bài toán cốt lõi của đặt vé hàng không`

- **Thời lượng:** 45–50 giây | **Mục tiêu:** Chốt nhanh luồng Hành khách, Admin và nhấn thật mạnh vào 3 "luật thép" (Invariants).
- **Trên màn hình có gì:** 3 cột: `Luồng Passenger` (tìm, giữ chỗ, trả tiền, check-in, hủy), `Điều phối Admin` (mở chuyến, điều phối trạng thái, hủy chuyến), và chiếc khiên `Bất biến Invariants` (chống đặt trùng ghế, cô lập sơ đồ ghế giữa chuyến chung tàu bay, tự hoàn tiền khi hủy chuyến).
- **Lời nói gợi ý:**
  > *"Nếu chỉ vẽ màn hình tìm chuyến và bấm nút mua vé thì rất dễ. Cái làm một hệ thống đặt vé thực sự khó là ba cột trước mặt mọi người.*
  > *Cột một là hành khách: vào tìm chuyến, nhìn sơ đồ ghế, giữ chỗ chưa trả tiền, rồi thanh toán, check-in hay đổi ý hủy vé. Cột hai là Admin: mở chuyến bay, đẩy chuyến bay lên tàu, cất cánh, hạ cánh — hoặc bất ngờ báo hủy.*
  > `[Chỉ vào biểu tượng chiếc khiên bên phải]` *Nhưng cột thứ ba — cái khiên **Invariants (những luật thép bất khả xâm phạm)** — mới là thứ bọn em sợ nhất: làm sao cấm tuyệt đối hai khách mua đè lên một ghế `1A`? Làm sao để hai chuyến bay dùng chung một chiếc máy bay A320 nhưng bán vé không dẫm chân lên nhau? Và làm sao khi Admin hủy một chuyến bay đang có hàng chục người đặt, toàn bộ ghế tự nhả ra và tiền của ai đã trả tự động chuyển sang hoàn tiền đồng bộ? Không xử lý khéo, chỉ cần thêm một tính năng là hệ thống rối ngay."*
- **Câu chuyển:** *"Câu trả lời đầu tiên của nhóm em nằm ở Slide 4: dựng một ranh giới rõ ràng giữa giao diện và bộ não xử lý."*

---

### Slide 4 — `Passive View tách biệt giao diện và miền`

- **Thời lượng:** 55–65 giây | **Mục tiêu:** Làm rõ kiến trúc **Passive View MVC** và ý tưởng **"hộp sâu giấu sự phức tạp sau một ranh giới đơn giản"**.
- **Trên màn hình có gì:** Trái: `JavaFX 25 SPA Views` (`PassengerWorkspaceView`, `SeatSelectionDialog`, `AdminWorkspaceView`); Giữa: `GuiController` *(nhớ: trong code là `ReservationGuiController`, hàm thật là `bookSeats()`, `processPayment()`) *; Phải: `38 Domain & Pattern Types` đứng đầu là `AirlineReservationSystem` (thuần Java 25, 0% JavaFX).
- **Lời nói gợi ý:**
  > *"Mọi người thử tưởng tượng xem: nếu các bạn nhét thẳng công thức tính giá vé và code trừ ghế vào ngay bên trong nút bấm của JavaFX, chuyện gì xảy ra? Mỗi lần sửa màu nút hay chuyển từ Console lên JavaFX, các bạn phải cầu nguyện đừng làm sai tiền vé! Quen không ạ?"*
  > *"Để thoát bẫy đó, nhóm em áp dụng kiến trúc **Passive View** — giống như thiết kế một 'hộp sâu': bên ngoài nhìn qua một mặt giao tiếp cực gọn, còn mọi thứ phức tạp giấu hết vào bên trong.*
  > `[Chỉ sang khung bên trái]` *Toàn bộ phần JavaFX 25 bên trái hoàn toàn 'bị động'. `SeatSelectionDialog` hay `AdminWorkspaceView` chỉ biết hai việc: vẽ cho đẹp và nghe cú click chuột. Nó không tự tính nổi 1 đồng tiền vé.*
  > `[Chỉ sang khung bên phải]` *Bên phải là 38 lớp Java thuần túy xoay quanh `AirlineReservationSystem`. Điểm ăn tiền nhất là: khối bên phải này **không hề import một chữ `javafx.*` nào**. Nó thậm chí không biết giao diện tồn tại!*
  > `[Chỉ vào khối giữa]` *Đứng ở giữa là `ReservationGuiController` — trên slide viết gọn là `GuiController` — đóng vai trò cánh cổng duy nhất: View gõ cửa đưa yêu cầu (`searchFlights`, `bookSeats`, `processPayment`, `cancelFlight`), Controller chuyển vào nhà bếp Domain nấu xong rồi trả kết quả sạch ra cho View vẽ lại. Nhờ vậy, hôm nay nhóm dùng JavaFX, ngày mai chạy lại trên Console hay viết JUnit test không bật màn hình vẫn chạy ngon ơ!"*
- **Bỏ túi cho người nói:** Nếu bị hỏi vì sao đặt tên `GuiController` trên slide mà trong code là `ReservationGuiController`, giải thích thẳng tên rút gọn để sơ đồ thoáng mắt.
- **Câu chuyển:** *"Đó là bức tranh nhìn từ trên xuống. Bây giờ em mời cả lớp chui vào trong khối Domain bên phải, xem nhóm xử lý cái bẫy đầu tiên — bẫy 'đặt nhầm ghế vào máy bay' ở Slide 5."*

---

### Slide 5 — `Mỗi chuyến bay sở hữu sơ đồ ghế riêng`

- **Thời lượng:** 55–65 giây | **Mục tiêu:** Chốt điểm Đóng gói & phân biệt rành rọt **Composition vs Association (ADR-0001)**.
- **Trên màn hình có gì:** Trái: `Composition (ADR-0001)`, `Cô lập trạng thái`, `Đóng gói bất biến (List.copyOf)`. Phải: 3 chuyến `VN101`, `VN205`, `VN309` cùng dùng `AC-320` tách xuống `Aircraft [read-only]` và `Flight.seats [read-write]` với `1A` mang 3 trạng thái khác nhau ở 3 chuyến.
- **Lời nói gợi ý:**
  > *"Cho em hỏi thật cả lớp: lúc mới đọc đề bài đặt vé máy bay, có bạn nào nghĩ trong đầu ngay là 'Mỗi máy bay có 16 cái ghế, vậy cứ tạo danh sách `seats` bỏ vào class `Aircraft` là hợp lý nhất' không ạ?"* `[Dừng 2 giây, cười]`
  > *"Bọn em hồi Milestone 1–2 cũng suýt làm y hệt thế! Nhưng nghĩ kỹ lại: chiếc Airbus `AC-320` sáng bay `VN101` Hà Nội – Sài Gòn, chiều bay tiếp `VN205` Sài Gòn – Đà Nẵng. Nếu ghế `1A` nằm trong `Aircraft`, sáng có khách mua `1A` trên `VN101`, chiều người khác mở `VN205` thấy ghế `1A` cũng khóa cứng luôn! Thảm họa ngay!*
  > *Nên bọn em ghi lại quyết định **ADR-0001** như hình bên phải: lớp `Aircraft` chỉ là khuôn mẫu **chỉ đọc** (hãng nào, 16 ghế, 4 ghế một hàng) và `Flight` nối tới nó bằng quan hệ **Association**. Còn bộ `Map<String, Seat>` thực sự lại thuộc quyền sở hữu **Composition** của từng `Flight` riêng lẻ. Nhờ vậy cùng là `1A` trên tàu `AC-320`: chuyến `VN101` đã bán (`OCCUPIED`), chuyến `VN205` vẫn trống (`AVAILABLE`), còn `VN309` đang giữ chỗ (`RESERVED`) — hoàn toàn cô lập!*
  > *Và để đóng gói cho tới nơi tới chốn: mọi trạng thái bên trong đều khóa `private`/`protected`, hàm `getSeats()` khi đưa ra ngoài chỉ trả bản sao bất biến `List.copyOf(...)`. Đưa cho giao diện xem thì được, chứ đố code bên ngoài tự `.clear()` hay `.add()` trộm một cái ghế nào!"*
- **Câu chuyển:** *"Ghế đã cô lập xong rồi, vậy tới bước tính tiền từng hạng ghế và phân quyền người dùng thì sao? Nhóm né bẫy `if-else` bằng Đa hình ở Slide 6."*

---

### Slide 6 — `Đa hình loại bỏ rẽ nhánh kiểm tra kiểu`

- **Thời lượng:** 55–65 giây | **Mục tiêu:** Thể hiện rõ Kế thừa, Lớp trừu tượng, Đa hình (`Person`, `Seat`, `PaymentStrategy`) xóa bỏ `if-else`.
- **Trên màn hình có gì:** 3 thẻ màu: `Person (abstract)` → `Passenger` & `Admin`; `Seat (abstract)` → `BusinessSeat (2.0x + Lounge)` & `EconomySeat (1.15x hàng đầu / 1.0x)`; `PaymentStrategy (@FunctionalInterface)` (Stream, không `instanceof` ở nghiệp vụ).
- **Lời nói gợi ý:**
  > *"Đây là câu chuyện của OOP Chương 5 và Chương 6, nhưng áp dụng đúng thì code nhẹ đi một nửa.*
  > *Hãy nghĩ tới cách viết bình thường khi tính tiền một đơn vé có cả ghế Thương gia lẫn ghế Phổ thông: nhiều bạn sẽ viết `if (seat.type == BUSINESS) nhân 2, else if (hàng đầu) nhân 1.15...`. Rồi ngày mai hãng mở thêm hạng Nhất (First Class) là bạn lại phải lục khắp dự án tìm từng câu `if` để sửa.*
  > `[Chỉ vào thẻ tím ở giữa]` *Ở đây, lớp trừu tượng `Seat` chỉ định nghĩa một hợp đồng duy nhất `calculatePrice(basePrice)`. Khi tính tổng đơn đặt chỗ, `Booking` không thèm hỏi ghế đó là ai — nó chỉ thả danh sách ghế qua Stream và gọi đúng một dòng: `seat.calculatePrice(...)`. `BusinessSeat` tự động nhân `2.0`, `EconomySeat` tự kiểm tra mình có nằm hàng đầu có chỗ để chân rộng để nhân `1.15` hay giữ `1.0`. Hoàn toàn **không có câu `if` kiểm tra loại ghế nào trong tầng tính toán**!* *(Ghi chú nhỏ cho người nói: nếu thầy hỏi khéo ở tầng UI có `instanceof` không, trả lời ngay là chỉ có ở `SeatSelectionDialog` để tô màu nút ghế cho đẹp mắt thôi).*
  > `[Chỉ thẻ xanh và thẻ cam]` *Tương tự với `Person` cho `Passenger` & `Admin`, và `PaymentStrategy` — được gắn `@FunctionalInterface` nên vừa nhận được 4 lớp thanh toán thật, vừa nhận được ngay một hàm Lambda ngắn gọn như `amount -> false` khi nhóm muốn giả lập cổng thanh toán bị rớt mạng trong lúc test!"*
- **Câu chuyển:** *"Đó là xong phần OOP cốt lõi Chương 1–6. Tiếp theo, bạn [Tên] sẽ đưa mọi người đi tiếp sang cấu trúc dữ liệu, xử lý lỗi và các Design Pattern giúp hệ thống chạy nhanh và dễ mở rộng ở Slide 7."*

---

### Slide 7 — `Collections O(1) và phân cấp ngoại lệ miền`

- **Thời lượng:** 50–60 giây | **Mục tiêu:** Chứng minh hiểu bản chất khi chọn `LinkedHashMap`, `HashSet`, Generics `<T>` và Unchecked Exceptions (`ADR-0003`).
- **Trên màn hình có gì:** Trái: `LinkedHashMap<String, V>`, `HashSet<Seat>`, `<T> T findById(Map)`, Stream API. Phải: `AirlineReservationException extends RuntimeException` và lưới 6 ngoại lệ (`Validation`, `EntityNotFound`, `DuplicateEntity`, `SeatNotAvailable`, `InvalidState`, `Payment`).
- **Lời nói gợi ý:**
  > *"Cảm ơn bạn [Tên]. Khi dữ liệu bắt đầu nhiều lên và người dùng bắt đầu bấm sai, có hai thứ dễ làm code chúng ta chậm và bẩn: tìm kiếm bằng vòng `for`, và `try-catch` giăng kín màn hình.*
  > *Ở bên trái, thay vì nhét chuyến bay hay hành khách vào `ArrayList` rồi dò từ đầu tới cuối, nhóm chọn `LinkedHashMap` — một công đôi việc: tra cứu theo mã chỉ tốn thời gian hằng số $O(1)$, mà khi đổ lên bảng UI vẫn giữ đúng thứ tự thêm vào chứ không bị nhảy lộn xộn như `HashMap` thường. Khi khách click chọn nhiều ghế, `HashSet` tự động chặn trường hợp chọn trùng một ghế hai lần. Còn thay vì viết 6 hàm tìm kiếm giống hệt nhau cho chuyến bay, vé, hành khách, một hàm Generic `<T> T findById(...)` cân tất cả trong vài dòng.*
  > `[Chỉ sang cột phải]` *Ở bên phải là cách nhóm giải quyết lỗi theo ADR-0003. Hồi học Chương 4, nhiều bạn hay cho ngoại lệ kế thừa thẳng `Exception` (Checked). Kết quả là dùng Stream hay JavaFX Lambda đều gãy vì cứ bị trình biên dịch bắt `throws` dây chuyền! Nhóm chuyển toàn bộ 6 ngoại lệ nghiệp vụ kế thừa `RuntimeException` qua lớp gốc `AirlineReservationException`: hàm lõi chỉ việc ném lỗi rõ nghĩa ngay tại chỗ vi phạm, còn tầng UI chỉ cần bắt gọn một cửa để hiện thông báo màu đỏ cho người dùng."*
- **Câu chuyển:** *"Xử lý xong lỗi đầu vào rồi, còn cái 'ác mộng' tiếp theo là quản lý trạng thái đơn vé lên xuống theo thời gian — mời mọi người nhìn cách nhóm xóa sạch `switch-case` ở Slide 8."*

---

### Slide 8 — `State Pattern đóng gói vòng đời đặt chỗ`

- **Thời lượng:** 55–65 giây | **Mục tiêu:** Giải thích vì sao State Pattern thắng `switch-case` và trình diễn hiệu ứng domino Cascade (ADR-0004).
- **Trên màn hình có gì:** Trên: `5 Singleton States` (`Pending`, `Confirmed`, `CheckedIn`, `Completed`, `Cancelled`) ↔ `BookingState Interface`. Dưới: `Flight Lifecycle Cascade (cancelFlight() · arriveFlight())` ↔ `ADR-0004` (`Hủy -> Ghế AVAILABLE + REFUNDED`, `Hạ cánh -> CHECKED_IN thành COMPLETED`). Phải: `Trạng thái kết thúc ném InvalidStateException`.
- **Lời nói gợi ý:**
  > *"Một đơn đặt vé có 5 trạng thái (`Pending`, `Confirmed`, `CheckedIn`, `Completed`, `Cancelled`) và tới 7 hành động đụng vào nó. Thử tưởng tượng nếu viết bằng `switch-case`: bạn sẽ có một bảng ma trận 5 nhân 7 = 35 nhánh `if/switch` nằm rải rác khắp class `Booking`! Quên check một nhánh thôi — ví dụ cho người ta hủy lại cái vé đã bay xong — là hệ thống sai bét.*
  > `[Chỉ vào vòng State ở trên]` *Với **State Pattern**, nhóm biến mỗi trạng thái thành một 'người gác cổng' Singleton riêng thực thi interface `BookingState`. Class `Booking` chẳng cần nhớ luật gì cả: khách bấm Check-in hay Hủy, nó đẩy thẳng việc cho `state` hiện tại xử lý. Nếu vé đang ở trạng thái kết thúc như `Completed` hay `Cancelled`, chính người gác cổng đó tự chặn lại bằng `InvalidStateException`!*
  > *(Ghi chú nhỏ thú vị: riêng vòng đời `Flight` lại đi theo một đường thẳng `SCHEDULED → BOARDING → DEPARTED → ARRIVED`, nên nhóm cố tình KHÔNG đẻ thêm 5 class `FlightState` thừa thãi mà chỉ dùng 1 bảng tra gọn — tránh tự làm code rườm rà).*
  > `[Chỉ mũi tên Cascade bên dưới]` *Và đây là phần ăn khớp cực kỳ đã ở ADR-0004: hiệu ứng domino **Cascade**. Khi Admin bấm hủy một chuyến bay (`cancelFlight`), hệ thống quét toàn bộ đơn đang mở trên chuyến đó, tự động nhả ghế về `AVAILABLE` và chuyển mọi khoản tiền đã thu sang `REFUNDED`. Còn khi chuyến bay hạ cánh (`arriveFlight`), hành khách nào đã `CHECKED_IN` tự động được chốt `COMPLETED`."*
- **Câu chuyển:** *"Ghép State Pattern với Strategy và Factory, ta có một hệ thống đúng chuẩn 'dễ thay đổi' theo 5 nguyên lý SOLID ở Slide 9."*

---

### Slide 9 — `Mở rộng theo nguyên lý SOLID`

- **Thời lượng:** 50–60 giây | **Mục tiêu:** Cho thấy hệ thống **"mở rộng bằng cách thêm file mới chứ không mổ xẻ file cũ"** (liên hệ trực tiếp bài nói: *good codebase is easy to change*).
- **Trên màn hình có gì:** Trái: `OCP`, `SRP`, `DIP`. Phải: `SeatFactory & PaymentFactory` ↔ `PaymentStrategy (ADR-0002)` với 4 kênh (`Card`, `EWallet`, `BankTransfer`, `Cash`), hộp tóm tắt `S · O · L · I · D`.
- **Lời nói gợi ý:**
  > *"Quay lại đúng thông điệp đầu giờ của nhóm: một codebase tốt là codebase bạn có thể thêm tính năng mới mà **không sợ làm gãy code cũ**.*
  > *Giả sử ngày mai thầy đóng vai chủ hãng hàng không và yêu cầu nhóm: 'Thêm cho thầy phương thức thanh toán thứ 5 — ví dụ Apple Pay hoặc QR Pay — ngay chiều nay!'.* `[Nhìn về phía thầy, cười nhẹ]`
  > *Trong code của nhóm theo ADR-0002, câu trả lời rất nhẹ nhàng: nhờ **Dependency Inversion**, `Booking` và `Payment` không hề biết `CardPayment` hay `CashPayment` là ai — chúng chỉ nói chuyện qua đúng 1 interface `PaymentStrategy`. Việc tạo đối tượng được gom vào `SeatFactory` và `PaymentFactory` (**Single Responsibility**). Nên để thêm cổng thanh toán thứ 5, nhóm chỉ việc tạo 1 class mới implement `PaymentStrategy`, cắm vào `PaymentFactory` — còn toàn bộ bộ não tính tiền của `Booking` không cần sửa lấy một chữ! Đó là **Open / Closed** — đóng với sửa đổi cũ, mở cho mở rộng mới."*
- **Câu chuyển:** *"Lý thuyết kiến trúc xong xuôi rồi. Bây giờ phần hay nhất: không nghe nhóm nói suông nữa, bạn [Tên] sẽ đưa mọi người nhìn giao diện JavaFX và chạy trực tiếp app ngay trên màn hình — mời Slide 10!"*

---

### Slide 10 — Giao diện SPA (`PassengerWorkspaceView` & `SeatSelectionDialog`)

- **Thời lượng:** 40–45 giây | **Mục tiêu:** Giới thiệu nhanh bố cục thiết kế & nhấn mạnh **giá tiền hiện trên UI là do Domain tính thật, UI không tự nhân tay**.
- **Trên màn hình có gì:** Mockup giao diện sidebar trái (`Arbitrum Aero`, bảng hệ số `Business 2.0x`, `Economy+ 1.15x`, `Economy 1.0x`), sơ đồ `SeatSelectionDialog` bố cục `2 - AISLE - 2`, và cột `Booking Summary` bên phải với nút `Hold Seats` & `Reserve & Pay Now`.
- **Lời nói gợi ý:**
  > *"Đây là bản mockup giao diện người dùng mà nhóm hướng tới theo phong cách Single Page Application hiện đại: cả phiên làm việc gọn trong một khung cửa sổ, không mở bung chục tab rối mắt. Điểm mọi người cần để ý trên bản đồ `2 - Lối đi - 2` này là khung **Booking Summary** bên phải: mỗi cú click ghế trên màn hình đều gửi thẳng qua Controller xuống hàm đa hình `calculatePrice` của tầng Domain để cộng tiền theo thời gian thực — giao diện không hề viết lậu lại công thức `2.0x` hay `1.15x`."*
  > *(Lưu ý nhẹ: trên ảnh mockup slide minh họa giá gốc 1.2 triệu nên tổng là 3.78 triệu, còn trên dữ liệu chạy thật của `VN101` giá gốc 1.5 triệu nên ngay sau đây mọi người sẽ thấy tổng là 4.725 triệu nhé!)*
- **Câu chuyển:** *"Không chần chừ nữa, đây là 4 bước Live Demo ngay trên app thật ở Slide 11!"*

---

### Slide 11 — `Kịch bản Live Demo 4 bước` (`.\mvnw.cmd javafx:run`)

- **Thời lượng:** 3.5 – 4.5 phút (tính cả thời gian thao tác trên app) | **Mục tiêu:** Chạy mượt, diễn giải tự nhiên như một vòng kiểm thử thật (đặc biệt biến cú click giả lập lỗi thanh toán thành điểm nhấn!).
- **Trên màn hình có gì:** Khung nổi 4 bước: `1. Quick Login & Register`, `2. Seat Map & Hold PENDING (1B + 2A trên VN101)`, `3. Payment Retry & Check-in (Simulate Failure -> Retry -> Check In)`, `4. Admin Cascade (Hạ cánh VN101 COMPLETED · Hủy VN103 REFUNDED)`.
- **Lời nói gợi ý trước khi Alt+Tab:**
  > *"Trên Slide 11 là 4 bước nhóm sẽ chạy sống ngay bây giờ: từ đăng ký tài khoản mới tự cấp mã, chọn ghế 2-2 và giữ chỗ `PENDING`, cố tình làm rớt mạng cổng thanh toán xem hệ thống có mất ghế không rồi thử lại và Check-in, tới quyền Admin điều phối hạ cánh và kích hoạt hiệu ứng domino hủy chuyến hoàn tiền. Em xin phép `Alt + Tab` sang ứng dụng JavaFX chạy thật!"*
- *(Thực hiện chi tiết từng click theo **Mục 7** bên dưới!)*
- **Câu chuyển khi Alt+Tab về Slide 12:** *"Xong phần chạy thật! Nhưng thử hỏi: làm sao nhóm em dám sửa code liên tục từ Swing lên JavaFX 25 mà không sợ gãy mất cái luồng thanh toán và hoàn tiền vừa rồi? Bí mật nằm ở Slide 12!"*

---

### Slide 12 — `85 bài kiểm thử JUnit 5 bảo vệ hệ thống`

- **Thời lượng:** 40–50 giây | **Mục tiêu:** Đưa ra **Feedback Loop (vòng phản hồi tự động)** là vũ khí giữ chất lượng code — càng chạy nhanh càng dám cải tiến thiết kế.
- **Trên màn hình có gì:** Trái: Khung xanh lớn con số **`85`** (`100% PASSING`, `Unit & Integration Tests`, `Java 25`). Phải: 4 nhóm kiểm thử (Entities & Đa hình, Điều phối & Cascade, Payment & 6 Exceptions, Headless GUI Controller & CSS Parser).
- **Lời nói gợi ý:**
  > *"Trong kỹ thuật phần mềm có câu rất hay: **tốc độ nhận phản hồi chính là giới hạn tốc độ code của bạn**. Nếu cứ sửa một dòng lại phải mở giao diện click tay từ đầu 10 phút mới biết đúng sai thì không ai dám dọn code cả!*
  > *Vì vậy nhóm xây dựng mạng lưới an toàn gồm **85 bài kiểm thử tự động JUnit 5**, chạy hết chỉ trong hơn 2 giây với tỷ lệ vượt qua 100% — 0 lỗi (`85` bài trên `22` class test thực tế). Điểm bọn em tâm đắc nhất là cụm test **Headless GUI Controller**: nhờ kiến trúc Passive View không dính `javafx.*`, JUnit test được toàn bộ luồng đăng nhập, giữ ghế, mô phỏng lỗi thanh toán và cascade Admin trên `ReservationGuiController` mà không cần bật cửa sổ đồ họa, cộng thêm bài test `CssParser` soi từng dòng file CSS giao diện!"*
- **Câu chuyển:** *"Nhìn lại tổng thể từ đầu tới giờ, mời thầy đối chiếu toàn bộ sản phẩm của nhóm với 10 tiêu chí của môn học ở Slide 13."*

---

### Slide 13 — `Đối chiếu sản phẩm với 10 tiêu chí đề bài`

- **Thời lượng:** 35–40 giây | **Mục tiêu:** Tổng hợp ngắn gọn bản đồ minh chứng để giảng viên dễ kiểm tra từng hạng mục theo rubric của môn học, giữ thái độ cầu thị chờ thầy đánh giá (tránh tự chấm 100/100 điểm trên slide hay trong lời nói).
- **Trên màn hình có gì:** Trái: 3 viên thuốc (`Nhóm 1 · Thiết kế OOP (TC 1–4)`, `Nhóm 2 · Đa hình & SOLID (TC 5–8)`, `Nhóm 3 · Kiểm thử & Demo (TC 9–10)`). Phải: Bảng `Bản đồ minh chứng trong dự án (tr. 13)` chỉ rõ Slide / ADR / Test cho 10 tiêu chí + thanh lệnh Maven phía dưới.
- **Lời nói gợi ý:**
  > *"Trước khi khép lại phần trình bày, Slide 13 tổng hợp lại bản đồ đối chiếu để thầy tiện theo dõi và kiểm chứng các hạng mục theo 10 tiêu chí của môn học: nhóm tiêu chí 1–4 về phân tích bài toán, UML, đóng gói và Composition; nhóm 5–8 về kế thừa, đa hình, Collections, ngoại lệ và các mẫu thiết kế SOLID; và nhóm 9–10 về 85 bài JUnit test cùng phần Live Demo JavaFX vừa rồi. Bất kỳ lúc nào thầy cũng có thể chạy lại `.\mvnw.cmd test`, mở JavaFX bằng `.\mvnw.cmd javafx:run`, hoặc kiểm chứng tầng lõi qua Console `Main`. Nhóm rất mong nhận được đánh giá và góp ý của thầy ạ."*

---

### Slide 14 — `Thank you`

- **Thời lượng:** 10 giây
- **Trên màn hình có gì:** Mặt cười vẽ tay tối giản + `Thank you`.
- **Lời nói gợi ý:**
  > *"Cảm ơn thầy Việt và cả lớp đã lắng nghe câu chuyện kỹ thuật của Nhóm 4. Hy vọng phần chia sẻ vừa rồi cho thấy chỉ cần chắc nền tảng OOP, việc làm chủ một hệ thống nhiều tầng hoàn toàn nằm trong tầm tay. Bây giờ nhóm em rất mong nhận được các câu hỏi 'hỏi xoáy' từ thầy và các bạn ạ!"*

---

## 7. Kịch bản Live Demo 4 bước chi tiết (`.\mvnw.cmd javafx:run`)

> **Mẹo diễn khi demo:** Đừng im lặng bấm chuột! Hãy vừa click vừa thuật lại ý nghĩa OOP/nghiệp vụ phía sau cú click đó như mẫu lời dẫn in nghiêng bên dưới.

### Bước 1 — Quick Login & Đăng ký hành khách mới (`PAX-03`)
1. Trên màn hình Login, chỉ nhanh vào **3 chip `Quick Demo Accounts`** (`PAX-01`, `PAX-02`, `ADM-01`) để giới thiệu tiện ích demo nhanh.
2. Bấm nút **`Create Passenger Account`** mở `PassengerRegistrationDialog`.
3. Bấm **`Auto-Fill Sample`** *(hoặc nhập tay — lưu ý nút Auto-Fill điền sẵn mật khẩu là **`pass123`**)* → Bấm **`Create Account`**.
4. Hộp thoại đóng lại và quay về màn hình Login với ID **`PAX-03`** đã được điền sẵn → Nhập mật khẩu (`pass123`) → Bấm **`Sign In`** để vào `PassengerWorkspaceView`.
   - *Lời dẫn:* *"Thầy thấy hệ thống tự đếm sinh mã kế tiếp `PAX-03` và kiểm tra không trùng CCCD trước khi cho đăng nhập vào không gian riêng của hành khách."*

### Bước 2 — Mở sơ đồ ghế `2-AISLE-2` & Giữ chỗ `PENDING` (`1B + 2A` trên `VN101`)
1. Ở tab **`Search & Book Flights`**, bấm chọn dòng **`VN101 (HAN → SGN)`** (giá gốc `1,500,000 VND`) → **Double-click** dòng đó (hoặc bấm **`Select Seats · VN101`**) để mở `SeatSelectionDialog`.
2. Trên sơ đồ cabin:
   - Chỉ ghế **`1A` đã bị khóa màu tối (`OCCUPIED`)** do `PAX-01` đã mua sẵn trong `DemoDataSeeder`.
   - Click chọn **`1B`** (Business `2.0x = 3,000,000 VND`) và **`2A`** (Economy+ hàng đầu `1.15x = 1,725,000 VND`).
   - Chỉ sang khung **Booking Summary**: `Total Fare` nhảy ngay thành **`4,725,000 VND`**.
3. Bấm **`Hold Seats (Pay Later)`** → Chuyển sang tab **`My Bookings`** xem đơn mới (`BK-3`) ở trạng thái **`PENDING`**.
   - *Lời dẫn:* *"Khi chọn 1 ghế Business và 1 ghế Economy+, UI gửi thẳng danh sách sang tầng Domain để gọi đa hình `calculatePrice` tính ra 4,725,000 VND. Lúc bấm giữ chỗ, ghế chuyển thành `RESERVED` để không ai mua đè trong lúc mình chuẩn bị trả tiền."*

### Bước 3 — Thanh toán "Cố tình làm rớt mạng" → Thử lại → Check-in
1. Trong tab **`My Bookings`**, chọn dòng booking `PENDING` vừa tạo → Bấm **`Pay Booking`**.
2. Trong `PaymentDialog`, đổi qua lại 1–2 thẻ phương thức (Card / E-Wallet) cho thầy thấy form đổi nhãn theo Strategy.
3. Tick vào checkbox **`Simulate Payment Failure (Demo Retry Flow)`** → Bấm **`Confirm & Pay`**.
   - *Lời dẫn kịch tính:* *"Nào, bây giờ thử kịch bản khó chịu nhất ngoài đời: khách bấm trả tiền nhưng đúng lúc đó cổng thanh toán báo lỗi! Chuyện gì xảy ra ạ?"*
   - Kết quả trên màn hình: Dialog **giữ nguyên chưa đóng**, hiện banner cảnh báo màu vàng: *"Payment attempt ... FAILED — booking remains PENDING and seats stay held."* (không mất chỗ!).
4. Ngay trên dialog đó, **bỏ tick** `Simulate Payment Failure (Demo Retry Flow)` → Bấm **`Confirm & Pay`** lại một lần nữa.
   - Kết quả: Thanh toán thành công, dialog đóng, đơn chuyển **`CONFIRMED`**, ghế chuyển **`OCCUPIED`**.
5. Chọn lại đơn **`CONFIRMED`** đó → Bấm **`Check In`** → Trạng thái chuyển thành **`CHECKED_IN`**.

### *(Bước đệm 15 giây — BẮT BUỘC để Bước 4 hiện được `REFUNDED`)*
- Lý do: Trên `VN103`, đơn mẫu `BK-2` của `PAX-01` mới đang `PENDING` (chưa trả tiền), nên nếu hủy ngay `VN103` sẽ chưa thấy giao dịch hoàn tiền `REFUNDED`.
- Thao tác nhanh ngay trên tài khoản hiện tại: về tab **`Search & Book Flights`** → Mở sơ đồ ghế **`VN103`** → Chọn 1 ghế trống (ví dụ **`1A`** hoặc **`2C`**) → Bấm **`Reserve & Pay Now`** → Bấm **`Confirm & Pay`** trong `PaymentDialog`. Sau đó bấm **`Logout`** ở góc trên phải Navbar.
  - *Lời dẫn:* *"Em đặt nhanh thêm một vé trả tiền ngay trên chuyến `VN103` trước khi đổi sang vai Admin, để lát nữa mình kiểm chứng được trường hợp hoàn tiền khi hủy chuyến."*

### Bước 4 — Quyền Admin (`ADM-01`): Cho hạ cánh `VN101` & Kích hoạt Domino Hủy `VN103`
1. Tại màn hình Login, **double-click** chip **`ADM-01 · Admin`** (mật khẩu `admin123`) để vào `AdminWorkspaceView`.
2. Ở tab **`Flight Operations`**:
   - **Cho hạ cánh `VN101` (`CHECKED_IN → COMPLETED`):** Chọn dòng **`VN101`**, bấm tuần tự 3 nút mở khóa theo đúng trình tự bay: **`Mark Boarding`** → **`Mark Departed`** → **`Mark Arrived`**.
   - **Hủy chuyến `VN103` (`CANCELLED` + `REFUNDED`):** Chọn dòng **`VN103`** → Bấm **`Cancel Flight`**.
3. Chuyển sang tab **`Booking Ledger`**:
   - Chỉ 4 thẻ KPI trên cùng (số chuyến, số đơn, hành khách, doanh thu thực thu đã tự động trừ khoản vừa hoàn).
   - Chỉ xuống bảng dưới: đơn vừa check-in trên `VN101` đã chuyển thành **`COMPLETED`**, còn các đơn trên `VN103` đồng loạt thành **`CANCELLED`** kèm trạng thái thanh toán **`REFUNDED`**!

### Phương án dự phòng (nếu máy chiếu gặp lỗi hiển thị JavaFX)
Mở PowerShell chạy:
```powershell
.\mvnw.cmd compile exec:java -Dexec.mainClass="com.airlinereservation.Main"
```
Đây là bản **Console smoke run tự động** chạy tuần tự 6 kịch bản lõi trên **chính `AirlineReservationSystem`** (Search → Book → Fail/Retry Payment → Check-in → Cancel/Refund & Flight Cancel Cascade) và in toàn bộ kết quả ra terminal để chứng minh tầng Domain hoàn toàn độc lập với JavaFX.

---

## 8. Ngân hàng 16 Câu hỏi Q&A (Trả lời thẳng, gọn, đúng trọng tâm)

### Nhóm A — Nền tảng OOP (Chương 1–6)

1. **Hỏi:** *Tại sao danh sách `seats` đặt trong `Flight` mà không đặt trong `Aircraft`?*
   - **Đáp:** Dạ `Aircraft` chỉ là cấu hình vật lý dùng chung của tàu bay (A320neo, 16 chỗ), còn quyền bán và trạng thái "trống hay đã mua" thuộc về **từng chuyến bay cụ thể**. Một chiếc A320 bay cả chuyến sáng `VN101` lẫn chuyến chiều `VN205`; đặt ghế trong `Aircraft` thì khóa `1A` chuyến sáng sẽ hỏng luôn chuyến chiều. Vì vậy theo ADR-0001, `Flight` giữ `Map<String, Seat>` theo quan hệ Composition, còn `Aircraft` là liên kết Association đọc thông số thôi ạ.

2. **Hỏi:** *Khi nào nhóm dùng `abstract class`, khi nào dùng `interface`?*
   - **Đáp:** Dạ khi các lớp con chia sẻ **dữ liệu trạng thái và code chung** thì dùng `abstract class` — như `Person` (chung `id`, `fullName`, `email`, `password`) cho `Passenger`/`Admin`, hoặc `Seat` (chung `seatNumber`, `status`, `reserve()`, `occupy()`, `release()`) cho `BusinessSeat`/`EconomySeat`. Còn khi các lớp chỉ cần chung **hợp đồng hành vi** mà không chung dữ liệu bên trong — như `BookingState` hay `PaymentStrategy` — nhóm dùng `interface` ạ.

3. **Hỏi:** *Đa hình thể hiện rõ nhất ở đoạn code nào?*
   - **Đáp:** Dạ rõ nhất ở `Booking.calculateTotalPrice()`: hàm chỉ duyệt danh sách `Seat` và gọi đúng một phương thức `seat.calculatePrice(basePrice)`. Lúc chạy thực tế, `BusinessSeat` tự nhân `2.0x`, `EconomySeat` tự kiểm tra hàng đầu `1.15x` hay thường `1.0x`, hoàn toàn không có `if-else` chia loại ghế. Ngoài ra còn có `PaymentStrategy.pay(amount)` và `BookingState` ủy quyền hành vi theo trạng thái ạ.

4. **Hỏi:** *Trong dự án có chỗ nào còn dùng `instanceof` không?*
   - **Đáp:** Dạ toàn bộ tầng nghiệp vụ Domain (tính tiền, đặt chỗ, thanh toán, điều phối hệ thống) là **0% `instanceof`**. Duy nhất ở lớp UI `SeatSelectionDialog` có kiểm tra `instanceof BusinessSeat` / `EconomySeat` để tô màu nút ghế vàng cho hạng Thương gia và gắn dòng chữ `"Lounge"` trên màn hình cho người dùng dễ nhìn thôi ạ.

5. **Hỏi:** *Đóng gói bảo vệ bất biến thế nào nếu ai đó gọi `flight.getSeats()` rồi `.clear()`?*
   - **Đáp:** Dạ mọi thuộc tính trong `Flight`, `Booking`, `Seat` đều là `private`/`protected`, và tất cả các hàm getter trả về danh sách như `Flight.getSeats()` hay `Passenger.getBookingHistory()` đều bọc bằng `List.copyOf(...)`. Nếu bên ngoài gọi `.clear()` hay `.add()`, Java lập tức chặn lại bằng `UnsupportedOperationException` ạ.

---

### Nhóm B — Collections, Generics & Ngoại lệ (Slide 7)

6. **Hỏi:** *Sao lại chọn `LinkedHashMap` mà không dùng `ArrayList` hay `HashMap`?*
   - **Đáp:** Dạ `ArrayList` tìm theo mã mất $O(n)$, còn `HashMap` tìm được $O(1)$ nhưng thứ tự phần tử bị xáo trộn mỗi lần duyệt. `LinkedHashMap` giữ được cả hai thứ nhóm cần: tra cứu theo mã $O(1)$ và giữ nguyên đúng thứ tự chèn để ghế `1A, 1B, 1C, 1D` hay bảng chuyến bay hiển thị ổn định trên giao diện ạ.

7. **Hỏi:** *`HashSet` giải quyết bài toán gì trong hệ thống?*
   - **Đáp:** Dạ dùng ở luồng giữ nhiều ghế (`Booking` và kiểm tra đầu vào trong `AirlineReservationSystem` / `ReservationGuiController`) để chặn ngay việc truyền trùng một mã ghế hai lần như `["1A", "1A"]` trong thời gian $O(1)$, còn trên `SeatSelectionDialog` dùng `LinkedHashSet` để vừa chống trùng vừa giữ thứ tự người dùng bấm ạ.

8. **Hỏi:** *Hàm Generic `<T> T findById(...)` viết ra để làm gì?*
   - **Đáp:** Dạ trong `AirlineReservationSystem` có nhiều danh mục lưu dạng `Map<String, T>` (`passengers`, `admins`, `bookings`...). Thay vì copy-paste 3–4 hàm tìm kiếm chỉ khác kiểu trả về và câu báo lỗi, nhóm viết một hàm generic dùng chung để kiểm tra khóa không rỗng, tra $O(1)$ và ném `EntityNotFoundException` thống nhất ạ.

9. **Hỏi:** *Tại sao cả 6 ngoại lệ miền đều kế thừa `RuntimeException` (Unchecked)? (ADR-0003)*
   - **Đáp:** Dạ vì chọn nhầm ghế đã bán hoặc hủy vé đã hoàn thành là lỗi vi phạm nghiệp vụ/trạng thái chứ không phải lỗi phần cứng bắt buộc `try-catch` ở từng hàm nhỏ. Dùng `RuntimeException` giữ cho Lambda trong Stream, `@FunctionalInterface PaymentStrategy` và các chữ ký hàm lõi cực sạch, rồi toàn bộ lỗi nổi thẳng lên biên trên để `ReservationGuiController` / UI bắt gọn qua một nhánh `catch (AirlineReservationException e)` ạ.

---

### Nhóm C — Design Patterns & SOLID (Slides 8 & 9)

10. **Hỏi:** *Nếu dùng `enum` + `switch-case` cho `Booking` thay cho State Pattern thì có dở hơn chỗ nào?*
    - **Đáp:** Dạ nếu có 5 trạng thái × 7 hành động mà viết `switch-case` thì mỗi lần thêm hoặc sửa luật một trạng thái, mình phải đi mò từng nhánh rẽ ở cả 7 hàm rất dễ sót. State Pattern gom toàn bộ luật của từng trạng thái vào đúng 1 lớp nhỏ gọn (như `PendingBookingState` hay `CheckedInBookingState`), lại dùng chung mặc định ném `InvalidStateException` ở `BookingState` cho mọi chuyển đổi trái phép nên code vừa ngắn vừa khó sai sót ạ.

11. **Hỏi:** *Khi chuyến bay hạ cánh `arriveFlight()`, đơn `CONFIRMED` nhưng khách quên check-in (No-show) thì sao?*
    - **Đáp:** Dạ theo thiết kế ADR-0004, `arriveFlight()` chỉ chuyển các đơn **`CHECKED_IN`** sang `COMPLETED` (và hủy các đơn **`PENDING`** chưa trả tiền để nhả ghế), còn đơn **`CONFIRMED` chưa check-in** được giữ nguyên trạng thái `CONFIRMED` để ghi nhận khách không lên máy bay (No-show) và không tự động hoàn tiền ạ.

12. **Hỏi:** *Muốn thêm phương thức thanh toán thứ 5 (ví dụ QR Pay) thì phải sửa những file nào?*
    - **Đáp:** Dạ chỉ thêm ở lớp ngoài: thêm giá trị enum `PaymentMethod`, tạo class `QrPayPayment implements PaymentStrategy`, đăng ký vào `PaymentFactory` (và mục chọn ở `PaymentDialog` nếu hiện lên UI). Toàn bộ các lớp lõi `Booking`, `Payment`, `AirlineReservationSystem` không phải sửa một dòng nào — đúng tinh thần Open/Closed và Dependency Inversion ạ.

13. **Hỏi:** *Tóm tắt 5 chữ cái S-O-L-I-D trong đồ án này trong chưa tới 30 giây?*
    - **Đáp:**
      - **S:** `SeatFactory` tạo ghế, `PaymentFactory` tạo thanh toán, View vẽ UI, Domain xử lý nghiệp vụ — mỗi lớp một việc.
      - **O:** Thêm hạng ghế hay cổng thanh toán mới không sửa code tính tiền cũ của `Booking`.
      - **L:** `BusinessSeat` và `EconomySeat` thay thế hoàn toàn `Seat`; 4 Strategy thay thế hoàn toàn `PaymentStrategy`.
      - **I:** `PaymentStrategy` chỉ ép cài đúng 1 hàm `pay(amount)`; `BookingState` chỉ lo vòng đời đặt chỗ.
      - **D:** `Booking` và `Payment` chỉ phụ thuộc interface `BookingState` và `PaymentStrategy`, không dính cứng lớp cụ thể.

---

### Nhóm D — Kiến trúc JavaFX, Passive View & Kiểm thử (Slides 4, 10–12)

14. **Hỏi:** *Passive View giúp gì cho việc test mà MVC thường khó làm hơn?*
    - **Đáp:** Dạ vì các màn hình JavaFX không giữ logic nghiệp vụ và `ReservationGuiController` **không hề import thư viện `javafx.*`**, nhóm chạy được 12 bài integration test ngay trong `ReservationGuiControllerTest` ở chế độ headless (chạy trong 70 mili-giây không cần mở cửa sổ thật) mà vẫn phủ được luồng đăng nhập, giữ ghế, mô phỏng lỗi thanh toán và cascade Admin ạ.

15. **Hỏi:** *Tại sao gọi giao diện này là Single Page Application (SPA)?*
    - **Đáp:** Dạ theo ADR-0006, toàn bộ ứng dụng chỉ dùng đúng một cửa sổ `MainWindow` với thanh Navbar cố định, đổi luân phiên vùng nội dung bên trong giữa màn Login, `PassengerWorkspaceView` và `AdminWorkspaceView`; chỉ các tác vụ cần tập trung sâu (Đăng ký, Chọn ghế `SeatSelectionDialog`, Thanh toán `PaymentDialog`) mới mở hộp thoại modal gọn nhẹ ạ.

16. **Hỏi:** *Nếu đưa hệ thống này lên web nhiều người đặt cùng lúc thì cần bổ sung gì?*
    - **Đáp:** Dạ hiện tại JavaFX chạy các thao tác UI tuần tự trên JavaFX Application Thread nên không tranh chấp luồng. Nếu lên web đa luồng, nhóm cần bổ sung cơ chế khóa đồng bộ (lock theo `Flight`/`Seat` ở mức ứng dụng hoặc khóa lạc quan trong database) tại hàm `bookSeats()` / `createBooking()` để chặn hai người cùng đọc thấy ghế `1A` đang `AVAILABLE` tại cùng một thời điểm ạ.
