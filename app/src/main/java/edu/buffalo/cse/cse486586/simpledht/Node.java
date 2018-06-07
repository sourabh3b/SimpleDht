package edu.buffalo.cse.cse486586.simpledht;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by sourabh on 4/4/18.
 * data structure for the chord node.  a node should store the values for all the keys for which it is responsible.
 * Objective : This structure should contain details like : type of request(node join, node leave), actual message, predecessor, successor
 * Also, a node should have information of previous(predecessor) & next(successor) node and a finger table(here I am using hashmap for the same, which stores all nodes
 * information available in the chord ring)
 * Note : To make a Node object serializable we implement the java.io.Serializable interface.
 */

public class Node implements Serializable {

    //Note: below structure is taken from previous assignment with addition fields for CHORD node details
    //Note details with previous and next nodes along with their port numbers
    String currentNodePort;//Node information corresponds to this node
    String previousNodeHashedValue; //details about previous node of current node
    String nextNodeHashedValue; //next or successor node details
    String previousNodePort;//port details of the previous & next node
    String nextNodePort; //next or successor node port
    String nodeID;//unique identification for the node


    //message detail for this node
    String messageCategory;//category of messages (i.e. possible scenarios in this assignment)
    int messageCategoryInt; //integer format of message category
    String data; //actual data of the message of the node
    int messageSequenceNumber; //sequence number in integer //todo: remove this if not used (because we re not concerned about total ordering)
    String messageSender;//message sender detail (distinguished by port)
    String messageDestination; //this variable is used to find the destination of current message being considered
    boolean isMessageDeliverable; //flag to check if message is deliverable


    //data structure to store node details for fast lookup
    private HashMap<String, String> fingerTable;//fingerTable for nodeID with the node detail, this is like a finger table which stores details about all nodes in the ring


    //empty constructor
    public Node() {
    }


    //constructor to form node object using currentNodePort,  messageCategory,  messageSender- this is
    public Node(String currentNodePort, String messageCategory, String messageSender) {
        this.currentNodePort = currentNodePort;
        this.messageCategory = messageCategory;
        this.messageSender = messageSender;
    }

    //constructor to form node object using currentNodePort,  messageCategory,  messageSender - this is used in
    public Node(String currentNodePort, String messageCategory, String nodeID, String data) {
        this.currentNodePort = currentNodePort;
        this.messageCategory = messageCategory;
        this.nodeID = nodeID;
        this.data = data;
    }

    //constructor to form node object using currentNodePort,  messageCategory,  messageSender , fingerTable- this is used in
    public Node(String messageCategory, String messageSender) {
        this.messageCategory = messageCategory;
        this.messageSender = messageSender;
    }


    public String getNodeID() {
        return nodeID;
    }

    public void setNodeID(String nodeID) {
        this.nodeID = nodeID;
    }

    public String getMessageCategory() {
        return messageCategory;
    }

    public void setMessageCategory(String messageCategory) {
        this.messageCategory = messageCategory;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getMessageSequenceNumber() {
        return messageSequenceNumber;
    }

    public void setMessageSequenceNumber(int messageSequenceNumber) {
        this.messageSequenceNumber = messageSequenceNumber;
    }

    public String getMessageSender() {
        return messageSender;
    }

    public void setMessageSender(String messageSender) {
        this.messageSender = messageSender;
    }

    public String getMessageDestination() {
        return messageDestination;
    }

    public void setMessageDestination(String messageDestination) {
        this.messageDestination = messageDestination;
    }

    public boolean isMessageDeliverable() {
        return isMessageDeliverable;
    }

    public void setMessageDeliverable(boolean messageDeliverable) {
        isMessageDeliverable = messageDeliverable;
    }

    public String getCurrentNodePort() {
        return currentNodePort;
    }

    public void setCurrentNodePort(String currentNodePort) {
        this.currentNodePort = currentNodePort;
    }

    public String getPreviousNodeHashedValue() {
        return previousNodeHashedValue;
    }

    public void setPreviousNodeHashedValue(String previousNodeHashedValue) {
        this.previousNodeHashedValue = previousNodeHashedValue;
    }

    public String getNextNodeHashedValue() {
        return nextNodeHashedValue;
    }

    public void setNextNodeHashedValue(String nextNodeHashedValue) {
        this.nextNodeHashedValue = nextNodeHashedValue;
    }

    public String getPreviousNodePort() {
        return previousNodePort;
    }

    public void setPreviousNodePort(String previousNodePort) {
        this.previousNodePort = previousNodePort;
    }

    public String getNextNodePort() {
        return nextNodePort;
    }

    public void setNextNodePort(String nextNodePort) {
        this.nextNodePort = nextNodePort;
    }

    public HashMap<String, String> getFingerTable() {
        return fingerTable;
    }

    public void setFingerTable(HashMap<String, String> fingerTable) {
        this.fingerTable = fingerTable;
    }

    @Override
    public String toString() {
        return "Node{" +
                "currentNodePort='" + currentNodePort + '\'' +
                ", previousNodeHashedValue='" + previousNodeHashedValue + '\'' +
                ", nextNodeHashedValue='" + nextNodeHashedValue + '\'' +
                ", previousNodePort='" + previousNodePort + '\'' +
                ", nextNodePort='" + nextNodePort + '\'' +
                ", nodeID='" + nodeID + '\'' +
                ", messageCategory='" + messageCategory + '\'' +
                ", messageCategoryInt=" + messageCategoryInt +
                ", data='" + data + '\'' +
                ", messageSequenceNumber=" + messageSequenceNumber +
                ", messageSender='" + messageSender + '\'' +
                ", messageDestination='" + messageDestination + '\'' +
                ", isMessageDeliverable=" + isMessageDeliverable +
                '}';
    }


}