# Test Cases

## Single Entity

### POST

  * Unknown fields onto typed entity
  * Two level object [1-1]
  * Create multiple instances
    * Some invalid
  * [DONE] Create instance of unknown entity type
  * [DONE] POSTing with id (in url, in payload)

### PUT

  * Update multiple instances
    * Some invalid
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

  * Make interface and default impl
  
### EntityStore

  * Optional versioning
  * Optional state machine
  
