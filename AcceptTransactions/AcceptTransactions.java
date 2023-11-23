import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class AcceptTransactions {

    public static final int NUMBER_OF_SITES = 3;

    public static void main(String[] arguments) throws IOException {
        try {
            Registry registry = LocateRegistry.getRegistry();

            // Look up the remote objects by their registered names
            Update site1 = (Update) registry.lookup("Site1Update");
            Update site2 = (Update) registry.lookup("Site2Update");
            Update site3 = (Update) registry.lookup("Site3Update");

            Update [] allSites = {site1, site2, site3};

            System.out.println("----- PROJECT DEMO -----");

            while (true) {
                System.err.println("\n1. To run a transaction on a site: Please use the format [<'TXN'>, <SITE NUMBER>," +
                        "<OPERATION>, <OBJECT NUMBER>, <UPDATE VALUE>]");
                System.err.println("2. To enforce failure on a site: Please use the format [<'FAIL'>, <SITE NUMBER>]");
                System.err.println("3. To recover a site: Please use the format [<'RECOVER'>, <SITE NUMBER>]");
                System.err.println("4. To print the global view of all sites: PLease use the format ['PRINT']");

                System.out.print("\nEnter input: ");

                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                String inputString = reader.readLine();

                String args [] = inputString.split(",");

                String function = args[0];
                int siteNumber, objectNumber, updateValue;
                char operation;

                switch (function) {
                    case "TXN":
                        siteNumber = Integer.valueOf(args[1]);
                        operation = args[2].toCharArray()[0];
                        objectNumber = Integer.valueOf(args[3]);
                        updateValue = Integer.valueOf(args[4]);

                        if (siteNumber >= 1 && siteNumber <= 3) {
                            allSites[siteNumber - 1].performUpdate(objectNumber, operation, updateValue, siteNumber);
                        } else {
                            System.err.println("Wrong site number given");
                            System.exit(0);
                        }
                        break;

                    case "FAIL":
                        siteNumber = Integer.valueOf(args[1]);

                        if (siteNumber >= 1 && siteNumber <= 3) {
                            allSites[siteNumber - 1].makeFailure();
                        } else {
                            System.err.println("Wrong site number given");
                            System.exit(0);
                        }

                        break;

                    case "RECOVER":

                        siteNumber = Integer.valueOf(args[1]);

                        if (siteNumber >= 1 && siteNumber <= 3) {
                            allSites[siteNumber - 1].initiateRecovery();
                        } else {
                            System.err.println("Wrong site number given");
                            System.exit(0);
                        }

                        break;

                    case "PRINT":

                        for (int i = 0; i < NUMBER_OF_SITES; i++) {
                            allSites[i].printGlobalView();
                        }
                        break;

                    default:
                        System.err.println("Unknown case given. Exiting!");
                        System.exit(0);
                }

                System.err.println();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
