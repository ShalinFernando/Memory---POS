import static test.A.PI;

public class VarArgs {

    public static void main(String[] args) {
        printSomething("IJSE","Galle","ESOFT","NSBM","NIBM");
    }

    public static void printSomething(String... something){
        for (String s : something) {
            System.out.println(s);
        }

        System.out.println(PI);
    }

}


