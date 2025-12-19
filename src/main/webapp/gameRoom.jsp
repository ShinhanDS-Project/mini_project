<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // 로그인 유저 정보 (없으면 GUEST)
    String loginId = (String) session.getAttribute("loginId");
    if (loginId == null) {
        loginId = "GUEST";
    }

    String contextPath = request.getContextPath();
%>

<!DOCTYPE html>
<html>
<head>
<title>웹소켓 오목</title>
<style>
	html, body {
	    width: 100%;
	    height: 100%;
	    margin: 0;
	}
	body {
		background-image: url("/omok/img/background.png");
		background-size: cover;
    	background-repeat: no-repeat;
	}
	body::before {
		content : "";
	    position: fixed;
	    inset: 0;
	    background: rgba(255, 255, 255, 0.5); /* 밝은 반투명 */
	    z-index: -1;
	    pointer-events: none;
	}
	
	#game-wrapper {
	    position: 0;
	    width: 100vw;
	    height: 100vh;
	}
    canvas {
        background-color: #DCB35C;
        cursor: pointer;
        border: 1px solid #000;
        z-index: 99999999;
    }
    #status {
        font-size: 20px;
        font-weight: bold;
        color: blue;
        margin-bottom: 10px;
    }
    
    #game-container {
	    position: relative;
	    width: 100%;
	    margin: auto;
	}	

	#board {
	    display: block;
	    margin: auto;
	}
	
	/* 공통 프로필 */
	.profile {
	    position: absolute;
	    width: 250px;
	    background: rgba(255,255,255,0.9);
	    border-radius: 12px;
	    padding: 10px;
	}
	
	/* 좌측 하단 */
	#profile-left {
	    bottom: 20px;
	    left: 20px;
	}
	
	/* 우측 상단 */
	#profile-right {
	    top: 20px;
	    right: 20px;
	}
	
	.avatar {
	    width: 48px;
	    height: 48px;
	    border-radius: 50%;
	}
	
	.info {
	    display: inline-block;
	    margin-left: 8px;
	}
	
	.nickname {
	    font-weight: bold;
	}
	
	.score {
	    font-size: 12px;
	    color: gray;
	}
	
	/* 채팅 */
	.chat-area {
	    margin-top: 8px;
	}
	
	.chat-bubble {
	    background: #eee;
	    border-radius: 12px;
	    padding: 6px 10px;
	    margin: 4px 0;
	    font-size: 13px;
	}
	
	/* 버튼 */
	.chat-buttons {
	    margin-top: 6px;
	}
	
	.chat-buttons button {
	    cursor: pointer;
	}
