package random;

import org.junit.Test;
import random.util.Extractor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestExtractor {

    @Test
    public void testIsConnected() {
        System.out.println(Extractor.isConnected());
    }

    @Test
    public void testGetListOfSSIDs() {
//        Stream.of(Extractor.getListOfSSIDs()).forEach(System.out::println);
//        Arrays.stream(Extractor.getListOfSSIDs()).forEach(System.out::println);
        List<String> list = new ArrayList<>(Arrays.asList(Extractor.getListOfSSIDs()));
        list.forEach(System.out::println);
    }

    @Test
    public void testGetConnectedSSID() {
        System.out.println(Extractor.getConnectedSSID());
    }

//    @Test
    public void testGetBroadcast() {
        System.out.println(Extractor.getBroadcast());
    }
}
