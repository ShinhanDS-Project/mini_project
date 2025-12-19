package webSocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import chat.ChatService;
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
     
        // 1. URL 쿼리스트링에서 시간 제한 파라미터 읽기 (예: ?time=30)
        String queryString = session.getQueryString();
        int timeLimit = 30; // 기본값 30초
        if (queryString != null) {
            try {
                // "time=30" 같은 문자열 파싱
                for (String param : queryString.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2 && pair[0].equals("time")) {
                        timeLimit = Integer.parseInt(pair[1]);
                    }
                }
            } catch (Exception e) {
                // 파싱 에러나면 기본값 유지
            }
        }
        
     // 2. 방 입장 (방이 없으면 만들 때 timeLimit을 사용!)
        // 람다식에서 쓰기 위해 final 변수로 복사
        final int finalTime = timeLimit;
        GameRoom room = rooms.computeIfAbsent(roomId, k -> new GameRoom(finalTime));
        room.enterUser(newPlayer);
        System.out.println("입장: " + tempName + " (방: " + roomId + ", 시간: " + room.getTimeLimit() + "초)");
        
        
       // 세션에 방 ID 저장 (나중에 쓰려고)
        session.getUserProperties().put("roomId", roomId);

    }
    
    //join, put, chat 등 로직 처리 관여 
    @OnMessage
    public void onMessage(String msg, Session session) {
        String roomId = (String) session.getUserProperties().get("roomId");
        GameRoom room = rooms.computeIfAbsent(roomId, k -> new GameRoom());

        //메시지타입이 JOIN일 경우 처리 => 이게 항상 최상단에서 먼저 이루어져야 함
        if (msg.startsWith("JOIN:")) {

            // 이미 입장한 경우 무시
            if (session.getUserProperties().containsKey("player")) return;

            String loginId = msg.substring(5);
            
            Player player = new Player();
            player.setUserSession(session);
            
            HttpSession httpSession = (HttpSession) session.getUserProperties().get("httpSession");

            UserVO userVO = null;
            if (httpSession != null) {
//                userVO.setUserId(httpSession.getAttribute("loginId"));
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
        
        Player player = (Player) session.getUserProperties().get("player");
        //오목 게임 메시지 처리(JOIN 이후만 가능)
        if (player == null) return;
        
        if (msg.startsWith("{")) {
        	ChatService chatService = new ChatService();
            chatService.handle(msg, player, room);
            return;
        }
        
        room.processMove(msg, player);
    }

	@OnClose
    public void onClose(Session session) {
        // 퇴장 처리는 간단하게 생략 (실제론 방에서 제거해줘야 함)
        System.out.println("연결 종료: " + session.getId());
    }
}
