package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;


@RestController
public class RestaurantController {


    //Establishes connection to database
    private Connection connect() {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/postgres",
                            "postgres", "root");
            System.out.println("Opened database successfully");
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return c;
    }

    //Creates order
    @RequestMapping (value = "/Order", method = RequestMethod.POST)
    public String createOrder(@RequestBody String input) throws SQLException{
        Connection c = connect();
        Statement s = null;
        try {
            s = c.createStatement();
           List<String> list = Arrays.asList(input.split(", ")); //all data is inserted through API in thsi format
            s.executeQuery("INSERT INTO public.\"Order_T\"\n" +
                    "(\"OrderMethod\", \"OrderServiceMethod\",\"CustomerID\", \"RestaurantID\", \"StaffID\")\n" +
                    "VALUES('"+list.get(0)+"', '"+list.get(1)+"', '"+list.get(2)+"', '"+list.get(3)+"', '"+list.get(4)+"');");
        } catch (Exception e) {
            System.err.println(e.getClass().getName()+": "+e.getMessage());
        } finally {
            s.close();
            c.close();
        }
        return "Finished!";
    }

    //Shows past orders for customer
    @RequestMapping(value = "/OrderHistory/{id}", method = RequestMethod.GET)
    public String viewOrderHistory(@PathVariable("id") int id) throws SQLException {
        Connection c = connect();
        Statement s = null;
        try {
            s = c.createStatement();
            ResultSet result = s.executeQuery("SELECT * FROM public.\"Order_T\"\n"+
                    "WHERE \"CustomerID\"="+id+" ORDER BY \"OrderDate\" DESC");
            return getResults(result, "%-21s ");
        } catch (Exception e) {
            System.err.println(e.getClass().getName()+": "+e.getMessage());
        } finally {
            s.close();
            c.close();
        }
        return "Finished!";
    }

    //Shows a customer's active orders
    @RequestMapping(value = "/OrderStatus/{id}", method = RequestMethod.GET)
    public String checkOrderStatus(@PathVariable("id") int id) throws SQLException {
        Connection c = connect();
        Statement s = null;
        try {
            s = c.createStatement();
            ResultSet result = s.executeQuery("SELECT * FROM public.\"Order_T\" WHERE \"OrderStatus\" < 4\n"+
                    "AND \"CustomerID\"="+id+" ORDER BY \"OrderStatus\" DESC");
            return getResults(result, "%-21s ");
        } catch (Exception e) {
            System.err.println(e.getClass().getName()+": "+e.getMessage());
        } finally {
            s.close();
            c.close();
        }
        return "Finished!";
    }

    //Shows a restaurant's active orders
    @RequestMapping(value = "/ActiveOrders/{id}", method = RequestMethod.GET)
    public String CheckActiveOrders(@PathVariable("id") int id) throws SQLException {
        Connection c = connect();
        Statement s = null;
        try {
            s = c.createStatement();
            ResultSet result = s.executeQuery("SELECT * FROM public.\"Order_T\" WHERE \"OrderStatus\" < 4\n"+
                    "AND \"RestaurantID\"="+id+" ORDER BY \"OrderStatus\" DESC");
            return getResults(result, "%-21s ");
        } catch (Exception e) {
            System.err.println(e.getClass().getName()+": "+e.getMessage());
        } finally {
            s.close();
            c.close();
        }
        return "Finished!";
    }

    //Saves staff's clock-in time
    @RequestMapping(value = "/ClockIn", method = RequestMethod.POST)
    public String clockIn(@RequestBody String input) throws SQLException {
        Connection c = connect();
        Statement s = null;
        try {
            s = c.createStatement();
            LocalTime time = java.time.LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm:ss");
            String formattedTime = time.format(formatter);
            List<String> list = Arrays.asList(input.split(", "));
            s.executeQuery("INSERT INTO public.\"ClockIn_T\"\n" +
                    "(\"ClockInTime\", \"StaffID\", \"ShiftID\")\n" +
                    "VALUES('"+formattedTime+"', "+list.get(0)+", "+list.get(1)+");");
        } catch (Exception e) {
            System.err.println(e.getClass().getName()+": "+e.getMessage());
        } finally {
            s.close();
            c.close();
        }
        return "Finished!";
    }

    //Saves staff's clock-out time
    @RequestMapping(value = "/ClockOut", method = RequestMethod.POST)
    public String clockOut(@RequestBody String input) throws SQLException {
        Connection c = connect();
        Statement s = null;
        try {
            s = c.createStatement();
            LocalTime time = java.time.LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm:ss");
            String formattedTime = time.format(formatter);
            List<String> list = Arrays.asList(input.split(", "));
            s.executeQuery("INSERT INTO public.\"ClockOut_T\"\n" +
                    "(\"ClockOutTime\", \"StaffID\", \"ShiftID\")\n" +
                    "VALUES('"+formattedTime+"', "+list.get(0)+", "+list.get(1)+");");
        } catch (Exception e) {
            System.err.println(e.getClass().getName()+": "+e.getMessage());
        } finally {
            s.close();
            c.close();
        }
        return "Finished!";
    }

    //Updates status of order
    @RequestMapping(value = "/UpdateOrderStatus", method = RequestMethod.PUT)
    public String updateOrderStatus(@RequestBody String input) throws SQLException {
        Connection c = connect();
        Statement s = null;
        try {
            s = c.createStatement();
            List<String> list = Arrays.asList(input.split(", "));
            s.executeQuery("UPDATE public.\"Order_T\"\n" +
                    "SET \"OrderStatus\"="+list.get(1)+"\n" +
                    "WHERE \"OrderID\"="+list.get(0)+";");
        } catch (Exception e) {
            System.err.println(e.getClass().getName()+": "+e.getMessage());
        } finally {
            s.close();
            c.close();
        }
        return "Finished!";
    }

    //Returns staff's past shifts
    @RequestMapping(value = "/ShiftHistory/{id}", method = RequestMethod.GET)
    public String shiftHistory(@PathVariable("id") int id) throws SQLException {
        Connection c = connect();
        Statement s = null;
        try {
            s = c.createStatement();
            ResultSet result = s.executeQuery("SELECT \"Shift_T\".\"ShiftID\", \"Shift_T\".\"ShiftTime\""+
                    "FROM public.\"ClockOut_T\"\n"+
                    "INNER JOIN \"Shift_T\" ON \"Shift_T\".\"ShiftID\" = \"ClockOut_T\".\"ShiftID\"\n"+
                    "WHERE \"ClockOut_T\".\"StaffID\"="+id);
            return getResults(result, "%-35s ");
        } catch (Exception e) {
            System.err.println(e.getClass().getName()+": "+e.getMessage());
        } finally {
            s.close();
            c.close();
        }
        return "Finished!";
    }

    //Add's item to restaurant's menu
    @RequestMapping (value = "/AddItem", method = RequestMethod.POST)
    public String addMenuItem(@RequestBody String input) throws SQLException{
        Connection c = connect();
        Statement s = null;
        try {
            s = c.createStatement();
            List<String> list = Arrays.asList(input.split(", "));
            s.executeQuery("INSERT INTO public.\"Item_T\"\n" +
                    "(\"ItemName\", \"ItemDesc\", \"ItemPrice\", \"RestaurantID\")\n" +
                    "VALUES('"+list.get(0)+"', '"+list.get(1)+"', "+list.get(2)+", "+list.get(3)+");");
        } catch (Exception e) {
            System.err.println(e.getClass().getName()+": "+e.getMessage());
        } finally {
            s.close();
            c.close();
        }
        return "Finished!";
    }

    //Update's item on restaurants menu
    @RequestMapping (value = "/UpdateItem", method = RequestMethod.POST)
    public String updateMenuItem(@RequestBody String input) throws SQLException{
        Connection c = connect();
        Statement s = null;
        List<String> list = Arrays.asList(input.split(", "));
        try {
            s = c.createStatement();
            s.executeQuery("INSERT INTO public.\"Item_T\"\n" +
                    "(\"ItemName\", \"ItemDesc\", \"ItemPrice\", \"RestaurantID\")\n" +
                    "VALUES('"+list.get(1)+"', '"+list.get(2)+"', "+list.get(3)+", "+list.get(4)+");");
            s.executeQuery("UPDATE public.\"Item_T\"\n" +
                    "SET \"ItemActive\"=false\n" +
                    "WHERE \"ItemID\"="+list.get(0)+";\n");
        } catch (Exception e) {
            System.err.println(e.getClass().getName()+": "+e.getMessage());
        } finally {
            s.close();
            c.close();
        }

        c = connect();
        s = null;
        try {
            s = c.createStatement();
            s.executeQuery("UPDATE public.\"Item_T\"\n" +
                    "SET \"ItemActive\"=false\n" +
                    "WHERE \"ItemID\"="+list.get(0)+";\n");
        } catch (Exception e) {
            System.err.println(e.getClass().getName()+": "+e.getMessage());
        } finally {
            s.close();
            c.close();
        }
        return "Finished!";
    }

    //Removes item from restaurant's menu
    @RequestMapping (value = "/RemoveItem", method = RequestMethod.PUT)
    public String removeMenuItem(@RequestBody String input) throws SQLException{
        Connection c = connect();
        Statement s = null;
        try {
            s = c.createStatement();
            List<String> list = Arrays.asList(input.split(", "));
            s.executeQuery("UPDATE public.\"Item_T\"\n" +
                    "SET \"ItemActive\"=false\n" +
                    "WHERE \"ItemID\"="+list.get(0)+";\n");
        } catch (Exception e) {
            System.err.println(e.getClass().getName()+": "+e.getMessage());
        } finally {
            s.close();
            c.close();
        }
        return "Finished!";
    }

    //Creates new shift
    @RequestMapping (value = "/CreateShift", method = RequestMethod.POST)
    public String createShift(@RequestBody String input) throws SQLException{
        Connection c = connect();
        Statement s = null;
        try {
            s = c.createStatement();
            List<String> list = Arrays.asList(input.split(", "));
            s.executeQuery("INSERT INTO public.\"Shift_T\"\n" +
                    "(\"ShiftTime\", \"RestaurantID\")\n" +
                    "VALUES('"+list.get(0)+"', "+list.get(1)+");");
        } catch (Exception e) {
            System.err.println(e.getClass().getName()+": "+e.getMessage());
        } finally {
            s.close();
            c.close();
        }
        return "Finished!";
    }

    //Formats results of queries
    private String getResults(ResultSet resultSet, String format) throws SQLException {
        StringBuilder results = new StringBuilder();
        ResultSetMetaData metaData = resultSet.getMetaData();

        for (int i = 0; i < metaData.getColumnCount(); i++) {
            results.append(String.format(format,metaData.getColumnName(i+1)));
        }

        while (resultSet.next()) {
            for (int i = 0; i < metaData.getColumnCount(); i++) {
                results.append(String.format(format,resultSet.getString(i+1)));
            }
        }

        return results.toString();
    }

}
