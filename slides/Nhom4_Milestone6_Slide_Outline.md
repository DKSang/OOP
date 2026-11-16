# Dàn ý Chi tiết & Kịch bản Thuyết trình Milestone 6 (Final Product)

- **Học phần:** Lập trình Hướng đối tượng (`INT1332_CLC`) — Học viện Công nghệ Bưu chính Viễn thông (PTIT)
- **Giảng viên hướng dẫn:** ThS. Phạm Hoàng Việt
- **Đề tài 21 (Advanced):** Airline Reservation System — Hệ thống Đặt vé Máy bay
- **Nhóm thực hiện:** Nhóm 4 — Lớp `D24CQCE05`
  - Đồng Việt Hưng (`B24DCCE117`)
  - Dương Kim Sang (`B24DCCE236`)
  - Lê Bảo Phúc (`B24DCCE215`)
  - Nguyễn Hữu Phi Long (`B24DCCE166`)
- **Phong cách thiết kế:** `anthropic-style-slides` (16:9, `1920×1080`, nền giấy ấm `--ad-paper`, điểm nhấn terracotta `--ad-clay`, bảng màu sage/lavender/peach/sky)
- **Ngôn ngữ:** Song ngữ Việt – Anh (Tiêu đề & ý chính tiếng Việt ngắn gọn, giữ nguyên thuật ngữ OOP, tên class/method/pattern tiếng Anh)
- **Thời lượng mục tiêu:** 12–15 phút trình bày + Q&A (linh hoạt cho 1 người thuyết trình xuyên suốt hoặc chia 4 phần cho 4 thành viên)

---

## Bảng ánh xạ 10 Tiêu chí Tham khảo (Thang 100 điểm) vào 14 Slides

| # | Tiêu chí chấm điểm (theo `Introduction.pdf` trang 13) | Điểm | Slide đảm nhiệm | Bằng chứng trong Repo (Code / ADR / Test) |
| :-: | :--- | :-: | :--- | :--- |
| 1 | **Problem analysis & requirements** | 10 | Slide 2, Slide 3 | Bài toán đặt vé đa hạng ghế, giữ chỗ/thanh toán lại, điều phối chuyến bay |
| 2 | **UML & object modeling** | 10 | Slide 4 | `diagrams/Nhom4_Milestone3_ClassDiagram.png`, 38 lớp/interface/enum |
| 3 | **Encapsulation & class design** | 10 | Slide 5 | Trường `private`/`protected`, `ValidationException` tại biên, `List.copyOf` |
| 4 | **Association / composition** | 10 | Slide 5 | **ADR-0001**: `Flight` sở hữu `Seat` (Composition), cô lập trạng thái giữa các chuyến |
| 5 | **Inheritance & polymorphism** | 10 | Slide 6 | `Person` → `Passenger`/`Admin`; `Seat` → `BusinessSeat (2.0x)` / `EconomySeat (1.0x/1.15x)` |
| 6 | **Interfaces & abstraction** | 10 | Slide 6, Slide 8, Slide 9 | `PaymentStrategy` (`@FunctionalInterface`), `BookingState`, `Person`, `Seat` |
| 7 | **Collections, generics, exceptions** | 10 | Slide 7 | `LinkedHashMap` $O(1)$, `HashSet`, Generic `<T> findById`, **ADR-0003** Unchecked Exceptions |
| 8 | **SOLID / design patterns** | 10 | Slide 8, Slide 9 | **ADR-0002**, **ADR-0004**, **ADR-0005/0006**: State, Strategy, Factory, MVC Passive View |
| 9 | **Code quality & testing** | 10 | Slide 12 | **85 bài kiểm thử JUnit 5** (18 test classes) xanh 100%, kiểm thử cả miền & GUI headless |
| 10 | **Demo & presentation** | 10 | Slide 10, Slide 11, Slide 13 | Giao diện JavaFX 25 SPA, `DemoDataSeeder`, kịch bản Live Demo 4 bước |

---

## Chi tiết Từng Slide (Nội dung Hiển thị & Speaker Notes)

---

### PHẦN I: BÀI TOÁN, TIẾN TRÌNH & KIẾN TRÚC TỔNG THỂ (Slides 1–4)
*(Thời lượng gợi ý: ~3 phút)*

#### Slide 1 — Trang bìa (Cover)
- **Layout (`data-layout`):** `cover` (Nền `sage-soft`, họa tiết hình học & mạng lưới nút bên trái, khối tiêu đề serif bên phải)
- **Eyebrow (Mono):** `INT1332_CLC · MILESTONE 6 · GROUP 4 (D24CQCE05)`
- **Title (Serif 100px):** `Airline Reservation System`
- **Subtitle / Role (Mono & Serif):**
  - Hệ thống Đặt vé & Điều phối Chuyến bay Hướng đối tượng
  - Thành viên: Đồng Việt Hưng · Dương Kim Sang · Lê Bảo Phúc · Nguyễn Hữu Phi Long
  - Giảng viên hướng dẫn: ThS. Phạm Hoàng Việt
