package io.github.vasurayudu.accounts.service;

import io.github.vasurayudu.accounts.domain.Account;
import io.github.vasurayudu.accounts.domain.AccountRepository;
import io.github.vasurayudu.accounts.domain.AccountType;
import io.github.vasurayudu.accounts.domain.InsufficientFundsException;
import io.github.vasurayudu.accounts.events.AccountEvent;
import io.github.vasurayudu.accounts.events.AccountEvent.EventType;
import io.github.vasurayudu.accounts.events.AccountEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock AccountRepository repository;
    @Mock AccountEventPublisher publisher;
    @InjectMocks AccountService service;

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account("Jane Doe", AccountType.CHECKING);
    }

    @Test
    void openSavesAccountAndPublishesEvent() {
        when(repository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account opened = service.open("Jane Doe", AccountType.SAVINGS);

        assertThat(opened.getBalance()).isEqualByComparingTo("0");
        verify(publisher).publish(argThat(e -> e.type() == EventType.ACCOUNT_OPENED));
    }

    @Test
    void depositIncreasesBalanceAndPublishesEvent() {
        when(repository.findById(account.getId())).thenReturn(Optional.of(account));

        service.deposit(account.getId(), new BigDecimal("250.00"));

        assertThat(account.getBalance()).isEqualByComparingTo("250.00");
        ArgumentCaptor<AccountEvent> captor = ArgumentCaptor.forClass(AccountEvent.class);
        verify(publisher).publish(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo(EventType.DEPOSIT);
        assertThat(captor.getValue().balanceAfter()).isEqualByComparingTo("250.00");
    }

    @Test
    void withdrawMoreThanBalanceIsRejectedAndNoEventPublished() {
        when(repository.findById(account.getId())).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> service.withdraw(account.getId(), new BigDecimal("10")))
                .isInstanceOf(InsufficientFundsException.class);
        verifyNoInteractions(publisher);
    }

    @Test
    void unknownAccountThrowsNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(id)).isInstanceOf(AccountNotFoundException.class);
    }
}
