package burp;

import java.io.*;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.awt.Component;
import java.io.PrintWriter;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.Box;
import java.awt.Dimension;
import java.awt.event.*;

/**
 * Burp Scripter, Burp extender to inject script output into HTTP messages
 *
 * @author Eriatarka
 *
 */

public class BurpExtender implements IBurpExtender,  IHttpListener, ISessionHandlingAction, ITab{
    private PrintWriter stdout;
    private PrintWriter stderr;
    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;
    private IHttpRequestResponse currentlyDisplayedItem;
    private JPanel panel;

    private static final String EMPTY_STRING = "";
    private String user_script;
    private List<java.lang.String> parameters_to_inject;
    private List<java.lang.String> parameters_values_to_inject;
    private List<java.lang.String> script_parameters;

    @Override
    public void registerExtenderCallbacks(final IBurpExtenderCallbacks callbacks) {
        this.callbacks = callbacks;
        helpers = callbacks.getHelpers();
        callbacks.setExtensionName("Burp Scripter");

        // obtain our output and error streams
        stdout = new PrintWriter(callbacks.getStdout(), true);
        stderr = new PrintWriter(callbacks.getStderr(), true);

        callbacks.registerSessionHandlingAction(this);
        // create our UI
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                // main panel
                panel = new JPanel();
                Box boxHorizontal = Box.createHorizontalBox();
                Box boxVertical = Box.createVerticalBox();

                boxVertical.add(Box.createRigidArea(new Dimension(0,15)));

                // Definido aquí para poder escribir la salida del script, se usa más abajo.
                Box boxHorizontalOutput = Box.createHorizontalBox();
                JTextArea outputTextArea = new JTextArea();
                JScrollPane output = new JScrollPane(outputTextArea);
                output.setPreferredSize(new Dimension(1050,325));


                boxHorizontal.add(new JLabel("Absolute path to the script to be executed:"));
                boxVertical.add(boxHorizontal);

                boxVertical.add(Box.createRigidArea(new Dimension(0,5)));

                boxHorizontal = Box.createHorizontalBox();
                JTextField script = new JTextField("",30);
                String scriptText = callbacks.loadExtensionSetting("Script");
                if (scriptText != null) {script.setText(scriptText); stdout.println("Loaded settings for Script field: "+scriptText);}
                boxHorizontal.add(script);
                boxVertical.add(boxHorizontal);

                boxVertical.add(Box.createRigidArea(new Dimension(0,15)));

                boxHorizontal = Box.createHorizontalBox();
                boxHorizontal.add(new JLabel("Script parameter list:  "));
                boxHorizontal.add(Box.createRigidArea(new Dimension(5,0)));
                JTextField script_params = new JTextField("",10);
                String script_paramsText = callbacks.loadExtensionSetting("Script_params");
                if (script_paramsText != null) {script_params.setText(script_paramsText); stdout.println("Loaded settings for Script_params field: "+script_paramsText);}
                boxHorizontal.add(script_params);
                boxVertical.add(boxHorizontal);

                boxVertical.add(Box.createRigidArea(new Dimension(0,15)));

                boxHorizontal = Box.createHorizontalBox();
                boxHorizontal.add(new JLabel("Write a list of comma separated parameter names without spaces in the same order as the parameter values printed by the script."));
                boxVertical.add(boxHorizontal);

                boxHorizontal = Box.createHorizontalBox();
                boxHorizontal.add(new JLabel("To update the parameter list, click save and disable and enable the extension in the 'Extender' tab."));
                boxVertical.add(boxHorizontal);

                boxVertical.add(Box.createRigidArea(new Dimension(0,15)));

                boxHorizontal = Box.createHorizontalBox();
                boxHorizontal.add(new JLabel("Parameter name list:  "));
                boxHorizontal.add(Box.createRigidArea(new Dimension(5,0)));
                JTextField params = new JTextField("",10);
                String paramsText = callbacks.loadExtensionSetting("Params");
                if (paramsText != null) {params.setText(paramsText); stdout.println("Loaded settings for Params field: "+paramsText);}
                boxHorizontal.add(params);
                boxVertical.add(boxHorizontal);

                boxVertical.add(Box.createRigidArea(new Dimension(0,25)));

                boxHorizontal = Box.createHorizontalBox();
                JButton test = new JButton("Test script");
                test.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e){
                        try{
                            String line;
                            stdout.println("Testing script "+script.getText());
                            outputTextArea.append("Testing script "+script.getText()+":\n");
                            Process p = Runtime.getRuntime().exec(script.getText());
                            BufferedReader input = new BufferedReader(new InputStreamReader((p.getInputStream())));
                            while ((line = input.readLine()) != null) {outputTextArea.append(line+"\n\n");}
                        } catch (IOException ex){
                            StringWriter sw = new StringWriter();
                            PrintWriter pw = new PrintWriter(sw);
                            ex.printStackTrace(pw);
                            String sStackTrace = sw.toString();
                            outputTextArea.append(sStackTrace);
                            stderr.println(ex);
                        }
                    }
                });
                boxHorizontal.add(test);

                boxHorizontal.add(Box.createRigidArea(new Dimension(25,0)));

                JButton save = new JButton("Save");
                save.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e2){
                        callbacks.saveExtensionSetting("Script", ""+script.getText());
                        callbacks.saveExtensionSetting("Params", ""+params.getText());
                        callbacks.saveExtensionSetting("Script_params", ""+script_params.getText());
                        stdout.println("Saved settings: "+script.getText()+", "+params.getText()+", "+script_params.getText());
                    }
                });
                boxHorizontal.add(save);

                boxHorizontal.add(Box.createRigidArea(new Dimension(25,0)));

                JButton clear = new JButton("Clear form");
                clear.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e3){
                        script.setText("");
                        params.setText("");
                        script_params.setText("");
                    }
                });
                boxHorizontal.add(clear);

                boxVertical.add(boxHorizontal);

                boxVertical.add(Box.createRigidArea(new Dimension(0,35)));

                boxHorizontal = Box.createHorizontalBox();
                boxHorizontal.add(new JLabel("Output:"));

                boxVertical.add(boxHorizontal);


                boxHorizontalOutput.add(output);
                boxVertical.add(boxHorizontalOutput);

                boxVertical.add(Box.createRigidArea(new Dimension(0,45)));

                boxHorizontal = Box.createHorizontalBox();
                JButton clear_output = new JButton("Clear output");
                clear_output.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e4){
                        outputTextArea.setText("");
                    }
                });
                boxHorizontal.add(clear_output);

                boxVertical.add(boxHorizontal);
                panel.add(boxVertical);
                // customize UI:
                callbacks.customizeUiComponent(panel);

                // Add tab:
                callbacks.addSuiteTab(BurpExtender.this);

                user_script = script.getText();
                parameters_to_inject = Arrays.asList(params.getText().split(","));
                script_parameters = Arrays.asList(script_params.getText().split(","));

            }
        });
    }

    //Implement ITab
    @Override
    public String getTabCaption(){return "Scripter";}

    @Override
    public Component getUiComponent(){return panel;}

    @Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo){}

    @Override
    public String getActionName() { return "Scripter - Execute custom script"; }

    @Override
    public void performAction(IHttpRequestResponse currentRequest, IHttpRequestResponse[] macroItems) {

        stdout.println("");

        // Get the request
        String strrequest = new String(currentRequest.getRequest());
        // Get the request info:
        IRequestInfo rqInfo = helpers.analyzeRequest(currentRequest);
        // retrieve all headers
        List<java.lang.String> headers = rqInfo.getHeaders();
        // get the request body
        String messageBody = strrequest.substring(rqInfo.getBodyOffset());

        // Re-compose the original request
        byte[] request = helpers.buildHttpMessage(headers, messageBody.getBytes());

        ArrayList<String> script_parameter_values = new ArrayList<String>();

        List<IParameter> params = rqInfo.getParameters();
        for (int i=0; i < params.size(); i++) {
            for (int j=0; j < script_parameters.size(); j++){
                if (params.get(i).getName().equals(script_parameters.get(j))) {
                    stdout.println("Matched "+params.get(i).getName()+"! (" + params.get(i).getValue() + ")");
                    script_parameter_values.add(params.get(i).getValue());
                }
            }
        }

        // Execute the user supplied script:
        try{
            String line;
            String output = "";
            String command[] = new String[script_parameter_values.size()+1];
            command[0] = user_script;
            for (int i=0; i < script_parameter_values.size(); i++) {
                command[i+1] = script_parameter_values.get(i);
            }
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader input = new BufferedReader(new InputStreamReader((p.getInputStream())));
            while ((line = input.readLine()) != null) {output += line.replaceAll("\\s+","");}
            parameters_values_to_inject = Arrays.asList(output.split(","));
            if (parameters_values_to_inject.size() != parameters_to_inject.size()) {throw new RuntimeException("The number of parameters supplied doesn't match the parameters received from the script");}
        } catch (IOException ex){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            String sStackTrace = sw.toString();
            stderr.println(sStackTrace);
            //stderr.println(ex);
        }

        for (int i=0; i < parameters_to_inject.size(); i++){
            try {
                IParameter out_parameter = helpers.buildParameter(parameters_to_inject.get(i), parameters_values_to_inject.get(i), helpers.getRequestParameter(request, parameters_to_inject.get(i)).getType());
                stdout.println("Updating parameter " + out_parameter.getName() + ": " + out_parameter.getValue());
                request = helpers.updateParameter(request, out_parameter);
            } catch (Exception ex2) {
                stderr.println("Exception updating parameter\n -Parameter: "+parameters_to_inject.get(i));
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex2.printStackTrace(pw);
                String sStackTrace = sw.toString();
                stderr.println(sStackTrace);
            }
        }

        currentRequest.setRequest(request);
    }
}