- **Speaker Notes (Lời dẫn thuyết trình):**
  > "Xin chào thầy và các bạn. Hôm nay Nhóm 4 lớp D24CQCE05 xin trình bày sản phẩm hoàn chỉnh ở Milestone 6 của Đề tài số 21 (nhóm đề tài nâng cao): Hệ thống Đặt vé Máy bay — Airline Reservation System. Bài trình bày của nhóm sẽ đi qua 4 chặng: (1) Phân tích bài toán và mô hình UML, (2) Các trụ cột OOP, Collections và Xử lý ngoại lệ, (3) Cải tiến SOLID & Design Patterns ở Milestone 5, và cuối cùng là (4) Kiến trúc JavaFX 25, bộ 85 bài kiểm thử JUnit 5 cùng phần Live Demo trực tiếp."

---

#### Slide 2 — Hành trình tiến hóa qua 6 Milestones
- **Layout (`data-layout`):** `timeline` (Trục thời gian ngang với 6 điểm mốc, điểm cuối `M6 · TODAY` nổi bật bằng màu `--ad-clay`)
- **Title (Serif 84px):** `Hành trình phát triển qua 6 cột mốc`
- **6 Điểm mốc trên trục (Axis Nodes & Cards):**
  1. `M1 · Problem`: Đặc tả bài toán đặt vé, ca sử dụng `Passenger` & `Admin`, quy tắc không đặt trùng ghế
  2. `M2 · OO Analysis`: Xác định 11 thực thể miền cốt lõi và trách nhiệm từng lớp (`CRC`)
  3. `M3 · UML Design`: Chuẩn hóa Class Diagram, quan hệ Composition `Flight–Seat` (`ADR-0001`)
  4. `M4 · Core Java 25`: Hiện thực hóa đa hình, Collections, Unchecked Exceptions & 52 Unit Tests
  5. `M5 · SOLID & Patterns`: Tái cấu trúc với `BookingState`, `PaymentStrategy`, `SeatFactory` & `PaymentFactory`
  6. `M6 · Final Product (TODAY)`: Giao diện **JavaFX 25 SPA**, kiến trúc **Passive View** & **85 JUnit 5 Tests**
- **Speaker Notes:**
  > "Để xây dựng một hệ thống vừa chặt chẽ về nghiệp vụ vừa dễ mở rộng, nhóm không viết code ngay từ đầu mà phát triển tuần tự qua 6 cột mốc: M1 phân tích bài toán và ca sử dụng, M2 xác định 11 lớp miền cốt lõi, M3 thiết kế UML với quyết định Composition `Flight–Seat` (`ADR-0001`), M4 hiện thực hóa lõi Java 25 cùng 52 unit test, M5 tái cấu trúc theo SOLID với `BookingState`, `PaymentStrategy` và Factory, và M6 hôm nay là sản phẩm hoàn thiện với giao diện JavaFX 25 SPA cùng 85 bài kiểm thử JUnit 5."

---

#### Slide 3 — Phân tích bài toán & Yêu cầu nghiệp vụ (Tiêu chí 1 — 10 điểm)
- **Layout (`data-layout`):** `pillars` (3 cột song song ngăn cách bởi đường kẻ mảnh 1px, mỗi cột gồm icon, tiêu đề có 1 từ khóa in đậm và mô tả cô đọng)
- **Title (Serif 84px):** `Ba bài toán cốt lõi của hệ thống đặt vé hàng không`
- **Nội dung 3 cột (Pillars):**
  - **Cột 1 (Icon `user`):** Luồng **Hành khách** (`Passenger`)
    - Tìm kiếm chuyến bay theo tuyến (`HAN → SGN`), ngày bay và số ghế trống; chọn ghế trực quan trên sơ đồ khoang; giữ chỗ (`PENDING`), thanh toán thử lại, check-in hoặc hủy vé hoàn tiền.
  - **Cột 2 (Icon `server`):** Điều phối **Chuyến bay** (`Admin`)
    - Quản lý danh mục sân bay, tàu bay (`A320neo`, `787-9 Dreamliner`), mở chuyến bay mới và điều khiển vòng đời vận hành (`SCHEDULED → BOARDING → DEPARTED → ARRIVED / CANCELLED`).
  - **Cột 3 (Icon `shield`):** Bảo toàn **Bất biến miền** (`Invariants`)
    - Chống đặt trùng ghế (double-booking), cô lập sơ đồ ghế giữa các chuyến bay dùng chung tàu bay, và tự động đồng bộ trạng thái dây chuyền (cascade) khi hủy chuyến hoặc hạ cánh.
