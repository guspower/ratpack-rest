# Test Cases

## Single Entity

### POST

  * Create multiple instances
    * Some invalid
    * Some pre existing
  * [DONE] Unknown fields onto typed entity. NOTE: no sensible way of getting multiple errors back w/out newing up an ObjectMapper per request
  * [DONE] Create instance of unknown entity type
  * [DONE] POSTing with id (in url, in payload)

### PUT

  * Update multiple instances
    * Some invalid
    * Some not found
  * [DONE] Unknown fields onto typed entity
  * [DONE] Update unknown entity id
  * [DONE] Update unknown entity type
    
### Performance

  * Concurrency test
  * Blocking test - slow store
  * Integrate coda hale metrics into benchmark reporting
  * [DONE] Throughput micro-benchmark [GET - single entity DONE, change to spread across multiple entities]
    
## Relationships
    
  * Implement basic resource functionality (from, to, name)
  * Use default name if none supplied
    
## Code

### Handler

  * Pull validation out into routing predicate
  * [DONE] Make entity store operations blocking
  
### RestEntity

  * [DONE] Make interface and default impl
  
### EntityStore

  * Optional versioning
  * Optional state machine
  * Plugable identity strategy
  * Custom validation (e.g. unique name)
  * JDBC implementation w/ connection pool and h2/hsqldb
  * Neo4j implementation
  
### Test Code

  * Remove duplication between micro benchmarks and RestDSLSpec
  * [DONE] Stop benchmarks running out of file handles
  * [DONE] Write report out to a file based on environment
  * [DONE] Include runtime memory and system details in report
