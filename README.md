# 🚀 Conecta Vida - Backend API

Repositório oficial da API (Spring Boot) do projeto **Conecta Vida**, responsável por fornecer dados para o aplicativo mobile (Cidadão) e para o painel administrativo web (Gestores de Saúde).

## 🛠️ Tecnologias Utilizadas
* **Java 21**
* **Spring Boot 3.2.5** (Arquitetura Multimodule: `core`, `admin-api`, `mobile-api`)
* **Banco de Dados:** PostgreSQL (Hospedado no Supabase)
* **Cache:** Redis
* **Gerenciador de Dependências:** Gradle

---

## 📋 Pré-requisitos
Antes de baixar o projeto, certifique-se de que sua máquina possui as seguintes ferramentas instaladas:

1. **[Java JDK 21](https://adoptium.net/)** (Verifique rodando `java -version` no terminal).
2. **[Docker Desktop](https://www.docker.com/products/docker-desktop/)** (Necessário para rodar o Redis localmente).
3. **IDE Recomendada:**
   * **IntelliJ IDEA** (Community ou Ultimate) - *Mais recomendado para projetos Spring multimodule.*
   * **Visual Studio Code** - *Requer as extensões "Extension Pack for Java" e "Spring Boot Extension Pack".*

---

## ⚙️ Passo a Passo para Instalação e Execução

### Passo 1: Clonar o Repositório
Abra o seu terminal e clone o projeto para a sua máquina:
```bash
https://github.com/Conecta-Vida/conecta-vida-backend.git
cd conecta-vida-backend

```

### Passo 2: Sincronizar o Gradle (Baixar Dependências)

O projeto utiliza o Gradle para gerenciar bibliotecas (como o driver do PostgreSQL, Spring Web e Spring Data JPA).

* **No IntelliJ:** Ao abrir a pasta do projeto, clique no ícone do elefante com a setinha de recarregar no menu lateral direito "Gradle" (ou na notificação "Load Gradle Project").
* **No VS Code:** Abra a paleta de comandos (`Ctrl + Shift + P`) e digite `Java: Clean workspace` seguido de `Java: Update Project`.

### Passo 3: Configurar o Redis (Cache)

A API exige uma conexão ativa com o Redis para iniciar o sistema de cache. Se for rodar localmente, abra o terminal e inicie o container via Docker:

```bash
docker run --name conecta-redis -p 6379:6379 -d redis

```

### Passo 4: Configurar as Variáveis de Ambiente (`application.properties`)

Navegue até os arquivos de propriedades dos módulos:

1. `admin-api/src/main/resources/application.properties`
2. `mobile-api/src/main/resources/application.properties`

Configure as credenciais de banco de dados e cache (solicite a senha real à equipe):

```properties
# ===============================
# BANCO DE DADOS (SUPABASE - POSTGRESQL)
# ===============================
# Utilizamos a porta 6543 (Session Pooler) para compatibilidade nativa com redes IPv4
spring.datasource.url=jdbc:postgresql://[aws-0-sa-east-1.pooler.supabase.com:6543/postgres](https://aws-0-sa-east-1.pooler.supabase.com:6543/postgres)
spring.datasource.username=postgres.pborxjuuexiqollesfxw
spring.datasource.password=COLOQUE_A_SENHA_AQUI
spring.datasource.driver-class-name=org.postgresql.Driver

# ===============================
# JPA / HIBERNATE
# ===============================
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# ===============================
# CACHE (REDIS)
# ===============================
spring.data.redis.host=localhost
spring.data.redis.port=6379

```

### Passo 5: Rodar a Aplicação

O projeto possui duas APIs distintas.

* **Para rodar a API do Painel Administrativo:**
Localize o arquivo `admin-api/src/main/java/br/edu/ifsp/conectaavida/admin/AdminApiApplication.java` e execute a classe. A API iniciará na porta `8080`.
* **Para rodar a API do Aplicativo Mobile:**
Localize o arquivo `mobile-api/src/main/java/br/edu/ifsp/conectaavida/mobile/MobileApiApplication.java` e execute a classe.

Se o terminal exibir `Started AdminApiApplication` ou `Started MobileApiApplication`, o backend estará rodando perfeitamente!

---

## 📚 Documentação da API (Swagger)

Com o projeto rodando, acesse a documentação interativa dos endpoints no seu navegador:

* **Admin API:** `http://localhost:8080/swagger-ui.html`


## Sistema de dependentes — Mobile API

O sistema de dependentes adiciona uma tabela separada `public.dependentes`. Dependentes não possuem login, senha, e-mail ou CPF; cada registro pertence a um usuário titular por `usuario_titular_id`. Os endpoints ficam exclusivamente na `mobile-api`; não foram adicionados controllers ou fluxos administrativos.

A entidade `Dependente` e o relacionamento opcional em `UsuarioCampanha` ficam no módulo compartilhado `core`. A migração SQL versionada está em `core/src/main/resources/db/migration/V2__dependentes_and_campaign_participants.sql`. O Flyway está habilitado nos módulos Admin e Mobile; por isso a migração aditiva será executada por qualquer API que inicialize com essa versão do módulo `core`, antes da validação do Hibernate. Não há alteração de controller, autenticação ou tela do Admin.

### Endpoints da Mobile API

Todas as rotas abaixo exigem JWT do titular:

- `GET /api/usuarios/{usuarioId}/dependentes`
- `GET /api/usuarios/{usuarioId}/dependentes/{dependenteId}`
- `POST /api/usuarios/{usuarioId}/dependentes`
- `PUT /api/usuarios/{usuarioId}/dependentes/{dependenteId}`
- `DELETE /api/usuarios/{usuarioId}/dependentes/{dependenteId}` (inativação lógica)

Na inscrição em campanha, `POST /api/campanhas/{id}/inscrever` aceita opcionalmente `{ "dependente_id": 123 }`. Sem esse campo, a inscrição é do titular. `DELETE /api/campanhas/{id}/desinscrever?dependente_id=123` cancela uma inscrição de dependente. `GET /api/campanhas/minhas` retorna o participante e o dependente associado a cada inscrição.

### Atualizar o banco Supabase

Em ambientes com Flyway ativo, a migração V2 é aplicada no deploy quando o arquivo estiver no classpath. Para executar manualmente, abra o SQL Editor do Supabase e execute o conteúdo de `core/src/main/resources/db/migration/V2__dependentes_and_campaign_participants.sql`. O script é idempotente; depois de aplicado manualmente, o Flyway poderá executá-lo sem recriar os objetos.

Antes de produção, faça backup e verifique `flyway_schema_history`. Não remova a coluna antiga de usuário/campanha nem registros existentes. A exclusão de dependentes é lógica (`ativo=false)) para preservar o histórico de eventos.
