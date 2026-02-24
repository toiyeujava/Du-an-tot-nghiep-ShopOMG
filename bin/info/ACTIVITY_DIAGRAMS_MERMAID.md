# Activity Diagrams Mermaid - ShopOMG (Tiếng Việt)

Tài liệu này chứa tất cả 13 Activity Diagrams bằng Mermaid syntax với tiếng Việt.

## Hướng Dẫn Sử Dụng

1. Copy code Mermaid của từng diagram
2. Paste vào https://mermaid.live/
3. Export sang SVG hoặc PNG
4. Import file SVG vào Draw.io (File → Import → chọn file SVG)

---

## 1. XÁC THỰC

### 1.1. Đăng Nhập Với Giới Hạn Số Lần Thử

```mermaid
flowchart TD
    Start([Người dùng truy cập /login])
    Start --> CheckEmail{Có tham số<br/>email?}
    CheckEmail -->|Có| GetRemaining[Hiển thị số lần<br/>thử còn lại]
    CheckEmail -->|Không| ShowForm[Hiển thị form<br/>đăng nhập]
    GetRemaining --> CheckLocked1{Tài khoản<br/>bị khóa?}
    CheckLocked1 -->|Có| ShowLockTime[Hiển thị số phút<br/>còn lại bị khóa]
    CheckLocked1 -->|Không| ShowForm
    ShowLockTime --> ShowForm
    
    ShowForm --> UserSubmit[Người dùng nhập<br/>thông tin]
    UserSubmit --> ValidateInput{Input<br/>hợp lệ?}
    ValidateInput -->|Không| ShowError[Hiển thị lỗi<br/>validation]
    ShowError --> ShowForm
    
    ValidateInput -->|Có| CheckLocked2{Tài khoản<br/>bị khóa?}
    CheckLocked2 -->|Có| CalcTime[Tính toán thời gian<br/>khóa còn lại]
    CalcTime --> ShowLockedErr[Hiển thị lỗi<br/>tài khoản bị khóa]
    ShowLockedErr --> End1([Quay lại login])
    
    CheckLocked2 -->|Không| CheckVerified{Email đã<br/>xác thực?}
    CheckVerified -->|Không| ShowVerifyErr[Hiển thị lỗi cần<br/>xác thực email]
    ShowVerifyErr --> End2([Chuyển đến<br/>xác thực])
    
    CheckVerified -->|Có| AuthUser{Thông tin<br/>đăng nhập<br/>đúng?}
    AuthUser -->|Không| RecordFail[Ghi nhận đăng<br/>nhập thất bại]
    RecordFail --> IncCounter[Tăng<br/>failed_login_attempts]
    IncCounter --> CheckLimit{Số lần thử<br/>>= 5?}
    CheckLimit -->|Có| LockAcc[Khóa tài khoản:<br/>account_locked_until<br/>= now + 15 phút]
    CheckLimit -->|Không| ShowFailErr[Hiển thị lỗi với<br/>số lần thử còn lại]
    LockAcc --> ShowLockMsg[Hiển thị thông báo<br/>tài khoản bị khóa]
    ShowLockMsg --> End3([Quay lại login])
    ShowFailErr --> End4([Quay lại login])
    
    AuthUser -->|Có| RecordSuccess[Ghi nhận đăng<br/>nhập thành công]
    RecordSuccess --> ResetAttempts[Reset<br/>failed_login_attempts = 0]
    ResetAttempts --> ClearLock[Xóa<br/>account_locked_until]
    ClearLock --> UpdateLogin[Cập nhật<br/>last_login]
    UpdateLogin --> CreateSession[Tạo phiên<br/>đăng nhập]
    CreateSession --> CheckRole{Vai trò<br/>người dùng?}
    CheckRole -->|ADMIN| EndAdmin([Chuyển đến<br/>/admin/dashboard])
    CheckRole -->|USER| EndUser([Chuyển đến<br/>/home])
    
    style Start fill:#E8F5E9
    style End1 fill:#FFEBEE
    style End2 fill:#FFEBEE
    style End3 fill:#FFEBEE
    style End4 fill:#FFEBEE
    style EndAdmin fill:#C8E6C9
    style EndUser fill:#C8E6C9
    style CheckEmail fill:#FFF9C4
    style CheckLocked1 fill:#FFF9C4
    style ValidateInput fill:#FFF9C4
    style CheckLocked2 fill:#FFF9C4
    style CheckVerified fill:#FFF9C4
    style AuthUser fill:#FFF9C4
    style CheckLimit fill:#FFF9C4
    style CheckRole fill:#FFF9C4
```

---

### 1.2. Đăng Ký Với Xác Thực Email

```mermaid
flowchart TD
    Start([Người dùng truy cập<br/>/account/sign-up])
    Start --> ShowForm[Hiển thị form<br/>đăng ký]
    ShowForm --> UserFill[Người dùng điền form:<br/>username, fullName,<br/>email, password, phone]
    UserFill --> UserSubmit[Người dùng<br/>submit đăng ký]
    UserSubmit --> ValidInput{Validation<br/>passed?}
    ValidInput -->|Không| ShowErr1[Hiển thị lỗi<br/>validation]
    ShowErr1 --> ShowForm
    
    ValidInput -->|Có| CheckPass{password ==<br/>confirmPassword?}
    CheckPass -->|Không| ShowErr2[Hiển thị lỗi<br/>mật khẩu không khớp]
    ShowErr2 --> ShowForm
    
    CheckPass -->|Có| CheckStrength{Mật khẩu<br/>đủ mạnh?}
    CheckStrength -->|Không| ShowErr3[Hiển thị lỗi<br/>độ mạnh mật khẩu]
    ShowErr3 --> ShowForm
    
    CheckStrength -->|Có| CheckEmail{Email đã<br/>tồn tại?}
    CheckEmail -->|Có| ShowErr4[Hiển thị lỗi<br/>email đã tồn tại]
    ShowErr4 --> ShowForm
    
    CheckEmail -->|Không| CheckUser{Username<br/>đã tồn tại?}
    CheckUser -->|Có| ShowErr5[Hiển thị lỗi<br/>username đã tồn tại]
    ShowErr5 --> ShowForm
    
    CheckUser -->|Không| CreateAcc[Tạo tài khoản với<br/>emailVerified = false]
    CreateAcc --> HashPass[Hash mật khẩu<br/>bằng BCrypt]
    HashPass --> SetRole[Đặt role = USER,<br/>isActive = true]
    SetRole --> SaveAcc[Lưu tài khoản<br/>vào database]
    SaveAcc --> GenToken[Tạo UUID<br/>verification token]
    GenToken --> SetExpiry[Đặt token expiry<br/>= now + 24 giờ]
    SetExpiry --> SaveToken[Lưu token vào<br/>email_verification_tokens]
    SaveToken --> BuildEmail[Xây dựng email<br/>xác thực với link token]
    BuildEmail --> SendEmail[Gửi email<br/>xác thực]
    SendEmail --> EndSuccess([Chuyển đến<br/>/verify-email-sent])
    
    style Start fill:#E8F5E9
    style EndSuccess fill:#C8E6C9
    style ValidInput fill:#FFF9C4
    style CheckPass fill:#FFF9C4
    style CheckStrength fill:#FFF9C4
    style CheckEmail fill:#FFF9C4
    style CheckUser fill:#FFF9C4
```

---

### 1.3. Đăng Nhập OAuth2 (Facebook/Google)

```mermaid
flowchart TD
    Start([Người dùng click nút<br/>đăng nhập mạng xã hội])
    Start --> Redirect[Chuyển hướng đến<br/>OAuth provider]
    Redirect --> UserAuth[Người dùng xác thực<br/>với provider]
    UserAuth --> CheckAuth{Xác thực<br/>thành công?}
    CheckAuth -->|Không| EndErr([Hiển thị lỗi<br/>xác thực])
    
    CheckAuth -->|Có| Callback[Nhận OAuth2<br/>callback]
    Callback --> Extract[Trích xuất thông tin:<br/>email, name, ID]
    Extract --> CheckEmailAvail{Email<br/>có sẵn?}
    CheckEmailAvail -->|Không| UseID[Sử dụng provider ID<br/>làm identifier]
    CheckEmailAvail -->|Có| UseEmail[Sử dụng email<br/>làm identifier]
    
    UseEmail --> FindByEmail[Tìm tài khoản<br/>theo email]
    UseID --> FindByID[Tìm tài khoản theo<br/>username = provider ID]
    
    FindByEmail --> AccExists{Tìm thấy<br/>tài khoản?}
    FindByID --> AccExists
    
    AccExists -->|Có| UpdateLogin[Cập nhật<br/>last_login]
    UpdateLogin --> CreateSession1[Tạo phiên<br/>đăng nhập]
    CreateSession1 --> EndHome1([Chuyển đến<br/>/home])
    
    AccExists -->|Không| CreateNew[Tạo tài khoản mới]
    CreateNew --> SetFields["Đặt username = provider ID<br/>email = email (nếu có)<br/>fullName = name<br/>emailVerified = true"]
    SetFields --> SetUserRole[Đặt role = USER,<br/>isActive = true]
    SetUserRole --> SaveNew[Lưu tài khoản mới<br/>vào database]
    SaveNew --> CreateSession2[Tạo phiên<br/>đăng nhập mới]
    CreateSession2 --> EndHome2([Chuyển đến<br/>/home])
    
    style Start fill:#E8F5E9
    style EndErr fill:#FFEBEE
    style EndHome1 fill:#C8E6C9
    style EndHome2 fill:#C8E6C9
    style CheckAuth fill:#FFF9C4
    style CheckEmailAvail fill:#FFF9C4
    style AccExists fill:#FFF9C4
```

