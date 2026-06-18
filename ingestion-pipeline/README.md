# Ingestion Pipeline - Expense Embedding Service

## Overview

The Ingestion Pipeline is a microservice that consumes expense events from Kafka, generates semantic embeddings using Ollama, stores them in PostgreSQL with pgvector support, and triggers analytics recalculation events.

**Purpose**: Enable AI-driven semantic search and analytics over expense data through vector embeddings.

## Architecture

### Component Stack

```
┌─────────────────────┐
│  Expense-Command    │ Creates expenses
└──────────┬──────────┘
           │ publishes
           ▼
    ┌────────────────┐
    │ Kafka Topic:   │
    │ expense-events │
    └────────┬───────┘
             │ consumes
             ▼
    ┌──────────────────────────┐
    │ Ingestion Pipeline       │
    │  - Event Consumer        │
    │  - Embedding Generator   │
    │  - Vector Storage        │
    │  - Event Publisher       │
    └────────┬─────────────────┘
             │ publishes
             ▼
    ┌─────────────────────────┐
    │ analytics-recalculation │
    │ (Out of scope)          │
    └─────────────────────────┘
```

### Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.2.0
- **Message Queue**: Apache Kafka
- **Vector Database**: PostgreSQL + pgvector
- **Embedding Model**: Ollama (nomic-embed-text)
- **Resilience**: Resilience4j (retry, circuit breaker)

## Key Features

### 1. Industry Best Practices for Embeddings

#### Semantic Text Construction
- Combines multiple expense dimensions:
  - Store information
  - Payment method
  - Item names
  - Categories and subcategories
  - Purchase amounts and dates
- Provides rich context for semantic similarity search
- Example:
  ```
  Store: Target. Payment method: Credit Card. Purchased items: 
  Groceries (Food) $50.00, Electronics (Tech) $100.00. Purchase date: 2026-05-07.
  ```

#### Embedding Model Selection
- **Model**: nomic-embed-text
- **Dimensions**: 768
- **Characteristics**:
  - Lightweight and efficient
  - Multilingual support
  - Good performance/cost ratio
  - Well-suited for expense domain
  - Can be updated by simply changing configuration

#### Vector Storage
- **pgvector Extension**: Native PostgreSQL vector type
- **Index Type**: HNSW (Hierarchical Navigable Small World)
- **Distance Metric**: Cosine similarity
- **Performance**: O(log n) similarity search even at scale

### 2. Error Handling & Resilience

#### Retry Strategy
- Exponential backoff: 1s, 2s, 4s delays
- Max 3 attempts per message
- Transient errors (network, timeouts) are retried
- Permanent errors (validation) fail fast

#### Dead Letter Topic (DLT)
```
Messages that fail all retries → expense-events-dlt
```
- Manual investigation required
- Future integration: create UI for dead letter queue management

#### Idempotency
- Duplicate event detection based on transaction ID
- Safe to replay messages
- Prevents duplicate embeddings

### 3. Semantic Search Capabilities

#### Vector Similarity Search
```sql
SELECT e.* FROM expense_management.expense_embeddings e
WHERE e.user_id = :userId
ORDER BY e.embedding_vector <=> :queryVector
LIMIT :limit;
```

#### Future AI Integrations
- **Semantic Search**: "Find similar expenses"
- **Anomaly Detection**: Identify unusual spending patterns
- **Smart Categorization**: Auto-categorize based on embeddings
- **Expenditure Insights**: Generate contextual spending insights

## Usage

### Running Locally

```bash
# Start Ollama with embedding model
ollama pull nomic-embed-text
ollama serve

# In another terminal, start the full stack
docker-compose up

# Application will be available at
http://localhost:8085/api/ingestion-pipeline/health
```

### Configuration

#### application.yml (Local)
```yaml
embedding:
  ollama:
    base-url: http://localhost:11434
  model: nomic-embed-text
  timeout-seconds: 30

spring:
  kafka:
    bootstrap-servers: localhost:9092
  datasource:
    url: jdbc:postgresql://localhost:5432/expense_db
```

#### Docker Environment Variables
```bash
EMBEDDING_OLLAMA_BASE_URL=http://ollama:11434
EMBEDDING_MODEL=nomic-embed-text
EMBEDDING_TIMEOUT_SECONDS=30
```

## API Endpoints

### Health Check
```bash
GET /api/ingestion-pipeline/health
```

Response:
```json
{
  "status": "UP",
  "service": "ingestion-pipeline",
  "embedding_service_healthy": true
}
```

### Metrics
```bash
GET /api/ingestion-pipeline/metrics
```

Response:
```json
{
  "embedding_metrics": {
    "embedding_generation_ms": 1500,
    "embedding_count": 42,
    "embedding_errors": 0
  },
  "embedding_model": "nomic-embed-text"
}
```

### Reset Metrics
```bash
GET /api/ingestion-pipeline/metrics/reset
```

## Database Schema

