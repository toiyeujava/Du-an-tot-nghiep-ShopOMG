# Flow Hoạt Động - ShopOMG E-commerce

## 1. Tổng Quan Kiến Trúc

```mermaid
graph TB
    subgraph "CLIENT (Browser)"
        A[Thymeleaf Templates]
        B[Static CSS/JS]
        C[WebSocket Client]
    end

    subgraph "CONTROLLER LAYER"
        D[User Controllers]
        E[Admin Controllers]
        F[Common Controllers]
    end

    subgraph "SERVICE LAYER"
        G[Business Services]
        H[Email/File Services]
        I[Security Services]
    end

    subgraph "DATA LAYER"
        J[JPA Repositories]
        K[SQL Server DB]
    end

    A --> D & E & F
    B --> A
    C --> F
    D & E & F --> G & H & I
    G & H & I --> J
    J --> K
```

---

## 2. Request-Response Flow Chung

```mermaid
sequenceDiagram
    participant B as Browser
    participant SF as SecurityFilterChain
    participant C as Controller
    participant S as Service
    participant R as Repository
    participant DB as SQL Server

    B->>SF: HTTP Request
    SF->>SF: Authentication Check
    SF->>SF: Authorization Check (ROLE_ADMIN/USER)
    
    alt Unauthorized
        SF-->>B: Redirect → /login
    else Authorized
        SF->>C: Pass Request
        C->>S: Call Business Logic
        S->>R: Data Access
        R->>DB: SQL Query
        DB-->>R: ResultSet
        R-->>S: Entity/DTO
        S-->>C: Processed Data
        C-->>B: Thymeleaf View + Model
    end
```

---

## 3. Flow Theo Từng Module

### 3.1 🏠 Trang Chủ / Duyệt Sản Phẩm

```mermaid
flowchart LR
    A["User truy cập /home"] --> B["HomeController.index()"]
    B --> C["ProductRepository.filterProducts()"]
    C --> D["CategoryRepository.getCategoryCounts()"]
    D --> E["Render user/home.html"]
    
    F["User truy cập /products"] --> G["ShopController.products()"]
    G --> H["ProductRepository.filterProducts()"]
    H --> I["Render user/product-list.html"]
    
    J["User xem chi tiết /products/{id}"] --> K["ShopController.productDetail()"]
    K --> L["ProductRepository.findById()"]
    L --> M["ProductVariantRepository"]
    M --> N["Render user/product-detail.html"]
```

### 3.2 🔐 Đăng Ký / Đăng Nhập / OAuth2

```mermaid
flowchart TD
    A["Guest"] --> B{"Phương thức"}
    
    B --> C["Form Login"]
    C --> D["POST /login"]
    D --> E["CustomUserDetailsService"]
    E --> F["AuthenticationEventListener"]
    F --> G{"Thành công?"}
    G -->|Yes| H["SecurityConfig.commonSuccessHandler()"]
    G -->|No| I["Login page + error"]
    H --> J{"Role?"}
    J -->|ADMIN| K["/admin/dashboard"]
    J -->|USER| L["/home"]
    
    B --> M["Google/Facebook OAuth2"]
    M --> N["CustomOAuth2UserService"]
    N --> O["Tạo/tìm Account"]
    O --> H
    
    B --> P["Đăng ký"]
    P --> Q["POST /account/sign-up"]
    Q --> R["AccountService.register()"]
    R --> S["EmailVerificationService"]
    S --> T["Gửi email xác thực"]
    T --> U["User xác thực email"]
    U --> V["Account activated"]
```

### 3.3 🛒 Giỏ Hàng → Thanh Toán

