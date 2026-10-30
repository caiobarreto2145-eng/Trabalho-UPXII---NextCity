-- EcoPontos - estrutura + dados iniciais para o banco na nuvem (Aiven, banco "defaultdb")

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

INSERT INTO sustainable_actions (name, description, category, points, active) VALUES
('Reciclagem', 'Separe corretamente os materiais recicláveis', 'Reciclagem', 20, true),
('Uso de bicicleta', 'Utilize bicicleta como meio de transporte', 'Mobilidade', 15, true),
('Economia de água', 'Evite desperdícios e reduza o consumo de água', 'Água', 10, true),
('Economia de energia', 'Reduza o consumo desnecessário de energia elétrica', 'Energia', 10, true),
('Plantio de árvore', 'Contribua para o meio ambiente participando de plantios', 'Meio ambiente', 50, true);

INSERT INTO rewards (name, description, points_required, stock, active) VALUES
('Brinde sustentável', 'Item produzido com materiais de baixo impacto ambiental', 100, 50, true),
('Produto ecológico', 'Item sustentável para o dia a dia', 250, 30, true),
('Cupom de desconto', 'Vantagem em produtos e serviços parceiros', 300, 20, true);

-- Depois de criar sua conta pelo app, torne-a administradora:
-- UPDATE users SET role = 'ADMIN' WHERE email = 'seu-email@exemplo.com';

-- Recompensas extras (demonstração)
INSERT INTO rewards (name, description, points_required, stock, active) VALUES
('Café sustentável', 'Um café em cafeteria parceira', 80, 30, true),
('Muda de árvore nativa', 'Uma muda para plantar em casa ou na calçada', 120, 25, true),
('Desconto de 10% em loja parceira', 'Cupom de 10% em lojas parceiras participantes', 150, 40, true),
('Ecobag reutilizável', 'Sacola de tecido para o dia a dia', 200, 20, true),
('Desconto em produtos de limpeza ecológicos', 'Cupom em produtos biodegradáveis', 250, 15, true);
