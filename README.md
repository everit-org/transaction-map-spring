# transaction-map-spring
Transactional Map implementation based on Spring PlatformTransaction


## Usage

The example above uses the [_ReadCommited Transactional Map_][0] implementation.

    // Passing null will use a HashMap inside the TransactionalMap
    Map<String, String> managedMap = new SpringTxMap<>(
        new ReadCommitedTransactionalMap<>(null));
    
    // This map can be used anytime. If there is an ongoing Spring transaction
    // ongoing, the map will synchronize itself and commit/rollback when the 
    // transaction ends. That is all!


[0]: https://github.com/everit-org/transaction-map-readcommited
