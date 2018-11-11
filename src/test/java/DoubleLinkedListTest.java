import com.dyy.study.linklist.DoublePointLinkedList;
import com.dyy.study.linklist.SingleLinkedList;

public class DoubleLinkedListTest {
    public static void main(String[] args) {
        DoublePointLinkedList list = new DoublePointLinkedList();
        list.addTail("A");
        list.addTail("B");
        list.addTail("C");
        list.addTail("D");
        list.display();
        DoublePointLinkedList newList = list.reversal();
        newList.display();
    }
}
