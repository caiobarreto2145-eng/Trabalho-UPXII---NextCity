-- EcoPontos - estrutura completa do banco (versão final com todos os passos)
CREATE DATABASE IF NOT EXISTS ecopontos_db;
USE ecopontos_db;

CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  points INT NOT NULL DEFAULT 0,          -- saldo para gastar
  total_points INT NOT NULL DEFAULT 0,    -- acumulado (ranking e nível)
  role VARCHAR(10) NOT NULL DEFAULT 'USER', -- USER, PARTNER ou ADMIN
  created_at DATETIME
);

CREATE TABLE IF NOT EXISTS sustainable_actions (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  description TEXT,
  category VARCHAR(50),
  points INT NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  requires_proof BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS action_records (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  action_id INT NOT NULL,
  points_earned INT NOT NULL,
  created_at DATETIME,
  status VARCHAR(10) NOT NULL DEFAULT 'APPROVED', -- PENDING, APPROVED, REJECTED
  photo_path VARCHAR(255) NULL,
  reviewed_at DATETIME NULL,
  review_note VARCHAR(255) NULL,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (action_id) REFERENCES sustainable_actions(id)
);

CREATE TABLE IF NOT EXISTS rewards (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  description TEXT,
  points_required INT NOT NULL,
  stock INT NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS redemptions (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  reward_id INT NOT NULL,
  points_used INT NOT NULL,
  created_at DATETIME,
  voucher_code VARCHAR(20) NOT NULL UNIQUE,
  status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE ou USED
  used_at DATETIME NULL,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (reward_id) REFERENCES rewards(id)
);

CREATE TABLE IF NOT EXISTS point_transactions (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  type VARCHAR(20) NOT NULL, -- EARN ou REDEEM
  amount INT NOT NULL,
  description VARCHAR(255),
  created_at DATETIME,
  FOREIGN KEY (user_id) REFERENCES users(id)
);
