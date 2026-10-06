package vista;

import dao.EntregaDAO;
import dao.PedidoDAO;
import dao.RepartidorDAO;
import modelo.Entrega;
import modelo.Pedido;
import modelo.Repartidor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Ventana para registrar, editar, eliminar y listar entregas.
 * Pedido y repartidor se eligen desde JComboBox cargados desde la base de datos.
 */
public class VentanaEntregas extends JFrame {

    private static final String TODOS = "TODOS";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final EntregaDAO entregaDAO;
    private final PedidoDAO pedidoDAO;
    private final RepartidorDAO repartidorDAO;

    private final JTextField campoId;
    private final JComboBox<Pedido> comboPedido;
    private final JComboBox<Repartidor> comboRepartidor;
    private final JTextField campoFecha;
    private final JTextField campoHora;
    // Los filtros contienen "TODOS" y luego objetos Pedido o Repartidor
    private final JComboBox<Object> filtroPedido;
    private final JComboBox<Object> filtroRepartidor;
    private final DefaultTableModel modeloTabla;
    private final JTable tabla;

    private List<Pedido> pedidos = new ArrayList<>();
    private List<Repartidor> repartidores = new ArrayList<>();
    private List<Entrega> todasLasEntregas = new ArrayList<>();
    // Entregas visibles en la tabla despues de aplicar los filtros, en el orden de las filas
    private List<Entrega> entregasEnTabla = new ArrayList<>();

    // Evita que los filtros redibujen la tabla mientras se recargan sus opciones
    private boolean recargandoCombos = false;