---

## 2. XÁC THỰC EMAIL

### 2.1. Xác Thực Email Từ Link

```mermaid
flowchart TD
    Start([Người dùng click<br/>link trong email])
    Start --> Parse[Parse URL:<br/>/verify-email?token=xxx]
    Parse --> CheckToken{Có tham số<br/>token?}
    CheckToken -->|Không| ShowInvalid[Hiển thị trang lỗi<br/>link không hợp lệ]
    ShowInvalid --> End1([Kết thúc])
    
    CheckToken -->|Có| ExtractToken[Trích xuất<br/>token string]
    ExtractToken --> QueryDB[Query bảng<br/>email_verification_tokens]
    QueryDB --> TokenExists{Token tồn tại<br/>trong DB?}
    TokenExists -->|Không| ShowExpired1[Hiển thị lỗi token<br/>hết hạn/không hợp lệ]
    ShowExpired1 --> End2([Kết thúc])
    
    TokenExists -->|Có| CheckExpiry{Token expiry<br/>> now?}
    CheckExpiry -->|Không| DeleteToken[Xóa token<br/>đã hết hạn]
    DeleteToken --> ShowExpired2[Hiển thị lỗi token<br/>hết hạn/không hợp lệ]
    ShowExpired2 --> End2
    
    CheckExpiry -->|Có| GetAcc[Lấy tài khoản<br/>liên kết]
    GetAcc --> SetVerified[Đặt emailVerified<br/>= true]
    SetVerified --> SaveAcc[Lưu tài khoản<br/>vào database]
    SaveAcc --> DeleteUsed[Xóa token<br/>đã sử dụng]
    DeleteUsed --> ShowSuccess[Hiển thị trang<br/>xác thực thành công]
    ShowSuccess --> ProvideLink[Cung cấp link<br/>đến /login]
    ProvideLink --> End3([Người dùng có thể<br/>đăng nhập])
    
    style Start fill:#E8F5E9
    style End1 fill:#FFEBEE
    style End2 fill:#FFEBEE
    style End3 fill:#C8E6C9
    style CheckToken fill:#FFF9C4
    style TokenExists fill:#FFF9C4
    style CheckExpiry fill:#FFF9C4
```

---

### 2.2. Gửi Lại Email Xác Thực

```mermaid
flowchart TD
    Start([Người dùng truy cập<br/>/resend-verification])
    Start --> ShowForm[Hiển thị form<br/>nhập email]
    ShowForm --> UserEnter[Người dùng nhập<br/>địa chỉ email]
    UserEnter --> UserSubmit[Người dùng<br/>submit form]
    UserSubmit --> ValidEmail{Email format<br/>hợp lệ?}
    ValidEmail -->|Không| ShowErr[Hiển thị lỗi<br/>format]
    ShowErr --> ShowForm
    
    ValidEmail -->|Có| FindAcc[Tìm tài khoản<br/>theo email]
    FindAcc --> AccExists{Tài khoản<br/>tồn tại?}
    AccExists -->|Không| ShowGeneric1[Hiển thị thông báo<br/>chung thành công]
    ShowGeneric1 --> End1([Chuyển đến<br/>/verify-email-sent])
    
    AccExists -->|Có| CheckVerified{Đã xác thực<br/>rồi?}
    CheckVerified -->|Có| ShowGeneric2[Hiển thị thông báo<br/>chung thành công]
    ShowGeneric2 --> End1
    
    CheckVerified -->|Không| DeleteOld[Xóa các token<br/>xác thực cũ]
    DeleteOld --> GenNew[Tạo UUID<br/>token mới]
    GenNew --> SetExpiry[Đặt expiry<br/>= now + 24 giờ]
    SetExpiry --> SaveToken[Lưu token mới<br/>vào database]
    SaveToken --> BuildEmail[Xây dựng email<br/>xác thực]
    BuildEmail --> SendEmail[Gửi email với<br/>token mới]
    SendEmail --> ShowSuccess[Hiển thị thông báo<br/>gửi lại thành công]
    ShowSuccess --> End2([Chuyển đến<br/>/verify-email-sent])
    
    style Start fill:#E8F5E9
    style End1 fill:#C8E6C9
    style End2 fill:#C8E6C9
    style ValidEmail fill:#FFF9C4
    style AccExists fill:#FFF9C4
    style CheckVerified fill:#FFF9C4
```

---

## 3. ĐẶT LẠI MẬT KHẨU

### 3.1. Yêu Cầu Quên Mật Khẩu

```mermaid
flowchart TD
    Start([Người dùng truy cập<br/>/forgot-password])
    Start --> ShowForm[Hiển thị form<br/>nhập email]
    ShowForm --> UserEnter[Người dùng<br/>nhập email]
    UserEnter --> UserSubmit[Người dùng<br/>submit form]
    UserSubmit --> ValidEmail{Email format<br/>hợp lệ?}
    ValidEmail -->|Không| ShowErr[Hiển thị lỗi<br/>validation]
    ShowErr --> ShowForm
    
    ValidEmail -->|Có| FindAcc[Tìm tài khoản<br/>theo email]
    FindAcc --> AccExists{Tài khoản<br/>tồn tại?}
    AccExists -->|Không| ShowGeneric1[Hiển thị thông báo<br/>chung]
    ShowGeneric1 --> End1([Kết thúc - bảo mật:<br/>không tiết lộ email])
    
    AccExists -->|Có| CheckActive{Tài khoản<br/>đang active?}
    CheckActive -->|Không| ShowGeneric2[Hiển thị thông báo<br/>chung]
    ShowGeneric2 --> End1
    
    CheckActive -->|Có| DeleteOld[Xóa các token<br/>reset password cũ]
    DeleteOld --> GenToken[Tạo UUID<br/>reset token]
    GenToken --> SetExpiry[Đặt expiry<br/>= now + 1 giờ]
    SetExpiry --> SetUsed[Đặt used<br/>= false]
    SetUsed --> SaveToken[Lưu token vào<br/>password_reset_tokens]
    SaveToken --> BuildEmail[Xây dựng email<br/>reset password]
    BuildEmail --> SendEmail[Gửi email với<br/>link reset]
    SendEmail --> ShowSuccess[Hiển thị thông báo<br/>thành công]
    ShowSuccess --> End2([Người dùng<br/>kiểm tra email])
    
    style Start fill:#E8F5E9
    style End1 fill:#C8E6C9
    style End2 fill:#C8E6C9
    style ValidEmail fill:#FFF9C4
    style AccExists fill:#FFF9C4
    style CheckActive fill:#FFF9C4
```

---

### 3.2. Đặt Lại Mật Khẩu Với Token

```mermaid
flowchart TD
    Start([Người dùng click link:<br/>/reset-password?token=xxx])
    Start --> CheckParam{Có tham số<br/>token?}
    CheckParam -->|Không| ShowInvalid[Hiển thị lỗi<br/>link không hợp lệ]
    ShowInvalid --> End1([Chuyển đến<br/>/login])
    
    CheckParam -->|Có| QueryDB[Query bảng<br/>password_reset_tokens]
    QueryDB --> TokenExists{Token<br/>tồn tại?}
    TokenExists -->|Không| ShowExpired1[Hiển thị lỗi<br/>hết hạn/không hợp lệ]
    ShowExpired1 --> End2([Chuyển đến<br/>/forgot-password])
    
    TokenExists -->|Có| CheckExpiry{expiry_date<br/>> now?}
    CheckExpiry -->|Không| ShowExpired2[Hiển thị lỗi<br/>hết hạn/không hợp lệ]
    ShowExpired2 --> End2
    
    CheckExpiry -->|Có| CheckUsed{Token used<br/>== false?}
    CheckUsed -->|Không| ShowUsedErr[Hiển thị lỗi<br/>đã sử dụng]
    ShowUsedErr --> End2
    
    CheckUsed -->|Có| ShowForm[Hiển thị form<br/>reset password]
    ShowForm --> UserFill[Người dùng nhập<br/>mật khẩu mới]
    UserFill --> UserSubmit[Người dùng<br/>submit form]
    UserSubmit --> ValidPass{Validation<br/>passed?}
    ValidPass -->|Không| ShowErr1[Hiển thị lỗi<br/>validation]
    ShowErr1 --> ShowForm
    
    ValidPass -->|Có| CheckStrength{Độ mạnh<br/>mật khẩu OK?}
    CheckStrength -->|Không| ShowErr2[Hiển thị yêu cầu<br/>độ mạnh]
    ShowErr2 --> ShowForm
    
    CheckStrength -->|Có| CheckMatch{newPassword ==<br/>confirmPassword?}
    CheckMatch -->|Không| ShowErr3[Hiển thị lỗi<br/>không khớp]
    ShowErr3 --> ShowForm
    
    CheckMatch -->|Có| Revalidate[Validate token<br/>lần nữa]
    Revalidate --> StillValid{Vẫn còn<br/>hợp lệ?}
    StillValid -->|Không| ShowExpired3[Hiển thị lỗi<br/>hết hạn/không hợp lệ]
    ShowExpired3 --> End2
    
    StillValid -->|Có| HashPass[Hash mật khẩu mới<br/>với BCrypt]
    HashPass --> UpdatePass[Cập nhật password<br/>của tài khoản]
    UpdatePass --> SaveAcc[Lưu tài khoản]
    SaveAcc --> MarkUsed[Đánh dấu<br/>token.used = true]
    MarkUsed --> SaveToken[Lưu token]
    SaveToken --> ShowSuccess[Hiển thị thông báo<br/>thành công]
    ShowSuccess --> End3([Chuyển đến<br/>/login])
    
    style Start fill:#E8F5E9
    style End1 fill:#FFEBEE
    style End2 fill:#FFEBEE
    style End3 fill:#C8E6C9
    style CheckParam fill:#FFF9C4
    style TokenExists fill:#FFF9C4
    style CheckExpiry fill:#FFF9C4
    style CheckUsed fill:#FFF9C4
    style ValidPass fill:#FFF9C4
    style CheckStrength fill:#FFF9C4
    style CheckMatch fill:#FFF9C4
    style StillValid fill:#FFF9C4
```

