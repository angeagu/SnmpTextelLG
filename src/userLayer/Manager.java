/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package userLayer;

import configuration.Configuration;
import configuration.Utilities;
import configuration.data.JFrameCommand;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JLabel;
import org.apache.commons.io.input.Tailer;
import org.apache.log4j.Logger;
import snmpAgent.SnmpAgent;

/**
 *
 * @author Tecnovision
 */
public class Manager extends javax.swing.JFrame {

    private static final Logger log = Logger.getLogger(Manager.class.getName());
    private final String LOCATION_FILE = "location.ini";

    // Ids for cmd commands
    public final static String CMD_MASTER_DISPLAY_ON = "cmdMasterDisplayOn";
    public final static String CMD_MASTER_DISPLAY_OFF = "cmdMasterDisplayOff";
    public final static String CMD_SLAVE_DISPLAY_ON = "cmdSlaveDisplayOn";
    public final static String CMD_SLAVE_DISPLAY_OFF = "cmdSlaveDisplayOff";
    public final static String CMD_MASTER_TERMOSTATO_ON = "cmdMasterTermostatoOn";
    public final static String CMD_MASTER_TERMOSTATO_OFF = "cmdMasterTermostatoOff";
    public final static String CMD_SLAVE_TERMOSTATO_ON = "cmdSlaveTermostatoOn";
    public final static String CMD_SLAVE_TERMOSTATO_OFF = "cmdSlaveTermostatoOff";
    public final static String CMD_MASTER_SCAN_LEDS = "cmdMasterScanLeds";
    public final static String CMD_SLAVE_SCAN_LEDS = "cmdSlaveScanLeds";

    // Ids for button pair
    public final static String MASTER_TERMOSTATO_BUTTON = "masterTermostatoButton";
    public final static String SLAVE_TERMOSTATO_BUTTON = "slaveTermostatoButton";
    public final static String MASTER_DISPLAY_BUTTON = "masterDisplayButton";
    public final static String SLAVE_DISPLAY_BUTTON = "slaveDisplayButton";

    /**
     * Creates new form Manager
     */
    private static Manager instance = null;
    private final HashMap<String, JLabel> jLabelElements;
    private final HashMap<String, ButtonPair> buttonElements;
    private SnmpAgent agent;

    // colores para el fondo y la letra de los botones
    Color colorFondoResaltado = Color.darkGray;
    Color colorFondoNormal = new Color(240, 240, 240);
    Color colorLetraOn = Color.green;
    Color colorLetraOff = Color.red;
    Color colorLetraCold = Color.cyan;

    ////// hay que cerrar el hilo para salir bien del programa
    private final TailerListener listener;
    private final Tailer tailer;

    private Manager() {
        jLabelElements = new HashMap<>();
        buttonElements = new HashMap<>();
        initComponents();
        this.readGuiInfo();

        publicarElementosGUI();
        setSize(this.getWidth(), jPanel1.getHeight()
                + getInsets().top
                + getInsets().bottom
                + 11);   // el reborde interior del jPanel

        /* Vamos a implementar la funcionalidad de unix "tail -f", para ello
         creamos y usamos el Tailer en una de las tres formas posibles...
         usando el método estático (listener y tailer), con el constructor
         file, listener, delay y boolean end (true = del final hacia adelante) */
        listener = new TailerListener(txtLog);
        tailer = Tailer.create(new File("renfe.log"), listener, 500, true);
    }

    public void exit() {
        tailer.stop();
        this.dispose();
    }

