/*
 * Warehouse Staff Management — warehouse.js
 * Handles: SKU search/filter, quantity update modal, REST API calls
 */

// ── CSRF Token (Spring Security) ──────────────────────────────────
function getCsrf() {
    return {
        token: document.querySelector('meta[name="_csrf"]')?.content ?? '',
        header: document.querySelector('meta[name="_csrf_header"]')?.content ?? 'X-CSRF-TOKEN'
    };
}

// ── Inventory Filter ──────────────────────────────────────────────
function filterInventory() {
    const search = document.getElementById('skuSearch')?.value.toLowerCase() ?? '';
    const category = document.getElementById('categoryFilter')?.value ?? '';
    const stock = document.getElementById('stockFilter')?.value ?? '';

    const rows = document.querySelectorAll('#inventoryBody tr');
    let visible = 0;

    rows.forEach(row => {
        const sku = (row.dataset.sku ?? '').toLowerCase();
        const cat = row.dataset.category ?? '';
        const level = row.dataset.stockLevel ?? '';
        const text = row.textContent.toLowerCase();

        const matchSearch = !search || sku.includes(search) || text.includes(search);
        const matchCategory = !category || cat === category;
        const matchStock = !stock || level === stock;

        const show = matchSearch && matchCategory && matchStock;
        row.style.display = show ? '' : 'none';
        if (show) visible++;
    });

    const countEl = document.getElementById('rowCount');
    if (countEl) {
        countEl.innerHTML = `Đang hiển thị <strong>${visible}</strong> SKU`;
    }
}

// ── Update Stock Modal ────────────────────────────────────────────
let _currentQty = 0;
let _currentVariantId = null;

function openUpdateModal(sku, productName, currentQty, variantId) {
    _currentQty = parseInt(currentQty) || 0;
    _currentVariantId = variantId ?? null;

    document.getElementById('modal-sku').textContent = sku;
    document.getElementById('modal-product-name').textContent = productName;
    document.getElementById('modal-current-qty').textContent = currentQty;
    document.getElementById('qtyChange').value = 1;

    updatePreview();

    const modal = new bootstrap.Modal(document.getElementById('updateStockModal'));
    modal.show();
}

function adjustQty(delta) {
    const input = document.getElementById('qtyChange');
    const val = Math.max(1, (parseInt(input.value) || 1) + delta);
    input.value = val;
    updatePreview();
}

document.addEventListener('DOMContentLoaded', () => {
    const qtyInput = document.getElementById('qtyChange');
    if (qtyInput) {
        qtyInput.addEventListener('input', updatePreview);
    }

    // Radio highlight style
    document.querySelectorAll('input[name="txType"]').forEach(radio => {
        radio.addEventListener('change', () => {
            document.querySelectorAll('input[name="txType"]').forEach(r => {
                r.closest('.form-check').classList.remove('border-warning', 'bg-warning-subtle',
                    'border-danger', 'bg-danger-subtle', 'border-indigo', 'bg-indigo-subtle');
            });
            const parent = radio.closest('.form-check');
            if (radio.value === 'in') parent.classList.add('border-success', 'bg-success-subtle');
            if (radio.value === 'out') parent.classList.add('border-danger', 'bg-danger-subtle');
            if (radio.value === 'adj') parent.style.borderColor = '#6366f1';
            updatePreview();
        });
    });

    // Highlight radio on init
    const checkedRadio = document.querySelector('input[name="txType"]:checked');
    if (checkedRadio) {
        checkedRadio.closest('.form-check').classList.add('border-success', 'bg-success-subtle');
    }
});

function updatePreview() {
    const qty = parseInt(document.getElementById('qtyChange')?.value) || 0;
    const txType = document.querySelector('input[name="txType"]:checked')?.value ?? 'in';
    const previewEl = document.getElementById('qty-preview');
    if (!previewEl) return;

    let newQty;
    if (txType === 'in') newQty = _currentQty + qty;
    if (txType === 'out') newQty = Math.max(0, _currentQty - qty);
    if (txType === 'adj') newQty = qty;

    previewEl.textContent = newQty ?? qty;
    previewEl.className = newQty <= 0
        ? 'fw-bold fs-4 text-danger'
        : newQty <= 10 ? 'fw-bold fs-4 text-warning' : 'fw-bold fs-4 text-success';
}

function submitUpdate() {
    const sku = document.getElementById('modal-sku').textContent;
    const qty = parseInt(document.getElementById('qtyChange').value);
    const note = document.getElementById('logNote').value;
    const txType = document.querySelector('input[name="txType"]:checked')?.value ?? 'adj';

    if (!qty || qty < 1) {
        alert('Vui lòng nhập số lượng hợp lệ (≥ 1).');
        return;
    }

    // If we have a real variant ID, call the REST API
    if (_currentVariantId) {
        const csrf = getCsrf();
        const body = { type: txType, quantity: qty, note: note };

        fetch(`/warehouse/api/variants/${_currentVariantId}/quantity`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                [csrf.header]: csrf.token
            },
            body: JSON.stringify(body)
        })
            .then(r => r.json())
            .then(data => {
                if (data.success) {
                    // Dismiss modal
                    const modalInst = bootstrap.Modal.getInstance(document.getElementById('updateStockModal'));
                    if (modalInst) modalInst.hide();

                    showToast(`Đã cập nhật ${sku}: tồn kho mới = ${data.newQuantity} đơn vị.`);

                    // Reload after short delay so table refreshes
                    setTimeout(() => location.reload(), 1800);
                } else {
                    alert('Lỗi: ' + data.message);
                }
            })
            .catch(() => alert('Không thể kết nối server. Vui lòng thử lại.'));

    } else {
        // Static demo mode (no variant ID) — just dismiss and toast
        const modalInst = bootstrap.Modal.getInstance(document.getElementById('updateStockModal'));
        if (modalInst) modalInst.hide();
        showToast(`Đã cập nhật ${sku}: ${txType === 'in' ? '+' : txType === 'out' ? '-' : '→'}${qty} đơn vị.`);
    }
}

