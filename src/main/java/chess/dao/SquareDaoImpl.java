package chess.dao;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import chess.entity.Square;

@Repository
public class SquareDaoImpl implements SquareDao {

    private final JdbcTemplate jdbcTemplate;

    public SquareDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveAll(List<Square> squares, long roomId) {
        String sql = "insert into square (position, piece, room_id) values (?, ?, ?)";

        List<Object[]> result = squares.stream()
            .map(s -> new Object[] {s.getPosition(), s.getPiece(), roomId})
            .collect(Collectors.toList());

        jdbcTemplate.batchUpdate(sql, result);
    }

    @Override
    public List<Square> findByRoomId(long roomId) {
        String sql = "select id, position, piece from square where room_id = ?";
        return jdbcTemplate.query(sql,
            (rs, rowNum) ->
                new Square(
                    rs.getLong("id"),
                    roomId,
                    rs.getString("position"),
                    rs.getString("piece")),
            roomId
        );
    }

    @Override
    public Optional<Square> findByRoomIdAndPosition(long roomId, String position) {
        String sql = "select id, piece from square where room_id = ? and position = ?";
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql,
            (rs, rowNum) -> new Square(rs.getLong("id"),
                roomId,
                position,
                rs.getString("piece")),
            roomId, position
        ));
    }

    @Override
    public void update(long roomId, String position, String piece) {
        String sql = "update square set piece = ? where room_id = ? and position = ?";
        jdbcTemplate.update(sql, piece, roomId, position);
    }

    @Override
    public void removeAll(long roomId) {
        String sql = "delete from square where room_id = ?";
        jdbcTemplate.update(sql, roomId);
    }
}
