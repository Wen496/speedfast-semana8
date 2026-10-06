package vista;

import dao.RepartidorDAO;
import modelo.Repartidor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Ventana para registrar, editar, eliminar y listar repartidores.
 */
public class VentanaRepartidores extends JFrame {

    private static final int LARGO_MAXIMO_NOMBRE = 100;

    private final RepartidorDAO repartidorDAO;
    // Se ejecuta despues de cada cambio para refrescar las demas ventanas
    private final Runnable alCambiarDatos;

    private final JTextField campoId;
    private final JTextField campoNombre;
    private final DefaultTableModel modeloTabla;
    private final JTable tabla;

    // Repartidores mostrados en la tabla, en el mismo orden de las filas
    private List<Repartidor> repartidoresEnTabla = new ArrayList<>();

    public VentanaRepartidores(RepartidorDAO repartidorDAO, Runnable alCambiarDatos) {
        this.repartidorDAO = repartidorDAO;
        this.alCambiarDatos = alCambiarDatos;

        setTitle("Gestion de Repartidores");
        setSize(500, 420);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Formulario
        JPanel panelFormulario = new JPanel(new GridLayout(2, 2, 10, 10));
        panelFormulario.add(new JLabel("ID:"));
        campoId = new JTextField();
        campoId.setEditable(false);
        panelFormulario.add(campoId);

        panelFormulario.add(new JLabel("Nombre:"));
        campoNombre = new JTextField();
        panelFormulario.add(campoNombre);

        // Botones CRUD
        JButton btnAgregar = new JButton("Agregar");
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnLimpiar = new JButton("Limpiar");

        btnAgregar.addActionListener(e -> agregar());
        btnEditar.addActionListener(e -> editar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        JPanel panelBotones = new JPanel();
        panelBotones.add(btnAgregar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);

        JPanel panelSuperior = new JPanel(new BorderLayout(10, 10));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        panelSuperior.add(panelFormulario, BorderLayout.CENTER);
        panelSuperior.add(panelBotones, BorderLayout.SOUTH);

        // Tabla
        modeloTabla = new DefaultTableModel(new String[]{"ID", "Nombre"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccionEnFormulario();
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Repartidores registrados"));

        add(panelSuperior, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    // Vuelve a leer los repartidores desde la base de datos y actualiza la tabla
    public void recargar() {
        try {
            repartidoresEnTabla = repartidorDAO.readAll();
        } catch (SQLException e) {
            repartidoresEnTabla = new ArrayList<>();
            Mensajes.errorSQL(this, "cargar los repartidores", e);
        }

        modeloTabla.setRowCount(0);
        for (Repartidor r : repartidoresEnTabla) {
            modeloTabla.addRow(new Object[]{r.getId(), r.getNombre()});
        }
    }

    private void agregar() {
        String nombre = validarNombre();
        if (nombre == null) {
            return;
        }

        try {
            repartidorDAO.create(new Repartidor(nombre));
            Mensajes.exito(this, "Repartidor registrado correctamente.");
            despuesDeCambio();
        } catch (SQLException e) {
            Mensajes.errorSQL(this, "registrar el repartidor", e);
        }
    }

    private void editar() {
        Integer id = idSeleccionado();
        if (id == null) {
            return;
        }
        String nombre = validarNombre();
        if (nombre == null) {
            return;
        }

        try {
            if (repartidorDAO.update(new Repartidor(id, nombre))) {
                Mensajes.exito(this, "Repartidor actualizado correctamente.");
            } else {
                Mensajes.advertencia(this, "El repartidor ya no existe en la base de datos.");
            }
            despuesDeCambio();
        } catch (SQLException e) {
            Mensajes.errorSQL(this, "actualizar el repartidor", e);
        }
    }

    private void eliminar() {
        Integer id = idSeleccionado();
        if (id == null) {
            return;
        }
        if (!Mensajes.confirmar(this, "Se eliminara el repartidor " + id + ". Desea continuar?")) {
            return;
        }

        try {
            if (repartidorDAO.delete(id)) {
                Mensajes.exito(this, "Repartidor eliminado correctamente.");
            } else {
                Mensajes.advertencia(this, "El repartidor ya no existe en la base de datos.");
            }
            despuesDeCambio();
        } catch (SQLException e) {
            Mensajes.errorSQL(this, "eliminar el repartidor", e);
        }
    }

    // Valida el nombre: obligatorio, maximo 100 caracteres y solo letras y espacios.
    // Devuelve el nombre limpio o null si no es valido.
    private String validarNombre() {
        String nombre = campoNombre.getText().trim();

        if (nombre.isEmpty()) {
            Mensajes.advertencia(this, "El nombre es obligatorio.");
            return null;
        }
        if (nombre.length() > LARGO_MAXIMO_NOMBRE) {
            Mensajes.advertencia(this, "El nombre no puede superar los " + LARGO_MAXIMO_NOMBRE + " caracteres.");
            return null;
        }
        if (!nombre.matches("[\\p{L} ]+")) {
            Mensajes.advertencia(this, "El nombre solo puede contener letras y espacios.");
            return null;
        }
        return nombre;
    }

    // Devuelve el id del repartidor seleccionado o null si no hay seleccion
    private Integer idSeleccionado() {
        if (campoId.getText().isEmpty()) {
            Mensajes.advertencia(this, "Selecciona un repartidor de la tabla.");
            return null;
        }
        return Integer.parseInt(campoId.getText());
    }

    private void cargarSeleccionEnFormulario() {
        int fila = tabla.getSelectedRow();
        if (fila >= 0 && fila < repartidoresEnTabla.size()) {
            Repartidor r = repartidoresEnTabla.get(fila);
            campoId.setText(String.valueOf(r.getId()));
            campoNombre.setText(r.getNombre());
        }
    }

    private void limpiarFormulario() {
        tabla.clearSelection();
        campoId.setText("");
        campoNombre.setText("");
    }

    private void despuesDeCambio() {
        limpiarFormulario();
        recargar();
        alCambiarDatos.run();
    }
}
