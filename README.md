# Vibranium Saga Orquestrada

Este projeto implementa um fluxo de **compra e venda** usando **microsserviços em Java 17 / Spring Boot** e comunicação assíncrona via **Kafka**, seguindo o padrão de arquitetura **SAGA Orquestrada** para transações distribuídas.

Toda a stack (infra + apps) pode ser executada via **Docker Compose**:

```bash
docker compose -f docker-compose.infra.yml up -d   # Infraestrutura
docker compose -f docker-compose.app.yml   up -d   # Aplicações (microsserviços)
```

Você também pode rodar os serviços manualmente com Maven/Java, se preferir.

---

## ✅ Requisitos

### Infraestrutura (Docker)

- Docker instalado e em execução  
- Docker Compose disponível (`docker compose` funcional)

Na raiz do projeto:

```bash
docker compose -f docker-compose.infra.yml up -d
```

Isso sobe:

- `zookeeper` – coordenação do Kafka  
- `kafka` – broker de eventos para a SAGA  
- `mysql-db` – banco relacional dos serviços  
- `elasticsearch` – armazenamento de logs estruturados  
- `kibana` – visualização de logs  
- `prometheus` – métricas  
- `grafana` – dashboards de métricas  
- `jaeger` – tracing distribuído (OTLP)  
- Rede Docker `saga-net`

### Serviços Java (quando rodar manualmente)

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

### Microsserviços principais

- `sale-service`  
  - Exposição da **API HTTP de compra/venda**  
  - Publica eventos no Kafka para iniciar o fluxo da SAGA

- `inventory-service`  
  - Responsável por **baixa/compensação de estoque**  
  - Consome eventos do Kafka (venda criada, cancelada, etc.)  
  - Persiste o estado de estoque relacionado à venda

- `payment-service`  
  - Responsável por **processar pagamentos** e, se necessário, estornar  
  - Consome eventos do Kafka e publica eventos de sucesso/falha  
  - Persiste transações de pagamento

- `orchestrator-service`  
  - É o **orquestrador da SAGA**  
  - Decide a próxima etapa (payment → inventory → confirmação, etc.)  
  - Dispara eventos de compensação em caso de falha (cancelar pagamento, liberar estoque, cancelar venda)

### Comunicação entre serviços

- Realizada via **Kafka** (tópicos específicos para cada evento da SAGA).
- O `orchestrator-service` coordena o fluxo, implementando o padrão **SAGA Orquestrada**:
  - Encadeia os passos (validação, pagamento, estoque…)
  - Garante compensações em caso de erro (rollback “por eventos”).

### Única API HTTP pública

**Toda a interação HTTP de compra/venda acontece apenas via `sale-service`.**

- Base URL: `http://localhost:8081`
- Contexto da API: `/api/v1/sales`

Os demais serviços (`inventory-service`, `payment-service`, `orchestrator-service`) trabalham apenas via **eventos Kafka**, sem expor endpoints HTTP para o fluxo da SAGA.

---

## 🧩 Infraestrutura no `docker-compose.infra.yml`

A infra é definida com serviços como:

```yaml
services:

  # ===============================
  # ZOOKEEPER
  # ===============================
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    container_name: zookeeper
    ports:
      - "2181:2181"
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    healthcheck:
      test: ["CMD", "bash", "-c", "echo ruok | nc localhost 2181"]
      interval: 10s
      timeout: 5s
      retries: 10
    networks:
      - saga-net

  # ===============================
  # KAFKA
  # ===============================
  kafka:
    image: confluentinc/cp-kafka:7.5.0
    container_name: kafka
    hostname: kafka
    ports:
      - "9092:9092"
      - "19092:19092"
    depends_on:
      zookeeper:
        condition: service_healthy
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181

      # Listener interno para containers (saga-net) e externo para localhost
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT
      KAFKA_LISTENERS: INTERNAL://kafka:19092,EXTERNAL://0.0.0.0:9092
      KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka:19092,EXTERNAL://localhost:9092
      KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL

      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    healthcheck:
      test: ["CMD", "bash", "-c", "nc -z localhost 9092"]
      interval: 10s
      timeout: 5s
      retries: 10
    networks:
      - saga-net

  # ===============================
  # MYSQL
  # ===============================
  db:
    image: mysql:8.0
    container_name: mysql-db
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: sales123
    volumes:
      - ./docker/mysql/init:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD", "mysqladmin","ping","-h","localhost","-psales123"]
      interval: 10s
      timeout: 5s
      retries: 10
    networks:
      - saga-net

  # ===============================
  # ELASTICSEARCH
  # ===============================
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    container_name: elasticsearch
    environment:
      discovery.type: single-node
      xpack.security.enabled: "false"
      ES_JAVA_OPTS: "-Xms512m -Xmx512m"
    ports:
      - "9200:9200"
    volumes:
      - es-data:/usr/share/elasticsearch/data
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9200"]
    networks:
      - saga-net

  # ===============================
  # KIBANA
  # ===============================
  kibana:
    image: docker.elastic.co/kibana/kibana:8.11.0
    container_name: kibana
    depends_on:
      elasticsearch:
        condition: service_healthy
    environment:
      ELASTICSEARCH_HOSTS: http://elasticsearch:9200
    ports:
      - "5601:5601"
    networks:
      - saga-net

  # ===============================
  # PROMETHEUS
  # ===============================
  prometheus:
    image: prom/prometheus:latest
    container_name: prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./docker/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
    depends_on:
      - kafka
    networks:
      - saga-net

  # ===============================
  # GRAFANA
  # ===============================
  grafana:
    image: grafana/grafana:latest
    container_name: grafana
    ports:
      - "3000:3000"
    environment:
      GF_SECURITY_ADMIN_USER: admin
      GF_SECURITY_ADMIN_PASSWORD: admin
    depends_on:
      - prometheus
    networks:
      - saga-net

  # ===============================
  # JAEGER (OTLP ENABLED)
  # ===============================
  jaeger:
    image: jaegertracing/all-in-one:1.51
    container_name: jaeger
    environment:
      COLLECTOR_OTLP_ENABLED: "true"
    command:
      - "--collector.otlp.enabled=true"
    ports:
      - "16686:16686"
      - "4317:4317"
      - "4318:4318"
    networks:
      - saga-net

volumes:
  es-data:

networks:
  saga-net:
    name: saga-net
    driver: bridge
```

