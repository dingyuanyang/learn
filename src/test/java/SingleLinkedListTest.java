import com.dyy.study.linklist.SingleLinkedList;

public class SingleLinkedListTest {
    public static void main(String[] args) {
        SingleLinkedList list = new SingleLinkedList();
        list.addHead("A");
        list.addHead("B");
        list.addHead("C");
        list.addHead("B");
        list.addHead("D");
        list.addHead("B");
        list.addHead("C");
        list.addHead("B");
        list.addHead("A");
//        list.reversal();
        System.out.println("是否是回文："+list.isPalindrome());
    }

}
