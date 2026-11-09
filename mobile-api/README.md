# Mobile API — execução e publicação

Este guia cobre somente o módulo `mobile-api`. O código e as configurações de `admin-api` não fazem parte deste deploy.

## Variáveis de ambiente

Crie um arquivo local `.env` (não versionar) com:

```dotenv
DATABASE_URL=jdbc:postgresql://SEU_HOST.pooler.supabase.com:6543/postgres?prepareThreshold=0&sslmode=require
DATABASE_USERNAME=postgres.SE_REF_DO_PROJETO
DATABASE_PASSWORD=SENHA_DO_SUPABASE
CONECTA_API_SALT=MESMO_SALT_CONFIGURADO_NA_ADMIN_API
CONECTA_JWT_SECRET=GERE_UMA_CHAVE_LONGA_E_ALEATORIA_COM_PELO_MENOS_32_CARACTERES
CONECTA_MOBILE_CORS_ORIGINS=http://localhost:5000,http://127.0.0.1:5000
```

**Importante:** `CONECTA_API_SALT` precisa ser o mesmo valor usado pela Admin API porque ambas consultam a mesma tabela `usuarios`. Não gere outro salt só para o mobile, pois as senhas existentes deixariam de validar entre os dois serviços. O segredo JWT é independente e exclusivo do Mobile API.

A URL JDBC deve usar SSL (`sslmode=require`). Não use a chave `service_role` do Supabase no Flutter ou no navegador; o app acessa o Postgres exclusivamente pelo backend.

## Executar localmente em Docker

Na raiz do repositório:

```bash
docker build -f mobile-api/Dockerfile -t conecta-vida-mobile-api .
docker run --rm --env-file .env -p 8081:8081 connects-vida-mobile-api
```

O container usa a porta definida pela variável `PORT` quando fornecida; localmente, o padrão é `8081`.

## Publicar no Render

O arquivo `render.yaml` cria apenas o serviço `conecta-vida-mobile-api`, construído por `mobile-api/Dockerfile`. Ele não constrói nem publica o módulo administrativo.

1. No Render, escolha **New → Blueprint** e conecte este repositório na branch `mobile`.
2. No formulário inicial, informe `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `CONECTA_API_SALT` e `CONECTA_MOBILE_CORS_ORIGINS`.
3. O Render gera `CONECTA_JWT_SECRET` automaticamente pelo Blueprint.
4. Configure `CONECTA_MOBILE_CORS_ORIGINS` com a origem exata do site Flutter Web publicado e, durante desenvolvimento, mantenha `http://localhost:5000` e `http://127.0.0.1:5000`.
5. Após o primeiro deploy, valide `https://SEU-SERVICO.onrender.com/actuator/health`.

O plano gratuito do Render pode suspender serviços sem tráfego e provocar demora na primeira chamada após inatividade. Para uma API sempre disponível, escolha um plano pago.

## Endpoints implementados para o aplicativo

- `POST /api/auth/register`: cria cidadão com permissão fixa de usuário comum.
- `POST /api/auth/login`: autentica e retorna JWT com validade de 12 horas.
- `GET /api/comunicacoes`: lista as comunicações para o feed.
- `GET /api/usuarios/email/{email}`, `PUT /api/usuarios/{id}` e `DELETE /api/usuarios/{id}`: leitura/edição/exclusão apenas do próprio perfil autenticado.
- `POST /api/campanhas/{id}/inscrever`, `DELETE /api/campanhas/{id}/desinscrever` e `GET /api/campanhas/{id}/inscritos`: inscrições limitadas ao usuário do JWT.
- `POST /api/mobile/notificacoes/registrar`: registro do token de push.

Rotas do perfil e das inscrições exigem `Authorization: Bearer <token>`. O endpoint de feed é público.

## Funcionalidades ainda pendentes

- A recuperação de senha retorna HTTP 501 porque o fluxo de envio e validação de e-mail ainda não existe para a API móvel.
- O registro de tokens de push usa o registro em memória existente no projeto; não é uma fila/armazenamento durável e o disparo real de push deve ser validado separadamente.
- A chave `CONECTA_API_SALT` precisa permanecer compartilhada com a Admin API. Planeje qualquer rotação junto com a atualização dos dois serviços.