    public VentanaEntregas(EntregaDAO entregaDAO, PedidoDAO pedidoDAO, RepartidorDAO repartidorDAO) {
        this.entregaDAO = entregaDAO;
        this.pedidoDAO = pedidoDAO;
        this.repartidorDAO = repartidorDAO;

        setTitle("Gestion de Entregas");
        setSize(700, 520);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Formulario
        JPanel panelFormulario = new JPanel(new GridLayout(5, 2, 10, 10));
        panelFormulario.add(new JLabel("ID:"));
        campoId = new JTextField();
        campoId.setEditable(false);
        panelFormulario.add(campoId);

        panelFormulario.add(new JLabel("Pedido:"));
        comboPedido = new JComboBox<>();
        panelFormulario.add(comboPedido);

        panelFormulario.add(new JLabel("Repartidor:"));
        comboRepartidor = new JComboBox<>();
        panelFormulario.add(comboRepartidor);

        panelFormulario.add(new JLabel("Fecha (AAAA-MM-DD):"));
        campoFecha = new JTextField();
        panelFormulario.add(campoFecha);

        panelFormulario.add(new JLabel("Hora (HH:MM):"));
        campoHora = new JTextField();
        panelFormulario.add(campoHora);

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
        filtroPedido = new JComboBox<>();
        filtroRepartidor = new JComboBox<>();
        filtroPedido.addActionListener(e -> {
            if (!recargandoCombos) {
                mostrarEntregasFiltradas();
            }
        });
        filtroRepartidor.addActionListener(e -> {
            if (!recargandoCombos) {
                mostrarEntregasFiltradas();
            }
        });

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFiltros.add(new JLabel("Filtrar por pedido:"));
        panelFiltros.add(filtroPedido);
        panelFiltros.add(new JLabel("Filtrar por repartidor:"));
        panelFiltros.add(filtroRepartidor);

        // Tabla
        modeloTabla = new DefaultTableModel(new String[]{"ID", "Pedido", "Repartidor", "Fecha", "Hora"}, 0) {
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
        panelTabla.setBorder(BorderFactory.createTitledBorder("Entregas registradas"));
        panelTabla.add(panelFiltros, BorderLayout.NORTH);
        panelTabla.add(new JScrollPane(tabla), BorderLayout.CENTER);

        add(panelSuperior, BorderLayout.NORTH);
        add(panelTabla, BorderLayout.CENTER);
    }

    // Vuelve a leer pedidos, repartidores y entregas, y refresca combos y tabla.
    // Se llama al abrir la ventana y cada vez que cambian pedidos o repartidores.
    public void recargar() {
        try {
            pedidos = pedidoDAO.readAll();
            repartidores = repartidorDAO.readAll();
            todasLasEntregas = entregaDAO.readAll();
        } catch (SQLException e) {
            Mensajes.errorSQL(this, "cargar los datos de entregas", e);
        }
        recargarCombos();
        mostrarEntregasFiltradas();
    }

    // Carga los combos del formulario y de los filtros conservando la seleccion actual por id
    private void recargarCombos() {
        recargandoCombos = true;

        Pedido pedidoActual = (Pedido) comboPedido.getSelectedItem();
        Repartidor repartidorActual = (Repartidor) comboRepartidor.getSelectedItem();
        Object filtroPedidoActual = filtroPedido.getSelectedItem();
        Object filtroRepartidorActual = filtroRepartidor.getSelectedItem();

        comboPedido.removeAllItems();
        filtroPedido.removeAllItems();
        filtroPedido.addItem(TODOS);
        for (Pedido p : pedidos) {
            comboPedido.addItem(p);
            filtroPedido.addItem(p);
        }

        comboRepartidor.removeAllItems();
        filtroRepartidor.removeAllItems();
        filtroRepartidor.addItem(TODOS);
        for (Repartidor r : repartidores) {
            comboRepartidor.addItem(r);
            filtroRepartidor.addItem(r);
        }

        if (pedidoActual != null) {
            seleccionarPedido(comboPedido, pedidoActual.getId());
        }
        if (repartidorActual != null) {
            seleccionarRepartidor(comboRepartidor, repartidorActual.getId());
        }
        if (filtroPedidoActual instanceof Pedido) {
            seleccionarPedido(filtroPedido, ((Pedido) filtroPedidoActual).getId());
        }
        if (filtroRepartidorActual instanceof Repartidor) {
            seleccionarRepartidor(filtroRepartidor, ((Repartidor) filtroRepartidorActual).getId());
        }

        recargandoCombos = false;
    }

    // Muestra en la tabla solo las entregas del pedido y/o repartidor elegidos en los filtros
    private void mostrarEntregasFiltradas() {
        Object pedidoFiltro = filtroPedido.getSelectedItem();
        Object repartidorFiltro = filtroRepartidor.getSelectedItem();

        List<Entrega> filtradas = new ArrayList<>();
        for (Entrega en : todasLasEntregas) {
            boolean cumplePedido = !(pedidoFiltro instanceof Pedido)
                    || en.getIdPedido() == ((Pedido) pedidoFiltro).getId();
            boolean cumpleRepartidor = !(repartidorFiltro instanceof Repartidor)
                    || en.getIdRepartidor() == ((Repartidor) repartidorFiltro).getId();
            if (cumplePedido && cumpleRepartidor) {
                filtradas.add(en);
            }
        }

        entregasEnTabla = filtradas;
        modeloTabla.setRowCount(0);
        for (Entrega en : entregasEnTabla) {
            modeloTabla.addRow(new Object[]{
                    en.getId(),
                    textoPedido(en.getIdPedido()),
                    textoRepartidor(en.getIdRepartidor()),
                    en.getFecha() != null ? en.getFecha().format(FORMATO_FECHA) : "",
                    en.getHora() != null ? en.getHora().format(FORMATO_HORA) : ""
            });
        }
    }

    private void agregar() {
        Entrega datos = validarFormulario();
        if (datos == null) {
            return;
        }

        try {
            entregaDAO.create(datos);
            Mensajes.exito(this, "Entrega registrada correctamente.");
            despuesDeCambio();
        } catch (SQLException e) {
            Mensajes.errorSQL(this, "registrar la entrega", e);
        }
    }

    private void editar() {
        Integer id = idSeleccionado();
        if (id == null) {
            return;
        }
        Entrega datos = validarFormulario();
        if (datos == null) {
            return;
        }
        datos.setId(id);

        try {
            if (entregaDAO.update(datos)) {
                Mensajes.exito(this, "Entrega actualizada correctamente.");
            } else {
                Mensajes.advertencia(this, "La entrega ya no existe en la base de datos.");
            }
            despuesDeCambio();
        } catch (SQLException e) {
            Mensajes.errorSQL(this, "actualizar la entrega", e);
        }
    }

    private void eliminar() {
        Integer id = idSeleccionado();
        if (id == null) {
            return;
        }
        if (!Mensajes.confirmar(this, "Se eliminara la entrega " + id + ". Desea continuar?")) {
            return;
        }

        try {
            if (entregaDAO.delete(id)) {
                Mensajes.exito(this, "Entrega eliminada correctamente.");
            } else {
                Mensajes.advertencia(this, "La entrega ya no existe en la base de datos.");
            }
            despuesDeCambio();
        } catch (SQLException e) {
            Mensajes.errorSQL(this, "eliminar la entrega", e);
        }
    }

    // Valida pedido, repartidor, fecha y hora.
    // Devuelve una Entrega con los datos del formulario o null si algo no es valido.
    private Entrega validarFormulario() {
        Pedido pedido = (Pedido) comboPedido.getSelectedItem();
        Repartidor repartidor = (Repartidor) comboRepartidor.getSelectedItem();
        String textoFecha = campoFecha.getText().trim();
        String textoHora = campoHora.getText().trim();

        if (pedido == null) {
            Mensajes.advertencia(this, "Debes seleccionar un pedido. Si la lista esta vacia, registra primero un pedido.");
            return null;
        }
        if (repartidor == null) {
            Mensajes.advertencia(this, "Debes seleccionar un repartidor. Si la lista esta vacia, registra primero un repartidor.");
            return null;
        }
        if (textoFecha.isEmpty() || textoHora.isEmpty()) {
            Mensajes.advertencia(this, "La fecha y la hora son obligatorias.");
            return null;
        }

        LocalDate fecha;
        try {
            fecha = LocalDate.parse(textoFecha, FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            Mensajes.advertencia(this, "La fecha debe tener el formato AAAA-MM-DD y ser valida. Ejemplo: 2026-10-05");
            return null;
        }

        LocalTime hora;
        try {
            hora = LocalTime.parse(textoHora, FORMATO_HORA);
        } catch (DateTimeParseException e) {
            Mensajes.advertencia(this, "La hora debe tener el formato HH:MM (24 horas). Ejemplo: 14:30");
            return null;
        }

        return new Entrega(pedido.getId(), repartidor.getId(), fecha, hora);
    }

    // Devuelve el id de la entrega seleccionada o null si no hay seleccion
    private Integer idSeleccionado() {
        if (campoId.getText().isEmpty()) {
            Mensajes.advertencia(this, "Selecciona una entrega de la tabla.");
            return null;
        }
        return Integer.parseInt(campoId.getText());
    }

    private void cargarSeleccionEnFormulario() {
        int fila = tabla.getSelectedRow();
        if (fila >= 0 && fila < entregasEnTabla.size()) {
            Entrega en = entregasEnTabla.get(fila);
            campoId.setText(String.valueOf(en.getId()));
            seleccionarPedido(comboPedido, en.getIdPedido());
            seleccionarRepartidor(comboRepartidor, en.getIdRepartidor());
            campoFecha.setText(en.getFecha() != null ? en.getFecha().format(FORMATO_FECHA) : "");
            campoHora.setText(en.getHora() != null ? en.getHora().format(FORMATO_HORA) : "");
        }
    }

    private void limpiarFormulario() {
        tabla.clearSelection();
        campoId.setText("");
        if (comboPedido.getItemCount() > 0) {
            comboPedido.setSelectedIndex(0);
        }
        if (comboRepartidor.getItemCount() > 0) {
            comboRepartidor.setSelectedIndex(0);
        }
        campoFecha.setText("");
        campoHora.setText("");
    }

    private void despuesDeCambio() {
        limpiarFormulario();
        recargar();
    }

    // Texto legible "id - direccion" para la columna Pedido
    private String textoPedido(int idPedido) {
        for (Pedido p : pedidos) {
            if (p.getId() == idPedido) {
                return p.toString();
            }
        }
        return String.valueOf(idPedido);
    }

    // Texto legible "id - nombre" para la columna Repartidor
    private String textoRepartidor(int idRepartidor) {
        for (Repartidor r : repartidores) {
            if (r.getId() == idRepartidor) {
                return r.toString();
            }
        }
        return String.valueOf(idRepartidor);
    }

    // Selecciona en el combo el pedido con el id indicado (el combo conserva el id internamente)
    private static void seleccionarPedido(JComboBox<?> combo, int idPedido) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object item = combo.getItemAt(i);
            if (item instanceof Pedido && ((Pedido) item).getId() == idPedido) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    // Selecciona en el combo el repartidor con el id indicado
    private static void seleccionarRepartidor(JComboBox<?> combo, int idRepartidor) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object item = combo.getItemAt(i);
            if (item instanceof Repartidor && ((Repartidor) item).getId() == idRepartidor) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }
}
