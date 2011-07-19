/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mobdoki.server;

import java.io.PrintWriter;
import org.json.JSONObject;

/**
 *
 * @author Andreas
 */
public class JSONObj extends JSONObject {
    // status codes
    final public static int OK = 200;               // Toast message - successful
    final public static int ERROR = 400;            // Toast message - client error
    final public static int SERVER_ERROR = 500;     // Dialog message - server error
    
    // messages
    final public static String DB_ERR = "Adatbázis hiba.";
    final public static String DB_FERR = "Az adatbázis nem érhető el.";
    final public static String PAR_ERR = "Paraméterezési hiba.";
    
    // Sets the status and the message parameter of the given JSON Object
    public void setStatus (int status, String message) {
        this.put("status", status);
        this.put("message", message);
    }
        
    public void setOKMessage(String message) {
        setStatus(OK, message);
    }
    
    public void setErrorMessage(String message) {
        setStatus (ERROR, message);
    }
    
    public void setParameterError() {
        setStatus (ERROR, PAR_ERR);
    }
    
    public void setServerError() {
        setStatus(SERVER_ERROR, DB_FERR);
    }
    
    public void setDBError() {
        setStatus(SERVER_ERROR, DB_ERR);
    }
    
    public void setOK() {
        setStatus(OK, "Sikeres végrehajtás.");
    }
    
    public void write(PrintWriter out) {
        out.println(this.toString());
    }
}
