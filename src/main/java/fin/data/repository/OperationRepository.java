package fin.data.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fin.data.entity.Operation;

@Repository
public interface OperationRepository extends JpaRepository<Operation, Long> {
	public List<Operation> findByDateBetweenOrderByDate(LocalDateTime startTime, LocalDateTime endTime);
}
