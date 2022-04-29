package chess;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import chess.dao.RoomDao;
import chess.dao.SquareDao;
import chess.domain.Status;
import chess.dto.BoardDto;
import chess.dto.MoveDto;
import chess.entity.Room;

public class ChessServiceTest {

    private ChessService chessService;
    private final RoomDao roomDao = new FakeRoomDao();
    private final SquareDao squareDao = new FakeSquareDao();
    private Long id;

    @BeforeEach
    void setUp() {
        chessService = new ChessService(roomDao, squareDao);
        Room room = chessService.createRoom("roma", "pw");
        id = room.getId();
    }

    @Test
    @DisplayName("방 이름과 비밀번호를 받아 방을 생성한다.")
    void createRoom() {
        Room actual = chessService.createRoom("sojukang", "pw");
        assertAll(
            () -> assertThat(actual.getName()).isEqualTo("sojukang"),
            () -> assertThat(actual.getPassword()).isEqualTo("pw")
        );
    }

    @Test
    @DisplayName("id를 입력받아 게임을 시작시키고 초기화된 BoardDto를 반환한다.")
    void startNewGame() {
        BoardDto boardDto = chessService.startNewGame(id);

        assertThat(boardDto.getTurn()).isEqualTo("white");
    }

    @Test
    @DisplayName("id로 Board의 정보를 얻어 BoardDto로 반환한다.")
    void find() {
        chessService.startNewGame(id);

        BoardDto boardDto = chessService.findRoom(id);

        assertAll(
            () -> assertThat(boardDto.getTurn()).isEqualTo("white"),
            () -> assertThat(boardDto.getBoard()).hasSize(64)
        );
    }

    @Test
    @DisplayName("체스 말 이동 기능을 검증한다.")
    void move() {
        chessService.startNewGame(id);
        BoardDto actual = chessService.move(id, new MoveDto("a2", "a4"));

        assertAll(
            () -> assertThat(actual.getTurn()).isEqualTo("black"),
            () -> assertThat(actual.getBoard()).containsEntry("a2", "empty"),
            () -> assertThat(actual.getBoard()).containsEntry("a4", "white_pawn")
        );
    }

    @Test
    @DisplayName("점수 반환 기능을 검증한다.")
    void status() {
        chessService.startNewGame(id);
        Status actual = chessService.status(id);

        assertAll(
            () -> assertThat(actual.getWhiteScore()).isEqualTo(38.0),
            () -> assertThat(actual.getBlackScore()).isEqualTo(38.0)
        );
    }

    @Test
    @DisplayName("delete로 id, password를 받아 Room, square 정보를 지운다.")
    void delete() {
        assertThat(chessService.delete(id, "pw")).isTrue();
    }

    @Test
    @DisplayName("delete로 password가 옳지 않은 경우 Exception을 던진다.")
    void deleteInvalidPasswordException() {
        assertThatThrownBy(() -> chessService.delete(id, "pww"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Password");
    }

    @Test
    @DisplayName("delete로 현재 상태가 empty가 아닐 때 삭제 시도할 경우 Exception을 던진다.")
    void deleteNotAllowedException() {
        chessService.startNewGame(id);
        assertThatThrownBy(() -> chessService.delete(id, "pw"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("진행중");
    }
}
