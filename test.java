import java.util.*;
import java.sql.*;


class Server{

    private final String url = "jdbc:postgresql://localhost:5432/first";
    private final String user = "postgres";  
    private final String pass = "6607";

    public Connection connect(){
        Connection conn=null;
        try {
    		
    		conn = DriverManager.getConnection(url, user, pass);
            System.out.println("Connection Sucessful..");
    		
    	}catch(Exception e){
            System.out.println("Unable to connect Server.");
    		System.out.println("Got an Error..");
    		System.out.println(e.getMessage());
        }
        return conn;

    }
}

class Location{

    public int selectLocation(Scanner sc, Connection conn) throws Exception{
        Statement st = conn.createStatement();
        ResultSet loc = st.executeQuery("Select * from location ");
        System.out.println("Choose one Theator Location.(Eg: 1)");
        int i=0, val=0;
        while (loc.next()){
            if (i==0)val=Integer.parseInt(loc.getString("L_ID"));
            System.out.println(++i+" "+loc.getString("L_Name"));
        }
        if(val!=0){
            System.out.print("\nEnter your choice:-");
            int id=sc.nextInt();

            ResultSet name = st.executeQuery("select L_Name from location where L_ID="+(id+ --val));
            name.next();
            System.out.println("\nPlease select a theatre at "+name.getString("L_Name")+":");
            
            return id+val;
        }else{
            System.out.println("Sorry, No Data Available in DataBase");
            return 0;
        }

    }

    public int selectTheator(Scanner sc, Connection conn, int L_ID) throws Exception{
        Statement st = conn.createStatement();
        ResultSet loc = st.executeQuery("select * from Theator where L_ID="+L_ID);
        int i=0,val=0;
        while (loc.next()){
            if (i==0)val=Integer.parseInt(loc.getString("T_ID"));
            System.out.println(++i+" "+loc.getString("Theator_Name"));
        }
        if(val!=0){
            System.out.print("\nEnter your choice:- ");
            int id=sc.nextInt();
            
            return id+ --val;
        }else{
            System.out.println("Sorry, No Data Available in DataBase");
            return 0;
        }
    }

}

class Theator{
    
    String show=null;
    int t_id;
    public int Booking(Scanner sc, Connection conn, int T_ID) throws Exception{
        t_id=T_ID;
        Statement st = conn.createStatement();
        String query = isDateAvl(sc, st, T_ID);
        if (query==null)
        return -1;
        ResultSet loc = st.executeQuery(query);
    
        System.out.println("\nPlease select the show timings:-");
        int i=0,val=0;
        while (loc.next()){
            if (i==0)val=Integer.parseInt(loc.getString("DT_ID"));
            System.out.println(++i+" 11:00AM "+loc.getString("mrng_show"));
            System.out.println(++i+" 2:30PM "+loc.getString("matine_show"));
            System.out.println(++i+" 6:30:PM "+loc.getString("first_show"));
            System.out.println(++i+" 10:30PM "+loc.getString("second_show")+"\n");
        }
        if(val!=0){
            int min=3, id;
            String checkAvl;
            while (true){
                System.out.print("Select a show time:- ");
                id=sc.nextInt();
                loc= st.executeQuery(query);
                loc.next();
                checkAvl = loc.getString(min+id);
                if (!checkAvl.equalsIgnoreCase("No Tickets"))
                break;
                System.out.println("\nNo seats available for selected slot. \nSelect another slot.");
            }
            show = id==1?"Mrng_Show 11:00AM":id==2?"Matine_Show 2:30PM":id==3?"First_Show 6:30PM":"Second_Show 10:30PM";
            
            loc = st.executeQuery("select * from Theator where T_ID="+T_ID);
            loc.next();
            System.out.println("\nPlease select your seat(s) for "+ show +" in "+ loc.getString("Theator_Name")+".");
            return id+val;
        }else{
            System.out.println("Sorry, No Data Available in DataBase");
            return -1;
        }

    }

    public String isDateAvl(Scanner sc, Statement st, int T_ID) throws Exception{
        ResultSet loc;
        int date,month,year;
        String query;
        while (true){
            System.out.print("\nPlease select your Date(dd) for booking(Eg: 29): ");
            date=sc.nextInt();
            System.out.print("\nPlease select your Month(MM) for booking(Eg: 05): ");
            month = sc.nextInt();
            System.out.print("\nPlease select your Year(YYYY) for booking(Eg: 2021): ");
            year=sc.nextInt();

            query="select * from datetime where t_id="+T_ID+" and date='"+year+"-"+month+"-"+date+"' " ;
            loc = st.executeQuery(query);
            if (loc.next())
            break;
            System.out.println("\nNo Shows available for your Date. \nChoose another Date slot.");
            System.out.println("1. Choose another Date slot.\n2. Exit");
            int val=sc.nextInt();
            if (val==2){
                return null;
            }
        }
        return query;
    }

    public void seatAvl(Scanner sc, Connection conn, int DT_ID) throws Exception{
        Statement st = conn.createStatement();

        String [] arr=show.split(" ");
        int num, sz, div,run;
        boolean avl;
        ResultSet loc;
        
        ArrayList<Integer> userSeats = new ArrayList<>();
        ArrayList<Integer> res;
        while (true){
            res= new ArrayList<>();
            loc = st.executeQuery("select * from seat where dt_id="+DT_ID);
            while (loc.next()){
                num = loc.getInt("seat_no");
                avl = loc.getBoolean(arr[0]);
                if (avl)
                res.add(num);
            }
            sz = res.size();
            div = (int) Math.pow(sz, 0.5) +1;
            
            Collections.sort(res);
            for(int i=0;i<sz;i++){
                if (i%div==0)
                System.out.println();
                System.out.print(res.get(i)+" ");
            }
            System.out.print("\nSelect your seat number (Eg: 1):");
            num = sc.nextInt();
            if(res.contains(num)){
                userSeats.add(num);
                updateDataBase(st, arr[0], DT_ID, num);
                System.out.println("Sucessfully selected seat number "+num+".\n1. Want to select another seat. \n2. Continue with "+userSeats.size()+" Seats.");
                run = sc.nextInt();
                if(run==2)break;
            }else{
                System.out.println("Please Enter a Valid Seat Number.");
            }
        }
        System.out.print("Your seat selection: ");
        for(int i=0;i<userSeats.size();i++)
        System.out.print(userSeats.get(i)+", ");
        System.out.println();
        
        System.out.print("Congrats !! Your ticket ");
        for(int i=0;i<userSeats.size();i++)
        System.out.print(userSeats.get(i)+", ");
        loc = st.executeQuery("select * from Theator where T_ID="+t_id);
        loc.next();
        System.out.print("has been booked in " +loc.getString("theator_name")+".");

    }

    public void updateDataBase(Statement st, String show, int DT_ID, int num) throws Exception{
        st.executeUpdate("update seat set "+show+"='false' where dt_id="+DT_ID+" and seat_no="+num);

    }

}

public class test{
 
    public static void main(String [] args) throws Exception {
        Scanner sc = new Scanner(System.in);
    	Server server = new Server();
        Connection conn = server.connect();

        while (true){
        Location l = new Location();
        int l_id = l.selectLocation(sc, conn);
        int t_id = l.selectTheator(sc, conn, l_id);

        Theator t = new Theator();
        int d_id = t.Booking(sc, conn, t_id);
        t.seatAvl(sc, conn, d_id);
        System.out.println("Want to book another time (Y or N)?");
        char c = sc.next().charAt(0);
        if (c=='N' || c=='n')
        break;
            
        }
    }
}