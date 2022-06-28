package eos;

import eos.assist.DbAccess;
import eos.model.web.HttpRequest;
import eos.model.web.HttpSession;
import com.sun.net.httpserver.HttpExchange;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Security {

    static final String USER_KEY        = "user";
    public static final String HASH_256 = "SHA-256";

    static DbAccess dbAccess;
    static Map<String, HttpSession> sessions = new ConcurrentHashMap<String, HttpSession>();

    //////// Thank you Apache Shiro! ////////
    private static ThreadLocal<HttpRequest> requestStorage = new InheritableThreadLocal<>();
    private static ThreadLocal<HttpExchange> exchangeStorage = new InheritableThreadLocal<>();

    public static void SAVE(HttpRequest request){
        requestStorage.set(request);
    }
    public static void SAVE(HttpExchange exchange){
        exchangeStorage.set(exchange);
    }

    public static HttpRequest getRequest(){
        return requestStorage.get();
    }
    public static HttpExchange getExchange(){
        return exchangeStorage.get();
    }

    public static boolean hasRole(String role){
        String user = getUser();
        if(user != null) {
            Set<String> roles = dbAccess.getRoles(user);
            if(roles.contains(role)){
                return true;
            }
        }
        return false;
    }

    public static boolean hasPermission(String permission){
        String user = getUser();
        if(user != null) {
            Set<String> permissions = dbAccess.getPermissions(user);
            if(permissions.contains(permission)){
                return true;
            }
        }
        return false;
    }

    public static String getUser(){
        HttpRequest req = Security.getRequest();
        HttpSession httpSession = req.getSession(false);

        if(httpSession != null){
            String user = (String) httpSession.get(Security.USER_KEY);
            return user;
        }
        return "";
    }

    public static String get(String key){
        HttpRequest req = Security.getRequest();
        HttpSession httpSession = req.getSession();
        if(httpSession != null){
            return String.valueOf(httpSession.get(key));
        }
        return "";
    }


    public static boolean set(String key, String value){
        HttpRequest req = Security.getRequest();
        HttpSession httpSession = req.getSession();
        if(httpSession != null){
            httpSession.set(key, value);
        }
        return true;
    }

    public static boolean signin(String username, String passwordUntouched){
        String hashed = Security.hash(passwordUntouched);
        String password = dbAccess.getPassword(username);

        if(!isAuthenticated() &&
                password.equals(hashed)){

            HttpRequest req = Security.getRequest();

            HttpSession oldHttpSession = req.getSession(false);
            if(oldHttpSession != null){
                oldHttpSession.dispose();
            }

            HttpSession httpSession = req.getSession(true);
            httpSession.set(Security.USER_KEY, username);
            sessions.put(httpSession.getId(), httpSession);

            return true;
        }

        return false;
    }

    public static boolean signout(){
        HttpRequest req = Security.getRequest();
        HttpSession httpSession = req.getSession(false);

        if(httpSession != null){
            httpSession.dispose();
            httpSession.remove(Security.USER_KEY);
            if(sessions.containsKey(httpSession.getId())){
                sessions.remove(httpSession.getId());
            }
        }
        return true;
    }

    public static boolean isAuthenticated(){
        HttpRequest req = Security.getRequest();
        if(req != null) {
            HttpSession httpSession = req.getSession(false);
            if (httpSession != null && sessions.containsKey(httpSession.getId())) {
                return true;
            }
        }
        return false;
    }

    public static boolean configure(DbAccess dbAccess){
        Security.dbAccess = dbAccess;
        return true;
    }

    public static String hash(String password){
        MessageDigest md = null;
        StringBuffer passwordHashed = new StringBuffer();

        try {
            md = MessageDigest.getInstance(Security.HASH_256);
            md.update(password.getBytes());

            byte byteData[] = md.digest();

            for (int i = 0; i < byteData.length; i++) {
                passwordHashed.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return passwordHashed.toString();
    }

}
