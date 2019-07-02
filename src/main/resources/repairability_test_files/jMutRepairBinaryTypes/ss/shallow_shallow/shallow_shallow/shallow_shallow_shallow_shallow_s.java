import java.util.Random;


class Deep {

    public static void main(String[] args) {
        Random random = new Random();
        int a = random.nextInt();
        int b = random.nextInt();

        if (a > b) {
            System.out.println("#shallow shallow");
        }
    }
}