- **Speaker Notes:**
  > "Đề tài 21 đặt ra bài toán thực tế phức tạp hơn nhiều so với CRUD thông thường. Hệ thống giải quyết 3 bài toán cốt lõi: Thứ nhất là hành trình của hành khách từ tìm chuyến, chọn từng vị trí ghế trên tàu bay, giữ chỗ chờ thanh toán đến làm thủ tục check-in. Thứ hai là công cụ điều phối của Admin quản lý đội tàu bay và lịch trình. Thứ ba — quan trọng nhất về mặt kỹ thuật — là bảo toàn các bất biến nghiệp vụ: không bao giờ để 2 người giữ cùng 1 ghế và tự động giải phóng ghế/hoàn tiền khi có sự cố hủy chuyến."

---

#### Slide 4 — Mô hình hóa UML & Kiến trúc phân tầng (Tiêu chí 2 — 10 điểm)
- **Layout (`data-layout`):** `dashed-system` (Kiến trúc hệ thống gồm 2 vùng nét đứt `ad-region` hai bên kết nối qua nút điều phối trung tâm)
- **Title (Serif 84px):** `Kiến trúc Passive View tách biệt hoàn toàn giao diện và nghiệp vụ`
- **Cấu trúc sơ đồ trên slide:**
  - **Vùng trái (`ad-region` — Tầng Giao diện JavaFX 25 SPA):**
    - `MainWindow` · `LoginView` · `PassengerWorkspaceView` · `SeatSelectionDialog` · `PaymentDialog` · `AdminWorkspaceView`
  - **Nút trung tâm (`ad-node` — Seam điều phối):**
    - `ReservationGuiController` ↔ `AirlineReservationSystem`
  - **Vùng phải (`ad-region` — Tầng Miền & Mẫu thiết kế):**
    - `Flight` · `Aircraft` · `Airport` · `Seat` (`Business` / `Economy`)
    - `Booking` + `BookingState` (5 states) · `Payment` + `PaymentStrategy` (4 strategies) · `SeatFactory` & `PaymentFactory`
- **Speaker Notes:**
  > "Về mô hình hóa đối tượng và kiến trúc tổng thể (theo ADR-0005 và ADR-0006), toàn bộ 38 lớp và giao diện của dự án được chia làm 3 tầng rõ rệt. Điểm đắt giá nhất trong thiết kế này là mẫu Passive View qua lớp `ReservationGuiController`: các màn hình JavaFX chỉ làm nhiệm vụ hiển thị và bắt sự kiện, tuyệt đối không chứa logic nghiệp vụ; ngược lại, tầng miền và controller hoàn toàn không phụ thuộc vào bất kỳ lớp đồ họa `javafx.*` nào. Nhờ đó, toàn bộ luồng điều khiển giao diện có thể chạy kiểm thử tự động trong tích tắc mà không cần mở cửa sổ thật."

---

### PHẦN II: NỀN TẢNG OOP, COLLECTIONS & XỬ LÝ NGOẠI LỆ (Slides 5–7)
*(Thời lượng gợi ý: ~3.5 phút)*

#### Slide 5 — Đóng gói & Quan hệ Composition `Flight–Seat` (Tiêu chí 3 & 4 — 20 điểm)
- **Layout (`data-layout`):** `split-fanout` (Cột trái nêu luận điểm + Cột phải vẽ sơ đồ 1 `Aircraft` mẫu sinh ra các sơ đồ `Seat` độc lập bên trong từng `Flight`)
- **Title cột trái (Serif):** `Mỗi chuyến bay sở hữu sơ đồ ghế độc lập`
- **3 Điểm nhấn cột trái:**
  - **Composition (`ADR-0001`):** `Aircraft` chỉ lưu cấu hình sức chứa (`totalSeats`); mỗi `Flight` khởi tạo và sở hữu riêng `Map<String, Seat>` khi được tạo.
  - **Cô lập trạng thái tuyệt đối:** Đặt ghế `1A` trên `VN101` không bao giờ làm ảnh hưởng tới ghế `1A` trên `VN205` dù dùng chung tàu bay `AC-320`.
  - **Đóng gói bất biến (`Encapsulation`):** Mọi trường dữ liệu là `private`/`protected`; các hàm đọc danh sách luôn trả về bản sao bất biến qua `List.copyOf(...)`.
- **Sơ đồ cột phải (`Fan-out Diagram`):**
  - Nguồn: `Aircraft AC-320 (A320neo · 16 seats)`
  - Nhánh 1 (`read-write` clay tag): `Flight VN101 (HAN → SGN)` → `Seat 1A [OCCUPIED]`, `Seat 1B [RESERVED]`
  - Nhánh 2 (`read-write` clay tag): `Flight VN205 (SGN → DAD)` → `Seat 1A [AVAILABLE]`, `Seat 1B [AVAILABLE]`
  - Nhánh 3 (`read-only` stone tag): `getSeats()` / `getSelectedSeats()` → `List.copyOf(...)` (chặn sửa đổi trực tiếp từ bên ngoài)
