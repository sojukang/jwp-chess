package chess;

import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import chess.dao.JdbcFixture;
import chess.dto.MoveDto;
import io.restassured.RestAssured;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ChessControllerTest {

    private static final long id = 1L;
    private static final String API_URL_PREFIX = "/api";

    @LocalServerPort
    int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        JdbcFixture jdbcFixture = new JdbcFixture(jdbcTemplate);
        RestAssured.port = port;
        jdbcFixture.dropTable("square");
        jdbcFixture.dropTable("room");
        jdbcFixture.createRoomTable();
        jdbcFixture.createSquareTable();
        jdbcFixture.insertRoom("roma", "white", "pw12345678");
    }

    @Test
    @DisplayName("새로운 방 생성 여부를 검증한다.")
    void create() {
        RestAssured.given().log().all()
            .body("name=sojukang&password=1234567890")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .when().post(API_URL_PREFIX + "/rooms")
            .then().log().all()
            .statusCode(HttpStatus.CREATED.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    @DisplayName("이미 존재하는 이름일 경우 400 응답을 던진다.")
    void createExceptionAlreadyExists() {
        RestAssured.given().log().all()
            .body("name=roma&password=pw12345678")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .when().post(API_URL_PREFIX + "/rooms")
            .then().log().all()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    @DisplayName("게임 시작시 턴과 보드의 반환값을 검증한다.")
    void start() {
        RestAssured.given().log().all()
            .when().post(API_URL_PREFIX + "/rooms/" + id)
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("turn", is("white"))
            .body("board.size()", is(64));
    }

    @Test
    @DisplayName("저장된 게임 정보 불러오기를 검증한다.")
    void findRoom() {
        RestAssured.post(API_URL_PREFIX + "/rooms/" + id);

        RestAssured.given().log().all()
            .when().get(API_URL_PREFIX + "/rooms/" + id)
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("turn", is("white"))
            .body("board.size()", is(64));
    }

    @Test
    @DisplayName("전체 방 조회 기능을 검증한다.")
    void findAllRooms() {
        RestAssured.given().log().all()
            .body("name=sojukang&password=1234567890")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .when().post(API_URL_PREFIX + "/rooms");

        RestAssured.given().log().all()
            .when().get(API_URL_PREFIX + "/rooms")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("rooms.size()", is(2));
    }

    @Test
    @DisplayName("체스 말 이동 기능을 검증한다.")
    void move() {
        RestAssured.post(API_URL_PREFIX + "/rooms/" + id);
        MoveDto moveDto = new MoveDto("a2", "a4");

        RestAssured.given().log().all()
            .body(moveDto)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when().patch(API_URL_PREFIX + "/rooms/" + id + "/move")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("turn", is("black"))
            .body("board.a2", is("empty"))
            .body("board.a4", is("white_pawn"));
    }

    @Test
    @DisplayName("점수 출력 기능을 검증한다.")
    void status() {
        RestAssured.post(API_URL_PREFIX + "/rooms/" + id);

        RestAssured.given().log().all()
            .when().get(API_URL_PREFIX + "/rooms/" + id + "/status")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("whiteScore", is(38.0F))
            .body("blackScore", is(38.0F));
    }

    @Test
    @DisplayName("이동할 수 없는 위치인 경우 400 응답을 던진다.")
    void moveExceptionWrongPosition() {
        RestAssured.post(API_URL_PREFIX + "/rooms/" + id);
        MoveDto moveDto = new MoveDto("a2", "a5");

        RestAssured.given().log().all()
            .body(moveDto)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when().patch(API_URL_PREFIX + "/rooms/" + id + "/move")
            .then().log().all()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("message", is("해당 Position으로 이동할 수 없습니다."));
    }

    @Test
    @DisplayName("해당 색의 차례가 아닐 경우 400 응답을 던진다.")
    void moveExceptionWrongTurn() {
        RestAssured.post(API_URL_PREFIX + "/rooms/" + id);
        MoveDto moveDto = new MoveDto("a7", "a6");

        RestAssured.given().log().all()
            .body(moveDto)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when().patch(API_URL_PREFIX + "/rooms/" + id + "/move")
            .then().log().all()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("message", is("당신의 차례가 아닙니다."));
    }

    @Test
    @DisplayName("아직 게임을 시작하지 않은 방에서 불러오기를 할 경우 400 응답을 던진다.")
    void findExceptionBeforeInit() {
        RestAssured.given().log().all()
            .when().get(API_URL_PREFIX + "/rooms/" + id)
            .then().log().all()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("message", is("해당 ID에 체스게임이 초기화되지 않았습니다."));
    }

    @Test
    @DisplayName("방 삭제 성공시 204 상태를 응답한다.")
    void delete() {
        RestAssured.given().log().all()
            .body("name=sojukang&password=1234567890")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .when().post(API_URL_PREFIX + "/rooms");

        RestAssured.given().log().all()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body("password=1234567890")
            .when().delete(API_URL_PREFIX + "/rooms/" + (id + 1))
            .then().log().all()
            .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @DisplayName("turn이 empty가 아닐 경우 삭제 시도하면 400 응답을 던진다.")
    void deleteNotAllowedException() {
        RestAssured.given().log().all()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body("password=pw12345678")
            .when().delete(API_URL_PREFIX + "/rooms/" + id)
            .then().log().all()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("message", is("진행중인 방은 삭제할 수 없습니다."));
    }

    @Test
    @DisplayName("비밀번호가 틀릴 경우 401 응답을 던진다.")
    void deleteInvalidPassword() {
        RestAssured.given().log().all()
            .body("name=sojukang&password=1234567890")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .when().post(API_URL_PREFIX + "/rooms");

        RestAssured.given().log().all()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body("password=12345678901")
            .when().delete(API_URL_PREFIX + "/rooms/" + (id + 1))
            .then().log().all()
            .statusCode(HttpStatus.UNAUTHORIZED.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("message", is("Password가 일치하지 않습니다."));
    }
}
