package sdntools;/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */

import com.jcraft.jsch.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.net.*;
import java.io.*;

public class X11Forwarding{

    public static void runSSh(JTextField textField, JTextArea jpane, String URLAddress){

        String xhost="127.0.0.1";
        int xport=0;

        try{
            JSch jsch=new JSch();

            String host;
            URL sdn_url = new URL(URLAddress);
            host = JOptionPane.showInputDialog(
                    "Enter username@hostname",
                    String.format("mininet@%s", sdn_url.getHost())
            );

            String user = host.substring(0, host.indexOf('@'));
            host = host.substring(host.indexOf('@') + 1);

            Session session = jsch.getSession(user, host, 22);


            session.setX11Host(xhost);
            session.setX11Port(xport+6000);

            // username and password will be given via UserInfo interface.
            UserInfo ui=new MyUserInfo();
            session.setUserInfo(ui);
            session.connect();

            Channel channel=session.openChannel("shell");

            channel.setXForwarding(true);

            channel.setInputStream(new CustomInputStream(textField));
            channel.setOutputStream(new CustomOutputStream(jpane));

            channel.connect();
        }
        catch(Exception e){
            System.out.println(e);
        }
    }

    public static void runSerialSSh(ArrayList<JTextField> JTextFieldArray, ArrayList<JTextArea> JTextAreaArray, String URLAddress){

        String xhost="127.0.0.1";
        int xport=0;

        try{
            JSch jsch=new JSch();

            String host;
            URL sdn_url = new URL(URLAddress);
            host = JOptionPane.showInputDialog(
                    "Enter username@hostname",
                    String.format("mininet@%s", sdn_url.getHost())
            );

            String user = host.substring(0, host.indexOf('@'));
            host = host.substring(host.indexOf('@') + 1);

            if (JTextFieldArray.size() != JTextAreaArray.size())
                return;

            for (int i = 0; i < JTextFieldArray.size(); i++) {
                Session session = jsch.getSession(user, host, 22);

                session.setX11Host(xhost);
                session.setX11Port(xport+6000);

                // username and password will be given via UserInfo interface.
                UserInfo ui=new MyUserInfo();
                session.setUserInfo(ui);
                session.connect();

                Channel channel=session.openChannel("shell");

                channel.setXForwarding(true);

                channel.setInputStream(new CustomInputStream(JTextFieldArray.get(i)));
                channel.setOutputStream(new CustomOutputStream(JTextAreaArray.get(i)));

                channel.connect();
            }
        }
        catch(Exception e){
            System.out.println(e);
        }
    }

    public static class MyUserInfo implements UserInfo, UIKeyboardInteractive{
        public String getPassword(){ return passwd; }
        public boolean promptYesNo(String str){
            Object[] options={ "yes", "no" };
            int foo=JOptionPane.showOptionDialog(null,
                    str,
                    "Warning",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return foo==0;
        }

        String passwd;
        JTextField passwordField=(JTextField)new JPasswordField(20);

        public String getPassphrase(){ return null; }
        public boolean promptPassphrase(String message){ return true; }
        public boolean promptPassword(String message){
            Object[] ob={passwordField};
            int result=
                    JOptionPane.showConfirmDialog(null, ob, message,
                            JOptionPane.OK_CANCEL_OPTION);
            if(result==JOptionPane.OK_OPTION){
                passwd="mininet";
                return true;
            }
            else{ return false; }
        }
        public void showMessage(String message){
            JOptionPane.showMessageDialog(null, message);
        }
        final GridBagConstraints gbc =
                new GridBagConstraints(0,0,1,1,1,1,
                        GridBagConstraints.NORTHWEST,
                        GridBagConstraints.NONE,
                        new Insets(0,0,0,0),0,0);
        private Container panel;
        public String[] promptKeyboardInteractive(String destination,
                                                  String name,
                                                  String instruction,
                                                  String[] prompt,
                                                  boolean[] echo){
            panel = new JPanel();
            panel.setLayout(new GridBagLayout());

            gbc.weightx = 1.0;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.gridx = 0;
            panel.add(new JLabel(instruction), gbc);
            gbc.gridy++;

            gbc.gridwidth = GridBagConstraints.RELATIVE;

            JTextField[] texts=new JTextField[prompt.length];
            for(int i=0; i<prompt.length; i++){
                gbc.fill = GridBagConstraints.NONE;
                gbc.gridx = 0;
                gbc.weightx = 1;
                panel.add(new JLabel(prompt[i]),gbc);

                gbc.gridx = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weighty = 1;
                if(echo[i]){
                    texts[i]=new JTextField(20);
                }
                else{
                    texts[i]=new JPasswordField(20);
                }
                panel.add(texts[i], gbc);
                gbc.gridy++;
            }

            if(JOptionPane.showConfirmDialog(null, panel,
                    destination+": "+name,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE)
                    ==JOptionPane.OK_OPTION){
                String[] response=new String[prompt.length];
                for(int i=0; i<prompt.length; i++){
                    response[i]=texts[i].getText();
                }
                return response;
            }
            else{
                return null;  // cancel
            }
        }
    }
    public static class CustomInputStream extends InputStream implements ActionListener {

        final JTextField field;
        final BlockingQueue<String> q;

        public CustomInputStream(JTextField field) {
            this.field = field;
            q = new LinkedBlockingQueue<>();
            field.addActionListener(this);
        }

        private String s;
        int pos;

        @Override
        public int read() throws IOException {
            while (null == s || s.length() <= pos) {
                try {
                    s = q.take();
                    pos = 0;
                } catch (InterruptedException ex) {

                }
            }
            int ret = (int) s.charAt(pos);
            pos++;
            return ret;
        }

        @Override
        public boolean markSupported() {
            return false;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int bytes_copied = 0;
            while (bytes_copied < 1) {
                while (null == s || s.length() <= pos) {
                    try {
                        s = q.take();
                        pos = 0;
                    } catch (InterruptedException ex) {

                    }
                }
                int bytes_to_copy = len < s.length()-pos ? len : s.length()-pos;
                System.arraycopy(s.getBytes(), pos, b, off, bytes_to_copy);
                pos += bytes_to_copy;
                bytes_copied += bytes_to_copy;
            }
            return bytes_copied;
        }

        @Override
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            q.add(field.getText() + "\r\n");
            field.setText("");
        }

    }

    // This is the code for making textarea as output stream
    public static class CustomOutputStream extends OutputStream {

        private JTextArea textArea;

        public CustomOutputStream(JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void write(int b) throws IOException {
            // redirects data to the text area
            textArea.append(String.valueOf((char) b));
            // scrolls the text area to the end of data
            textArea.setCaretPosition(textArea.getText().length());
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            String s = new String(b,off,len);
            textArea.append(s);
            textArea.setCaretPosition(textArea.getText().length());
        }

        @Override
        public void write(byte[] b) throws IOException {
            this.write(b, 0, b.length);
        }
    }
}
