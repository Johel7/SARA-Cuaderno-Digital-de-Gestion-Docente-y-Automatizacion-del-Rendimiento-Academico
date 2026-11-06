package sara.modelo;

/* Clase Asignatura: materia que se imparte (Matematica, Fisica...) */
public class Asignatura {

    private int id;
    private String nombre;
    private String regimen;

    public Asignatura(int id, String nombre, String regimen) {
        this.id = id;
        this.nombre = nombre;
        this.regimen = regimen;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRegimen() {
        return regimen;
    }

    @Override
    public String toString() {
        return nombre + " (" + regimen + ")";
    }
}
