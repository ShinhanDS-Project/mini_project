package chat;

import javax.servlet.http.HttpSession;

import org.json.JSONObject;

import domain.GameRoom;
import domain.Player;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ChatService { 
	ChatRequest chatRequest;
	
	public void handle(String msg, Player player, GameRoom room) {
		chatRequest = parseChat(msg, player);

		//메시지 유효성 검사
		if (!chatRequest.isChat(chatRequest.getType())) return;
		if (!chatRequest.isValid(chatRequest.getKind())) return;

		String jsonString = buildChatJsonString(chatRequest);
		room.broadcast(jsonString);
	}
	 
	 //메시지 파싱 메서드
	 public ChatRequest parseChat(String msg, Player player) {
		 //json 파싱 
		 JSONObject json = new JSONObject(msg);
		 System.out.println(json);
		 JSONObject jsonPayload =json.getJSONObject("payload");
		 
		 String type = json.getString("type");
		 String kind = jsonPayload.getString("kind");
		 String content = jsonPayload.getString("content");
		 
		 return new ChatRequest(type, kind, content,player);
	 }
	 
	 public static String buildChatJsonString(ChatRequest chatRequest) {
	
	    StringBuilder sb = new StringBuilder();

	    sb.append("{")
	      .append("\"type\":\"").append(chatRequest.getType()).append("\",")

	      .append("\"payload\":{")
	        .append("\"kind\":\"").append(chatRequest.getKind()).append("\",")
	        .append("\"content\":\"").append(escape(chatRequest.getContent())).append("\"")
	      .append("},")

	      .append("\"time\":").append(System.currentTimeMillis()).append(",")

	      .append("\"player\":{")
	        .append("\"userId\":\"")
	        .append(chatRequest.getPlayer().getUserId())
	        .append("\"")
	      .append("}")

	    .append("}");

	    return sb.toString();
	}
	 
	 private static String escape(String text) {
	    if (text == null) return "";
	    return text
	            .replace("\\", "\\\\")
	            .replace("\"", "\\\"")
	            .replace("\n", "\\n")
	            .replace("\r", "\\r");
	}
}
