
class NPEfix {

    public static void main (String[] args) {

        hello();

        void hello(){
            String ptr2 = "hi";
            String ptr = ptr2;

            if (ptr.equals("gfg"))
                System.out.print("Same");
            else
                System.out.print("Not Same");
        }

    }
}