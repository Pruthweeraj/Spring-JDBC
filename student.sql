-- phpMyAdmin SQL Dump
-- version 4.2.11
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: Jan 14, 2015 at 09:45 PM
-- Server version: 5.6.21
-- PHP Version: 5.5.19

# SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
# SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: 'shopping'
--

-- --------------------------------------------------------

--
-- Table structure for table 'student'
--
DROP TABLE student;
CREATE TABLE student (
  id SERIAL NOT NULL,
  dob date,
  email varchar(255) NOT NULL,
  mobno bigint NOT NULL,
  name varchar(20),
  password varchar(20)
);

--
-- Dumping data for table 'student'
--
DELETE FROM student;
# INSERT INTO 'student' ('id', 'dob', 'email', 'mobno', 'name', 'password') VALUES
#   (1, '2015-01-15 02:09:32', 'hello@gmail.com', 2324, 'Tarunkumar', 'hellohello');
# INSERT INTO 'student' ('id', 'dob', 'email', 'mobno', 'name', 'password') VALUES
#   (2, '2015-01-15 02:09:32', 'hello2@gmail.com', 2324, 'Tarunkumar2', 'hellohello');

--
-- Indexes for dumped tables
--

--
-- Indexes for table 'student'
--
# ALTER TABLE 'student'
#  ADD PRIMARY KEY ('id');
