package random.services;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

public class WinWiFiServiceTest {

    @Test
    public void testIsConnected() {
        System.out.println(WinWiFiService.isConnected());
    }

    @Test
    public void testGetListOfSSIDs() {
        Stream.of(WinWiFiService.getListOfSSIDs()).forEach(System.out::println);
//        Arrays.stream(WinWiFiService.getListOfSSIDs()).forEach(System.out::println);
//        new ArrayList<>(Arrays.asList(WinWiFiService.getListOfSSIDs())).forEach(System.out::println);
    }

    @Test
    public void testGetConnectedSSID() {
        System.out.println(WinWiFiService.getConnectedSSID());
    }

    @Disabled
    @Test
    public void testGetBroadcast() {
        System.out.println(WinWiFiService.getBroadcast());
    }
}
