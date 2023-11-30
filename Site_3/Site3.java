import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.io.*;

public class Site3 implements Update {

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

    int backlogNSVector [] = new int[NUMBER_OF_SITES];

    int mySite = 3;

    public Site3() {

        super();

        for (int i = 0; i < NUMBER_OF_LOGICAL_OBJECTS; i++) {
            this.logicalObjects[i] = 0;
        }

        this.sessionNumber = 1;

        for (int i = 0; i < NUMBER_OF_SITES; i++) {
            this.nomimalSessionVector[i] = 1;
            this.backlogNSVector[i] = 1;
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
        String filePath = SESSION_NUMBER_FILE_PATH + "Site3.txt";

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


        for (int i = 0; i < NUMBER_OF_SITES; i++) {
            if ((i + 1) != this.mySite) {

                try {
                    String stubName = "Site" + String.valueOf(i + 1) + "Update";
                    Registry registry = LocateRegistry.getRegistry();
                    Update stub = (Update) registry.lookup(stubName);

                    stub.setNominalSessionVector(this.mySite - 1, 0);
                } catch (Exception e) {
                    System.err.println("Error in updating NS vector of sites.");
                    e.printStackTrace();
                }

            }
        }

        return true;
    }

    public boolean setNominalSessionVector(int site, int value) {

        if (this.sessionNumber == 0) {
            // My site is actually failed so I cannot update my NS vector at this time
            // Therefore, I will update my back log NS vector

            this.backlogNSVector[site] = value;
            return true;
        }

        try {
            this.nomimalSessionVector[site] = value;
            this.backlogNSVector[site] = value;
            System.err.println("Site " + String.valueOf(this.mySite) + ": Control Transaction: Marking Site " + String.valueOf(site + 1) + " as failed. ");
            // Updating the backlog NS vector as well in order to stay consistent

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void publishUpdatedSessionNumbers(int newSessionNumber, int site) {

        for (int i = 0; i < NUMBER_OF_SITES; i++) {

            if (this.mySite - 1 == i) {
                continue;
            }

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

    public boolean refeshLogicalObjects() {

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
            return false;
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

        return true;

    }


    public void initiateRecovery() {

        int oldSessionNumber = -1;

        try {

            String filePath = SESSION_NUMBER_FILE_PATH + "Site3.txt";
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

        // We need to update the NS vector of this site using the backlog NS updates that we have

        // Currently, we don't have updated NS vector as we have failed

        System.err.print("Old NS vector at the time of failure: ");
        for (int i = 0; i < NUMBER_OF_SITES; i++) {
            System.err.print(String.valueOf(this.nomimalSessionVector[i]) + " ");
        }

        // Copying the updated NS vector
        for (int i = 0; i < NUMBER_OF_SITES; i++) {
            this.nomimalSessionVector[i] = this.backlogNSVector[i];
        }

        // Also we need to update the new Session Number with the NS vector

        this.nomimalSessionVector[this.mySite - 1] = newSessionNumber;

        System.err.print("\nNS vector refreshed with the updated: ");
        for (int i = 0; i < NUMBER_OF_SITES; i++) {
            System.err.print(String.valueOf(this.nomimalSessionVector[i]) + " ");
        }
        System.out.println();

        // Now we need to let everybody know of the new Session Number of the site.
        // At this point the site is still is recovery initiation phase
        // The site needs to execute a copier transaction and set its actual session number as newSessionNumber
        // for it to be fully operational

        // Next step: publish updated session number to all available sites

        this.publishUpdatedSessionNumbers(newSessionNumber, mySite);

        // Now, the site is in the process of recovery
        // Next it must execute a copier transaction to refresh all logical objects

        if (this.refeshLogicalObjects()) {
            // The logical objects are now refreshed.
            // Now, we can load the new session number in the actual session number

            this.sessionNumber = newSessionNumber;

            System.err.println("Site " + String.valueOf(mySite) + ": Recovery process completed for site " + String.valueOf(mySite));
            System.err.println("Site " + String.valueOf(mySite) + ": The site is now fully operational. ");
        } else {
            System.err.println("Site " + String.valueOf(mySite) + ": The site cannot become operational. ");
        }


        return;
    }
    public void updateNominalSessionNumber(int failedSite) {
        for (int i = 0; i < NUMBER_OF_SITES; i++) {

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

        Site3 siteThree = new Site3();
        try {
            Update updateStub = (Update) UnicastRemoteObject.exportObject(siteThree, 0);

            Registry registry = LocateRegistry.getRegistry();
            registry.bind("Site3Update", updateStub);

            System.err.println("Site 3 ready!");
        } catch (Exception e) {
            System.err.println("Encountered an issue.");
            e.printStackTrace();
        }
    }
}