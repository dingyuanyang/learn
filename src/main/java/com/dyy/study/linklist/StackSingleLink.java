package com.dyy.study.linklist;

public class StackSingleLink {
    private SingleLinkedList list;

    public StackSingleLink() {
        this.list = new SingleLinkedList();
    }

    public void push(Object val) {
        list.addHead(val);
    }

    public Object pop() {
        Object top = list.deleteHead();
        return top;
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public void display() {
        list.display();
    }
}
