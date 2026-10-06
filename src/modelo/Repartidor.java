package modelo;

public class Repartidor {

    private int id;
    private String nombre;

    // Constructor para un repartidor nuevo: el id lo asigna la base de datos
    public Repartidor(String nombre) {
        this.nombre = nombre;
    }

    // Constructor para un repartidor leido desde la base de datos
    public Repartidor(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // Texto legible para los JComboBox: id - nombre
    @Override
    public String toString() {
        return id + " - " + nombre;
    }
}
