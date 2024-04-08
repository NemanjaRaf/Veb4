package com.example.veb44;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@WebListener
public class SessionListener implements HttpSessionListener {
    private static final List<HttpSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        sessions.add(se.getSession());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        sessions.remove(se.getSession());
    }

    public static List<HttpSession> getSessions() {
        return sessions;
    }
}
