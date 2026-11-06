package sara.modelo;

import java.time.LocalDate;

/*
 * Clase Actividad
 * Una actividad evaluativa (quiz, tarea, examen...) con su porcentaje.
 * Pertenece a un docente, una asignatura, un grupo y un corte.
 */
public class Actividad {

    private int id;
    private int idUsuario;
    private int idAsignatura;
    private int idGrado;
    private int idSeccion;
    private int idCorte;
    private int anioLectivo;
    private String nombre;
    private double porcentaje;
    private LocalDate fecha;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public int getIdAsignatura() { return idAsignatura; }
    public void setIdAsignatura(int idAsignatura) { this.idAsignatura = idAsignatura; }

    public int getIdGrado() { return idGrado; }
    public void setIdGrado(int idGrado) { this.idGrado = idGrado; }

    public int getIdSeccion() { return idSeccion; }
    public void setIdSeccion(int idSeccion) { this.idSeccion = idSeccion; }

    public int getIdCorte() { return idCorte; }
    public void setIdCorte(int idCorte) { this.idCorte = idCorte; }

    public int getAnioLectivo() { return anioLectivo; }
    public void setAnioLectivo(int anioLectivo) { this.anioLectivo = anioLectivo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getPorcentaje() { return porcentaje; }
    public void setPorcentaje(double porcentaje) { this.porcentaje = porcentaje; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
}
