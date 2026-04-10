package tn.dhc.services;

import tn.dhc.entities.User;
import tn.dhc.utils.MyConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

public class UserService implements IService<User> {

    private Connection cnx;
    private static User currentUser;

    public UserService() {
        cnx = MyConnection.getInstance().getConnection();
    }

    // ✅ CREATE
    @Override
    public void ajouter(User u) {
        try {
            String sql = "INSERT INTO user (nom, prenom, mail, tel, mdp, role) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = cnx.prepareStatement(sql);

            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getMail());
            ps.setInt(4, u.getTel());
            ps.setString(5, u.getMdp());
            ps.setString(6, u.getRole());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ✅ DELETE
    @Override
    public void supprimer(User u) {
        try {
            String sql = "DELETE FROM user WHERE id=?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, u.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ✅ UPDATE
    @Override
    public void modifier(User u) {
        try {
            String sql = "UPDATE user SET nom=?, prenom=?, mail=?, tel=?, role=? WHERE id=?";
            PreparedStatement ps = cnx.prepareStatement(sql);

            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getMail());
            ps.setInt(4, u.getTel());
            ps.setString(5, u.getRole());
            ps.setInt(6, u.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ✅ GET ALL
    @Override
    public List<User> getAll() {
        List<User> users = new ArrayList<>();

        try {
            String sql = "SELECT * FROM user";
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return users;
    }

    // ✅ GET ONE
    @Override
    public User getOneById(int id) {
        try {
            String sql = "SELECT * FROM user WHERE id=?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    // 🔐 LOGIN (hors interface)
    public boolean login(String email, String mdp) {
        try {
            String sql = "SELECT * FROM user WHERE mail=?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User u = mapResultSetToUser(rs);


                String hashed = BCrypt.hashpw(u.getMdp(), BCrypt.gensalt());
                if (BCrypt.checkpw(mdp, u.getMdp())) {
                    currentUser = u;

                    String update = "UPDATE user SET last_login_at=?, login_count = login_count + 1 WHERE id=?";
                    PreparedStatement ps2 = cnx.prepareStatement(update);
                    ps2.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                    ps2.setInt(2, u.getId());
                    ps2.executeUpdate();

                    return true;
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        currentUser = null;
    }

    // 🔁 mapping
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("mail"),
                rs.getInt("tel"),
                rs.getString("mdp"),
                rs.getString("role"),
                rs.getString("specialite"),
                rs.getTimestamp("last_login_at") != null
                        ? rs.getTimestamp("last_login_at").toLocalDateTime()
                        : null,
                rs.getInt("login_count")
        );
    }
}