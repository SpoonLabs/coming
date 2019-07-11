package repairability_test_files.NPEfix.NPEfix4.four;

class NPEfix {

    public static void main (String[] args) {
        String ptr2 = "hi";

        hello();

        void hello(){
            String ptr = ptr2;

            if (ptr.equals("gfg"))
                System.out.print("Same");
            else
                System.out.print("Not Same");
        }

    }
}