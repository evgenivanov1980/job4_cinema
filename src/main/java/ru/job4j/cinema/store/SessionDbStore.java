package ru.job4j.cinema.store;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.cinema.model.Session;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SessionDbStore {
    private final String selectAll = "select * from sessions";
    private final String insertSession  = "insert into sessions (name) values (?)";
    private final String findById = "select * from sessions where id = ? ";
    private final String update = "UPDATE sessions set name = ?";
    private final BasicDataSource pool;
    private static final Logger LOG = LoggerFactory.getLogger(Session.class.getName());

    public SessionDbStore(BasicDataSource pool) {
        this.pool = pool;
    }

    public List<Session> findAll() {
        List<Session> sessions = new ArrayList<>();
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement(selectAll)
        ) {
            try (ResultSet it = ps.executeQuery()) {
                while (it.next()) {
                    sessions.add(createSession(it));
                }
            }
        } catch (Exception e) {
            LOG.error("Error", e);
        }
        return sessions;
    }

    public Session add(Session session) {
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement(insertSession,
                     PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, session.getName());

            ps.execute();
            try (ResultSet id = ps.getGeneratedKeys()) {
                if (id.next()) {
                    session.setId(id.getInt(1));
                }
            }
        } catch (Exception e) {
            LOG.error("Error", e);
        }
        return session;
    }

    public Session findById(int id) {
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement(findById)
        ) {
            ps.setInt(1, id);
            try (ResultSet it = ps.executeQuery()) {
                if (it.next()) {
                    return createSession(it);
                }
            }
        } catch (Exception e) {
            LOG.error("Error", e);
        }
        return null;
    }

    public boolean updateSession(Session session) {
        boolean result = false;
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement(update)) {
            ps.setString(1, session.getName());
            result = ps.executeUpdate() > 0;
        } catch (Exception e) {
            LOG.error("Error", e);
        }
        return result;
    }

    private  Session createSession(ResultSet it) throws SQLException {
        return new Session(it.getInt("id"),
                it.getString("name"));
    }
}
