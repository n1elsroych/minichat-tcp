package threads;

import entities.Client;
import events.ClientDisconnectedEvent;
import events.ServerEventsListener;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

public class DisconnectionsHandler extends Thread{
    Map<Integer, Client> clients;
    
    private ArrayList<ServerEventsListener> listeners;
    
    public DisconnectionsHandler(Map<Integer, Client> clients){
        this.clients = clients;
        listeners = new ArrayList<>();
    }
    
    @Override
    public void run() {
        while(true) {
            
            for (Map.Entry<Integer, Client> entry : clients.entrySet()){
                Client client = entry.getValue();
                Socket clientSocket = client.getSocket();
                if (!clientSocket.isConnected()){
                    try {
                        clientSocket.close();
                        System.out.println("El cliente "+client.getNickname()+" con ID = "+client.getId()+" ya no esta conectado");
                        triggerClientDisconnectedEvent(client.getId());
                    } catch (IOException ex) {
                        System.out.println(ex);
                    }
                }
            }
        }
    }
    
    public void addEventsListener(ServerEventsListener listener){
        listeners.add(listener);
    }
    
    public void removeMiEventoListener(ServerEventsListener listener) {
        listeners.remove(listener);
    }
    
    public void triggerClientDisconnectedEvent(int id) {
        ClientDisconnectedEvent evt = new ClientDisconnectedEvent(this, id);
        for (ServerEventsListener listener : listeners) {
            listener.onClientDisconnected(evt);
        }
    }
    
}
