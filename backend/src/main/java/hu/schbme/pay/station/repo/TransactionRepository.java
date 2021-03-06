package hu.schbme.pay.station.repo;

import hu.schbme.pay.station.model.TransactionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends CrudRepository<TransactionEntity, Long> {

    List<TransactionEntity> findAllByRegularIsTrue();

    List<TransactionEntity> findAllByRegularIsFalse();

    long countAllByGateway(String gateway);

    List<TransactionEntity> findAllByGateway(String gateway);

    List<TransactionEntity> findAllByOrderById();

}
