package services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.ProcessedMessageRepository;

@Service
public class ProcessedMessageService {

    @Autowired
    private ProcessedMessageRepository processedMessageRepository;
}
