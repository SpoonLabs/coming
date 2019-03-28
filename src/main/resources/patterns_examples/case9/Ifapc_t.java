public class Main{
    public static void main(String[] args){
        for (int i = 0; i < 100; i++){
            int v = 0;
            if (i > 1){
                v = l1.get(i - 1) + l1.get(i - 2);
            }
            System.out.println(v);
        }
    }
}