package sara.modelo;

/* Clase Seccion: seccion (A, B, C...) que pertenece a un grado */
public class Seccion {

    private int id;
    private int idGrado;
    private String nombre;

    public Seccion(int id, int idGrado, String nombre) {
        this.id = id;
        this.idGrado = idGrado;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public int getIdGrado() {
        return idGrado;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
