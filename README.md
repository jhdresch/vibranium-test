# Vibranium Saga Orquestrada

Este projeto implementa um fluxo de **compra e venda** usando **microsserviços em Java 17 / Spring Boot** e comunicação assíncrona via **Kafka**, seguindo o padrão de arquitetura **SAGA** para orquestração de transações distribuídas.

A infraestrutura (Kafka, MySQL, Elasticsearch, etc.) sobe em Docker, mas os serviços Java podem ser compilados e executados **localmente** com Maven + Java 17.

---

## ✅ Requisitos

### Infraestrutura (Docker)

Para subir apenas a **infra**:

- Docker instalado e em execução  
- Docker Compose disponível (`docker compose` funcional)  

Na raiz do projeto:

```bash
docker compose -f docker-compose.infra.yml up -d
```

Isso sobe:

- `zookeeper`
- `kafka`
- `mysql-db`
- `elasticsearch`
- `kibana`
- Rede `saga-net`

### Serviços Java (localmente)

Para rodar os microsserviços localmente:

- **Java 17 (JDK 17)**  
  ```bash
  java -version
  ```
- **Maven 3.8+ / 3.9+**  
  ```bash
  mvn -version
  ```

---

## 🧱 Visão da Arquitetura

- **Microserviços principais**:
  - `sale-service` → expõe a API HTTP de compra/venda
  - `inventory-service`
  - `payment-service`
  - `orchestrator-service`

- **Comunicação entre serviços**:
  - Realizada via **Kafka** (tópicos)
  - Os eventos de venda, pagamento e estoque são propagados entre os serviços via mensagens
  - O `orchestrator-service` coordena o fluxo das etapas, implementando o padrão **SAGA Orquestrada**

- **Ponto importante**:  
  **Existe apenas uma API HTTP pública exposta para compra/venda**, e ela está no **`sale-service`**.  
  Todo o restante do fluxo (pagamento, reserva de estoque, compensações, cancelamentos) acontece via eventos no Kafka, entre os microserviços.

---

## 🌐 API de Compra/Venda (único endpoint HTTP)

Toda a interação de “compra” e “venda” é feita através da mesma API HTTP, exposta pelo **`sale-service`**.

### Serviço responsável

- **Microserviço:** `sale-service`
- **Base URL local:** `http://localhost:8081`
- **Contexto da API:** `/api/v1/sales`

Os outros serviços **não expõem endpoints HTTP para esse fluxo** – eles participam apenas por eventos Kafka dentro da arquitetura SAGA.

---

## 🛒 Criar uma Compra (REQUISIÇÃO POST)

Para **realizar uma compra**, faça uma requisição HTTP `POST` para o `sale-service`.

### Endpoint

```http
POST http://localhost:8081/api/v1/sales
Content-Type: application/json
```

### Exemplo de requisição no Postman (compra)

No Postman:

1. Crie uma nova request `POST`.
2. URL: `http://localhost:8081/api/v1/sales`
3. Aba **Headers**:
   - `Content-Type: application/json`
4. Aba **Body**:
   - Selecione `raw`
   - Selecione `JSON`
   - Corpo:

```json
{
  "userId": 2,
  "sellerId": 4,
  "offerId": 4,
  "productId": 1,
  "quantity": 10,
  "value": 250.00,
  "type": 1
}
```

### O que esses campos representam

- `userId`: ID do comprador
- `sellerId`: ID do vendedor
- `offerId`: alguma oferta/cotação associada à venda
- `productId`: ID do produto
- `quantity`: quantidade comprada
- `value`: valor total da operação
- `type`: tipo da operação (por exemplo, `1` representando “compra” – conforme seu domínio)

Depois de enviar essa requisição:

