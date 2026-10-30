# Como colocar o EcoPontos no ar (Render + Aiven)

1. **Banco (Aiven):** crie um serviço MySQL no plano Free. No painel, copie Host, Port, User (avnadmin) e Password. O banco se chama `defaultdb`.
2. **Tabelas:** no MySQL Workbench, conecte no Aiven (SSL ligado) e rode `database/03_schema_nuvem.sql`.
3. **App (Render):** New → Web Service → conecte o repositório do GitHub → Runtime **Docker** → plano **Free**. Em Environment, crie:
   - `DB_URL` = `jdbc:mysql://HOST:PORT/defaultdb?sslMode=REQUIRED&serverTimezone=America/Sao_Paulo`
   - `DB_USER` = `avnadmin`
   - `DB_PASSWORD` = senha do Aiven
   - `JWT_SECRET` = uma frase aleatória com 32 caracteres ou mais
4. Crie sua conta pelo app e, no Workbench (Aiven), rode: `UPDATE users SET role = 'ADMIN' WHERE email = 'seu-email@exemplo.com';`
5. Na sua máquina, para rodar localmente, crie na raiz do projeto o arquivo `application-local.properties` (não vai para o GitHub) com: `spring.datasource.password=SUA_SENHA_DO_MYSQL_LOCAL`

Atenção: no plano gratuito do Render o app "dorme" após ~15 min sem uso (a primeira abertura leva ~30 s) e as fotos enviadas somem quando o serviço reinicia. Abra o app alguns minutos antes de apresentar.
