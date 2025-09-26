-- MySQL dump 10.13  Distrib 9.4.0, for macos13.7 (arm64)
--
-- Host: localhost    Database: balance_db
-- ------------------------------------------------------
-- Server version	9.4.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `accounts`
--

DROP TABLE IF EXISTS `accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `accounts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `balance` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT 'Текущий баланс с точностью 2 знака',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `version` int DEFAULT '0' COMMENT 'Для оптимистичной блокировки',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Лицевые счета';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `accounts`
--

LOCK TABLES `accounts` WRITE;
/*!40000 ALTER TABLE `accounts` DISABLE KEYS */;
INSERT INTO `accounts` VALUES (1,784.25,'2025-09-23 06:54:22','2025-09-23 06:54:22',5);
/*!40000 ALTER TABLE `accounts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `accumulative_operations`
--

DROP TABLE IF EXISTS `accumulative_operations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `accumulative_operations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `account_id` bigint NOT NULL,
  `amount` decimal(15,2) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `period_date` date NOT NULL COMMENT 'Период накопления (год-месяц)',
  `reference_id` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `reference_id` (`reference_id`),
  KEY `idx_account_id` (`account_id`),
  KEY `idx_period_date` (`period_date`),
  KEY `idx_reference_id` (`reference_id`),
  CONSTRAINT `accumulative_operations_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `accounts` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Накопительные списания';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `accumulative_operations`
--

LOCK TABLES `accumulative_operations` WRITE;
/*!40000 ALTER TABLE `accumulative_operations` DISABLE KEYS */;
INSERT INTO `accumulative_operations` VALUES (1,1,15.75,'Ежемесячная комиссия с высокой точностью','2024-01-31','acc_high_precision_001','2025-09-24 05:56:36'),(2,1,15.75,'Комиссия с высокой точностью 1','2024-01-31','acc_prec_001','2025-09-24 05:58:17'),(3,1,12.35,'Комиссия с высокой точностью 2','2024-02-29','acc_prec_002','2025-09-24 05:58:17'),(4,1,18.93,'Комиссия с высокой точностью 3','2024-03-31','acc_prec_003','2025-09-24 05:58:17'),(5,1,9.12,'Бонус с высокой точностью','2024-04-30','acc_prec_004','2025-09-24 05:58:17');
/*!40000 ALTER TABLE `accumulative_operations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `processed_messages`
--

DROP TABLE IF EXISTS `processed_messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `processed_messages` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `message_id` varchar(255) NOT NULL COMMENT 'ID сообщения из ActiveMQ',
  `message_type` varchar(255) DEFAULT NULL,
  `account_id` bigint DEFAULT NULL,
  `processed_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `status` enum('PROCESSED','ERROR') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `message_id` (`message_id`),
  KEY `idx_message_id` (`message_id`),
  KEY `idx_account_id` (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Обработанные сообщения для идемпотентности';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `processed_messages`
--

LOCK TABLES `processed_messages` WRITE;
/*!40000 ALTER TABLE `processed_messages` DISABLE KEYS */;
/*!40000 ALTER TABLE `processed_messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transactions`
--

DROP TABLE IF EXISTS `transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transactions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `account_id` bigint NOT NULL,
  `amount` decimal(15,2) NOT NULL COMMENT 'Сумма операции с точностью 2 знака',
  `operation_type` enum('INCOME','EXPENSE') NOT NULL COMMENT 'Тип операции: приход/расход',
  `description` varchar(255) DEFAULT NULL,
  `reference_id` varchar(255) DEFAULT NULL,
  `operation_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `reference_id` (`reference_id`),
  KEY `idx_account_id` (`account_id`),
  KEY `idx_operation_date` (`operation_date`),
  KEY `idx_reference_id` (`reference_id`),
  CONSTRAINT `transactions_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `accounts` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Базовые операции прихода/расхода';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transactions`
--

LOCK TABLES `transactions` WRITE;
/*!40000 ALTER TABLE `transactions` DISABLE KEYS */;
INSERT INTO `transactions` VALUES (1,1,500.00,'INCOME','Тестовое пополнение','test_income_1','2025-09-23 07:01:18','2025-09-23 07:01:18'),(2,1,700.00,'EXPENSE','Штраф','test_income_2','2025-09-23 07:02:39','2025-09-23 07:02:39'),(3,1,14.82,'EXPENSE','Отредактированная операция','test_income_5','2025-09-23 07:30:27','2025-09-23 07:30:27');
/*!40000 ALTER TABLE `transactions` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-09-26 10:24:18