- **Speaker Notes:**
  > "Ở tiêu chí Đóng gói và Quan hệ giữa các lớp, quyết định thiết kế quan trọng nhất của nhóm được ghi trong ADR-0001. Một chiếc tàu bay ngoài đời thực bay nhiều chặng mỗi ngày; nếu đặt danh sách ghế nằm trong lớp `Aircraft`, việc đặt ghế ở chuyến sáng sẽ làm mất ghế ở chuyến chiều. Vì vậy, nhóm thiết kế quan hệ Association giữa `Flight` và `Aircraft`, nhưng dùng quan hệ Composition (sở hữu chặt) giữa `Flight` và `Seat`: mỗi chuyến bay tự sinh và quản lý sơ đồ ghế riêng. Đồng thời, mọi getter trả về danh sách đều bọc qua `List.copyOf`, ngăn chặn hoàn toàn việc code bên ngoài tự ý thêm/xóa phần tử mà không qua kiểm tra nghiệp vụ."

---

#### Slide 6 — Kế thừa, Trừu tượng & Đa hình thời gian chạy (Tiêu chí 5 & 6 — 20 điểm)
- **Layout (`data-layout`):** `tint-cards` (3 thẻ màu pastel: `sage`, `lavender`, `peach` đặt cạnh nhau, mỗi thẻ đại diện cho 1 trục kế thừa/trừu tượng)
- **Title (Serif 84px):** `Đa hình giúp loại bỏ hoàn toàn cấu trúc rẽ nhánh kiểm tra kiểu`
- **3 Thẻ nội dung (`Tint Cards`):**
  - **Thẻ 1 (`sage` — Icon `user`):** Trục định danh `Person (abstract)`
    - Lớp con: `Passenger` (lịch sử `bookingHistory`, số hộ chiếu/CCCD) & `Admin` (`staffCode`, quyền điều phối).
    - Đa hình: Ghi đè `getRole()` và dùng chung hành vi kiểm tra thông tin liên hệ `updateContactInfo(...)`.
  - **Thẻ 2 (`lavender` — Icon `layers`):** Trục tính giá `Seat (abstract)`
    - Lớp con: `BusinessSeat` (`2.0x` + quyền phòng chờ `hasLoungeAccess`) & `EconomySeat` (`1.0x` tiêu chuẩn hoặc `1.15x` cho hàng `Extra Legroom`).
    - Đa hình: `Booking.calculateTotalPrice()` gọi `seat.calculatePrice(basePrice)` qua Stream mà không dùng bất kỳ lệnh `instanceof` hay `if-else` nào.
  - **Thẻ 3 (`peach` — Icon `key`):** Giao diện `PaymentStrategy`
    - Đánh dấu `@FunctionalInterface` với phương thức duy nhất `pay(double amount)`.
    - Cho phép truyền cả 4 lớp chiến lược thực tế lẫn biểu thức Lambda khi mô phỏng lỗi cổng thanh toán.
- **Speaker Notes:**
  > "Về Kế thừa, Trừu tượng và Đa hình, hệ thống xây dựng 3 trục rõ ràng. Trục thứ nhất là lớp trừu tượng `Person` chia sẻ thông tin định danh cho `Passenger` và `Admin`. Trục thứ hai là lớp trừu tượng `Seat` với phương thức `calculatePrice(basePrice)`: ghế Thương gia nhân hệ số 2.0 kèm phòng chờ, ghế Phổ thông hàng đầu cộng phụ phí chỗ để chân rộng 1.15x. Nhờ đa hình động, hàm tính tổng tiền của đơn đặt vé chỉ cần gọi `selectedSeats.stream().mapToDouble(s -> s.calculatePrice(basePrice)).sum()` — hoàn toàn sạch bóng `if-else` hay `instanceof`. Trục thứ ba là giao diện hàm `PaymentStrategy` phục vụ đa hình các phương thức thanh toán."

---

#### Slide 7 — Collections, Generics & Phân cấp Ngoại lệ Miền (Tiêu chí 7 — 10 điểm)
- **Layout (`data-layout`):** `two-panel` (2 bảng đối xứng trên nền `stone-200`: Bảng trái trình bày Collections & Generics; Bảng phải trình bày 6 lớp Unchecked Domain Exceptions)
- **Title (Serif 84px):** `Tra cứu O(1) với Collections và kiểm soát lỗi biên bằng Unchecked Exceptions`
- **Bảng trái — `Collections, Generics & Stream API`:**
  - `LinkedHashMap<String, V>`: Lưu danh mục `airports`, `aircrafts`, `flights`, `passengers`, `bookings` và sơ đồ `Flight.seats` → Vừa tra cứu theo mã $O(1)$, vừa bảo toàn thứ tự hiển thị (`1A, 1B, 1C, 1D`).
  - `HashSet<Seat>` & `HashSet<String>`: Phát hiện tức thì ghế trùng lặp trong cùng một yêu cầu đặt chỗ.
  - Generic Helper `<T> T findById(Map<String, T>, ...)`: Tái sử dụng logic tra cứu an toàn kiểu cho mọi thực thể, tự động ném `EntityNotFoundException` nếu vắng mặt.
