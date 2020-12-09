package random.chrom;

import com.sun.jna.platform.win32.Crypt32Util;
import random.util.OperatingSystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;

public class ChromeSecurity {

    private ChromeSecurity() {
    }

    public static String getOSXKeychainPasswordAsAdmin(String host) {
        try {
            host = host.replace("https://", "").replace("http://", "");
            final String command = "security find-internet-password -gs "
                    + host.substring(0, host.indexOf('/') > 0 ? host.indexOf('/') : host.length()) + " -w";
            final Process result = Runtime.getRuntime().exec(command);
            final BufferedReader in = new BufferedReader(new InputStreamReader(result.getInputStream()));
            final String password = in.readLine();
            in.close();
            return password != null ? password : "";
        } catch (final IOException e) {
            return "";
        }
    }

    public static String getWin32Password(byte[] encryptedData) {
        try {
            return new String(Crypt32Util.cryptUnprotectData(encryptedData));
        } catch (Exception e) {
            return "";
        }
    }

    public static String getPassword(ResultSet results) throws Exception {
        String password;
        switch (OperatingSystem.getOperatingSystem()) {
            case WINDOWS:
                password = getWin32Password(results.getBytes("password_value"));
                break;
            case MAC:
                password = getOSXKeychainPasswordAsAdmin(results.getString("action_url"));
                break;
            default:
                throw new Exception(System.getProperty("os.name") + " is not supported by this application!");
        }
        return password;
    }
}
