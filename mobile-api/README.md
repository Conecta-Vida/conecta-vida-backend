# Mobile API — desenvolvimento local

Este guia cobre somente o módulo `mobile-api`. O módulo `admin-api` e as suas configurações não são alterados por estas instruções.

## Variáveis necessárias

A Mobile API lê a configuração do ambiente do processo Java. Configure estas variáveis no terminal ou na configuração de execução da sua IDE:

| Variável | Valor |
|---|---|
| `DATABASE_URL` | JDBC URL do Supabase com `sslmode=require` |
| `DATABASE_USERNAME` | Usuário PostgreSQL do Supabase |
| `DATABASE_PASSWORD` | Senha PostgreSQL do Supabase |
| `CONECTA_API_SALT` | Mesmo salt usado pela Admin API |
| `CONECTA_JWT_SECRET` | Segredo exclusivo da Mobile API, com pelo menos 32 caracteres |
| `CONECTA_MOBILE_CORS_ORIGINS` | Origens locais permitidas no navegador |

O valor padrão de CORS já inclui:

```text
http://localhost:5000
http://127.0.0.1:5000
```

Se iniciar o Flutter em outra porta, configure `CONECTA_MOBILE_CORS_ORIGINS` com as origens exatas, separadas por vírgula.

**Atenção:** o `CONECTA_API_SALT` deve ser exatamente o mesmo valor da Admin API, pois os dois módulos consultam a mesma tabela `usuarios`. O `CONECTA_JWT_SECRET` é independente e não deve ser compartilhado com o Admin.

Arquivos `.env` não são carregados automaticamente pelo Spring Boot neste projeto. Para desenvolvimento, exporte as variáveis no terminal ou configure-as em *Run/Debug Configurations* na IDE. Não versione senhas nem segredos.

### Exemplo no PowerShell

Preencha os valores com as credenciais locais do projeto:

```powershell
$env:DATABASE_URL = "jdbc:postgresql://SEU_HOST.pooler.supabase.com:6543/postgres?prepareThreshold=0&sslmode=require"
$env:DATABASE_USERNAME = "postgres.SE_REF_DO_PROJETO"
$env:DATABASE_PASSWORD = "SUA_SENHA"
$env:CONECTA_API_SALT = "O_MESMO_SALT_DA_ADMIN_API"
$env:CONECTA_JWT_SECRET = "UMA_CHAVE_ALEATORIA_LONGA_E_EXCLUSIVA_PARA_MOBILE"
$env:CONECTA_MOBILE_CORS_ORIGINS = "http://localhost:5000,http://127.0.0.1:5000"
```

### Exemplo no Bash

```bash
export DATABASE_URL='jdbc:postgresql://SEU_HOST.pooler.supabase.com:6543/postgres?prepareThreshold=0&sslmode=require'
export DATABASE_USERNAME='postgres.SE_REF_DO_PROJETO'
export DATABASE_PASSWORD='SUA_SENHA'
export CONECTA_API_SALT='O_MESMO_SALT_DA_ADMIN_API'
export CONECTA_JWT_SECRET='UMA_CHAVE_ALEATORIA_LONGA_E_EXCLUSIVA_PARA_MOBILE'
export CONECTA_MOBILE_CORS_ORIGINS='http://localhost:5000,http://127.0.0.1:5000'
```

## Executar a Mobile API

Na raiz do repositório backend, com Java 21 e as variáveis configuradas:

```bash
./gradlew :mobile-api:bootRun
```

No Windows:

```powershell
.\gradlew.bat :mobile-api:bootRun
```

A API escuta na porta `8081`. O banco Supabase deve estar acessível pela máquina local.

## Conferir o CORS

O valor padrão está em `mobile-api/src/main/resources/application.properties`, na propriedade:

```properties
app.security.allowed-origins=${CONECTA_MOBILE_CORS_ORIGINS:http://localhost:5000,http://127.0.0.1:5000}
```

A classe `mobile-api/src/main/java/br/edu/ifsp/conectaavida/mobile/config/MobileSecurityConfig.java` lê essa propriedade e configura as origens, métodos e cabeçalhos permitidos no Spring Security.

Com a API ligada, teste o preflight no PowerShell ou terminal:

```bash
curl -i -X OPTIONS http://localhost:8081/api/auth/login \
  -H "Origin: http://localhost:5000" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: content-type"
```