- **Bảng phải — `Phân cấp Unchecked Exceptions (ADR-0003)`:**
  - Gốc: `AirlineReservationException extends RuntimeException` (giữ sạch chữ ký Stream API & UI Controller).
  - 6 lớp chuyên biệt:
    1. `ValidationException` (dữ liệu rỗng/âm, trùng sân bay đi-đến)
    2. `EntityNotFoundException` (không tìm thấy mã tra cứu)
    3. `DuplicateEntityException` (trùng mã khóa chính / CCCD)
    4. `SeatNotAvailableException` (ghế đã đặt hoặc khác chuyến)
    5. `InvalidStateException` (vi phạm máy trạng thái FSM)
    6. `PaymentException` (lệch số tiền / hoàn tiền sai trạng thái)
- **Speaker Notes:**
  > "Ở tiêu chí số 7 về Collections, Generics và Xử lý ngoại lệ: Nhóm chọn `LinkedHashMap` thay vì `ArrayList` hay `HashMap` thông thường cho toàn bộ danh mục hệ thống và sơ đồ ghế, vì chúng ta cần cả hai đặc tính: tốc độ tra cứu theo mã $O(1)$ và giữ nguyên thứ tự ghế từ hàng 1 đến hàng cuối khi vẽ lên giao diện. Về xử lý ngoại lệ (ADR-0003), toàn bộ 6 lớp ngoại lệ miền đều kế thừa từ `AirlineReservationException` mở rộng `RuntimeException`. Việc dùng Unchecked Exception theo phong cách Spring/Hibernate hiện đại giúp các biểu thức Stream API và giao diện `PaymentStrategy` không bị ô nhiễm bởi từ khóa `throws`, trong khi tầng GUI vẫn bắt và hiển thị thông báo lỗi chính xác theo từng tình huống nghiệp vụ."

---

### PHẦN III: SOLID & DESIGN PATTERNS — MILESTONE 5 (Slides 8–9)
*(Thời lượng gợi ý: ~3.5 phút)*

#### Slide 8 — State Pattern & Hiệu ứng Dây chuyền Vòng đời Đặt vé (Tiêu chí 8a — SOLID & Patterns)
- **Layout (`data-layout`):** `container-flow` (Khung lớn `stone-100` biểu diễn máy trạng thái `BookingState` gồm 5 trạng thái kết nối với trạng thái ghế `SeatStatus` và hiệu ứng Cascade từ `Flight`)
- **Title (Serif 84px):** `State Pattern đóng gói trọn vẹn vòng đời đơn đặt chỗ và hiệu ứng dây chuyền`
- **Sơ đồ dòng chảy bên trong Container (`ADR-0004`):**
  - Giao diện `BookingState` được hiện thực bởi 5 lớp Singleton trạng thái:
    1. `PendingBookingState` (`Ghế = RESERVED`): Cho phép `confirmPayment()` (nếu lỗi vẫn giữ `PENDING` để thử lại) hoặc `cancel()` (`Ghế → AVAILABLE`).
    2. `ConfirmedBookingState` (`Ghế = OCCUPIED`): Cho phép `checkIn()` (khi chuyến bay đang `SCHEDULED`/`BOARDING`) hoặc `cancel()` (`Ghế → AVAILABLE`, `Payment → REFUNDED`).
    3. `CheckedInBookingState`: Chặn hủy vé cá nhân; khi chuyến bay hạ cánh (`onFlightArrived`) tự động chuyển sang `CompletedBookingState`.
    4. `CompletedBookingState` & 5. `CancelledBookingState`: Các trạng thái kết thúc (Terminal States), từ chối mọi thao tác thay đổi bằng `InvalidStateException`.
  - **Hiệu ứng dây chuyền (Cascade Box ở cạnh bên):**
    - `cancelFlight(flightId)` → Toàn bộ đơn đang hoạt động chuyển sang `CANCELLED`, nhả hết ghế về `AVAILABLE` và tự động `REFUNDED` mọi thanh toán đã thu.
    - `arriveFlight(flightId)` → Đơn `CHECKED_IN` thành `COMPLETED`; đơn `PENDING` quá hạn bị `CANCELLED`; khách No-show giữ `CONFIRMED`.
- **Speaker Notes:**
  > "Bước sang Milestone 5 — Cải tiến thiết kế theo SOLID và Design Patterns (ADR-0004): Thay vì để lớp `Booking` phình to với hàng loạt câu lệnh `switch-case` kiểm tra trạng thái, nhóm áp dụng **State Pattern** thông qua giao diện `BookingState` và 5 lớp trạng thái cụ thể. Mỗi lớp trạng thái tự quyết định hành động nào được phép và hành động nào bị chặn bởi `InvalidStateException`. Đặc biệt, State Pattern phối hợp cực kỳ ăn khớp với hiệu ứng dây chuyền khi vận hành chuyến bay: khi Admin hủy một chuyến bay, hệ thống kích hoạt `onFlightCancelled` trên từng đơn, tự động giải phóng ghế về `AVAILABLE` và hoàn tiền `REFUNDED` cho toàn bộ hành khách đã thanh toán mà không bỏ sót bất kỳ trường hợp biên nào."

