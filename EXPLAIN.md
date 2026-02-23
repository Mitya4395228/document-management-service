## Поисковой запрос для поиска документов по статусу и дате создания.
```sql
EXPLAIN (ANALYZE) SELECT * FROM document_flow.documents WHERE 1=1 AND created_at >= '2026-02-23 04:08:00' AND status = 'DRAFT' ORDER BY created_at ASC LIMIT 100
```

## 1. Без индексов

### QUERY PLAN
```sql
"Limit  (cost=1350.82..1351.07 rows=100 width=78) (actual time=2.606..2.614 rows=100 loops=1)"
"  ->  Sort  (cost=1350.82..1363.43 rows=5045 width=78) (actual time=2.605..2.608 rows=100 loops=1)"
"        Sort Key: created_at"
"        Sort Method: top-N heapsort  Memory: 38kB"
"        ->  Seq Scan on documents  (cost=0.00..1158.00 rows=5045 width=78) (actual time=0.532..2.161 rows=10000 loops=1)"
"              Filter: ((created_at >= '2026-02-23 04:08:00'::timestamp without time zone) AND ((status)::text = 'DRAFT'::text))"
"              Rows Removed by Filter: 30000"
"Planning Time: 0.060 ms"
"Execution Time: 2.626 ms"
```
Так как нет индексов по полям created_at и status, то запрос выполняется последовательно Seq Scan. PostgreSQL читает всю таблицу, фильтрует строки и выполняет сортировку.

## 2. С индексом по дате создания.

### Создание индекса
```sql
CREATE INDEX idx_documents_created_at ON document_flow.documents (created_at);
```

### QUERY PLAN
```sql
"Limit  (cost=0.29..20.79 rows=100 width=78) (actual time=1.819..1.864 rows=100 loops=1)"
"  ->  Index Scan using idx_documents_created_at on documents  (cost=0.29..1034.71 rows=5045 width=78) (actual time=1.818..1.858 rows=100 loops=1)"
"        Index Cond: (created_at >= '2026-02-23 04:08:00'::timestamp without time zone)"
"        Filter: ((status)::text = 'DRAFT'::text)"
"        Rows Removed by Filter: 10000"
"Planning Time: 0.192 ms"
"Execution Time: 1.879 ms"
```
PostgreSQL использует индекс idx_documents_created_at. Отталкиваясь от этого индекса, PostgreSQL начинает со строк, в которых created_at >= '2026-02-23 04:08:00'::timestamp without time zone и по этим строкам фильтрует те, у которых status = 'DRAFT'. В данном запросе из-за наличия индекса idx_documents_created_at сокращаются наклодные операции, в часности уменьшенно число Rows Removed by Filter: с **30000** до **10000**.
