package edu.buffalo.cse.cse486586.simpledht;

import android.net.Uri;
import android.os.AsyncTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sourabh on 3/29/18.
 * This class contains all constants needed for the program.
 * Idea is to keep constants separate so that there single configuration point for the program that can be changed in a single class
 * rather than changing through out the program. This class also contains the necessary helper function used through the execution of the test script
 */

public class Constants {

    //Starting and Ending port (Other ports are incremented by value of 4)
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";

    static final String BASE_EMULATOR_SERIAL = "5554";

    //list of all ports for all AVDs
    static final String[] PortList = {REMOTE_PORT0, REMOTE_PORT1, REMOTE_PORT2, REMOTE_PORT3, REMOTE_PORT4};

    //server port
    static final int SERVER_PORT = 10000;

    //namespace for content provider for 3
    static final String contentProviderURI = "content://edu.buffalo.cse.cse486586.simpledht.provider";

    //Content Uri corresponding to given contentProviderURI
    static final Uri CONTENT_URL = Uri.parse(contentProviderURI);

    //string constants for key and value
    static final String KEY = "key";
    static final String VALUE = "value";

    //matrix columns used
    public static String[] matrixColumns = {KEY, VALUE};

    //previous node hash value in string format
    static String previousNodeHashValue;

    //current AVD port being considered
    static String myPort;

    //global hashmap to store current node's hashed data with port, this is used to get data in provider, Ref : [http://www.baeldung.com/java-treemap]
    //TreeMap is a allNodesDetailMap implementation that keeps its entries sorted according to the natural ordering of its keys
    static TreeMap<String, String> globalNodePortHashTable;

    //nodeHashMap - used for query function
    public static HashMap<String, String> nodeHashMap;


    /*
    function that checks the emulator instance emulator-5554 should receive all new node join requests
    we should not choose a random node to receive new node join requests, and you should start the content
    provider on emulator-5554 first.
     */
    static boolean isSingleNodeInstance(String currentPort) {
        if (currentPort.equals(REMOTE_PORT0)) {
            return true;
        }
        return false;
    }


    //category string values
    static String nodeJoinCase = Category.Insert.name();

    static String relayNodeInsertion = Category.RelayNodeInsertion.name();

    static String nodeJoinRequest = Category.NodeJoin.name();


    //previous node details (with enum, ID, port)
    static String previousNodeDetails = Category.ModifyPrevious.name();
    static int previousNodeUpdateID = 1;
    static String predecessorPort;

    //next node details
    static String nextNodeDetails = Category.ModifyNext.name();
    static int nextNodeUpdateID = 2;
    static String successorPort;

    //flag for identifying both previous and next node
    static int bothNodesUpdateID = 3;


    //enum for categories
    static String getSpecificData = Category.GetSpecificData.name();
    static String getAllData = Category.GetAllData.name();
    static String relaySingleNodeCase = Category.RelaySingleNodeCase.name();
    static String stabilize = Category.Stablize.name();



    //list of all possible scenarios which can be used with a node to be considered
    static final String[] categories = {REMOTE_PORT0, REMOTE_PORT1, REMOTE_PORT2, REMOTE_PORT3, REMOTE_PORT4};


    static String nextNodeHashValue;


    //global hashmap to store node details (as key-val (nodeID-val))
    /* Advantage of using concurrent hashmap is that, we won't ge any exception while performing any nodejoin,delete, at any point of the iteration in tha algorithm
    this is not available with normal hashmap (which will return Run-time exception saying ConcurrentModificationException.)
    Reference : [https://www.geeksforgeeks.org/difference-hashmap-concurrenthashmap/] , [https://dzone.com/articles/how-concurrenthashmap-works-internally-in-java]
    Note: In multiple threaded environment HashMap is usually faster than ConcurrentHashMap, but we don't need to synchronize the whole allNodesDetailMap, that it's advantage
    Reference : [https://javahungry.blogspot.com/2014/02/hashmap-vs-concurrenthashmap-java-collections-interview-question.html]
    * */
    public static ConcurrentHashMap<String, String> allNodesDetailMap = new ConcurrentHashMap();


    //map to store node hash values
    public static String[] hashIndexes;

