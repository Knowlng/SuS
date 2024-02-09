package java;

import javax.servlet.http.HttpServletRequest;

public class CommandInjectionExample {

    public void doGet(HttpServletRequest request) throws Exception {
        String ipAddress = request.getParameter("ipAddress");

        String command = "/sbin/ping -c 1 " + ipAddress;
        Process p = Runtime.getRuntime().exec(command);

    }
}