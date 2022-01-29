package hu.schbme.pay.station.repo;

import hu.schbme.pay.station.model.AdministratorEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdministratorRepository extends CrudRepository<AdministratorEntity, Integer> {

    AdministratorEntity findByUsername(String username);

    boolean existsByUsername(String username);

}
