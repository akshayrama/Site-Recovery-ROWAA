import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.io.*;

public class Site2 implements Update {

    public static final int NUMBER_OF_SITES = 3;
    public static final int NUMBER_OF_LOGICAL_OBJECTS = 5;
    public static final String SESSION_NUMBER_FILE_PATH = "../sessionNumberLocalStorage/";

    // First, we need to define the logical objects that we would use in our transactions
    // For each site, we need a session number, nominal session vector

    // Let's create them

    // Let the logical objects all be obj1, obj2, obj3, obj4, obj5

    int logicalObjects [] = new int [NUMBER_OF_LOGICAL_OBJECTS];

    int sessionNumber;
    int nomimalSessionVector [] = new int[NUMBER_OF_SITES];

    int mySite = 2;

    public Site2() {

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
        System.out.println("\nList of logical objects in site: " + String.valueOf(this.mySite));
        for (int i = 0; i < NUMBER_OF_LOGICAL_OBJECTS; i++) {
            System.out.println("Obj" + String.valueOf(i + 1) + " : " + String.valueOf(this.logicalObjects[i]));
        }
        System.out.println();
    }

    public boolean makeFailure() {
        String filePath = SESSION_NUMBER_FILE_PATH + "Site2.txt";

        try {
            FileWriter fileWriter = new FileWriter(filePath);
            fileWriter.write(String.valueOf(this.sessionNumber));
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        this.sessionNumber = 0;

        System.err.println("Site " + String.valueOf(mySite) + ": Control Transaction: Marking Site " + String.valueOf(mySite) + " as failed. ");

        return true;
    }

    public boolean setNominalSessionVector(int site, int value) {
        try {
            this.nomimalSessionVector[site] = value;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void publishUpdatedSessionNumbers(int newSessionNumber, int site) {

        for (int i = 0; i < NUMBER_OF_SITES; i++) {

            // Need to get all the stubs of all sites and update the NS vector
            // With the new session number

            try {
                String nominalStub = "Site" + String.valueOf(i + 1) + "Update";
                Registry registry = LocateRegistry.getRegistry();
                Update stub = (Update) registry.lookup(nominalStub);

                stub.setNominalSessionVector(site - 1, newSessionNumber);

                System.err.println("Site " + String.valueOf(mySite) + ": New Session Number " + String.valueOf(newSessionNumber) + " loaded for Site " +
                        String.valueOf(site));
            } catch (Exception e) {
                System.err.println("Some error occurred. ");
                e.printStackTrace();
            }

        }
    }

    public int getLogicalObjectValue(int objectNumber) {
        return this.logicalObjects[objectNumber - 1];
    }

    public void setLogicalObjectValue(int objectNumber, int newObjectValue) {
        this.logicalObjects[objectNumber - 1] = newObjectValue;
    }

    public void refeshLogicalObjects() {

        // Need to iterate over the NS vector to check if any site is operational

        int siteToRefresh = -1;

        for (int i = 0; i < NUMBER_OF_SITES; i++) {
            if (this.nomimalSessionVector[i] != 0 && (i + 1) != this.mySite) {
                siteToRefresh = i;
                break;
            }
        }

        if (siteToRefresh == -1) {
            System.err.println("Site " + String.valueOf(mySite) + ": There are no other operational sites to execute a copier transaction from. ");
            return;
        }


        try {
            String stubName = "Site" + String.valueOf(siteToRefresh + 1) + "Update";
            Registry registry = LocateRegistry.getRegistry();
            Update stub = (Update) registry.lookup(stubName);

            for (int i = 1; i <= NUMBER_OF_LOGICAL_OBJECTS; i++) {

                int newValue = stub.getLogicalObjectValue(i);
                this.setLogicalObjectValue(i, newValue);
            }

            System.err.println("Site " + String.valueOf(mySite) + ": Copier transaction executed successfully!");
        } catch(Exception e) {
            System.err.println("Site " + String.valueOf(mySite) + ": Some issue occurred.");
            e.printStackTrace();
        }

    }


    public void initiateRecovery() {

        int oldSessionNumber = -1;

        try {

            String filePath = SESSION_NUMBER_FILE_PATH + "Site2.txt";
            File file = new File(filePath);

            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line = bufferedReader.readLine();
            bufferedReader.close();

            oldSessionNumber = Integer.valueOf(line);
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.err.println("Site " + String.valueOf(mySite) + " is trying to recover. ");
        System.err.println("Site " + String.valueOf(mySite) + ": Have read the old session number of the site as " + String.valueOf(oldSessionNumber));

        int newSessionNumber = oldSessionNumber + 1;

        // Now we need to let everybody know of the new Session Number of the site.
        // At this point the site is still is recovery initiation phase
        // The site needs to execute a copier transaction and set its actual session number as newSessionNumber
        // for it to be fully operational

        // Even though the site has been failed, its NS vector has been updated accordingly
        // This failed site can still process control transactions
        // Just not user transactions
        // While it's logical data items are out of date, its NS vector should be updated with
        // the operational sites

        // Next step: publish updated session number to all available sites including its own

        this.publishUpdatedSessionNumbers(newSessionNumber, mySite);

        // Now, the site is in the process of recovery
        // Next it must execute a copier transaction to refresh all logical objects

        this.refeshLogicalObjects();

        // The logical objects are now refreshed.
        // Now, we can load the new session number in the actual session number

        this.sessionNumber = newSessionNumber;

        System.err.println("Site " + String.valueOf(mySite) + ": Recovery process completed for site " + String.valueOf(mySite));
        System.err.println("Site " + String.valueOf(mySite) + ": The site is now fully operational. ");

        return;
    }
    public void updateNominalSessionNumber(int failedSite) {
        for (int i = 0; i < NUMBER_OF_SITES; i++) {

            // Don't need to check for all available sites
            // This is a control transaction of type 2
            // So this must be executed on all the sites
            // Irrespective of whether they are available or not

            try {
                String nominalStub = "Site" + String.valueOf(i + 1) + "Update";
                Registry registry = LocateRegistry.getRegistry();
                Update stub = (Update) registry.lookup(nominalStub);

                stub.setNominalSessionVector(failedSite, 0);

                System.err.println("Site " + String.valueOf(mySite) + ": Control transaction: Marked site " + String.valueOf(failedSite + 1) + " as failed. ");
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Site " + String.valueOf(mySite) + ": Failed to update nominal session vector.");
            }

        }
    }

    public boolean performUpdateOnSingleSite(int objectNumber, char operation, int updateValue, int originSite) {
        if (this.sessionNumber == 0) {
            System.err.println("This site is currently down and cannot accept transactions. ");
            return false;
        }

        if (this.mySite != originSite) {
            System.err.println("Update given by another site's transaction");
            System.err.println("Transaction originated at site: " + String.valueOf(originSite));
        } else {
            System.err.println("Transaction originated in the current site");
        }

        if (operation == '+') {
            this.logicalObjects[objectNumber - 1] += updateValue;
        } else if (operation == '-') {
            this.logicalObjects[objectNumber - 1] -= updateValue;
        } else if (operation == '*') {
            this.logicalObjects[objectNumber - 1] *= updateValue;
        } else if (operation == '/') {
            this.logicalObjects[objectNumber - 1] /= updateValue;
        } else {
            return false;
        }

        return true;
    }

    public boolean performUpdate(int objectNumber, char operation, int updateValue, int originSite) {

        if (!this.performUpdateOnSingleSite(objectNumber, operation, updateValue, originSite)) {
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

                    if (!stub.performUpdateOnSingleSite(objectNumber, operation, updateValue, originSite)) {
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

        Site2 siteTwo = new Site2();
        try {
            Update updateStub = (Update) UnicastRemoteObject.exportObject(siteTwo, 0);

            Registry registry = LocateRegistry.getRegistry();
            registry.bind("Site2Update", updateStub);

            System.err.println("Site 2 ready!");
        } catch (Exception e) {
            System.err.println("Encountered an issue.");
            e.printStackTrace();
        }
    }
}