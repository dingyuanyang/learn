package com.dyy.study.linklist;

public class QueueLinkList {
    private DoublePointLinkedList list;

    public QueueLinkList() {
        list = new DoublePointLinkedList();
    }

    public void insert(Object val) {
        list.addTail(val);
    }

    public boolean delete() {
        return list.deleteHead();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public int getSize() {
        return list.getSize();
    }

    public void display() {
        list.display();
    }
}
