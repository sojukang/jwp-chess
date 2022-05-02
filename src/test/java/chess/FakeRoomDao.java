package chess;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import chess.dao.RoomDao;
import chess.entity.Room;

public class FakeRoomDao implements RoomDao {

    private final List<Room> rooms = new ArrayList<>();
    private long id;

    public FakeRoomDao() {
        this.id = 1L;
    }

    @Override
    public long save(Room room) {
        rooms.add(new Room(id++, room.getName(), room.getPassword(), room.getTurn()));
        return id - 1;
    }

    @Override
    public Optional<Room> findByName(String name) {
        return rooms.stream()
            .filter(room -> room.getName().equals(name))
            .findAny();
    }

    @Override
    public Optional<Room> findByNameAndPassword(String name, String password) {
        Optional<Room> room = findByName(name);
        if (room.isPresent() && room.get().getPassword().equals(password)) {
            return room;
        }
        return Optional.empty();
    }

    @Override
    public Optional<Room> findById(long roomId) {
        try {
            Room room = rooms.get((int)(roomId - 1));
            return Optional.of(room);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void update(long id, String turn) {
        Room room = rooms.get((int)(id - 1));
        rooms.set((int)(id - 1), new Room(id, room.getName(), room.getPassword(), turn));
    }

    @Override
    public List<Room> findAll() {
        return List.copyOf(rooms);
    }

    @Override
    public Optional<Room> findByIdAndPassword(long id, String password) {
        Room room = rooms.get((int)(id - 1));
        if (room.getPassword().equals(password)) {
            return Optional.of(room);
        }
        return Optional.empty();
    }

    @Override
    public void delete(long roomId) {
        rooms.remove((int)(roomId - 1));
    }
}
