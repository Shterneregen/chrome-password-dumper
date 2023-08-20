package random.services;

import random.chrome.ChromeService;

import java.lang.invoke.MethodHandles;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AccountService {
    private static final Logger LOG = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    private static final ChromeService chromeService = new ChromeService();
    private static final AccountFormatterService accountFormatterService = new AccountFormatterService();

    public void showAccountsInfo() {
        try {
            accountFormatterService.showAccountsInfo(chromeService.getProfiles());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void showShortAccountsInfo() {
        try {
            accountFormatterService.showShortAccountsInfo(chromeService.getProfiles());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void saveAccountsInfoToFile(String filePath, String pubKeyPath) {
        try {
            accountFormatterService.saveAccountsInfoToFile(chromeService.getProfiles(), filePath, pubKeyPath);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