---

#### Slide 9 — Strategy & Factory Pattern tuân thủ nguyên lý SOLID (Tiêu chí 8b — 10 điểm)
- **Layout (`data-layout`):** `split-diagram` (Cột trái tóm tắt nguyên lý SOLID `SRP`, `OCP`, `DIP`; Cột phải minh họa sơ đồ kết hợp `PaymentFactory` + `PaymentStrategy` và `SeatFactory`)
- **Title cột trái (Serif):** `Mở rộng phương thức thanh toán và cấu hình khoang ghế theo nguyên lý SOLID`
- **Điểm nhấn cột trái:**
  - **Open/Closed Principle (`OCP` — `ADR-0002`):** Thêm cổng thanh toán mới (ví dụ: `Crypto` hay `ApplePay`) chỉ cần thêm lớp cài đặt `PaymentStrategy`, không sửa đổi `Booking` hay `AirlineReservationSystem`.
  - **Single Responsibility (`SRP` — `ADR-0004`):** Tách logic khởi tạo sơ đồ khoang ghế ra `SeatFactory` và logic lắp ghép giao dịch ra `PaymentFactory`.
- **Sơ đồ cột phải (`Factory + Strategy Flow`):**
  - **Cụm 1 (`SeatFactory.createSeatsForAircraft`):**
    - Nhận `Aircraft(totalSeats)` → Tự động chia hàng 4 ghế (`A, B, C, D`): 20% ghế đầu là `BusinessSeat (2.0x, Lounge)`, hàng Economy kế tiếp là `EconomySeat (1.15x Extra Legroom)`, các hàng sau là `EconomySeat (1.0x)`.
  - **Cụm 2 (`PaymentFactory` + `PaymentStrategy`):**
    - `PaymentFactory` tạo cặp `Payment` + 1 trong 4 chiến lược: `CardPayment`, `EWalletPayment`, `BankTransferPayment`, `CashPayment` → Thực thi `Payment.process(strategy)`.
- **Callout cuối cột phải:** Tuân thủ trọn vẹn 5 nguyên lý **S · O · L · I · D** trong toàn bộ miền nghiệp vụ.
- **Speaker Notes:**
  > "Bên cạnh State Pattern, nhóm áp dụng thêm hai mẫu thiết kế là **Strategy Pattern** và **Factory Pattern**. Với `PaymentStrategy` (ADR-0002), bản ghi giao dịch `Payment` chỉ lưu trạng thái tiền tệ, còn thuật toán xác thực và trừ tiền được ủy quyền cho 4 chiến lược: Thẻ ngân hàng, Ví điện tử, Chuyển khoản và Tiền mặt — tuân thủ chặt chẽ nguyên lý Đóng-Mở (OCP) và Đảo ngược phụ thuộc (DIP). Với `SeatFactory` và `PaymentFactory`, trách nhiệm khởi tạo phức tạp (như tính toán tỷ lệ 20% ghế Thương gia, gán hàng ghế Extra Legroom 1.15x) được tách rời hoàn toàn khỏi thực thể `Flight`, giữ cho mỗi lớp chỉ đảm nhận đúng một trách nhiệm duy nhất theo nguyên lý SRP."

---

### PHẦN IV: GIAO DIỆN JAVAFX 25, KIỂM THỬ JUNIT 5 & LIVE DEMO (Slides 10–14)
*(Thời lượng gợi ý: ~4 phút + Live Demo)*

#### Slide 10 — Giao diện JavaFX 25 SPA: Trải nghiệm Đặt ghế & Thanh toán (Tiêu chí 10a)
- **Layout (`data-layout`):** `ui-showcase` (Nền `sage` có đường vân `contour`, hiển thị cửa sổ giao diện Hành khách và hộp thoại Sơ đồ khoang ghế 2-Lối đi-2)
- **Title (Serif 84px):** `Giao diện JavaFX 25 trực quan hóa sơ đồ khoang tàu bay và trạng thái ghế`
- **Các thành phần UI nổi bật trên Stage:**
  - **Màn hình `PassengerWorkspaceView`:**
    - Tìm kiếm chuyến bay nhanh kèm nút đảo chiều sân bay (`HAN ⇄ SGN`), bộ lọc ngày bay và số lượng ghế.
    - Bảng danh sách chuyến bay và bảng `My Bookings` cập nhật tức thì theo thời gian thực.
  - **Hộp thoại `SeatSelectionDialog` (Sơ đồ thân máy bay `2 - AISLE - 2`):**
    - Phân biệt màu sắc trực quan theo hạng ghế và trạng thái: `Business (2.0x)`, `Economy+ Extra Legroom (1.15x)`, `Economy (1.0x)`, ghế đang chọn, ghế `RESERVED` và ghế `OCCUPIED`.
    - Cho phép chọn cùng lúc nhiều ghế, tự động cộng dồn giá vé đa hình và chọn giữa **`Hold Seats (Pay Later)`** hoặc **`Reserve & Pay Now`**.
