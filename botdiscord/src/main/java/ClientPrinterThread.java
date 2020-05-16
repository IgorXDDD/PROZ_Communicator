
import Messages.serverToClient.ServerToClientMessage;
import Messages.serverToClient.ServerToClientMessageType;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Semaphore;

public class ClientPrinterThread extends Thread {
    private BufferedReader in;
    private ObjectInputStream inObject;

    ClientPrinterThread(BufferedReader in, ObjectInputStream inObject){
        this.in=in;
        this.inObject = inObject;

        mutex = new Semaphore(0);

    }

    public void initializeMessage( String text, String userId){
        this.text = text;
        this.userId = userId;
    }
    String text;
    String userId;
    TextChannel textChannel;

    public void sendEventChannel(TextChannel textChannel){
        this.textChannel = textChannel;
    }

    private ServerToClientMessage receiveMessage(){
        ServerToClientMessage message = null;

        try {
            message = (ServerToClientMessage)inObject.readObject();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return message;
    }

    static Semaphore mutex;

    public void releaseMutex(){
        mutex.release();
    }

    public void run(){
        try{
            String inputFromServer;
            ServerToClientMessage message = null;

            mutex.acquire();
            while((message = receiveMessage()) != null){
                inputFromServer = message.getText();
                if(inputFromServer.equals("") || inputFromServer.equals("\n")){
                    continue;
                }

                if(message.getType().equals(ServerToClientMessageType.IMAGE)){
                    new MessageBuilder().addAttachment(new URL(inputFromServer)).send(textChannel);
                }else{

                    textChannel.sendMessage(inputFromServer);
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
