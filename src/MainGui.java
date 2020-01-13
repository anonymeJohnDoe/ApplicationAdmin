import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by cyril rocca Gr 2227 INPRES .
 */
public class MainGui extends JFrame{


    private JButton pauseButton;
    private JButton buttonListUser;
    private JList listUser;
    private JButton stopButton;
    private JPanel rootPanel;
    private JList listLogs;
    private JTextField textFieldTime;
    private PrintWriter writerSrv;
    private BufferedInputStream readerSrv;
    private ArrayList<String> _arrayOfArg = new ArrayList<>();
    private String _separator;
    private String _endOfLine;
    private DefaultListModel<String> dlmListUser;
    private DefaultListModel<String> dlmListLogs;
    private Socket srvCompagnie;

    public MainGui(Socket socketSrvCompagnie, PrintWriter writerServeur, BufferedInputStream readerServeur, String separator, String endofline) {

        super("ApplicFrontiere(Main)");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setContentPane(rootPanel);
        setExtendedState(JFrame.MAXIMIZED_HORIZ);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setVisible(true);

        this.srvCompagnie = socketSrvCompagnie;

        dlmListLogs = new DefaultListModel<String>();
        listLogs.setModel(dlmListLogs);

        dlmListUser = new DefaultListModel<String>();
        listUser.setModel(dlmListUser);

        this._separator = separator;
        this._endOfLine = endofline;
        this.writerSrv = writerServeur;
        this.readerSrv = readerServeur;
        
        buttonListUser.addActionListener(e -> {
            ListUsers();
        });
        
        pauseButton.addActionListener(e -> {
            Pause();
        });
        
        stopButton.addActionListener(e -> {
            Stop();    
        });



    }

    private String SendRequest(String request) throws IOException {

        String response = "";

            writerSrv.write(request);
            writerSrv.flush();

            System.out.println("Commande [" + request + "] envoyée au serveur");

            //On attend la réponse
            response = readSrv();
            System.out.println("\t * " + response + " : Réponse reçue " + response);


        return response;
    }

    //Méthode pour lire les réponses du serveur
    private String readSrv() throws IOException{
        String response = "";
        int stream;
        byte[] b = new byte[4096];
        stream = readerSrv.read(b);
        response = new String(b, 0, stream);
        return response;
    }


    private String MakeRequest(String cmd, ArrayList<String> arrayOfArg) {
        String request = cmd;

        for(String str : arrayOfArg) {
            request += _separator + str;
        }

        request += _endOfLine;


        return request;
    }



    private void AnalyseReponse(String cmd, String response) {
        switch (cmd) {
            case "LCLIENTS":
                if(!response.equals("EMPTY")) {
                    dlmListUser.clear();
                    splitStringsIntoUsers(response);
                    dlmListLogs.addElement("Retrieve list of user: SUCCESS");
                } else {
                    dlmListLogs.addElement("Retrieve list of user : EMPTY");
                }
                break;

            case "PAUSE":
                if(response.equals("OK")) {
                    dlmListLogs.addElement("SERVER PAUSE : SUCCESS");
                } else {
                    dlmListLogs.addElement("SERVER PAUSE : FAILED");
                }
                break;

            case "WAKEUP":
                if(response.equals("OK")) {
                    dlmListLogs.addElement("SERVER WAKEUP : SUCCESS");
                } else {
                    dlmListLogs.addElement("SERVER WAKEUP : FAILED");
                }
                break;

            case "STOP":
                if(response.equals("OK")) {
                    dlmListLogs.addElement("SERVER STOP : SUCCESS");
                    try {
                        srvCompagnie.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    dlmListLogs.addElement("SERVER STOP : FAILED");
                }
                break;
        }
    }

    private void splitStringsIntoUsers(String response) {

        String tokfull = response.replaceAll(".$", "");
        String[] users = tokfull.split("\\|");

        for(String str: users){
            dlmListUser.addElement(str);
        }

        System.out.println("All Users add correctly to the JLIST");
    }

    private void Pause() {
        _arrayOfArg.clear();

        if(pauseButton.getText().equals("Pause"))
            _arrayOfArg.add("PAUSE");
        else
            _arrayOfArg.add("WAKEUP");

        String request = MakeRequest("HAFICSA", _arrayOfArg );

        String response = null;
        try {
            response = SendRequest(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        dlmListLogs.addElement("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
        dlmListLogs.addElement("String reponse serveur : *" + response + "*");


        if(pauseButton.getText().equals("Pause"))
            AnalyseReponse("PAUSE",response);
        else
            AnalyseReponse("WAKEUP",response);

        if(pauseButton.getText().equals("Pause")){
            pauseButton.setText("WakeUp");
        }else{
            pauseButton.setText("Pause");
        }
    }

    private void Stop() {

        String time = "";
        _arrayOfArg.clear();
        _arrayOfArg.add("STOP");
        if(textFieldTime.getText().equals(""))
            time = "500";
        else
            time = textFieldTime.getText();

        _arrayOfArg.add(time);
        String request = MakeRequest("HAFICSA", _arrayOfArg );

        String response = null;
        try {
            response = SendRequest(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        dlmListLogs.addElement("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
        dlmListLogs.addElement("String reponse serveur : *" + response + "*");

        AnalyseReponse("STOP",response);

        stopButton.setEnabled(false);
    }

    private void ListUsers() {
        _arrayOfArg.clear();
        _arrayOfArg.add("LCLIENTS");
        String request = MakeRequest("HAFICSA", _arrayOfArg );

        String response = null;
        try {
            response = SendRequest(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        dlmListLogs.addElement("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
        dlmListLogs.addElement("String reponse serveur : *" + response + "*");

        AnalyseReponse("LCLIENTS",response);
    }

}
