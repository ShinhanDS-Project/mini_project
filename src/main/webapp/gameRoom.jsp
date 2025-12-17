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
    canvas {
        background-color: #DCB35C;
        cursor: pointer;
        border: 1px solid #000;
    }
    #status {
        font-size: 20px;
        font-weight: bold;
        color: blue;
        margin-bottom: 10px;
    }
</style>
</head>
<body>

<h2>
    웹소켓 오목 (방 번호:
    <span id="roomDisplay"></span>)
</h2>

<div id="status">연결 중...</div>
<canvas id="board" width="570" height="570"></canvas>

<script>
    //방 번호
    const roomId = prompt("입장할 방 번호를 입력하세요 (예: 100)", "100");
    document.getElementById("roomDisplay").innerText = roomId;

    //로그인 정보
    const loginId = "<%= loginId %>";

    //바둑판
    const canvas = document.getElementById("board");
    const ctx = canvas.getContext("2d");
    const gap = 30;

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
</script>
</body>
</html>