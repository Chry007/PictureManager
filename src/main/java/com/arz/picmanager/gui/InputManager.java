package com.arz.picmanager.gui;

import com.arz.picmanager.service.PictureFinder;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class InputManager extends JFrame {
    private JTextField inputTextField;
    private JTextField outputTextField;
    private JButton go;
    private JPanel rootPanel;
    private JButton inputPickerButton;
    private JButton outputPickerButton;
    private JCheckBox validateOutputFolder;
    private JTextPane foundPicNoPane;
    private JTextPane evaluatedPicsNoPane;
    private JTextPane copiedPicsNoPaneTextPane;
    private final JFileChooser fc = new JFileChooser();

    private int foundPicNo = 0;
    private int copiedPicsNo = 0;
    private int evaluatedPicsNo = 0;
    private int distinctPicsFound = 0;


    public InputManager() {
        this.add(this.rootPanel);
        this.setTitle("Photo Manager");
        this.setSize(800, 200);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);

        inputPickerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fc.showOpenDialog(InputManager.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    if (file.isDirectory()) {
                        inputTextField.setText(file.getAbsolutePath());
                    }
                }
            }
        });

        outputPickerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fc.showOpenDialog(InputManager.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    if (file.isDirectory()) {
                        outputTextField.setText(file.getAbsolutePath());
                    }
                }
            }
        });

        go.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Thread background = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String message = new PictureFinder(InputManager.this).doWork(new File(inputTextField.getText()), new File(outputTextField.getText()), validateOutputFolder.isSelected());
                            JOptionPane.showMessageDialog(InputManager.this, message, "Result: ", JOptionPane.INFORMATION_MESSAGE);
                            System.exit(0);
                        }
                    });
                    background.start();
                } catch (Exception exc) {
                    StringBuilder sb = new StringBuilder(exc.toString());
                    for (StackTraceElement ste : exc.getStackTrace()) {
                        sb.append("\n\tat ");
                        sb.append(ste);
                    }
                    String trace = sb.toString();
                    JOptionPane.showMessageDialog(InputManager.this, trace, "ERROR ", JOptionPane.ERROR_MESSAGE);
                    throw exc;
                }
            }
        });
    }

    public void addPicFound() {
        this.foundPicNo++;
        this.foundPicNoPane.setText(this.foundPicNo + " Pictures found");
    }

    public void addPicEvaluated(boolean copied) {
        this.evaluatedPicsNo++;
        if (copied) {
            this.distinctPicsFound++;
        }
        this.evaluatedPicsNoPane.setText(this.evaluatedPicsNo + "Pictures evaluated, " + this.distinctPicsFound + " from which are distinct");
    }

    public void addPiccopied() {
        this.copiedPicsNo++;
        this.copiedPicsNoPaneTextPane.setText(this.copiedPicsNo + " pictures copied");
    }

    public void finish() {
        System.exit(0);
    }
}
