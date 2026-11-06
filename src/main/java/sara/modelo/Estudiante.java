package sara.modelo;

import java.time.LocalDate;

/*
 * Clase Estudiante
 * Guarda los datos personales del estudiante y, cuando se consulta una
 * lista, tambien los datos de su matricula (grado, seccion, estado).
 */
public class Estudiante {

    // datos personales
    private int id;
    private String codigo;
    private String nombres;
    private String apellidos;
    private String sexo;            // "M" o "F"
    private LocalDate fechaNacimiento;
    private String direccion;
    private String telefono;
    private int idUsuario;          // docente que lo registro

    // datos de la matricula
    private int idMatricula;
    private int idGrado;
    private int idSeccion;
    private String nombreGrado;
    private String nombreSeccion;
    private int anioLectivo;
    private String modalidad;
    private String estado;          // ACTIVA o RETIRADA

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public int getIdMatricula() { return idMatricula; }
    public void setIdMatricula(int idMatricula) { this.idMatricula = idMatricula; }

    public int getIdGrado() { return idGrado; }
    public void setIdGrado(int idGrado) { this.idGrado = idGrado; }

    public int getIdSeccion() { return idSeccion; }
    public void setIdSeccion(int idSeccion) { this.idSeccion = idSeccion; }

    public String getNombreGrado() { return nombreGrado; }
    public void setNombreGrado(String nombreGrado) { this.nombreGrado = nombreGrado; }

    public String getNombreSeccion() { return nombreSeccion; }
    public void setNombreSeccion(String nombreSeccion) { this.nombreSeccion = nombreSeccion; }

    public int getAnioLectivo() { return anioLectivo; }
    public void setAnioLectivo(int anioLectivo) { this.anioLectivo = anioLectivo; }

    public String getModalidad() { return modalidad; }
    public void setModalidad(String modalidad) { this.modalidad = modalidad; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    public boolean estaRetirado() {
        return "RETIRADA".equals(estado);
    }
}
