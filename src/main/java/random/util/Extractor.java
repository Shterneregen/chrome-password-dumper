package random.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Extractor {

    private static final Logger LOG = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    public static String NOT_SET = "NOT_SET";

    public static boolean isEnabled() {
        try {
            String state;
            ProcessBuilder builder = new ProcessBuilder(
                    "cmd.exe", "/c", "netsh interface show interface \"Wi-Fi\"");
            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = r.readLine()) != null) {
                //line = r.readLine();
                if (line.contains("Administrative state")) {
                    state = line.split("\\s+")[3];
                    state = state.toLowerCase();
                    return state.equals("enabled");
                }
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
        return false;
    }

    public static boolean isConnected() {
        try {
//            ProcessBuilder builder = new ProcessBuilder(
//                    "cmd.exe", "/c", "netsh interface show interface \"Wi-Fi\"");
//            builder.redirectErrorStream(true);
//            Process p = builder.start();
//            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
//            BufferedReader r = call("netsh interface show interface \"Wi-Fi\"");
            BufferedReader r = call("netsh interface show interface \"Wi-Fi\"");
            if (r == null) return false;
            String state;
            String line;
            while ((line = r.readLine()) != null) {
                System.out.println(line);
                if (line.contains("Connect state")) {
                    state = line.split("\\s+")[3];
                    System.out.println("connected");
                    System.out.println(state);
                    state = state.toLowerCase();
                    return state.equals("connected");
                }
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
        return false;
    }

    public static String getConnectedSSID() {
        String ssid = NOT_SET;
        BufferedReader r = call("netsh wlan show interfaces");
        try {
            if (r == null) return ssid;
            String line;
            while ((line = r.readLine()) != null) {
                System.out.println(line);
                //line = r.readLine();
//                if (line.contains("SSID")) {
//                    ssid = line.split("\\s+")[3];
////                    System.out.println(ssid);
//                    return ssid;
//                }
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
        return ssid;
    }

    public static String[] getListOfSSIDs() {
        String[] ssidList;
        String ssid;
        ArrayList<String> arr = new ArrayList<>();
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "cmd.exe", "/c", "netsh wlan show networks");
            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), "cp866"));
            String line;
            while ((line = r.readLine()) != null) {
                if (line.contains("SSID")) {
                    ssid = line.split("\\s+")[2];
//                    ssid = line.split("\\s+")[3];
                    System.out.println(ssid);
                    arr.add(ssid);
                }
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
        ssidList = new String[arr.size()];
        arr.toArray(ssidList);
        return ssidList;
    }

    public static String getIP(final String interfaceName) {
        String ip = NOT_SET;
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "cmd.exe", "/c", "netsh interface ip show addresses " + interfaceName);
            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = r.readLine()) != null) {
                //line = r.readLine();
                if (line.contains("IP Address")) {
                    ip = line.split("\\s+")[3];
                    return ip;
                }
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
        return ip;
    }

    public static String getSubnetMask() {
        String sb = NOT_SET;
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "cmd.exe", "/c", "netsh interface ip show addresses");
            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = r.readLine()) != null) {
                if (line.contains("Subnet Prefix")) {
                    sb = line.split("\\s+")[5];
                    sb = sb.substring(0, sb.length() - 1);
                    return sb;
                }
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
        return sb;
    }

    public static String getBroadcast() {
        String subnetMask = getSubnetMask();

        // TODO: iml
        String interfaceName = "\"Interface-Name\"";
        String ip = getIP(interfaceName);

        String[] arrSubnetMask = subnetMask.split("\\.");
        String[] arrIP = ip.split("\\.");
        int[] networkAddress = new int[4];
        int[] broadcastAddress = new int[4];

        for (int i = 0; i < 4; i++) {
            networkAddress[i] = Integer.parseInt(arrIP[i]) & Integer.parseInt(arrSubnetMask[i]);
        }

        StringBuilder broadcast = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            //broadcastAddress[i] =  networkAddress[i] | (~Integer.parseInt(arrSubnetMask[i]) & 0xff);
            broadcast.append(".").append(networkAddress[i] | (~Integer.parseInt(arrSubnetMask[i]) & 0xff));
        }

        //mask AND ip you get network address
        //Invert Mask OR Network Address you get broadcast

        return broadcast.substring(1);
    }

    private static BufferedReader call(String command) {
        try {
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
            builder.redirectErrorStream(true);
            Process p = builder.start();
            return new BufferedReader(new InputStreamReader(p.getInputStream(), "cp866"));
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }
}
