//package ru.denis.balance_accounting_system;
//
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import ru.denis.balance_accounting_system.dto.AccumulativeRequest;
//import ru.denis.balance_accounting_system.dto.AccumulativeResponse;
//import ru.denis.balance_accounting_system.models.Account;
//import ru.denis.balance_accounting_system.models.AccumulativeOperation;
//import ru.denis.balance_accounting_system.repositories.AccountRepository;
//import ru.denis.balance_accounting_system.repositories.AccumulativeOperationRepository;
//import ru.denis.balance_accounting_system.services.AccumulativeOperationService;
//import ru.denis.balance_accounting_system.services.BalanceService;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.Optional;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class BalanceAccountingSystemApplicationTests {
//
//	@Mock
//	private AccountRepository accountRepository;
//
//	@Mock
//	private AccumulativeOperationRepository accumulativeOperationRepository;
//
//	@InjectMocks
//	private AccumulativeOperationService accumulativeOperationService;
//
//	@Test
//	void processAccumulativeOperation_Success() {
//		// Given
//		Long accountId = 1L;
//		AccumulativeRequest request = new AccumulativeRequest();
//		request.setAmount(new BigDecimal("15.75023"));
//		request.setDescription("Ежемесячная комиссия с высокой точностью");
//		request.setPeriod("2024-01");
//		request.setReferenceId("acc_high_precision_001");
//
//		Account account = new Account();
//		account.setId(accountId);
//		account.setBalance(new BigDecimal("1000.0"));
//		account.setVersion(0);
//
//		when(accountRepository.findByIdWithLock(accountId)).thenReturn(Optional.of(account));
//		when(accumulativeOperationRepository.existsByReferenceId(request.getReferenceId())).thenReturn(false);
//		when(accumulativeOperationRepository.save(any(AccumulativeOperation.class))).thenAnswer(invocation -> {
//			AccumulativeOperation op = invocation.getArgument(0);
//			op.setId(1L);
//			return op;
//		});
//		when(accountRepository.save(any(Account.class))).thenReturn(account);
//
//		// When
//		AccumulativeResponse response = accumulativeOperationService.processAccumulativeOperation(accountId, request);
//
//
//		// Then
//		assertThat(response).isNotNull();
//		assertThat(response.getAccountId()).isEqualTo(accountId);
//		assertThat(response.getAmountCalculated()).isEqualTo(new BigDecimal("15.75023"));
//		assertThat(response.getDescription()).isEqualTo("Ежемесячная комиссия с высокой точностью");
//		assertThat(response.getPeriodDate()).isEqualTo(LocalDate.of(2024, 1, 31));
//
//		verify(accountRepository).findByIdWithLock(accountId);
//		verify(accumulativeOperationRepository).existsByReferenceId("test_ref_001");
//		verify(accumulativeOperationRepository).save(any(AccumulativeOperation.class));
//		verify(accountRepository).save(account);
//	}
//
//}
