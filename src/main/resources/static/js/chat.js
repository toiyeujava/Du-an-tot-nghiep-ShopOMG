var stompClient = null;

// Kết nối ngay khi trang tải xong (hoặc khi bấm mở chat)
function connect() {
    var socket = new SockJS('/ws-chat'); // Kết nối tới endpoint đã config ở Java
    stompClient = Stomp.over(socket);
    
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        
        // Đăng ký nhận tin nhắn từ Server gửi về
        stompClient.subscribe('/topic/public', function (message) {
            showMessage(JSON.parse(message.body));
        });
    });
}

// Gọi hàm connect ngay khi file js chạy
connect();

function toggleChatBox() {
    var chatBox = document.getElementById("chatBox");
    if (chatBox.style.display === "none" || chatBox.style.display === "") {
        chatBox.style.display = "flex";
        setTimeout(() => document.getElementById("chatInput").focus(), 100);
    } else {
        chatBox.style.display = "none";
    }
}

function handleEnter(e) {
    if (e.key === 'Enter') {
        sendMessage();
    }
}

function sendMessage() {
    var input = document.getElementById("chatInput");
    var messageText = input.value.trim();

    if (messageText !== "" && stompClient) {
        var chatMessage = {
            sender: "Khách", // Sau này lấy từ session đăng nhập
            content: messageText
        };
        
        // Gửi tin lên Server (Java Controller)
        stompClient.send("/app/sendMessage", {}, JSON.stringify(chatMessage));
        input.value = "";
    }
}

// Hàm hiển thị tin nhắn nhận được từ Server
function showMessage(message) {
    var chatBody = document.getElementById("chatBody");
    var messageElement = document.createElement("div");

    // Phân biệt tin nhắn của mình và người khác
    // Lưu ý: Đây là logic đơn giản, sau này cần so sánh ID người dùng
    if (message.sender === "Khách") { 
         messageElement.className = "message user"; // Bên phải
    } else {
         messageElement.className = "message bot"; // Bên trái (Nhân viên)
    }

    messageElement.innerText = message.content; // + " (" + message.sender + ")";
    chatBody.appendChild(messageElement);
    chatBody.scrollTop = chatBody.scrollHeight;
}