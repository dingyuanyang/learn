package com.dyy.study.linklist;

public class SingleLinkedList {

    private int size;

    private Node head;

    public class Node {
        private Object date;

        private Node next;

        public Node(Object date) {
            this.date = date;
        }
    }

    public SingleLinkedList() {
        this.size = 0;
        this.head = null;
    }

    /**
     * 链表头加入
     *
     * @param val
     * @return
     */
    public Object addHead(Object val) {
        Node newHead = new Node(val);
        if (size > 0) {
            newHead.next = head;
        }
        head = newHead;
        size++;
        return val;
    }

    public void addHead(Node node){
        head = node;
        size = 1;
    }

    /**
     * 删除头节点
     *
     * @return
     */
    public Object deleteHead() {
        Object headDate = head.date;
        head = head.next;
        size--;
        return headDate;
    }

    /**
     * 查找结点
     *
     * @param val
     * @return
     */
    public Node find(Object val) {
        if (size == 0) {
            return null;
        }
        Node currentNode = head;
        int tempSize = size;
        while (tempSize > 0) {
            if (currentNode.date == val) {
                return currentNode;
            } else {
                currentNode = currentNode.next;
            }
            tempSize--;
        }
        return null;
    }

    public boolean delete(Object val) {
        if (size == 0) {
            return false;
        }
        Node current = head;
        Node pre = head;
        while (current.date != val) {
            if (current.next == null) {
                return false;
            } else {
                pre = current;
                current = current.next;
            }
        }
        if (current.next == null) {
            head = null;
            size = 0;
            return true;
        }
        pre.next = current.next;
        size--;
        return true;
    }

    public boolean isEmpty() {
        return (size == 0);
    }

    public void display() {
        if (size == 0) {
            System.out.println("[]");
            return;
        }
        String result = "";
        Node node = head;
        while (node.next != null) {
            result += node.date + "->";
            node = node.next;
        }
        result += node.date;
        System.out.println("[" + result + "]");
    }

    public void reversal() {
        if (size == 0 || head.next == null) {
            return;
        }
        Node pre = null;
        Node current = head;
        while (current != null) {
            Node next = current.next;
            //当前节点指向上一个节点
            current.next = pre;
            //当前节点变更为下一次的上一个节点
            pre = current;
            //进入到下一个节点
            current = next;
        }
        //链表遍历完成以后，最后一个null节点的上一个节点就是以前的尾节点，变更为新的头节点
        head = pre;
        return;
    }

    public Node findMiddleNode() {
        if (size == 0) {
            return null;
        }
        Node fast = head;
        Node slow = head;
        while (fast.next != null && fast.next.next != null) {
            fast = fast.next.next;
            slow = slow.next;
        }
        return slow;
    }

    public boolean isPalindrome(){
        Node midNode = findMiddleNode();
        display();
        SingleLinkedList temp = new SingleLinkedList();
        temp.setHead(midNode);
        temp.setSize(1);
        temp.reversal();
        Node current = head;
        temp.display();
        Node next = temp.getHead();
        while (!current.equals(midNode)){
            if(current.next== null){
                return false;
            }
            if(next.next== null){
                return false;
            }
            if(!current.date.equals(next.date)){
                return false;
            }
            next = next.next;
            current = current.next;
        }
        return true;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Node getHead() {
        return head;
    }

    public void setHead(Node head) {
        this.head = head;
    }
}
