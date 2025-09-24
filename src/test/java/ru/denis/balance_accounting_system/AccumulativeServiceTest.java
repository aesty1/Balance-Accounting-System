//package ru.denis.balance_accounting_system;
//
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import ru.denis.balance_accounting_system.dto.AccumulativeRequest;
//import ru.denis.balance_accounting_system.models.Account;
//import ru.denis.balance_accounting_system.models.AccumulativeOperation;
//import ru.denis.balance_accounting_system.repositories.AccountRepository;
//import ru.denis.balance_accounting_system.repositories.AccumulativeOperationRepository;
//import ru.denis.balance_accounting_system.services.AccumulativeOperationService;
//import ru.denis.balance_accounting_system.services.BalanceService;
//
//import java.math.BigDecimal;
//import java.util.Optional;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//public class AccumulativeServiceTest {
//
//    @Mock
//    private AccountRepository accountRepository;
//
//    @Mock
//    private AccumulativeOperationRepository accumulativeOperationRepository;
//
//    @Mock
//    private AccumulativeOperationService accumulativeOperationService;
//
//    @Mock
//    private BalanceService balanceService;
//
//    @Test
//    void processAccumulativeOperation_Success() {
//        // Given
//        Long accountId = 1L;
//        AccumulativeRequest request = new AccumulativeRequest();
//        request.setAmount(new BigDecimal("15.75023"));
//        request.setDescription("Ежемесячная комиссия с высокой точностью");
//        request.setPeriod("2024-01");
//        request.setReferenceId("acc_high_precision_001");
//
//        Account account = new Account();
//        account.setId(accountId);
//        account.setBalance(new BigDecimal("1000.0"));
//        account.setVersion(0);
//
//        when(accountRepository.findByIdWithLock(accountId)).thenReturn(Optional.of(account));
//        when(accumulativeOperationRepository.existsByReferenceId(request.getReferenceId())).thenReturn(false);
//        when(accumulativeOperationRepository.save(any(AccumulativeOperation.class))).thenAnswer(invocation -> {
//            AccumulativeOperation op = invocation.getArgument(0);
//            op.setId(1L);
//            return op;
//        });
//        when(accountRepository.save(any(Account.class))).thenReturn(account);
//        // When
//
//        // Then
//    }
//
//}
