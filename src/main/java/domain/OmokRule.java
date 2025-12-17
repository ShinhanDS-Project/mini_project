package domain;

public class OmokRule {
    private int[][] board = new int[19][19]; // 0:빈곳, 1:흑, 2:백
    private int turn = 1; // 1: 흑 차례(선공), 2: 백 차례
    
    public int getTurn() { return turn; }
    
    // 착수 로직
    public int putStone(int x, int y, int color) {
        if (x < 0 || x >= 19 || y < 0 || y >= 19) return -1; // 범위 초과
        if (board[y][x] != 0) return -1; // 이미 돌 있음
        if (color != turn) return -2; // 니 차례 아님
        board[y][x] = color; // 돌 두기
        // 승리 체크
        if (checkWin(x, y, color)) {
            return 100; // 승리
        }
        // 턴 넘기기
        turn = (turn == 1) ? 2 : 1;
        return 0; // 정상 진행
    }
    // 승리 판별 (가로, 세로, 대각선 5목 체크)
    private boolean checkWin(int x, int y, int color) {
        int[][] directions = {{1,0}, {0,1}, {1,1}, {1,-1}}; // 가로, 세로, 대각선, 역대각선
        for (int[] d : directions) {
            int count = 1;
            // 한쪽 방향 탐색
            for (int i = 1; i < 5; i++) {
                int nx = x + d[0] * i;
                int ny = y + d[1] * i;
                if (nx < 0 || nx >= 19 || ny < 0 || ny >= 19 || board[ny][nx] != color) break;
                count++;
            }
            // 반대 방향 탐색
            for (int i = 1; i < 5; i++) {
                int nx = x - d[0] * i;
                int ny = y - d[1] * i;
                if (nx < 0 || nx >= 19 || ny < 0 || ny >= 19 || board[ny][nx] != color) break;
                count++;
            }
            if (count >= 5) return true; // 5개 이상이면 승리
        }
        return false;
    }
}