package repositories;

import models.Account;
import models.AccumulativeOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccumulativeOperationRepository extends JpaRepository<AccumulativeOperation, Long> {
}
