# Activity Diagrams - ShopOMG E-Commerce Project

This document contains comprehensive activity diagrams for all major features implemented in the ShopOMG Spring Boot application.

## Table of Contents

1. [Authentication Flows](#1-authentication-flows)
2. [Email Verification Flows](#2-email-verification-flows)
3. [Password Reset Flows](#3-password-reset-flows)
4. [Account Management Flows](#4-account-management-flows)
5. [Admin Management Flows](#5-admin-management-flows)
6. [Shopping Flows](#6-shopping-flows)

---

## 1. Authentication Flows

### 1.1 User Login with Attempt Limiting

This diagram shows the complete login flow including failed login tracking, account lockout after 5 failed attempts, and 15-minute automatic unlock.

```mermaid
flowchart TD
    Start([User accesses /login]) --> CheckEmail{Email parameter provided?}
    CheckEmail -->|Yes| GetRemaining[Display remaining attempts]
    CheckEmail -->|No| ShowForm[Show login form]
    GetRemaining --> CheckLocked{Account locked?}
    CheckLocked -->|Yes| ShowLockTime[Show minutes until unlock]
    CheckLocked -->|No| ShowForm
    ShowLockTime --> ShowForm
    
    ShowForm --> UserSubmit[User submits credentials]
    UserSubmit --> ValidateInput{Input valid?}
    ValidateInput -->|No| ShowError[Show validation error]
    ShowError --> ShowForm
    
    ValidateInput -->|Yes| CheckAccountLocked{Account locked?}
    CheckAccountLocked -->|Yes| CalculateTime[Calculate remaining lock time]
    CalculateTime --> ShowLockedError[Show locked error with time]
    ShowLockedError --> End1([Return to login])
    
    CheckAccountLocked -->|No| CheckEmailVerified{Email verified?}
    CheckEmailVerified -->|No| ShowVerifyError[Show email verification required]
    ShowVerifyError --> End2([Redirect to verification])
    
    CheckEmailVerified -->|Yes| AuthenticateUser{Credentials correct?}
    AuthenticateUser -->|No| RecordFailed[Record failed attempt]
    RecordFailed --> IncrementCounter[Increment failed_login_attempts]
    IncrementCounter --> CheckLimit{Attempts >= 5?}
    CheckLimit -->|Yes| LockAccount[Set account_locked_until = now + 15min]
    CheckLimit -->|No| ShowFailError[Show error with remaining attempts]
    LockAccount --> ShowLockedMsg[Show account locked message]
    ShowLockedMsg --> End3([Return to login])
    ShowFailError --> End4([Return to login])
    
    AuthenticateUser -->|Yes| RecordSuccess[Record successful login]
    RecordSuccess --> ResetAttempts[Reset failed_login_attempts = 0]
    ResetAttempts --> ClearLock[Clear account_locked_until]
    ClearLock --> UpdateLastLogin[Update last_login timestamp]
    UpdateLastLogin --> CreateSession[Create security session]
    CreateSession --> CheckRole{User role?}
    CheckRole -->|ADMIN| RedirectAdmin([Redirect to /admin/dashboard])
    CheckRole -->|USER| RedirectHome([Redirect to /home])
```

**Key Features:**
- Maximum 5 login attempts before lockout
- 15-minute automatic lock duration
- Real-time display of remaining attempts
- Email verification check
- Role-based redirection after successful login

---

### 1.2 User Registration with Email Verification

```mermaid
flowchart TD
    Start([User accesses /account/sign-up]) --> ShowRegForm[Show registration form]
    ShowRegForm --> UserFill[User fills form: username, fullName, email, password, phone]
    UserFill --> UserSubmit[User submits registration]
    UserSubmit --> ValidateInput{Input validation passed?}
    ValidateInput -->|No| ShowErrors[Show validation errors]
    ShowErrors --> ShowRegForm
    
    ValidateInput -->|Yes| CheckPasswordMatch{password == confirmPassword?}
    CheckPasswordMatch -->|No| ShowPasswordError[Show mismatch error]
    ShowPasswordError --> ShowRegForm
    
    CheckPasswordMatch -->|Yes| CheckPasswordStrength{Password meets strength requirements?}
    CheckPasswordStrength -->|No| ShowStrengthError[Show password strength error]
    ShowStrengthError --> ShowRegForm
    
    CheckPasswordStrength -->|Yes| CheckEmailExists{Email already exists?}
    CheckEmailExists -->|Yes| ShowEmailError[Show email exists error]
    ShowEmailError --> ShowRegForm
    
    CheckEmailExists -->|No| CheckUsernameExists{Username already exists?}
    CheckUsernameExists -->|Yes| ShowUsernameError[Show username exists error]
    ShowUsernameError --> ShowRegForm
    
    CheckUsernameExists -->|No| CreateAccount[Create account with emailVerified = false]
    CreateAccount --> HashPassword[Hash password using BCrypt]
    HashPassword --> SetRole[Set role = USER, isActive = true]
    SetRole --> SaveAccount[Save account to database]
    SaveAccount --> GenerateToken[Generate UUID verification token]
    GenerateToken --> SetExpiry[Set token expiry = now + 24 hours]
    SetExpiry --> SaveToken[Save token to email_verification_tokens]
    SaveToken --> BuildEmail[Build verification email with token link]
    BuildEmail --> SendEmail[Send verification email]
    SendEmail --> RedirectSuccess([Redirect to /verify-email-sent])
```

**Key Features:**
- Password strength validation (min 8 chars, uppercase, lowercase, number, special char)
- Email and username uniqueness checks
- BCrypt password hashing
- 24-hour verification token validity
- Professional email template with verification link

---

### 1.3 OAuth2 Social Login (Facebook/Google)

```mermaid
flowchart TD
    Start([User clicks social login button]) --> RedirectOAuth[Redirect to OAuth provider]
    RedirectOAuth --> UserAuth[User authenticates with provider]
    UserAuth --> ProviderAuth{Authentication successful?}
    ProviderAuth -->|No| ShowError([Show authentication error])
    
    ProviderAuth -->|Yes| ReceiveCallback[Receive OAuth2 callback]
    ReceiveCallback --> ExtractInfo[Extract user info: email, name, ID]
    ExtractInfo --> CheckEmail{Email available?}
    CheckEmail -->|No| UseProviderID[Use provider ID as identifier]
    CheckEmail -->|Yes| UseEmail[Use email as identifier]
    
    UseEmail --> FindAccount[Search account by email]
    UseProviderID --> FindByID[Search account by username = provider ID]
    
    FindAccount --> AccountExists{Account found?}
    FindByID --> AccountExists
    
    AccountExists -->|Yes| UpdateLastLogin[Update last_login]
    UpdateLastLogin --> CreateSession[Create security session]
    CreateSession --> RedirectHome([Redirect to /home])
    
    AccountExists -->|No| CreateNewAccount[Create new account]
    CreateNewAccount --> SetOAuthFields["Set username = provider ID<br/>email = email (if available)<br/>fullName = name<br/>emailVerified = true"]
    SetOAuthFields --> SetUserRole[Set role = USER, isActive = true]
    SetUserRole --> SaveNewAccount[Save account to database]
    SaveNewAccount --> CreateNewSession[Create security session]
    CreateNewSession --> RedirectNewUser([Redirect to /home])
```

**Key Features:**
- Supports Facebook and Google OAuth2
- Auto-creates account for first-time OAuth users
- Auto-verifies email for OAuth accounts
- Seamless login for existing users
- Provider ID fallback when email not available

---

## 2. Email Verification Flows

### 2.1 Email Verification from Link

```mermaid
flowchart TD
    Start([User clicks link in email]) --> ParseURL[Parse /verify-email?token=xxx]
    ParseURL --> CheckToken{Token parameter present?}
    CheckToken -->|No| ShowInvalidLink[Show invalid link error page]
    ShowInvalidLink --> End1([End])
    
    CheckToken -->|Yes| ExtractToken[Extract token string]
    ExtractToken --> QueryDB[Query email_verification_tokens]
    QueryDB --> TokenFound{Token exists in DB?}
    TokenFound -->|No| ShowExpiredError[Show expired/invalid token error]
    ShowExpiredError --> End2([End])
    
    TokenFound -->|Yes| CheckExpiry{Token expiry > now?}
    CheckExpiry -->|No| DeleteToken[Delete expired token]
    DeleteToken --> ShowExpiredError
    
    CheckExpiry -->|Yes| GetAccount[Get associated account]
    GetAccount --> UpdateAccount[Set emailVerified = true]
    UpdateAccount --> SaveAccount[Save account to database]
    SaveAccount --> DeleteUsedToken[Delete used token]
    DeleteUsedToken --> ShowSuccess[Show verification success page]
    ShowSuccess --> ProvideLoginLink[Provide link to /login]
    ProvideLoginLink --> End3([User can now login])
```

**Key Features:**
- Token validity check (24-hour expiration)
- One-time use tokens (deleted after verification)
- Clear error messages for expired/invalid tokens
- Immediate account activation upon verification

---

### 2.2 Resend Verification Email

```mermaid
flowchart TD
    Start([User accesses /resend-verification]) --> ShowForm[Show email input form]
    ShowForm --> UserEnter[User enters email address]
    UserEnter --> UserSubmit[User submits form]
    UserSubmit --> ValidateEmail{Email format valid?}
    ValidateEmail -->|No| ShowFormatError[Show format error]
    ShowFormatError --> ShowForm
    
    ValidateEmail -->|Yes| FindAccount[Search account by email]
    FindAccount --> AccountExists{Account found?}
    AccountExists -->|No| ShowGenericSuccess[Show generic success message]
    ShowGenericSuccess --> End1([Redirect to /verify-email-sent])
    
    AccountExists -->|Yes| CheckVerified{Already verified?}
    CheckVerified -->|Yes| ShowGenericSuccess
    
    CheckVerified -->|No| DeleteOldTokens[Delete old verification tokens]
    DeleteOldTokens --> GenerateNewToken[Generate new UUID token]
    GenerateNewToken --> SetNewExpiry[Set expiry = now + 24 hours]
    SetNewExpiry --> SaveNewToken[Save token to database]
    SaveNewToken --> BuildResendEmail[Build verification email]
    BuildResendEmail --> SendResendEmail[Send email with new token]
    SendResendEmail --> ShowResendSuccess[Show success message]
    ShowResendSuccess --> End2([Redirect to /verify-email-sent])
```

**Key Features:**
- Generic success message (security: no email enumeration)
- Old tokens cleanup before generating new one
- 24-hour validity for new token
- Same email template as initial registration

---

## 3. Password Reset Flows

### 3.1 Forgot Password Request

```mermaid
flowchart TD
    Start([User accesses /forgot-password]) --> ShowForm[Show email input form]
    ShowForm --> UserEnter[User enters email]
    UserEnter --> UserSubmit[User submits form]
    UserSubmit --> ValidateInput{Email format valid?}
    ValidateInput -->|No| ShowFormatError[Show validation error]
    ShowFormatError --> ShowForm
    
    ValidateInput -->|Yes| FindAccount[Search account by email]
    FindAccount --> AccountFound{Account exists?}
    AccountFound -->|No| ShowGenericMsg[Show generic success message]
    ShowGenericMsg --> End1([End - security: no email enumeration])
    
    AccountFound -->|Yes| CheckActive{Account active?}
    CheckActive -->|No| ShowGenericMsg
    
    CheckActive -->|Yes| DeleteOldTokens[Delete old password reset tokens]
    DeleteOldTokens --> GenerateToken[Generate UUID reset token]
    GenerateToken --> SetExpiry[Set expiry = now + 1 hour]
    SetExpiry --> SetUsedFlag[Set used = false]
    SetUsedFlag --> SaveToken[Save token to password_reset_tokens]
    SaveToken --> BuildEmail[Build password reset email]
    BuildEmail --> SendEmail[Send email with reset link]
    SendEmail --> ShowSuccess[Show generic success message]
    ShowSuccess --> End2([User checks email])
```

**Key Features:**
- Email enumeration protection (always shows success message)
- 1-hour token expiration
- Old tokens cleanup
- Professional HTML email template
- Secure token generation using UUID

---

### 3.2 Reset Password with Token

```mermaid
flowchart TD
    Start([User clicks link: /reset-password?token=xxx]) --> CheckTokenParam{Token parameter present?}
    CheckTokenParam -->|No| ShowInvalidError[Show invalid link error]
    ShowInvalidError --> RedirectLogin1([Redirect to /login])
    
    CheckTokenParam -->|Yes| ValidateToken[Query password_reset_tokens]
    ValidateToken --> TokenExists{Token found?}
    TokenExists -->|No| ShowExpiredError[Show expired/invalid error]
    ShowExpiredError --> RedirectForgot1([Redirect to /forgot-password])
    
    TokenExists -->|Yes| CheckExpiry{expiry_date > now?}
    CheckExpiry -->|No| ShowExpiredError
    
    CheckExpiry -->|Yes| CheckUsed{Token used == false?}
    CheckUsed -->|No| ShowUsedError[Show already used error]
    CheckUsedError --> RedirectForgot2([Redirect to /forgot-password])
    
    CheckUsed -->|Yes| ShowResetForm[Show reset password form]
    ShowResetForm --> UserFillPass[User enters new password]
    UserFillPass --> UserSubmit[User submits form]
    UserSubmit --> ValidatePass{Validation passed?}
    ValidatePass -->|No| ShowValidationError[Show validation errors]
    ShowValidationError --> ShowResetForm
    
    ValidatePass -->|Yes| CheckStrength{Password strength OK?}
    CheckStrength -->|No| ShowStrengthError[Show strength requirements]
    ShowStrengthError --> ShowResetForm
    
    CheckStrength -->|Yes| CheckMatch{newPassword == confirmPassword?}
    CheckMatch -->|No| ShowMatchError[Show mismatch error]
    ShowMatchError --> ShowResetForm
    
    CheckMatch -->|Yes| RevalidateToken[Validate token again]
    RevalidateToken --> StillValid{Still valid?}
    StillValid -->|No| ShowExpiredError
    
    StillValid -->|Yes| HashPassword[Hash new password with BCrypt]
    HashPassword --> UpdatePassword[Update account password]
    UpdatePassword --> SaveAccount[Save account]
    SaveAccount --> MarkUsed[Set token.used = true]
    MarkUsed --> SaveToken[Save token]
    SaveToken --> ShowSuccess[Show success message]
    ShowSuccess --> RedirectLogin2([Redirect to /login])
```

**Key Features:**
- Token validation (expiry, used flag)
- Password strength validation
- Re-validation before password change (prevent race conditions)
- BCrypt password hashing
- One-time token usage

---

## 4. Account Management Flows

### 4.1 View User Profile

```mermaid
flowchart TD
    Start([User accesses /account/profile]) --> GetPrincipal[Get authenticated principal]
    GetPrincipal --> CheckAuthType{OAuth2 or Form auth?}
    CheckAuthType -->|OAuth2| ExtractOAuthEmail[Extract email from OAuth2 token]
    CheckAuthType -->|Form| ExtractUsername[Extract username from principal]
    
    ExtractOAuthEmail --> SearchByEmail[Search account by email]
    SearchByEmail --> AccountFound1{Account found?}
    ExtractUsername --> SearchByUsername[Search account by username]
    SearchByUsername --> AccountFound1
    
    AccountFound1 -->|No| RedirectError([Redirect to /login?error=true])
    AccountFound1 -->|Yes| LoadAccount[Load account details]
    LoadAccount --> CreateForm[Create ProfileForm DTO]
    CreateForm --> PopulateFields["Populate: username, fullName,<br/>phone, email, avatarUrl,<br/>birthDate, gender"]
    PopulateFields --> AddToModel[Add profileForm to model]
    AddToModel --> SetActivePage[Set activePage = profile]
    SetActivePage --> RenderView([Render user/account-profile.html])
```

**Key Features:**
- Support for both Form and OAuth2 authentication
- Fallback search (email first, then username)
- Pre-populated form with current data
- Read-only email display

---

### 4.2 Update User Profile with Avatar

```mermaid
flowchart TD
    Start([User submits profile update form]) --> GetFormData[Receive ProfileForm data + avatarFile]
    GetFormData --> ValidateInput{Input validation passed?}
    ValidateInput -->|No| ShowErrors[Show validation errors on form]
    ShowErrors --> ReturnView([Return to profile view])
    
    ValidateInput -->|Yes| GetPrincipal[Get authenticated principal]
    GetPrincipal --> FindAccount[Find account using helper method]
    FindAccount --> AccountFound{Account found?}
    AccountFound -->|No| RedirectError([Redirect to /login?error=true])
    
    AccountFound -->|Yes| UpdateBasicFields["Update: fullName, phone,<br/>birthDate, gender"]
    UpdateBasicFields --> CheckAvatar{Avatar file uploaded?}
    CheckAvatar -->|No| SaveAccount[Save account to database]
    
    CheckAvatar -->|Yes| ValidateFile{File not empty?}
    ValidateFile -->|No| SaveAccount
    
    ValidateFile -->|Yes| SaveFile[Save file to uploads directory]
    SaveFile --> GetFilePath[Get relative file path]
    GetFilePath --> UpdateAvatar[Set account.avatar = filePath]
    UpdateAvatar --> SaveAccount
    
    SaveAccount --> AddFlashMsg[Add success flash message]
    AddFlashMsg --> RedirectProfile([Redirect to /account/profile])
```

**Key Features:**
- Separate validation for form fields and file upload
- Optional avatar upload (doesn't overwrite if not provided)
- File storage in uploads directory
- Flash message for user feedback
- Transaction support for data consistency

---

## 5. Admin Management Flows

### 5.1 Admin Dashboard Access

```mermaid
flowchart TD
    Start([Admin accesses /admin/dashboard]) --> CheckAuth{User authenticated?}
    CheckAuth -->|No| RedirectLogin([Redirect to /login])
    CheckAuth -->|Yes| CheckRole{Role == ADMIN?}
    CheckRole -->|No| ShowAccessDenied([Show 403 Access Denied])
    CheckRole -->|Yes| LoadDashboardData[Load dashboard statistics]
    LoadDashboardData --> CountProducts[Count total products]
    CountProducts --> CountOrders[Count total orders]
    CountOrders --> CountUsers[Count total users]
    CountUsers --> CalculateRevenue[Calculate total revenue]
    CalculateRevenue --> GetRecentOrders[Get recent orders]
    GetRecentOrders --> AddDataToModel[Add all data to model]
    AddDataToModel --> SetPageTitle[Set pageTitle = Tổng quan - Admin]
    SetPageTitle --> RenderView([Render admin/dashboard.html])
```

**Key Features:**
- Role-based access control (ADMIN only)
- Dashboard statistics aggregation
- Recent activity display

---

### 5.2 Product Management Flow

```mermaid
flowchart TD
    Start([Admin accesses /admin/products]) --> CheckAuth{Authenticated & ADMIN?}
    CheckAuth -->|No| AccessDenied([Access Denied])
    CheckAuth -->|Yes| LoadProducts[Load all products from database]
    LoadProducts --> AddToModel[Add products list to model]
    AddToModel --> RenderList([Render admin/products.html])
    
    RenderList --> UserAction{Admin action?}
    UserAction -->|Create New| ShowCreateForm[Navigate to /admin/products/create]
    ShowCreateForm --> DisplayForm[Display product creation form]
    DisplayForm --> AdminFills[Admin fills product details]
    AdminFills --> SubmitCreate[Admin submits form]
    SubmitCreate --> ValidateProduct{Validation passed?}
    ValidateProduct -->|No| ShowCreateErrors[Show validation errors]
    ShowCreateErrors --> DisplayForm
    ValidateProduct -->|Yes| SaveNewProduct[Save new product to database]
    SaveNewProduct --> RedirectList1([Redirect to /admin/products])
    
    UserAction -->|Edit| LoadProduct[Load product by ID]
    LoadProduct --> ShowEditForm[Display edit form with data]
    ShowEditForm --> AdminUpdates[Admin updates fields]
    AdminUpdates --> SubmitUpdate[Admin submits update]
    SubmitUpdate --> ValidateUpdate{Validation passed?}
    ValidateUpdate -->|No| ShowUpdateErrors[Show errors]
    ShowUpdateErrors --> ShowEditForm
    ValidateUpdate -->|Yes| UpdateProduct[Update product in database]
    UpdateProduct --> RedirectList2([Redirect to /admin/products])
    
    UserAction -->|Delete| ConfirmDelete{Confirm deletion?}
    ConfirmDelete -->|No| CancelDelete([Cancel])
    ConfirmDelete -->|Yes| DeleteProduct[Delete product from database]
    DeleteProduct --> RedirectList3([Redirect to /admin/products])
```

**Key Features:**
- CRUD operations for products
- Form validation on create/update
- Confirmation for delete operations

---

### 5.3 Order Management & Account Management

```mermaid
flowchart TD
    Start([Admin navigates to management section]) --> SelectSection{Section?}
    
    SelectSection -->|Orders| AccessOrders[Access /admin/orders]
    AccessOrders --> LoadOrders[Load all orders from database]
    LoadOrders --> SortOrders[Sort by creation date descending]
    SortOrders --> DisplayOrders([Display orders list])
    DisplayOrders --> OrderActions{Admin action?}
    OrderActions -->|View Details| ShowOrderDetail[Show order details]
    OrderActions -->|Update Status| UpdateOrderStatus[Update order status]
    UpdateOrderStatus --> SaveOrder[Save order changes]
    SaveOrder --> RefreshOrders([Refresh orders list])
    
    SelectSection -->|Categories| AccessCategories[Access /admin/categories]
    AccessCategories --> LoadCategories[Load all categories]
    LoadCategories --> DisplayCategories([Display categories list])
    DisplayCategories --> CategoryActions{Admin action?}
    CategoryActions -->|Create| CreateCategory[Create new category]
    CategoryActions -->|Edit| EditCategory[Edit category]
    CategoryActions -->|Delete| DeleteCategory[Delete category]
    CreateCategory --> SaveCategory[Save category]
    EditCategory --> SaveCategory
    SaveCategory --> RefreshCategories([Refresh categories list])
    
    SelectSection -->|Accounts| AccessAccounts[Access /admin/accounts]
    AccessAccounts --> LoadAccounts[Load all accounts]
    LoadAccounts --> DisplayAccounts([Display accounts list])
    DisplayAccounts --> AccountActions{Admin action?}
    AccountActions -->|View Profile| ShowUserProfile[Show user profile]
    AccountActions -->|Toggle Active| ToggleAccess[Toggle isActive flag]
    AccountActions -->|Unlock Account| UnlockUser[Reset failed attempts & unlock]
    ToggleAccess --> SaveAccountChanges[Save account changes]
    UnlockUser --> SaveAccountChanges
    SaveAccountChanges --> RefreshAccounts([Refresh accounts list])
```

**Key Features:**
- Centralized admin control panel
- Order status management
- Category CRUD operations
- Account activation/deactivation
- Manual account unlock capability

---

## 6. Shopping Flows

### 6.1 Browse Products & View Details

```mermaid
flowchart TD
    Start([User accesses /products]) --> LoadProducts[Load products from database]
    LoadProducts --> ApplyFilters{Filters applied?}
    ApplyFilters -->|Yes| FilterByCategory[Filter by category]
    FilterByCategory --> FilterByPrice[Filter by price range]
    FilterByPrice --> FilterBySearch[Filter by search keyword]
    FilterBySearch --> SortResults[Sort results]
    ApplyFilters -->|No| SortResults
    SortResults --> Paginate[Paginate results]
    Paginate --> DisplayGrid([Display product grid])
    
    DisplayGrid --> UserClick{User clicks product?}
    UserClick -->|Yes| NavigateDetail[Navigate to /product/{id}]
    UserClick -->|No| BrowseMore[Continue browsing]
    
    NavigateDetail --> LoadProductDetail[Load product by ID]
    LoadProductDetail --> ProductExists{Product found?}
    ProductExists -->|No| Show404([Show 404 error])
    ProductExists -->|Yes| LoadImages[Load product images]
    LoadImages --> LoadReviews[Load product reviews]
    LoadReviews --> LoadRelated[Load related products]
    LoadRelated --> DisplayDetail([Display product detail page])
    DisplayDetail --> UserDetailAction{User action?}
    UserDetailAction -->|Add to Cart| AddToCart[Add item to cart]
    UserDetailAction -->|View Another| NavigateDetail
    UserDetailAction -->|Go Back| DisplayGrid
```

**Key Features:**
- Multi-criteria filtering (category, price, search)
- Pagination for performance
- Related products recommendation
- Product reviews display

---

### 6.2 Shopping Cart & Checkout

```mermaid
flowchart TD
    Start([User adds product to cart]) --> CheckSession{User session exists?}
    CheckSession -->|No| CreateSession[Create new session]
    CheckSession -->|Yes| GetCart[Get cart from session]
    CreateSession --> InitCart[Initialize empty cart]
    
    GetCart --> CheckProduct{Product in cart?}
    InitCart --> CheckProduct
    CheckProduct -->|Yes| UpdateQty[Update quantity]
    CheckProduct -->|No| AddNewItem[Add new cart item]
    UpdateQty --> RecalculateTotal[Recalculate cart total]
    AddNewItem --> RecalculateTotal
    RecalculateTotal --> SaveCart[Save cart to session]
    SaveCart --> ShowCartBadge[Update cart badge counter]
    
    ShowCartBadge --> UserNav{User navigates to?}
    UserNav -->|View Cart| DisplayCart[Navigate to /cart]
    UserNav -->|Continue Shopping| BackToProducts([Back to products])
    
    DisplayCart --> ShowCartItems[Display all cart items]
    ShowCartItems --> CartAction{User action?}
    CartAction -->|Update Qty| UpdateQuantity[Update item quantity]
    UpdateQuantity --> RecalculateTotal
    CartAction -->|Remove Item| RemoveFromCart[Remove cart item]
    RemoveFromCart --> RecalculateTotal
    CartAction -->|Proceed| CheckAuth{User authenticated?}
    
    CheckAuth -->|No| RedirectLogin([Redirect to login])
    CheckAuth -->|Yes| NavigateCheckout[Navigate to /checkout]
    NavigateCheckout --> LoadAddress[Load user address]
    LoadAddress --> DisplayCheckoutForm([Display checkout form])
    DisplayCheckoutForm --> UserSubmit[User submits order]
    UserSubmit --> ValidateOrder{Validation passed?}
    ValidateOrder -->|No| ShowCheckoutErrors[Show validation errors]
    ShowCheckoutErrors --> DisplayCheckoutForm
    
    ValidateOrder -->|Yes| CreateOrder[Create order in database]
    CreateOrder --> ClearCart[Clear cart from session]
    ClearCart --> SendConfirmEmail[Send order confirmation email]
    SendConfirmEmail --> ShowSuccess([Show order success page])
```

**Key Features:**
- Session-based cart management
- Real-time quantity updates
- Cart total recalculation
- Authentication requirement for checkout
- Order confirmation email
- Cart persistence across pages

---

## Summary

This document contains comprehensive activity diagrams for the ShopOMG e-commerce application featuring:

- **Authentication System**: Login with attempt limiting, registration with email verification, OAuth2 social login
- **Email Verification**: Token-based email verification and resend functionality
- **Password Reset**: Secure password reset flow with token expiration
- **Account Management**: Profile viewing and updating with avatar upload
- **Admin Features**: Dashboard, product management, order management, category and account administration
- **Shopping**: Product browsing, detailed views, cart management, and checkout

All flows include proper validation, error handling, security measures, and user feedback mechanisms.
