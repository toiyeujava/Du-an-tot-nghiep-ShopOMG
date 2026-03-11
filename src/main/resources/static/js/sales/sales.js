/**
 * Sales Dashboard JavaScript
 * Components dùng chung cho trang Nhân viên Bán hàng
 * ==================================================
 */

// ===== CSRF HELPER =====
const Csrf = {
    getToken: () => document.querySelector("meta[name='_csrf']")?.getAttribute("content") || '',
    getHeader: () => document.querySelector("meta[name='_csrf_header']")?.getAttribute("content") || 'X-CSRF-TOKEN',
    headers: function () {
        return { [this.getHeader()]: this.getToken(), 'Content-Type': 'application/json' };
    }
};


// ===== TOAST NOTIFICATION =====
// Dùng: Toast.show('Cập nhật thành công!', 'success')
//       Toast.show('Có lỗi xảy ra!', 'danger')
//       Toast.show('Đang xử lý...', 'warning')
const Toast = {
    show: function (message, type = 'success') {
        const icons = {
            success: 'fas fa-check-circle',
            danger:  'fas fa-times-circle',
            warning: 'fas fa-exclamation-triangle',
            info:    'fas fa-info-circle'
        };

        let container = document.getElementById('toast-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toast-container';
            container.className = 'toast-container position-fixed bottom-0 end-0 p-3';
            container.style.zIndex = '9999';
            document.body.appendChild(container);
        }

        const id = 'toast-' + Date.now();
        const toastEl = document.createElement('div');
        toastEl.innerHTML = `
            <div id="${id}" class="toast align-items-center text-white bg-${type} border-0 mb-2" role="alert">
                <div class="d-flex">
                    <div class="toast-body">
                        <i class="${icons[type] || icons.info} me-2"></i>${message}
                    </div>
                    <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
                </div>
            </div>`;
        container.appendChild(toastEl.firstElementChild);
        new bootstrap.Toast(document.getElementById(id), { delay: 3500 }).show();
    }
};


// ===== CONFIRM MODAL =====
// Dùng: ConfirmModal.show('Bạn có chắc muốn hủy đơn?', () => { ... })
const ConfirmModal = {
    show: function (message, onConfirm, options = {}) {
        const {
            title       = 'Xác nhận',
            confirmText = 'Xác nhận',
            cancelText  = 'Hủy',
            type        = 'danger'   // danger | warning | primary
        } = options;

        // Tạo modal nếu chưa có
        let modal = document.getElementById('salesConfirmModal');
        if (!modal) {
            document.body.insertAdjacentHTML('beforeend', `
                <div class="modal fade" id="salesConfirmModal" tabindex="-1">
                    <div class="modal-dialog modal-dialog-centered">
                        <div class="modal-content border-0 shadow">
                            <div class="modal-header border-0 pb-0">
                                <h6 class="modal-title fw-bold" id="salesConfirmTitle"></h6>
                                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                            </div>
                            <div class="modal-body" id="salesConfirmMessage"></div>
                            <div class="modal-footer border-0 pt-0">
                                <button type="button" class="btn btn-light" data-bs-dismiss="modal" id="salesConfirmCancel"></button>
                                <button type="button" class="btn" id="salesConfirmOk"></button>
                            </div>
                        </div>
                    </div>
                </div>`);
            modal = document.getElementById('salesConfirmModal');
        }

        document.getElementById('salesConfirmTitle').textContent   = title;
        document.getElementById('salesConfirmMessage').textContent = message;
        document.getElementById('salesConfirmCancel').textContent  = cancelText;

        const okBtn = document.getElementById('salesConfirmOk');
        okBtn.textContent  = confirmText;
        okBtn.className    = `btn btn-${type}`;

        // Gắn handler (xóa handler cũ trước)
        const newOkBtn = okBtn.cloneNode(true);
        okBtn.parentNode.replaceChild(newOkBtn, okBtn);
        newOkBtn.addEventListener('click', () => {
            bootstrap.Modal.getInstance(modal).hide();
            onConfirm();
        });

        new bootstrap.Modal(modal).show();
    }
};


// ===== ORDER STATUS BADGE =====
// Dùng: OrderBadge.render('PENDING') → trả về HTML badge
const OrderBadge = {
    config: {
        PENDING:   { label: 'Chờ xử lý',   cls: 'bg-warning text-dark', icon: 'fa-clock' },
        CONFIRMED: { label: 'Đã xác nhận', cls: 'bg-info text-dark',    icon: 'fa-check' },
        SHIPPING:  { label: 'Đang giao',   cls: 'bg-primary',           icon: 'fa-truck' },
        COMPLETED: { label: 'Hoàn thành',  cls: 'bg-success',           icon: 'fa-check-circle' },
        CANCELLED: { label: 'Đã huỷ',      cls: 'bg-danger',            icon: 'fa-times-circle' }
    },
    render: function (status) {
        const cfg = this.config[status] || { label: status, cls: 'bg-secondary', icon: 'fa-question' };
        return `<span class="badge ${cfg.cls}"><i class="fas ${cfg.icon} me-1"></i>${cfg.label}</span>`;
    }
};


// ===== UPDATE ORDER STATUS (API CALL) =====
// Dùng: OrderManager.updateStatus(orderId, 'CONFIRMED', () => location.reload())
const OrderManager = {
    updateStatus: async function (orderId, newStatus, onSuccess) {
        try {
            const res = await fetch(`/sales/orders/${orderId}/status`, {
                method: 'PUT',
                headers: Csrf.headers(),
                body: JSON.stringify({ status: newStatus })
            });
            const data = await res.json();
            if (data.success) {
                Toast.show(data.message || 'Cập nhật trạng thái thành công!', 'success');
                if (onSuccess) onSuccess();
            } else {
                Toast.show(data.message || 'Có lỗi xảy ra!', 'danger');
            }
        } catch (err) {
            Toast.show('Lỗi kết nối: ' + err.message, 'danger');
        }
    },

    cancelOrder: function (orderId, reason, onSuccess) {
        fetch(`/sales/orders/${orderId}/cancel`, {
            method: 'PUT',
            headers: Csrf.headers(),
            body: JSON.stringify({ reason })
        })
        .then(r => r.json())
        .then(data => {
            if (data.success) {
                Toast.show('Đã hủy đơn hàng!', 'success');
                if (onSuccess) onSuccess();
            } else {
                Toast.show(data.message || 'Không thể hủy đơn!', 'danger');
            }
        })
        .catch(err => Toast.show('Lỗi kết nối: ' + err.message, 'danger'));
    }
};


// ===== FORMAT HELPERS =====
// Tái sử dụng từ admin.js, thêm một số tiện ích cho Sales
const Format = {
    currency: (amount) => new Intl.NumberFormat('vi-VN', {
        style: 'currency', currency: 'VND'
    }).format(amount),

    date: (dateStr) => new Date(dateStr).toLocaleDateString('vi-VN', {
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit'
    }),

    phone: (phone) => phone ? phone.replace(/(\d{4})(\d{3})(\d{3})/, '$1 $2 $3') : '-'
};


// ===== LOADING BUTTON =====
// Dùng: Loading.start(btn) / Loading.stop(btn, 'Lưu')
const Loading = {
    start: function (btn) {
        btn.dataset.originalText = btn.innerHTML;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Đang xử lý...';
        btn.disabled = true;
    },
    stop: function (btn, text) {
        btn.innerHTML = text || btn.dataset.originalText || 'Xác nhận';
        btn.disabled = false;
    }
};