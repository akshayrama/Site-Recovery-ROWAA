import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Update extends  Remote {

    boolean performUpdate(int objectNumber, char operation, int updateValue, int originSite) throws RemoteException;

    boolean makeFailure() throws RemoteException;

    int getSessionNumber() throws RemoteException;

    void updateNominalSessionNumber(int failedSite) throws RemoteException;

    boolean setNominalSessionVector(int site, int value) throws RemoteException;

    void printGlobalView() throws RemoteException;

    void publishUpdatedSessionNumbers(int newSessionNumber, int site) throws RemoteException;

    int getLogicalObjectValue(int objectNumber) throws RemoteException;

    void setLogicalObjectValue(int objectNumber, int newObjectValue) throws RemoteException;

    boolean refeshLogicalObjects() throws RemoteException;

    void initiateRecovery() throws RemoteException;

    boolean performUpdateOnSingleSite(int objectNumber, char operation, int updateValue, int originSite) throws RemoteException;
 }