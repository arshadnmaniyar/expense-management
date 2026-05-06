package org.example.expensecommand.service;

import org.example.expensecommand.domain.store.Store;
import org.example.expensecommand.domain.store.StoreRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
//@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository repository;

    public StoreService (StoreRepository repository) {
        this.repository = repository;
    }

    public UUID getOrCreateStore(String storeName) {

        return repository.findByStoreName(storeName)
                .map(Store::getStoreId)
                .orElseGet(() -> {
                    Store store = new Store();
                    store.setStoreName(storeName);
                    return repository.save(store).getStoreId();
                });
    }
}
