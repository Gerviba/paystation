package hu.schbme.pay.station.repo;

import hu.schbme.pay.station.model.GatewayEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GatewayRepository extends CrudRepository<GatewayEntity, Integer> {

    List<GatewayEntity> findAll();

    Optional<GatewayEntity> findByName(String name);

}