1. O `sale-service` recebe os dados.
2. Registra a intenção de venda/compra.
3. Dispara **eventos no Kafka** (como “SALE_CREATED”) para os demais serviços.
4. A partir daí:
   - `inventory-service` verifica/atualiza estoque.
   - `payment-service` tenta processar o pagamento.
   - `orchestrator-service` coordena o fluxo e aplica a SAGA:
     - Se tudo der certo → confirma a venda.
     - Se algo falhar → dispara eventos de compensação (cancelar pagamento, liberar estoque, cancelar venda, etc.).

Toda essa segunda parte é 100% **assíncrona e baseada em eventos Kafka** – você não precisa chamar manually outros endpoints HTTP.

---

## 💡 E a “venda” (o outro lado)?

Na sua arquitetura, **compra e venda** são tratados pelo mesmo endpoint `/api/v1/sales`.  
A distinção é feita:

- Pelo **payload** (`type`, `value`, etc.).
- Pela lógica de domínio dentro do `sale-service` e dos demais serviços.

Ou seja:

- Você **não tem uma segunda API HTTP separada** para venda.
- A **única API HTTP pública é a de `sale-service`**, e a partir dela a arquitetura SAGA cuida do resto via Kafka.

Se você quiser representar “venda” (em outro sentido de operação) via API, geralmente será com outro valor de `type` ou outro endpoint dentro do `sale-service` (por exemplo, `/api/v1/sales/sell`), mas a lógica continua dentro do mesmo microserviço.

---

## ⚙️ Build e Execução Local (Java 17 + Maven)

### 1. Build de todos os serviços

Na raiz do projeto:

```bash
mvn -q -DskipTests clean package
```

Ou, por serviço:

```bash
cd sale-service
mvn -q -DskipTests clean package

cd ../inventory-service
mvn -q -DskipTests clean package

cd ../payment-service
mvn -q -DskipTests clean package

cd ../orchestrator-service
mvn -q -DskipTests clean package
```

### 2. Rodar cada serviço com Java 17

Exemplo para `sale-service`:

```bash
cd sale-service
java -jar target/sale-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

Para os demais:

```bash
cd ../inventory-service
java -jar target/inventory-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=local

cd ../payment-service
java -jar target/payment-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=local

cd ../orchestrator-service
java -jar target/orchestrator-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

Certifique-se de que o profile `local` (ou equivalente) use:

- `jdbc:mysql://localhost:3306/...`
- `spring.kafka.bootstrap-servers=localhost:9092`
- `elasticsearch: http://localhost:9200`

---

## 🔁 Fluxo resumido da SAGA

1. Cliente chama **apenas a API do `sale-service`** (`POST /api/v1/sales`).
2. `sale-service` publica evento no **Kafka**.
3. `orchestrator-service` e outros serviços (`inventory-service`, `payment-service`) reagem aos eventos.
4. Em caso de falha em alguma etapa:
   - A SAGA dispara **eventos de compensação** (ex.: cancelar pagamento, estornar estoque, cancelar venda).
5. Em caso de sucesso em todas as etapas:
   - A venda é concluída e marcada como finalizada.

Toda a coordenação é feita por mensagens no Kafka; a API HTTP é **única e centralizada** em `sale-service`.

---

## 🧾 Resumo rápido

- Subir infra:

  ```bash
  docker compose -f docker-compose.infra.yml up -d
  ```

- Buildar serviços localmente:

  ```bash
  mvn -q -DskipTests clean package
  ```

- Rodar `sale-service` (Java 17):

  ```bash
  cd sale-service
  java -jar target/sale-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
  ```

- Chamar **única API de compra/venda** (Postman):

  ```http
  POST http://localhost:8081/api/v1/sales
  Content-Type: application/json
  ```

  Body (JSON):

  ```json
  {
    "userId": 2,
    "sellerId": 4,
    "offerId": 4,
    "productId": 1,
    "quantity": 10,
    "value": 250.00,
    "type": 1
  }
  ```

- Todo o resto da orquestração é feito internamente via Kafka, usando o padrão **SAGA**.