package server;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import client.View;
import interfaces.Chat;
import interfaces.Server;
import javafx.application.Application;

public class ChatServer extends UnicastRemoteObject implements Server {

    private static final long serialVersionUID = 1L;
    private Map<String, Chat> chatter = new HashMap<String, Chat>();

    public ChatServer() throws RemoteException {
        super();
    }

    @Override
    public synchronized boolean logInPossible(String name) {
        return !this.chatter.containsKey(name);
    }

    @Override
    public synchronized void addUser(String name, Chat client) {
        this.chatter.put(name, client);
        this.sendUserListToClients();
    }

    @Override
    public synchronized void logOut(String name) {
        this.chatter.remove(name);
        this.sendUserListToClients();
    }

    public synchronized void sendUserListToClients() {
        while (!this.iterateForUserList())
            ;
    }

    private boolean iterateForUserList() {
        for (Iterator<Map.Entry<String, Chat>> it = this.chatter.entrySet().iterator(); it.hasNext();) {
            try {
                it.next().getValue().sendUserList(this.chatter.keySet().toArray(new String[0])); } catch (RemoteException e) {
                it.remove();
                return false;
            }
        }
        return true;
    }

    @Override
    public synchronized void postMessage(String name, String message) {
        String finalMessage = name + ": " + message;
        boolean b = false;
        for (Iterator<Map.Entry<String, Chat>> it = this.chatter.entrySet().iterator(); it.hasNext();) {
            try {
                it.next().getValue().receiveMessage(finalMessage); } catch (RemoteException e) {
                it.remove();
                b = true;
            }
        } if (b) {
            this.sendUserListToClients();
        }
    }

    public static void main(String[] args) throws RemoteException, MalformedURLException, AlreadyBoundException,
            InterruptedException {
        LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
        Naming.bind(Server.DEFAULT_NAME, new ChatServer());

        Thread.sleep(100);

        Application.launch(View.class);
    }
}