/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.peertopeerchatapp;

/**
 *
 * @author Administrator
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PeerToPeerChatApp extends JFrame {

    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private String localIpAddress;
    private int localPort;
    private String remoteIpAddress;
    private int remotePort;

    private DatagramSocket socket;

    public PeerToPeerChatApp(String localIpAddress, int localPort, String remoteIpAddress, int remotePort) {
        this.localIpAddress = localIpAddress;
        this.localPort = localPort;
        this.remoteIpAddress = remoteIpAddress;
        this.remotePort = remotePort;

        initializeUI();
        initializeSocket();

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Peer-to-Peer Chat App");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());

        messageField = new JTextField();
        inputPanel.add(messageField, BorderLayout.CENTER);

        sendButton = new JButton("Send");
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(inputPanel, BorderLayout.SOUTH);
    }

    private void initializeSocket() {
        try {
            socket = new DatagramSocket(localPort);
            new Thread(new ReceiverThread()).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        try {
            String message = messageField.getText();
            if (!message.isEmpty()) {
                String formattedMessage = formatMessage(message, true);
                chatArea.append(formattedMessage);

                byte[] data = message.getBytes();
                DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(remoteIpAddress), remotePort);
                socket.send(packet);

                messageField.setText("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatMessage(String message, boolean sent) {
        String color = sent ? "red" : "green";
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        return String.format("<html><font color=%s>%s [%s]: %s</font><br></html>", color, timestamp, sent ? "You" : "Friend", message);
    }

    private class ReceiverThread implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String message = new String(packet.getData(), 0, packet.getLength());
                    String formattedMessage = formatMessage(message, false);
                    chatArea.append(formattedMessage);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new PeerToPeerChatApp("127.0.0.1", 49677, "127.0.0.1", 49678);
            }
        });
    }
}
