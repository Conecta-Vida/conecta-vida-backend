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