A resposta deve conter `Access-Control-Allow-Origin: http://localhost:5000` e permitir o método solicitado. Também é possível conferir a requisição `OPTIONS` na aba **Network/Rede** das ferramentas de desenvolvedor do Chrome.

## Endpoints disponíveis para o aplicativo

- `POST /api/auth/register`: cria cidadão com permissão fixa de usuário comum.
- `POST /api/auth/login`: autentica e retorna JWT com validade de 12 horas.
- `GET /api/comunicacoes`: lista comunicações para o feed.
- `GET /api/usuarios/email/{email}`, `PUT /api/usuarios/{id}` e `DELETE /api/usuarios/{id}`: leitura/edição/exclusão apenas do próprio perfil autenticado.
- `POST /api/campanhas/{id}/inscrever`, `DELETE /api/campanhas/{id}/desinscrever` e `GET /api/campanhas/{id}/inscritos`: inscrições limitadas ao usuário do JWT.
- `POST /api/mobile/notificacoes/registrar`: registro do token de push.

As rotas do perfil e das inscrições exigem `Authorization: Bearer <token>`.

## Funcionalidades pendentes

- A recuperação de senha retorna HTTP 501 porque o envio e a validação de e-mail ainda não estão configurados na Mobile API.
- O registro dos tokens de push usa armazenamento em memória e não é persistente após reiniciar a API.
- O envio real de notificações push precisa ser validado separadamente.


## Sistema de dependentes (em desenvolvimento)

Os dependentes não são contas de acesso e não são gravados na tabela `usuarios`. Eles ficam em `public.dependentes`, vinculados ao titular por `usuario_titular_id`. Não é solicitado CPF, e-mail nem senha do dependente.

### Funcionalidades e endpoints da Mobile API

Com um JWT do titular:

- `GET /api/usuarios/{usuarioId}/dependentes`: lista dependentes ativos do titular.
- `GET /api/usuarios/{usuarioId}/dependentes/{dependenteId}`: consulta um dependente ativo do titular.
- `POST /api/usuarios/{usuarioId}/dependentes`: cadastra nome, data de nascimento, parentesco e sexo opcional.
- `PUT /api/usuarios/{usuarioId}/dependentes/{dependenteId}`: atualiza os dados.
- `DELETE /api/usuarios/{usuarioId}/dependentes/{dependenteId}`: inativa o dependente sem apagar seu histórico de inscrições.

O campo `parentesco` recebe `Pai`, `Mãe`, `Cônjuge`, `Tutelado` ou outro texto informado pelo usuário. O backend valida o titular pelo JWT; o ID do titular no caminho deve corresponder ao usuário autenticado.

Para inscrever um participante:

```http
POST /api/campanhas/{comunicacaoId}/inscrever
Authorization: Bearer <token>
Content-Type: application/json

{ "dependente_id": 123 }
```

Para inscrever o próprio titular, envie `{}` ou omita `dependente_id`. Para cancelar inscrição de dependente:

```http
DELETE /api/campanhas/{comunicacaoId}/desinscrever?dependente_id=123
Authorization: Bearer <token>
```

O endpoint `GET /api/campanhas/minhas` retorna também `dependente_id`, `participante_nome` e `parentesco`, permitindo que o Flutter identifique quem participará. A inscrição do titular continua usando `dependente_id: null`.

### Banco de dados / Supabase

A migração versionada está em:

`core/src/main/resources/db/migration/V2__dependentes_and_campaign_participants.sql`

Como `spring.flyway.enabled=true` e o módulo `core` faz parte do classpath das APIs, a migração é aplicada pelo Flyway quando uma versão contendo esse arquivo é iniciada. A migração cria a tabela de dependentes, adiciona `dependente_id` a `usuarios_campanhas` e substitui a unicidade antiga por índices parciais que permitem várias pessoas do mesmo titular na mesma campanha.

**Aplicação manual no Supabase (opcional):**

1. Abra o projeto Supabase correto.
2. Entre em **SQL Editor** e crie uma nova consulta.
3. Copie o conteúdo completo do arquivo SQL acima.
4. Execute e confira se `dependentes`, `dependente_id` e os índices foram criados.
5. Não execute SQL diferente da migração versionada. O script usa `IF NOT EXISTS` para tolerar que a mesma estrutura já tenha sido criada manualmente antes de o Flyway executar a versão V2.

Antes de produção, confira o histórico em `flyway_schema_history` e faça backup do banco. Não altere `usuarios`, não apague inscrições antigas e não execute a migração em um banco diferente do ambiente que será usado pelo serviço.