    public void publicarElementosGUI() {
        // Label
        jLabelElements.put(labelMasterLightSensor.getName(), labelMasterLightSensor);
        jLabelElements.put(labelMasterErrorLeds.getName(), labelMasterErrorLeds);
        jLabelElements.put(labelMasterErrorModules.getName(), labelMasterErrorModules);
        jLabelElements.put(labelMasterTemperature.getName(), labelMasterTemperature);
        jLabelElements.put(labelSlaveLightSensor.getName(), labelSlaveLightSensor);
        jLabelElements.put(labelSlaveErrorLeds.getName(), labelSlaveErrorLeds);
        jLabelElements.put(labelSlaveErrorModules.getName(), labelSlaveErrorModules);
        jLabelElements.put(labelSlaveTemperature.getName(), labelSlaveTemperature);

        jLabelElements.put(labelMasterDoor.getName(), labelMasterDoor);
        jLabelElements.put(labelSlaveDoor.getName(), labelSlaveDoor);

        // Grupos de botones
        ButtonPair masterTermostato = new ButtonPair(cmdMasterTermostatoOn, cmdMasterTermostatoOff, colorLetraCold, colorLetraOff);
        ButtonPair slaveTermostato = new ButtonPair(cmdSlaveTermostatoOn, cmdSlaveTermostatoOff, colorLetraCold, colorLetraOff);
        ButtonPair masterDisplay = new ButtonPair(cmdMasterDisplayOn, cmdMasterDisplayOff, colorLetraOn, colorLetraOff);
        ButtonPair slaveDisplay = new ButtonPair(cmdSlaveDisplayOn, cmdSlaveDisplayOff, colorLetraOn, colorLetraOff);
        buttonElements.put(MASTER_TERMOSTATO_BUTTON, masterTermostato);
        buttonElements.put(SLAVE_TERMOSTATO_BUTTON, slaveTermostato);
        buttonElements.put(MASTER_DISPLAY_BUTTON, masterDisplay);
        buttonElements.put(SLAVE_DISPLAY_BUTTON, slaveDisplay);
    }

    public static Manager getInstance() {
        if (instance == null) {
            instance = new Manager();
        }

        return instance;
    }

    public JLabel getJLabelFromComponentName(String componentName) {
        return jLabelElements.get(componentName);
    }

    public ButtonPair getJButtonFromComponentName(String componentName) {
        return buttonElements.get(componentName);
    }

    public void setSnmpAgent(SnmpAgent currentAgent) {
        agent = currentAgent;
    }

    public void resaltarCmd(JButton cmd, boolean siNo, Color color) {
        // resaltamos un botón cambiando el color de fondo y poniendo el texto en negrita
        cmd.setBackground(colorFondoResaltado);
        cmd.setForeground(color);
        Font newFont = cmd.getFont().deriveFont(Font.BOLD);
        cmd.setFont(newFont);
    }

    public void noResaltarCmd(JButton cmd) {
        // se va a poner normal el color de fondo, y el texto en PLAIN y negro
        cmd.setBackground(colorFondoNormal);
        cmd.setForeground(Color.black);
        Font newFont = cmd.getFont().deriveFont(Font.PLAIN);
        cmd.setFont(newFont);
    }

    public void setLabelMasterFotocelula(String labelMasterFotocelula) {
        this.labelMasterLightSensor.setText(labelMasterFotocelula);
    }

    public void setLabelMasterTemperatura(String labelMasterTemperatura) {
        this.labelMasterTemperature.setText(labelMasterTemperatura);
    }

    public void setLabelMasterErrorLeds(String labelMasterErrorLeds) {
        this.labelMasterErrorLeds.setText(labelMasterErrorLeds);
    }

    public void setLabelMasterErrorModulos(String labelMasterErrorModulos) {
        this.labelMasterErrorModules.setText(labelMasterErrorModulos);
    }

    public void setLabelSlaveFotocelula(String labelSlaveFotocelula) {
        this.labelSlaveLightSensor.setText(labelSlaveFotocelula);
    }

    public void setLabelSlaveTemperatura(String labelSlaveTemperatura) {
        this.labelSlaveTemperature.setText(labelSlaveTemperatura);
    }

    public void setLabelSlaveErrorLeds(String labelSlaveErrorLeds) {
        this.labelSlaveErrorLeds.setText(labelSlaveErrorLeds);
    }

    public void setLabelSlaveErrorModulos(String labelSlaveErrorModulos) {
        this.labelSlaveErrorModules.setText(labelSlaveErrorModulos);
    }

    private void readGuiInfo() {
            //jSlider1.setValue(Utilidades.getBrightness(LOCATION_FILE));

        // otros parámetros
        Point initialPoint = this.getPosition(LOCATION_FILE);
        Dimension initialDimension = this.getDimension(LOCATION_FILE);

        if ((initialPoint.x != -1) && (initialPoint.y != -1)) {
            setLocation(initialPoint);
        } else {
            setLocationRelativeTo(null);
        }

        if ((initialDimension.width != -1) && (initialDimension.height != -1)) {
            setSize(initialDimension);
        }
    }