- **Speaker Notes:**
  > "Ở tầng giao diện đồ họa, nhóm hiện thực hóa một ứng dụng Single-Page Application (SPA) trên nền JavaFX 25 lấy cảm hứng từ hệ thống thiết kế Arbitrum. Khi hành khách chọn một chuyến bay, hộp thoại `SeatSelectionDialog` mô phỏng trực quan khoang máy bay cấu hình 2–Lối đi–2. Người dùng nhìn thấy ngay ghế nào thuộc hạng Thương gia 2.0x có phòng chờ, ghế nào là Economy+ 1.15x rộng chân, ghế nào đã có người giữ hoặc đã bán. Hành khách có thể chọn nhiều ghế cùng lúc, xem tổng tiền tính tự động theo đa hình, và chọn giữ chỗ trả sau hoặc thanh toán ngay."

---

#### Slide 11 — Bảng điều khiển Admin & Kịch bản Live Demo (Tiêu chí 10b — 10 điểm)
- **Layout (`data-layout`):** `ui-demo-blue` (Nền `sky` + đường vân `contour` dành riêng cho màn hình tương tác sản phẩm & kịch bản Live Demo)
- **Title (Serif 84px):** `Điều phối vận hành thời gian thực và kịch bản Live Demo 4 bước`
- **Nội dung hiển thị trên Stage:**
  - **Cửa sổ chính (`AdminWorkspaceView`):**
    - 4 thẻ KPI thời gian thực: **Total Flights**, **Total Bookings**, **Registered Passengers**, **Settled Revenue**.
    - Bộ nút điều khiển thông minh (`Smart Action Buttons`): **`Mark Boarding`**, **`Mark Departed`**, **`Mark Arrived`**, **`Cancel Flight`** tự động bật/tắt (enable/disable) dựa trên trạng thái hợp lệ tiếp theo của chuyến bay đang chọn.
  - **Thẻ nổi (`Floating Modal / Demo Script — 4 Bước Live Demo`):**
    1. **Quick Login & Register:** Double-click chip `PAX-02` (hoặc tạo tài khoản mới với mã `PAX-03` cấp tự động).
    2. **Seat Map & Hold:** Tìm chuyến `VN101 (HAN → SGN)`, chọn 1 ghế Business + 1 ghế Economy+, nhấn giữ chỗ `PENDING`.
    3. **Payment Retry & Check-in:** Bật `Simulate Payment Failure` để thấy đơn vẫn giữ ghế `PENDING`, sau đó thanh toán lại thành công (`CONFIRMED`) và bấm **`Check In`**.
    4. **Admin Cascade:** Chuyển sang tài khoản `ADM-01`, thử cho `VN101` hạ cánh (`ARRIVED` → đơn thành `COMPLETED`) và hủy chuyến `VN103` (`CANCELLED` → tự động hoàn tiền `REFUNDED`).
- **Speaker Notes:**
  > "Phía quản trị viên (`AdminWorkspaceView`) cung cấp bảng điều khiển vận hành với 4 chỉ số KPI doanh thu và lưu lượng theo thời gian thực, cùng cơ chế khóa nút thông minh chỉ mở những bước chuyển trạng thái hợp lệ. Ngay sau đây (hoặc ở cuối bài), nhóm sẽ thực hiện Live Demo 4 bước ngay trên ứng dụng đang chạy: từ đăng nhập nhanh bằng chip mẫu, chọn ghế đa hạng, mô phỏng lỗi thanh toán rồi thanh toán lại và check-in, cho đến màn hình Admin kích hoạt hạ cánh và hủy chuyến để chứng minh hiệu ứng dây chuyền hoàn tiền tự động."

---

#### Slide 12 — Kiểm thử Tự động JUnit 5 & Chất lượng Mã nguồn (Tiêu chí 9 — 10 điểm)
- **Layout (`data-layout`):** `big-number` (Con số lớn `85` bằng font serif 300px bên trái trên nền stage + Danh sách 4 cụm kết quả kiểm thử `ad-row` bên phải)
- **Title (Serif 84px):** `85 bài kiểm thử JUnit 5 bảo vệ toàn bộ nghiệp vụ và giao diện`
- **Con số trung tâm (Big Numeral):** `85`
  - **Caption dưới số:** `Unit & Integration Tests · 18 Test Classes · 0 Failures · Java 25`
- **4 Hàng kết quả chi tiết bên phải (`ad-row`):**
  1. **Domain & Entities (`32 tests`):** `FlightTest`, `BookingTest`, `SeatTest`, `SeatFactoryTest`, `PaymentTest`, `PassengerTest`, `AdminTest`, `AirportTest`, `AircraftTest` — Kiểm chứng đa hình giá vé, cô lập ghế và máy trạng thái `BookingState`.
  2. **System Orchestration & Cascades (`22 tests`):** 6 lớp `AirlineReservationSystem*Test` & `DemoDataSeederTest` — Kiểm chứng chống trùng khóa, tìm chuyến, giữ chỗ, retry thanh toán, cascade hủy chuyến & hạ cánh.
  3. **Payment & Exceptions (`7 tests`):** `PaymentStrategyTest`, `PaymentFactoryTest`, `DomainExceptionTest` — Xác minh 4 chiến lược thanh toán và 6 lớp ngoại lệ biên qua `assertThrows`.
  4. **Headless GUI & Theme (`24 tests`):** `ReservationGuiControllerTest` (20 kịch bản UI đầu-cuối) & `ArbitrumThemeTest` (3 bài kiểm tra parser CSS JavaFX) + `MainTest`.