    /*
    *
    * I am considering 6 types of message passing based on type of message sent by client - this is the crux of the chord implementation. todo: remove unused enums (many categories are overlapping)
    * */
    public static enum Category {
        Test(0), //not used
        Insert(1), //insert query
        NodeJoinMsg(2), //join query
        NodeJoin(3), //node wants to join the CHORD ring (request)
        ModifyPrevious(4), //update previous node or predecessor
        ModifyNext(5), //update next or successor
        GetSpecificData(6), //for @ types queries (delete, insert, update)
        GetAllData(7), //for * type queries (delete, insert, update)
        PropogateGetSingleNode(8),//special case of handling single node in the CHORD ring
        PropogateMultipleNodes(9),//
        DeleteSingleNode(10),//delete single node from chord
        DeleteMultipleNodes(11),//
        RelayNodeInsertion(12), //
        RelaySingleNodeCase(13),//
        Stablize(14); //stablize the ring


        //helper function to get value for the enum
        //Ref : https://stackoverflow.com/questions/7996335/how-to-match-int-to-enum/7996473#7996473
        private int enumValue;

        Category(int Value) {
            this.enumValue = Value;
        }

        public int getValue() {
            return enumValue;
        }

    }


    /*
    * function to update previous node &  next node details
    * Reference : [https://piazza.com/class/jd0o4je7bdm1kz?cid=397]
    * */
    public static void updateNeighbourDetails(Node currentNode, int location) {

        //update previous node details
        if (location == previousNodeUpdateID) {
            Constants.previousNodeHashValue = currentNode.getPreviousNodeHashedValue();
            Constants.predecessorPort = currentNode.getPreviousNodePort();
        } //update next node details
        else if (location == nextNodeUpdateID) {
            Constants.nextNodeHashValue = currentNode.getNextNodeHashedValue();
            Constants.successorPort = currentNode.getNextNodePort();
        } else if (location == 3) { //update both
            Constants.previousNodeHashValue = currentNode.getPreviousNodeHashedValue();
            Constants.nextNodeHashValue = currentNode.getNextNodeHashedValue();
            Constants.predecessorPort = currentNode.getPreviousNodePort();
            Constants.successorPort = currentNode.getNextNodePort();
        } else {
            //ignore
        }
    }

    //function update globalHash table besed on * and @ queries
    //boolean flag variables fo distinguish * & @ cases
    public static boolean atCase = false;
    public static boolean starCase = false;
    public static void checkForSingleOrMultipleQueryType(Node currentNode, String messageCategory) {

        //update global hashtble with current node finger table
        nodeHashMap = currentNode.getFingerTable();
        if (messageCategory.equals(Constants.getSpecificData)) {
            //@ case
            starCase = true;
        } else if (messageCategory.equals(Constants.getAllData)) {
            //* case
            atCase = true;
        }
    }

    //function to return modified node which is sent to next (receive and pass on)
    public static Node relayModifiedNode(Node currentNode, String currentNodeHashValue) {
        //initialize node to be returned
        Node modifiedNode = new Node();

        //calculate index of currentNode hash value
        int currentNodeIndex = Arrays.asList(hashIndexes).indexOf(currentNodeHashValue);

        //if currentNodeIndex is at the first
        if (currentNodeIndex != hashIndexes.length - 1) switch (currentNodeIndex) {
            case 0:
                modifiedNode.setPreviousNodeHashedValue(hashIndexes[hashIndexes.length - 1]);
                modifiedNode.setNextNodeHashedValue(hashIndexes[1]);
                break;
            default:
                modifiedNode.setPreviousNodeHashedValue(hashIndexes[currentNodeIndex - 1]);
                modifiedNode.setNextNodeHashedValue(hashIndexes[currentNodeIndex + 1]);
                break;
        }
        else { // index is found for last position
            modifiedNode.setPreviousNodeHashedValue(hashIndexes[currentNodeIndex - 1]);
            modifiedNode.setNextNodeHashedValue(hashIndexes[0]);
        }

        //update category of this message
        modifiedNode.setMessageCategory(Constants.nodeJoinCase);

        //port of this modified node should be sender's port
        modifiedNode.setCurrentNodePort(currentNode.getMessageSender());


        int previousNodeHashValueIndex = Arrays.asList(hashIndexes).indexOf(modifiedNode.getPreviousNodeHashedValue());
        modifiedNode.setPreviousNodePort(Integer.toString(Integer.parseInt(Constants.globalNodePortHashTable.get(hashIndexes[previousNodeHashValueIndex])) * 2));


        int nextNodeHashValueIndex = Arrays.asList(hashIndexes).indexOf(modifiedNode.getNextNodeHashedValue());
        modifiedNode.setNextNodePort(Integer.toString(Integer.parseInt(Constants.globalNodePortHashTable.get(hashIndexes[nextNodeHashValueIndex])) * 2));

        //send modified node object to server
        new ChordClient().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, modifiedNode, null);

