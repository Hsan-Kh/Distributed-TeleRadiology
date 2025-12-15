package client;

import CorbaModule.*;
import org.omg.CORBA.ORB;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class AdminConsole {

    public static void main(String[] args) {
        try {
            System.out.println("--- Hospital Admin Console (CORBA) ---");
            ORB orb = ORB.init(args, null);
            File iorFile = new File("server.ior");
            if (!iorFile.exists()) {
                System.err.println("ERROR: 'server.ior' not found!");
                System.err.println("Make sure HospitalServer is running first.");
                return;
            }

            String ior = new String(Files.readAllBytes(Paths.get("server.ior"))).trim();
            org.omg.CORBA.Object obj = orb.string_to_object(ior);
            CorbaModule.Monitor monitor = CorbaModule.MonitorHelper.narrow(obj);

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