package dao;

import modelo.Pedido;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla pedidos.
 * Los metodos lanzan SQLException para que la vista muestre el mensaje al usuario.
 */
public class PedidoDAO {

    // Inserta un pedido nuevo
    public void create(Pedido pedido) throws SQLException {
        String sql = "INSERT INTO pedidos (direccion, tipo, estado) VALUES (?, ?, ?)";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = ConexionDB.conectar();
            ps = con.prepareStatement(sql);
            ps.setString(1, pedido.getDireccion());
            ps.setString(2, pedido.getTipo());
            ps.setString(3, pedido.getEstado());
            ps.executeUpdate();
        } finally {
            ConexionDB.cerrar(con, ps, null);
        }
    }

    // Devuelve todos los pedidos ordenados por id
    public List<Pedido> readAll() throws SQLException {
        String sql = "SELECT id, direccion, tipo, estado FROM pedidos ORDER BY id";
        List<Pedido> pedidos = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = ConexionDB.conectar();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                pedidos.add(new Pedido(
                        rs.getInt("id"),
                        rs.getString("direccion"),
                        rs.getString("tipo"),
                        rs.getString("estado")
                ));
            }
        } finally {
            ConexionDB.cerrar(con, ps, rs);
        }
        return pedidos;
    }

    // Actualiza direccion, tipo y estado de un pedido existente.
    // Devuelve false si no existe un pedido con ese id.
    public boolean update(Pedido pedido) throws SQLException {
        String sql = "UPDATE pedidos SET direccion = ?, tipo = ?, estado = ? WHERE id = ?";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = ConexionDB.conectar();
            ps = con.prepareStatement(sql);
            ps.setString(1, pedido.getDireccion());
            ps.setString(2, pedido.getTipo());
            ps.setString(3, pedido.getEstado());
            ps.setInt(4, pedido.getId());
            return ps.executeUpdate() > 0;
        } finally {
            ConexionDB.cerrar(con, ps, null);
        }
    }

    // Elimina un pedido por su id.
    // Devuelve false si no existe un pedido con ese id.
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM pedidos WHERE id = ?";
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
