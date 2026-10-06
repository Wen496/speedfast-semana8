package dao;

import modelo.Repartidor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla repartidores.
 * Los metodos lanzan SQLException para que la vista muestre el mensaje al usuario.
 */
public class RepartidorDAO {

    // Inserta un repartidor nuevo
    public void create(Repartidor repartidor) throws SQLException {
        String sql = "INSERT INTO repartidores (nombre) VALUES (?)";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = ConexionDB.conectar();
            ps = con.prepareStatement(sql);
            ps.setString(1, repartidor.getNombre());
            ps.executeUpdate();
        } finally {
            ConexionDB.cerrar(con, ps, null);
        }
    }

    // Devuelve todos los repartidores ordenados por id
    public List<Repartidor> readAll() throws SQLException {
        String sql = "SELECT id, nombre FROM repartidores ORDER BY id";
        List<Repartidor> repartidores = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = ConexionDB.conectar();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                repartidores.add(new Repartidor(rs.getInt("id"), rs.getString("nombre")));
            }
        } finally {
            ConexionDB.cerrar(con, ps, rs);
        }
        return repartidores;
    }

    // Actualiza el nombre de un repartidor existente.
    // Devuelve false si no existe un repartidor con ese id.
    public boolean update(Repartidor repartidor) throws SQLException {
        String sql = "UPDATE repartidores SET nombre = ? WHERE id = ?";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = ConexionDB.conectar();
            ps = con.prepareStatement(sql);
            ps.setString(1, repartidor.getNombre());
            ps.setInt(2, repartidor.getId());
            return ps.executeUpdate() > 0;
        } finally {
            ConexionDB.cerrar(con, ps, null);
        }
    }

    // Elimina un repartidor por su id.
    // Devuelve false si no existe un repartidor con ese id.
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM repartidores WHERE id = ?";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = ConexionDB.conectar();
            ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } finally {
            ConexionDB.cerrar(con, ps, null);
        }
    }
}
