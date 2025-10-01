package ru.denis.balance_accounting_system.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.denis.balance_accounting_system.models.ArchiveMetadata;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ArchiveMetadataRepository extends JpaRepository<ArchiveMetadata, Long> {

    List<ArchiveMetadata> findByPeriodStartLessThanEqual(LocalDate periodStartIsLessThan);

    List<ArchiveMetadata> findByPeriodEndBefore(LocalDate periodEndBefore);

    boolean existsByTableName(String tableName);
}
