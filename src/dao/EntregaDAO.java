package dao;

import modelo.Entrega;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla entregas.
 * Cada entrega asocia un pedido con un repartidor en una fecha y hora.
 * Los metodos lanzan SQLException para que la vista muestre el mensaje al usuario.
 */
public class EntregaDAO {

    // Inserta una entrega nueva
    public void create(Entrega entrega) throws SQLException {
        String sql = "INSERT INTO entregas (id_pedido, id_repartidor, fecha, hora) VALUES (?, ?, ?, ?)";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = ConexionDB.conectar();
            ps = con.prepareStatement(sql);
            ps.setInt(1, entrega.getIdPedido());
            ps.setInt(2, entrega.getIdRepartidor());
            ps.setDate(3, Date.valueOf(entrega.getFecha()));
            ps.setTime(4, Time.valueOf(entrega.getHora()));
            ps.executeUpdate();
        } finally {
            ConexionDB.cerrar(con, ps, null);
        }
    }

    // Devuelve todas las entregas ordenadas por id
    public List<Entrega> readAll() throws SQLException {
        String sql = "SELECT id, id_pedido, id_repartidor, fecha, hora FROM entregas ORDER BY id";
        List<Entrega> entregas = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = ConexionDB.conectar();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                // fecha y hora admiten NULL en la tabla, por eso se revisan antes de convertir
                Date fecha = rs.getDate("fecha");
                Time hora = rs.getTime("hora");
                entregas.add(new Entrega(
                        rs.getInt("id"),
                        rs.getInt("id_pedido"),
                        rs.getInt("id_repartidor"),
                        fecha != null ? fecha.toLocalDate() : null,
                        hora != null ? hora.toLocalTime() : null
                ));
            }
        } finally {
            ConexionDB.cerrar(con, ps, rs);
        }
        return entregas;
    }

    // Actualiza pedido, repartidor, fecha y hora de una entrega existente.
    // Devuelve false si no existe una entrega con ese id.
    public boolean update(Entrega entrega) throws SQLException {
        String sql = "UPDATE entregas SET id_pedido = ?, id_repartidor = ?, fecha = ?, hora = ? WHERE id = ?";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = ConexionDB.conectar();
            ps = con.prepareStatement(sql);
            ps.setInt(1, entrega.getIdPedido());
            ps.setInt(2, entrega.getIdRepartidor());
            ps.setDate(3, Date.valueOf(entrega.getFecha()));
            ps.setTime(4, Time.valueOf(entrega.getHora()));
            ps.setInt(5, entrega.getId());
            return ps.executeUpdate() > 0;
        } finally {
            ConexionDB.cerrar(con, ps, null);
        }
    }

    // Elimina una entrega por su id.
    // Devuelve false si no existe una entrega con ese id.
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM entregas WHERE id = ?";
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
