# SARA - Cuaderno Digital de Gestión Docente y Automatización del Rendimiento Académico

Este proyecto está desarrollado bajo Eclipse IDE y utiliza Maven para la gestión automática de dependencias y librerías, y MySQL Workbench para el servidor de base de datos. Está completamente optimizado para ser configurado e iniciado automáticamente con un solo clic.

---

### ⚙️ Requisitos previos en su computadora:
1. Tener el servidor local de **MySQL Workbench** activo y corriendo en el puerto 3306.
2. Tener configuradas las variables de entorno del sistema para **Java** (`javac`) y **Maven** (`mvn`).

---

### 🚀 Cómo ejecutar (Instrucciones en Un Solo Clic):
1. Descargue este repositorio como un archivo **ZIP** y descomprímalo en su computadora (o clónelo usando Git).
2. Busque el archivo ejecutable llamado **`ejecutar.bat`** en la carpeta raíz del proyecto y haga **doble clic** sobre él.
3. El asistente en la consola le realizará 3 preguntas automáticas para conectarse a su servidor local:
   * *Usuario de MySQL* (por defecto `root`).
   * *Su contraseña personal de MySQL Workbench* (El script es dinámico y utilizará la contraseña de su entorno local, no la del desarrollador).
   * *Nombre que desea darle a la Base de Datos* (ejemplo: `sara_db`).
4. **¡Eso es todo!** De forma automática, el script creará la base de datos en su Workbench, importará toda la estructura y registros desde el archivo `schema.sql`, compilará el código fuente con Maven e iniciará la aplicación de inmediato sin necesidad de importar el proyecto dentro de Eclipse.

---

### 🔑 Credenciales de Acceso al Sistema (Login)
Una vez que la aplicación Java se ejecute y muestre la pantalla de inicio de sesión, puede ingresar al sistema utilizando cualquiera de las siguientes cuentas de prueba que ya se encuentran precargadas en la base de datos:

* **Cuenta de Administrador:**
  * **Usuario:** admin
  * **Contraseña:** admin123

* **Cuenta de Docente:**
  * **Usuario:** docente
  * **Contraseña:** docente123
