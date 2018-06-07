package edu.buffalo.cse.cse486586.simpledht;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/***
 * ChordClient is an AsyncTask that should send a node over the network.
 * It is created by ChordClient.executeOnExecutor() call
 *
 * @author stevko
 *
 */
public class ChordClient extends AsyncTask<Node, Void, Void> {

    static final String TAG = ChordClient.class.getSimpleName();


    @Override
    protected Void doInBackground(Node... nodes) {

        /* Note : Below code is referenced from previous assignment with little modification to send node data to other clients
         * Objective : message communication to other clients including self
         * Algorithm :
         * 1. Fetch node data from 1st parameter of the function ( nodes[0])
         * 2. Create socket for fetched node (by calling getCurrentNodePort() function)
         * 3. Create a output stream from the socket coming as a param in AsyncTask
         * 4. Create node object output stream which can be sent as an object to other clients
         * 5. write message to buffered writer
         * 6. Flush and close Buffered writer
         * 7    . Close socket
         * Reference : [https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html]
         *           : [https://www.youtube.com/watch?v=mq-f7zPZ7b8]
         */

        //create the node to be sent
        Node nodeToSend = nodes[0];
        try {
            //getting port number of the node in integer format
            int portInt = Integer.parseInt(nodeToSend.getCurrentNodePort());

            //creating socket corresponding to current AVD port
            Socket currentSocket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), portInt);


            //create a output stream from the socket coming as a param in AsyncTask
            OutputStream outputStream = currentSocket.getOutputStream();

            //create node object output stream which can be sent as an object to other clients
            ObjectOutputStream nodeObjectOutputStream= new ObjectOutputStream(outputStream);


            //write message to buffered writer
            nodeObjectOutputStream.writeObject(nodeToSend);

            //flush & close buffered writer
            nodeObjectOutputStream.flush();
            nodeObjectOutputStream.close();

            //close the current socket
            currentSocket.close();

        } catch (UnknownHostException e) {
            Log.e(TAG, "Client Task Unknown Host Exception");
        } catch (IOException e) {
            Log.e(TAG, "Client Task Socket IOException");
        }

        return null;
    }
}