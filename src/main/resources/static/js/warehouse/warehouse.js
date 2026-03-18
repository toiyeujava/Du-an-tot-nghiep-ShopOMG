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
let currentInventoryPage = 1;
const INVENTORY_PAGE_SIZE = 10;

function filterInventory() {
    const search = document.getElementById('skuSearch')?.value.toLowerCase() ?? '';
    const category = document.getElementById('categoryFilter')?.value ?? '';
    const stock = document.getElementById('stockFilter')?.value ?? '';

    const rows = document.querySelectorAll('#inventoryBody tr');
    
    // First, collect all matching rows
    const matchedRows = [];
    rows.forEach(row => {
        const sku = (row.dataset.sku ?? '').toLowerCase();
        const cat = row.dataset.category ?? '';
        const level = row.dataset.stockLevel ?? '';
        const text = row.textContent.toLowerCase();

        const matchSearch = !search || sku.includes(search) || text.includes(search);
        const matchCategory = !category || cat === category;
        const matchStock = !stock || level === stock;

        if (matchSearch && matchCategory && matchStock) {
            matchedRows.push(row);
        } else {
            row.style.display = 'none';
        }
    });

    // Calculate pagination
    const totalItems = matchedRows.length;
    const totalPages = Math.ceil(totalItems / INVENTORY_PAGE_SIZE) || 1;
    
    if (currentInventoryPage > totalPages) {
        currentInventoryPage = totalPages;
    }
    if (currentInventoryPage < 1) currentInventoryPage = 1;

    const startIndex = (currentInventoryPage - 1) * INVENTORY_PAGE_SIZE;
    const endIndex = Math.min(startIndex + INVENTORY_PAGE_SIZE, totalItems);

    // Apply visibility based on page
    matchedRows.forEach((row, index) => {
        if (index >= startIndex && index < endIndex) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });

    // Update UI info
    const countEl = document.getElementById('rowCount');
    if (countEl) {
        countEl.innerHTML = `Đang tìm được <strong>${totalItems}</strong> SKU`;
    }
    
    const pageInfo = document.getElementById('inventoryPageInfo');
    if (pageInfo) {
        pageInfo.innerHTML = totalItems === 0 ? 'Không có dữ liệu' : `Hiển thị từ ${startIndex + 1} đến ${endIndex} trong tổng số ${totalItems} SKU`;
    }

    renderInventoryPagination(totalPages);
}

function renderInventoryPagination(totalPages) {
    const paginationEl = document.getElementById('inventoryPagination');
    if (!paginationEl) return;
    
    let html = '';
    
    html += `<li class="page-item ${currentInventoryPage === 1 ? 'disabled' : ''}">
                <a class="page-link" href="javascript:void(0)" onclick="goToInventoryPage(${currentInventoryPage - 1})">Trước</a>
             </li>`;
             
    for (let i = 1; i <= totalPages; i++) {
        if (totalPages > 7) {
            if (i !== 1 && i !== totalPages && Math.abs(i - currentInventoryPage) > 2) {
                if (Math.abs(i - currentInventoryPage) === 3) {
                    html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
                }
                continue;
            }
        }
        
        html += `<li class="page-item ${i === currentInventoryPage ? 'active' : ''}">
                    <a class="page-link" href="javascript:void(0)" onclick="goToInventoryPage(${i})">${i}</a>
                 </li>`;
    }
    
    html += `<li class="page-item ${currentInventoryPage === totalPages ? 'disabled' : ''}">
                <a class="page-link" href="javascript:void(0)" onclick="goToInventoryPage(${currentInventoryPage + 1})">Sau</a>
             </li>`;
             
    paginationEl.innerHTML = html;
}

function goToInventoryPage(page) {
    currentInventoryPage = page;
    filterInventory();
}

// ── Update on Filter Change ───────────────────────────────────────
document.getElementById('skuSearch')?.addEventListener('input', () => { currentInventoryPage = 1; filterInventory(); });
document.getElementById('categoryFilter')?.addEventListener('change', () => { currentInventoryPage = 1; filterInventory(); });
document.getElementById('stockFilter')?.addEventListener('change', () => { currentInventoryPage = 1; filterInventory(); });

