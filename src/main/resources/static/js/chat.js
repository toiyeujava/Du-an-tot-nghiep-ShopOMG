// 1. Lấy user từ biến toàn cục bên home.html
var username = window.chatUser;
var stompClient = null;
var pendingMedia = null;

function connect() {
    if (!username) { console.log("Chưa đăng nhập -> Không kết nối Chat."); return; }

    var socket = new SockJS('/ws-chat');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    const csrfToken  = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const csrfHeader = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");
    const headers = {};
    if (csrfHeader && csrfToken) headers[csrfHeader] = csrfToken;

    stompClient.connect(headers, function(frame) {
        console.log('Đã kết nối: ' + username);
        stompClient.subscribe('/user/queue/messages', function(message) {
            var msgBody = JSON.parse(message.body);
            if (msgBody.sender === "Admin") {
                showMessage(msgBody, "bot");
                var chatBox = document.getElementById("chatBox");
                if (chatBox && chatBox.style.display === "none") {
                    // Chat box đang ẩn -> tăng badge
                    incrementChatToggleBadge();
                } else {
                    chatBox.style.display = "flex";
                }
            }
        });
    });
}

function sendMessage() {
    var input = document.getElementById("chatInput");
    if (!input || !stompClient || !username) return;

    var text = input.value.trim();

    // Gửi kèm media
    if (pendingMedia) {
        var msg = { sender: username, recipient: "Admin", content: text || "", mediaUrl: pendingMedia.url, mediaType: pendingMedia.mediaType };
        stompClient.send("/app/chat.sendToAdmin", {}, JSON.stringify(msg));
        showMessage(msg, "user");
        cancelMediaPreview();
        input.value = "";
        return;
    }

    if (text) {
        stompClient.send("/app/chat.sendToAdmin", {}, JSON.stringify({ sender: username, recipient: "Admin", content: text }));
        showMessage({ content: text }, "user");
        input.value = "";
    }
}

// Upload file
function handleChatFileSelect(event) {
    var file = event.target.files[0];
    if (!file) return;

    var formData = new FormData();
    formData.append('file', file);

    const csrfToken  = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const csrfHeader = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");
    const headers = {};
    if (csrfHeader && csrfToken) headers[csrfHeader] = csrfToken;

    fetch('/api/chat/upload', { method: 'POST', headers, body: formData })
        .then(r => r.json())
        .then(data => {
            if (data.error) { alert(data.error); return; }
            pendingMedia = { url: data.url, mediaType: data.mediaType };

            // Hiện preview nhỏ
            var preview = document.getElementById('chatMediaPreview');
            if (preview) {
                preview.style.display = 'flex';
                preview.querySelector('.preview-thumb').innerHTML = data.mediaType === 'image'
                    ? '<img src="' + data.url + '" style="height:40px;border-radius:6px;">'
                    : '<video src="' + data.url + '" style="height:40px;border-radius:6px;"></video>';
            }
        })
        .catch(() => alert('Lỗi upload!'));

    event.target.value = '';
}

function cancelMediaPreview() {
    pendingMedia = null;
    var preview = document.getElementById('chatMediaPreview');
    if (preview) { preview.style.display = 'none'; preview.querySelector('.preview-thumb').innerHTML = ''; }
}

function showMessage(message, type) {
    var chatBody = document.getElementById("chatBody");
    if (!chatBody) return;

    var div = document.createElement("div");
    div.className = (type === "user") ? "message user" : "message bot";

    if (message.mediaUrl) {
        if (message.content) { var p = document.createElement('p'); p.style.margin='0 0 4px 0'; p.innerText = message.content; div.appendChild(p); }
        if (message.mediaType === 'image') {
            var img = document.createElement('img');
            img.src = message.mediaUrl;
            img.style.cssText = 'max-width:180px;max-height:140px;border-radius:8px;display:block;cursor:pointer;';
            img.onclick = function() { window.open(message.mediaUrl, '_blank'); };
            div.appendChild(img);
        } else {
            var vid = document.createElement('video');
            vid.src = message.mediaUrl;
            vid.style.cssText = 'max-width:200px;border-radius:8px;display:block;';
            vid.controls = true;
            div.appendChild(vid);
        }
    } else {
        div.innerText = message.content;
    }

    chatBody.appendChild(div);
    chatBody.scrollTop = chatBody.scrollHeight;
}

function toggleChatBox() {
    var chatBox = document.getElementById("chatBox");
    if (chatBox.style.display === "none" || chatBox.style.display === "") {
        chatBox.style.display = "flex";
        clearChatToggleBadge(); // Xóa badge khi mở
        setTimeout(() => { var input = document.getElementById("chatInput"); if (input) input.focus(); }, 100);
    } else {
        chatBox.style.display = "none";
    }
}

var chatUnreadCount = 0;

function incrementChatToggleBadge() {
    chatUnreadCount++;
    var badge = document.getElementById('chatToggleBadge');
    if (badge) {
        badge.textContent = chatUnreadCount > 9 ? '9+' : chatUnreadCount;
        badge.style.display = 'flex';
    }
}

function clearChatToggleBadge() {
    chatUnreadCount = 0;
    var badge = document.getElementById('chatToggleBadge');
    if (badge) badge.style.display = 'none';
}

function handleEnter(e) { if (e.key === 'Enter') sendMessage(); }

function loadHistory() {
    if (!username) return;
    const csrfToken  = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const csrfHeader = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");
    fetch('/api/chat/history?user=' + username, {
        headers: { ...(csrfHeader && csrfToken ? { [csrfHeader]: csrfToken } : {}) }
    })
    .then(r => r.json())
    .then(messages => {
        messages.forEach(msg => {
            let type = (msg.sender === username) ? "user" : "bot";
            showMessage(msg, type);
        });
    })
    .catch(err => console.error("Lỗi load history:", err));
}

loadHistory();
connect();