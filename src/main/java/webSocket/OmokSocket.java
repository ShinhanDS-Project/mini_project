package webSocket;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import domain.GameRoom;
import domain.Player;
import vo.UserVO;


@ServerEndpoint("/play/{roomId}")
public class OmokSocket {
    // 전체 방 목록
    private static Map<String, GameRoom> rooms = new ConcurrentHashMap<String, GameRoom>();
    
    //소켓 연결만 관여하기 
    @OnOpen
    public void onOpen(Session session, @PathParam("roomId") String roomId) {
        System.out.println("연결됨: " + roomId);
//        // 방이 없으면 생성, 있으면 가져오기
//        GameRoom room = rooms.computeIfAbsent(roomId, k -> new GameRoom());
//        
//        //player 생성
//        Player player = new Player();
//        player.setUserSession(session);
//        
//        // 세션에 방 ID 저장 (나중에 쓰려고)
        session.getUserProperties().put("roomId", roomId);
//        
//        // 방 입장
//        room.enterUser(player);
    }
    
    //join, put, chat 등 로직 처리 관여 
    @OnMessage
    public void onMessage(String msg, Session session) {
        String roomId = (String) session.getUserProperties().get("roomId");
//        if (roomId == null) return;
        
        GameRoom room = rooms.computeIfAbsent(roomId, k -> new GameRoom());
        
        //메시지타입이 JOIN일 경우 처리
        if (msg.startsWith("JOIN:")) {

            // 이미 입장한 경우 무시
            if (session.getUserProperties().containsKey("player")) return;

            String loginId = msg.substring(5);
            
            Player player = new Player();
            player.setUserSession(session);
            
            HttpSession httpSession = (HttpSession) session.getUserProperties().get("httpSession");

            UserVO userVO = null;
            if (httpSession != null) {
                userVO = (UserVO) httpSession.getAttribute("loginId");
            }
            
            if (userVO != null) {
//	            player.setUserId(userVO.getUserId());
//	            player.setNickname(userVO.getNickname());
//	            player.setAvatar(userVO.getAvatar());
//	            player.setStoneStyle(userVO.getStoneStyle());
            	
            	player.setUserId("kim1234");
	            player.setNickname("gabeeni");
	            player.setAvatar("hihi.jsp");
	            player.setStoneStyle(0);
                
            } else {
                player.setUserId("GUEST");
                player.setNickname("게스트");
                player.setAvatar("default.png");
                player.setStoneStyle(0);
            }

            // session에 플레이어 저장
            session.getUserProperties().put("player", player);
            
            // 방 입장
            room.enterUser(player);
            return;
        }
        
        //오목 게임 메시지 처리(JOIN 이후만 가능)
        Player player = (Player) session.getUserProperties().get("player");
        if (player == null) return;
        room.processMove(msg, player);
        
        
    }
    
    @OnClose
    public void onClose(Session session) {
        // 퇴장 처리는 간단하게 생략 (실제론 방에서 제거해줘야 함)
        System.out.println("연결 종료: " + session.getId());
    }
}