document.addEventListener('DOMContentLoaded', () => {
    filterInventory(); // Run on load
});


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
    'tab-suppliers': 'suppliers-tab',
    'tab-receipts': 'receipts-tab'
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
                'tab-suppliers': '/warehouse/inventory#tab-suppliers',
                'tab-receipts': '/warehouse/inventory#tab-receipts'
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

    // ── Bind Product Search for Receipt Modal ──────────────────────────────
    const searchInput = document.getElementById('receiptProductSearch');
    if (searchInput) {
        searchInput.addEventListener('input', function(e) {
            const query = e.target.value.trim();
            const resultsContainer = document.getElementById('receiptSearchResults');
            const supplierId = document.getElementById('receiptSupplier')?.value;
            
            if (!supplierId) {
                resultsContainer.innerHTML = `<div class="p-3 text-danger text-center small">Vui lòng chọn Nhà cung cấp trước khi tìm kiếm sản phẩm.</div>`;
                return;
            }

            if(query.length < 2) {
                resultsContainer.innerHTML = '';
                return;
            }
            
            fetch(`/warehouse/api/variants/search?q=${encodeURIComponent(query)}&supplierId=${supplierId}`)
                .then(r => r.json())
                .then(variants => {
                    resultsContainer.innerHTML = '';
                    if(!variants || variants.length === 0) {
                        resultsContainer.innerHTML = `<div class="p-3 text-muted text-center small">Không tìm thấy sản phẩm.</div>`;
                        return;
                    }
                    variants.forEach(v => {
                        const item = document.createElement('a');
                        item.href = '#';
                        item.className = 'list-group-item list-group-item-action d-flex justify-content-between align-items-center py-2';
                        item.innerHTML = `
                            <div style="max-width: 80%;">
                                <div class="fw-bold small text-truncate">${v.product.name}</div>
                                <div class="text-muted" style="font-size: 0.75rem;">SKU: <span class="fw-semibold">${v.sku}</span> | Màu: ${v.color} | Size: ${v.size}</div>
                            </div>
                            <button class="btn btn-sm btn-outline-warning rounded-circle p-1" style="width:28px;height:28px;" title="Thêm">
                                <i class="fas fa-plus fa-xs"></i>
                            </button>
                        `;
                        item.onclick = (ev) => {
                            ev.preventDefault();
                            addVariantToReceipt(v);
                            
                            // Clear search
                            document.getElementById('receiptProductSearch').value = '';
                            resultsContainer.innerHTML = '';
                            document.getElementById('receiptProductSearch').focus();
                        };
                        resultsContainer.appendChild(item);
                    });
                });
        });
    }

});

// ═══════════════════════════════════════════════════════════════════
// SUPPLIER MANAGEMENT
// ═══════════════════════════════════════════════════════════════════

function loadSuppliers() {
    fetch('/warehouse/api/suppliers')
        .then(r => r.json())
        .then(data => {
            const tbody = document.getElementById('suppliersBody');
            if (!tbody) return;
            tbody.innerHTML = '';
            
            if (!data || data.length === 0) {
                tbody.innerHTML = `<tr><td colspan="8" class="text-center py-4 text-muted">Chưa có nhà cung cấp nào.</td></tr>`;
                return;
            }

            data.forEach(s => {
                const statusBadge = s.isActive 
                    ? `<span class="badge bg-success bg-opacity-10 text-success fw-bold p-2" style="font-size: 0.75rem;">Đang hoạt động</span>`
                    : `<span class="badge bg-danger bg-opacity-10 text-danger fw-bold p-2" style="font-size: 0.75rem;">Ngừng hoạt động</span>`;
                
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td class="text-muted fw-semibold">#${s.id}</td>
                    <td class="fw-bold text-dark">${s.name}</td>
                    <td>${s.phone || '-'}</td>
                    <td>${s.email || '-'}</td>
                    <td>${s.address || '-'}</td>
                    <td>${s.taxCode || '-'}</td>
                    <td class="text-center">${statusBadge}</td>
                    <td class="text-end">
                        <button class="btn btn-sm btn-outline-primary shadow-sm" onclick='editSupplier(${JSON.stringify(s).replace(/'/g, "&#39;")})' title="Chi tiết/Sửa">
                            <i class="fas fa-edit"></i>
                        </button>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        })
        .catch(err => console.error("Error loading suppliers", err));
}

