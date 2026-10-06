package modelo;

public class Pedido {

    // Valores permitidos, iguales a los ENUM de la tabla pedidos
    public static final String[] TIPOS = {"COMIDA", "ENCOMIENDA", "EXPRESS"};
    public static final String[] ESTADOS = {"PENDIENTE", "EN_REPARTO", "ENTREGADO"};

    private int id;
    private String direccion;
    private String tipo;
    private String estado;

    // Constructor para un pedido nuevo: el id lo asigna la base de datos
    public Pedido(String direccion, String tipo, String estado) {
        this.direccion = direccion;
        this.tipo = tipo;
        this.estado = estado;
    }

    // Constructor para un pedido leido desde la base de datos
    public Pedido(int id, String direccion, String tipo, String estado) {
        this.id = id;
        this.direccion = direccion;
        this.tipo = tipo;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    // Texto legible para los JComboBox: id - direccion
    @Override
    public String toString() {
        return id + " - " + direccion;
    }
}
