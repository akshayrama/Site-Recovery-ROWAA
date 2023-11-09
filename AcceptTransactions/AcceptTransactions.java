import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class AcceptTransactions {

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry();

            // Look up the remote objects by their registered names
            Update site1 = (Update) registry.lookup("Update");
//            Update site2 = (Update) registry.lookup("Site2");

            int objectNumber = 1;
            char operation = '+';
            int updateValue = 10;

            boolean success1 = site1.performUpdate(objectNumber, operation, updateValue);
//            boolean success2 = site2.performUpdate(objectNumber, operation, updateValue);

            System.out.println(success1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
