/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mobdoki.server;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author Andreas
 */
public class Sessions {
    private volatile static Sessions mySessions;        // singleton instance
    private volatile HashMap<String,SessionInfo> sessions;       // felhasznaloi munkamenetek
    
    // singleton instance lekerese
    synchronized public static Sessions MySessions() {
       if (mySessions==null) mySessions = new Sessions();
       return mySessions;
    }
    // Sessions (singleton osztaly) protected konstruktora
    protected Sessions () {
        sessions = new HashMap<String,SessionInfo>();
    }
    
    // Uj felhasznaloi munkamenet felvetele
    synchronized public boolean addSession (String SSID0, int id0, String username0, String usertype0, Date validTime0) {        
        if (sessions.containsKey(SSID0)) return false;
        Collection<SessionInfo> temp = sessions.values();
        
        for (SessionInfo ss : temp) {
            if (ss.equalsUN(username0)) removeSession(ss.getSSID());
        }
        
        sessions.put(SSID0, new SessionInfo(SSID0, id0, username0, usertype0, validTime0));
        return true;
    }
    
    // Felhasznaloi munkamenet torlese
    synchronized public void removeSession (String SSID0) {        
        sessions.remove(SSID0);
    }
    
    // Az adott SSID-ju felhasznaloneve az alabbi?
    synchronized public boolean isUserName(String SSID0, String username0) {
        if (sessions.containsKey(SSID0) && sessions.get(SSID0).equalsUN(username0)) return true;
        
        return false;
    }
    
    // Az adott SSID-ju felhasznalo felhasznaloneve
    synchronized public int getUserID(String SSID0) {
        if (sessions.containsKey(SSID0)) return sessions.get(SSID0).id;
        
        return 0;
    }
    
    // Az adott SSID-ju felhasznalo orvos?
    synchronized public boolean isDoctor(String SSID0) {
        if (sessions.containsKey(SSID0) && sessions.get(SSID0).isDoctor()) return true;
        
        return false;
    }
    
    // Az adott SSID-ju felhasznalo paciens?
    synchronized public boolean isPatient(String SSID0) {
        if (sessions.containsKey(SSID0) && sessions.get(SSID0).isPatient()) return true;
        
        else return false;
    }
    
    // Az adott SSID ervenyes?
    synchronized public boolean isValid(String SSID0) {
        if (sessions.containsKey(SSID0) && sessions.get(SSID0).isValid()) return true;

        return false;
    }
    
    synchronized public boolean makeItValid(String SSID0) {
        if (sessions.containsKey(SSID0)) {
            sessions.get(SSID0).makeItValid();
            return true;
        }
        return false;   
    }
    
    synchronized public boolean isDoctorAndValid(String SSID0){
        if (isDoctor(SSID0)) {          // Ha orvos
            if (!isValid(SSID0)) {          // Ha nem ervenyes, akkor meghosszabbit   
                return false;
            }
            return true;
        }
        return false;
    }
    
    synchronized public boolean isPatientAndValid(String SSID0, JSONObj json){
        if (isPatient(SSID0)) {          // Ha paciens
            if (!isValid(SSID0)) {          // Ha nem ervenyes, akkor meghosszabbit   
                sessions.get(SSID0).makeItValid();                  // 30 perces munkamenet
            }
            return true;
        }
        json.setErrorMessage("A művelethez nincs jogosultsága.");
        return false;
    }
    
    // Felhasznaloi munkamenet
    class SessionInfo {
        private String SSID;
        private int id;
        private String username;
        private String usertype;
        private Date validTime;
        
        SessionInfo (String SSID0, int id0, String username0, String usertype0, Date validTime0){
            SSID=SSID0;
            id=id0;
            username=username0;
            usertype=usertype0;
            validTime=validTime0;
        }
        
        boolean isDoctor() {
            return usertype.equals("doctor");
        }
        boolean isPatient() {
            return usertype.equals("patient");
        }
        boolean isValid() {
            Date date = new Date();
            return validTime.after(date);
        }
        
        boolean equalsUN (String username0) {
            return username.equals(username0);
        }
        
        boolean equalsSSID (String SSID0) {
            return SSID.equals(SSID0);
        }
        
        String getSSID() {
            return SSID;
        }
        
        void makeItValid() {
            Calendar calendar = Calendar.getInstance();     // 30 perces munkamenet
            calendar.add(Calendar.MINUTE, 30);
            validTime = calendar.getTime();
        }
    }
}