---

## 4. QUẢN LÝ TÀI KHOẢN

### 4.1. Xem Thông Tin Cá Nhân

```mermaid
flowchart TD
    Start([Người dùng truy cập<br/>/account/profile])
    Start --> GetPrincipal[Lấy authenticated<br/>principal]
    GetPrincipal --> CheckType{OAuth2 hay<br/>Form auth?}
    CheckType -->|OAuth2| ExtractEmail[Trích xuất email<br/>từ OAuth2 token]
    CheckType -->|Form| ExtractUser[Trích xuất username<br/>từ principal]
    
    ExtractEmail --> FindByEmail[Tìm tài khoản<br/>theo email]
    ExtractUser --> FindByUser[Tìm tài khoản<br/>theo username]
    
    FindByEmail --> AccFound{Tìm thấy<br/>tài khoản?}
    FindByUser --> AccFound
    
    AccFound -->|Không| EndErr([Chuyển đến<br/>/login?error=true])
    AccFound -->|Có| LoadAcc[Load thông tin<br/>tài khoản]
    LoadAcc --> CreateForm[Tạo ProfileForm<br/>DTO]
    CreateForm --> PopulateFields["Điền dữ liệu:<br/>username, fullName,<br/>phone, email, avatarUrl,<br/>birthDate, gender"]
    PopulateFields --> AddModel[Thêm profileForm<br/>vào model]
    AddModel --> SetPage[Đặt activePage<br/>= profile]
    SetPage --> EndRender([Render user/<br/>account-profile.html])
    
    style Start fill:#E8F5E9
    style EndErr fill:#FFEBEE
    style EndRender fill:#C8E6C9
    style CheckType fill:#FFF9C4
    style AccFound fill:#FFF9C4
```

---

### 4.2. Cập Nhật Thông Tin Cá Nhân Với Avatar

```mermaid
flowchart TD
    Start([Người dùng submit form<br/>cập nhật profile])
    Start --> GetData[Nhận dữ liệu<br/>ProfileForm + avatarFile]
    GetData --> ValidInput{Input validation<br/>passed?}
    ValidInput -->|Không| ShowErr[Hiển thị lỗi<br/>validation trên form]
    ShowErr --> End1([Quay lại<br/>trang profile])
    
    ValidInput -->|Có| GetPrincipal[Lấy authenticated<br/>principal]
    GetPrincipal --> FindAcc[Tìm tài khoản bằng<br/>helper method]
    FindAcc --> AccFound{Tìm thấy<br/>tài khoản?}
    AccFound -->|Không| End2([Chuyển đến<br/>/login?error=true])
    
    AccFound -->|Có| UpdateFields["Cập nhật:<br/>fullName, phone,<br/>birthDate, gender"]
    UpdateFields --> CheckAvatar{File avatar<br/>được upload?}
    CheckAvatar -->|Không| SaveAcc[Lưu tài khoản<br/>vào database]
    
    CheckAvatar -->|Có| ValidFile{File không<br/>rỗng?}
    ValidFile -->|Không| SaveAcc
    
    ValidFile -->|Có| SaveFile[Lưu file vào<br/>thư mục uploads]
    SaveFile --> GetPath[Lấy đường dẫn<br/>file tương đối]
    GetPath --> UpdateAvatar[Cập nhật<br/>account.avatar = filePath]
    UpdateAvatar --> SaveAcc
    
    SaveAcc --> AddFlash[Thêm flash message<br/>thành công]
    AddFlash --> End3([Chuyển đến<br/>/account/profile])
    
    style Start fill:#E8F5E9
    style End1 fill:#FFEBEE
    style End2 fill:#FFEBEE
    style End3 fill:#C8E6C9
    style ValidInput fill:#FFF9C4
    style AccFound fill:#FFF9C4
    style CheckAvatar fill:#FFF9C4
    style ValidFile fill:#FFF9C4
```

---

## 5. QUẢN LÝ ADMIN

### 5.1. Truy Cập Admin Dashboard

```mermaid
flowchart TD
    Start([Admin truy cập<br/>/admin/dashboard])
    Start --> CheckAuth{Người dùng<br/>đã xác thực?}
    CheckAuth -->|Không| End1([Chuyển đến<br/>/login])
    CheckAuth -->|Có| CheckRole{Role ==<br/>ADMIN?}
    CheckRole -->|Không| End2([Hiển thị 403<br/>Access Denied])
    CheckRole -->|Có| LoadData[Load dữ liệu<br/>thống kê dashboard]
    LoadData --> CountProd[Đếm tổng số<br/>sản phẩm]
    CountProd --> CountOrder[Đếm tổng số<br/>đơn hàng]
    CountOrder --> CountUser[Đếm tổng số<br/>người dùng]
    CountUser --> CalcRev[Tính tổng<br/>doanh thu]
    CalcRev --> GetRecent[Lấy đơn hàng<br/>gần đây]
    GetRecent --> AddModel[Thêm tất cả dữ liệu<br/>vào model]
    AddModel --> SetTitle[Đặt pageTitle =<br/>Tổng quan - Admin]
    SetTitle --> End3([Render admin/<br/>dashboard.html])
    
    style Start fill:#E8F5E9
    style End1 fill:#FFEBEE
    style End2 fill:#FFEBEE
    style End3 fill:#C8E6C9
    style CheckAuth fill:#FFF9C4
    style CheckRole fill:#FFF9C4
```

---

### 5.2. Quản Lý Sản Phẩm

```mermaid
flowchart TD
    Start([Admin truy cập<br/>/admin/products])
    Start --> CheckAuth{Đã xác thực<br/>& ADMIN?}
    CheckAuth -->|Không| End1([Access Denied])
    CheckAuth -->|Có| LoadProd[Load tất cả sản phẩm<br/>từ database]
    LoadProd --> AddModel[Thêm danh sách<br/>sản phẩm vào model]
    AddModel --> RenderList([Render admin/<br/>products.html])
    
    RenderList --> Action{Hành động<br/>của Admin?}
    
    Action -->|Tạo mới| NavCreate[Chuyển đến<br/>/admin/products/create]
    NavCreate --> ShowCreate[Hiển thị form<br/>tạo sản phẩm]
    ShowCreate --> FillCreate[Admin điền<br/>thông tin sản phẩm]
    FillCreate --> SubmitCreate[Admin submit<br/>form]
    SubmitCreate --> ValidCreate{Validation<br/>passed?}
    ValidCreate -->|Không| ShowErrCreate[Hiển thị lỗi<br/>validation]
    ShowErrCreate --> ShowCreate
    ValidCreate -->|Có| SaveNew[Lưu sản phẩm mới<br/>vào database]
    SaveNew --> End2([Chuyển đến<br/>/admin/products])
    
    Action -->|Sửa| LoadEdit[Load sản phẩm<br/>theo ID]
    LoadEdit --> ShowEdit[Hiển thị form sửa<br/>với dữ liệu]
    ShowEdit --> FillEdit[Admin cập nhật<br/>các trường]
    FillEdit --> SubmitEdit[Admin submit<br/>cập nhật]
    SubmitEdit --> ValidEdit{Validation<br/>passed?}
    ValidEdit -->|Không| ShowErrEdit[Hiển thị lỗi]
    ShowErrEdit --> ShowEdit
    ValidEdit -->|Có| UpdateProd[Cập nhật sản phẩm<br/>trong database]
    UpdateProd --> End3([Chuyển đến<br/>/admin/products])
    
    Action -->|Xóa| ConfirmDel{Xác nhận<br/>xóa?}
    ConfirmDel -->|Không| End4([Hủy])
    ConfirmDel -->|Có| DeleteProd[Xóa sản phẩm<br/>khỏi database]
    DeleteProd --> End5([Chuyển đến<br/>/admin/products])
    
    style Start fill:#E8F5E9
    style End1 fill:#FFEBEE
    style End2 fill:#C8E6C9
    style End3 fill:#C8E6C9
    style End4 fill:#FFEBEE
    style End5 fill:#C8E6C9
    style CheckAuth fill:#FFF9C4
    style Action fill:#FFF9C4
    style ValidCreate fill:#FFF9C4
    style ValidEdit fill:#FFF9C4
    style ConfirmDel fill:#FFF9C4
```

---

### 5.3. Quản Lý Đơn Hàng & Tài Khoản

