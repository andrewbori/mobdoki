/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mobdoki.server.servlet.medicalinfo;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mobdoki.server.Connect;
import mobdoki.server.JSONObj;
import mobdoki.server.Sessions;
import org.json.JSONArray;
import org.json.JSONObject;
import org.postgresql.geometric.PGpoint;

/**
 *
 * @author Andreas
 */
public class GetEverything extends HttpServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        JSONObj json = new JSONObj();
        
        String SSID = request.getParameter("ssid");
        
        try {
            if (!Sessions.MySessions().isValid(SSID)) {
                json.setUnauthorizedError();
                return;
            }
            
            Class.forName(Connect.driver); //load the driver
            Connection db = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);

            if (db!=null) {
                String sqlText = "SELECT id, name, seriousness, coalesce(details,'-') FROM \"Sickness\" ORDER BY name";
                PreparedStatement ps = db.prepareStatement(sqlText);
                ResultSet results = ps.executeQuery();          // A Sickness tabla osszes sorana
                
                JSONArray sicknessArray = new JSONArray();              // JSON tomb a lekerdezett neveknek
                if (results != null) {
                    JSONObject sickness;
                    while (results.next()) {
                        sickness = new JSONObject();
                        sickness.put("id", results.getInt(1));
                        sickness.put("name", results.getString(2));
                        sickness.put("seriousness", results.getDouble(3));
                        sickness.put("details", results.getString(4));
                        
                        sicknessArray.put(sickness);
                    }
                    results.close();
                    json.put("sickness", sicknessArray);                   // betegseg tomb hozzafuzese a kimeneti JSON objektumhoz
                    json.setOK();
                } else json.setDBError();                  // adatbazis hiba
                
                sqlText = "SELECT id,name,coordinates,address,coalesce(phone,'-'),coalesce(email,'-') FROM \"Hospital\" ORDER BY name";
                ps = db.prepareStatement(sqlText);
                results = ps.executeQuery();
                
                JSONArray hospitalArray = new JSONArray();              // JSON tomb a lekerdezett neveknek
                if (results != null) {
                    JSONObject hospital;
                    while (results.next()) {
                        hospital = new JSONObject();
                        hospital.put("id",results.getInt(1));
                        hospital.put("name",results.getString(2));
                        PGpoint point = (PGpoint)results.getObject(3);
                        hospital.put("lat", point.x);
                        hospital.put("lon", point.y);
                        hospital.put("address", results.getString(4));
                        hospital.put("phone", results.getString(5));
                        hospital.put("email", results.getString(6));
                        hospitalArray.put(hospital);
                    }
                    results.close();
                    json.put("hospital", hospitalArray);                   // nevek tomb hozzafuzese az kimeneti JSON objektumhoz
                    json.setOK();
                } else json.setDBError();                  // adatbazis hiba
                
                sqlText = "SELECT id, name FROM \"Symptom\" ORDER BY name";
                ps = db.prepareStatement(sqlText);
                results = ps.executeQuery();          // A Symptom tabla osszes soranak a "name" attributuma
                
                JSONArray symptomArray = new JSONArray();              // JSON tomb a lekerdezett neveknek
                if (results != null) {
                    JSONObject symptom;
                    while (results.next()) {
                        symptom = new JSONObject();
                        symptom.put("id", results.getInt(1));
                        symptom.put("name", results.getString(2));
                        symptomArray.put(symptom);
                    }
                    results.close();
                    json.put("symptom", symptomArray);                  // tunet tomb hozzafuzese a kimeneti JSON objektumhoz
                    json.setOK();
                } else json.setDBError();                  // adatbazis hiba
                
                sqlText = "SELECT \"hospitalID\", \"sicknessID\" FROM \"Curing\"";
                ps = db.prepareStatement(sqlText);
                results = ps.executeQuery();          // A Symptom tabla osszes soranak a "name" attributuma
                
                JSONArray curingArray = new JSONArray();              // JSON tomb a lekerdezett neveknek
                if (results != null) {
                    JSONObject curing;
                    while (results.next()) {
                        curing = new JSONObject();
                        curing.put("hopsitalID", results.getInt(1));
                        curing.put("sicknessID", results.getInt(2));
                        curingArray.put(curing);
                    }
                    results.close();
                    json.put("curing", curingArray);                  // tunet tomb hozzafuzese a kimeneti JSON objektumhoz
                    json.setOK();
                } else json.setDBError();                  // adatbazis hiba
                
                sqlText = "SELECT \"sicknessID\", \"symptomID\" FROM \"Diagnosis\"";
                ps = db.prepareStatement(sqlText);
                results = ps.executeQuery();          // A Symptom tabla osszes soranak a "name" attributuma
                
                JSONArray diagnosisArray = new JSONArray();              // JSON tomb a lekerdezett neveknek
                if (results != null) {
                    JSONObject diagnosis;
                    while (results.next()) {
                        diagnosis = new JSONObject();
                        diagnosis.put("sicknessID", results.getInt(1));
                        diagnosis.put("symptomID", results.getInt(2));
                        diagnosisArray.put(diagnosis);
                    }
                    results.close();
                    json.put("diagnosis", diagnosisArray);                  // tunet tomb hozzafuzese a kimeneti JSON objektumhoz
                    json.setOK();
                } else json.setDBError();                  // adatbazis hiba
                
                db.close();
            } else json.setServerError();        // adatbazis nem erheto el
        } catch (Exception e) {
            json.setDBError();                  // adatbazis hiba
        }
        finally {
            json.write(out);
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
