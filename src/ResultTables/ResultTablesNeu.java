package ResultTables;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.nocrala.tools.texttablefmt.Table;

public class ResultTablesNeu {
	
	private static final String pathSQLFiles ="D:\\GitHub Repositories\\LF-8";
	private static final String db = "gm3";
	private static final String folderNameTheory = "SQLFiles_Theorie";
	private static final String folderNameAufgaben = "SQLFiles_Aufgaben";
	private static final String SQLFileNameBeginning = "sqlFile";
	private static final String SQLOutputFileNameBeginning = SQLFileNameBeginning.concat("Output");
	private static final String textAnzahlDatensätze = "Anzahl Datensätze insgesamt: ";
	
	public static void main(String[] args) {
		try {
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + db + "?useUnicode=true&characterSetResults=utf8&characterEncoding=utf8", "root", "");
			System.out.println("connected");
			File f = new File(pathSQLFiles);
			updateEveryFile(con, f);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static boolean isSQLFolder(String folderName) {
		return folderName.equals(folderNameAufgaben) 
				|| folderName.equals(folderNameTheory);
	}
	
	private static boolean isSQLTheoryFolder(String name) {
		return name.equals(folderNameTheory);
	}
	
	private static boolean isSQLAufgabenFolder(String name) {
		return name.equals(folderNameAufgaben);
	}

	private static void updateEveryFile(Connection con, File f) {
		//Mit Stream überarbeiten
		File[] files = f.listFiles();
		if(files!=null) {
			for(File fi : files) {
				if(!fi.isDirectory()) {
					continue;
				}
				
				String folderName = fi.getName();
				if(isSQLFolder(folderName)) {
					//SQLFolder gefunden
					updateSQLFolder(con, fi);
				}
				//Weitersuchen
				else {
					updateEveryFile(con, fi);
				}
			}
		}
	}

	private static void updateSQLFolder(Connection con, File folder) {
		try {
			Files.list(folder.toPath())
				.filter(x -> isSQLFile(x.getFileName().toString()))
				.forEach(x-> {
					if(isSQLTheoryFolder(folder.getName())) {
						createOutputResult(x, con, false);
					}
					else {
						createOutputResult(x, con, true);
					}
				});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void createOutputResult(Path file, Connection con, boolean isSQLAufgabenFolder) {
		StringBuilder build = new StringBuilder();
		try {
			Files.lines(file).forEach( x -> build.append(x));
			String sqlCommands = build.toString();
			sqlCommands = sqlCommands.replace("\\%", "%");
			System.out.println(sqlCommands);
			sqlCommands = sqlCommands.toLowerCase();
			boolean printDots = !(sqlCommands.contains("limit 1 ") || sqlCommands.contains("limit 1;") || !sqlCommands.contains("limit"));
			System.out.println(printDots);
			Statement st = con.createStatement();
			String[] commands = sqlCommands.split(";");
			for(String command : commands) {
				command = command.concat(";");
				int rowNumberToShow = -1;
				//Nur bei Aufgaben, mit printDots und Select Befehlen
				if(isSQLAufgabenFolder && printDots && command.startsWith("select")) {
					try {
						rowNumberToShow = Integer.parseInt(command.split("limit ")[1].replace(";", ""));
						String regEx = "limit [2-9]+[0-9]*|limit [1-9]+[0-9]+";
						command = command.replaceFirst(regEx, "");
					}
					catch(Exception e) {
						e.printStackTrace();
					}
				}
				if(st.execute(command)) {
					ResultSet result = st.getResultSet();
					Path parentFolder = file.getParent();
					printResultSetToFile(parentFolder, createOutputFileName(file.getFileName().toString()),result, printDots, rowNumberToShow);
				}
			}
			
		} catch (IOException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private static void printResultSetToFile(Path folder, String fileName, ResultSet rs, boolean printDots,
			int rowNumberToShow) {
		boolean printResultNumber = rowNumberToShow != -1;
		String fs = System.getProperty("file.separator");
		Path path = Paths.get(folder.toAbsolutePath().toString().concat(fs).concat(fileName));
		try {
			BufferedWriter writer = Files.newBufferedWriter(path);
			final int colcnt = rs.getMetaData().getColumnCount();
			final Table t = new Table(colcnt);
			for (int col = 1; col <= colcnt; col++) {
				t.addCell(rs.getMetaData().getColumnLabel(col));
			}
			while (rs.next() && rowNumberToShow !=0) {
				for (int col = 1; col <= colcnt; col++) {
					final Object o = rs.getObject(col);
					t.addCell(o == null ? "null" : o.toString());
				}
				rowNumberToShow--;
			}
			if(printDots) {
				for (int col = 1; col <= colcnt; col++) {
					t.addCell("...");
				}
				if(printResultNumber) {
					writer.write(t.render());
					rs.last();
					int number = rs.getRow();
					writer.write("\n");
					writer.write(textAnzahlDatensätze.concat("" + number));
				}
			}
			else {
				writer.write(t.render());
			}
			writer.flush();
			writer.close();
			System.out.println("hello");
		} catch (IOException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static String createOutputFileName(String input) {
		if(!isSQLFile(input)) {
			return "";
		}
		return SQLOutputFileNameBeginning.concat(input.replaceAll("\\D", "")).concat(".txt");
	}

	private static boolean isSQLFile(String fileName) {
		return fileName.matches("^".concat(SQLFileNameBeginning).concat("[1-9]+[0-9]?\\.txt$"));
	}

}