    public static void writeGuiInfo(int x, int y, int width, int height, String filename) {
        PrintWriter writer = null;

        try {
            writer = new PrintWriter(filename, "UTF-8");

        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            log.error("ERROR al escribir en fichero " + filename + ": " + ex);
        }

        if (writer != null) {
            writer.println("# Coordenadas de la posicion del Manager");
            String lineX = "x=" + x;
            String lineY = "y=" + y;
            String lineWidth = "width=" + width;
            String lineHeight = "height=" + height;
            //String lineImageFileName = "background=" + imageFileName;
            //String lineExcelFileName = "excel=" + excelFileName;
            //String lineBrightnessValue = "brightness=" + brightnessValue;

            writer.println(lineX);
            writer.println(lineY);
            writer.println(lineWidth);
            writer.println(lineHeight);
            //writer.println(lineImageFileName);
            //writer.println(lineExcelFileName);
            //writer.println(lineBrightnessValue);
            writer.close();
        }
    }

    public static Point getPosition(String filename) {
        Point point = new Point(-1, -1);
        String line;

        try (FileReader fr = new FileReader(filename)) {
            BufferedReader br = new BufferedReader(fr);

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if ((!line.isEmpty()) && (!line.startsWith("#"))) {

                    try {
                        String[] elements = line.split("=");

                        if ((elements != null) && (elements.length == 2) && (elements[0].equals("x"))) {
                            point.x = Integer.parseInt(elements[1]);

                        } else if ((elements != null) && (elements.length == 2) && (elements[0].equals("y"))) {
                            point.y = Integer.parseInt(elements[1]);
                        }

                    } catch (NumberFormatException ex) {
                        point = new Point(-1, -1);
                    }
                }
            }

            br.close();

        } catch (FileNotFoundException ex) {
            log.error("Error fichero " + filename + " no encontrado");

        } catch (IOException ex) {
            log.error("Error al leer de fichero " + filename);
        }

