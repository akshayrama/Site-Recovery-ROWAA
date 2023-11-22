import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.io.*;

public class Site1 implements Update {

    public static final int NUMBER_OF_SITES = 2;
    public static final int NUMBER_OF_LOGICAL_OBJECTS = 5;
    public static final String SESSION_NUMBER_FILE_PATH = "../sessionNumberLocalStorage/";

    // First, we need to define the logical objects that we would use in our transactions
    // For each site, we need a session number, nominal session vector

    // Let's create them

    // Let the logical objects all be obj1, obj2, obj3, obj4, obj5

    int logicalObjects [] = new int [NUMBER_OF_LOGICAL_OBJECTS];

    int sessionNumber;
    int nomimalSessionVector [] = new int[NUMBER_OF_SITES];

    int mySite = 1;

    public Site1() {

        super();

        for (int i = 0; i < NUMBER_OF_LOGICAL_OBJECTS; i++) {
            this.logicalObjects[i] = 0;
        }

        this.sessionNumber = 1;

        for (int i = 0; i < NUMBER_OF_SITES; i++) {
           this.nomimalSessionVector[i] = 1;
        }
    }

    public int getSessionNumber() {
        return this.sessionNumber;
    }

    public void printGlobalView() {
        System.out.println("----LOGICAL OBJECTS-----");
        for (int i = 0; i < NUMBER_OF_LOGICAL_OBJECTS; i++) {
            System.out.println("Obj" + String.valueOf(i + 1) + " : " + String.valueOf(this.logicalObjects[i]));
        }
        System.out.println();
    }

    public boolean makeFailure() {
        String filePath = SESSION_NUMBER_FILE_PATH + "Site1.txt";

        try {
            FileWriter fileWriter = new FileWriter(filePath);
            fileWriter.write(String.valueOf(this.sessionNumber));
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        this.sessionNumber = 0;

        System.err.println("Control Transaction: Marking Site " + String.valueOf(mySite) + " as failed. ");

        return true;
    }

    public boolean setNominalSessionVector(int failedSite) {
        try {
            this.nomimalSessionVector[failedSite] = 0;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public void updateNominalSessionNumber(int failedSite) {
        for (int i = 0; i < NUMBER_OF_SITES; i++) {
            if (this.nomimalSessionVector[i] != 0) {

                try {
                    String nominalStub = "Site" + String.valueOf(i + 1) + "Update";
                    Registry registry = LocateRegistry.getRegistry();
                    Update stub = (Update) registry.lookup(nominalStub);

                    stub.setNominalSessionVector(failedSite);

                    System.err.println("Control transaction: Marked site " + String.valueOf(failedSite) + " as failed. ");
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Failed to update nominal session vector.");
                }
            }
        }
    }

    public boolean performUpdateOnSingleSite(int objectNumber, char operation, int updateValue) {
        if (this.sessionNumber == 0) {
            System.err.println("This site is currently down and cannot accept transactions. ");
            return false;
        }

        if (operation == '+') {
            this.logicalObjects[objectNumber - 1] += updateValue;
        } else if (operation == '-') {
            this.logicalObjects[objectNumber - 1] -= updateValue;
        } else {
            return false;
        }

        return true;
    }

    public boolean performUpdate(int objectNumber, char operation, int updateValue) {

        if (!this.performUpdateOnSingleSite(objectNumber, operation, updateValue)) {
            System.err.println("Error performing update on site.");
            return false;
        }

        // Now, this update needs to be performed on all available sites
        // We need to get the list of all available sites

        // This information is stored in the NS vector

        for (int i = 0; i < NUMBER_OF_SITES; i++) {
            if (this.nomimalSessionVector[i] != 0 && i != mySite - 1) {

                try {
                    // The site is perceived to be operational

                    String stubName = "Site" + String.valueOf(i + 1) + "Update";
                    Registry registry = LocateRegistry.getRegistry();
                    Update stub = (Update) registry.lookup(stubName);

                    if (this.nomimalSessionVector[i] != stub.getSessionNumber()) {

                        // The current site recognizes that this site has a failure
                        // Need to issue a control transaction of type 2

                        this.nomimalSessionVector[i] = 0;
                        this.updateNominalSessionNumber(i);
                        continue;
                    }

                    if (!stub.performUpdateOnSingleSite(objectNumber, operation, updateValue)) {
                        System.err.println("Update is not performed properly on site " + String.valueOf(i + 1));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Encountered an issue");
                }

            }
        }

        return true;
    }

    public static void main(String args[])  {

        Site1 siteOne = new Site1();
        try {
            Update updateStub = (Update) UnicastRemoteObject.exportObject(siteOne, 0);

            Registry registry = LocateRegistry.getRegistry();
            registry.bind("Site1Update", updateStub);

            System.err.println("Site 1 ready!");
        } catch (Exception e) {
            System.err.println("Encountered an issue.");
            e.printStackTrace();
        }
    }
}