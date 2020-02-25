package com.github.rudder.spring;

import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class SomeEntityService {

    private final SomeEntityRepository repo;

    @Inject
    public SomeEntityService(final SomeEntityRepository repo) {
        this.repo = repo;
    }

    public SomeEntity findSomeEntityById(final Long aLong) {
        return repo.findSomeEntityById(aLong);
    }

    public SomeEntityRepository getRepo() {
        return repo;
    }

    public SomeEntity createSome(final String title) {
        final SomeEntity entity = new SomeEntity();
        entity.setTitle(title);
        return repo.save(entity);
    }

}
