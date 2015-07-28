# Test Cases

## Single Entity

### POST

  * Unknown fields onto typed entity
  * Two level object [1-1]
  * Create instance of unknown entity type
  * Create multiple instances
    * Some invalid
  * [DONE] POSTing with id (in url, in payload)

### PUT

  * Update unknown entity id
  * Update unknown entity type
  * Update multiple instances
    * Some invalid
    
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
  
