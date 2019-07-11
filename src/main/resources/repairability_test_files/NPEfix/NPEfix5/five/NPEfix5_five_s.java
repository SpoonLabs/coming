
class NPEfix {

    public static void main (String[] args) {

        hello();

        void hello(){
            String ptr2 = "hi";
            String ptr = null;

            if (ptr.equals("gfg"))
                System.out.print("Same");
            else
                System.out.print("Not Same");
        }

    }
}