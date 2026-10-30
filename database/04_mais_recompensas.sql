-- EcoPontos - Mais recompensas (sugestão dos participantes do teste de usabilidade)
-- ATENÇÃO: são recompensas de DEMONSTRAÇÃO. Troque nomes, valores e estoque quando as parcerias reais forem fechadas.
USE ecopontos_db;

INSERT INTO rewards (name, description, points_required, stock, active) VALUES
('Café sustentável', 'Um café em cafeteria parceira', 80, 30, true),
('Muda de árvore nativa', 'Uma muda para plantar em casa ou na calçada', 120, 25, true),
('Desconto de 10% em loja parceira', 'Cupom de 10% em lojas parceiras participantes', 150, 40, true),
('Ecobag reutilizável', 'Sacola de tecido para o dia a dia', 200, 20, true),
('Desconto em produtos de limpeza ecológicos', 'Cupom em produtos biodegradáveis', 250, 15, true);

SELECT id, name, points_required, stock FROM rewards ORDER BY points_required;
