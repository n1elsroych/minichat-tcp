package threads;

import events.MessageReceivedEvent;
import events.ServerEventsListener;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ClientHandler extends Thread{
    private DataInputStream in;
    
    private ArrayList<ServerEventsListener> listeners;
    
    public ClientHandler(InputStream inputStream){
        in = new DataInputStream(inputStream);
        listeners = new ArrayList<>();
    }
    
    @Override
    public void run() {
        boolean isConnected = true;
        while (isConnected) {
            try {
                String message = in.readUTF();
                if (message.equals("/salir")) isConnected = false;
                triggerMessageReceivedEvent(message);
            } catch (IOException ex) {
                System.out.println(ex);
                isConnected = false;
            }
        }
    }
    
    public void addEventsListener(ServerEventsListener listener){
        listeners.add(listener);
    }
    
    public void removeMiEventoListener(ServerEventsListener listener) {
        listeners.remove(listener);
    }
    
    public void triggerMessageReceivedEvent(String message) {
        MessageReceivedEvent evt = new MessageReceivedEvent(this, message);
        for (ServerEventsListener listener : listeners) {
            listener.onReceivedMessage(evt);
        }
    }
}