```mermaid
flowchart TD
    Start([Admin điều hướng đến<br/>phần quản lý])
    Start --> SelectSection{Phần nào?}
    
    SelectSection -->|Đơn hàng| AccessOrders[Truy cập<br/>/admin/orders]
    AccessOrders --> LoadOrders[Load tất cả đơn hàng<br/>từ database]
    LoadOrders --> SortOrders[Sắp xếp theo ngày tạo<br/>giảm dần]
    SortOrders --> DisplayOrders([Hiển thị danh sách<br/>đơn hàng])
    DisplayOrders --> OrderAction{Hành động<br/>của Admin?}
    OrderAction -->|Xem chi tiết| ShowDetail[Hiển thị chi tiết<br/>đơn hàng]
    OrderAction -->|Cập nhật trạng thái| UpdateStatus[Cập nhật trạng thái<br/>đơn hàng]
    UpdateStatus --> SaveOrder[Lưu thay đổi<br/>đơn hàng]
    SaveOrder --> End1([Làm mới danh sách<br/>đơn hàng])
    
    SelectSection -->|Tài khoản| AccessAccounts[Truy cập<br/>/admin/accounts]
    AccessAccounts --> LoadAccounts[Load tất cả<br/>tài khoản]
    LoadAccounts --> DisplayAccounts([Hiển thị danh sách<br/>tài khoản])
    DisplayAccounts --> AccountAction{Hành động<br/>của Admin?}
    AccountAction -->|Xem profile| ShowProfile[Hiển thị profile<br/>người dùng]
    AccountAction -->|Bật/tắt Active| ToggleActive[Bật/tắt cờ<br/>isActive]
    AccountAction -->|Mở khóa| UnlockAcc[Reset failed attempts<br/>& unlock]
    ToggleActive --> SaveAcc[Lưu thay đổi<br/>tài khoản]
    UnlockAcc --> SaveAcc
    SaveAcc --> End2([Làm mới danh sách<br/>tài khoản])
    
    style Start fill:#E8F5E9
    style End1 fill:#C8E6C9
    style End2 fill:#C8E6C9
    style SelectSection fill:#FFF9C4
    style OrderAction fill:#FFF9C4
    style AccountAction fill:#FFF9C4
```

---

## 6. MUA SẮM

### 6.1. Duyệt Sản Phẩm & Xem Chi Tiết

```mermaid
flowchart TD
    Start([Người dùng truy cập<br/>/products])
    Start --> LoadProd[Load sản phẩm<br/>từ database]
    LoadProd --> CheckFilter{Có áp dụng<br/>bộ lọc?}
    CheckFilter -->|Có| FilterCat[Lọc theo<br/>danh mục]
    FilterCat --> FilterPrice[Lọc theo<br/>khoảng giá]
    FilterPrice --> FilterSearch[Lọc theo từ khóa<br/>tìm kiếm]
    FilterSearch --> SortResult[Sắp xếp<br/>kết quả]
    CheckFilter -->|Không| SortResult
    SortResult --> Paginate[Phân trang<br/>kết quả]
    Paginate --> DisplayGrid([Hiển thị lưới<br/>sản phẩm])
    
    DisplayGrid --> UserClick{Người dùng<br/>click sản phẩm?}
    UserClick -->|Có| NavDetail[Điều hướng đến<br/>/product/{id}]
    UserClick -->|Không| Browse[Tiếp tục duyệt]
    
    NavDetail --> LoadDetail[Load sản phẩm<br/>theo ID]
    LoadDetail --> ProdExists{Sản phẩm<br/>tồn tại?}
    ProdExists -->|Không| End1([Hiển thị<br/>404 error])
    ProdExists -->|Có| LoadImages[Load hình ảnh<br/>sản phẩm]
    LoadImages --> LoadReviews[Load đánh giá<br/>sản phẩm]
    LoadReviews --> LoadRelated[Load sản phẩm<br/>liên quan]
    LoadRelated --> DisplayDetail([Hiển thị trang<br/>chi tiết sản phẩm])
    DisplayDetail --> DetailAction{Hành động<br/>người dùng?}
    DetailAction -->|Thêm vào giỏ| AddCart[Thêm vào<br/>giỏ hàng]
    DetailAction -->|Xem sản phẩm khác| NavDetail
    DetailAction -->|Quay lại| DisplayGrid
    
    style Start fill:#E8F5E9
    style End1 fill:#FFEBEE
    style CheckFilter fill:#FFF9C4
    style UserClick fill:#FFF9C4
    style ProdExists fill:#FFF9C4
    style DetailAction fill:#FFF9C4
```

---

### 6.2. Giỏ Hàng & Thanh Toán

```mermaid
flowchart TD
    Start([Người dùng thêm<br/>sản phẩm vào giỏ])
    Start --> CheckSession{Session người dùng<br/>tồn tại?}
    CheckSession -->|Không| CreateSession[Tạo session<br/>mới]
    CheckSession -->|Có| GetCart[Lấy giỏ hàng<br/>từ session]
    CreateSession --> InitCart[Khởi tạo giỏ hàng<br/>rỗng]
    
    GetCart --> CheckProd{Sản phẩm đã có<br/>trong giỏ?}
    InitCart --> CheckProd
    CheckProd -->|Có| UpdateQty[Cập nhật<br/>số lượng]
    CheckProd -->|Không| AddNew[Thêm item mới<br/>vào giỏ]
    UpdateQty --> RecalcTotal[Tính lại tổng<br/>giỏ hàng]
    AddNew --> RecalcTotal
    RecalcTotal --> SaveCart[Lưu giỏ hàng<br/>vào session]
    SaveCart --> UpdateBadge[Cập nhật badge<br/>số lượng giỏ hàng]
    
    UpdateBadge --> UserNav{Người dùng<br/>điều hướng đến?}
    UserNav -->|Xem giỏ| NavCart[Điều hướng<br/>đến /cart]
    UserNav -->|Tiếp tục mua| End1([Quay lại<br/>sản phẩm])
    
    NavCart --> ShowCart[Hiển thị tất cả<br/>item trong giỏ]
    ShowCart --> CartAction{Hành động<br/>trong giỏ?}
    CartAction -->|Cập nhật SL| UpdateItem[Cập nhật số lượng<br/>item]
    UpdateItem --> RecalcTotal
    CartAction -->|Xóa item| RemoveItem[Xóa item<br/>khỏi giỏ]
    RemoveItem --> RecalcTotal
    CartAction -->|Thanh toán| CheckAuth{Người dùng<br/>đã xác thực?}
    
    CheckAuth -->|Không| End2([Chuyển đến<br/>/login])
    CheckAuth -->|Có| NavCheckout[Điều hướng<br/>đến /checkout]
    NavCheckout --> LoadAddr[Load địa chỉ<br/>người dùng]
    LoadAddr --> ShowCheckout([Hiển thị form<br/>checkout])
    ShowCheckout --> SubmitOrder[Người dùng submit<br/>đơn hàng]
    SubmitOrder --> ValidOrder{Validation<br/>passed?}
    ValidOrder -->|Không| ShowErr[Hiển thị lỗi<br/>validation]
    ShowErr --> ShowCheckout
    
    ValidOrder -->|Có| CreateOrder[Tạo đơn hàng<br/>trong database]
    CreateOrder --> ClearCart[Xóa giỏ hàng<br/>khỏi session]
    ClearCart --> SendEmail[Gửi email xác nhận<br/>đơn hàng]
    SendEmail --> End3([Hiển thị trang<br/>thành công đơn hàng])
    
    style Start fill:#E8F5E9
    style End1 fill:#C8E6C9
    style End2 fill:#FFEBEE
    style End3 fill:#C8E6C9
    style CheckSession fill:#FFF9C4
    style CheckProd fill:#FFF9C4
    style UserNav fill:#FFF9C4
    style CartAction fill:#FFF9C4
    style CheckAuth fill:#FFF9C4
    style ValidOrder fill:#FFF9C4
```

---

## 7. QUẢN LÝ DANH MỤC (CRUD)

### 7.1. Quản Lý Danh Mục - CRUD Đầy Đủ

