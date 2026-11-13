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

### Người dùng & Đặt chỗ

**Person**:
Khái niệm trừu tượng đại diện cho một cá nhân tương tác với hệ thống, nắm giữ thông tin định danh và liên lạc cơ bản (họ tên, email, số điện thoại).
_Avoid_: Account, Human

**Passenger**:
Hành khách sử dụng hệ thống để tìm chuyến bay, thực hiện hoặc yêu cầu hủy đặt vé (`Booking`) và trực tiếp tham gia chuyến bay (bao gồm thông tin ngày sinh, giới tính, hộ chiếu/CCCD và lịch sử đặt chỗ).
_Avoid_: Customer, Traveler, Client

**Admin**:
Quản trị viên hệ thống có quyền quản lý danh mục tàu bay (`Aircraft`), chuyến bay (`Flight`) và giám sát vận hành.
_Avoid_: Manager, Staff, Operator

**Booking**:
Một đơn đặt chỗ của `Passenger` cho một `Flight` cụ thể với danh sách `Seat` đã chọn, tổng chi phí (`totalPrice`) và vòng đời trạng thái riêng (`BookingStatus`).
_Avoid_: Order, Reservation, Ticket

### Thanh toán & Điều phối

**Payment**:
Bản ghi giao dịch thanh toán gắn liền với một `Booking`, lưu trữ mã giao dịch, số tiền, phương thức (`PaymentMethod`), thời điểm và trạng thái kết quả (`PaymentStatus`).
_Avoid_: Invoice, Bill, Receipt

**PaymentStrategy**:
Giao diện trừu tượng định nghĩa hành vi xử lý thanh toán cho một giao dịch, được hiện thực hóa bởi từng phương thức thanh toán cụ thể (`CardPayment`, `EWalletPayment`, `BankTransferPayment`, `CashPayment`).
_Avoid_: PaymentProcessor, BillingGateway

**AirlineReservationSystem**:
Bộ điều phối trung tâm của hệ thống, quản lý danh mục `Flight`, `Aircraft`, `Airport`, `Passenger`, `Booking` và điều phối các luồng nghiệp vụ tìm kiếm chuyến bay, tạo đặt chỗ và hủy đặt chỗ.
_Avoid_: FlightManager, SeatManager, BookingManager
