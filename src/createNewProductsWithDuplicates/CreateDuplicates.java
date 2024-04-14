package createNewProductsWithDuplicates;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateDuplicates {
	private static final String db = "gm3";
	private static final String SQL_Statement_DUPLICTES = "Select * from produkt order by rand() limit 15";
	private static final String SQL_INSERT_INTO_BEGINNING = "insert into produkt(bez, vpreis, mwst, lagerbesatnd, eid, tid) values(";
	private static final String[] COLUMN_NAMES = {"bez", "vpreis", "mwst", "lagerbestand", "eid", "tid"};
	
	public static void main(String[] args) {
		try {
			File f = new File("files/createNewProductsWithDuplicates.sql");
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + db + "?useUnicode=true&characterSetResults=utf8&characterEncoding=utf8", "root", "");
			Statement st = con.createStatement();
			ResultSet result = st.executeQuery(SQL_Statement_DUPLICTES);
			StringBuilder builder = new StringBuilder();
			builder.append("(");
			while(result.next()) {
				String bez = result.getString(COLUMN_NAMES[0]);
				double vpreis = result.getDouble(COLUMN_NAMES[1]);
				double mwst = result.getDouble(COLUMN_NAMES[2]);
				int lagerbestand = result.getInt(COLUMN_NAMES[3]);
				int eid = result.getInt(COLUMN_NAMES[4]);
				int tid = result.getInt(COLUMN_NAMES[5]);
				String value = String.join(",", "'" + bez +"'", vpreis+"",mwst+"",lagerbestand+"", eid+"",tid+"");
				builder.append(value).append("),\n(");
			}
			BufferedWriter writer = Files.newBufferedWriter(f.toPath());
			writer.write(SQL_INSERT_INTO_BEGINNING);
			String s = builder.substring(0, builder.length()-3);
			s = s.concat(");");
			writer.write(s);
			writer.flush();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
