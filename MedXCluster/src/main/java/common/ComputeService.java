package common;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ComputeService extends Remote {
    // Workers call this to get work
    ImageChunk getTask() throws RemoteException;

    // Workers call this to return work
    void submitResult(ProcessedChunk result) throws RemoteException;
}