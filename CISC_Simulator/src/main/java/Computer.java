

public class Computer {

	public static void main(String[] args) {
		//initialization of registers and memory
		Registers registers = new Registers();
		Memory mm = new Memory();//remember the first 6 addresses are preserved
		Instructions instruction = new Instructions();
                
                String str = "1011";
                char[] temp = str.toCharArray();
                System.out.println(str);  
                System.out.println(String.valueOf(temp));  
                
                Simulator_MainWindow window = new Simulator_MainWindow(registers, mm, instruction);
                window.show();
                
                
}