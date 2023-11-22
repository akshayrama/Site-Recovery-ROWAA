import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Update extends  Remote {

    boolean performUpdate(int objectNumber, char operation, int updateValue) throws RemoteException;

    boolean makeFailure(int siteNumber) throws RemoteException;

    int getSessionNumber() throws RemoteException;

    void updateNominalSessionNumber(int failedSite) throws RemoteException;

    boolean setNominalSessionVector(int failedSite) throws RemoteException;

    void printGlobalView() throws RemoteException;

    boolean performUpdateOnSingleSite(int objectNumber, char operation, int updateValue) throws RemoteException;
}