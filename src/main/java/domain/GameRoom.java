package domain;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Random;
public class GameRoom {
	private String roomId;
    private String title;
    private Player player1;
    private Player player2;
    private int limitTime;
    private Boolean gameStatus; //flase: 대기중/ true: 게임중
    private OmokRule rule;
    
    private static final int BLACK = 1;
    private static final int WHITE = 2;
    
    //방 생성될 때 오목 규칙 객체 생성
    public GameRoom() {
        this.rule = new OmokRule();
    }
    
    // 유저 입장 (동기화 필수)
    public synchronized void enterUser(Player player) {
        if (player1 == null) {
        	player1 = player;
            sendMessage(player1, "INFO:상대방을 기다리는 중...");
            
        } else if (player2 == null) {
        	player2 = player;
            startGame(); // 2명 찼으니 시작
          
        } else {	//3명인 경우
            sendMessage(player, "ERROR:방이 꽉 찼습니다.");
            try { player.getUserSession().close(); } catch(Exception e){}
        }
    }
    // 게임 시작 (랜덤 역할 분배)
    private void startGame() {
    	System.out.println("p1: "+player1);
    	System.out.println("p2: "+player2);
    	
        Random random = new Random();
        
        if (random.nextBoolean()) {
            player1.setStone(BLACK); 
            player2.setStone(WHITE);
        } else {
        	player1.setStone(WHITE); 
            player2.setStone(BLACK);
        }
        System.out.println(player1);
        System.out.println(player2);
        //player1.setStoneStyle(BLACK); -> 유저 정보에서 어떤 돌 선택했는지 가져와서 setStone 값 확인 후 흑/백 이미지 중 택 1  
        
        //시작문구 안내
        sendMessage(player1, "START: "+ player1.getStone());
        sendMessage(player2, "START: "+ player2.getStone()); 
    }
    
    
    // 돌 두기 요청 처리
    public void processMove(String msg, Player player) {
        if (player1 == null || player2 == null) return; // 게임 시작 전
        
        try {
            String[] parts = msg.split(","); //좌표값
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int myColor = player.getStone();
            
            // 규칙 판별
            int result = rule.putStone(x, y, myColor);
            if (result == 0) {
                // 정상 착수 -> 양쪽에 돌 그리기 신호 전송
                broadcast("PUT:" + x + "," + y + "," + myColor);
                
            } else if (result == 100) {
                // 승리 -> 마지막 돌 그리고 게임 종료 신호
                broadcast("PUT:" + x + "," + y + "," + myColor);
                broadcast("WIN:" + (myColor == 1 ? "BLACK" : "WHITE"));
                
            } else if (result == -2) {
                sendMessage(player, "INFO:당신 차례가 아닙니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // 메시지 뿌리기
    private void broadcast(String msg) {
    	if (player1 != null) sendMessage(player1, msg);
        if (player2 != null) sendMessage(player2, msg);
    }
    
    private void sendMessage(Player player, String msg) {
        try {
            if (player.getUserSession() != null && player.getUserSession().isOpen()) {
            	player.getUserSession().getBasicRemote().sendText(msg);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}