```mermaid
flowchart TD
    Start([Admin truy cập<br/>/admin/categories])
    Start --> CheckAuth{Đã xác thực<br/>& ADMIN?}
    CheckAuth -->|Không| End1([Access Denied])
    CheckAuth -->|Có| LoadCat[Load tất cả danh mục<br/>từ database]
    LoadCat --> SortCat[Sắp xếp theo<br/>tên/ngày tạo]
    SortCat --> DisplayList([Hiển thị danh sách<br/>danh mục])
    
    DisplayList --> Action{Hành động<br/>của Admin?}
    
    Action -->|Tạo mới| ShowCreateForm[Hiển thị form<br/>tạo danh mục]
    ShowCreateForm --> FillCreate[Admin điền:<br/>Tên, Mô tả, Icon]
    FillCreate --> SubmitCreate[Admin submit<br/>form tạo]
    SubmitCreate --> ValidCreate{Validation<br/>passed?}
    ValidCreate -->|Không| ShowErrCreate[Hiển thị lỗi:<br/>Tên trống, Tên trùng]
    ShowErrCreate --> ShowCreateForm
    ValidCreate -->|Có| CheckDupCreate{Tên danh mục<br/>đã tồn tại?}
    CheckDupCreate -->|Có| ShowErrDup[Hiển thị lỗi<br/>tên đã tồn tại]
    ShowErrDup --> ShowCreateForm
    CheckDupCreate -->|Không| SaveNew[Lưu danh mục mới<br/>vào database]
    SaveNew --> AddSuccess[Thêm flash message<br/>thành công]
    AddSuccess --> End2([Làm mới danh sách<br/>danh mục])
    
    Action -->|Sửa| LoadEdit[Load danh mục<br/>theo ID]
    LoadEdit --> CheckExists{Danh mục<br/>tồn tại?}
    CheckExists -->|Không| ShowErr404[Hiển thị lỗi<br/>404 Not Found]
    ShowErr404 --> End3([Quay lại danh sách])
    CheckExists -->|Có| ShowEditForm[Hiển thị form sửa<br/>với dữ liệu hiện tại]
    ShowEditForm --> FillEdit[Admin cập nhật:<br/>Tên, Mô tả, Icon]
    FillEdit --> SubmitEdit[Admin submit<br/>form sửa]
    SubmitEdit --> ValidEdit{Validation<br/>passed?}
    ValidEdit -->|Không| ShowErrEdit[Hiển thị lỗi<br/>validation]
    ShowErrEdit --> ShowEditForm
    ValidEdit -->|Có| CheckDupEdit{Tên trùng với<br/>danh mục khác?}
    CheckDupEdit -->|Có| ShowErrDup2[Hiển thị lỗi<br/>tên đã tồn tại]
    ShowErrDup2 --> ShowEditForm
    CheckDupEdit -->|Không| UpdateCat[Cập nhật danh mục<br/>trong database]
    UpdateCat --> UpdateSuccess[Thêm flash message<br/>cập nhật thành công]
    UpdateSuccess --> End4([Làm mới danh sách<br/>danh mục])
    
    Action -->|Xóa| CheckProducts{Danh mục có<br/>sản phẩm?}
    CheckProducts -->|Có| ShowErrHasProd[Hiển thị lỗi: Không thể<br/>xóa danh mục có sản phẩm]
    ShowErrHasProd --> End5([Quay lại danh sách])
    CheckProducts -->|Không| ConfirmDel{Xác nhận<br/>xóa?}
    ConfirmDel -->|Không| End6([Hủy xóa])
    ConfirmDel -->|Có| DeleteCat[Xóa danh mục<br/>khỏi database]
    DeleteCat --> DelSuccess[Thêm flash message<br/>xóa thành công]
    DelSuccess --> End7([Làm mới danh sách<br/>danh mục])
    
    style Start fill:#E8F5E9
    style End1 fill:#FFEBEE
    style End2 fill:#C8E6C9
    style End3 fill:#FFEBEE
    style End4 fill:#C8E6C9
    style End5 fill:#FFEBEE
    style End6 fill:#FFEBEE
    style End7 fill:#C8E6C9
    style CheckAuth fill:#FFF9C4
    style Action fill:#FFF9C4
    style ValidCreate fill:#FFF9C4
    style CheckDupCreate fill:#FFF9C4
    style CheckExists fill:#FFF9C4
    style ValidEdit fill:#FFF9C4
    style CheckDupEdit fill:#FFF9C4
    style CheckProducts fill:#FFF9C4
    style ConfirmDel fill:#FFF9C4
```

---

## 8. QUẢN LÝ SẢN PHẨM CHI TIẾT

### 8.1. Tạo/Sửa Sản Phẩm Với Upload Hình Ảnh

```mermaid
flowchart TD
    Start([Admin chọn Tạo/Sửa<br/>sản phẩm])
    Start --> CheckMode{Chế độ?}
    
    CheckMode -->|Tạo mới| ShowCreateForm[Hiển thị form<br/>tạo sản phẩm trống]
    CheckMode -->|Sửa| LoadProd[Load sản phẩm<br/>theo ID]
    LoadProd --> ProdExists{Sản phẩm<br/>tồn tại?}
    ProdExists -->|Không| End1([Hiển thị 404<br/>Not Found])
    ProdExists -->|Có| ShowEditForm[Hiển thị form sửa<br/>với dữ liệu hiện tại]
    
    ShowCreateForm --> FillForm[Admin điền thông tin]
    ShowEditForm --> FillForm
    FillForm --> FillDetails["Nhập: Tên, Mô tả,<br/>Giá, Số lượng, Danh mục"]
    FillDetails --> UploadImages{Upload<br/>hình ảnh?}
    UploadImages -->|Có| SelectFiles[Chọn file hình ảnh<br/>từ máy tính]
    SelectFiles --> ValidateFiles{File hợp lệ?<br/>JPG/PNG, < 5MB}
    ValidateFiles -->|Không| ShowFileErr[Hiển thị lỗi:<br/>Format/Size không hợp lệ]
    ShowFileErr --> UploadImages
    ValidateFiles -->|Có| PreviewImages[Hiển thị preview<br/>hình ảnh]
    PreviewImages --> SubmitForm[Admin submit form]
    UploadImages -->|Không| SubmitForm
    
    SubmitForm --> ValidateForm{Validation<br/>passed?}
    ValidateForm -->|Không| ShowFormErr["Hiển thị lỗi:<br/>- Tên trống<br/>- Giá <= 0<br/>- Số lượng < 0<br/>- Chưa chọn danh mục"]
    ShowFormErr --> FillForm
    
    ValidateForm -->|Có| CheckDup{Tên sản phẩm<br/>đã tồn tại?}
    CheckDup -->|Có| ShowDupErr[Hiển thị lỗi<br/>tên đã tồn tại]
    ShowDupErr --> FillForm
    
    CheckDup -->|Không| SaveImages{Có hình ảnh<br/>để lưu?}
    SaveImages -->|Có| UploadToServer[Upload hình ảnh<br/>lên server]
    UploadToServer --> GeneratePaths[Tạo đường dẫn<br/>file cho từng ảnh]
    GeneratePaths --> SaveProduct[Lưu sản phẩm<br/>vào database]
    SaveImages -->|Không| SaveProduct
    
    SaveProduct --> SaveImageRecords[Lưu thông tin hình ảnh<br/>vào product_images]
    SaveImageRecords --> CheckMode2{Chế độ?}
    CheckMode2 -->|Tạo mới| AddSuccessMsg[Flash message:<br/>Tạo sản phẩm thành công]
    CheckMode2 -->|Sửa| UpdateSuccessMsg[Flash message:<br/>Cập nhật thành công]
    AddSuccessMsg --> End2([Chuyển đến<br/>/admin/products])
    UpdateSuccessMsg --> End2
    
    style Start fill:#E8F5E9
    style End1 fill:#FFEBEE
    style End2 fill:#C8E6C9
    style CheckMode fill:#FFF9C4
    style ProdExists fill:#FFF9C4
    style UploadImages fill:#FFF9C4
    style ValidateFiles fill:#FFF9C4
    style ValidateForm fill:#FFF9C4
    style CheckDup fill:#FFF9C4
    style SaveImages fill:#FFF9C4
    style CheckMode2 fill:#FFF9C4
```

---

## 9. TÌM KIẾM & LỌC SẢN PHẨM

### 9.1. Tìm Kiếm Sản Phẩm Nâng Cao

```mermaid
flowchart TD
    Start([Người dùng truy cập<br/>trang sản phẩm])
    Start --> ShowPage[Hiển thị trang sản phẩm<br/>với form tìm kiếm]
    ShowPage --> UserAction{Hành động<br/>người dùng?}
    
    UserAction -->|Nhập từ khóa| EnterKeyword[Nhập từ khóa<br/>vào ô tìm kiếm]
    UserAction -->|Chọn danh mục| SelectCat[Chọn danh mục<br/>từ dropdown]
    UserAction -->|Chọn khoảng giá| SelectPrice[Chọn khoảng giá:<br/>Min - Max]
    UserAction -->|Sắp xếp| SelectSort[Chọn sắp xếp:<br/>Giá, Tên, Mới nhất]
    
    EnterKeyword --> ApplyFilters[Áp dụng bộ lọc]
    SelectCat --> ApplyFilters
    SelectPrice --> ApplyFilters
    SelectSort --> ApplyFilters
    
    ApplyFilters --> BuildQuery[Xây dựng SQL query<br/>với điều kiện]
    BuildQuery --> CheckKeyword{Có từ<br/>khóa?}
    CheckKeyword -->|Có| AddKeywordFilter["WHERE name LIKE '%keyword%'<br/>OR description LIKE '%keyword%'"]
    CheckKeyword -->|Không| CheckCategory{Có chọn<br/>danh mục?}
    AddKeywordFilter --> CheckCategory
    
    CheckCategory -->|Có| AddCatFilter[AND category_id = ?]
    CheckCategory -->|Không| CheckPriceRange{Có khoảng<br/>giá?}
    AddCatFilter --> CheckPriceRange
    
    CheckPriceRange -->|Có| AddPriceFilter[AND price BETWEEN<br/>min AND max]
    CheckPriceRange -->|Không| ApplySort{Có sắp<br/>xếp?}
    AddPriceFilter --> ApplySort
    
    ApplySort -->|Giá tăng| SortPriceAsc[ORDER BY price ASC]
    ApplySort -->|Giá giảm| SortPriceDesc[ORDER BY price DESC]
    ApplySort -->|Tên A-Z| SortNameAsc[ORDER BY name ASC]
    ApplySort -->|Mới nhất| SortNewest[ORDER BY created_date DESC]
    ApplySort -->|Không| DefaultSort[ORDER BY id DESC]
    
    SortPriceAsc --> ExecuteQuery[Thực thi query<br/>lên database]
    SortPriceDesc --> ExecuteQuery
    SortNameAsc --> ExecuteQuery
    SortNewest --> ExecuteQuery
    DefaultSort --> ExecuteQuery
    
    ExecuteQuery --> GetResults[Lấy kết quả<br/>sản phẩm]
    GetResults --> CheckEmpty{Có kết quả?}
    CheckEmpty -->|Không| ShowNoResults[Hiển thị thông báo:<br/>Không tìm thấy sản phẩm]
    ShowNoResults --> SuggestClear[Gợi ý xóa bộ lọc<br/>hoặc tìm kiếm khác]
    SuggestClear --> End1([Hiển thị trang<br/>không có kết quả])
    
    CheckEmpty -->|Có| CountTotal[Đếm tổng số<br/>kết quả]
    CountTotal --> Paginate[Phân trang:<br/>20 sản phẩm/trang]
    Paginate --> DisplayResults[Hiển thị kết quả<br/>với highlight từ khóa]
    DisplayResults --> ShowFilters[Hiển thị bộ lọc<br/>đang áp dụng]
    ShowFilters --> ShowCount[Hiển thị: Tìm thấy<br/>X sản phẩm]
    ShowCount --> End2([Hiển thị trang<br/>kết quả tìm kiếm])
    
    End2 --> UserAction2{Hành động<br/>tiếp theo?}
    UserAction2 -->|Xóa bộ lọc| ClearFilters[Xóa tất cả<br/>bộ lọc]
    ClearFilters --> ShowPage
    UserAction2 -->|Chỉnh sửa| UserAction
    UserAction2 -->|Xem sản phẩm| ViewProduct[Xem chi tiết<br/>sản phẩm]
    
    style Start fill:#E8F5E9
    style End1 fill:#FFEBEE
    style End2 fill:#C8E6C9
    style UserAction fill:#FFF9C4
    style CheckKeyword fill:#FFF9C4
    style CheckCategory fill:#FFF9C4
    style CheckPriceRange fill:#FFF9C4
    style ApplySort fill:#FFF9C4
    style CheckEmpty fill:#FFF9C4
    style UserAction2 fill:#FFF9C4
```

