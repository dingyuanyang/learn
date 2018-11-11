package com.dyy.study.linklist;

public class DoublePointLinkedList {
    private Node head;

    private Node tail;

    private int size;

    private class Node {
        private Object date;
        private Node next;

        public Node(Object date) {
            this.date = date;
        }
    }

    public DoublePointLinkedList() {
        head = null;
        tail = null;
        size = 0;
    }

    public void addHead(Object val) {
        Node node = new Node(val);
        if (size == 0) {
            head = node;
            tail = node;
        } else {
            node.next = head;
            head = node;
        }
        size++;
    }

    public void addTail(Object val){
        Node node = new Node(val);
        if(size == 0){
            head = node;
            tail = node;
        }else{
            tail.next = node;
            tail = node;
        }
        size++;
    }

    public boolean deleteHead() {
        if (size == 0) {
            return false;
        }
        if (head.next == null) {
            head = null;
            tail = null;
        } else {
            head = head.next;
        }
        size--;
        return true;
    }

    public boolean isEmpty() {
        return (size == 0);
    }

    public int getSize() {
        return size;
    }

    public void display() {
        if (size == 0) {
            System.out.println("[]");
            return;
        }
        Node node = head;
        String result = "";
        while (node.next != null) {
            result += node.date + "->";
            node = node.next;
        }
        result += node.date;
        System.out.println("[" + result + "]");
    }

    public DoublePointLinkedList reversal(){
        DoublePointLinkedList newList = new DoublePointLinkedList();
        if(size == 0){
            return newList;
        }
        if(head.next==null){
            newList.head = head;
            newList.tail = tail;
        }
        Node node = head;
        while (node.next != null){
            newList.addHead(node.date);
            node = node.next;
        }
        newList.addHead(node.date);
        return newList;
    }
}