```mermaid
flowchart TD
    A["User chọn sản phẩm"] --> B{"Hành động"}
    
    B -->|"Thêm vào giỏ"| C["POST /cart/add"]
    B -->|"Mua ngay"| D["POST /cart/buy-now (AJAX)"]
    
    C --> E["CartController.addToCart()"]
    E --> F["CartService.addToCart()"]
    F --> G["CartRepository.save()"]
    G --> H["Redirect → /cart"]
    
    D --> I["CartController.buyNow()"]
    I --> J["CartService.addToCart()"]
    J --> K["Return Cart ID"]
    K --> L["Redirect → /checkout?ids=cartId"]
    
    H --> M["Xem giỏ hàng /cart"]
    M --> N{"Cập nhật"}
    N -->|"Số lượng"| O["PUT /cart/{id}/update (AJAX)"]
    N -->|"Xóa"| P["POST /cart/{id}/remove"]
    N -->|"Thanh toán"| Q["GET /checkout?ids=..."]
    
    Q --> R["CheckoutController.checkout()"]
    R --> S["Hiển thị form thanh toán"]
    S --> T["POST /checkout/place-order"]
    T --> U["OrderService.createOrder()"]
    U --> V["Giảm stock ProductVariant"]
    V --> W["Xóa items khỏi Cart"]
    W --> X["Redirect → /checkout/success"]
```

### 3.4 📦 Quản Lý Đơn Hàng (Admin)

```mermaid
flowchart TD
    A["Admin xem /admin/orders"] --> B["AdminOrderController.orders()"]
    B --> C["OrderService.getAllOrders()"]
    C --> D["Hiển thị danh sách đơn"]
    
    D --> E{"Thao tác"}
    E -->|"Duyệt"| F["POST /{id}/approve"]
    F --> G["PENDING → CONFIRMED"]
    
    E -->|"Giao hàng"| H["POST /{id}/ship"]
    H --> I["CONFIRMED → SHIPPING"]
    
    E -->|"Hoàn thành"| J["POST /{id}/complete"]
    J --> K["SHIPPING → COMPLETED"]
    
    E -->|"Hủy"| L["POST /{id}/cancel"]
    L --> M["Hoàn lại Stock"]
    M --> N["ANY → CANCELLED"]
```

**State Machine - Trạng thái đơn hàng:**

```mermaid
stateDiagram-v2
    [*] --> PENDING : Đặt hàng
    PENDING --> CONFIRMED : Admin duyệt
    CONFIRMED --> SHIPPING : Giao hàng
    SHIPPING --> COMPLETED : Hoàn thành
    
    PENDING --> CANCELLED : Admin/User hủy
    CONFIRMED --> CANCELLED : Admin hủy
    SHIPPING --> CANCELLED : Admin hủy
    
    CANCELLED --> [*]
    COMPLETED --> [*]
```

### 3.5 👤 Quản Lý Tài Khoản (User)

```mermaid
flowchart LR
    A["User"] --> B["/account/profile"]
    A --> C["/account/addresses"]
    A --> D["/account/orders"]
    A --> E["/account/reviews"]
    
    B --> F["AccountProfileController"]
    F --> G["Cập nhật: tên, SĐT, avatar"]
    F --> H["Đổi mật khẩu"]
    
    C --> I["AddressController (REST)"]
    I --> J["CRUD địa chỉ"]
    I --> K["Đặt mặc định"]
    
    D --> L["Xem lịch sử đơn hàng"]
```

### 3.6 👨‍💼 Quản Lý Sản Phẩm (Admin)

```mermaid
flowchart TD
    A["Admin /admin/products"] --> B["AdminProductController"]
    B --> C["Danh sách sản phẩm"]
    
    C --> D{"Thao tác"}
    D -->|"Thêm"| E["GET /admin/products/create"]
    E --> F["POST /admin/products/save"]
    F --> G["ProductService.createProduct()"]
    G --> H["FileService.save() → Upload ảnh"]
    
    D -->|"Sửa"| I["GET /admin/products/edit/{id}"]
    I --> J["POST /admin/products/update"]
    J --> K["ProductService.updateProduct()"]
    
    D -->|"Xóa"| L["POST /admin/products/delete/{id}"]
    L --> M["ProductService.deleteProduct() → Soft delete"]
    
    D -->|"Biến thể"| N["GET /admin/products/{id}/variants"]
    N --> O["AdminProductVariantController"]
    O --> P["CRUD: Color + Size + Quantity"]
```

