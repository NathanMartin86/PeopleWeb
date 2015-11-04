import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zach on 10/19/15.
 */
public class People {

    static final int SHOW_COUNT = 20;

    static String readFile(String fileName) {
        File f = new File(fileName);
        try {
            FileReader fr = new FileReader(f);
            int fileSize = (int) f.length();
            char[] fileContent = new char[fileSize];
            fr.read(fileContent);
            return new String(fileContent);
        } catch (Exception e) {
            return null;
        }
    }

    public static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE IF EXISTS people");
        stmt.execute("CREATE TABLE people (id IDENTITY, first_name VARCHAR, last_name VARCHAR,email VARCHAR, country VARCHAR,ip VARCHAR)");
    }

    public static void insertPerson(Connection conn, String first_name, String last_name, String email, String country, String ip) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO people VALUES (NULL,?,?,?,?,?,)");
        stmt.setString(1, first_name);
        stmt.setString(2, last_name);
        stmt.setString(3, email);
        stmt.setString(4, country);
        stmt.setString(5, ip);
        stmt.execute();
    }

    public static Person selectPerson(Connection conn, int id) throws SQLException {
        Person person = new Person();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM people WHERE id = ?");
        stmt.setInt(1, id);
        ResultSet results = stmt.executeQuery();
        while (results.next()) {
            person.id = results.getInt("id");
            person.firstName = results.getString("first_name");
            person.lastName = results.getString("last_name");
            person.country = results.getString("country");
            person.email = results.getString("email");
            person.ip = results.getString("ip");
        }
        return person;
    }

    public static void populateDatabase(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        String fileContent = readFile("people.csv");
        String[] lines = fileContent.split("\n");
        for (String line : lines) {
            if (line == lines[0])
                continue;
            String[] columns = line.split(",");
            Person person = new Person(Integer.valueOf(columns[0]), columns[1], columns[2], columns[3], columns[4], columns[5]);
            PreparedStatement stmt1 = conn.prepareStatement("INSERT INTO people VALUES(NULL,?,?,?,?,?,)");
            stmt1.setString(1, columns[1]);
            stmt1.setString(2, columns[2]);
            stmt1.setString(3, columns[3]);
            stmt1.setString(4, columns[4]);
            stmt1.setString(5, columns[5]);
            stmt1.execute();
        }
    }
    public static ArrayList<Person> selectPeople(Connection conn, int offset) throws SQLException {
        ArrayList<Person> peeps = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM people LIMIT ? OFFSET?");
        stmt.setInt(1,SHOW_COUNT);
        stmt.setInt(2,offset);
        ResultSet results = stmt.executeQuery();
        while (results.next()){
            Person person = new Person();
            person.id = results.getInt("id");
            person.firstName = results.getString("first_name");
            person.lastName = results.getString("last_name");
            person.country = results.getString("country");
            person.email = results.getString("email");
            person.ip = results.getString("ip");
            peeps.add(person);
        }
            return peeps;
    }


    public static void main(String[] args) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        createTables(conn);
        populateDatabase(conn);


        Spark.get(
                "/",
            ((request, response) -> {
                String offset = request.queryParams("offset");
                int offsetNum;
                if(offset == null){
                    offsetNum = 0;
                }else{
                    offsetNum = Integer.valueOf(offset);
                }
                selectPeople(conn,offsetNum);


                HashMap m = new HashMap();
                m.put("people", selectPeople(conn,offsetNum));
                m.put("newOffset",offsetNum+SHOW_COUNT);
                m.put("oldOffset",offsetNum-SHOW_COUNT);

                return new ModelAndView(m, "people.html");
            }),
                new MustacheTemplateEngine()
        );

        Spark.get(
                "/person",
                ((request, response) -> {
                    HashMap m = new HashMap();
                    String id = request.queryParams("id");

                    try {
                        int idNum = Integer.valueOf(id);
                        Person person = selectPerson(conn,idNum);
                        m.put("person", person);
                    }catch (Exception e) {
                    }
                    return new ModelAndView(m,"person.html");
                }),
                new MustacheTemplateEngine()
        );
    }
}
