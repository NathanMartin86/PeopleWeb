import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

/**
 * Created by macbookair on 11/4/15.
 */
public class PeopleTest {
    public Connection startConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./test");
        People.createTables(conn);
        return conn;
    }
    public void endConnection(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE people");
        conn.close();
    }
    @Test
    public void testPerson() throws SQLException {
    Connection conn = startConnection();
    People.insertPerson(conn,"Nathan","Martin","nj.martin123@gmail.com","USA","12.345.678");
    Person person = People.selectPerson(conn,1);
    endConnection(conn);
        assertTrue( person!= null);

    }
    @Test
    public void testPeople()throws SQLException{
        Connection conn = startConnection();
        People.insertPerson(conn,"Bob","Saget","BS@fullhouse.net","USA","12.345.678");
        People.insertPerson(conn,"John","Stamos","JS@fullhouse.net","USA","12.345.678");
        ArrayList<Person> person = People.selectPeople(conn,1);
        endConnection(conn);

        assertTrue(person.size()!= 0);
    }
}