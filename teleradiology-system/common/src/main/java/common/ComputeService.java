package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ComputeService extends Remote {
    ImageChunk getTask() throws RemoteException;
    void submitResult(ProcessedChunk result) throws RemoteException;
}