package domain;

import javax.websocket.Session;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    private String userId;
    private Session userSession;
    private String nickname;
    private String avatar;
    private int score;
    private int stone;   // BLACK:1 / WHITE:2
    private int stoneStyle; // 1,2,3,...-> 해당 코드에 맞는 이미지 파일 찾아와야함.
}
