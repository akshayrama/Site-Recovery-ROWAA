import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Site1 {

    public static final int NUMBER_OF_SITES = 3;

    // First, we need to define the logical objects that we would use in our transactions
    // For each site, we need a session number, nominal session vector

    // Let's create them

    // Let the logical objects all be obj1, obj2, obj3, obj4, obj5

    int obj1, obj2, obj3, obj4, obj5;

    int sessionNumber;

    int nomimalSessionVector [] = new int[NUMBER_OF_SITES];

    public Site1() {
        this.obj1 = 0;
        this.obj2 = 0;
        this.obj3 = 0;
        this.obj4 = 0;
        this.obj5 = 0;

        this.sessionNumber = 0;

        for (int i = 0; i < NUMBER_OF_SITES; i++) {
            nomimalSessionVector[i] = 0;
        }
    }




    public static void main(String args[]) {

        Site1 siteOne = new Site1();

    }
}