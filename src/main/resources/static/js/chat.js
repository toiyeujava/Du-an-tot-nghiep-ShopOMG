

// 1. Lấy user từ biến toàn cục bên home.html
var username = window.chatUser;
var stompClient = null;

function connect() {
	// 2. Nếu chưa login (username là null) -> Dừng, không kết nối
	if (!username) {
		console.log("Chưa đăng nhập -> Không kết nối Chat.");
		return;
	}

	var socket = new SockJS('/ws-chat');
	stompClient = Stomp.over(socket);

	stompClient.connect({}, function(frame) {
		console.log('Đã kết nối: ' + username);

		stompClient.subscribe('/user/queue/messages', function(message) {
			var msgBody = JSON.parse(message.body);
			if (msgBody.sender === "Admin") {
				showMessage(msgBody, "bot");

				// Hiện chat box nếu đang ẩn
				var chatBox = document.getElementById("chatBox");
				if (chatBox && chatBox.style.display === "none") {
					chatBox.style.display = "flex";
				}
			}
		});
	});
}

function sendMessage() {
	var input = document.getElementById("chatInput");
	if (!input) return;

	var messageText = input.value.trim();

	if (messageText !== "" && stompClient && username) {
		var chatMessage = {
			sender: username,
			recipient: "Admin",
			content: messageText
		};

		stompClient.send("/app/chat.sendToAdmin", {}, JSON.stringify(chatMessage));
		showMessage({ content: messageText }, "user");
		input.value = "";
	} else {
		alert("Lỗi: Bạn chưa kết nối hoặc chưa đăng nhập.");
	}
}

function showMessage(message, type) {
	var chatBody = document.getElementById("chatBody");
	if (!chatBody) return;

	var messageElement = document.createElement("div");
	messageElement.className = (type === "user") ? "message user" : "message bot";
	messageElement.innerText = message.content;

	chatBody.appendChild(messageElement);
	chatBody.scrollTop = chatBody.scrollHeight;
}

// Hàm này BẮT BUỘC phải có để nút bấm hoạt động
function toggleChatBox() {
	var chatBox = document.getElementById("chatBox");
	if (chatBox.style.display === "none" || chatBox.style.display === "") {
		chatBox.style.display = "flex";
		var input = document.getElementById("chatInput");
		if (input) setTimeout(() => input.focus(), 100);
	} else {
		chatBox.style.display = "none";
	}
}

function handleEnter(e) {
	if (e.key === 'Enter') sendMessage();
}

// Chạy kết nối
connect();