function openSupplierModal() {
    document.getElementById('supplierForm').reset();
    document.getElementById('supplierId').value = '';
    document.getElementById('supplierModalTitle').textContent = 'Thêm Nhà Cung Cấp';
    document.getElementById('supplierStatusContainer').style.display = 'none';
    const modal = new bootstrap.Modal(document.getElementById('supplierModal'));
    modal.show();
}

function editSupplier(s) {
    document.getElementById('supplierId').value = s.id;
    document.getElementById('supplierName').value = s.name;
    document.getElementById('supplierPhone').value = s.phone || '';
    document.getElementById('supplierTaxCode').value = s.taxCode || '';
    document.getElementById('supplierEmail').value = s.email || '';
    document.getElementById('supplierAddress').value = s.address || '';
    
    document.getElementById('supplierModalTitle').textContent = 'Cập Nhật Nhà Cung Cấp';
    document.getElementById('supplierStatusContainer').style.display = 'block';
    document.getElementById('supplierIsActive').checked = s.isActive;
    
    const modal = new bootstrap.Modal(document.getElementById('supplierModal'));
    modal.show();
}

function saveSupplier() {
    const id = document.getElementById('supplierId').value;
    const isNew = !id;
    
    const data = {
        name: document.getElementById('supplierName').value,
        phone: document.getElementById('supplierPhone').value,
        taxCode: document.getElementById('supplierTaxCode').value,
        email: document.getElementById('supplierEmail').value,
        address: document.getElementById('supplierAddress').value,
        isActive: isNew ? true : document.getElementById('supplierIsActive').checked
    };
    
    if(!data.name) {
        alert("Vui lòng nhập tên nhà cung cấp");
        return;
    }
    
    const csrf = getCsrf();
    const url = isNew ? '/warehouse/api/suppliers' : `/warehouse/api/suppliers/${id}`;
    const method = isNew ? 'POST' : 'PUT';
    
    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json',
            [csrf.header]: csrf.token
        },
        body: JSON.stringify(data)
    })
    .then(r => {
        if(!r.ok) throw new Error("Server error");
        return r.json();
    })
    .then(saved => {
        bootstrap.Modal.getInstance(document.getElementById('supplierModal')).hide();
        showToast(isNew ? "Thêm mới thành công!" : "Cập nhật thành công!");
        loadSuppliers();
    })
    .catch(err => alert("Lỗi hệ thống khi lưu nhà cung cấp."));
}

// ═══════════════════════════════════════════════════════════════════
// INVENTORY RECEIPT MANAGEMENT
// ═══════════════════════════════════════════════════════════════════

let receiptLineItems = [];
let currentReceiptId = null;

