-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: sys
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `sys_config`
--

LOCK TABLES `sys_config` WRITE;
/*!40000 ALTER TABLE `sys_config` DISABLE KEYS */;
INSERT INTO `sys_config` VALUES ('diagnostics.allow_i_s_tables','OFF','2026-11-02 02:05:02',NULL),('diagnostics.include_raw','OFF','2026-11-02 02:05:02',NULL),('ps_thread_trx_info.max_length','65535','2026-11-02 02:05:02',NULL),('statement_performance_analyzer.limit','100','2026-11-02 02:05:02',NULL),('statement_performance_analyzer.view',NULL,'2026-11-02 02:05:02',NULL),('statement_truncate_len','64','2026-11-02 02:05:02',NULL);
/*!40000 ALTER TABLE `sys_config` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-06 13:55:30
-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: sara
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `actividad`
--

LOCK TABLES `actividad` WRITE;
/*!40000 ALTER TABLE `actividad` DISABLE KEYS */;
INSERT INTO `actividad` VALUES (1,2,7,7,16,1,2026,'Examen',50.00,'2026-04-05'),(2,2,7,7,16,1,2026,'Tarea',30.00,'2026-03-20'),(3,2,7,7,16,1,2026,'Quiz 1',20.00,'2026-03-10'),(4,2,7,7,16,2,2026,'Examen',50.00,'2026-04-05'),(5,2,7,7,16,2,2026,'Tarea',30.00,'2026-03-20'),(6,2,7,7,16,2,2026,'Quiz 1',20.00,'2026-03-10'),(7,2,7,7,16,3,2026,'Examen',50.00,'2026-04-05'),(8,2,7,7,16,3,2026,'Tarea',30.00,'2026-03-20'),(9,2,7,7,16,3,2026,'Quiz 1',20.00,'2026-03-10'),(10,2,7,7,16,4,2026,'Examen',50.00,'2026-04-05'),(11,2,7,7,16,4,2026,'Tarea',30.00,'2026-03-20'),(12,2,7,7,16,4,2026,'Quiz 1',20.00,'2026-03-10'),(16,2,11,7,16,1,2026,'Singing the alphabeth Song',30.00,'2026-01-11'),(17,2,11,7,16,1,2026,'Spell your name',30.00,'2026-02-11'),(18,2,11,7,16,1,2026,'Exam',40.00,'2026-03-11');
/*!40000 ALTER TABLE `actividad` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `asignatura`
--

LOCK TABLES `asignatura` WRITE;
/*!40000 ALTER TABLE `asignatura` DISABLE KEYS */;
INSERT INTO `asignatura` VALUES (10,'Biología','Secundaria'),(3,'Ciencias Naturales','Primaria'),(5,'Educación Física','Primaria'),(12,'Educación Física','Secundaria'),(4,'Estudios Sociales','Primaria'),(8,'Física','Secundaria'),(11,'Inglés','Secundaria'),(1,'Lengua y Literatura','Primaria'),(6,'Lengua y Literatura','Secundaria'),(2,'Matemática','Primaria'),(7,'Matemática','Secundaria'),(9,'Química','Secundaria');
/*!40000 ALTER TABLE `asignatura` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `asistencia`
--

LOCK TABLES `asistencia` WRITE;
/*!40000 ALTER TABLE `asistencia` DISABLE KEYS */;
/*!40000 ALTER TABLE `asistencia` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `calificacion`
--

LOCK TABLES `calificacion` WRITE;
/*!40000 ALTER TABLE `calificacion` DISABLE KEYS */;
INSERT INTO `calificacion` VALUES (1,1,1,47.00),(2,2,1,40.50),(3,3,1,34.50),(4,4,1,25.50),(5,5,1,43.50),(6,1,2,29.10),(7,2,2,25.20),(8,3,2,21.60),(9,4,2,16.20),(10,5,2,27.00),(11,1,3,18.40),(12,2,3,15.80),(13,3,3,13.40),(14,4,3,9.80),(15,5,3,17.00),(16,1,4,48.00),(17,2,4,41.50),(18,3,4,35.50),(19,4,4,26.50),(20,5,4,44.50),(21,1,5,29.70),(22,2,5,25.80),(23,3,5,22.20),(24,4,5,16.80),(25,5,5,27.60),(26,1,6,18.80),(27,2,6,16.20),(28,3,6,13.80),(29,4,6,10.20),(30,5,6,17.40),(31,1,7,45.50),(32,2,7,39.00),(33,3,7,33.00),(34,4,7,24.00),(35,5,7,42.00),(36,1,8,28.20),(37,2,8,24.30),(38,3,8,20.70),(39,4,8,15.30),(40,5,8,26.10),(41,1,9,17.80),(42,2,9,15.20),(43,3,9,12.80),(44,4,9,9.20),(45,5,9,16.40),(46,1,10,47.50),(47,2,10,41.00),(48,3,10,35.00),(49,4,10,26.00),(50,1,11,29.40),(51,2,11,25.50),(52,3,11,21.90),(53,4,11,16.50),(54,1,12,18.60),(55,2,12,16.00),(56,3,12,13.60),(57,4,12,10.00),(58,4,16,30.00),(59,4,17,30.00),(60,4,18,40.00),(61,5,16,20.00),(62,5,17,10.00),(63,5,18,10.00),(64,1,16,30.00),(65,1,17,25.00),(66,1,18,5.00),(67,2,16,30.00),(68,2,17,30.00),(69,2,18,30.00),(70,3,16,5.00),(71,3,17,6.00),(72,3,18,5.00);
/*!40000 ALTER TABLE `calificacion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `corte`
--

LOCK TABLES `corte` WRITE;
/*!40000 ALTER TABLE `corte` DISABLE KEYS */;
INSERT INTO `corte` VALUES (1,'I Corte','Primer corte evaluativo'),(2,'II Corte','Segundo corte evaluativo'),(3,'III Corte','Tercer corte evaluativo'),(4,'IV Corte','Cuarto corte evaluativo');
/*!40000 ALTER TABLE `corte` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `estudiante`
--

LOCK TABLES `estudiante` WRITE;
/*!40000 ALTER TABLE `estudiante` DISABLE KEYS */;
INSERT INTO `estudiante` VALUES (1,'E-001','Ana','Lopez','F','2013-03-14',NULL,NULL,2),(2,'E-002','Carlos','Mendoza','M','2013-07-02',NULL,NULL,2),(3,'E-003','Sofía','Rivas','F','2012-11-20',NULL,NULL,2),(4,'E-004','Luis','Castillo','M','2013-01-09',NULL,NULL,2),(5,'E-005','María','Gómez','F','2013-05-27',NULL,NULL,2);
/*!40000 ALTER TABLE `estudiante` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `grado`
--

LOCK TABLES `grado` WRITE;
/*!40000 ALTER TABLE `grado` DISABLE KEYS */;
INSERT INTO `grado` VALUES (4,'Cuarto grado'),(10,'Décimo grado'),(9,'Noveno grado'),(8,'Octavo grado'),(1,'Primer grado'),(5,'Quinto grado'),(2,'Segundo grado'),(7,'Séptimo grado'),(6,'Sexto grado'),(3,'Tercer grado'),(11,'Undécimo grado');
/*!40000 ALTER TABLE `grado` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `matricula`
--

LOCK TABLES `matricula` WRITE;
/*!40000 ALTER TABLE `matricula` DISABLE KEYS */;
INSERT INTO `matricula` VALUES (1,1,7,16,2026,'Regular','2026-02-01','ACTIVA'),(2,2,7,16,2026,'Regular','2026-02-01','ACTIVA'),(3,3,7,16,2026,'Regular','2026-02-01','ACTIVA'),(4,4,7,16,2026,'Regular','2026-02-01','ACTIVA'),(5,5,7,16,2026,'Regular','2026-02-01','ACTIVA');
/*!40000 ALTER TABLE `matricula` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `nota`
--

LOCK TABLES `nota` WRITE;
/*!40000 ALTER TABLE `nota` DISABLE KEYS */;
INSERT INTO `nota` VALUES (32,1,7,1,94.50,'AA'),(33,2,7,1,81.50,'AS'),(34,3,7,1,69.50,'AF'),(35,4,7,1,51.50,'AI'),(36,5,7,1,87.50,'AS'),(37,1,7,2,96.50,'AA'),(38,2,7,2,83.50,'AS'),(39,3,7,2,71.50,'AF'),(40,4,7,2,53.50,'AI'),(41,5,7,2,89.50,'AS'),(42,1,7,3,91.50,'AA'),(43,2,7,3,78.50,'AS'),(44,3,7,3,66.50,'AF'),(45,4,7,3,48.50,'AI'),(46,5,7,3,84.50,'AS'),(47,1,7,4,95.50,'AA'),(48,2,7,4,82.50,'AS'),(49,3,7,4,70.50,'AF'),(50,4,7,4,52.50,'AI'),(51,4,11,1,100.00,'AA'),(52,5,11,1,40.00,'AI'),(53,1,11,1,60.00,'AF'),(54,2,11,1,90.00,'AA'),(55,3,11,1,16.00,'AI');
/*!40000 ALTER TABLE `nota` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `rendimiento`
--

LOCK TABLES `rendimiento` WRITE;
/*!40000 ALTER TABLE `rendimiento` DISABLE KEYS */;
INSERT INTO `rendimiento` VALUES (1,7,2026,'Regular',5,5,3,1,0,100.00);
/*!40000 ALTER TABLE `rendimiento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `seccion`
--

LOCK TABLES `seccion` WRITE;
/*!40000 ALTER TABLE `seccion` DISABLE KEYS */;
INSERT INTO `seccion` VALUES (10,1,'A'),(9,1,'B'),(14,2,'A'),(13,2,'B'),(20,3,'A'),(19,3,'B'),(2,4,'A'),(1,4,'B'),(12,5,'A'),(11,5,'B'),(18,6,'A'),(17,6,'B'),(16,7,'A'),(15,7,'B'),(8,8,'A'),(7,8,'B'),(6,9,'A'),(5,9,'B'),(4,10,'A'),(3,10,'B'),(22,11,'A'),(21,11,'B');
/*!40000 ALTER TABLE `seccion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` VALUES (1,'Administrador','SARA','admin','240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9','ADMINISTRADOR',1),(2,'Docente','Demostración','docente','9228105f4ea40ae4b70fde55ee2caf4495c1f188333c41ef566417ab04819f56','DOCENTE',1);
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-06 13:55:30