---

## 10. ĐÁNH GIÁ SẢN PHẨM

### 10.1. Người Dùng Đánh Giá Sản Phẩm

```mermaid
flowchart TD
    Start([Người dùng truy cập<br/>trang chi tiết sản phẩm])
    Start --> LoadProduct[Load thông tin<br/>sản phẩm]
    LoadProduct --> LoadReviews[Load tất cả đánh giá<br/>của sản phẩm]
    LoadReviews --> CalcAvg[Tính điểm trung bình<br/>và tổng số đánh giá]
    CalcAvg --> DisplayProduct[Hiển thị sản phẩm<br/>với đánh giá]
    
    DisplayProduct --> CheckAuth{Người dùng<br/>đã đăng nhập?}
    CheckAuth -->|Không| ShowLoginPrompt[Hiển thị: Đăng nhập<br/>để đánh giá]
    ShowLoginPrompt --> End1([Chỉ xem đánh giá])
    
    CheckAuth -->|Có| CheckPurchased{Đã mua<br/>sản phẩm này?}
    CheckPurchased -->|Không| ShowPurchaseReq[Hiển thị: Cần mua<br/>sản phẩm để đánh giá]
    ShowPurchaseReq --> End2([Chỉ xem đánh giá])
    
    CheckPurchased -->|Có| CheckReviewed{Đã đánh giá<br/>rồi?}
    CheckReviewed -->|Có| ShowEditOption[Hiển thị nút<br/>Sửa đánh giá]
    ShowEditOption --> UserChoose{Người dùng<br/>chọn?}
    UserChoose -->|Xem thôi| End3([Xem đánh giá])
    UserChoose -->|Sửa| LoadOldReview[Load đánh giá<br/>cũ của user]
    LoadOldReview --> ShowReviewForm
    
    CheckReviewed -->|Không| ShowReviewBtn[Hiển thị nút<br/>Viết đánh giá]
    ShowReviewBtn --> UserClick[Người dùng click<br/>Viết đánh giá]
    UserClick --> ShowReviewForm[Hiển thị form<br/>đánh giá]
    
    ShowReviewForm --> SelectRating[Chọn số sao:<br/>1-5 sao]
    SelectRating --> WriteComment[Viết nhận xét<br/>về sản phẩm]
    WriteComment --> UploadPhotos{Upload ảnh<br/>sản phẩm?}
    UploadPhotos -->|Có| SelectPhotos[Chọn ảnh<br/>tối đa 5 ảnh]
    SelectPhotos --> ValidatePhotos{Ảnh hợp lệ?<br/>JPG/PNG, < 2MB}
    ValidatePhotos -->|Không| ShowPhotoErr[Hiển thị lỗi<br/>ảnh không hợp lệ]
    ShowPhotoErr --> UploadPhotos
    ValidatePhotos -->|Có| PreviewPhotos[Preview ảnh<br/>đã chọn]
    PreviewPhotos --> SubmitReview[Người dùng submit<br/>đánh giá]
    UploadPhotos -->|Không| SubmitReview
    
    SubmitReview --> ValidateReview{Validation<br/>passed?}
    ValidateReview -->|Không| ShowReviewErr["Hiển thị lỗi:<br/>- Chưa chọn sao<br/>- Nhận xét quá ngắn"]
    ShowReviewErr --> ShowReviewForm
    
    ValidateReview -->|Có| SavePhotos{Có ảnh<br/>để lưu?}
    SavePhotos -->|Có| UploadToServer[Upload ảnh<br/>lên server]
    UploadToServer --> SaveReview[Lưu đánh giá<br/>vào database]
    SavePhotos -->|Không| SaveReview
    
    SaveReview --> SavePhotoRecords[Lưu thông tin ảnh<br/>vào review_images]
    SavePhotoRecords --> UpdateAvgRating[Cập nhật điểm TB<br/>của sản phẩm]
    UpdateAvgRating --> SendNotif[Gửi thông báo<br/>cho admin/seller]
    SendNotif --> ShowSuccess[Hiển thị thông báo<br/>đánh giá thành công]
    ShowSuccess --> End4([Làm mới trang<br/>sản phẩm])
    
    style Start fill:#E8F5E9
    style End1 fill:#FFEBEE
    style End2 fill:#FFEBEE
    style End3 fill:#C8E6C9
    style End4 fill:#C8E6C9
    style CheckAuth fill:#FFF9C4
    style CheckPurchased fill:#FFF9C4
    style CheckReviewed fill:#FFF9C4
    style UserChoose fill:#FFF9C4
    style UploadPhotos fill:#FFF9C4
    style ValidatePhotos fill:#FFF9C4
    style ValidateReview fill:#FFF9C4
    style SavePhotos fill:#FFF9C4
```

---

## 11. XỬ LÝ THANH TOÁN

### 11.1. Quy Trình Thanh Toán Đơn Hàng

```mermaid
flowchart TD
    Start([Người dùng click<br/>Thanh toán])
    Start --> CheckAuth{Đã đăng<br/>nhập?}
    CheckAuth -->|Không| End1([Chuyển đến<br/>/login])
    
    CheckAuth -->|Có| CheckCart{Giỏ hàng<br/>có sản phẩm?}
    CheckCart -->|Không| ShowEmptyCart[Hiển thị: Giỏ hàng<br/>trống]
    ShowEmptyCart --> End2([Quay lại<br/>trang sản phẩm])
    
    CheckCart -->|Có| LoadUserInfo[Load thông tin<br/>người dùng]
    LoadUserInfo --> ShowCheckoutForm[Hiển thị form<br/>thanh toán]
    ShowCheckoutForm --> FillInfo["Điền/Xác nhận:<br/>- Họ tên<br/>- Số điện thoại<br/>- Địa chỉ giao hàng"]
    FillInfo --> SelectPayment{Chọn phương thức<br/>thanh toán?}
    
    SelectPayment -->|COD| SelectCOD[Thanh toán khi<br/>nhận hàng - COD]
    SelectPayment -->|VNPay| SelectVNPay[Thanh toán online<br/>qua VNPay]
    SelectPayment -->|MoMo| SelectMoMo[Thanh toán online<br/>qua MoMo]
    
    SelectCOD --> AddNote[Thêm ghi chú<br/>đơn hàng - tùy chọn]
    SelectVNPay --> AddNote
    SelectMoMo --> AddNote
    
    AddNote --> ReviewOrder[Xem lại đơn hàng:<br/>Sản phẩm, Số lượng,<br/>Tổng tiền]
    ReviewOrder --> ConfirmOrder{Xác nhận<br/>đặt hàng?}
    ConfirmOrder -->|Không| End3([Hủy thanh toán])
    
    ConfirmOrder -->|Có| ValidateForm{Validation<br/>passed?}
    ValidateForm -->|Không| ShowFormErr["Hiển thị lỗi:<br/>- Thiếu thông tin<br/>- SĐT không hợp lệ<br/>- Địa chỉ trống"]
    ShowFormErr --> ShowCheckoutForm
    
    ValidateForm -->|Có| CheckStock{Kiểm tra<br/>tồn kho?}
    CheckStock -->|Hết hàng| ShowStockErr[Hiển thị: Sản phẩm<br/>đã hết hàng]
    ShowStockErr --> End4([Quay lại<br/>giỏ hàng])
    
    CheckStock -->|Đủ hàng| CreateOrder[Tạo đơn hàng<br/>trong database]
    CreateOrder --> SaveOrderItems[Lưu chi tiết<br/>đơn hàng]
    SaveOrderItems --> UpdateStock[Trừ số lượng<br/>tồn kho]
    UpdateStock --> CheckPaymentMethod{Phương thức<br/>thanh toán?}
    
    CheckPaymentMethod -->|COD| SetStatusPending[Đặt status =<br/>PENDING]
    SetStatusPending --> ClearCart[Xóa giỏ hàng]
    ClearCart --> SendConfirmEmail[Gửi email<br/>xác nhận đơn hàng]
    SendConfirmEmail --> ShowSuccessCOD[Hiển thị: Đặt hàng<br/>thành công - COD]
    ShowSuccessCOD --> End5([Chuyển đến trang<br/>đơn hàng thành công])
    
    CheckPaymentMethod -->|VNPay/MoMo| SetStatusWaiting[Đặt status =<br/>WAITING_PAYMENT]
    SetStatusWaiting --> RedirectPayment[Chuyển hướng đến<br/>cổng thanh toán]
    RedirectPayment --> UserPay[Người dùng thanh toán<br/>trên cổng]
    UserPay --> PaymentCallback{Kết quả<br/>thanh toán?}
    
    PaymentCallback -->|Thành công| UpdateStatusPaid[Cập nhật status =<br/>PAID]
    UpdateStatusPaid --> ClearCart2[Xóa giỏ hàng]
    ClearCart2 --> SendConfirmEmail2[Gửi email xác nhận<br/>đã thanh toán]
    SendConfirmEmail2 --> ShowSuccessPaid[Hiển thị: Thanh toán<br/>thành công]
    ShowSuccessPaid --> End6([Chuyển đến trang<br/>đơn hàng thành công])
    
    PaymentCallback -->|Thất bại| UpdateStatusFailed[Cập nhật status =<br/>PAYMENT_FAILED]
    UpdateStatusFailed --> RestoreStock[Hoàn lại<br/>tồn kho]
    RestoreStock --> ShowPaymentErr[Hiển thị: Thanh toán<br/>thất bại]
    ShowPaymentErr --> End7([Quay lại<br/>trang thanh toán])
    
    style Start fill:#E8F5E9
    style End1 fill:#FFEBEE
    style End2 fill:#FFEBEE
    style End3 fill:#FFEBEE
    style End4 fill:#FFEBEE
    style End5 fill:#C8E6C9
    style End6 fill:#C8E6C9
    style End7 fill:#FFEBEE
    style CheckAuth fill:#FFF9C4
    style CheckCart fill:#FFF9C4
    style SelectPayment fill:#FFF9C4
    style ConfirmOrder fill:#FFF9C4
    style ValidateForm fill:#FFF9C4
    style CheckStock fill:#FFF9C4
    style CheckPaymentMethod fill:#FFF9C4
    style PaymentCallback fill:#FFF9C4
```

