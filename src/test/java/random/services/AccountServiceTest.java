package random.services;

import org.junit.jupiter.api.Test;

class AccountServiceTest {
    private static final AccountService accountService = new AccountService();

    @Test
    void testShowAccountsInfo() {
        accountService.showAccountsInfo();
    }
}