### expense_embeddings Table
```sql
CREATE TABLE expense_management.expense_embeddings (
    embedding_id UUID PRIMARY KEY,
    transaction_id VARCHAR(255) NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    expense_date DATE NOT NULL,
    store_name VARCHAR(255),
    category VARCHAR(100),
    amount DECIMAL(19, 2),
    embedding_text TEXT NOT NULL,
    embedding_vector vector NOT NULL,  -- pgvector type
    embedding_model VARCHAR(100) DEFAULT 'ollama-nomic-embed-text',
    embedding_generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_expense_embeddings_user_id ON expense_management.expense_embeddings(user_id);
CREATE INDEX idx_expense_embeddings_category ON expense_management.expense_embeddings(category);
CREATE INDEX idx_expense_embeddings_vector_hnsw 
    ON expense_management.expense_embeddings USING hnsw (embedding_vector vector_cosine_ops);
```

## Event Flow

### Input Event: ExpenseCreatedEvent
```json
{
  "transactionId": "txn-12345",
  "date": "2026-05-07",
  "requestBody": "{\"userId\": \"uuid-xyz\", \"store\": \"Target\", \"items\": [...], \"purchaseDate\": \"2026-05-07\"}"
}
```

### Processing Steps
1. **Validation**: Check event format and completeness
2. **Idempotency**: Verify not already processed
3. **Parsing**: Extract CreateExpenseRequest from requestBody
4. **Embedding Text Construction**: Build semantic representation
5. **Embedding Generation**: Call Ollama API
6. **Vector Storage**: Save to pgvector
7. **Event Publication**: Emit AnalyticsRecalculationEvent

### Output Event: AnalyticsRecalculationEvent
```json
{
  "eventId": "evt-789",
  "transactionId": "txn-12345",
  "userId": "uuid-xyz",
  "embeddingId": "emb-456",
  "eventType": "EXPENSE_EMBEDDED",
  "timestamp": "2026-05-07T15:30:00",
  "source": "ingestion-pipeline"
}
```

## Monitoring

### Logging
```
DEBUG: Embedding text construction and vector dimensions
INFO: Successful embedding generation and storage
WARN: Duplicate events, missing data
ERROR: Embedding failures, database errors, Ollama unavailability
```

### Metrics (via Actuator)
```bash
GET /actuator/metrics
GET /actuator/health
GET /actuator/prometheus
```

### Health Check
Ollama connectivity is verified on:
- Application startup
- `/api/ingestion-pipeline/health` endpoint
- Periodic health checks (configurable)

## Performance Characteristics

### Throughput
- Single instance: ~100-200 embeddings/minute (depends on Ollama)
- Scaling: Horizontal scaling via Kafka consumer groups
- Bottleneck: Ollama embedding generation time (~500-1000ms per embedding)

### Storage
- Single embedding: ~1KB (768 floats * 4 bytes + metadata)
- 10,000 expenses: ~10MB
- HNSW index overhead: ~25-30%

### Latency
- Total end-to-end: 1-2 seconds per expense
  - Event consumption: <10ms
  - Embedding generation: 500-1000ms
  - Database storage: 50-100ms
  - Event publication: 50-100ms

## Future Enhancements

### Short-term
- [ ] Batch embedding generation (reduce API calls)
- [ ] Vector embedding model versioning
- [ ] Dead letter queue UI for manual replay
- [ ] Embedding cache for frequently accessed vectors
- [ ] Custom embedding fine-tuning on expense domain

### Medium-term
- [ ] Real-time similarity search API
- [ ] Streaming aggregation of embeddings
- [ ] Time-series analysis of embedding clusters
- [ ] Automatic expense categorization using embeddings
- [ ] Anomaly detection for unusual spending

### Long-term
- [ ] Multi-modal embeddings (receipts, descriptions)
- [ ] Federated learning for privacy-preserving analytics
- [ ] Custom embedding model training
- [ ] Integration with other ML pipelines

## Troubleshooting

### Ollama Connection Issues
```
Error: Failed to initialize embedding model
Solution: Ensure Ollama is running at http://localhost:11434
Check: curl http://localhost:11434/api/tags
```

### Out of Memory
```
Error: Java heap space
Solution: Increase JVM memory
JVM_OPTS: -Xmx2G
```

### Slow Embedding Generation
```
Check: 
- Ollama CPU/Memory usage
- Network latency to Ollama
- pgvector index status
- Embedding model size
```

### Messages in DLT
```
Investigate:
- Check application logs for error details
- Verify event format
- Check Ollama availability
- Check database connectivity
```

## Testing

### Run Tests
```bash
mvn clean test
```

### Integration Tests
```bash
mvn verify -P integration-tests
```

### Load Testing
```bash
jmeter -j expense-embedding-load-test.jmx
```

## References

- [pgvector Documentation](https://github.com/pgvector/pgvector)
- [Ollama Documentation](https://ollama.ai)
- [Nomic Embed Text Model](https://huggingface.co/nomic-ai/nomic-embed-text)
- [Spring Kafka](https://spring.io/projects/spring-kafka)
- [Resilience4j](https://resilience4j.readme.io/)
- [Vector Similarity Search Best Practices](https://archive.ics.uci.edu/ml/datasets)

## Team

- **Service Owner**: Data Pipeline Team
- **Contact**: data-pipeline@example.com
- **Slack**: #expense-management

---

**Last Updated**: May 7, 2026
**Version**: 1.0.0

