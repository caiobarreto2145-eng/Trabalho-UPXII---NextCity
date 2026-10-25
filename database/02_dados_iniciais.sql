USE ecopontos_db;

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