function loadReceipts() {
    fetch('/warehouse/api/receipts?t=' + new Date().getTime())
        .then(r => r.json())
        .then(data => {
            const tbody = document.getElementById('receiptsBody');
            if (!tbody) return;
            tbody.innerHTML = '';
            
            if (!data || data.length === 0) {
                tbody.innerHTML = `<tr><td colspan="7" class="text-center py-4 text-muted">Chưa có phiếu nhập nào.</td></tr>`;
                return;
            }

            data.forEach(r => {
                let statusBadge = '';
                if(r.status === 'PENDING') statusBadge = `<span class="badge bg-warning bg-opacity-10 text-warning fw-bold p-2" style="font-size: 0.75rem;">Đang nháp</span>`;
                else if(r.status === 'COMPLETED') statusBadge = `<span class="badge bg-success bg-opacity-10 text-success fw-bold p-2" style="font-size: 0.75rem;">Đã hoàn thành</span>`;
                else statusBadge = `<span class="badge bg-danger bg-opacity-10 text-danger fw-bold p-2" style="font-size: 0.75rem;">Đã huỷ</span>`;
                
                let ts = '-';
                if(r.createdAt) {
                    const d = new Date(r.createdAt);
                    ts = `${String(d.getDate()).padStart(2,'0')}/${String(d.getMonth()+1).padStart(2,'0')}/${d.getFullYear()} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`;
                }
                
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td class="fw-bold text-dark">${r.receiptCode}</td>
                    <td>${r.supplier ? r.supplier.name : '-'}</td>
                    <td>${r.account ? r.account.fullName : '-'}</td>
                    <td class="text-end text-danger fw-bold">${r.totalAmount ? r.totalAmount.toLocaleString('vi-VN') : '0'} đ</td>
                    <td>${ts}</td>
                    <td class="text-center">${statusBadge}</td>
                    <td class="text-end">
                        <button class="btn btn-sm btn-outline-info shadow-sm me-1" onclick="viewReceipt(${r.id})" title="Xem chi tiết">
                            <i class="fas fa-eye"></i>
                        </button>
                        <a href="/warehouse/api/receipts/${r.id}/export" class="btn btn-sm btn-outline-success shadow-sm" title="Xuất file Excel" target="_blank">
                            <i class="fas fa-file-excel"></i>
                        </a>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        })
        .catch(err => console.error("Error loading receipts", err));
}

function openInventoryReceiptModal() {
    currentReceiptId = null;
    receiptLineItems = [];
    document.getElementById('receiptTotalAmount').textContent = '0';
    document.getElementById('receiptNote').value = '';
    document.getElementById('receiptNote').readOnly = false;
    document.getElementById('receiptProductSearch').value = '';
    document.getElementById('receiptSearchResults').innerHTML = '';
    document.getElementById('receiptModalTitle').textContent = 'Lập Phiếu Nhập Kho';
    document.getElementById('receiptStatusBadge').innerHTML = '<span class="badge bg-warning text-dark fw-bold">LẬP MỚI</span>';
    
    document.getElementById('productSearchPanel').style.display = 'block';
    
    const supplierContainer = document.getElementById('supplierSelectContainer');
    if (supplierContainer) {
        supplierContainer.style.display = 'block';
    }
    
    document.getElementById('receiptSupplierReadonlyContainer').style.display = 'none';
    document.getElementById('receiptActionButtons').style.display = 'block';
    
    // load supplier options
    fetch('/warehouse/api/suppliers')
        .then(r => r.json())
        .then(data => {
            const select = document.getElementById('receiptSupplier');
            select.innerHTML = '<option value="">-- Chọn Nhà cung cấp --</option>';
            data.filter(s => s.isActive).forEach(s => {
                const opt = document.createElement('option');
                opt.value = s.id;
                opt.textContent = s.name;
                select.appendChild(opt);
            });
        });
        
    renderReceiptItems();
    
    const modal = new bootstrap.Modal(document.getElementById('inventoryReceiptModal'));
    modal.show();
}

function renderReceiptItems() {
    const tbody = document.getElementById('receiptLineItems');
    const emptyState = document.getElementById('receiptEmptyItems');
    
    if (receiptLineItems.length === 0) {
        tbody.innerHTML = '';
        emptyState.classList.remove('d-none');
        document.getElementById('receiptTotalAmount').textContent = '0';
        return;
    }
    
    emptyState.classList.add('d-none');
    tbody.innerHTML = '';
    
    let total = 0;
    
    receiptLineItems.forEach((item, index) => {
        const itemTotal = item.quantity * item.importPrice;
        total += itemTotal;
        
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><span class="badge bg-secondary">${item.sku}</span></td>
            <td>
                <div class="fw-bold text-dark text-truncate" style="max-width:200px;" title="${item.productName}">${item.productName}</div>
                <div class="small text-muted">Màu: ${item.color} | Size: ${item.size}</div>
            </td>
            <td class="text-center">
                <input type="number" class="form-control form-control-sm text-center" min="1" value="${item.quantity}" onchange="updateReceiptItemQty(${index}, this.value)">
            </td>
            <td class="text-end">
                <div class="input-group input-group-sm">
                    <input type="number" class="form-control text-end" min="0" value="${item.importPrice}" onchange="updateReceiptItemPrice(${index}, this.value)">
                    <span class="input-group-text">đ</span>
                </div>
            </td>
            <td class="text-end fw-bold text-danger">${itemTotal.toLocaleString('vi-VN')} đ</td>
            <td class="text-center">
                <button class="btn btn-sm btn-outline-danger border-0" onclick="removeReceiptItem(${index})" title="Xoá">
                    <i class="fas fa-trash-alt"></i>
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });
    
    document.getElementById('receiptTotalAmount').textContent = total.toLocaleString('vi-VN');
}

function updateReceiptItemQty(index, val) {
    let q = parseInt(val);
    if(isNaN(q) || q < 1) q = 1;
    receiptLineItems[index].quantity = q;
    renderReceiptItems();
}

function updateReceiptItemPrice(index, val) {
    let p = parseFloat(val);
    if(isNaN(p) || p < 0) p = 0;
    receiptLineItems[index].importPrice = p;
    renderReceiptItems();
}

function removeReceiptItem(index) {
    receiptLineItems.splice(index, 1);
    renderReceiptItems();
}



function addVariantToReceipt(v) {
    // Check if exists
    const existing = receiptLineItems.find(item => item.variantId === v.id);
    if(existing) {
        existing.quantity += 1;
    } else {
        receiptLineItems.push({
            variantId: v.id,
            sku: v.sku,
            productName: v.product.name,
            color: v.color,
            size: v.size,
            quantity: 1,
            importPrice: v.price || 0 // Default price roughly, user should edit
        });
    }
    renderReceiptItems();
}

function buildReceiptRequestBody() {
    const supplierId = document.getElementById('receiptSupplier').value;
    if(!supplierId) {
        alert("Vui lòng chọn nhà cung cấp!");
        return null;
    }
    if(receiptLineItems.length === 0) {
        alert("Phiếu nhập chưa có sản phẩm nào!");
        return null;
    }
    
    return {
        supplierId: parseInt(supplierId),
        note: document.getElementById('receiptNote').value,
        details: receiptLineItems.map(item => ({
            variantId: item.variantId,
            quantity: item.quantity,
            importPrice: item.importPrice
        }))
    };
}

function saveReceiptDraft() {
    const reqBody = buildReceiptRequestBody();
    if(!reqBody) return;
    
    const csrf = getCsrf();
    const isUpdate = currentReceiptId != null;
    const url = isUpdate ? `/warehouse/api/receipts/${currentReceiptId}` : '/warehouse/api/receipts';
    const method = isUpdate ? 'PUT' : 'POST';

    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json',
            [csrf.header]: csrf.token
        },
        body: JSON.stringify(reqBody)
    })
    .then(r => {
        if (!r.ok) throw new Error('Server error');
        return r.json();
    })
    .then(receipt => {
        bootstrap.Modal.getInstance(document.getElementById('inventoryReceiptModal')).hide();
        Swal.fire('Thành công!', 'Đã lưu nháp phiếu nhập kho.', 'success');
        loadReceipts();
    })
    .catch(err => {
        console.error("Lỗi khi lưu nháp", err);
        Swal.fire('Lỗi!', 'Hệ thống gặp sự cố khi lưu nháp.', 'error');
    });
}