---

## 12. THEO DÕI ĐƠN HÀNG

### 12.1. Người Dùng Theo Dõi Trạng Thái Đơn Hàng

```mermaid
    flowchart TD
        Start([Người dùng truy cập<br/>/account/orders])
        Start --> CheckAuth{Đã đăng<br/>nhập?}
        CheckAuth -->|Không| End1([Chuyển đến<br/>/login])
        
        CheckAuth -->|Có| LoadOrders[Load tất cả đơn hàng<br/>của người dùng]
        LoadOrders --> SortOrders[Sắp xếp theo<br/>ngày tạo giảm dần]
        SortOrders --> CheckEmpty{Có đơn<br/>hàng?}
        CheckEmpty -->|Không| ShowEmpty[Hiển thị: Bạn chưa<br/>có đơn hàng nào]
        ShowEmpty --> SuggestShop[Gợi ý: Bắt đầu<br/>mua sắm]
        SuggestShop --> End2([Hiển thị trang<br/>đơn hàng trống])
        
        CheckEmpty -->|Có| GroupByStatus[Nhóm đơn hàng<br/>theo trạng thái]
        GroupByStatus --> DisplayTabs["Hiển thị tabs:<br/>- Tất cả<br/>- Chờ xác nhận<br/>- Đang giao<br/>- Đã giao<br/>- Đã hủy"]
        DisplayTabs --> ShowOrders[Hiển thị danh sách<br/>đơn hàng]
        
        ShowOrders --> UserAction{Hành động<br/>người dùng?}
        
        UserAction -->|Xem chi tiết| SelectOrder[Chọn đơn hàng<br/>để xem]
        SelectOrder --> LoadDetail[Load chi tiết<br/>đơn hàng]
        LoadDetail --> ShowDetail["Hiển thị:<br/>- Thông tin sản phẩm<br/>- Địa chỉ giao hàng<br/>- Phương thức thanh toán<br/>- Trạng thái hiện tại"]
        ShowDetail --> LoadTimeline[Load timeline<br/>trạng thái]
        LoadTimeline --> DisplayTimeline["Hiển thị timeline:<br/>1. Đã đặt hàng<br/>2. Đã xác nhận<br/>3. Đang giao<br/>4. Đã giao"]
        DisplayTimeline --> CheckStatus{Trạng thái<br/>đơn hàng?}
        
        CheckStatus -->|PENDING| ShowPendingActions[Hiển thị: Hủy đơn hàng]
        CheckStatus -->|CONFIRMED| ShowConfirmedActions[Hiển thị: Liên hệ shop]
        CheckStatus -->|SHIPPING| ShowShippingInfo[Hiển thị: Mã vận đơn,<br/>Theo dõi shipper]
        CheckStatus -->|DELIVERED| ShowDeliveredActions[Hiển thị: Đánh giá,<br/>Mua lại]
        CheckStatus -->|CANCELLED| ShowCancelledInfo[Hiển thị: Lý do hủy,<br/>Mua lại]
        
        ShowPendingActions --> ActionChoice{Chọn<br/>hành động?}
        ShowConfirmedActions --> ActionChoice
        ShowShippingInfo --> ActionChoice
        ShowDeliveredActions --> ActionChoice
        ShowCancelledInfo --> ActionChoice
        
        ActionChoice -->|Hủy đơn| ConfirmCancel{Xác nhận<br/>hủy?}
        ConfirmCancel -->|Không| End3([Quay lại<br/>chi tiết])
        ConfirmCancel -->|Có| EnterReason[Nhập lý do<br/>hủy đơn]
        EnterReason --> UpdateCancel[Cập nhật status =<br/>CANCELLED]
        UpdateCancel --> RestoreStock[Hoàn lại<br/>tồn kho]
        RestoreStock --> SendCancelNotif[Gửi thông báo<br/>cho admin]
        SendCancelNotif --> ShowCancelSuccess[Hiển thị: Đã hủy<br/>đơn hàng]
        ShowCancelSuccess --> End4([Làm mới danh sách<br/>đơn hàng])
        
        ActionChoice -->|Đánh giá| CheckReviewed{Đã đánh giá<br/>sản phẩm?}
        CheckReviewed -->|Có| ShowReviewed[Hiển thị: Đã đánh giá]
        ShowReviewed --> End5([Xem đánh giá])
        CheckReviewed -->|Không| RedirectReview[Chuyển đến trang<br/>đánh giá sản phẩm]
        RedirectReview --> End6([Viết đánh giá])
        
        ActionChoice -->|Mua lại| AddToCart[Thêm sản phẩm<br/>vào giỏ hàng]
        AddToCart --> ShowAddSuccess[Hiển thị: Đã thêm<br/>vào giỏ hàng]
        ShowAddSuccess --> End7([Chuyển đến<br/>giỏ hàng])
        
        ActionChoice -->|Theo dõi shipper| ShowMap[Hiển thị bản đồ<br/>vị trí shipper]
        ShowMap --> ShowETA[Hiển thị thời gian<br/>giao hàng dự kiến]
        ShowETA --> End8([Theo dõi<br/>real-time])
        
        UserAction -->|Lọc theo trạng thái| FilterStatus[Chọn tab<br/>trạng thái]
        FilterStatus --> ShowOrders
        
        style Start fill:#E8F5E9
        style End1 fill:#FFEBEE
        style End2 fill:#FFEBEE
        style End3 fill:#C8E6C9
        style End4 fill:#C8E6C9
        style End5 fill:#C8E6C9
        style End6 fill:#C8E6C9
        style End7 fill:#C8E6C9
        style End8 fill:#C8E6C9
        style CheckAuth fill:#FFF9C4
        style CheckEmpty fill:#FFF9C4
        style UserAction fill:#FFF9C4
        style CheckStatus fill:#FFF9C4
        style ActionChoice fill:#FFF9C4
        style ConfirmCancel fill:#FFF9C4
        style CheckReviewed fill:#FFF9C4
```

---

## 13. QUẢN LÝ NGƯỜI DÙNG (ADMIN)

### 13.1. Admin Quản Lý Tài Khoản Người Dùng

