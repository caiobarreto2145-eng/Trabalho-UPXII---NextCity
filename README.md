# 🌱 EcoPontos

## 📌 Sobre o projeto

Este projeto consiste no desenvolvimento de uma plataforma digital para incentivar a adoção de práticas sustentáveis por meio de **gamificação, pontos e recompensas**.

A plataforma permite que os usuários se cadastrem, registrem ações sustentáveis, acumulem pontos e utilizem esses pontos para resgatar recompensas.

O projeto busca unir **tecnologia e sustentabilidade**, tornando a prática de ações sustentáveis mais atrativa e incentivando a participação dos usuários.

## 🔗 Acesse o projeto

* 🌐 **Landing page:** file:///C:/Users/caion/Downloads/ecopontos.html
* 🎨 **Mockups das telas do app:** [`design/ecopontos_telas_app.png`](design/ecopontos_telas_app.png)
* ⚙️ **Código do backend:** [`ecopontos-api/`](ecopontos-api/)

## 🎯 Objetivo

Criar uma plataforma capaz de:

* Incentivar práticas sustentáveis;
* Registrar ações realizadas pelos usuários;
* Atribuir pontos de acordo com cada ação;
* Permitir o resgate de recompensas;
* Estimular a participação por meio da gamificação;
* Futuramente, possibilitar rankings e desafios.

## 👥 Público-alvo

O público-alvo inicial é formado principalmente por:

* Jovens;
* Adultos interessados em sustentabilidade;
* Escolas;
* Empresas;
* Órgãos públicos.

## 🚀 Funcionalidades

* [x] Cadastro de usuários
* [x] Login (autenticação via token JWT)
* [x] Cadastro de ações sustentáveis
* [x] Registro de ações realizadas
* [x] Sistema de pontuação (validado no backend)
* [x] Catálogo de recompensas
* [x] Resgate de recompensas
* [x] Ranking de usuários
* [x] Histórico de ações, resgates e transações
* [ ] Desafios sustentáveis (planejado para versão futura)
* [ ] Notificações (planejado para versão futura)
* [ ] Validação de ações por foto com aprovação de admin (ideia levantada em aula, planejada para versão futura)

## 🛠️ Tecnologias

### Backend

* Java 17
* Spring Boot (Web, Data JPA, Security)
* Autenticação JWT
* Senhas criptografadas com BCrypt

### Banco de dados

* MySQL 8

### Frontend

* HTML
* CSS
* JavaScript

## 🗄️ Banco de dados

O banco de dados armazena informações relacionadas a:

* Usuários;
* Ações sustentáveis;
* Registros de ações;
* Pontuação (transações de pontos);
* Recompensas;
* Resgates.

Modelado com 6 tabelas (`users`, `sustainable_actions`, `action_records`, `rewards`, `redemptions`, `point_transactions`), mapeadas via Spring Data JPA com `ddl-auto=validate`.

## 📊 Mercado

O projeto possui potencial de expansão no mercado brasileiro.

* **TAM:** mercado total brasileiro que pode utilizar uma plataforma digital de sustentabilidade.
* **SAM:** público inicialmente acessível, com foco no estado de São Paulo.
* **SOM:** parcela do público que será alcançada inicialmente por meio de divulgação e parcerias.

## 📁 Estrutura do projeto

```text
Trabalho-UPXII---NextCity/
│
├── ecopontos-api/     # Código do backend (Java/Spring Boot)
├── design/            # Mockups das telas do app
├── index.html         # Landing page (publicada via GitHub Pages)
└── README.md          # Informações principais
```

## 📚 Documentação

A documentação do projeto inclui:

* Lean Canvas;
* Estudo de mercado;
* Requisitos do sistema;
* Modelagem do banco de dados;
* Arquitetura do sistema;
* Documentação das funcionalidades.

## 📈 Desenvolvimento

O backend está funcional, com cadastro, login, registro de ações sustentáveis, pontuação, recompensas, ranking e histórico implementados e testados. A landing page e os mockups das telas do app também já foram produzidos. O projeto segue em desenvolvimento rumo à entrega completa.

## 🔮 Estudos futuros

Entre as possíveis melhorias futuras estão:

* Aplicativo para dispositivos móveis;
* Inteligência artificial;
* Sistema de desafios;
* Notificações;
* Validação de ações por foto com aprovação de admin;
* Parcerias com empresas e comércios;
* Expansão para escolas e órgãos públicos.

## 👨‍💻 Status

🚧 **Protótipo Funcional (70%)**

Este projeto está sendo desenvolvido para fins acadêmicos.
