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
| Người 4 — `Nguyễn Phước Hoài Nhật`[@nhatnphde180216-design](https://github.com/nhatnphde180216-design) | `DE180216` | Phòng chiếu, suất chiếu, đặt vé, quản lý đơn, lịch sử đơn, báo cáo doanh thu |

## Đóng góp nhóm

- **Quy trình làm việc**: Nhánh `main` là bản phát hành, `develop` là nhánh phát triển, các tính năng và tài liệu được phát triển trên các nhánh `feature/*` và `docs/*`. Mỗi PR yêu cầu ít nhất 1 reviewer và phải vượt qua kiểm tra CI (`./mvnw verify`).
- **Tiêu chuẩn commit**: Sử dụng Conventional Commits (ví dụ `feat(controller): add movie detail endpoint`).
- **Kiểm tra**: Tất cả các unit test và integration test phải chạy thành công (`./mvnw test`).
- **Release**: Khi tính năng ổn định, tạo Pull Request tới `release/*`, sau khi merge sẽ tự động tạo tag phiên bản và đóng gói artifact.


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

## Ứng dụng Desktop (Assignment 02)

FU Cinema Desktop là ứng dụng quầy vé JavaFX tích hợp Spring Boot, tái sử dụng service, repository và entity của phần web. Hai ứng dụng dùng chung cơ sở dữ liệu `FUCinemaTicketDB` trên SQL Server. Chỉ tài khoản **ADMIN** đăng nhập được vào desktop.

### Yêu cầu

- JDK 21, Microsoft SQL Server và file `application-local.properties` đã cấu hình như phần web ở trên.
- JavaFX 21.0.5 được khai báo trong `pom.xml`, không cần cài thêm.

### Cách chạy web và desktop

Hai ứng dụng có hai entry point riêng, chạy trong hai cửa sổ terminal:

```bash
# Web (http://localhost:8080)
./mvnw spring-boot:run

# Desktop (không khởi động Tomcat)
./mvnw spring-boot:run "-Dspring-boot.run.main-class=com.fu.cinema.desktop.DesktopLauncher"
```

Trên Windows dùng `mvnw.cmd`. Trong IntelliJ có thể chạy trực tiếp lớp `com.fu.cinema.desktop.DesktopLauncher`.

### Tài khoản đăng nhập desktop

| Vai trò | Email | Mật khẩu | Ghi chú |
|---|---|---|---|
| Admin | `admin@fucinema.vn` | `Admin@123` | Vào được desktop |
| Customer | `an.nv@gmail.com` | `Customer@123` | Bị từ chối khi đăng nhập desktop |

### Phân công phần Desktop

| TV | Họ tên / MSSV | TODO |
|---|---|---|
| A (Leader) | Phạm Lê Anh Tài — `DE190702` | 33, 34, 36, 46 |
| B | Hoàng Khánh Hòa — `DE190162` | 35, 43, 44, 45 |
| C | Nguyễn Phước Hoài Nhật — `DE180216` | 38, 39, 40 |
| D | Cao Đức Tiến — `DE190608` | 37, 41, 42 |

### Kiểm thử tự động

Test tích hợp dùng `@SpringBootTest`, `@ActiveProfiles("test")` và H2;
không cần SQL Server cho bộ test này. Không khởi tạo JavaFX Stage/Scene trong
constructor Spring bean để tránh lỗi toolkit khi chạy context test.

Ba trường hợp trong `DesktopFacadeTest`:

1. `adminWithCorrectPassword_canLoginDesktop`.
2. `adminWithWrongPassword_cannotLoginDesktop`.
3. `customerAccount_cannotLoginDesktop`.

**Kết quả build do nhóm cung cấp ngày 07/11/2026 lúc 22:33:03 (UTC+7):**

```text
Tests run: 26, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Total time: 53.930 s
```

- Kết quả trên thuộc lần chạy đã cung cấp, không tự xác nhận các commit sau đó.
- Cần bổ sung SHA commit của bản kiểm tra và ảnh/log riêng của `DesktopFacadeTest`.
- 26 test tự động đạt **không đồng nghĩa** mọi kịch bản JavaFX/SQL Server bên dưới đã đạt.

### Ảnh giao diện desktop

Ảnh chụp từ bản chạy thật của nhóm, lưu trong thư mục `docs/images/desktop/`.

#### Đăng nhập
![Màn hình đăng nhập desktop](docs/images/desktop/login.png)

#### Dashboard
![Dashboard desktop](docs/images/desktop/dashboard.png)

#### Quản lý phim
![Quản lý phim desktop](docs/images/desktop/movie.png)

#### Phòng và suất chiếu
![Phòng và suất chiếu desktop](docs/images/desktop/schedule.png)

#### Bán vé tại quầy
![Bán vé tại quầy](docs/images/desktop/counter-sale.png)

#### Báo cáo doanh thu
![Báo cáo doanh thu desktop](docs/images/desktop/report.png)

### Kết quả kiểm thử TC-D01–TC-D20

Chuẩn bị dữ liệu thử nghiệm và chạy web–desktop đồng thời trên SQL Server.
Điền **Pass / Fail / Blocked**, người kiểm tra, ngày và ảnh/log cho từng dòng sau khi chạy.
“Chưa ghi nhận” nghĩa là chưa có kết quả cung cấp, không khẳng định nhóm chưa từng chạy.

| TC | Nội dung / kết quả mong đợi | Trạng thái | Người / ngày / minh chứng |
| TC-D01 | Desktop mở login, không có Tomcat; web vẫn truy cập được | Chưa ghi nhận | Cần bổ sung |
| TC-D02 | ADMIN nhập sai mật khẩu → thông báo sai thông tin | Chưa ghi nhận | Cần bổ sung |
| TC-D03 | CUSTOMER đăng nhập → bị từ chối | Chưa ghi nhận | Cần bổ sung |
| TC-D04 | ADMIN đúng → vào main, status bar có tên; dashboard khớp web | Chưa ghi nhận | Cần bổ sung |
| TC-D05 | Web tạo suất hôm nay → refresh dashboard thấy thay đổi | Chưa ghi nhận | Cần bổ sung |
| TC-D06 | Thêm, sửa, xóa thể loại thử nghiệm → bảng cập nhật | Chưa ghi nhận | Cần bổ sung |
| TC-D07 | Xóa thể loại còn phim → lỗi BR-05, dữ liệu còn nguyên | Chưa ghi nhận | Cần bổ sung |
| TC-D08 | Lọc phim kết hợp và phân trang đúng; đổi lọc về trang đầu | Chưa ghi nhận | Cần bổ sung |
| TC-D09 | Phim 20 phút → dialog không đóng, ô lỗi có thông báo | Chưa ghi nhận | Cần bổ sung |
| TC-D10 | Xóa phim có suất → STOPPED, dòng tô xám | Chưa ghi nhận | Cần bổ sung |
| TC-D11 | Tạo suất trùng phòng/giờ → báo rõ suất xung đột | Chưa ghi nhận | Cần bổ sung |
| TC-D12 | Phim 120 phút, bắt đầu 19:00 → gợi ý kết thúc 21:15 | Chưa ghi nhận | Cần bổ sung |
| TC-D13 | Bán 3 ghế, đã thanh toán → tổng đúng, CONFIRMED, ghế đã bán | Chưa ghi nhận | Cần bổ sung |
| TC-D14 | Chọn ghế thứ 9 → bỏ chọn và cảnh báo tối đa 8 | Chưa ghi nhận | Cần bổ sung |
| TC-D15 | Khách chưa đủ tuổi mua phim T18 → lỗi BR-11 | Chưa ghi nhận | Cần bổ sung |
| TC-D16 | Web và desktop tranh chấp cùng ghế → chỉ một bên thành công, DB không trùng | Chưa ghi nhận | Cần bổ sung |
| TC-D17 | Xác nhận PENDING, hủy CONFIRMED → trạng thái đúng | Chưa ghi nhận | Cần bổ sung |
| TC-D18 | Đơn CANCELLED → nút xác nhận/hủy bị disable | Chưa ghi nhận | Cần bổ sung |
| TC-D19 | Báo cáo khớp web; CSV mở Excel đúng tiếng Việt | Chưa ghi nhận | Cần bổ sung |
| TC-D20 | Đăng xuất về login; đóng desktop kết thúc tiến trình | Chưa ghi nhận | Cần bổ sung |

### Release v2.0.0 — checklist trước phát hành

**Trạng thái trong tài liệu này: chưa xác nhận đã phát hành.**
JAR trong log build được cung cấp vẫn là phiên bản `0.0.1-SNAPSHOT`.
Không ghi release hoàn tất chỉ dựa trên build thành công.

- [ ] Tất cả module desktop đã tích hợp, PR review và merge theo quy trình.
- [ ] TC-D01–TC-D20 có kết quả và minh chứng; xử lý các lỗi còn tồn tại.
- [ ] Có ít nhất 3 test Facade; toàn bộ `clean verify` đạt.
- [ ] README có ít nhất 6 ảnh thật, bảng phân công, URL PR và hướng dẫn chạy đã kiểm tra.
- [ ] Người khác clone repository và chạy được web/desktop theo README.
- [ ] D14 được C và D review.
- [ ] Tạo nhánh `release/v2.0.0` từ develop đã tích hợp.
- [ ] Cập nhật phiên bản `pom.xml` thành `2.0.0`; chạy lại `clean verify`.
- [ ] PR release vào main có 2 approvals và được merge.
- [ ] Tạo tag `v2.0.0` trên đúng commit main đã kiểm tra, tạo GitHub Release có mô tả.
- [ ] Back-merge main về develop.

URL release và SHA kiểm tra: **Cần bổ sung sau khi phát hành**.

### Lỗi desktop thường gặp

| Hiện tượng | Cách kiểm tra |
|---|---|
| IntelliJ reload báo Guice / ArtifactHandlerManager | Kiểm tra Maven importer; trường hợp máy nhóm đã xử lý bằng Bundled Maven 3, importer/runner dùng JDK 21 |
| Không tìm thấy FXML/CSS | Kiểm tra `View.path()`, `/desktop/fxml/`, `/desktop/css/desktop.css` và tài nguyên đã được copy khi build |
| `package ...desktop.util/facade does not exist` | Kiểm tra D2/D3/D4 đã tích hợp vào nhánh hiện tại; fetch không tự merge code |
| File test sai tên / duplicate DesktopFacadeTest | Chỉ giữ một `DesktopFacadeTest.java` trong src/test; không đặt test trong file AuthFacade.java |
| `A bound value cannot be set` | Không để FxAsync thay đổi disable của nút đã bind; khóa form hoặc container phù hợp |
| `Toolkit not initialized` khi test | Không tạo Stage/Scene hoặc gọi UI trong constructor bean; chuẩn bị kiểm thử session phù hợp với quy định FX thread |
| `LazyInitializationException` | Ánh xạ entity → record trong transaction tại Facade, không đưa entity lên controller |
| Giao diện bị treo | Kiểm tra lời gọi Facade chạy qua FxAsync/Task, không truy vấn DB trên FX thread |
| Facade ghi không lưu dữ liệu | Kiểm tra transaction ghi không tham gia transaction read-only; kiểm tra quyền ADMIN |


## Lỗi thường gặp

| Hiện tượng | Cách xử lý |
|---|---|
| `Login failed for user 'sa'` hoặc không kết nối được cổng 1433 | Kiểm tra mật khẩu trong `application-local.properties`; bật SQL Server Authentication và TCP/IP trong SQL Server Configuration Manager |
| `The driver could not establish a secure connection ... (SSL)` | URL kết nối cần có `encrypt=true;trustServerCertificate=true` (đã có trong file mẫu) |
| Không thấy dữ liệu mẫu | Dữ liệu chỉ được tạo khi bảng tài khoản trống; xoá và tạo lại database để seed lại |