### O que cada peça faz rapidamente

- **Zookeeper + Kafka**: backbone de eventos da SAGA. Todos os microserviços se comunicam via tópicos Kafka.
- **MySQL (`mysql-db`)**: armazena dados de vendas, pagamentos, estoque (e outros domínios).
- **Elasticsearch + Kibana**: guardam e exibem logs e eventos da aplicação.
- **Prometheus + Grafana**: coletam métricas (via Actuator / Micrometer) e exibem dashboards.
- **Jaeger**: recebe spans via OTLP e permite **tracing distribuído** entre os microsserviços.

---

## 🚀 Subindo tudo com Docker

### 1. Subir a infra primeiro

Na raiz do projeto:

```bash
docker compose -f docker-compose.infra.yml up -d
```

Verifique se está tudo saudável:

```bash
docker ps
```

### 2. Subir os microsserviços

Em seguida:

```bash
docker compose -f docker-compose.app.yml up -d
```

No `docker-compose.app.yml` você terá serviços como:

- `sale-service`
- `inventory-service`
- `payment-service`
- `orchestrator-service`

Cada um configurado para:

- Usar `SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:19092`
- Usar `SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/...`
- Enviar traces para `jaeger:4318`

---

## 🌐 API de Compra/Venda (único endpoint HTTP)

**Serviço responsável:** `sale-service`  
**Base URL (quando rodando local ou em container mapeado):** `http://localhost:8081`  
**Contexto:** `/api/v1/sales`

### Criar uma compra

```http
POST http://localhost:8081/api/v1/sales
Content-Type: application/json
```

### Exemplo de corpo (Postman / HTTP)

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

**Campos:**

- `userId`: ID do comprador
- `sellerId`: ID do vendedor
- `offerId`: ID da oferta/cotação
- `productId`: ID do produto
- `quantity`: quantidade
- `value`: valor total
- `type`: tipo de operação (ex.: `1` = compra)

Após o `POST`:

1. `sale-service` registra a venda e publica um evento no Kafka (ex.: `SALE_CREATED`).
2. `orchestrator-service` consome esse evento e controla o fluxo:
   - chama (via evento) `payment-service`  
   - depois `inventory-service`  
   - e publica eventos de sucesso/falha.
3. Em caso de falha, o orquestrador dispara **eventos de compensação** para reverter o que já foi feito.

---

## ⚙️ Build e Execução Local (sem Docker para apps)

Se quiser rodar os serviços manualmente (após subir só a infra com Docker):

### 1. Build de todos os serviços

Na raiz:

```bash
mvn -q -DskipTests clean package
```

Ou por serviço:

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

### 2. Rodar cada serviço

```bash
cd sale-service
java -jar target/sale-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=local

cd ../inventory-service
java -jar target/inventory-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=local

cd ../payment-service
java -jar target/payment-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=local

cd ../orchestrator-service
java -jar target/orchestrator-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

O profile `local` deve usar:

- `spring.kafka.bootstrap-servers=localhost:9092`
- `spring.datasource.url=jdbc:mysql://localhost:3306/...`
- OTEL/Jaeger apontando para `http://localhost:4318` (se Jaeger estiver via Docker).

---

## 🔁 Fluxo resumido da SAGA

1. Cliente faz `POST /api/v1/sales` no `sale-service`.
2. `sale-service` publica evento de criação da venda no Kafka.
3. `orchestrator-service` consome o evento, decide o próximo passo e envia comandos (eventos) para:
   - `payment-service` (processar pagamento)
   - `inventory-service` (baixar estoque)
4. Em caso de erro:
   - `orchestrator-service` publica eventos de **compensação** (cancelar pagamento, repor estoque, cancelar venda).
5. Em caso de sucesso em todas as etapas:
   - A venda é marcada como concluída e o estado final é persistido.

Tudo isso é monitorável via:

- **Kibana** (logs)
- **Grafana** (métricas via Prometheus)
- **Jaeger** (traces distribuídos da SAGA)

---

## 🧾 Resumo rápido de comandos

- Subir **infra**:

  ```bash
  docker compose -f docker-compose.infra.yml up -d
  ```

- Subir **apps** (microsserviços):

  ```bash
  docker compose -f docker-compose.app.yml up -d
  ```

- Criar uma venda/compra:

  ```http
  POST http://localhost:8081/api/v1/sales
  Content-Type: application/json
  ```

  Body:

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

A partir daí, a **SAGA orquestrada** faz todo o trabalho pesado via Kafka entre `sale-service`, `payment-service`, `inventory-service` e `orchestrator-service`.