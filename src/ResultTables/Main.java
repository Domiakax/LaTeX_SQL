package ResultTables;

public class Main {
	
	public static void main(String[] args) {
		
		String s = " sElect... LImit 1;";
		boolean b = s.matches("(?i)Select.*");
		System.out.println(b);
		
		String command = "Select... LImit 5;";
		String regEx = "(?i)limit [2-9]+[0-9]*|(?i)limit [1-9]+[0-9]+";
//		command = command.replaceFirst(regEx, "");
//		System.out.println(command);
		
		String[] array = command.split("(?i)limit ");
		for(String t: array) {
			System.out.println(t);
		}
	}

}
