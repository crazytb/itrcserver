// TcpServerTest.java
package itrc_server;
// import static itrc_server.Itrc_server.getTime;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
 
public class Itrc_server {
    private static final String jdbcUrl = "jdbc:mysql://localhost:3307/meshnodeinfo";
    private static final String userId = "root";
    private static final String userPass = "itrc";
    public static Connection conn;
    public static Statement stmt = null;
    public static void main(String[] args) throws SQLException, IOException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(jdbcUrl, userId, userPass);
            stmt = conn.createStatement();
            System.out.println(getTime() + "MySQL 드라이버가 준비되었습니다.");
        } catch(ClassNotFoundException e) {
            System.err.println("ClassNotFoundException");
        }
        // MySQL 드라이버 구동
        
        ServerSocket serverSocket = null;
        serverSocket = new ServerSocket(5000);
        System.out.println(getTime() + "서버가 준비되었습니다.");
        // 서버소켓을 생성하고 5000번 포트와 결합시킨다.
        
        initializeTable();
        System.out.println(getTime() + "MySQL DB를 초기화하고 새로운 테이블을 생성합니다.");
                
        while (true) {
            try {
                System.out.println(getTime() + "연결요청을 기다립니다.");
                // 서버소켓은 클라이언트의 연결요청이 올 때까지 실행을 멈추고 계속 기다린다.
                // 클라이언트의 연결요청이 오면 클라이언트 소켓과 통신할 새로운 소켓을 생성한다.
                Socket socket = serverSocket.accept();
                System.out.println(getTime() + socket.getInetAddress() + "로부터 연결요청이 들어왔습니다.");
                 
                InputStream in = socket.getInputStream(); // 소켓의 입력스트림을 얻는다.
                DataInputStream dis = new DataInputStream(in); // 기본형 단위로 처리하는 보조스트림
                 
                String clientMac = dis.readUTF();
                System.out.println(getTime() + "클라이언트 MAC : \"" + clientMac + "\"");
                String clientSsid = dis.readUTF();
                System.out.println(getTime() + "클라이언트 SSID : \"" + clientSsid + "\"");
                String clientApmac = dis.readUTF();
                System.out.println(getTime() + "클라이언트 AP MAC : \"" + clientApmac + "\"");
                String clientTs = dis.readUTF();
                System.out.println(getTime() + "클라이언트 Timestamp : \"" + clientTs + "\"");
                System.out.println(getTime() + "연결을 종료합니다.");
                // 소켓으로 부터 받은 데이터를 출력한다.
                
                Meshnode nMeshnode = new Meshnode(clientMac, clientSsid, clientApmac, clientTs);
                nMeshnode.insertMeshnode();
                // MySQL DB 명령어를 수행한다.
                
                dis.close();
                socket.close();
                // 스트림과 소켓을 닫아준다.
            } catch (IOException e) {
            } // try - catch
        } // while
    } // main
     
    static String getTime() {
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss.SSS");
        return f.format(new Date());
    } // getTime
    
    static void initializeTable() throws SQLException {
        try {
            String sqlInitialize = "DROP table meshnodeinfo";
            stmt.executeUpdate(sqlInitialize); // DB 초기화
            String sqlSetup = "create table meshnodeinfo (nodemac varchar(20) not null, ssid varchar(20), apmac varchar(20), ts datetime(3), seqid int(10) auto_increment, primary key (nodemac, seqid)) engine=MyISAM";
            stmt.executeUpdate(sqlSetup); // DB 세팅
        } catch (com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException e) {
            String sqlSetup = "create table meshnodeinfo (nodemac varchar(20) not null, ssid varchar(20), apmac varchar(20), ts datetime(3), seqid int(10) auto_increment, primary key (nodemac, seqid)) engine=MyISAM";
            stmt.executeUpdate(sqlSetup); // DB 세팅
        }
    }
    
    public static class Meshnode {
        private String nodemac = null;
        private String ssid = null;
        private String apmac = null;
        private String ts = null;
//        Connection conn;
//        private Statement stmt;
        
        public Meshnode(String nNodemac, String nSSID, String nApmac, String nTs)
        {
            this.nodemac = nNodemac;
            this.ssid = nSSID;
            this.apmac = nApmac;
            this.ts = nTs;
            System.out.println("메쉬노드가 생성되었습니다.");
        }
        
//        public void showMeshnode() throws SQLException
//        {
//            String sqlSelect = "SELECT * from meshnodeinfo where nodemac='" + nodemac + "'"; // DB table 출력
//            stmt.executeQuery(sqlSelect);
//        }

        public void insertMeshnode() throws SQLException {
            String sqlInsert = "INSERT INTO meshnodeinfo (nodemac, ssid, apmac, ts) VALUES ('" + nodemac + "', '" + ssid + "', '" + apmac + "', " + ts + ")";
            stmt.executeUpdate(sqlInsert);
        }
    }
} // TcpServerTest


 
/* [결과]
20140430054501.763MySQL 드라이버가 준비되었습니다.
20140430054501.765서버가 준비되었습니다.
20140430054502.420연결요청을 기다립니다.
20140430054503.753/163.152.14.209로부터 연결요청이 들어왔습니다.
20140430054503.754클라이언트 MAC : "90:E6:BA:BA:81:9E"
20140430054503.754클라이언트 SSID : "ITRC_AP"
20140430054503.754클라이언트 AP MAC : "12:12:12:12:12:12"
20140430054503.754클라이언트 Timestamp : "20140430054503.743"
20140430054503.754연결을 종료합니다.
메쉬노드가 생성되었습니다.
20140430054503.887연결요청을 기다립니다.
 */