### 3.7 💬 Chat (WebSocket)

```mermaid
sequenceDiagram
    participant U as User Browser
    participant WS as WebSocket/STOMP
    participant CC as ChatController
    participant CS as InMemoryChatService
    participant A as Admin Browser

    U->>WS: Connect (SockJS)
    U->>WS: SEND /app/chat.sendMessage
    WS->>CC: handleChatMessage()
    CC->>CS: Store message
    CC->>WS: Broadcast to /topic/messages
    WS->>A: Message received
    A->>WS: SEND /app/chat.sendMessage (reply)
    WS->>CC: handleChatMessage()
    CC->>WS: Broadcast to /topic/messages
    WS->>U: Reply received
```

### 3.8 🔑 Quên / Đặt Lại Mật Khẩu

```mermaid
flowchart TD
    A["User quên mật khẩu"] --> B["GET /forgot-password"]
    B --> C["Nhập email"]
    C --> D["POST /forgot-password"]
    D --> E["PasswordResetService.createToken()"]
    E --> F["EmailService.sendResetEmail()"]
    F --> G["User nhận email"]
    G --> H["Click link reset"]
    H --> I["GET /reset-password?token=xxx"]
    I --> J["Nhập mật khẩu mới"]
    J --> K["POST /reset-password"]
    K --> L["AccountService.changePassword()"]
    L --> M["Redirect → /login?resetSuccess"]
```

### 3.9 📊 Dashboard (Admin)

```mermaid
flowchart LR
    A["Admin /admin/dashboard"] --> B["AdminDashboardController"]
    B --> C["DashboardService"]
    C --> D["Tổng doanh thu"]
    C --> E["Đơn hàng theo trạng thái"]
    C --> F["Sản phẩm bán chạy"]
    C --> G["Thống kê tổng quan"]
    D & E & F & G --> H["Render admin/dashboard.html"]
```

---

## 4. Tổng Hợp Endpoints

### User Endpoints

| Method | URL | Controller | Chức năng |
|--------|-----|-----------|-----------|
| GET | `/`, `/home` | HomeController | Trang chủ |
| GET | `/products` | ShopController | Danh sách sản phẩm |
| GET | `/products/{id}` | ShopController | Chi tiết sản phẩm |
| GET/POST | `/cart/**` | CartController | Giỏ hàng (CRUD) |
| GET/POST | `/checkout/**` | CheckoutController | Thanh toán |
| GET/POST | `/account/sign-up` | AccountAuthController | Đăng ký |
| GET/POST | `/login` | Spring Security | Đăng nhập |
| GET/POST | `/account/profile` | AccountProfileController | Hồ sơ cá nhân |
| REST | `/account/addresses/**` | AddressController | Địa chỉ |
| GET | `/account/orders` | AccountProfileController | Đơn hàng của tôi |
| GET/POST | `/forgot-password` | PasswordResetController | Quên mật khẩu |
| GET/POST | `/reset-password` | PasswordResetController | Đặt lại mật khẩu |
| GET | `/verify-email` | EmailVerificationController | Xác thực email |

### Admin Endpoints

| Method | URL | Controller | Chức năng |
|--------|-----|-----------|-----------|
| GET | `/admin/dashboard` | AdminDashboardController | Dashboard |
| GET/POST | `/admin/products/**` | AdminProductController | QL sản phẩm |
| GET/POST | `/admin/products/{id}/variants/**` | AdminProductVariantController | QL biến thể |
| GET/POST | `/admin/orders/**` | AdminOrderController | QL đơn hàng |
| GET/POST | `/admin/categories/**` | AdminCategoryController | QL danh mục |
| GET/POST | `/admin/accounts/**` | AdminAccountController | QL tài khoản |
| GET | `/admin/chat` | AdminChatController | Chat hỗ trợ |
