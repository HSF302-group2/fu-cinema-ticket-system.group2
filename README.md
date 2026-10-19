# FU Cinema Ticket System

Website bán vé xem phim: khách hàng xem phim, chọn suất chiếu, chọn ghế và đặt vé; quản trị viên quản lý khách hàng, phim, phòng chiếu, suất chiếu, đơn đặt vé và xem báo cáo doanh thu.

Bài tập nhóm môn **HSF302**.

## Công nghệ

| Thành phần | Phiên bản / ghi chú |
|---|---|
| Ngôn ngữ | Java 21 |
| Framework | Spring Boot 4.1.1 (Spring MVC, Spring Data JPA/Hibernate, Bean Validation) |
| Giao diện | Thymeleaf + Bootstrap 5.3 |
| Cơ sở dữ liệu | Microsoft SQL Server (chạy ứng dụng), H2 in-memory (chạy test) |
| Mã hoá mật khẩu | BCrypt (`spring-security-crypto`) |
| Khác | Lombok, Maven Wrapper (`mvnw`) |

## Yêu cầu cài đặt

- JDK 21
- Microsoft SQL Server (bật SQL Server Authentication và TCP/IP, cổng 1433)
- Không cần cài Maven: dùng `mvnw` / `mvnw.cmd` đi kèm repo

## Cài đặt & chạy

1. **Clone** repo:
   ```bash
   git clone https://github.com/planhtai2005-web/fu-cinema-ticket-system.git
   cd fu-cinema-ticket-system
   ```
2. **Tạo database** trong SQL Server:
   ```sql
   CREATE DATABASE FUCinemaTicketDB;
   ```
3. **Tạo file cấu hình cục bộ** từ file mẫu rồi điền tài khoản SQL Server của bạn:
   ```bash
   # Linux / macOS / Git Bash
   cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties

   # Windows (cmd)
   copy src\main\resources\application-local.properties.example src\main\resources\application-local.properties
   ```
   Sửa `spring.datasource.username` và `spring.datasource.password` trong `application-local.properties`.
   File này **không được commit** (đã nằm trong `.gitignore`).
4. **Chạy ứng dụng**:
   ```bash
   ./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
   ```
5. Mở http://localhost:8080
6. Lần chạy đầu tiên (khi bảng tài khoản còn trống), **dữ liệu mẫu được tạo tự động**. Các lần sau dữ liệu được giữ nguyên.

## Tài khoản mẫu

| Vai trò | Email | Mật khẩu | Ghi chú |
|---|---|---|---|
| Admin | `admin@fucinema.vn` | `Admin@123` | Vào được khu vực `/admin` |
| Customer | `an.nv@gmail.com` | `Customer@123` | Khách hàng đủ tuổi |
| Customer (15 tuổi) | `binh.tt@gmail.com` | `Customer@123` | Dùng để thử giới hạn độ tuổi phim |

Đây là tài khoản demo trong dữ liệu mẫu, không dùng cho môi trường thật.

## Dữ liệu mẫu

- 1 tài khoản Admin và 2 khách hàng.
- 5 thể loại phim.
- 6 phim: 3 đang chiếu (`SHOWING`), 2 sắp chiếu (`COMING_SOON`), 1 đã ngừng (`STOPPED`).
- 3 phòng chiếu: Room 01 (2D), Room 02 (3D) đang hoạt động; Room 03 (IMAX) đang bảo trì.
- Các suất chiếu (quá khứ và sắp tới, tính theo ngày hiện tại) và một số đơn đặt vé mẫu để thử báo cáo.

## Chạy test

```bash
./mvnw verify          # Windows: mvnw.cmd verify
```

Test dùng profile `test` với **H2 in-memory** (`src/test/resources/application-test.properties`), nên chạy được trên mọi máy mà không cần SQL Server. Test khởi động toàn bộ ứng dụng để phát hiện sớm lỗi ánh xạ entity, sai tên truy vấn và sai cú pháp JPQL.

## Chức năng

**Khách (chưa đăng nhập):** xem danh sách phim, tìm kiếm/lọc theo thể loại, xem chi tiết phim và lịch chiếu, đăng ký, đăng nhập.

**Customer:** chọn suất chiếu và ghế, đặt vé; xem lịch sử đặt vé và huỷ vé theo quy định; cập nhật hồ sơ, đổi mật khẩu; (bonus) đánh giá phim đã xem.

