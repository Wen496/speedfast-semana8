package vista;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Mensajes al usuario con JOptionPane, reutilizados por todas las ventanas.
 * Traduce los errores SQL mas comunes a un texto claro.
 */
public class Mensajes {

    // Codigos de error de MySQL relacionados con claves foraneas
    private static final int FK_REGISTRO_EN_USO = 1451;
    private static final int FK_REFERENCIA_INEXISTENTE = 1452;

    // Espacio extra a la derecha del texto para que Windows no lo corte con "..."
    private static final int MARGEN_DERECHO = 30;

    public static void exito(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, crearTexto(mensaje), "Operacion exitosa", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void advertencia(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, crearTexto(mensaje), "Datos invalidos", JOptionPane.WARNING_MESSAGE);
    }

    public static void error(Component padre, String titulo, String mensaje) {
        JOptionPane.showMessageDialog(padre, crearTexto(mensaje), titulo, JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirmar(Component padre, String mensaje) {
        int opcion = JOptionPane.showConfirmDialog(padre, crearTexto(mensaje), "Confirmar", JOptionPane.YES_NO_OPTION);
        return opcion == JOptionPane.YES_OPTION;
    }

    // Muestra un error de base de datos. "accion" describe lo que se intentaba hacer,
    // por ejemplo "eliminar el pedido".
    public static void errorSQL(Component padre, String accion, SQLException e) {
        // El detalle tecnico queda en consola para revision
        System.err.println("Error SQL al " + accion + " (codigo " + e.getErrorCode() + "): " + e.getMessage());

        String motivo;
        if (e.getErrorCode() == FK_REGISTRO_EN_USO) {
            motivo = "El registro tiene entregas asociadas. Elimina primero esas entregas.";
        } else if (e.getErrorCode() == FK_REFERENCIA_INEXISTENTE) {
            motivo = "El pedido o el repartidor seleccionado ya no existe. Recarga los datos.";
        } else if (e.getSQLState() != null && e.getSQLState().startsWith("08")) {
            motivo = "No hay conexion con la base de datos. Verifica que MySQL este iniciado.";
        } else {
            motivo = "Detalle: " + e.getMessage();
        }

        error(padre, "Error de base de datos", "No se pudo " + accion + ".\n" + motivo);
    }

    // Arma el texto del mensaje como etiqueta con margen derecho, para que se vea completo.
    // Los saltos de linea "\n" se convierten en saltos HTML.
    private static JLabel crearTexto(String mensaje) {
        String texto = mensaje.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>");
        JLabel etiqueta = new JLabel("<html>" + texto + "</html>");
        etiqueta.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, MARGEN_DERECHO));
        return etiqueta;
    }
}