// ── Log by SKU Modal ──────────────────────────────────────────────
function openLogModal(sku) {
    document.getElementById('log-sku-title').textContent = sku;
    const modal = new bootstrap.Modal(document.getElementById('skuLogModal'));
    modal.show();
}

// ── Toast ─────────────────────────────────────────────────────────
function showToast(message) {
    const toastEl = document.getElementById('successToast');
    const msgEl = document.getElementById('toastMessage');
    if (toastEl && msgEl) {
        msgEl.textContent = message;
        const toast = new bootstrap.Toast(toastEl, { delay: 4000 });
        toast.show();
    }
}

// ── Export CSV (fake) ─────────────────────────────────────────────
function exportCSV() {
    showToast('Đang xuất file CSV… Vui lòng chờ.');
}

// ═══════════════════════════════════════════════════════════════════
// SPA SIDEBAR NAVIGATION
// Intercepts [data-wh-tab] sidebar link clicks.
// Instead of reloading the page, it:
//   1. Fades out #wh-main-content
//   2. Activates the corresponding Bootstrap tab
//   3. Updates sidebar active state
//   4. Updates the page title (#wh-page-title)
//   5. Fades content back in
// ═══════════════════════════════════════════════════════════════════

// Map tab IDs → their Bootstrap tab button IDs in inventory.html
const TAB_BTN_MAP = {
    'tab-dashboard': 'dashboard-tab',   // New dashboard tab (first tab, active by default)
    'tab-inventory': 'inventory-tab',
    'tab-alerts': 'alert-tab',
    'tab-log': 'log-tab',
    'tab-update': 'log-tab',         // Update opens from log tab (modal available there)
};

function whSwitchTab(tabId, title) {
    const contentEl = document.getElementById('wh-main-content');
    if (!contentEl) return;

    // ── Step 1: fade out ──────────────────────────────────────────
    contentEl.classList.add('wh-fade-out');

    setTimeout(() => {
        // ── Step 2: activate Bootstrap tab ───────────────────────
        const btnId = TAB_BTN_MAP[tabId] ?? 'inventory-tab';
        const tabBtn = document.getElementById(btnId);
        if (tabBtn) {
            const bsTab = bootstrap.Tab.getOrCreateInstance(tabBtn);
            bsTab.show();
        }

        // ── Step 3: update page title ─────────────────────────────
        const titleEl = document.getElementById('wh-page-title');
        if (titleEl && title) titleEl.textContent = title;

        // ── Step 4: if tab-update → open update stock modal ───────
        if (tabId === 'tab-update') {
            const bsModal = new bootstrap.Modal(document.getElementById('updateStockModal'));
            bsModal.show();
        }

        // ── Step 5: fade back in ──────────────────────────────────
        contentEl.classList.remove('wh-fade-out');

    }, 180); // matches CSS transition duration
}

function whUpdateSidebarActive(clickedLink) {
    // Remove active from all nav links
    document.querySelectorAll('.wh-nav-link').forEach(a => a.classList.remove('active'));
    // Add to clicked one
    clickedLink.classList.add('active');
}

// Wire up sidebar links on DOMContentLoaded
document.addEventListener('DOMContentLoaded', () => {

    document.querySelectorAll('a[data-wh-tab]').forEach(link => {
        link.addEventListener('click', e => {
            e.preventDefault();

            const tabId = link.dataset.whTab;
            const title = link.dataset.whTitle ?? '';

            whUpdateSidebarActive(link);
            whSwitchTab(tabId, title);

            // Push state for browser history / deep-link support
            const urlMap = {
                'tab-dashboard': '/warehouse/inventory',
                'tab-inventory': '/warehouse/inventory',
                'tab-alerts': '/warehouse/inventory#tab-alerts',
                'tab-log': '/warehouse/inventory#tab-log',
                'tab-update': '/warehouse/inventory#tab-update',
            };
            if (urlMap[tabId]) {
                history.pushState({ tab: tabId }, title, urlMap[tabId]);
            }
        });
    });

    // Restore tab from URL hash on page load (e.g. /warehouse/inventory#tab-log)
    const hash = window.location.hash.replace('#', '');
    if (hash && TAB_BTN_MAP[hash]) {
        const matchingLink = document.querySelector(`a[data-wh-tab="${hash}"]`);
        if (matchingLink) {
            whUpdateSidebarActive(matchingLink);
            whSwitchTab(hash, matchingLink.dataset.whTitle ?? '');
        }
    }

    // Handle browser back/forward
    window.addEventListener('popstate', e => {
        if (e.state?.tab) {
            const link = document.querySelector(`a[data-wh-tab="${e.state.tab}"]`);
            if (link) {
                whUpdateSidebarActive(link);
                whSwitchTab(e.state.tab, link.dataset.whTitle ?? '');
            }
        }
    });
});

