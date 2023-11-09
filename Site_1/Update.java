import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Update extends  Remote {

    boolean performUpdate(int objectNumber, char operation, int updateValue) throws RemoteException;
}