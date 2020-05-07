package client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import interfaces.Chat;
import interfaces.Server;

public class ChatClient extends UnicastRemoteObject implements Chat {
    private String name;
    private Server server;
    private static final long serialVersionUID = 1L;
    private View view;

    //Testing out branches

    public ChatClient(View view) throws RemoteException, MalformedURLException, NotBoundException {
        super();
        this.view = view;
        server = (Server) Naming.lookup("rmi://127.0.0.1/" + Server.DEFAULT_NAME);
    }

    public boolean isNameOk(String name) throws RemoteException {
        if (server.logInPossible(name)) {
            server.addUser(name, this);
            this.name = name;
            return true;
        }
        return false;
    }

    @Override
    public void sendUserList(String[] userList) {
        view.newUserList(userList);
    }

    @Override
    public void receiveMessage(String message) {
        view.setMessage(message);
    }

    public void sendMessage(String message) {
        new Thread(() -> this.send(message)).start();
    }

    private void send(String message) {
        try {
            this.server.postMessage(this.name, message); } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void logout() {
        try {
            this.server.logOut(this.name); } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}