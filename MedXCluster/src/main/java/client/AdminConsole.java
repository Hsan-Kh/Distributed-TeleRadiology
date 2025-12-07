package client;

import org.omg.CORBA.*;
import CorbaModule.*;
import java.io.*;
import java.nio.file.*;
import java.util.Scanner;

/**
 * The CORBA Client.
 * Simulates a "Hospital Administrator" checking system status.
 */

public class AdminConsole {

    public static void main(String[] args) {
        try {
            System.out.println("--- Hospital Admin Console (CORBA) ---");

            // 1. Initialize ORB (Object Request Broker)
            ORB orb = ORB.init(args, null);

            // 2. Read the Connection String (IOR) from file
            File iorFile = new File("server.ior");
            if (!iorFile.exists()) {
                System.err.println("ERROR: 'server.ior' not found!");
                System.err.println("Make sure HospitalServer is running first.");
                return;
            }

            String ior = new String(Files.readAllBytes(Paths.get("server.ior"))).trim();

            // 3. Connect to the Monitor Object
            org.omg.CORBA.Object obj = orb.string_to_object(ior);
            CorbaModule.Monitor monitor = CorbaModule.MonitorHelper.narrow(obj);

            // 4. Interactive Menu loop
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("\n[1] Check System Status");
                System.out.println("[2] Exit");
                System.out.print("Select Option: ");

                String input = scanner.nextLine();

                if (input.equals("1")) {
                    String status = monitor.getStatus();
                    System.out.println(">> SERVER RESPONSE: " + status);
                } else if (input.equals("2")) {
                    System.out.println("Exiting Admin Console...");
                    break;
                } else {
                    System.out.println("Invalid option.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