**Admin:** dashboard; quản lý khách hàng, thể loại, phim, phòng chiếu, suất chiếu (kiểm tra trùng lịch); quản lý đơn đặt vé (xác nhận, huỷ); báo cáo doanh thu theo khoảng ngày.

**Phân quyền:** `/admin/**` chỉ dành cho Admin, `/customer/**` yêu cầu đăng nhập Customer. Truy cập sai quyền hiển thị trang 403.

## Cấu trúc mã nguồn

```
src/main/java/com/fu/cinema
├── config        # cấu hình chung: mã hoá mật khẩu, dữ liệu mẫu, interceptor phân quyền
├── controller    # tầng web (admin/, customer/): chỉ gọi service, không gọi repository
├── dto           # form, request và kết quả truy vấn
├── entity        # ánh xạ JPA
├── enums         # Role, trạng thái, loại phòng, độ tuổi...
├── exception     # BusinessException, ResourceNotFoundException, GlobalExceptionHandler
├── repository    # Spring Data JPA
├── service       # interface + impl (impl/): toàn bộ quy tắc nghiệp vụ
└── util          # tiện ích dùng chung
src/main/resources
├── templates     # Thymeleaf (fragments/, admin/, customer/, auth/, error/)
└── static        # CSS
src/test          # test khởi động ứng dụng với H2
```

## Thành viên & phân công

| Thành viên | MSSV | Chức năng |
|---|---|---|
| Người 1 (Leader) — `Phạm Lê Anh Tài` [@planhtai2005-web](https://github.com/planhtai2005-web) | `DE190702` | Khởi tạo project, entity, repository, dữ liệu mẫu, service layer, xử lý lỗi, đăng nhập/đăng ký/phân quyền, README & release |
| Người 2 — `Cao Đức Tiến`[@CaoDucTien-web](https://github.com/CaoDucTien) | `DE190608` | Quản lý khách hàng, hồ sơ cá nhân và đổi mật khẩu |
| Người 3 — `Hoàng Khánh Hòa`[@hoangkhanhhoa1976vn-web](https://github.com/hoangkhanhhoa1976vn-de) | `DE190162` | Thể loại, phim, trang chủ & chi tiết phim, đánh giá (bonus) |
| Người 4 — `Nguyễn Phước Hoài Nhật`[@nhatnphde180216-web](https://github.com/nhatnphde180216-design) | `DE180216` | Phòng chiếu, suất chiếu, đặt vé, quản lý đơn, lịch sử đơn, báo cáo doanh thu |

## Quy trình làm việc nhóm

- Nhánh: `main` (bản phát hành) ← `release/*` ← `develop` ← `feature/*`, `docs/*`, `fix/*`.
- Mỗi issue một nhánh và một Pull Request; mỗi PR cần ít nhất 1 người review trước khi merge.
- Commit theo [Conventional Commits](https://www.conventionalcommits.org/) (`feat(booking): ...`, `fix(auth): ...`) và ghi `Refs #n` / `Closes #n`.
- Chạy `./mvnw verify` thành công trước khi mở PR.

## Ảnh màn hình

<!--
Sau khi chụp ảnh, lưu vào docs/images/ rồi bỏ dấu chú thích khối này:

![Trang chủ](docs/images/home.png)
![Chi tiết phim](docs/images/movie-detail.png)
![Chọn ghế](docs/images/seats.png)
![Lịch sử đặt vé](docs/images/my-bookings.png)
![Admin - Dashboard](docs/images/admin-dashboard.png)
![Admin - Báo cáo doanh thu](docs/images/admin-report.png)
-->

## Lỗi thường gặp

| Hiện tượng | Cách xử lý |
|---|---|
| `Login failed for user 'sa'` hoặc không kết nối được cổng 1433 | Kiểm tra mật khẩu trong `application-local.properties`; bật SQL Server Authentication và TCP/IP trong SQL Server Configuration Manager |
| `The driver could not establish a secure connection ... (SSL)` | URL kết nối cần có `encrypt=true;trustServerCertificate=true` (đã có trong file mẫu) |
| Không thấy dữ liệu mẫu | Dữ liệu chỉ được tạo khi bảng tài khoản trống; xoá và tạo lại database để seed lại |