```mermaid
flowchart TD
    Start([Admin truy cập<br/>/admin/accounts])
    Start --> CheckAuth{Đã xác thực<br/>& ADMIN?}
    CheckAuth -->|Không| End1([Access Denied])
    
    CheckAuth -->|Có| LoadAccounts[Load tất cả<br/>tài khoản]
    LoadAccounts --> FilterRole{Lọc theo<br/>vai trò?}
    FilterRole -->|Tất cả| ShowAll[Hiển thị tất cả<br/>tài khoản]
    FilterRole -->|USER| ShowUsers[Hiển thị chỉ<br/>tài khoản USER]
    FilterRole -->|ADMIN| ShowAdmins[Hiển thị chỉ<br/>tài khoản ADMIN]
    
    ShowAll --> SortAccounts[Sắp xếp theo<br/>ngày tạo/tên]
    ShowUsers --> SortAccounts
    ShowAdmins --> SortAccounts
    
    SortAccounts --> DisplayList["Hiển thị danh sách với:<br/>- Avatar<br/>- Tên, Email<br/>- Vai trò<br/>- Trạng thái Active<br/>- Ngày tạo"]
    
    DisplayList --> SearchBox[Hiển thị ô tìm kiếm:<br/>Tên, Email, SĐT]
    SearchBox --> AdminAction{Hành động<br/>của Admin?}
    
    AdminAction -->|Tìm kiếm| EnterSearch[Nhập từ khóa<br/>tìm kiếm]
    EnterSearch --> SearchDB[Tìm trong database<br/>theo tên/email/phone]
    SearchDB --> ShowResults[Hiển thị kết quả<br/>tìm kiếm]
    ShowResults --> AdminAction
    
    AdminAction -->|Xem chi tiết| SelectUser[Chọn tài khoản<br/>để xem]
    SelectUser --> LoadUserDetail[Load thông tin<br/>chi tiết]
    LoadUserDetail --> ShowUserInfo["Hiển thị:<br/>- Thông tin cá nhân<br/>- Lịch sử đơn hàng<br/>- Số lần đăng nhập thất bại<br/>- Thời gian khóa (nếu có)"]
    ShowUserInfo --> DetailAction{Hành động<br/>chi tiết?}
    
    DetailAction -->|Quay lại| DisplayList
    DetailAction -->|Sửa| EditUser
    DetailAction -->|Khóa/Mở khóa| ToggleActive
    DetailAction -->|Reset mật khẩu| ResetPassword
    
    AdminAction -->|Sửa thông tin| EditUser[Load form sửa<br/>thông tin]
    EditUser --> UpdateFields["Cập nhật:<br/>- Họ tên<br/>- Số điện thoại<br/>- Vai trò<br/>- Trạng thái"]
    UpdateFields --> ValidateEdit{Validation<br/>passed?}
    ValidateEdit -->|Không| ShowEditErr[Hiển thị lỗi<br/>validation]
    ShowEditErr --> EditUser
    ValidateEdit -->|Có| SaveChanges[Lưu thay đổi<br/>vào database]
    SaveChanges --> LogAction1[Ghi log: Admin sửa<br/>thông tin user]
    LogAction1 --> ShowEditSuccess[Hiển thị: Cập nhật<br/>thành công]
    ShowEditSuccess --> End2([Làm mới danh sách])
    
    AdminAction -->|Khóa/Mở khóa| ToggleActive{Trạng thái<br/>hiện tại?}
    ToggleActive -->|Active| ConfirmDeactivate{Xác nhận<br/>khóa tài khoản?}
    ConfirmDeactivate -->|Không| End3([Hủy])
    ConfirmDeactivate -->|Có| SetInactive[Đặt isActive<br/>= false]
    SetInactive --> LogoutUser[Đăng xuất user<br/>khỏi hệ thống]
    LogoutUser --> LogAction2[Ghi log: Admin khóa<br/>tài khoản]
    LogAction2 --> SendNotif1[Gửi email thông báo<br/>cho user]
    SendNotif1 --> ShowDeactivateSuccess[Hiển thị: Đã khóa<br/>tài khoản]
    ShowDeactivateSuccess --> End4([Làm mới danh sách])
    
    ToggleActive -->|Inactive| ConfirmActivate{Xác nhận<br/>mở khóa?}
    ConfirmActivate -->|Không| End5([Hủy])
    ConfirmActivate -->|Có| SetActive[Đặt isActive<br/>= true]
    SetActive --> ResetAttempts[Reset failed_login_attempts<br/>= 0]
    ResetAttempts --> ClearLock[Xóa account_locked_until]
    ClearLock --> LogAction3[Ghi log: Admin mở khóa<br/>tài khoản]
    LogAction3 --> SendNotif2[Gửi email thông báo<br/>cho user]
    SendNotif2 --> ShowActivateSuccess[Hiển thị: Đã mở khóa<br/>tài khoản]
    ShowActivateSuccess --> End6([Làm mới danh sách])
    
    AdminAction -->|Reset mật khẩu| ResetPassword[Tạo mật khẩu<br/>tạm thời]
    ResetPassword --> HashTempPass[Hash mật khẩu<br/>tạm thời]
    HashTempPass --> UpdatePassword[Cập nhật password<br/>trong database]
    UpdatePassword --> SetForceChange[Đặt cờ: Bắt buộc<br/>đổi mật khẩu]
    SetForceChange --> LogAction4[Ghi log: Admin reset<br/>mật khẩu]
    LogAction4 --> SendTempPass[Gửi email mật khẩu<br/>tạm thời cho user]
    SendTempPass --> ShowResetSuccess[Hiển thị: Đã reset<br/>mật khẩu]
    ShowResetSuccess --> End7([Làm mới danh sách])
    
    AdminAction -->|Xóa tài khoản| ConfirmDelete{Xác nhận<br/>xóa?}
    ConfirmDelete -->|Không| End8([Hủy])
    ConfirmDelete -->|Có| CheckOrders{Tài khoản có<br/>đơn hàng?}
    CheckOrders -->|Có| ShowDeleteErr[Hiển thị lỗi: Không thể<br/>xóa tài khoản có đơn hàng]
    ShowDeleteErr --> SuggestDeactivate[Gợi ý: Khóa tài khoản<br/>thay vì xóa]
    SuggestDeactivate --> End9([Quay lại])
    
    CheckOrders -->|Không| DeleteAccount[Xóa tài khoản<br/>khỏi database]
    DeleteAccount --> DeleteRelated[Xóa dữ liệu liên quan:<br/>Reviews, Tokens]
    DeleteRelated --> LogAction5[Ghi log: Admin xóa<br/>tài khoản]
    LogAction5 --> ShowDeleteSuccess[Hiển thị: Đã xóa<br/>tài khoản]
    ShowDeleteSuccess --> End10([Làm mới danh sách])
    
    style Start fill:#E8F5E9
    style End1 fill:#FFEBEE
    style End2 fill:#C8E6C9
    style End3 fill:#FFEBEE
    style End4 fill:#C8E6C9
    style End5 fill:#FFEBEE
    style End6 fill:#C8E6C9
    style End7 fill:#C8E6C9
    style End8 fill:#FFEBEE
    style End9 fill:#FFEBEE
    style End10 fill:#C8E6C9
    style CheckAuth fill:#FFF9C4
    style FilterRole fill:#FFF9C4
    style AdminAction fill:#FFF9C4
    style DetailAction fill:#FFF9C4
    style ValidateEdit fill:#FFF9C4
    style ToggleActive fill:#FFF9C4
    style ConfirmDeactivate fill:#FFF9C4
    style ConfirmActivate fill:#FFF9C4
    style ConfirmDelete fill:#FFF9C4
    style CheckOrders fill:#FFF9C4
```

---

## HƯỚNG DẪN SỬ DỤNG

### Bước 1: Copy Code Mermaid
- Chọn một diagram bất kỳ ở trên
- Copy toàn bộ code trong khối ```mermaid ... ```

### Bước 2: Render Trên Mermaid Live
1. Truy cập https://mermaid.live/
2. Paste code vào editor bên trái
3. Diagram sẽ tự động hiển thị bên phải

### Bước 3: Export
- Click nút "Actions" ở góc trên bên phải
- Chọn "Download SVG" hoặc "Download PNG"
- Lưu file về máy

### Bước 4: Import Vào Draw.io
1. Mở https://app.diagrams.net/
2. File → Import → chọn file SVG vừa download
3. Chỉnh sửa nếu cần (màu sắc, vị trí, text)
4. File → Save as → chọn .drawio để lưu

### Lưu Ý
- Mermaid Live có thể bị giới hạn kích thước diagram
- Nếu diagram quá lớn, có thể chia nhỏ thành nhiều phần
- SVG import vào Draw.io có thể cần điều chỉnh layout
- Màu sắc đã được set sẵn trong code Mermaid

---

## TÓM TẮT

Tài liệu này chứa **20 Activity Diagrams** hoàn chỉnh bằng tiếng Việt:

### Nhóm 1: Xác Thực & Bảo Mật (3 diagrams)
✅ Đăng nhập với giới hạn số lần thử  
✅ Đăng ký với xác thực email  
✅ Đăng nhập OAuth2 (Facebook/Google)

### Nhóm 2: Email Verification (2 diagrams)
✅ Xác thực email từ link  
✅ Gửi lại email xác thực

### Nhóm 3: Password Reset (2 diagrams)
✅ Yêu cầu quên mật khẩu  
✅ Đặt lại mật khẩu với token

### Nhóm 4: Account Management (2 diagrams)
✅ Xem thông tin cá nhân  
✅ Cập nhật thông tin với avatar

### Nhóm 5: Admin Management (4 diagrams)
✅ Truy cập dashboard  
✅ Quản lý sản phẩm (CRUD cơ bản)  
✅ Quản lý đơn hàng & tài khoản (cơ bản)  
✅ **Quản lý người dùng** (CRUD chi tiết) - MỚI!

### Nhóm 6: Shopping (2 diagrams)
✅ Duyệt sản phẩm & xem chi tiết  
✅ Giỏ hàng & thanh toán (cơ bản)

### Nhóm 7: CRUD Chi Tiết & Tính Năng Nâng Cao (5 diagrams - MỚI!)
✅ **Quản lý danh mục** (CRUD đầy đủ)  
✅ **Quản lý sản phẩm** với upload hình ảnh  
✅ **Tìm kiếm & lọc** sản phẩm nâng cao  
✅ **Đánh giá sản phẩm** với upload ảnh  
✅ **Xử lý thanh toán** (COD, VNPay, MoMo)  
✅ **Theo dõi đơn hàng** với timeline trạng thái

---

**📊 Tổng cộng: 20 Activity Diagrams**

**⏱️ Thời gian ước tính**: 6-8 giờ để vẽ hoàn chỉnh tất cả các diagrams.

**💡 Lưu ý**: Bạn có thể vẽ từng diagram riêng lẻ hoặc gộp tất cả vào một file Draw.io với nhiều pages (tabs).

**🎯 Phù hợp cho**: Báo cáo đồ án tốt nghiệp Giai đoạn 1 - đầy đủ các chức năng CRUD và tính năng nâng cao!

Chúc bạn vẽ thành công! 🎨