function completeReceipt() {
    console.log("=> begin completeReceipt()");
    const reqBody = buildReceiptRequestBody();
    if(!reqBody) {
        console.log("buildReceiptRequestBody returned null, aborting.");
        return;
    }
    
    console.log("Request body built:", reqBody);
    
    // Add a native confirm just in case Swal is acting up or blocked
    Swal.fire({
        title: 'Xác nhận Hoàn Thành?',
        text: 'Số lượng kho của sản phẩm sẽ được tự động cộng và không thể sửa đổi sau khi hoàn thành.',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#3085d6',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Có, Hoàn thành!',
        cancelButtonText: 'Huỷ'
    }).then((result) => {
        console.log("Swal result:", result);
        if (result.isConfirmed) {
            const csrf = getCsrf();
            const isUpdate = currentReceiptId != null;
            const url = isUpdate ? `/warehouse/api/receipts/${currentReceiptId}` : '/warehouse/api/receipts';
            const method = isUpdate ? 'PUT' : 'POST';
            console.log(`Sending ${method} request to ${url}`);
            
            // First, Create or Update Receipt (Pending)
            fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json',
                    [csrf.header]: csrf.token
                },
                body: JSON.stringify(reqBody)
            })
            .then(r => {
                console.log("Update/Create response status:", r.status);
                if(!r.ok) {
                    return r.text().then(text => {
                        console.error("Server error text:", text);
                        throw new Error('Cập nhật phiếu lỗi: ' + text);
                    });
                }
                return r.json();
            })
            .then(receipt => {
                console.log("Successfully saved pending receipt. ID:", receipt.id);
                // Then Complete Receipt
                const completeUrl = `/warehouse/api/receipts/${receipt.id}/complete`;
                console.log("Calling completion URL:", completeUrl);
                return fetch(completeUrl, {
                    method: 'PUT',
                    headers: { [csrf.header]: csrf.token }
                });
            })
            .then(r => {
                console.log("Complete response status:", r.status);
                if (!r.ok) {
                    return r.text().then(text => {
                        console.error("Completion server error text:", text);
                        throw new Error('Server error completing: ' + text);
                    });
                }
                return r.json();
            })
            .then(completedReceipt => {
                console.log("Successfully completed receipt:", completedReceipt);
                try {
                    bootstrap.Modal.getInstance(document.getElementById('inventoryReceiptModal')).hide();
                } catch (e) {
                    console.error("Failed to hide modal:", e);
                }
                Swal.fire({
                    title: 'Thành công!',
                    text: 'Đã lập phiếu nhập thành công! Tồn kho đã được tự động cộng.',
                    icon: 'success'
                }).then(() => {
                    window.location.reload();
                });
            })
            .catch(err => {
                console.error("Lỗi khi lập phiếu Catch block:", err);
                Swal.fire(
                    'Lỗi!',
                    `Hệ thống gặp sự cố: ${err.message}`,
                    'error'
                );
            });
        }
    }).catch(err => {
        console.error("Lỗi Swal:", err);
    });
}

