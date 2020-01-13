import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by cyril rocca Gr 2227 INPRES .
 */
public class loginGUI extends JFrame{


    private String _hostCompagnie;
    private int _portCompagnie;
    private String _hostTerminaux;
    private int _portTerminaux;
    private JTextField textFieldUser;
    private JPasswordField passwordField;
    private JButton buttonConnect;
    private JPanel rootPanel;
    private JList listReponse;
    private JComboBox comboBox;
    private String _separator;
    private String _endOfLine;
    private DefaultListModel<String> dlm;
    private Socket _connexionSrvCompagnie = null;
    private Socket _connexionSrvTerminaux = null;
    private PrintWriter _writerServeurCompagnie = null;
    private BufferedInputStream _readerServeurCompagnie = null;
    private PrintWriter _writerServeurTerminaux = null;
    private BufferedInputStream _readerServeurTerminaux = null;
    private ArrayList<String> _arrayOfArg = new ArrayList<>();

    public loginGUI() {

        super("ApplicFrontiere(Main)");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        ReadPropertyFile();

        setContentPane(rootPanel);
        setExtendedState(JFrame.MAXIMIZED_HORIZ);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setVisible(true);

        dlm = new DefaultListModel<String>();
        listReponse.setModel(dlm);

        textFieldUser.setText("admin");
        passwordField.setText("admin");

        try {
            _connexionSrvCompagnie = new Socket(_hostCompagnie,  _portCompagnie);
            _writerServeurCompagnie = new PrintWriter(_connexionSrvCompagnie.getOutputStream(), true);
            _readerServeurCompagnie = new BufferedInputStream(_connexionSrvCompagnie.getInputStream());

            dlm.addElement("Connexion Serveur Compagnie OK");
/*
            _connexionSrvTerminaux = new Socket(_hostTerminaux,  _portTerminaux);
            _writerServeurTerminaux = new PrintWriter(_connexionSrvTerminaux.getOutputStream(), true);
            _readerServeurTerminaux = new BufferedInputStream(_connexionSrvTerminaux.getInputStream());

            dlm.addElement("Connexion Serveur Compagnie OK");*/
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        buttonConnect.addActionListener(e -> {
            Login();     
        });
    }

    private void Login() {

        _arrayOfArg.clear();
        _arrayOfArg.add("LOGINA");
        _arrayOfArg.add(textFieldUser.getText());
        _arrayOfArg.add( String.valueOf(passwordField.getPassword()));

        String request = MakeRequest("HAFICSA", _arrayOfArg );

        String response = null;
        try {
            response = SendRequest(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        dlm.addElement("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
        dlm.addElement("String reponse serveur : *" + response + "*");

        AnalyseReponse("LOGINA",response);


    }

    private String SendRequest(String request) throws IOException {

        String response = "";

        if(comboBox.getSelectedItem().toString().contains("Compagnie")){

            _writerServeurCompagnie.write(request);
            _writerServeurCompagnie.flush();

            System.out.println("Commande [" + request + "] envoyée au serveur Compagnie");

            //On attend la réponse
            response = readSrvCompagnie();
            System.out.println("\t * " + response + " : Réponse reçue " + response);

        } else {

            _writerServeurTerminaux.write(request);
            _writerServeurTerminaux.flush();

            System.out.println("Commande [" + request + "] envoyée au serveur Terminaux");

            //On attend la réponse
            response = readSrvTerminaux();
            System.out.println("\t * " + response + " : Réponse reçue " + response);
        }


        return response;
    }

    //Méthode pour lire les réponses du serveur
    private String readSrvCompagnie() throws IOException{
        String response = "";
        int stream;
        byte[] b = new byte[4096];
        stream = _readerServeurCompagnie.read(b);
        response = new String(b, 0, stream);
        return response;
    }

    //Méthode pour lire les réponses du serveur
    private String readSrvTerminaux() throws IOException{
        String response = "";
        int stream;
        byte[] b = new byte[4096];
        stream = _readerServeurTerminaux.read(b);
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
            case "LOGINA":
                if(response.equals("OK")) {
                    dlm.clear();
                    dlm.addElement("Admin successfully connected");

                    if(comboBox.getSelectedItem().toString().contains("Compagnie")){
                        new MainGui(_connexionSrvCompagnie, _writerServeurCompagnie, _readerServeurCompagnie, _separator, _endOfLine);
                        dispose();
                    }else{
                        new MainGui(_connexionSrvCompagnie, _writerServeurTerminaux, _readerServeurTerminaux, _separator, _endOfLine);
                        dispose();
                    }

                } else {
                    dlm.addElement("Admin already connect or wrong password");
                }
                break;
        }
    }

    public void ReadPropertyFile(){

        //Lecture PROPERTY FILE
        Properties _propFile = new Properties();
        InputStream _InStream = null;
        try
        {
            _InStream = new FileInputStream("config.properties");
            _propFile.load(_InStream);

            _portCompagnie = Integer.parseInt(_propFile.getProperty("PORT_SRV_COMPAGNIE"));
            _hostCompagnie = _propFile.getProperty("HOST_SRV_COMPAGNIE");
            _portTerminaux = Integer.parseInt(_propFile.getProperty("PORT_SRV_TERMINAUX"));
            _hostTerminaux = _propFile.getProperty("HOST_SRV_TERMINAUX");
            _separator = _propFile.getProperty("SEPARATOR");
            _endOfLine = _propFile.getProperty("ENDOFLINE");

            _InStream.close();

        } catch (IOException e) {
            System.err.println("Error Reading Properties Files [" + e + "]");
        }

    }
}
