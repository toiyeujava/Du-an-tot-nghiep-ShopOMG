/**
 * Admin Dashboard JavaScript
 * Common utilities and functions for admin pages
 * ==================================================
 */

// ===== CATEGORY CRUD =====
const CategoryManager = {
    isEditMode: false,

    openCreateModal: function () {
        this.isEditMode = false;
        document.getElementById('modalTitle').textContent = 'Thêm Loại mới';
        document.getElementById('categoryId').value = '';
        document.getElementById('categoryName').value = '';
        document.getElementById('categoryImage').value = '';
    },

    openEditModal: function (id, name, image) {
        this.isEditMode = true;
        document.getElementById('modalTitle').textContent = 'Sửa Loại';
        document.getElementById('categoryId').value = id;
        document.getElementById('categoryName').value = name;
        document.getElementById('categoryImage').value = image === 'null' ? '' : (image || '');
        new bootstrap.Modal(document.getElementById('categoryModal')).show();
    },

    save: async function () {
        const id = document.getElementById('categoryId').value;
        const name = document.getElementById('categoryName').value;
        const image = document.getElementById('categoryImage').value;

        if (!name.trim()) {
            alert('Vui lòng nhập tên loại!');
            return;
        }

        const url = this.isEditMode ? `/admin/categories/${id}` : '/admin/categories';
        const method = this.isEditMode ? 'PUT' : 'POST';

        try {
            const response = await fetch(url, {
                method: method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, image, isActive: true })
            });

            const result = await response.json();

            if (result.success) {
                alert(result.message);
                location.reload();
            } else {
                alert('Lỗi: ' + result.message);
            }
        } catch (error) {
            alert('Lỗi kết nối: ' + error.message);
        }
    },

    delete: async function (id) {
        if (!confirm('Bạn có chắc muốn xóa loại này?')) return;

        try {
            const response = await fetch(`/admin/categories/${id}`, {
                method: 'DELETE'
            });

            const result = await response.json();

            if (result.success) {
                document.getElementById('row-' + id).remove();
                alert(result.message);
            } else {
                alert('Lỗi: ' + result.message);
            }
        } catch (error) {
            alert('Lỗi kết nối: ' + error.message);
        }
    }
};

// Legacy function wrappers for backward compatibility
function openCreateModal() { CategoryManager.openCreateModal(); }
function openEditModal(id, name, image) { CategoryManager.openEditModal(id, name, image); }
function saveCategory() { CategoryManager.save(); }
function deleteCategory(id) { CategoryManager.delete(id); }


// ===== VARIANT CRUD =====
const VariantManager = {
    isEditMode: false,
    productId: null,

    init: function (productId) {
        this.productId = productId;
    },

    openCreateModal: function () {
        this.isEditMode = false;
        document.getElementById('modalTitle').textContent = 'Thêm biến thể mới';
        document.getElementById('variantId').value = '';
        document.getElementById('sku').value = '';
        document.getElementById('color').value = '';
        document.getElementById('size').value = '';
        document.getElementById('quantity').value = '0';
        document.getElementById('variantForm').action = '/admin/products/' + this.productId + '/variants';
    },

    openEditModal: function (id, sku, color, size, quantity) {
        this.isEditMode = true;
        document.getElementById('modalTitle').textContent = 'Sửa biến thể';
        document.getElementById('variantId').value = id;
        document.getElementById('sku').value = sku === 'null' ? '' : (sku || '');
        document.getElementById('color').value = color === 'null' ? '' : (color || '');
        document.getElementById('size').value = size === 'null' ? '' : (size || '');
        document.getElementById('quantity').value = quantity || 0;
        document.getElementById('variantForm').action = '/admin/products/' + this.productId + '/variants/' + id;
        new bootstrap.Modal(document.getElementById('variantModal')).show();
    }
};


// ===== UTILITY FUNCTIONS =====

// Format currency VND
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

// Format date
function formatDate(dateString) {
    return new Date(dateString).toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Confirm delete action
function confirmDelete(message = 'Bạn có chắc muốn xóa?') {
    return confirm(message);
}

// Show toast notification
function showToast(message, type = 'success') {
    const toastHtml = `
        <div class="toast align-items-center text-white bg-${type === 'success' ? 'success' : 'danger'} border-0" role="alert">
            <div class="d-flex">
                <div class="toast-body">${message}</div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        </div>
    `;

    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.className = 'toast-container position-fixed bottom-0 end-0 p-3';
        document.body.appendChild(container);
    }

    container.innerHTML = toastHtml;
    const toast = new bootstrap.Toast(container.querySelector('.toast'));
    toast.show();
}


// ===== DASHBOARD CHART =====
function initRevenueChart(chartData) {
    if (!chartData || !document.getElementById('revenueChart')) return;

    const labels = chartData.map(d => d.month);
    const values = chartData.map(d => Number(d.revenue) / 1000000);

    const ctx = document.getElementById('revenueChart').getContext('2d');
    new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Doanh thu (Triệu)',
                data: values,
                borderColor: '#3699ff',
                backgroundColor: 'rgba(54, 153, 255, 0.1)',
                fill: true,
                tension: 0.4
            }]
        },
        options: {
            plugins: { legend: { display: false } },
            scales: { x: { grid: { display: false } } }
        }
    });
}
