import java.util.Random;


class Deep {

    public static void main(String[] args) {

        int[] array = new int[100000];
        Random random = new Random();
        array = random.ints(100000, 10, 100000).toArray();
        int a = random.nextInt();


        if (5 > array[(array[a - 8])]) {
            System.out.println("#shallow deep");
        }
    }
}