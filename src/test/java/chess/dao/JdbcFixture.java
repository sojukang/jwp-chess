package chess.dao;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcFixture {

    private final JdbcTemplate jdbcTemplate;

    public JdbcFixture(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void dropTable(String tableName) {
        jdbcTemplate.execute("DROP TABLE " + tableName + " IF EXISTS");
    }

    public void createRoomTable() {
        jdbcTemplate.execute("create table room ("
            + " id bigint not null auto_increment,"
            + " name VARCHAR(255) not null,"
            + " password varchar(255) not null,"
            + " turn varchar(10) not null,"
            + " primary key (id))");
    }

    public void createSquareTable() {
        jdbcTemplate.execute(
            "create table square ("
                + " id bigint not null auto_increment,"
                + " position varchar(5) not null,"
                + " piece varchar(20) not null,"
                + " room_id bigint not null,"
                + " primary key (id),"
                + " foreign key (room_id) references room (id))");
    }

    public void insertRoom(String name, String turn, String password) {
        jdbcTemplate.update("INSERT INTO room(name, password, turn) VALUES (?, ?, ?)", name, password, turn);
    }

    public void insertSquares(List<String> squares) {
        List<Object[]> pieces = squares
            .stream()
            .map(piece -> piece.split(","))
            .collect(Collectors.toList());

        jdbcTemplate.batchUpdate("insert into square (position, piece, room_id) values (?, ?, ?)", pieces);
    }
}
