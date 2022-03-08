package jmschat;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

public class Jms {
    private Zoom zoom;
    private ConnectionFactory factory;
    private TopicConnection con;
    private Topic topic;
    private TopicSession session;
    private TopicPublisher publisher;
    private TopicSubscriber subsriber;
    
    public Jms(Zoom zoom){
        this.zoom = zoom;
        initJMS();
    }
    
    private void initJMS(){
        try {
            InitialContext ctx  = new InitialContext();
            factory = (ConnectionFactory) ctx.lookup("chat/factory");
            topic = (Topic) ctx.lookup("chat/destination");
            con = (TopicConnection) factory.createConnection();
            con.start();
            session = con.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            publisher = session.createPublisher(topic);
            subsriber = session.createSubscriber(topic);
            subsriber.setMessageListener(new Listener(zoom));
        } catch (NamingException | JMSException ex) {
            JOptionPane.showMessageDialog(zoom, ex);
        }
    }
    
    public void sendMessage(String message){
        TextMessage msg;
        try {
            msg = session.createTextMessage();
            msg.setText(zoom.getUsername() + "-" + message);
            publisher.publish(msg);
        } catch (JMSException ex) {
            JOptionPane.showMessageDialog(zoom, ex);
        }
    }

//    Getters and Setters
    public Zoom getZoom() {
        return zoom;
    }

    public void setZoom(Zoom zoom) {
        this.zoom = zoom;
    }

    public ConnectionFactory getFactory() {
        return factory;
    }

    public void setFactory(ConnectionFactory factory) {
        this.factory = factory;
    }

    public TopicConnection getCon() {
        return con;
    }

    public void setCon(TopicConnection con) {
        this.con = con;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public TopicSession getSession() {
        return session;
    }

    public void setSession(TopicSession session) {
        this.session = session;
    }

    public TopicPublisher getPublisher() {
        return publisher;
    }

    public void setPublisher(TopicPublisher publisher) {
        this.publisher = publisher;
    }

    public TopicSubscriber getSubsriber() {
        return subsriber;
    }

    public void setSubsriber(TopicSubscriber subsriber) {
        this.subsriber = subsriber;
    }
}

class Listener implements MessageListener{
    private Zoom zoom;
    
    public Listener(Zoom zoom){
        this.zoom = zoom;
    }

    public Zoom getZoom() {
        return zoom;
    }

    public void setZoom(Zoom zoom) {
        this.zoom = zoom;
    }
    
    @Override
    public void onMessage(Message msg) {
        try {
            // Solve timestamp
            long timestamp = msg.getJMSTimestamp();
            String pattern = "YYYY:MM:dd HH:mm:ss";
            String time = new SimpleDateFormat(pattern).format(new Date(timestamp));
            TextMessage text = (TextMessage) msg;
            // Solve message            
            String[] texts = text.getText().split("-");
            String msgfr = texts[0];
            texts = Arrays.copyOfRange(texts, 1, texts.length);
            String content = String.join("-", texts);
            String namevsb = msgfr.equals(zoom.getUsername()) ? "Me" : msgfr;
            content = namevsb + "(" + time + ") : " +  content; 
            zoom.handleMessage(content);
        } catch (JMSException ex) {
            Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}