        return point;
    }

    public static Dimension getDimension(String filename) {
        Dimension dimension = new Dimension(-1, -1);
        String line;

        try (FileReader fr = new FileReader(filename)) {
            BufferedReader br = new BufferedReader(fr);

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if ((!line.isEmpty()) && (!line.startsWith("#"))) {

                    try {
                        String[] elements = line.split("=");

                        if ((elements != null) && (elements.length == 2) && (elements[0].equals("width"))) {
                            dimension.width = Integer.parseInt(elements[1]);

                        } else if ((elements != null) && (elements.length == 2) && (elements[0].equals("height"))) {
                            dimension.height = Integer.parseInt(elements[1]);
                        }

                    } catch (NumberFormatException ex) {
                        dimension = new Dimension(-1, -1);
                    }
                }
            }

            br.close();

        } catch (FileNotFoundException ex) {
            log.error("Error fichero " + filename + " no encontrado");

        } catch (IOException ex) {
            log.error("Error al leer de fichero " + filename);
        }

        return dimension;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        cmdMasterDisplayOff = new javax.swing.JButton();
        cmdMasterDisplayOn = new javax.swing.JButton();
        cmdMasterTermostatoOff = new javax.swing.JButton();
        cmdMasterTermostatoOn = new javax.swing.JButton();
        cmdSlaveDisplayOff = new javax.swing.JButton();
        cmdSlaveDisplayOn = new javax.swing.JButton();
        cmdSlaveTermostatoOff = new javax.swing.JButton();
        cmdSlaveTermostatoOn = new javax.swing.JButton();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        labelMasterLightSensor = new javax.swing.JLabel();
        labelMasterTemperature = new javax.swing.JLabel();
        labelSlaveLightSensor = new javax.swing.JLabel();
        labelSlaveTemperature = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        cmdSlaveScanLeds = new javax.swing.JButton();
        cmdMasterScanLeds = new javax.swing.JButton();
        labelSlaveErrorLeds = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        labelMasterErrorLeds = new javax.swing.JLabel();
        labelSlaveErrorModules = new javax.swing.JLabel();
        labelMasterErrorModules = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        labelSlaveDoor = new javax.swing.JLabel();
        labelMasterDoor = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        cmdLog = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtLog = new javax.swing.JTextArea();

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(" displayManager");
        setName("frameManager"); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));

        jLabel3.setBackground(new java.awt.Color(153, 153, 153));
        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("masterFace");
        jLabel3.setOpaque(true);

        jLabel8.setBackground(new java.awt.Color(153, 153, 153));
        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("slaveFace");
        jLabel8.setOpaque(true);

        cmdMasterDisplayOff.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cmdMasterDisplayOff.setText("OFF");
        cmdMasterDisplayOff.setFocusable(false);
        cmdMasterDisplayOff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdMasterDisplayOffActionPerformed(evt);
            }
        });

        cmdMasterDisplayOn.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cmdMasterDisplayOn.setText("ON");
        cmdMasterDisplayOn.setFocusable(false);
        cmdMasterDisplayOn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdMasterDisplayOnActionPerformed(evt);
            }
        });

        cmdMasterTermostatoOff.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cmdMasterTermostatoOff.setText("OFF");
        cmdMasterTermostatoOff.setFocusable(false);
        cmdMasterTermostatoOff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdMasterTermostatoOffActionPerformed(evt);
            }
        });

        cmdMasterTermostatoOn.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cmdMasterTermostatoOn.setText("ON");
        cmdMasterTermostatoOn.setFocusable(false);
        cmdMasterTermostatoOn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdMasterTermostatoOnActionPerformed(evt);
            }
        });

        cmdSlaveDisplayOff.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cmdSlaveDisplayOff.setText("OFF");
        cmdSlaveDisplayOff.setFocusable(false);
        cmdSlaveDisplayOff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdSlaveDisplayOffActionPerformed(evt);
            }
        });

        cmdSlaveDisplayOn.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cmdSlaveDisplayOn.setText("ON");
        cmdSlaveDisplayOn.setFocusable(false);
        cmdSlaveDisplayOn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdSlaveDisplayOnActionPerformed(evt);
            }
        });

        cmdSlaveTermostatoOff.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cmdSlaveTermostatoOff.setText("OFF");
        cmdSlaveTermostatoOff.setFocusable(false);
        cmdSlaveTermostatoOff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdSlaveTermostatoOffActionPerformed(evt);
            }
        });

        cmdSlaveTermostatoOn.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cmdSlaveTermostatoOn.setText("ON");
        cmdSlaveTermostatoOn.setFocusable(false);
        cmdSlaveTermostatoOn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdSlaveTermostatoOnActionPerformed(evt);
            }
        });

        jLabel29.setBackground(new java.awt.Color(255, 255, 255));
        jLabel29.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel29.setText("displayEncendido");

        jLabel30.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel30.setText("termostato");

        labelMasterLightSensor.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelMasterLightSensor.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        labelMasterLightSensor.setFocusable(false);
        labelMasterLightSensor.setName("labelMasterLightSensor"); // NOI18N

        labelMasterTemperature.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelMasterTemperature.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        labelMasterTemperature.setFocusable(false);
        labelMasterTemperature.setName("labelMasterTemperature"); // NOI18N

        labelSlaveLightSensor.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelSlaveLightSensor.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        labelSlaveLightSensor.setFocusable(false);
        labelSlaveLightSensor.setName("labelSlaveLightSensor"); // NOI18N

        labelSlaveTemperature.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelSlaveTemperature.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        labelSlaveTemperature.setFocusable(false);
        labelSlaveTemperature.setName("labelSlaveTemperature"); // NOI18N

        jLabel32.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel32.setText("fotocélula (curva)");

        jLabel34.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel34.setText("temperaturaInterior");

        cmdSlaveScanLeds.setText("scanLeds");
        cmdSlaveScanLeds.setFocusable(false);
        cmdSlaveScanLeds.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdSlaveScanLedsActionPerformed(evt);
            }
        });

        cmdMasterScanLeds.setText("scanLeds");
        cmdMasterScanLeds.setFocusable(false);
        cmdMasterScanLeds.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdMasterScanLedsActionPerformed(evt);
            }
        });

        labelSlaveErrorLeds.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelSlaveErrorLeds.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        labelSlaveErrorLeds.setFocusable(false);
        labelSlaveErrorLeds.setName("labelSlaveErrorLeds"); // NOI18N

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel4.setText("errorLeds");

        labelMasterErrorLeds.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelMasterErrorLeds.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        labelMasterErrorLeds.setFocusable(false);
        labelMasterErrorLeds.setName("labelMasterErrorLeds"); // NOI18N

        labelSlaveErrorModules.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelSlaveErrorModules.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        labelSlaveErrorModules.setFocusable(false);
        labelSlaveErrorModules.setName("labelSlaveErrorModules"); // NOI18N

        labelMasterErrorModules.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelMasterErrorModules.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        labelMasterErrorModules.setFocusable(false);
        labelMasterErrorModules.setName("labelMasterErrorModules"); // NOI18N

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel5.setText("filasMódulosConError   ");

        labelSlaveDoor.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelSlaveDoor.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        labelSlaveDoor.setName("labelSlaveDoor"); // NOI18N

        labelMasterDoor.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelMasterDoor.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        labelMasterDoor.setName("labelMasterDoor"); // NOI18N

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel6.setText("puerta");

        cmdLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdLogActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel29, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel34, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel30, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel32, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(labelMasterDoor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(labelMasterErrorModules, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cmdMasterScanLeds, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(labelMasterLightSensor, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(cmdMasterTermostatoOn, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmdMasterTermostatoOff))
                            .addComponent(labelMasterTemperature, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(cmdMasterDisplayOn, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmdMasterDisplayOff))
                            .addComponent(labelMasterErrorLeds, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(labelSlaveDoor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(labelSlaveErrorModules, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(labelSlaveErrorLeds, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cmdSlaveScanLeds, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(labelSlaveTemperature, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(labelSlaveLightSensor, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(cmdSlaveDisplayOn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(cmdSlaveTermostatoOn, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(cmdSlaveTermostatoOff, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(cmdSlaveDisplayOff)))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(cmdLog)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmdSlaveDisplayOff)
                    .addComponent(cmdSlaveDisplayOn)
                    .addComponent(cmdMasterDisplayOff)
                    .addComponent(cmdMasterDisplayOn)
                    .addComponent(jLabel29))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmdSlaveTermostatoOff)
                    .addComponent(cmdSlaveTermostatoOn)
                    .addComponent(cmdMasterTermostatoOff)
                    .addComponent(cmdMasterTermostatoOn)
                    .addComponent(jLabel30))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(labelSlaveLightSensor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labelMasterLightSensor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel32, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(labelSlaveTemperature, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labelMasterTemperature, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel34, javax.swing.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                    .addComponent(labelSlaveDoor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labelMasterDoor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmdSlaveScanLeds)
                    .addComponent(cmdMasterScanLeds))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                    .addComponent(labelSlaveErrorLeds, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labelMasterErrorLeds, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(labelSlaveErrorModules, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labelMasterErrorModules, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(cmdLog))
        );

        jPanel3.setBackground(new java.awt.Color(204, 204, 204));

        txtLog.setColumns(20);
        txtLog.setRows(5);
        jScrollPane1.setViewportView(txtLog);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmdMasterDisplayOnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdMasterDisplayOnActionPerformed
        //resaltarCmd(cmdMasterDisplayOn, true, colorLetraOn);
        //noResaltarCmd(cmdMasterDisplayOff);
        this.cmdSendCommand(CMD_MASTER_DISPLAY_ON);
    }//GEN-LAST:event_cmdMasterDisplayOnActionPerformed

    private void cmdMasterDisplayOffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdMasterDisplayOffActionPerformed
        //resaltarCmd(cmdMasterDisplayOff, true, colorLetraOff);
        //noResaltarCmd(cmdMasterDisplayOn);
        this.cmdSendCommand(CMD_MASTER_DISPLAY_OFF);
    }//GEN-LAST:event_cmdMasterDisplayOffActionPerformed

    private void cmdSlaveDisplayOnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdSlaveDisplayOnActionPerformed
        //resaltarCmd(cmdSlaveDisplayOn, true, colorLetraOn);
        //noResaltarCmd(cmdSlaveDisplayOff);
        this.cmdSendCommand(CMD_SLAVE_DISPLAY_ON);
    }//GEN-LAST:event_cmdSlaveDisplayOnActionPerformed

    private void cmdSlaveDisplayOffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdSlaveDisplayOffActionPerformed
        //resaltarCmd(cmdSlaveDisplayOff, true, colorLetraOff);
        //noResaltarCmd(cmdSlaveDisplayOn);
        this.cmdSendCommand(CMD_SLAVE_DISPLAY_OFF);
    }//GEN-LAST:event_cmdSlaveDisplayOffActionPerformed

    private void cmdMasterTermostatoOnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdMasterTermostatoOnActionPerformed
        //resaltarCmd(cmdMasterTermostatoOn, true, colorLetraCold);
        //noResaltarCmd(cmdMasterTermostatoOff);
        this.cmdSendCommand(CMD_MASTER_TERMOSTATO_ON);
    }//GEN-LAST:event_cmdMasterTermostatoOnActionPerformed

    private void cmdMasterTermostatoOffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdMasterTermostatoOffActionPerformed
        //resaltarCmd(cmdMasterTermostatoOff, true, colorLetraOff);
        //noResaltarCmd(cmdMasterTermostatoOn);
        this.cmdSendCommand(CMD_MASTER_TERMOSTATO_OFF);
    }//GEN-LAST:event_cmdMasterTermostatoOffActionPerformed

    private void cmdSlaveTermostatoOnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdSlaveTermostatoOnActionPerformed
        //resaltarCmd(cmdSlaveTermostatoOn, true, colorLetraCold);
        //noResaltarCmd(cmdSlaveTermostatoOff);
        this.cmdSendCommand(CMD_SLAVE_TERMOSTATO_ON);
    }//GEN-LAST:event_cmdSlaveTermostatoOnActionPerformed

    private void cmdSlaveTermostatoOffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdSlaveTermostatoOffActionPerformed
        //resaltarCmd(cmdSlaveTermostatoOff, true, colorLetraOff);
        //noResaltarCmd(cmdSlaveTermostatoOn);
        this.cmdSendCommand(CMD_SLAVE_TERMOSTATO_OFF);
    }//GEN-LAST:event_cmdSlaveTermostatoOffActionPerformed

    private void cmdMasterScanLedsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdMasterScanLedsActionPerformed
        this.cmdSendCommand(CMD_MASTER_SCAN_LEDS);
    }//GEN-LAST:event_cmdMasterScanLedsActionPerformed

    private void cmdSlaveScanLedsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdSlaveScanLedsActionPerformed
        this.cmdSendCommand(CMD_SLAVE_SCAN_LEDS);
    }//GEN-LAST:event_cmdSlaveScanLedsActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        try {
            Point point = this.getLocation();
            Dimension dimension = this.getSize();
            //String imageFileName = comboImagen.getSelectedItem().toString();
            //String excelFileName = comboExcel.getSelectedItem().toString();
            //int brightnessValue = jSlider1.getValue();
            //this.writeGuiInfo(point.x, point.y, dimension.width, dimension.height, imageFileName, excelFileName, brightnessValue, LOCATION_FILE);
            this.writeGuiInfo(point.x, point.y, dimension.width, dimension.height, LOCATION_FILE);
        } catch (Exception ex) {
            log.error("no se pudo escribir el archivo de posicionamiento.");
        }
    }//GEN-LAST:event_formWindowClosing

    private void cmdLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdLogActionPerformed
        if (Configuration.getInstance().isShowingLog()) {
            Configuration.getInstance().setShowingLog(false);
            setSize(this.getWidth(), jPanel1.getPreferredSize().height
                    + getInsets().top
                    + getInsets().bottom
                    + 11);   // el reborde interior del jPanel
        } else {
            Configuration.getInstance().setShowingLog(true);
            setSize(this.getWidth(), jPanel1.getHeight()
                    + jPanel3.getPreferredSize().height
                    + getInsets().top
                    + getInsets().bottom);
        }
    }//GEN-LAST:event_cmdLogActionPerformed

    private void cmdSendCommand(String id) {
        JFrameCommand jFrameCommand = Configuration.getInstance().getJFrameCommand(id);
        if ((agent != null) && (jFrameCommand != null)) {
            String cardId = jFrameCommand.getCardId();
            String command = jFrameCommand.getCommand();
            String faceId = jFrameCommand.getFaceId();
            agent.sendCommand(cardId, command, faceId);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Manager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Manager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Manager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Manager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Manager().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cmdLog;
    private javax.swing.JButton cmdMasterDisplayOff;
    private javax.swing.JButton cmdMasterDisplayOn;
    private javax.swing.JButton cmdMasterScanLeds;
    private javax.swing.JButton cmdMasterTermostatoOff;
    private javax.swing.JButton cmdMasterTermostatoOn;
    private javax.swing.JButton cmdSlaveDisplayOff;
    private javax.swing.JButton cmdSlaveDisplayOn;
    private javax.swing.JButton cmdSlaveScanLeds;
    private javax.swing.JButton cmdSlaveTermostatoOff;
    private javax.swing.JButton cmdSlaveTermostatoOn;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelMasterDoor;
    private javax.swing.JLabel labelMasterErrorLeds;
    private javax.swing.JLabel labelMasterErrorModules;
    private javax.swing.JLabel labelMasterLightSensor;
    private javax.swing.JLabel labelMasterTemperature;
    private javax.swing.JLabel labelSlaveDoor;
    private javax.swing.JLabel labelSlaveErrorLeds;
    private javax.swing.JLabel labelSlaveErrorModules;
    private javax.swing.JLabel labelSlaveLightSensor;
    private javax.swing.JLabel labelSlaveTemperature;
    private javax.swing.JTextArea txtLog;
    // End of variables declaration//GEN-END:variables
}