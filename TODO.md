# Test Cases

## Single Entity

### POST

  * Two level object [1-1]
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

  * Throughput benchmark
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
  
