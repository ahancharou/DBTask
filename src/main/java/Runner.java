import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Runner {
    static final String DB_URL = "jdbc:mysql://localhost:3306/socium";
    static final String USER = "admin";
    static final String PASSWORD = "admin";

    static Random random = new Random();
    static String textPool ="abcdefghijklmnopqrstuwxyzABCDEFGHIJKLMNOPQRSTUWZYX";

    public static void main (String [] args) {

        Connection connection;
        Statement statement;
        PreparedStatement preparedStatement;
        try
        {
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            statement = connection.createStatement();
            //statement.execute("CREATE DATABASE socium");
            //statement.execute("DROP table likes ");
           // statement.execute("DROP table posts");
            //statement.execute("DROP table friendships");
           // statement.execute("DROP table users ");

            statement.execute("CREATE TABLE users (id INT(11) not NULL, name VARCHAR(30) not NULL, surname VARCHAR(30) not NULL, birthdate DATE NOT NULL, PRIMARY KEY (id))");
            statement.execute("CREATE TABLE posts (id INT(11) not NULL, userid int(11) not NULL, text VARCHAR(50) not NULL, timestamp TIMESTAMP, PRIMARY KEY (id), FOREIGN KEY (userid) REFERENCES users(id))");
            statement.execute("CREATE TABLE likes (postid INT(11) not null, userid int(11) not null, timestamp TIMESTAMP, FOREIGN KEY (postid) REFERENCES posts(id), FOREIGN KEY (userid) REFERENCES users(id))");
            statement.execute("CREATE TABLE friendships (userid1 INT(11) not null, userid2 int(11) not null, timestamp TIMESTAMP, FOREIGN KEY (userid1) REFERENCES users(id), FOREIGN KEY (userid2) REFERENCES users(id))");
            System.out.println("Tables created");

            //Generate users (1000)
            System.out.println("Generating users...");
            for (int i = 1; i<1010; i++){
                preparedStatement = connection.prepareStatement("INSERT INTO users(id, name, surname, birthdate) values (?,?,?,?)");
                preparedStatement.setInt(1, i);
                preparedStatement.setString(2, generateString(randBetween(5,15)));
                preparedStatement.setString(3, generateString(randBetween(8,20)));
                preparedStatement.setDate(4, generateDate());
                preparedStatement.executeUpdate();
            }
            System.out.println("table Users filled with data");
            //Generate posts (20000)
            System.out.println("Generating posts...");
            for (int i = 1; i<20010; i++){
                preparedStatement = connection.prepareStatement("INSERT INTO posts(id, userid, text, timestamp) VALUES (?,?,?,?)");
                preparedStatement.setInt(1, i);
                preparedStatement.setInt(2, randBetween(1,1000));
                preparedStatement.setString(3, generateString(randBetween(20,40)));
                preparedStatement.setTimestamp(4, generateTimestamp());
                preparedStatement.executeUpdate();
            }
            System.out.println("table posts filled with data");
            //Generate likes (30000)
            System.out.println("Generating likes...");
            for (int i = 1; i<30010; i++){
                preparedStatement = connection.prepareStatement("INSERT INTO likes(postid, userid, timestamp) VALUES (?,?,?)");
                preparedStatement.setInt(1, randBetween(1,20000));
                preparedStatement.setInt(2, randBetween(1,1000));
                preparedStatement.setTimestamp(3, generateTimestamp());
                preparedStatement.executeUpdate();
            }
            System.out.println("table likes filled with data");

            //Generate friendships (70000)
            System.out.println("Generating friendships...");
            for (int i = 1; i<70010; i++){
                preparedStatement = connection.prepareStatement("INSERT INTO friendships(userid1, userid2, timestamp) VALUES (?,?,?)");
                preparedStatement.setInt(1, randBetween(1,1000));
                preparedStatement.setInt(2, randBetween(1,1000));
                preparedStatement.setTimestamp(3, generateTimestamp());
                preparedStatement.executeUpdate();
            }
            System.out.println("table friendships filled with data");
            System.out.println("Running query...");
            ResultSet resultSet = statement.executeQuery("select distinct users.name, users.surname from users, friendships, posts, likes"
                    +" where (((friendships.userid1 = users.id) or (friendships.userid2 = users.id)) and (((users.id = posts.userid) and "
                    +"(likes.postid = posts.id) and likes.timestamp like '2015-03-%')))  having count(*) >100");

            System.out.println("Users that have > 100 friends and >100 likes in March 2015:");
            while (resultSet.next()){
                System.out.print(resultSet.getString("name")+" ");
                System.out.println(resultSet.getString("surname"));
            }
            System.out.print("End.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String generateString(int length)
    {
        char[] text = new char[length];
        for (int i = 0; i < length; i++)
        {
            text[i] = textPool.charAt(random.nextInt(textPool.length()));
        }
        return new String(text);
    }

    private static java.sql.Date generateDate (){

        Calendar gc = new GregorianCalendar();

        int year = randBetween(1980, 2010);

        gc.set(Calendar.YEAR, year);

        int dayOfYear = randBetween(1, gc.getActualMaximum(Calendar.DAY_OF_YEAR));

        gc.set(Calendar.DAY_OF_YEAR, dayOfYear);

        return new java.sql.Date(gc.getTimeInMillis());
    }

    private static java.sql.Timestamp generateTimestamp(){
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            java.util.Date date = dateFormat.parse(randBetween(1,30)+"/"+randBetween(1,12)+"/"+2015);
            return new Timestamp(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static int randBetween(int start, int end) {
        return start + (int)Math.round(Math.random() * (end - start));
    }

}
