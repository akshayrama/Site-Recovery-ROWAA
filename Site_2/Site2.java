import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Site2 implements Update {

    public static final int NUMBER_OF_SITES = 3;
    public static final int NUMBER_OF_LOGICAL_OBJECTS = 5;

    // First, we need to define the logical objects that we would use in our transactions
    // For each site, we need a session number, nominal session vector

    // Let's create them

    // Let the logical objects all be obj1, obj2, obj3, obj4, obj5

    int logicalObjects [] = new int [NUMBER_OF_LOGICAL_OBJECTS];

    int sessionNumber;
    int nomimalSessionVector [] = new int[NUMBER_OF_SITES];

    public Site2() {

        super();

        for (int i = 0; i < NUMBER_OF_LOGICAL_OBJECTS; i++) {
            logicalObjects[i] = 0;
        }

        this.sessionNumber = 0;

        for (int i = 0; i < NUMBER_OF_SITES; i++) {
            nomimalSessionVector[i] = 0;
        }
    }

    public boolean performUpdate(int objectNumber, char operation, int updateValue) {
        if (operation == '+') {
            logicalObjects[objectNumber - 1] += updateValue;
        } else if (operation == '-') {
            logicalObjects[objectNumber - 1] -= updateValue;
        } else {
            return false;
        }

        return true;
    }

    public static void main(String args[]) {

        Site2 siteTwo = new Site2();
        try {
            Update updateStub = (Update) UnicastRemoteObject.exportObject(siteTwo, 0);

            Registry registry = LocateRegistry.getRegistry();
            System.out.println("fokfokfokf");
            registry.bind("Site2", updateStub);

            System.err.println("Site 2 ready!");
        } catch (Exception e) {
            System.err.println("Encountered an issue.");
            e.printStackTrace();
        }



    }
}