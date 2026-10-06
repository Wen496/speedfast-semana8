package vista;

import dao.PedidoDAO;
import modelo.Pedido;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Ventana para registrar, editar, eliminar y listar pedidos,
 * con filtros opcionales por estado y por tipo.
 */
public class VentanaPedidos extends JFrame {

    private static final int LARGO_MAXIMO_DIRECCION = 100;
    private static final String TODOS = "TODOS";

    private final PedidoDAO pedidoDAO;
    // Se ejecuta despues de cada cambio para refrescar las demas ventanas
    private final Runnable alCambiarDatos;

    private final JTextField campoId;
    private final JTextField campoDireccion;
    private final JComboBox<String> comboTipo;
    private final JComboBox<String> comboEstado;
    private final JComboBox<String> filtroEstado;
    private final JComboBox<String> filtroTipo;
    private final DefaultTableModel modeloTabla;
    private final JTable tabla;

    // Todos los pedidos leidos desde la base de datos
    private List<Pedido> todosLosPedidos = new ArrayList<>();
    // Pedidos visibles en la tabla despues de aplicar los filtros, en el orden de las filas
    private List<Pedido> pedidosEnTabla = new ArrayList<>();

    public VentanaPedidos(PedidoDAO pedidoDAO, Runnable alCambiarDatos) {
        this.pedidoDAO = pedidoDAO;
        this.alCambiarDatos = alCambiarDatos;

        setTitle("Gestion de Pedidos");
        setSize(650, 500);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Formulario
        JPanel panelFormulario = new JPanel(new GridLayout(4, 2, 10, 10));
        panelFormulario.add(new JLabel("ID:"));
        campoId = new JTextField();
        campoId.setEditable(false);
        panelFormulario.add(campoId);

        panelFormulario.add(new JLabel("Direccion:"));
        campoDireccion = new JTextField();
        panelFormulario.add(campoDireccion);

        panelFormulario.add(new JLabel("Tipo:"));
        comboTipo = new JComboBox<>(Pedido.TIPOS);
        panelFormulario.add(comboTipo);

        panelFormulario.add(new JLabel("Estado:"));
        comboEstado = new JComboBox<>(Pedido.ESTADOS);
        panelFormulario.add(comboEstado);

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

        // Filtros
        filtroEstado = new JComboBox<>(agregarOpcionTodos(Pedido.ESTADOS));
        filtroTipo = new JComboBox<>(agregarOpcionTodos(Pedido.TIPOS));
        filtroEstado.addActionListener(e -> mostrarPedidosFiltrados());
        filtroTipo.addActionListener(e -> mostrarPedidosFiltrados());

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFiltros.add(new JLabel("Filtrar por estado:"));
        panelFiltros.add(filtroEstado);
        panelFiltros.add(new JLabel("Filtrar por tipo:"));
        panelFiltros.add(filtroTipo);

        // Tabla
        modeloTabla = new DefaultTableModel(new String[]{"ID", "Direccion", "Tipo", "Estado"}, 0) {
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

        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBorder(BorderFactory.createTitledBorder("Pedidos registrados"));
        panelTabla.add(panelFiltros, BorderLayout.NORTH);
        panelTabla.add(new JScrollPane(tabla), BorderLayout.CENTER);

        add(panelSuperior, BorderLayout.NORTH);
        add(panelTabla, BorderLayout.CENTER);
    }

    // Vuelve a leer los pedidos desde la base de datos y actualiza la tabla
    public void recargar() {
        try {
            todosLosPedidos = pedidoDAO.readAll();
        } catch (SQLException e) {
            todosLosPedidos = new ArrayList<>();
            Mensajes.errorSQL(this, "cargar los pedidos", e);
        }
        mostrarPedidosFiltrados();
    }

    // Muestra en la tabla solo los pedidos que cumplen los filtros seleccionados
    private void mostrarPedidosFiltrados() {
        String estado = (String) filtroEstado.getSelectedItem();
        String tipo = (String) filtroTipo.getSelectedItem();

        List<Pedido> filtrados = new ArrayList<>();
        for (Pedido p : todosLosPedidos) {
            boolean cumpleEstado = TODOS.equals(estado) || estado.equals(p.getEstado());
            boolean cumpleTipo = TODOS.equals(tipo) || tipo.equals(p.getTipo());
            if (cumpleEstado && cumpleTipo) {
                filtrados.add(p);
            }
        }

        pedidosEnTabla = filtrados;
        modeloTabla.setRowCount(0);
        for (Pedido p : pedidosEnTabla) {
            modeloTabla.addRow(new Object[]{p.getId(), p.getDireccion(), p.getTipo(), p.getEstado()});
        }
    }

    private void agregar() {
        String direccion = validarDireccion();
        if (direccion == null) {
            return;
        }

        try {
            pedidoDAO.create(new Pedido(direccion, tipoSeleccionado(), estadoSeleccionado()));
            Mensajes.exito(this, "Pedido registrado correctamente.");
            despuesDeCambio();
        } catch (SQLException e) {
            Mensajes.errorSQL(this, "registrar el pedido", e);
        }
    }

    private void editar() {
        Integer id = idSeleccionado();
        if (id == null) {
            return;
        }
        String direccion = validarDireccion();
        if (direccion == null) {
            return;
        }

        try {
            if (pedidoDAO.update(new Pedido(id, direccion, tipoSeleccionado(), estadoSeleccionado()))) {
                Mensajes.exito(this, "Pedido actualizado correctamente.");
            } else {
                Mensajes.advertencia(this, "El pedido ya no existe en la base de datos.");
            }
            despuesDeCambio();
        } catch (SQLException e) {
            Mensajes.errorSQL(this, "actualizar el pedido", e);
        }
    }

    private void eliminar() {
        Integer id = idSeleccionado();
        if (id == null) {
            return;
        }
        if (!Mensajes.confirmar(this, "Se eliminara el pedido " + id + ". Desea continuar?")) {
            return;
        }

        try {
            if (pedidoDAO.delete(id)) {
                Mensajes.exito(this, "Pedido eliminado correctamente.");
            } else {
                Mensajes.advertencia(this, "El pedido ya no existe en la base de datos.");
            }
            despuesDeCambio();
        } catch (SQLException e) {
            Mensajes.errorSQL(this, "eliminar el pedido", e);
        }
    }

    // Valida la direccion: obligatoria y de maximo 100 caracteres.
    // Devuelve la direccion limpia o null si no es valida.
    private String validarDireccion() {
        String direccion = campoDireccion.getText().trim();

        if (direccion.isEmpty()) {
            Mensajes.advertencia(this, "La direccion es obligatoria.");
            return null;
        }
        if (direccion.length() > LARGO_MAXIMO_DIRECCION) {
            Mensajes.advertencia(this, "La direccion no puede superar los " + LARGO_MAXIMO_DIRECCION + " caracteres.");
            return null;
        }
        return direccion;
    }

    private String tipoSeleccionado() {
        return (String) comboTipo.getSelectedItem();
    }

    private String estadoSeleccionado() {
        return (String) comboEstado.getSelectedItem();
    }

    // Devuelve el id del pedido seleccionado o null si no hay seleccion
    private Integer idSeleccionado() {
        if (campoId.getText().isEmpty()) {
            Mensajes.advertencia(this, "Selecciona un pedido de la tabla.");
            return null;
        }
        return Integer.parseInt(campoId.getText());
    }

    private void cargarSeleccionEnFormulario() {
        int fila = tabla.getSelectedRow();
        if (fila >= 0 && fila < pedidosEnTabla.size()) {
            Pedido p = pedidosEnTabla.get(fila);
            campoId.setText(String.valueOf(p.getId()));
            campoDireccion.setText(p.getDireccion());
            comboTipo.setSelectedItem(p.getTipo());
            comboEstado.setSelectedItem(p.getEstado());
        }
    }

    private void limpiarFormulario() {
        tabla.clearSelection();
        campoId.setText("");
        campoDireccion.setText("");
        comboTipo.setSelectedIndex(0);
        comboEstado.setSelectedIndex(0);
    }

    private void despuesDeCambio() {
        limpiarFormulario();
        recargar();
        alCambiarDatos.run();
    }

    // Arma las opciones de un filtro: "TODOS" seguido de los valores permitidos
    private static String[] agregarOpcionTodos(String[] valores) {
        String[] opciones = new String[valores.length + 1];
        opciones[0] = TODOS;
        System.arraycopy(valores, 0, opciones, 1, valores.length);
        return opciones;
    }
}
