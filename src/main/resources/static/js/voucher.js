/**
 * js/voucher.js
 * Quản lý logic Áp dụng Voucher trên giao diện sử dụng Fetch API.
 * Bao gồm tính năng chọn voucher, gửi request, xử lý success/error, và cập nhật DOM.
 */

document.addEventListener("DOMContentLoaded", function () {
    const inputCode = document.getElementById("voucherInput");
    const btnApply = document.getElementById("btnApplyVoucher");
    const containerToast = createToastContainer();

    // 1. Logic cho nút "Chọn" trên các voucher có sẵn
    const btnSelects = document.querySelectorAll(".btn-select-voucher");
    btnSelects.forEach(btn => {
        btn.addEventListener("click", function () {
            const code = this.getAttribute("data-code");
            inputCode.value = code;
        });
    });

    // Hỗ trợ ấn Enter trong ô input
    inputCode.addEventListener("keypress", function (e) {
        if (e.key === "Enter") {
            e.preventDefault();
            btnApply.click();
        }
    });

    // 2. Logic gọi API khi bấm "Áp dụng"
    btnApply.addEventListener("click", function () {
        const code = inputCode.value.trim();
        if (!code) {
            showToast("Vui lòng nhập mã giảm giá!", "warning");
            return;
        }

        // Đổi trạng thái UI đang loading
        const originalText = btnApply.innerHTML;
        btnApply.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>';
        btnApply.disabled = true;

        // Lấy tổng tiền giỏ hàng từ DOM
        const cartTotalAmount = getCartTotalAmount();
        const shippingFee = window._shippingFee || 30000;
        
        // Chuẩn bị payload
        const payload = {
            voucherCode: code,
            cartTotalAmount: cartTotalAmount,
            shippingFee: shippingFee
        };

        // Lấy CSRF token (Spring Security dùng Cookie hoặc thẻ meta)
        const csrfToken = getCsrfToken();

        // Gửi Request
        fetch('/api/vouchers/apply', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken 
            },
            body: JSON.stringify(payload)
        })
        .then(response => {
            // Check HTTP status code
            if (response.status === 400 || response.status === 404) {
                return response.json().then(data => { throw data; });
            }
            if (!response.ok) {
                throw new Error("Lỗi máy chủ (" + response.status + ")");
            }
            return response.json();
        })
        .then(data => {
            // Khôi phục nút
            btnApply.innerHTML = originalText;
            btnApply.disabled = false;

            if (data.valid) {
                // Thành công
                showToast(data.message, "success");
                
                // Cập nhật DOM (số tiền)
                updateUIDiscountAndTotal(data.discountAmount, data.finalTotal);

                // Nếu bạn đang ở trang Checkout (chứa form), lưu mã voucher vào 1 thẻ input hidden để lúc submit gửi lên Backend
                const hiddenInput = document.getElementById("appliedVoucherCode");
                if (hiddenInput) {
                    hiddenInput.value = code;
                }

                // Tự động đóng Modal
                const modalEl = document.getElementById("voucherModal");
                if (modalEl) {
                    const modal = bootstrap.Modal.getInstance(modalEl);
                    if (modal) modal.hide();
                }

            } else {
                // Thất bại (logic bên trong Service trả về valid=false)
                showToast(data.message, "danger");
            }
        })
        .catch(error => {
            // Bắt các lỗi HTTP 400, 500
            btnApply.innerHTML = originalText;
            btnApply.disabled = false;
            
            // Xử lý thông báo lỗi
            let errorMsg = "Không thể áp dụng mã. Vui lòng thử lại.";
            if (error.message) errorMsg = error.message; // String error throw
            
            // Nếu lỗi là do @Valid của Jakarta ném ra
            if (error.errors) {
               // VD: Validation lỗi -> array object { defaultMessage: "..." }
               errorMsg = Object.values(error.errors).join(", ");
            } else if (error.voucherCode) {
               // Lỗi HashMap
               errorMsg = error.voucherCode;
            }

            showToast(errorMsg, "danger");
        });
    });

    // ---------- Helper Functions ----------

    // Lấy CSRF token từ Meta tags (chuẩn Spring Security)
    function getCsrfToken() {
        // Cần đảm bảo trong <head> thẻ meta chứa token
        const tokenMeta = document.querySelector('meta[name="_csrf"]');
        if (tokenMeta) return tokenMeta.getAttribute('content');
        return '';
    }

    // Đọc giá trị tổng tiền giỏ hàng từ 1 element (ID)
    function getCartTotalAmount() {
        // Cố gắng tìm phần tử chứa tổng tiền, ví dụ ở trang cart/checkout
        const totalEl = document.getElementById("cartTotalAmountRaw"); // Một thẻ input hidden lưu giá trị số hoặc thẻ div chứa thuộc tính data-value
        if (totalEl) {
            return parseFloat(totalEl.value || totalEl.getAttribute("data-value"));
        }
        
        // Nếu không có, cố gắng parse từ thẻ chứa text (Loại bỏ các ký tự đ, dấu phẩy)
        const textEl = document.getElementById("cartTotalAmountText");
        if (textEl) {
            let str = textEl.innerText.replace(/[^\d]/g, ''); // Xóa hết chữ, giữ lại số
            return parseFloat(str);
        }

        // Fallback an toàn nếu không tìm thấy DOM (để test ko chết lỗi)
        return 999999999; 
    }

    // Format tiền Việt Nam Đồng
    function formatCurrencyVND(amount) {
        return new Intl.NumberFormat('vi-VN').format(amount) + 'đ';
    }

    // Cập nhật DOM (giảm giá và tổng tiền)
    function updateUIDiscountAndTotal(discountAmount, finalTotal) {
        // Tìm element hiển thị discount
        const discountEl = document.getElementById("discountAmountDisplay");
        if (discountEl) {
            discountEl.innerText = "- " + formatCurrencyVND(discountAmount);
            discountEl.style.color = "#e74c3c"; // Hiện màu đỏ
        }
        
        // Tìm element hiển thị Final Total
        const totalEls = document.querySelectorAll(".finalTotalDisplay");
        totalEls.forEach(el => {
            el.innerText = formatCurrencyVND(finalTotal);
        });

        // Tìm element (div/tr) đang ẩn chứa dòng "Giảm giá" để bật nó lên
        const discountRow = document.getElementById("discountRow");
        if (discountRow) {
            discountRow.style.display = ""; // hoặc 'flex', 'table-row'
        }
    }

    // Logic tạo Bootstrap 5 Toast động để hiện thông báo 
    function createToastContainer() {
        let container = document.getElementById("toast-container");
        if (!container) {
            container = document.createElement("div");
            container.id = "toast-container";
            container.className = "toast-container position-fixed bottom-0 end-0 p-3";
            container.style.zIndex = "9999";
            document.body.appendChild(container);
        }
        return container;
    }

    function showToast(message, type = "success") {
        const bgClass = type === "success" ? "bg-success" : (type === "warning" ? "bg-warning text-dark" : "bg-danger");
        const textColor = type === "warning" ? "text-dark" : "text-white";
        const icon = type === "success" ? "fa-check-circle" : "fa-exclamation-circle";

        const toastId = "toast_" + Date.now();
        const toastHTML = `
            <div id="${toastId}" class="toast align-items-center ${bgClass} ${textColor} border-0" role="alert" aria-live="assertive" aria-atomic="true" data-bs-delay="4000">
              <div class="d-flex">
                <div class="toast-body fw-bold">
                  <i class="fas ${icon} me-2"></i> ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
              </div>
            </div>
        `;
        
        containerToast.insertAdjacentHTML("beforeend", toastHTML);
        const toastElement = document.getElementById(toastId);
        const bsToast = new bootstrap.Toast(toastElement);
        bsToast.show();

        // Xóa khỏi DOM sau khi ẩn
        toastElement.addEventListener("hidden.bs.toast", function () {
            toastElement.remove();
        });
    }
});
