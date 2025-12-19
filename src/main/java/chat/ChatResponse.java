package chat;

import domain.Player;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//클라 -> 서버 
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse {
    private String type = "CHAT";
    private String kind;
    private String content;
    private long time;
    private Player player;   // 보내는 사람 정보
    
    
    
}