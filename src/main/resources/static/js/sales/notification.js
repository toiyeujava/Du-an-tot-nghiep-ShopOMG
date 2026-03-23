/**
 * notification.js - Bonus-1: Real-time Notification via WebSocket
 * Kết nối đến /ws-chat (SockJS + STOMP), lắng nghe /topic/sales/new-order
 * và hiển thị toast notification khi có đơn hàng mới.
 *
 * Cần include SockJS và StompJS trước file này (đã có trong sales-layout.html).
 */

(function () {
    'use strict';

    // ─── CONFIG ──────────────────────────────────────────────────────────────
    const WS_ENDPOINT  = '/ws-chat';
    const TOPIC        = '/topic/sales/new-order';
    const RECONNECT_MS = 5000;   // tự kết nối lại sau 5 giây nếu mất kết nối

    let stompClient = null;
    let reconnectTimer = null;

    // ─── CONNECT ─────────────────────────────────────────────────────────────
    function connect() {
        const socket = new SockJS(WS_ENDPOINT);
        stompClient = Stomp.over(socket);

        // Tắt log debug của STOMP (bỏ comment dòng dưới nếu muốn debug)
        stompClient.debug = null;

        const csrfToken = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
        const csrfHeader = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");
        const headers = {};
        if (csrfHeader && csrfToken) {
            headers[csrfHeader] = csrfToken;
        }

        stompClient.connect(
            headers,
            onConnected,
            onError
        );
    }

    function onConnected(frame) {
        console.log('[Notification] WebSocket connected:', frame);
        clearTimeout(reconnectTimer);

        // Cập nhật badge trạng thái kết nối (nếu có)
        setStatusBadge(true);

        // Subscribe vào topic nhận đơn hàng mới
        stompClient.subscribe(TOPIC, function (message) {
            try {
                const payload = JSON.parse(message.body);
                if (payload.type === 'NEW_ORDER') {
                    handleNewOrder(payload);
                }
            } catch (e) {
                console.error('[Notification] Lỗi parse message:', e);
            }
        });

        // Subscribe chat — chỉ đếm badge khi không ở trang chat
        if (window.location.pathname !== '/sales/chat') {
            stompClient.subscribe('/user/queue/messages', function (message) {
                try {
                    const data = JSON.parse(message.body);
                    incrementChatBadge(data.sender || null);
                } catch(e) {
                    incrementChatBadge(null);
                }
            });
        }
    }

    function onError(error) {
        console.warn('[Notification] WebSocket lỗi, thử kết nối lại sau', RECONNECT_MS, 'ms:', error);
        setStatusBadge(false);
        scheduleReconnect();
    }

    function scheduleReconnect() {
        clearTimeout(reconnectTimer);
        reconnectTimer = setTimeout(function () {
            console.log('[Notification] Đang kết nối lại WebSocket...');
            connect();
        }, RECONNECT_MS);
    }

    // ─── HANDLE NEW ORDER ────────────────────────────────────────────────────
    function handleNewOrder(payload) {
        // 1. Phát âm thanh thông báo (tuỳ chọn)
        playNotificationSound();

        // 2. Hiển thị toast
        showToast(payload);

        // 3. Cập nhật badge số đơn PENDING trên sidebar
        incrementPendingBadge();

        // 4. Cập nhật counter trên dashboard (nếu đang ở trang dashboard)
        updateDashboardCounter(payload);
    }

    // ─── TOAST ───────────────────────────────────────────────────────────────
    function showToast(payload) {
        ensureToastContainer();

        const amount = formatCurrency(payload.amount);
        const orderId = payload.orderId || '?';
        const customer = payload.customerName || 'Khách vãng lai';
        const time = payload.time || 'vừa xong';

        const toast = document.createElement('div');
        toast.className = 'notif-toast';
        toast.innerHTML = `
            <div class="notif-toast-icon">
                <i class="fas fa-shopping-cart"></i>
            </div>
            <div class="notif-toast-body">
                <div class="notif-toast-title">
                    <strong>Đơn hàng mới #${orderId}</strong>
                    <span class="notif-toast-time">${time}</span>
                </div>
                <div class="notif-toast-sub">
                    <span>${customer}</span> · <span class="text-success fw-bold">${amount}</span>
                </div>
            </div>
            <button class="notif-toast-close" onclick="this.closest('.notif-toast').remove()">
                <i class="fas fa-times"></i>
            </button>
        `;

        // Click vào toast → chuyển đến chi tiết đơn
        toast.addEventListener('click', function (e) {
            if (!e.target.closest('.notif-toast-close')) {
                window.location.href = '/sales/orders/' + orderId;
            }
        });

        const container = document.getElementById('notif-toast-container');
        container.prepend(toast);

        // Tự động xoá sau 6 giây
        setTimeout(function () {
            toast.classList.add('notif-toast-hide');
            setTimeout(function () { toast.remove(); }, 400);
        }, 6000);
    }

    function ensureToastContainer() {
        if (document.getElementById('notif-toast-container')) return;

        // Inject CSS
        const style = document.createElement('style');
        style.textContent = `
            #notif-toast-container {
                position: fixed;
                top: 20px;
                right: 20px;
                z-index: 9999;
                display: flex;
                flex-direction: column;
                gap: 10px;
                max-width: 340px;
            }
            .notif-toast {
                display: flex;
                align-items: center;
                gap: 12px;
                background: #fff;
                border: 1px solid #e5e7eb;
                border-left: 4px solid #22c55e;
                border-radius: 10px;
                padding: 12px 14px;
                box-shadow: 0 4px 16px rgba(0,0,0,0.12);
                cursor: pointer;
                animation: notifSlideIn 0.3s ease;
                transition: opacity 0.4s, transform 0.4s;
            }
            .notif-toast:hover {
                box-shadow: 0 6px 20px rgba(0,0,0,0.16);
                transform: translateX(-4px);
            }
            .notif-toast-hide {
                opacity: 0;
                transform: translateX(20px);
            }
            .notif-toast-icon {
                flex-shrink: 0;
                width: 38px;
                height: 38px;
                border-radius: 50%;
                background: #dcfce7;
                display: flex;
                align-items: center;
                justify-content: center;
                color: #16a34a;
                font-size: 15px;
            }
            .notif-toast-body { flex: 1; min-width: 0; }
            .notif-toast-title {
                display: flex;
                justify-content: space-between;
                align-items: center;
                font-size: 13.5px;
                margin-bottom: 3px;
            }
            .notif-toast-time {
                font-size: 11px;
                color: #9ca3af;
                font-weight: 400;
            }
            .notif-toast-sub { font-size: 12.5px; color: #6b7280; }
            .notif-toast-close {
                flex-shrink: 0;
                background: none;
                border: none;
                color: #9ca3af;
                cursor: pointer;
                padding: 2px 4px;
                font-size: 13px;
                line-height: 1;
                border-radius: 4px;
            }
            .notif-toast-close:hover { color: #374151; background: #f3f4f6; }
            @keyframes notifSlideIn {
                from { opacity: 0; transform: translateX(30px); }
                to   { opacity: 1; transform: translateX(0); }
            }
        `;
        document.head.appendChild(style);

        // Tạo container
        const container = document.createElement('div');
        container.id = 'notif-toast-container';
        document.body.appendChild(container);
    }

    // ─── SIDEBAR BADGE ────────────────────────────────────────────────────────
    function incrementPendingBadge() {
        // Badge trong sidebar: <span class="badge bg-danger"> th:text="${pendingCount}"
        const badge = document.querySelector('.admin-menu .badge.bg-danger');
        if (!badge) return;
        const current = parseInt(badge.textContent, 10) || 0;
        badge.textContent = current + 1;
        badge.style.display = '';  // đảm bảo hiển thị
    }

    // ─── DASHBOARD COUNTER ────────────────────────────────────────────────────
    function updateDashboardCounter(payload) {
        // Nếu đang ở trang dashboard, cập nhật số "Đơn hôm nay" và "Chờ xử lý"
        // Các phần tử này cần có data attribute tương ứng để nhận dạng
        // Ví dụ: <span id="stat-today-count">5</span>
        const todayEl   = document.getElementById('stat-today-count');
        const pendingEl = document.getElementById('stat-pending-count');

        if (todayEl) {
            const v = parseInt(todayEl.textContent, 10) || 0;
            todayEl.textContent = v + 1;
        }
        if (pendingEl) {
            const v = parseInt(pendingEl.textContent, 10) || 0;
            pendingEl.textContent = v + 1;
        }
    }

    // ─── STATUS BADGE (tuỳ chọn) ─────────────────────────────────────────────
    function setStatusBadge(connected) {
        // Nếu muốn hiển thị trạng thái kết nối WebSocket ở header,
        // thêm 1 element với id="ws-status" vào sales-layout.html
        const el = document.getElementById('ws-status');
        if (!el) return;
        el.className = connected
            ? 'badge bg-success-subtle text-success small'
            : 'badge bg-secondary-subtle text-secondary small';
        el.textContent = connected ? 'Live' : 'Offline';
    }

    // ─── SOUND (tuỳ chọn) ────────────────────────────────────────────────────
    function playNotificationSound() {
        // Web Audio API – tạo âm "ding" nhẹ, không cần file âm thanh
        try {
            const ctx = new (window.AudioContext || window.webkitAudioContext)();
            const oscillator = ctx.createOscillator();
            const gainNode   = ctx.createGain();

            oscillator.connect(gainNode);
            gainNode.connect(ctx.destination);

            oscillator.type = 'sine';
            oscillator.frequency.setValueAtTime(880, ctx.currentTime);
            oscillator.frequency.exponentialRampToValueAtTime(440, ctx.currentTime + 0.3);
            gainNode.gain.setValueAtTime(0.2, ctx.currentTime);
            gainNode.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.5);

            oscillator.start(ctx.currentTime);
            oscillator.stop(ctx.currentTime + 0.5);
        } catch (e) {
            // Bỏ qua nếu AudioContext không được hỗ trợ
        }
    }

    // ─── HELPER ──────────────────────────────────────────────────────────────
    function formatCurrency(amount) {
        if (!amount && amount !== 0) return '';
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(amount);
    }

    // ─── CHAT BADGE ──────────────────────────────────────────────────────────
    function incrementChatBadge(sender) {
        // 1. Tăng badge tổng trên sidebar
        const badge = document.getElementById('chat-sidebar-badge');
        if (badge) {
            const current = parseInt(badge.textContent, 10) || 0;
            badge.textContent = (current + 1) > 9 ? '9+' : (current + 1);
            badge.classList.remove('d-none');
        }
        // 2. Lưu unread theo từng sender vào sessionStorage
        if (sender) {
            const stored = JSON.parse(sessionStorage.getItem('chat_unread') || '{}');
            stored[sender] = (stored[sender] || 0) + 1;
            sessionStorage.setItem('chat_unread', JSON.stringify(stored));
        }
    }

    // ─── BOOT ────────────────────────────────────────────────────────────────
    // Khởi động khi DOM sẵn sàng
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', connect);
    } else {
        connect();
    }

})();