function viewReceipt(id) {
    fetch(`/warehouse/api/receipts/${id}`)
        .then(r => r.json())
        .then(data => {
            const isPending = data.status === 'PENDING';
            
            document.getElementById('receiptModalTitle').textContent = isPending ? `Sửa Phiếu Nháp: ${data.receiptCode}` : `Chi tiết Phiếu Nhập: ${data.receiptCode}`;
            
            let statusBadge = '';
            if(data.status === 'PENDING') statusBadge = `<span class="badge bg-warning text-dark fw-bold">ĐANG NHÁP</span>`;
            else if(data.status === 'COMPLETED') statusBadge = `<span class="badge bg-success fw-bold">HOÀN THÀNH</span>`;
            else statusBadge = `<span class="badge bg-danger fw-bold">ĐÃ HUỶ</span>`;
            document.getElementById('receiptStatusBadge').innerHTML = statusBadge;
            
            if (isPending) {
                currentReceiptId = data.id;
                document.getElementById('productSearchPanel').style.display = 'block';
                document.getElementById('receiptActionButtons').style.display = 'block';
                
                const supplierSelectContainer = document.getElementById('supplierSelectContainer');
                if(supplierSelectContainer) supplierSelectContainer.style.display = 'block';
                document.getElementById('receiptSupplierReadonlyContainer').style.display = 'none';
                
                // Dynamically fetch and populate suppliers to ensure the dropdown isn't empty when opening directly
                const select = document.getElementById('receiptSupplier');
                fetch('/warehouse/api/suppliers?t=' + new Date().getTime())
                    .then(res => res.json())
                    .then(suppliers => {
                        select.innerHTML = '<option value="">-- Chọn Nhà cung cấp --</option>';
                        suppliers.filter(s => s.isActive).forEach(s => {
                            const opt = document.createElement('option');
                            opt.value = s.id;
                            opt.textContent = s.name;
                            if (data.supplier && data.supplier.id === s.id) {
                                opt.selected = true;
                            }
                            select.appendChild(opt);
                        });
                    });
                
                const noteEl = document.getElementById('receiptNote');
                noteEl.value = data.note || '';
                noteEl.readOnly = false;
                
                receiptLineItems = (data.receiptDetails || []).map(d => ({
                    variantId: d.productVariant ? d.productVariant.id : null,
                    sku: d.productVariant ? d.productVariant.sku : '-',
                    productName: d.productVariant && d.productVariant.product ? d.productVariant.product.name : 'Unknown',
                    color: d.productVariant ? d.productVariant.color : '-',
                    size: d.productVariant ? d.productVariant.size : '-',
                    quantity: d.quantity,
                    importPrice: d.importPrice
                }));
                
                renderReceiptItems();
                
                const modal = new bootstrap.Modal(document.getElementById('inventoryReceiptModal'));
                modal.show();
            } else {
                currentReceiptId = null;
                document.getElementById('productSearchPanel').style.display = 'none';
                document.getElementById('receiptActionButtons').style.display = 'none';
                
                const supplierSelectContainer = document.getElementById('supplierSelectContainer');
                if(supplierSelectContainer) supplierSelectContainer.style.display = 'none';
                document.getElementById('receiptSupplierReadonlyContainer').style.display = 'block';
                document.getElementById('receiptSupplierReadonly').textContent = data.supplier ? data.supplier.name : '-';
                
                const noteEl = document.getElementById('receiptNote');
                noteEl.value = data.note || '';
                noteEl.readOnly = true;
                
                receiptLineItems = (data.receiptDetails || []).map(d => ({
                    variantId: d.productVariant ? d.productVariant.id : null,
                    sku: d.productVariant ? d.productVariant.sku : '-',
                    productName: d.productVariant && d.productVariant.product ? d.productVariant.product.name : 'Unknown',
                    color: d.productVariant ? d.productVariant.color : '-',
                    size: d.productVariant ? d.productVariant.size : '-',
                    quantity: d.quantity,
                    importPrice: d.importPrice
                }));
                
                const tbody = document.getElementById('receiptLineItems');
                document.getElementById('receiptEmptyItems').classList.add('d-none');
                tbody.innerHTML = '';
                
                let total = 0;
                
                receiptLineItems.forEach((item, index) => {
                    const itemTotal = item.quantity * item.importPrice;
                    total += itemTotal;
                    
                    const tr = document.createElement('tr');
                    tr.innerHTML = `
                        <td><span class="badge bg-secondary">${item.sku}</span></td>
                        <td>
                            <div class="fw-bold text-dark text-truncate" style="max-width:200px;" title="${item.productName}">${item.productName}</div>
                            <div class="small text-muted">Màu: ${item.color} | Size: ${item.size}</div>
                        </td>
                        <td class="text-center fw-bold">${item.quantity}</td>
                        <td class="text-end">${item.importPrice.toLocaleString('vi-VN')} đ</td>
                        <td class="text-end fw-bold text-danger">${itemTotal.toLocaleString('vi-VN')} đ</td>
                        <td class="text-center"></td>
                    `;
                    tbody.appendChild(tr);
                });
                
                document.getElementById('receiptTotalAmount').textContent = total.toLocaleString('vi-VN');
                
                const modal = new bootstrap.Modal(document.getElementById('inventoryReceiptModal'));
                modal.show();
            }
        });
}

// ── Hook up tab loading logic ──────────────────────────────
document.getElementById('suppliers-tab')?.addEventListener('show.bs.tab', loadSuppliers);
document.getElementById('receipts-tab')?.addEventListener('show.bs.tab', loadReceipts);

// Load initially if those are active 
if(document.getElementById('tab-suppliers')?.classList.contains('active')) loadSuppliers();
if(document.getElementById('tab-receipts')?.classList.contains('active')) loadReceipts();
