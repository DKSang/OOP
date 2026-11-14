# Airline Reservation System (Đề tài 21)

Hệ thống đặt vé máy bay quản lý lịch trình chuyến bay, tàu bay, sơ đồ ghế, hồ sơ hành khách, quy trình đặt vé và thanh toán.

## Language

### Hạ tầng & Lịch trình bay

**Airport**:
Một sân bay chuẩn hóa (mã sân bay, tên sân bay, thành phố) đóng vai trò là điểm xuất phát (`departureAirport`) hoặc điểm hạ cánh (`arrivalAirport`) của một `Flight`.
_Avoid_: Station, Location, Terminal

**Aircraft**:
Một tàu bay vật lý cụ thể mà hãng hàng không quản lý (mã tàu bay, mẫu máy bay, nhà sản xuất và sức chứa thiết kế), tồn tại độc lập và có thể được gán cho nhiều `Flight` khác nhau.
_Avoid_: Plane, Flight

**Flight**:
Một chuyến bay cụ thể được lên lịch khai thác trên một chặng bay tại thời điểm xác định bằng một `Aircraft`, có mức giá cơ sở (`basePrice`), vòng đời vận hành riêng (`FlightStatus`) và sở hữu sơ đồ `Seat` mở bán riêng biệt.
_Avoid_: Route, Trip, Airplane

**Seat**:
Một chỗ ngồi mở bán gắn với một `Flight` cụ thể (được khởi tạo theo cấu hình của `Aircraft`), mang số hiệu ghế (`seatNumber`), trạng thái khả dụng (`SeatStatus`) và quy tắc tính giá theo hạng ghế.
_Avoid_: PhysicalSeat, Chair

**EconomySeat**:
Ghế hạng phổ thông trên một `Flight`, tính giá vé theo giá cơ sở (`basePrice`) tiêu chuẩn hoặc cộng phụ phí khi có chỗ để chân rộng (`hasExtraLegroom`).
_Avoid_: NormalSeat, StandardSeat

**BusinessSeat**:
Ghế hạng thương gia trên một `Flight`, áp dụng hệ số nhân (`priceMultiplier`) trên giá cơ sở (`basePrice`) kèm quyền lợi phòng chờ (`hasLoungeAccess`).
_Avoid_: VIPSeat, FirstClassSeat

**SeatFactory**:
Bộ khởi tạo chịu trách nhiệm tạo các đối tượng `Seat` (`EconomySeat`, `BusinessSeat`) và dựng sơ đồ ghế mở bán tiêu chuẩn cho từng `Flight` theo sức chứa của `Aircraft`.
_Avoid_: SeatBuilder, CabinGenerator

### Người dùng & Đặt chỗ

**Person**:
Khái niệm trừu tượng đại diện cho một cá nhân tương tác với hệ thống, nắm giữ thông tin định danh, liên lạc cơ bản (họ tên, email, số điện thoại) và thông tin xác thực mật khẩu để đăng nhập theo vai trò.
_Avoid_: Account, Human

**Passenger**:
Hành khách sử dụng hệ thống để đăng nhập, tìm chuyến bay, thực hiện hoặc yêu cầu hủy đặt vé (`Booking`) và trực tiếp tham gia chuyến bay (bao gồm thông tin ngày sinh, giới tính, hộ chiếu/CCCD và lịch sử đặt chỗ).
_Avoid_: Customer, Traveler, Client

**Admin**:
Quản trị viên hệ thống có quyền đăng nhập vào giao diện điều hành để quản lý danh mục tàu bay (`Aircraft`), chuyến bay (`Flight`) và giám sát toàn bộ đặt chỗ (`Booking`) cùng hành khách (`Passenger`).
_Avoid_: Manager, Staff, Operator

**Booking**:
Một đơn đặt chỗ của `Passenger` cho một `Flight` cụ thể với danh sách `Seat` đã chọn, tổng chi phí (`totalPrice`) và vòng đời trạng thái riêng (`BookingStatus`).
_Avoid_: Order, Reservation, Ticket

**BookingState**:
Giao diện đại diện cho một trạng thái cụ thể trong vòng đời của `Booking` (`PendingBookingState`, `ConfirmedBookingState`, `CheckedInBookingState`, `CompletedBookingState`, `CancelledBookingState`), đóng gói các quy tắc chuyển trạng thái hợp lệ và phản ứng trước sự kiện của `Flight`.
_Avoid_: BookingPhase, ReservationLifecycle

### Thanh toán & Điều phối

**Payment**:
Bản ghi giao dịch thanh toán gắn liền với một `Booking`, lưu trữ mã giao dịch, số tiền, phương thức (`PaymentMethod`), thời điểm và trạng thái kết quả (`PaymentStatus`).
_Avoid_: Invoice, Bill, Receipt

**PaymentStrategy**:
Giao diện trừu tượng định nghĩa hành vi xử lý thanh toán cho một giao dịch, được hiện thực hóa bởi từng phương thức thanh toán cụ thể (`CardPayment`, `EWalletPayment`, `BankTransferPayment`, `CashPayment`).
_Avoid_: PaymentProcessor, BillingGateway

**PaymentFactory**:
Bộ khởi tạo chịu trách nhiệm tạo bản ghi giao dịch `Payment` và đối tượng `PaymentStrategy` tương ứng với `PaymentMethod`, đảm bảo tính nhất quán giữa phương thức khai báo và chiến lược thực thi thanh toán.
_Avoid_: BillingCreator, TransactionBuilder

**AirlineReservationSystem**:
Bộ điều phối trung tâm của hệ thống, quản lý danh mục `Flight`, `Aircraft`, `Airport`, `Passenger`, `Admin`, `Booking` và điều phối các luồng nghiệp vụ xác thực người dùng, tìm kiếm chuyến bay, tạo đặt chỗ, thanh toán và hủy đặt chỗ.
_Avoid_: FlightManager, SeatManager, BookingManager

### Giao diện & Điều phối Hiển thị (GUI)

**ReservationGuiController**:
Bộ điều phối giao diện người dùng đóng vai trò cầu nối giữa các màn hình hiển thị và `AirlineReservationSystem`, quản lý phiên đăng nhập hiện hành (`Person`) và chuyển đổi dữ liệu nghiệp vụ phục vụ hiển thị mà không chứa thành phần đồ họa cụ thể.
_Avoid_: GuiManager, FormHelper, SwingService

**DemoDataSeeder**:
Bộ khởi tạo dữ liệu mẫu tiêu chuẩn cho hệ thống (`Airport`, `Aircraft`, `Flight`, `Passenger`, `Admin`, `Booking`), phục vụ khởi chạy nhanh và khôi phục trạng thái trình diễn trên giao diện.
_Avoid_: MockDatabase, DummyLoader
