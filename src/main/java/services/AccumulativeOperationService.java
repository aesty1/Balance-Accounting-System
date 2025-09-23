package services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.AccumulativeOperationRepository;

@Service
public class AccumulativeOperationService {

    @Autowired
    private AccumulativeOperationRepository accumulativeOperationRepository;
}