</style>
<script>
document.addEventListener("DOMContentLoaded", () => {
	//방 번호
    const roomId = prompt("입장할 방 번호를 입력하세요 (예: 100)", "100");
    document.getElementById("roomDisplay").innerText = roomId;

    //로그인 정보
    const loginId = "<%= loginId %>";

    //바둑판
    const canvas = document.getElementById("board");
    const ctx = canvas.getContext("2d");
    canvas.width = canvas.offsetWidth;
    canvas.height = canvas.offsetHeight;

    const BOARD_SIZE = 19;
    const gap = canvas.width / BOARD_SIZE;
    

    function drawBoard() {
        ctx.beginPath();
        for (let i = 0; i < 19; i++) {
            ctx.moveTo(gap/2, gap/2 + gap*i);
            ctx.lineTo(gap/2 + gap*18, gap/2 + gap*i);
            ctx.moveTo(gap/2 + gap*i, gap/2);
            ctx.lineTo(gap/2 + gap*i, gap/2 + gap*18);
        }
        ctx.stroke();
    }
    drawBoard();

    //웹소켓 연결
    const ws = new WebSocket("ws://localhost:8081/omok/play/" + roomId);
    let myColor = "";

    ws.onopen = () => {
        console.log("WebSocket 연결됨");
        //이 세션이 어떤 사용인지
        ws.send("JOIN:" + loginId);
    };

    ws.onmessage = (event) => {
    	const msg = event.data;
        const statusDiv = document.getElementById("status");

        // json형식의 채팅 메시지 처리
        // 일단은 채팅 구현 되는지 봐야 하니까 {로 시작여부 검증하고 나중에 json type 지정해서 그걸로 구분
        if (msg.startsWith("{")) {
            const json = JSON.parse(msg);

            if (json.type === "CHAT") renderChat(json); // 채팅 렌더링 함수
            return; 
        }

        if (msg.startsWith("INFO:")) {
            statusDiv.innerText = msg.substring(5);
        }
        else if (msg.startsWith("START:")) {
            myColor = msg.split(":")[1];
            statusDiv.innerText = "게임 시작! 당신은 " + myColor;
            statusDiv.style.color =
                (myColor === "1") ? "black" : "gray";
        }
        else if (msg.startsWith("PUT:")) {
            const data = msg.split(":")[1].split(",");
            drawStone(
                parseInt(data[0]),
                parseInt(data[1]),
                parseInt(data[2])
            );
        }
        else if (msg.startsWith("WIN:")) {
            alert(msg.split(":")[1] + " 승리!");
            ws.close();
        }
    };

    //돌 놓고 처리하기
    canvas.onclick = (event) => {
        if (myColor === "") return;

        const x = Math.round((event.offsetX - gap/2) / gap);
        const y = Math.round((event.offsetY - gap/2) / gap);

        if (x >= 0 && x < 19 && y >= 0 && y < 19) {
            ws.send(x + "," + y);
        }
    };

    function drawStone(x, y, color) {
        ctx.beginPath();
        ctx.arc(gap/2 + x*gap, gap/2 + y*gap, 13, 0, Math.PI * 2);
        ctx.fillStyle = (color === 1) ? "black" : "white";
        ctx.fill();
        if (color === 2) {
            ctx.strokeStyle = "black";
            ctx.stroke();
        }
    }
    
    //채팅 버튼 열기
    const chatInput = document.getElementById("chat-input");
	document.getElementById("chat-open").onclick = () => {
	    chatInput.style.display = "block";
	    chatInput.focus();
	};
	
	//이모지 버튼 전송
	document.querySelectorAll(".emoji").forEach(btn => {
	    btn.onclick = () => {
	        ws.send(JSON.stringify({
	            type: "CHAT",
	            payload : {
		            kind: "EMOJI",
		            content: btn.dataset.emoji
	            }
	        }));
	    };
	});
	
	//키보드 엔터 입력 시 채팅 전송
	chatInput.addEventListener("keydown", e => {
	    if (e.key === "Enter") {
	        const text = chatInput.value.trim();
	        if (!text) return;
	
	        ws.send(JSON.stringify({
	            type: "CHAT",
	            payload : {
		            kind: "TEXT",
		            content: text
	            }
	        }));
	
	        chatInput.value = "";
	        chatInput.style.display = "none";
	    }
	});
	
	//채팅 렌더링 
	function renderChat(data) {
	    const isMine = data.player.userId === loginId;
	    const area = isMine
	        ? document.getElementById("chat-left")
	        : document.getElementById("chat-right");
	
	    const bubble = document.createElement("div");
	    bubble.className = "chat-bubble";
	    bubble.innerText = data.payload.content;
	
	    area.appendChild(bubble);
	
	    // 3초 후 자동 제거
	    setTimeout(() => bubble.remove(), 3000);
	}
	
});

    
	
</script>
</head>

<body>
<div id = "game-wrapper"> 
	<h2>
	    웹소켓 오목 (방 번호:
	    <span id="roomDisplay"></span>)
	</h2>
	
	<div id="status">연결 중...</div>
	
	<div id="game-container">
	
	    <!-- 오목판 (기존 그대로) -->
	    <canvas id="board" width="700" height="700"></canvas>
	
	    <!-- 내 프로필 (좌측 하단) -->
	    <div id="profile-left" class="profile">
	        <img class="avatar" src="/omok/img/avatar/default.png">
	        <div class="info">
	            <div class="nickname">NICKNAME</div>
	            <div class="score">점수 24</div>
	        </div>
	
	        <!-- 채팅 말풍선 영역 -->
	        <div class="chat-area" id="chat-left"></div>
	
	        <!-- 채팅 버튼 -->
	        <div class="chat-buttons">
	            <button class="emoji" data-emoji="😊">😊</button>
	            <button class="emoji" data-emoji="✌️">✌️</button>
	            <button class="emoji" data-emoji="🙋‍♀️">🙋‍♀️</button>
	            <button id="chat-open">💬</button>
	        </div>
	
	        <!-- 채팅 입력 -->
	        <input type="text" id="chat-input"
	               placeholder="메시지 입력 (Enter)"
	               maxlength="30" style="display:none;">
	    </div>
	
	    <!-- 상대 프로필 (우측 상단) -->
	    <div id="profile-right" class="profile">
	        <img class="avatar" src="/omok/img/avatar/default.png">
	        <div class="info">
	            <div class="nickname">NICKNAME</div>
	            <div class="score">점수 22</div>
	        </div>
	
	        <div class="chat-area" id="chat-right"></div>
	    </div>
	
	</div>
</div>

</body>
</html>