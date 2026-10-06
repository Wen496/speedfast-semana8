package vista;

import dao.EntregaDAO;
import dao.PedidoDAO;
import dao.RepartidorDAO;

import javax.swing.*;
import java.awt.*;

/**
 * Menu principal de SpeedFast. Abre las ventanas de gestion de cada entidad.
 */
public class VentanaPrincipal extends JFrame {

    private final PedidoDAO pedidoDAO;
    private final RepartidorDAO repartidorDAO;
    private final EntregaDAO entregaDAO;

    // Se crea una sola ventana por entidad y se reutiliza cada vez que se abre
    private VentanaRepartidores ventanaRepartidores;
    private VentanaPedidos ventanaPedidos;
    private VentanaEntregas ventanaEntregas;

    public VentanaPrincipal() {
        pedidoDAO = new PedidoDAO();
        repartidorDAO = new RepartidorDAO();
        entregaDAO = new EntregaDAO();

        setTitle("SpeedFast - Gestion de Entregas");
        setSize(400, 260);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JButton btnRepartidores = new JButton("Gestionar repartidores");
        JButton btnPedidos = new JButton("Gestionar pedidos");
        JButton btnEntregas = new JButton("Gestionar entregas");

        btnRepartidores.addActionListener(e -> abrirRepartidores());
        btnPedidos.addActionListener(e -> abrirPedidos());
        btnEntregas.addActionListener(e -> abrirEntregas());

        panel.add(btnRepartidores);
        panel.add(btnPedidos);
        panel.add(btnEntregas);

        add(panel, BorderLayout.CENTER);
    }

    private void abrirRepartidores() {
        if (ventanaRepartidores == null) {
            ventanaRepartidores = new VentanaRepartidores(repartidorDAO, this::refrescarEntregas);
        }
        mostrar(ventanaRepartidores);
        ventanaRepartidores.recargar();
    }

    private void abrirPedidos() {
        if (ventanaPedidos == null) {
            ventanaPedidos = new VentanaPedidos(pedidoDAO, this::refrescarEntregas);
        }
        mostrar(ventanaPedidos);
        ventanaPedidos.recargar();
    }

    private void abrirEntregas() {
        if (ventanaEntregas == null) {
            ventanaEntregas = new VentanaEntregas(entregaDAO, pedidoDAO, repartidorDAO);
        }
        mostrar(ventanaEntregas);
        ventanaEntregas.recargar();
    }

    // Cuando cambian repartidores o pedidos, la ventana de entregas recarga sus combos y su tabla
    private void refrescarEntregas() {
        if (ventanaEntregas != null && ventanaEntregas.isVisible()) {
            ventanaEntregas.recargar();
        }
    }

    private void mostrar(JFrame ventana) {
        ventana.setVisible(true);
        ventana.toFront();
    }
}
