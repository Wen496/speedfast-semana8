package main;

import dao.ConexionDB;
import vista.Mensajes;
import vista.VentanaPrincipal;

import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            verificarConexion();
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }

    // Prueba la conexion al iniciar para avisar de inmediato si hay un problema
    private static void verificarConexion() {
        Connection con = null;
        try {
            con = ConexionDB.conectar();
            System.out.println("Conexion exitosa a speedfast_db.");
        } catch (SQLException e) {
            Mensajes.error(null, "Error de conexion",
                    "No se pudo conectar a la base de datos:\n" + e.getMessage());
        } finally {
            ConexionDB.cerrar(con, null, null);
        }
    }
}
