package chess.domain.piece;

import java.util.List;

import chess.domain.position.Direction;
import chess.domain.position.Position;

public class Queen extends StraightMovablePiece {

    public static final Position BLACK_INIT_LOCATION = Position.of("d8");
    public static final Position WHITE_INIT_LOCATION = Position.of("d1");

    public Queen(Color color) {
        super(color, PieceType.QUEEN);
    }

    @Override
    protected List<Direction> getMovableDirections() {
        return Direction.kingAndQueenDirections();
    }
}