        return modifiedNode;

    }

    //function to create nextnode based on the information of presentNode (which is obtained from relayModifiedNode function)
    public static void updateNextNodeInformation(Node currentNode, String currentNodeHashValue, Node modifiedNode) {
        //initialize new node
        Node nextNodeInformation = new Node();

        //variable for new node index
        int newNextNodeIndex;
        newNextNodeIndex = Arrays.asList(Constants.hashIndexes).indexOf(modifiedNode.getPreviousNodeHashedValue());
        if (Arrays.asList(Constants.hashIndexes).indexOf(modifiedNode.getPreviousNodeHashedValue()) > 0) {
            nextNodeInformation.setCurrentNodePort(Integer.toString(Integer.parseInt(Constants.globalNodePortHashTable.get(Constants.hashIndexes[newNextNodeIndex])) * 2));
        }
        nextNodeInformation.setNextNodeHashedValue(currentNodeHashValue);
        nextNodeInformation.setCurrentNodePort(Integer.toString(Integer.parseInt(Constants.globalNodePortHashTable.get(Constants.hashIndexes[newNextNodeIndex])) * 2));
        nextNodeInformation.setNextNodePort(currentNode.getMessageSender());
        nextNodeInformation.setMessageCategory(Constants.nextNodeDetails);

        //send nextNode information to the server
        new ChordClient().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, nextNodeInformation, null);
    }

    //function to create prev node besed on the information of presentNode (which is obtained from relayModifiedNode function)
    public static void updatePreviousNodeInformation(Node presentNode, String currentNodeHashValue, Node modifiedNode) {
        Node previousNodeInformation = new Node();

        int newPrevNodeIndex = Arrays.asList(Constants.hashIndexes).indexOf(modifiedNode.getNextNodeHashedValue());
        previousNodeInformation.setMessageCategory(Constants.previousNodeDetails);
        previousNodeInformation.setCurrentNodePort(Integer.toString(Integer.parseInt(Constants.globalNodePortHashTable.get(Constants.hashIndexes[newPrevNodeIndex])) * 2));
        previousNodeInformation.setPreviousNodeHashedValue(currentNodeHashValue);
        previousNodeInformation.setPreviousNodePort(presentNode.getMessageSender());

        //send previousNodeInformation to client
        new ChordClient().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, previousNodeInformation, null);

    }

    //relay this message to nextnode, also updating local allNodesDetailMap of this node
    //this method inserts data into global allNodesDetailMap from
    public static synchronized void relayMessageQuery() {
        //initialize hashmap
        HashMap<String, String> localNodeMap = new HashMap();
        Iterator<Map.Entry<String, String>> iterator = allNodesDetailMap.entrySet().iterator();
        if (iterator.hasNext()) {
            do {
                Map.Entry<String, String> currentEntry = iterator.next();
                localNodeMap.put(currentEntry.getKey(), currentEntry.getValue());
            } while (iterator.hasNext());
        }
        //creating a dummy node which need to be placed & stabilized the ring as well
        Node dummyNode = new Node();

        //this dummynode should send stablise messge category with it
        dummyNode.setMessageCategory(Constants.stabilize);
        dummyNode.setMessageSender(Constants.myPort);
        dummyNode.setCurrentNodePort(Constants.successorPort);
        dummyNode.setFingerTable(localNodeMap);

        //sending dummy node
        new ChordClient().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, dummyNode, null);
    }
}
