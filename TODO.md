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

  * Throughput micro-benchmark [GET - DONE]
  * Concurrency test
  * Blocking test - slow store
    
## Code

### Handler

  * Make entity store operations blocking
  * Pull validation out into routing predicate
  
### RestEntity

  * [DONE] Make interface and default impl
  
### EntityStore

  * Optional versioning
  * Optional state machine
  * Plugable identity strategy
  * Custom validation (e.g. unique name)
  
### Test Code

  * Remove duplication between micro benchmarks and RestDSLSpec
  * Stop benchmarks running out of file handles
  * [DONE] Write report out to a file based on environment
  * [DONE] Include runtime memory and system details in report
