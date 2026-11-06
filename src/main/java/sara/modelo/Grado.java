package sara.modelo;

/* Clase Grado: un grado escolar (Primer grado, Septimo grado, etc.) */
public class Grado {

    private int id;
    private String nombre;

    public Grado(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    // Lo que se muestra dentro de los combos
    @Override
    public String toString() {
        return nombre;
    }
}
