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

// ── Log Filter ──────────────────────────────────────────────
function filterLogs() {
    const search = document.getElementById('logSearch')?.value.toLowerCase() ?? '';
    const type = document.getElementById('logTypeFilter')?.value ?? '';
    const dateFrom = document.getElementById('logDateFrom')?.value ?? '';
    const dateTo = document.getElementById('logDateTo')?.value ?? '';

    const rows = document.querySelectorAll('#logBody tr');

    rows.forEach(row => {
        // Skip empty state row if it exists
        if (row.querySelector('td[colspan]')) return;

        const sku = (row.dataset.sku ?? '').toLowerCase();
        const rowType = row.dataset.type ?? '';
        const rowDate = row.dataset.timestamp ?? '';
        const text = row.textContent.toLowerCase();

        const matchSearch = !search || sku.includes(search) || text.includes(search);
        const matchType = !type || rowType === type;
        
        let matchDate = true;
        if (dateFrom && rowDate < dateFrom) matchDate = false;
        if (dateTo && rowDate > dateTo) matchDate = false;

        const show = matchSearch && matchType && matchDate;
        row.style.display = show ? '' : 'none';
    });
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
            const allContainers = document.querySelectorAll('input[name="txType"]');
            allContainers.forEach(r => {
                const btnDiv = r.closest('.form-check');
                if (btnDiv) {
                    // Remove all possible highlight classes
                    btnDiv.classList.remove('border-success', 'bg-success-subtle',
                        'border-danger', 'bg-danger-subtle', 'border-primary', 'bg-primary-subtle');
                    btnDiv.style.borderColor = ''; 
                }
            });
            
            const parent = radio.closest('.form-check');
            if (parent) {
                if (radio.value === 'in') parent.classList.add('border-success', 'bg-success-subtle');
                if (radio.value === 'out') parent.classList.add('border-danger', 'bg-danger-subtle');
                if (radio.value === 'adj') parent.classList.add('border-primary', 'bg-primary-subtle');
            }
            updatePreview();
        });
    });

    // Highlight radio on init
    const checkedRadio = document.querySelector('input[name="txType"]:checked');
    if (checkedRadio) {
        const parent = checkedRadio.closest('.form-check');
        if (parent) {
            if (checkedRadio.value === 'in') parent.classList.add('border-success', 'bg-success-subtle');
            if (checkedRadio.value === 'out') parent.classList.add('border-danger', 'bg-danger-subtle');
            if (checkedRadio.value === 'adj') parent.classList.add('border-primary', 'bg-primary-subtle');
        }
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

                    // Tải lại các thành phần giao diện bằng AJAX thay vì load lại toàn bộ trang
                    fetch(window.location.href)
                        .then(res => res.text())
                        .then(html => {
                            const parser = new DOMParser();
                            const doc = parser.parseFromString(html, 'text/html');
                            
                            // 1. Cập nhật tab Tồn kho
                            const invBody = document.getElementById('inventoryBody');
                            if (invBody && doc.getElementById('inventoryBody')) {
                                invBody.innerHTML = doc.getElementById('inventoryBody').innerHTML;
                            }
                            
                            // 2. Cập nhật tab Cảnh báo (ID: tab-alerts)
                            const alertsTab = document.getElementById('tab-alerts');
                            if (alertsTab && doc.getElementById('tab-alerts')) {
                                alertsTab.innerHTML = doc.getElementById('tab-alerts').innerHTML;
                            }

                            // 3. Cập nhật tab Logs
                            const logBody = document.getElementById('logBody');
                            if (logBody && doc.getElementById('logBody')) {
                                logBody.innerHTML = doc.getElementById('logBody').innerHTML;
                            }

                            // 4. Chỉ cập nhật badge trên Sidebar thay vì toàn bộ Sidebar
                            // Để không làm mất các event listeners của thẻ <a>
                            const oldAlertLink = document.querySelector('a[data-wh-tab="tab-alerts"]');
                            const newAlertLink = doc.querySelector('a[data-wh-tab="tab-alerts"]');
                            if (oldAlertLink && newAlertLink) {
                                oldAlertLink.innerHTML = newAlertLink.innerHTML;
                            }

                            // 5. Cập nhật badge trên thanh tabs
                            const oldAlertTabBtn = document.getElementById('alert-tab');
                            const newAlertTabBtn = doc.getElementById('alert-tab');
                            if (oldAlertTabBtn && newAlertTabBtn) {
                                oldAlertTabBtn.innerHTML = newAlertTabBtn.innerHTML;
                            }

                            // Lưu ý: Không cập nhật HTML của #tab-dashboard vì nó chứa các script 
                            // sẽ không chạy nếu thêm bằng innerHTML và số lượng bán ra không thay đổi
                            // khi update tồn kho thủ công.
                            
                            // Cập nhật Header warning badge (nếu có)
                            const topHeader = document.querySelector('.wh-top-header');
                            if (topHeader && doc.querySelector('.wh-top-header')) {
                                topHeader.innerHTML = doc.querySelector('.wh-top-header').innerHTML;
                            }

                            // Áp dụng lại các bộ lọc hiện tại để giao diện không bị giật/reset
                            if (typeof filterInventory === 'function') filterInventory();
                            if (typeof filterLogs === 'function') filterLogs();
                        })
                        .catch(err => {
                            console.error("Lỗi khi tải lại giao diện:", err);
                            // Nếu lỗi AJAX thì fallback load lại trang
                            setTimeout(() => location.reload(), 1800);
                        });
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
function openLogModal(variantId, sku) {
    document.getElementById('log-sku-title').textContent = sku ?? 'SKU';

    // Reset states
    document.getElementById('logLoading').classList.remove('d-none');
    document.getElementById('logTableWrapper').classList.add('d-none');
    document.getElementById('logEmpty').classList.add('d-none');
    document.getElementById('logModalBody').innerHTML = '';
    document.getElementById('logVariantProduct').textContent = '–';
    document.getElementById('logVariantDetail').textContent = '–';
    document.getElementById('logVariantQty').textContent = '–';

    const modal = new bootstrap.Modal(document.getElementById('skuLogModal'));
    modal.show();

    // Fetch logs from API
    fetch(`/warehouse/api/variants/${variantId}/logs`)
        .then(r => r.json())
        .then(data => {
            document.getElementById('logLoading').classList.add('d-none');

            // Fill variant info bar
            document.getElementById('logVariantProduct').textContent = data.productName ?? '–';
            document.getElementById('logVariantDetail').textContent =
                `Màu: ${data.color || '–'} · Size: ${data.size || '–'}`;
            document.getElementById('logVariantQty').textContent =
                `Tồn hiện tại: ${data.currentQty ?? 0}`;

            const logs = data.logs ?? [];
            if (logs.length === 0) {
                document.getElementById('logEmpty').classList.remove('d-none');
                return;
            }

            document.getElementById('logTableWrapper').classList.remove('d-none');
            const tbody = document.getElementById('logModalBody');

            logs.forEach((log, i) => {
                // Type badge
                let typeBadge;
                if (log.type === 'in')       typeBadge = '<span class="badge bg-success">Nhập kho</span>';
                else if (log.type === 'out')  typeBadge = '<span class="badge bg-danger">Xuất kho</span>';
                else                          typeBadge = '<span class="badge bg-primary">Điều chỉnh</span>';

                // Change indicator
                let changeText;
                if (log.changeAmount > 0)       changeText = `<span class="text-success fw-bold">+${log.changeAmount}</span>`;
                else if (log.changeAmount < 0)  changeText = `<span class="text-danger fw-bold">${log.changeAmount}</span>`;
                else                            changeText = `<span class="text-muted">0</span>`;

                // Format timestamp
                let ts = '–';
                if (log.timestamp) {
                    const d = new Date(log.timestamp);
                    ts = `${String(d.getDate()).padStart(2,'0')}/${String(d.getMonth()+1).padStart(2,'0')}/${d.getFullYear()} `
                       + `${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`;
                }

                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td class="text-center text-muted">${i + 1}</td>
                    <td class="text-nowrap">${ts}</td>
                    <td class="text-center">${typeBadge}</td>
                    <td class="text-end">${log.oldQuantity}</td>
                    <td class="text-center">${changeText}</td>
                    <td class="text-end fw-bold">${log.newQuantity}</td>
                    <td>${log.note ? `<small>${log.note}</small>` : '<span class="text-muted">–</span>'}</td>
                    <td><small>${log.accountName ?? '–'}</small></td>
                `;
                tbody.appendChild(tr);
            });
        })
        .catch(err => {
            console.error('Lỗi tải log:', err);
            document.getElementById('logLoading').classList.add('d-none');
            document.getElementById('logEmpty').classList.remove('d-none');
            document.getElementById('logEmpty').querySelector('p').textContent = 'Lỗi khi tải dữ liệu. Vui lòng thử lại.';
        });
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

// ── Export Excel (.xlsx) via SheetJS ──────────────────────────────
function exportCSV() {
    if (typeof XLSX === 'undefined') {
        alert('Thư viện xuất file chưa tải xong. Vui lòng thử lại sau vài giây.');
        return;
    }

    showToast('Đang xuất file Excel… Vui lòng chờ.');

    const tbody = document.getElementById('inventoryBody');
    if (!tbody) { alert('Không tìm thấy bảng dữ liệu.'); return; }

    const now = new Date();
    const dateStr = now.toLocaleDateString('vi-VN');
    const hourStr = now.toLocaleTimeString('vi-VN');

    // ── Collect visible rows from DOM ───────────────────────────────
    const dataRows = [];
    tbody.querySelectorAll('tr').forEach(tr => {
        if (tr.style.display === 'none') return;
        const cells = tr.querySelectorAll('td');
        if (cells.length === 0) return;

        const sku      = cells[1]?.innerText?.trim() ?? '';
        const product  = cells[2]?.querySelector('.fw-semibold, .fw-bold, span, div')?.innerText?.trim()
                       ?? cells[2]?.innerText?.trim() ?? '';
        const category = cells[3]?.innerText?.trim() ?? '';
        const color    = cells[4]?.innerText?.trim() ?? '';
        const size     = cells[5]?.innerText?.trim() ?? '';
        const qtyRaw   = cells[6]?.innerText?.trim() ?? '';
        const qty      = parseInt(qtyRaw) || 0;
        const levelEl  = cells[7]?.querySelector('[aria-valuenow]');
        const levelPct = levelEl ? parseInt(levelEl.getAttribute('aria-valuenow')) : null;
        const status   = cells[8]?.innerText?.trim() ?? '';

        dataRows.push({ sku, product, category, color, size, qty, levelPct, status });
    });

    if (dataRows.length === 0) { alert('Không có dữ liệu để xuất.'); return; }

    // ── Build worksheet data array (each item = one cell row) ──────
    // Row 1: Title
    const titleRow  = ['DANH SÁCH TỒN KHO - SHOP OMG!', '', '', '', '', '', '', '', ''];
    // Row 2: Metadata
    const metaRow   = [`Ngày xuất: ${dateStr} ${hourStr}`, '', '', '', '', `Tổng SKU: ${dataRows.length}`, '', '', ''];
    // Row 3: blank spacer
    const spacer    = ['', '', '', '', '', '', '', '', ''];
    // Row 4: headers
    const headers   = ['STT', 'SKU', 'Sản phẩm', 'Danh mục', 'Màu sắc', 'Size', 'Tồn kho', 'Mức tồn (%)', 'Trạng thái'];

    const wsData = [titleRow, metaRow, spacer, headers];

    dataRows.forEach((row, i) => {
        wsData.push([
            i + 1,
            row.sku,
            row.product,
            row.category,
            row.color,
            row.size,
            row.qty,
            row.levelPct ?? '',
            row.status,
        ]);
    });

    // ── Create worksheet ────────────────────────────────────────────
    const ws = XLSX.utils.aoa_to_sheet(wsData);

    // Column widths (in characters)
    ws['!cols'] = [
        { wch: 6 },   // STT
        { wch: 14 },  // SKU
        { wch: 30 },  // Sản phẩm
        { wch: 16 },  // Danh mục
        { wch: 10 },  // Màu sắc
        { wch: 8 },   // Size
        { wch: 10 },  // Tồn kho
        { wch: 13 },  // Mức tồn %
        { wch: 16 },  // Trạng thái
    ];

    // Merge title across all columns (A1:I1)
    ws['!merges'] = [
        { s: { r: 0, c: 0 }, e: { r: 0, c: 8 } }, // title row
        { s: { r: 1, c: 0 }, e: { r: 1, c: 4 } }, // meta left
        { s: { r: 1, c: 5 }, e: { r: 1, c: 8 } }, // meta right
    ];

    // ── Create workbook and download ────────────────────────────────
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Tồn kho');

    const fileName = `ton-kho-${now.getFullYear()}${String(now.getMonth()+1).padStart(2,'0')}${String(now.getDate()).padStart(2,'0')}.xlsx`;
    XLSX.writeFile(wb, fileName);
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

