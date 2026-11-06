package sara.modelo;

/* Clase Corte: periodo evaluativo (I, II, III y IV Corte) */
public class Corte {

    private int id;
    private String nombre;

    public Corte(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
