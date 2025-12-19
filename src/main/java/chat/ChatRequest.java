package chat;

import java.io.IOException;

import domain.Player;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//클라 -> 서버 
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRequest {
    private String type;        // "CHAT"으로 join과 omok과 분리
    private String kind;     	// "EMOJI"랑 "TEXT"로 이모지랑 텍스트 구분
    private String content;
    private long time;
    private Player player; 
    
    
    public ChatRequest(String type, String kind, String content, Player player) {
    	this.type = type;
    	this.kind = kind;
    	this.content = content;
    	this.player = player;
    }
    
    public ChatRequest(String type, String kind, String content, long time) {
    	this.type = type;
    	this.kind = kind;
    	this.content = content;
    	this.time = time;
    }

    //메시지가 CHAT인지 -> 추후 OMOK, JOIN도 json 요청 응답으로 변경하면 해당 메서드 추가 
    public boolean isChat(String type) {
        return "CHAT".equals(type);
    }
    
    //전송된 메시지 종류에 따른 유효성 검사 (텍스트면 길이 제한)
    public boolean isValid(String kind) {
        if ("TEXT".equals(kind)) {
            return content != null && content.length() <= 30;
        }
        if ("EMOJI".equals(kind)) {
            return content != null;
        }
        return false;
    }
    
    private void sendMessage(Player player, String msg) {
        try {
            if (player.getUserSession() != null && player.getUserSession().isOpen()) {
            	player.getUserSession().getBasicRemote().sendText(msg);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

}