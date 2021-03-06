package hu.schbme.pay.station.repo;

import hu.schbme.pay.station.model.ItemEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends CrudRepository<ItemEntity, Integer> {

    List<ItemEntity> findAll();

    List<ItemEntity> findAllByOrderById();

    List<ItemEntity> findAllByCodeAndActiveTrueOrderByPriceDesc(String code);

}
