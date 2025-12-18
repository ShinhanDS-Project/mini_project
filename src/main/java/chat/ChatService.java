package chat;

import org.json.JSONObject;

import domain.GameRoom;
import domain.Player;

public class ChatService {
	 public void handle(ChatRequest req, Player player, GameRoom room) {

	        if (!req.isChat()) return;

	        // 2. 유효성 검사
	        if (!req.isValid()) return;

	        // 3. 서버 시간
	        long now = System.currentTimeMillis();

	        // 4. ⭐ 응답 객체 생성 (핵심)
	        ChatResponse res = new ChatResponse(
	            "CHAT",
	            req.getKind(),
	            req.getContent(),
	            now,
	            player
	        );

//	        // 5. JSON 직렬화
//	        String json = new JSONObject();
//
//	        // 6. 방에 브로드캐스트
//	        room.broadcast(json);
	    }
}
