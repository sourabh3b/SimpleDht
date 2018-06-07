package edu.buffalo.cse.cse486586.simpledht;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import android.util.Log;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.database.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import android.content.*;
import android.telephony.TelephonyManager;

import java.net.ServerSocket;
import java.net.Socket;

import android.os.AsyncTask;

import java.util.*;

public class SimpleDhtProvider extends ContentProvider {

    static final String TAG = SimpleDhtProvider.class.getSimpleName();

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        /* only 3 cases
        *   - use only first 2 parameters
            - if * is given in selection parameter, then delete all key-val stored in entire DHT
            - if @ is given in selection parameter, then delete all key-val stored in current AVD
            - other
            Algorithm :
            1. remove data from global map
            2. return number of elements (although returning 1 also works)
        * */
        int numberOfElements = 0;
        if (selection.equals("*")) {
            Constants.allNodesDetailMap.remove(selection);
            numberOfElements = Constants.allNodesDetailMap.size();
        } else if (selection.equals("@")) {
            Constants.allNodesDetailMap.remove(selection);
            numberOfElements = Constants.allNodesDetailMap.size();
        } else {
            Constants.allNodesDetailMap.remove(selection);
            numberOfElements = Constants.allNodesDetailMap.size();
        }
        return numberOfElements;
    }

    @Override
    public String getType(Uri uri) {
        Log.d(TAG, "getType() is called ####### ");
        // You do not need to implement this.
        return null;
    }

    /* returns uri after inserting content values
        * Uri : Table in the provider
        * ContentValues : used to store a set of values that the ContentResolver can process.
        * */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
            /*
             * TODO: You need to implement this method. Note that values will have two columns (a key
             * column and a value column) and one row that contains the actual (key, value) pair to be
             * inserted.
             *
             * For actual storage, you can use any option. If you know how to use SQL, then you can use
             * SQLite. But this is not a requirement. You can use other storage options, such as the
             * internal storage option that we used in PA1. If you want to use that option, please
             * take a look at the code for PA1.
             * Algorithm :
             * 1. Check for empty value - return
             * 2. fetch key, value from content value
             * 3. fetch hash value corresponding to the key from content value - check exception
             * 4. check if there are no nodes in the ring
             *     4.1 If yes, then insert the current key-val into the allNodesDetailMap
             * 5. Check if there exist one node in the ring
                  * 5.1 If yes, compare current hash value with the already existing node and place it at appropriate position
             * 6. If condition 4,5 are not true, this means that there are mor than 2 nodes in the chord ring, in this case
             *    place the current key-val in approapriate position with comparision with all other nodes in the ring, this is done by
             *    propagating the message to next nodes (i.e. sending the current node details to other clients in the ring)
             * Node: there is an overlap in conditions 4,5,6  therfore, many if-else conditions can be merged into small if-else, this will reduce number of lines
             *
             *
             * Reference :
             * [0] : https://developer.android.com/training/data-storage/files.html#WriteInternalStorage
             * [1] : https://developer.android.com/reference/android/content/Context.html
             * [2] : https://developer.android.com/reference/android/content/Context.html#openFileOutput(java.lang.String, int)
             * [3] : https://www.mkyong.com/java/how-to-write-to-file-in-java-fileoutputstream-example/
             * [4] : http://www.java2s.com/Code/Android/Core-Class/ContextopenFileOutput.htm
             */

        //If empty value is coming from argument then return
        if (values.size() == 0) {
            Log.v(TAG, "Empty value coming to insert !!");
            return null;
        }

        //fetch key, value from content value
        String keyToInsert = values.get(Constants.KEY).toString();
        String valueToInsert = values.get(Constants.VALUE).toString();

        //fetch hash value corresponding to the key from content value - check exception
        String hashValueForKey = null;
        String currentNodeHashValue = null;
        try {
            hashValueForKey = genHash(keyToInsert);
            currentNodeHashValue = genHash(Integer.toString(Integer.parseInt(Constants.myPort) / 2));
        } catch (Exception e) {
            Log.v(TAG, "genHash error !!");
        }

            /*check if there are no nodes in the ring, this is compared by checking current node hash value with previousNodeHashValue
            This is done by comparing the previous node hash value with current node hash value and put key-val to the global allNodesDetailMap
            If there exist more nodes, then create a node corresponding to the key-val and relay this node information to next route
            */
        if (Constants.previousNodeHashValue.compareTo(currentNodeHashValue) > 0) {

            //check if there are no nodes in the ring, this is compared by checking current node hash value with previousNodeHashValue
            //This is done by comparing the previous node hash value with current node hash value and put key-val to the global allNodesDetailMap OR checking if current hash value is less then previous node
            if ((hashValueForKey.compareTo(currentNodeHashValue) > 0 && hashValueForKey.compareTo(Constants.previousNodeHashValue) > 0) || (hashValueForKey.compareTo(currentNodeHashValue) <= 0 && hashValueForKey.compareTo(Constants.previousNodeHashValue) < 0)) {
                Constants.allNodesDetailMap.put(keyToInsert, valueToInsert);
            } else {
                //If there exist more nodes, then create a node (using appropriate constructor) corresponding to the key-val and relay this node information to next route
                //assigning currentNode port to successor port, key as nodeID, value as the current incoming value, and category as relayNode
                Node newIncomingNode = new Node(Constants.successorPort, Constants.relayNodeInsertion, keyToInsert, valueToInsert);

                //relay this message to other nodes (i.e. sending node information to other chord clients)
                new ChordClient().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, newIncomingNode, null);
            }

            //check if there exist already single node existing in the ring, in this case put the current node's details into global allNodesDetailMap
        } else {
            if (hashValueForKey.compareTo(Constants.previousNodeHashValue) > 0 && hashValueForKey.compareTo(currentNodeHashValue) <= 0 || currentNodeHashValue.equals(Constants.previousNodeHashValue)) {
                Constants.allNodesDetailMap.put(keyToInsert, valueToInsert);


                //relay this message to other nodes (i.e. sending node information to other chord clients)
            } else {
                Node newIncomingNode = new Node(Constants.successorPort, Constants.relayNodeInsertion, keyToInsert, valueToInsert);
                new ChordClient().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, newIncomingNode, null);
            }
        }
        return null;
    }

    /*
       *Returns a cursor that provides read access to the nodeHashMap query
       *Uri : Table in the provider
       *projection : array of string which corresponds to the column which is used to retreive each row
       *selection : element of query selection
       *selectionArgs : argument
       *sortOrder : ordering
       * */

    //this function is invoked when activity is started.
    @Override
    public boolean onCreate() {

        //Note: Below code is referenced from previous assignment
         /*
         * Calculate the port number that this AVD listens on. (Referenced from PA1)
         * It is just a hack that I came up with to get around the networking limitations of AVDs.
         * The explanation is provided in the PA1 spec.
         * Algorithm :
         * 1. Calculate the port number that this AVD listens on
         * 2. Generate nodeID using genHash
         * 3. Start server (Corner case : Handle 1 node)
         */
        //Ref : [https://stackoverflow.com/questions/11327876/how-to-get-getsystemservice-using-myconext] Note previous approach wasn't working
        TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        Constants.myPort = String.valueOf((Integer.parseInt(portStr) * 2)); //keeping this global in order to reuse later

        Log.d(TAG, "Current port is >>>>>>>>" + Constants.myPort);

        //generating hash function for the current port
        String currentNodeID = null;
        Constants.globalNodePortHashTable = new TreeMap();
        try {
            currentNodeID = genHash(Integer.toString(Integer.parseInt(Constants.myPort) / 2));
        } catch (Exception e) {
            Log.d(TAG, "Cannot convert port with genHash function");
        }
        //copy current node to previous next node ID because this is just single node (server)
        singleNodePreviousNextHashAdjust(currentNodeID);
        singleNodePreviousNextPortAdjust(Constants.myPort);

        //todo : since CHORD deals with hashed keys, we need to hash myPort and get the nodeID corresponding to the AVD device
        //Creating Server Socket (Referenced from PA1)
        try {
         /*
         * Create a server socket as well as a thread (AsyncTask) that listens on the server
         * port.
         * AsyncTask is a simplified thread construct that Android provides.
         * http://developer.android.com/reference/android/os/AsyncTask.html
         */
            ServerSocket serverSocket = new ServerSocket(Constants.SERVER_PORT);
            new ChordServer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.e(TAG, "Can't create a ServerSocket");
            return false;
        }

        //base hash value for the port 5554
        String baseHashValue = null;

        //if current port is not 5554 then this node is to be added into chord ring, send node details to the client
        if (!Constants.isSingleNodeInstance(Constants.myPort)) {
            //create a node object and fill the details (i.e type of request category, current port and sender of this request)
            Node joiningRequestNode = new Node(Constants.REMOTE_PORT0, Constants.nodeJoinRequest, Constants.myPort);
            new ChordClient().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, joiningRequestNode, null);
        }
        //else this is a single node instance (the case of emulator-5554) ( single node instance)
        try {
            baseHashValue = genHash(Constants.BASE_EMULATOR_SERIAL);
            //insert this hashed data to global hashmap (with key as hash value, value the emulator serialID) for referencing
            Constants.globalNodePortHashTable.put(baseHashValue, Constants.BASE_EMULATOR_SERIAL);
        } catch (Exception e) {
            Log.e(TAG, "genHash exception");
            return false;
        }
        return true;

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }


    /*
        *Returns a cursor that provides read access to the nodeHashMap query
        *Uri : Table in the provider
        *projection : array of string which corresponds to the column which is used to retreive each row
        *selection : element of query selection
        *selectionArgs : argument
        *sortOrder : ordering
        * */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        //initializing emptyMatrixCursor (which will be filled based on the selection value)
        MatrixCursor emptyMatrixCursor = new MatrixCursor(Constants.matrixColumns);

        /*check for @ in selection,  if @ is given in selection parameter, then return all key-val stored in current AVD
            Algorithm :
            1. Iterate global allNodesDetailMap until the end
            2. at each iteration add current entry as a row
         */
        Log.d(TAG, "selection >>>>>>" + selection);

        if (selection.equals("@")) {
            //initialize the iterator for allNodesDetailMap
            Iterator<Map.Entry<String, String>> iterator = Constants.allNodesDetailMap.entrySet().iterator();
            if (iterator.hasNext()) {
                do {
                    Map.Entry<String, String> currentEntry = iterator.next();

                    //at each iteration add current entry as a row
                    emptyMatrixCursor.addRow(new String[]{currentEntry.getKey(), currentEntry.getValue()});
                } while (iterator.hasNext());
            }
        } else if (selection.equals("*")) {
            /*
            * check for * in selection,  if * is given in selection parameter, then return all key-val stored in entire DHT
            * Algorithm :
            * 1. Generate hash
            * 2. check if single node is present in the system
            *  2.1 iterate and add row to matrix cursor
            * 3. Else,
            *   3.1 Ger start index (which will act as entry for this key-val pair
            *   3.2 Create a dummyNode which needs to be propogated to the server
            *   3.3 IF there are any concurrent request, then listen those and update matrixcursor
            *   3.4 Return cursor
            * */
            String ownHash = null;
            try {
                ownHash = genHash(Integer.toString(Integer.parseInt(Constants.myPort) / 2));
            } catch (Exception e) {

            }
            //check for single node present in the system
            if (Constants.previousNodeHashValue.compareTo(ownHash) != 0) {

                Constants.relayMessageQuery();

                //infinite loop until a ping message comes for this process
                do if (Constants.atCase) {
                    Constants.atCase = false;
                    break;
                } while (true);

                //as soon as a ping message is received matrix cursor needs to be modifies
                // for this a temporary matrix variable is creates which contains the data already existing int eh global nodeHashMap
                MatrixCursor tempMatrixCursor = new MatrixCursor(Constants.matrixColumns);

                Iterator<Map.Entry<String, String>> iterator = Constants.nodeHashMap.entrySet().iterator();
                if (iterator.hasNext()) {
                    do {
                        Map.Entry<String, String> currentMapEntry = iterator.next();
                        tempMatrixCursor.addRow(new Object[]{currentMapEntry.getKey(), currentMapEntry.getValue()});
                    } while (iterator.hasNext());
                }

                //copy tempMatrixCursor to curreent matrix cursor which is to be returned
                emptyMatrixCursor = tempMatrixCursor;

            } else {
                //If there's a node present in the ring
                //iterate the global allNodesDetailMap and add the rows in the matrix cursor
                Iterator<Map.Entry<String, String>> iterator = Constants.allNodesDetailMap.entrySet().iterator();
                if (iterator.hasNext()) {
                    do {
                        Map.Entry<String, String> currentEntry = iterator.next();
                        //add current entry in the ring
                        emptyMatrixCursor.addRow(new String[]{currentEntry.getKey(), currentEntry.getValue()});
                    } while (iterator.hasNext());
                }
            }
        } else {
            //else return the data based on the selection input parameter
            //check is current selection has an entry in the allNodesDetailMap, if it exist, then add this to matrix cursor, else forward detail to next node
            if (Constants.allNodesDetailMap.get(selection) != null) {
                String value = Constants.allNodesDetailMap.get(selection);
                emptyMatrixCursor.addRow(new String[]{selection, Constants.allNodesDetailMap.get(selection)});
            } else {

                //propogate this node detail to server
                Node tempNode = new Node();

                //sinnce this is single node, relay with single node case is considered (we can also use Stablize category here)
                tempNode.setMessageCategory(Constants.relaySingleNodeCase);
                tempNode.setNodeID(selection);
                tempNode.setMessageSender(Constants.myPort);
                tempNode.setMessageDestination(Constants.myPort);
                tempNode.setCurrentNodePort(Constants.successorPort);
                new ChordClient().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, tempNode, null);


                //receive message for all nodes
                do if (Constants.starCase) {
                    Constants.starCase = false;
                    break;
                } while (true);

                //as soon reply is received insert message to matrix cursor and return
                String selectionValue = Constants.nodeHashMap.get(selection);
                MatrixCursor matrixCursor = new MatrixCursor(Constants.matrixColumns);

                matrixCursor.addRow(new Object[]{selection, selectionValue});
                emptyMatrixCursor = matrixCursor;
            }
        }
        return emptyMatrixCursor;
    }


    //this function handles single node present in the chord ring
    void singleNodePreviousNextHashAdjust(String currentNodeID) {
        Constants.previousNodeHashValue = currentNodeID;
        Constants.nextNodeHashValue = currentNodeID;
    }

    //function to handle single node and copies successor and predecessor node port to current port (because there is only 1 node)
    void singleNodePreviousNextPortAdjust(String currentPort) {
        Constants.predecessorPort = currentPort;
        Constants.successorPort = currentPort;
    }


    //function to check message category for the node
     /*
            check message category and take appropriate action for this node
            There are only 6 scenarios :
            1. This is 1st node
                1.1 form ring with this node
            2. There already exist X nodes in the chord
                2.1 if X == 1
                    then
                2.3 if X > 1
                    then
            3. Update previous node
            4. Update next node
            5. Request for relay this node (Similar to step 2.3)
             */
    void checkCategory(String messageCategory, Node currentNode) {

        //fetch nodeID for this node
        String currentNodeID = currentNode.getNodeID();

            /*
            * Algorithms description
            *
            * ///////Relay node - in this category, a node should be propogated to next node (This is done by calling insert functino)
            * 1. Create content value (contentValues)
            * 2. put the node details into contentValues
             *  2.1 put nodeID as key
             *  2.2 put nodeData as value
            * 3. call insert function for the contentValues creates in step 2
            *
            *
            * ///////Single Node case
            * 1. check if currentNodeID already exist in the hash table
            * 2. If exist, then put this node to the ring and relay this node information
            * 3. Else, modify existing node with currentNode information and rely this information
            *
            *
            *
            * /////// join ring request - in this category, a node want to join the ring (assuming this is not the first node in the system)
            * 1. fetch message sender details from the node
            * 2. Generate hash for this node which needs to be inserted into the ring
            * 3. Informa
            *
            *
            * ///////For New Node joining
            * 1. fetch message sender details from the node
            * 2. Generate hash for this node which needs to be inserted into the ring
                * 2.1 put this node into the globalNodePort hash table
            * 3. send this node information to the server
            * 4. update server about the previous node (predecessor)
            * 5. update server about the next node (successor node)
            *
            *
            * ////////Single Node Case
            * 1. just add to hash allNodesDetailMap
            * 2. modify previous, next node
            * 3. save data into content provider
            *
            * ////Any node join request - we need to maintain ring structure
            * 1. Relay this modified node which is to be inserted into chord (while keeping information of current inserted available in hash table)
            * 2. Relay previous node which is to be inserted into chord
            * 3. Relay next  node which is to be inserted into chord
            * */


        //check if message category is for single or multiple nodes (i.e * or @ query types)
        Constants.checkForSingleOrMultipleQueryType(currentNode, messageCategory);


        /*check for single node case
            If nodeID is not found in global hashmap, then this node is not present in the ring, and place this node to
            the ring, and set CurrentNodePort with successor port

         */
        if (messageCategory.equals(Constants.relaySingleNodeCase)) {
            //check allNodesDetailMap if the currentNodeID is present of not
            boolean doesNodeExist = Constants.allNodesDetailMap.get(currentNodeID) == null;

            //if yes, set CurrentNodePort for next node
            if (doesNodeExist) {
                currentNode.setCurrentNodePort(Constants.successorPort);
                new ChordClient().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, currentNode, null);
            } else {
                //modify node details and broadcase this modifies node to previous node (who sent this request)
                String value = Constants.allNodesDetailMap.get(currentNodeID);

                HashMap<String, String> localHashMap = new HashMap();

                localHashMap.put(currentNodeID, value);

                Node modifiedNode = new Node();
                //Node modifiedNode = new Node(messageCategory,Constants.myPort);

                //modify the message details associated with this node
                modifiedNode.setMessageSender(Constants.myPort);
                modifiedNode.setMessageCategory(Constants.getSpecificData);

                //update the finger table (which now contains this modifed node)
                modifiedNode.setFingerTable(localHashMap);
                modifiedNode.setCurrentNodePort(currentNode.getMessageDestination());

                //relay information to client with the information of this node
                new ChordClient().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, modifiedNode, null);
            }
        }

         /*check if node wants to relay the currentNode to the next node
           here we put node and data in content value
         */
        if (messageCategory.equals(Constants.relayNodeInsertion)) {
            //Initialize new content value
            ContentValues contentValues = new ContentValues();

            //Put nodeID as key into content value
            contentValues.put(Constants.KEY, currentNode.getNodeID());

            //Put content value as received node data
            contentValues.put(Constants.VALUE, currentNode.getData());

            //Insert values to content resolver
            insert(Constants.CONTENT_URL, contentValues);
        }


        /*
        check if node wants to join the ring, handle case where node's previous, next nodes are also updates i.e stabilizes the ring)
         */
        if (messageCategory.equals(Constants.nodeJoinRequest)) {
            String currentNodeHashValue = null;
            int hashIndexLocation = 0;
            try {
                currentNodeHashValue = genHash(Integer.toString(Integer.parseInt(currentNode.getMessageSender()) / 2));
            } catch (Exception e) {
                Log.d(TAG, "genHash error");
            }

            //getting message sender details, because this will be used to copy to the port corresponding the hash value of this node
            String currentMessageSender = currentNode.getMessageSender();
            String modifiedPort = Integer.toString(Integer.parseInt(currentMessageSender) / 2);

            //insert this updated key-val into global hash table
            Constants.globalNodePortHashTable.put(currentNodeHashValue, modifiedPort);
            Constants.hashIndexes = new String[Constants.globalNodePortHashTable.size()];

            //push the index of current node into hashIndex array for next node lookup
            Iterator<String> iterator = Constants.globalNodePortHashTable.keySet().iterator();
            if (iterator.hasNext()) {
                do {
                    String key = iterator.next();
                    Constants.hashIndexes[hashIndexLocation++] = key;
                } while (iterator.hasNext());
            }

            //todo: need information about previous, current, next node - create function to update these 3 nodes

            //get modifies node as a nodeHashMap of relaying current node
            //function called for relay this modified node which is to be inserted into chord
            Node modifiedNode = Constants.relayModifiedNode(currentNode, currentNodeHashValue);

            //send previous node to the client (along with updation of previouis node)
            Constants.updatePreviousNodeInformation(currentNode, currentNodeHashValue, modifiedNode);

            //send next node information to the server which is updated as modifiedNode is done in previous step
            Constants.updateNextNodeInformation(currentNode, currentNodeHashValue, modifiedNode);

        }

        /*check if category is wants to join
        before inserting the node into ring, it is important to update the previous and next nodes
        so that ring is maintained as per CHORD, in this case update both previous and next node details by currentNode
        */
        if (messageCategory.equals(Constants.nodeJoinCase)) {
            Constants.updateNeighbourDetails(currentNode, Constants.bothNodesUpdateID);
        }

        //check if node wants to update next node
        if (messageCategory.equals(Constants.nextNodeDetails)) {
            Constants.updateNeighbourDetails(currentNode, Constants.nextNodeUpdateID);
        }

        //check if catagory is to update previous node
        if (messageCategory.equals(Constants.previousNodeDetails)) {
            Constants.updateNeighbourDetails(currentNode, Constants.previousNodeUpdateID);
        }


        /*
        Check for refreshing finger table entries, withe the current node's fhash table
        Reference : [https://www.slideshare.net/paulyang0125/paul-present-chord-v1]
         */
        if (messageCategory.equals(Constants.stabilize)) {

            //get currentNode local allNodesDetailMap
            HashMap<String, String> currentNodeLocalMap = currentNode.getFingerTable();

            Iterator<Map.Entry<String, String>> iterator = Constants.allNodesDetailMap.entrySet().iterator();
            if (iterator.hasNext()) {
                do {
                    Map.Entry<String, String> me = iterator.next();
                    currentNodeLocalMap.put(me.getKey(), me.getValue());
                } while (iterator.hasNext());
            }

            //check if current nodes's port is not equal the sender's port
            //if yes, then all entries from the current allNodesDetailMap need to be inserted to currentNode's local allNodesDetailMap,
            //which is sent to next successor node(by calling client and sending the node details with it)
            if (!currentNode.getCurrentNodePort().equals(currentNode.getMessageSender())) {
                currentNode.setCurrentNodePort(Constants.successorPort);
                currentNode.setFingerTable(currentNodeLocalMap);

            } else {
                //else we need to inform all nodes in the ring about this node
                currentNode.setMessageCategory(Constants.getAllData);
                currentNode.setFingerTable(currentNodeLocalMap);
            }

            //send the current nodeinfo to next successor node(by calling client and sending the node details with it)
            new ChordClient().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, currentNode, null);
        }

    }


    /*
    * Since we are using SHA-1 as the hash function to generate IDs for the DHT.
    * this function is used to generate the keys
    * */
    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }


    /***
     * ChordServer is an AsyncTask that should handle incoming messages. It is created by
     * ChordServer.executeOnExecutor() call in SimpleMessengerActivity.
     *
     * Please make sure you understand how AsyncTask works by reading
     * http://developer.android.com/reference/android/os/AsyncTask.html
     *
     * @author stevko
     *
     */
    private class ChordServer extends AsyncTask<ServerSocket, Node, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            /*
            * Note : Below code is referenced from PA1 with slight modification to get node object from socket
            Algorithm :
            * 0. In order to continue accepting more connections, use infinite while loop
            * 1. Listen for a connection to be made to the socket coming  as a param in AsyncTask and accepts it. [ Reference : https://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html]
            * 2. Create InputStream form incoming socket
            * 3. To send message to UI thread, call onProgressUpdate with bufferReader.readLine() string value (which runs on UI thread as a nodeHashMap of calling this function)
            * */
            try {

                //this is done to keep reading multiple messages (although grader gives 5 points without this, but is a good practice for a socket to accept client's connection infinitely)
                //at least one time send & receive message
                do {
                    //server is ready to accept data starting
                    Socket socket = serverSocket.accept();

                    //Basic Stream flow in Java : InputStream -> InputStreamReader -> BufferReader -> Java Program [ Reference : https://www.youtube.com/watch?v=mq-f7zPZ7b8  ; https://www.youtube.com/watch?v=BSyTJSbNPdc]
                    //taking Objectinput from socket as a stream
                    ObjectInputStream inputStreamFromSocket = new ObjectInputStream(socket.getInputStream());

                    //creating buffer reader from inputStreamFromSocket (combining InputStreamReader -> BufferReader flow in one statement)
                    // BufferedReader bufferReader = new BufferedReader(new InputStreamReader(inputStreamFromSocket));
                    Node nodeObject = null;
                    try {
                        //getting node object from input stream from socket
                        nodeObject = (Node) inputStreamFromSocket.readObject();
                    } catch (Exception e) {

                    }

                    //This is invoked in doBackground() to send message to UI thread to call onProgressUpdate (which runs on UI thread as a nodeHashMap of this function calling)
                    //publishing progress with bufferReader.readline() - which returns a line of String which has been read by bufferReader
                    publishProgress(nodeObject);

                } while (true);
            } catch (IOException e) {
                Log.e(TAG, "Message receive exception");
            }

            return null;
        }

        protected void onProgressUpdate(Node... node) {
            Node currentNode = node[0];
            String messageCategory = currentNode.getMessageCategory();

            Log.d(TAG, "message category >>>>>" + messageCategory);

            /*
            check message category and take appropriate action for this node
            There are only 8 scenarios : - these cases re handled in checkCategory function
            1. This is 1st node
                1.1 form ring with this node
            2. There already exist X nodes in the chord
                2.1 if X == 1
                    then
                2.3 if X > 1
                    then
            3. Update previous node
            4. Update next node
            5. Request for relay this node (Similar to step 2.3)
            6. relay single node case
            7. * case - return all
            8. @ case - return current
             */
            checkCategory(messageCategory, currentNode);

        }
    }
}