- **Speaker Notes:**
  > "Để chứng minh chất lượng mã nguồn ở tiêu chí số 9, nhóm áp dụng phương pháp phát triển hướng kiểm thử (TDD) với JUnit 5. Từ 52 bài test ở Milestone 4, bộ kiểm thử hiện tại đã đạt **85 bài kiểm thử tự động** trải đều trên 18 lớp test với tỷ lệ vượt qua 100%. Không chỉ kiểm thử các thực thể miền và các ca biên bằng `assertThrows`, nhóm còn viết 20 bài kiểm thử cho toàn bộ luồng điều khiển giao diện `ReservationGuiControllerTest` và dùng chính `CssParser` của JavaFX trong `ArbitrumThemeTest` để đảm bảo bảng kiểu giao diện không có bất kỳ lỗi cú pháp hay cảnh báo nào."

---

#### Slide 13 — Đối chiếu Sản phẩm với 10 Tiêu chí Đề bài
- **Layout (`data-layout`):** `pill-stack` (3 pill tóm tắt nhóm hạng mục kết hợp bảng bản đồ minh chứng theo 10 tiêu chí tham khảo của học phần để giảng viên tiện kiểm chứng)
- **Title (Serif 84px):** `Đối chiếu sản phẩm với 10 tiêu chí đề bài`
- **3 Nhóm hạng mục tổng kết (3 Pill Cards bên trái):**
  - **Nhóm 1 — Thiết kế & Mô hình OOP (TC 1–4):**
    - Đề tài 21 nâng cao; sơ đồ UML 38 kiểu; đóng gói bất biến với `List.copyOf`; quan hệ Composition `Flight–Seat` (`ADR-0001`).
  - **Nhóm 2 — Đa hình, Cấu trúc & SOLID (TC 5–8):**
    - Đa hình `Person`, `Seat`, `PaymentStrategy`; tra cứu $O(1)$ với `LinkedHashMap`; 6 Unchecked Exceptions (`ADR-0003`); 3 Design Patterns **State, Strategy, Factory** theo **SOLID**.
  - **Nhóm 3 — Kiểm thử & Trình diễn UI (TC 9–10):**
    - Giao diện **JavaFX 25 SPA** theo kiến trúc **Passive View** (`ADR-0005/0006`); kịch bản Console & GUI đồng nhất; **85 bài kiểm thử JUnit 5** không lỗi.
- **Speaker Notes:**
  > "Trước khi bước vào phần hỏi đáp, Slide 13 tổng hợp bản đồ đối chiếu giữa các thành phần nhóm đã triển khai với 10 tiêu chí tham khảo của học phần để thầy tiện theo dõi và đánh giá: từ phân tích bài toán, UML, đóng gói, đa hình, cấu trúc dữ liệu, các mẫu thiết kế SOLID cho tới bộ 85 bài kiểm thử JUnit 5 và giao diện JavaFX 25 vừa trình diễn. Toàn bộ mã nguồn, 6 tài liệu ADR và bộ test đều có thể chạy kiểm chứng trực tiếp qua Maven; nhóm rất mong nhận được nhận xét và góp ý của thầy để hoàn thiện thêm."

---

#### Slide 14 — Lời cảm ơn & Hỏi đáp (Closing)
- **Layout (`data-layout`):** `thanks` (Nền `sage` đồng bộ với trang bìa `sage-soft`, biểu tượng nét vẽ tay tối giản ở giữa và dòng chữ lớn `Thank you` serif 176px theo đúng chuẩn `DESIGN.md §16.1`)
- **Text hiển thị:** `Thank you`
- **Speaker Notes:**
  > "Cảm ơn thầy và các bạn đã lắng nghe phần trình bày của Nhóm 4. Nhóm rất mong nhận được các câu hỏi, nhận xét và góp ý từ thầy và các bạn!"

---

## Phụ lục: Các lệnh chạy nhanh phục vụ buổi bảo vệ

1. **Khởi chạy giao diện JavaFX 25 (Live Demo):**
   ```powershell
   .\mvnw.cmd javafx:run
   ```
2. **Chạy kịch bản Console Smoke Run (6 luồng nghiệp vụ tự động):**
   ```powershell
   .\mvnw.cmd compile exec:java -Dexec.mainClass="com.airlinereservation.Main"
   ```
3. **Chạy toàn bộ 85 bài kiểm thử JUnit 5 trước mặt giảng viên:**
   ```powershell
   .\mvnw.cmd test
   ```
