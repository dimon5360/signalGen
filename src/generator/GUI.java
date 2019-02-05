package generator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import generator.Console;
import generator.WavConverter;

public class GUI {
	// GUI
	private JPanel panel;
	private JFrame frame;
	private JButton butStart, butSelDir;
	private JLabel signalLengthLabel, freqDiscreteLabel, baseFreqLabel, distortionCoefLabel;
	private JLabel linearVoltage12Label, linearVoltage13Label;
	private JLabel energyAct1Label, energyAct2Label, energyAct3Label;
	private JLabel energyReact1Label, energyReact2Label, energyReact3Label;
	private JLabel powerAct1Label, powerAct2Label, powerAct3Label;
	private JLabel powerReact1Label, powerReact2Label, powerReact3Label;
	private JLabel amplLabelU1, amplLabelU2, amplLabelU3;
	private JLabel amplLabelI1, amplLabelI2, amplLabelI3;
	private JLabel phase12Label, phase13Label, phaseIU1Label, phaseIU2Label, phaseIU3Label;
	private JLabel channelsLabel, errorLabel, distortionLabel;
	private JTable table;
	private JTextField signalLengthField, freqDiscreteField, baseFreqField, distortionCoefField;
	private JTextField linearVoltage12Field, linearVoltage13Field;
	private JTextField energyAct1Field, energyAct2Field, energyAct3Field;
	private JTextField energyReact1Field, energyReact2Field, energyReact3Field;
	private JTextField powerAct1Field, powerAct2Field, powerAct3Field;
	private JTextField powerReact1Field, powerReact2Field, powerReact3Field;
	private JTextField amplFieldU1, amplFieldU2, amplFieldU3;
	private JTextField amplFieldI1, amplFieldI2 ,amplFieldI3;
	private JTextField phase12Field, phase13Field, phaseIU1Field, phaseIU2Field, phaseIU3Field;
	private JComboBox<String> box;
	private Box contents;
	private Random random;
	private int numChannel;
	public static JRadioButton channel1, channel2, channel3, channel4, channel5, channel6;
	public static JRadioButton coefChannel1, coefChannel2, coefChannel3, coefChannel4, coefChannel5, coefChannel6;
	
	private double ampl; 							// Output signal amplitude.
	private double baseFreq;						// Base frequency.
	private double [] ampl_U0;						// Voltage amplitudes array.
	private double [] ampl_I0;						// Current amplitudes array.
	private double [] phase;						// Interphase angles array.
	private double [] phaseIU;						// Phase shift angles array.
	public double t; 								// Current time.
	public int k; 									// Harmonic order number.
	public double[][] coeff; 						// Harmonic coefficient (for 6 channel up to 50 harmonics).
	public Object c;			
	public double freqDiscrete = 0; 				// Discrete frequency.
	private double[][] value; 						// Output amplitudes array.
	private double[] time; 							// Time array.

	public String[] comboBoxItems = { "Voltage harmonic", "Current harmonic" };
	public static String coef_begin = "0.1"; 		// Bottom border of voltage harmonic coefficient.

	private static Logger log; 						// Logs.
	private String configDirect = "../generator.ini";
	private String refDirect = "../output/";
	public double endSignal = 0; 					// Signal length.

	private int wordNum;
	public double t_growth; 						// Time increment, s.
	public int frameSizeX = 850, frameSizeY = 690;  // Window size.

	public ArrayList<String[]> tableData;
	public String[][] dict;
	
	public int j, masLen, numPeriods;
	public String version;
	private int countError = 0;
    private byte channels[] = {0,0,0,0,0,0};
    private byte channelsCoef[] = {0,0,0,0,0,0};
    

	static double fi_garmonic = 0;
	
	public GUI() throws Exception 
	{			
		version = "v.1.3";
		dict = new String[200][200];
		coeff = new double[5][10];
		random = new Random();
		phase = new double[2];
		phaseIU = new double[3];
		ampl_U0 = new double[3];
		ampl_I0 = new double[3];
		
		try (FileReader reader = new FileReader(configDirect)) {

			int c, count = 0, charNum;
			for (int p = 0; p < dict.length; p++) 
			{
				for (charNum = 0; charNum < 2; charNum++) 
				{
					dict[p][charNum] = "";
				}
			}
			wordNum = 0;
			charNum = 0;
			while ((c = reader.read()) != -1) 
			{				
				if (c == 0x00)
					continue;
				else if (c == 0x0A && count > 1) 	// 0x0A - '\n'
				{
					wordNum++;
					charNum = 0;
					continue;
				}
				else if (c == 0x3A) 				// 0x3A - ':'
				{
					charNum++;
					continue;
				}

				dict[wordNum][charNum] += (char) c;
				count++;
			}

			try 
			{				
				freqDiscrete = Double.parseDouble(dict[0][1]);
				endSignal = Double.parseDouble(dict[1][1]);
				baseFreq = Double.parseDouble(dict[2][1]);
				ampl_U0[0] = Double.parseDouble(dict[3][1]);
				ampl_U0[1] = Double.parseDouble(dict[4][1]);
				ampl_U0[2] = Double.parseDouble(dict[5][1]);
				ampl_I0[0] = Double.parseDouble(dict[6][1]);
				ampl_I0[1] = Double.parseDouble(dict[7][1]);
				ampl_I0[2] = Double.parseDouble(dict[8][1]);
				phase[0] = Double.parseDouble(dict[9][1]);
				phase[1] = Double.parseDouble(dict[10][1]);
				phaseIU[0] = Double.parseDouble(dict[11][1]);
				phaseIU[1] = Double.parseDouble(dict[12][1]);
				phaseIU[2] = Double.parseDouble(dict[13][1]);				
				
			} catch (NumberFormatException num) {
				System.out.println("Some data in the *.ini file is missing." + num);
				errorLabel.setText("Some data in the *.ini file is missing." + num);

			}
		} catch (IOException ex) {

			System.out.println(ex.getMessage());
			errorLabel.setText(ex.getMessage());
		}
		try {
			log = Logger.getLogger(GUI.class.getName());
		} catch (Exception ex) {
			log.info(ex.getMessage());
			errorLabel.setText(ex.getMessage());
		}

		CreateGUI();
	}
	
	public void CreateGUI() 
	{
		// New Frame
		frame = new JFrame("MIMT generator " + version);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowListener() {

			public void windowActivated(WindowEvent event) {
			}

			public void windowClosed(WindowEvent event) {
			}

			public void windowDeactivated(WindowEvent event) {
			}

			public void windowDeiconified(WindowEvent event) {
			}

			public void windowIconified(WindowEvent event) {
			}

			public void windowOpened(WindowEvent event) {
			}

			public void windowClosing(WindowEvent event) {
				Object[] options = { "Yes", "No" };
				int n = JOptionPane.showOptionDialog(event.getWindow(), "Save data?", "Confirm",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				if (n == 0) {
					try (FileWriter writer = new FileWriter(configDirect)) {
						
						String freq = freqDiscreteField.getText();
						String quant = signalLengthField.getText();
						String baseF = baseFreqField.getText();
						String amplU1 = amplFieldU1.getText();
						String amplU2 = amplFieldU2.getText();
						String amplU3 = amplFieldU3.getText();
						String amplI1 = amplFieldI1.getText();
						String amplI2 = amplFieldI2.getText();
						String amplI3 = amplFieldI3.getText();
						String phase12 = phase12Field.getText();
						String phase13 = phase13Field.getText();
						String phaseIU1 = phaseIU1Field.getText();
						String phaseIU2 = phaseIU2Field.getText();
						String phaseIU3 = phaseIU3Field.getText();

						if (freq != null && quant != null && baseF != null && amplU1 != null && amplU2 != null && amplU3 != null) 
						{
							writer.write("Discrete frequency, Hz : " + freq);
							writer.write("\r\n");
							writer.write("Signal duration, s : " + quant);
							writer.write("\r\n");
							writer.write("Base frequency, Hz : " + baseF);
							writer.write("\r\n");
							writer.write("Amplitude U1, V : " + amplU1);
							writer.write("\r\n");
							writer.write("Amplitude U2, V : " + amplU2);
							writer.write("\r\n");
							writer.write("Amplitude U3, V : " + amplU3);
							writer.write("\r\n");
							writer.write("Amplitude I1, mA : " + amplI1);
							writer.write("\r\n");
							writer.write("Amplitude I2, mA : " + amplI2);
							writer.write("\r\n");
							writer.write("Amplitude I3, mA : " + amplI3);
							writer.write("\r\n");
							writer.write("Shift between phases 1 and 2 : " + phase12);
							writer.write("\r\n");
							writer.write("Shift between phases 1 and 3 : " + phase13);
							writer.write("\r\n");
							writer.write("Phase shift between U1 and I1 : " + phaseIU1);
							writer.write("\r\n");
							writer.write("Phase shift between U2 and I2 : " + phaseIU2);
							writer.write("\r\n");
							writer.write("Phase shift between U3 and I3 : " + phaseIU3);
						}

						int index = 0;
						for (int l = 0; l < 5; l++) {
							for (int m = 0; m < 10; m++) {
								index = l * 10 + m + 1;
								writer.write("\r\n");
								try {
									c = table.getValueAt(l, m);
									if (c.toString() == "") {
										coeff[l][m] = 0;
									}  
									else if(index == 1)	{
										coeff[l][m] = 100;
									}
									else {
										coeff[l][m] = Double.parseDouble(c.toString());
									}

									writer.write("Coefficient " + index + ", % : " + Double.toString(coeff[l][m]));
								} catch (NumberFormatException num) {
									log.info("Input data error: " + num.getMessage());
									errorLabel.setText("Input data error: " + num.getMessage());

								}
							}
						}
					} 
					catch (IOException ex) {

						System.out.println(ex.getMessage());
						errorLabel.setText(ex.getMessage());
					}
					
					System.gc();
					Runtime.getRuntime().gc();

					event.getWindow().setVisible(false);
					System.exit(0);
				}
				else
				{					
					System.gc();
					Runtime.getRuntime().gc();

					event.getWindow().setVisible(false);
					System.exit(0);
				}
			}
		});

		frame.setBounds(100, 100, frameSizeX, frameSizeY);
		frame.setResizable(false); // Window resize is disable.

		panel = new JPanel();
		panel.setLayout(null);
		frame.add(panel);
		
		errorLabel = new JLabel();
		errorLabel.setBounds(200, 620, 550, 20);
		panel.add(errorLabel);	

		box = new JComboBox<String>(comboBoxItems);
		box.setBounds(10, 150, 160, 20);
		panel.add(box);
			
		
		
		channelsLabel = new JLabel("Channel:      1     2     3     4     5     6");
		channelsLabel.setBounds(5, 578, 250, 20);
		panel.add(channelsLabel);
		

		distortionLabel = new JLabel("Coefficient: 1     2     3     4     5     6");
		distortionLabel.setBounds(5, 618, 250, 20);
		panel.add(distortionLabel);
		
		channel1 = new JRadioButton();
		channel1.setActionCommand("Button pressed");
		channel1.setBounds(65, 598, 22, 12);
		channel1.setSelected(false);
	    panel.add(channel1);

		channel2 = new JRadioButton();
		channel2.setActionCommand("Button pressed");
		channel2.setBounds(86, 598, 22, 12);
		channel2.setSelected(false);
	    panel.add(channel2);

		channel3 = new JRadioButton();
		channel3.setActionCommand("Button pressed");
		channel3.setBounds(108, 598, 22, 12);
		channel3.setSelected(false);
	    panel.add(channel3);

	    channel4 = new JRadioButton();
	    channel4.setActionCommand("Button pressed");
	    channel4.setBounds(130, 598, 22, 12);
	    channel4.setSelected(false);
	    panel.add(channel4);
		
	    channel5 = new JRadioButton();
	    channel5.setActionCommand("Button pressed");
	    channel5.setBounds(152, 598, 22, 12);
	    channel5.setSelected(false);
	    panel.add(channel5);

	    channel6 = new JRadioButton();
	    channel6.setActionCommand("Button pressed");
	    channel6.setBounds(174, 598, 22, 12);
	    channel6.setSelected(false);
	    panel.add(channel6);

	    coefChannel1 = new JRadioButton();
	    coefChannel1.setActionCommand("Button pressed");
	    coefChannel1.setBounds(65, 638, 22, 12);
	    coefChannel1.setSelected(false);
	    panel.add(coefChannel1);

	    coefChannel2 = new JRadioButton();
	    coefChannel2.setActionCommand("Button pressed");
	    coefChannel2.setBounds(86, 638, 22, 12);
	    coefChannel2.setSelected(false);
	    panel.add(coefChannel2);

	    coefChannel3 = new JRadioButton();
	    coefChannel3.setActionCommand("Button pressed");
	    coefChannel3.setBounds(108, 638, 22, 12);
	    coefChannel3.setSelected(false);
	    panel.add(coefChannel3);

	    coefChannel4 = new JRadioButton();
	    coefChannel4.setActionCommand("Button pressed");
	    coefChannel4.setBounds(130, 638, 22, 12);
	    coefChannel4.setSelected(false);
	    panel.add(coefChannel4);
		
	    coefChannel5 = new JRadioButton();
	    coefChannel5.setActionCommand("Button pressed");
	    coefChannel5.setBounds(152, 638, 22, 12);
	    coefChannel5.setSelected(false);
	    panel.add(coefChannel5);

	    coefChannel6 = new JRadioButton();
	    coefChannel6.setActionCommand("Button pressed");
	    coefChannel6.setBounds(174, 638, 22, 12);
	    coefChannel6.setSelected(false);
	    panel.add(coefChannel6);
	    

		
		butStart = new JButton("Start");
		butStart.setBounds(frameSizeX - 120, frameSizeY - 70, 100, 20);
		butStart.setActionCommand("Button pressed");
		panel.add(butStart);

		signalLengthLabel = new JLabel("Signal, s:");
		signalLengthLabel.setBounds(10, 50, 160, 20);
		panel.add(signalLengthLabel);
		signalLengthField = new JTextField();
		signalLengthField.setBounds(10, 70, 160, 20);
		panel.add(signalLengthField);
		if (endSignal != 0)
			signalLengthField.setText(Double.toString(endSignal));

		freqDiscreteLabel = new JLabel("Discrete frequency, Hz:");
		freqDiscreteLabel.setBounds(10, 90, 160, 20);
		panel.add(freqDiscreteLabel);
		freqDiscreteField = new JTextField();
		freqDiscreteField.setBounds(10, 110, 160, 20);
		panel.add(freqDiscreteField);
		if (freqDiscrete != 0)
			freqDiscreteField.setText(Double.toString(freqDiscrete));

		int tableX = 120, tableY = 260;
		JLabel lableGarmonick1 = new JLabel("1 < N < 10");
		lableGarmonick1.setBounds(30, tableY, 100, 30);
		panel.add(lableGarmonick1);
		JLabel lableGarmonick2 = new JLabel("11 < N < 20");
		lableGarmonick2.setBounds(30, tableY + 30, 100, 30);
		panel.add(lableGarmonick2);
		JLabel lableGarmonick3 = new JLabel("21 < N < 30");
		lableGarmonick3.setBounds(30, tableY + 60, 100, 30);
		panel.add(lableGarmonick3);
		JLabel lableGarmonick4 = new JLabel("31 < N < 40");
		lableGarmonick4.setBounds(30, tableY + 90, 100, 30);
		panel.add(lableGarmonick4);
		JLabel lableGarmonick5 = new JLabel("41 < N < 50");
		lableGarmonick5.setBounds(30, tableY + 120, 100, 30);
		panel.add(lableGarmonick5);

		JLabel lable1 = new JLabel("1");
		lable1.setBounds(tableX + 25, tableY - 20, 50, 20);
		panel.add(lable1);
		JLabel lable2 = new JLabel("2");
		lable2.setBounds(tableX + 85, tableY - 20, 50, 20);
		panel.add(lable2);
		JLabel lable3 = new JLabel("3");
		lable3.setBounds(tableX + 145, tableY - 20, 50, 20);
		panel.add(lable3);
		JLabel lable4 = new JLabel("4");
		lable4.setBounds(tableX + 205, tableY - 20, 50, 20);
		panel.add(lable4);
		JLabel lable5 = new JLabel("5");
		lable5.setBounds(tableX + 265, tableY - 20, 50, 20);
		panel.add(lable5);
		JLabel lable6 = new JLabel("6");
		lable6.setBounds(tableX + 325, tableY - 20, 50, 20);
		panel.add(lable6);
		JLabel lable7 = new JLabel("7");
		lable7.setBounds(tableX + 385, tableY - 20, 50, 20);
		panel.add(lable7);
		JLabel lable8 = new JLabel("8");
		lable8.setBounds(tableX + 445, tableY - 20, 50, 20);
		panel.add(lable8);
		JLabel lable9 = new JLabel("9");
		lable9.setBounds(tableX + 505, tableY - 20, 50, 20);
		panel.add(lable9);
		JLabel lable10 = new JLabel("10");
		lable10.setBounds(tableX + 560, tableY - 20, 50, 20);
		panel.add(lable10);

		JLabel coef1 = new JLabel();
		coef1.setBounds(730, tableY, 100, 30);
		panel.add(coef1);
		JLabel coef2 = new JLabel();
		coef2.setBounds(730, tableY + 30, 100, 30);
		panel.add(coef2);
		JLabel coef3 = new JLabel();
		coef3.setBounds(730, tableY + 60, 100, 30);
		panel.add(coef3);
		JLabel coef4 = new JLabel();
		coef4.setBounds(730, tableY + 90, 100, 30);
		panel.add(coef4);
		JLabel coef5 = new JLabel();
		coef5.setBounds(730, tableY + 120, 100, 30);
		panel.add(coef5);

		tableData = new ArrayList<String[]>();
		String[][] data = new String[5][10];

		int cnt = 14;
		for (int n = 0; n < 5; n++) {
			for (int m = 0; m < 10; m++) {
				data[n][m] = dict[cnt][1];
				cnt++;
			}
			tableData.add(data[n]);
		}
		MyModel model = new MyModel(tableData, true);
		table = new JTable(model);

		table.setRowHeight(30);
		table.setShowVerticalLines(true);
		table.setShowHorizontalLines(true);
		table.setToolTipText("Value in percent"); 
																

		contents = new Box(BoxLayout.Y_AXIS);
		contents.setBounds(tableX, tableY, 600, 500);
		contents.add(table);
		panel.add(contents);

		baseFreqLabel = new JLabel("Frequency, Hz:");
		baseFreqLabel.setBounds(10, 10, 160, 20);
		panel.add(baseFreqLabel);

		baseFreqField = new JTextField();
		baseFreqField.setBounds(10, 30, 160, 20);
		panel.add(baseFreqField);
		if (baseFreq != 0)
			baseFreqField.setText(Double.toString(baseFreq));

		
		amplLabelU1 = new JLabel("Voltage, V:");
		amplLabelU1.setBounds(410, 10, 160, 20);
		panel.add(amplLabelU1);
		amplFieldU1 = new JTextField();
		amplFieldU1.setBounds(410, 30, 160, 20);
		panel.add(amplFieldU1);
		amplFieldU1.setToolTipText("from 0 to 265 V");
		if (ampl_U0[0] != 0)
			amplFieldU1.setText(Double.toString(ampl_U0[0]));
		
		amplLabelU2 = new JLabel("Voltage, V:");
		amplLabelU2.setBounds(410, 50, 160, 20);
		panel.add(amplLabelU2);
		amplFieldU2 = new JTextField();
		amplFieldU2.setBounds(410, 70, 160, 20);
		panel.add(amplFieldU2);
		amplFieldU2.setToolTipText("from 0 to 265 V");
		if (ampl_U0[1] != 0)
			amplFieldU2.setText(Double.toString(ampl_U0[1]));
		
		amplLabelU3 = new JLabel("Voltage, V:");
		amplLabelU3.setBounds(410, 90, 160, 20);
		panel.add(amplLabelU3);
		amplFieldU3 = new JTextField();
		amplFieldU3.setBounds(410, 110, 160, 20);
		panel.add(amplFieldU3);
		amplFieldU3.setToolTipText("from 0 to 265 V");
		if (ampl_U0[2] != 0)
			amplFieldU3.setText(Double.toString(ampl_U0[2]));

		
		amplLabelI1 = new JLabel("Current, mA:");
		amplLabelI1.setBounds(210, 10, 160, 20);
		panel.add(amplLabelI1);
		amplFieldI1 = new JTextField();
		amplFieldI1.setBounds(210, 30, 160, 20);
		panel.add(amplFieldI1);
		amplFieldI1.setToolTipText("from 0 to 60 mA");
		if(ampl_I0[0] != 0)
			amplFieldI1.setText(Double.toString(ampl_I0[0]));

		amplLabelI2 = new JLabel("Current, mA:");
		amplLabelI2.setBounds(210, 50, 160, 20);
		panel.add(amplLabelI2);
		amplFieldI2 = new JTextField();
		amplFieldI2.setBounds(210, 70, 160, 20);
		panel.add(amplFieldI2);
		amplFieldI2.setToolTipText("from 0 to 60 mA");
		if(ampl_I0[1] != 0)
			amplFieldI2.setText(Double.toString(ampl_I0[1]));
		
		amplLabelI3 = new JLabel("Current, mA:");
		amplLabelI3.setBounds(210, 90, 160, 20);
		panel.add(amplLabelI3);
		amplFieldI3 = new JTextField();
		amplFieldI3.setBounds(210, 110, 160, 20);
		panel.add(amplFieldI3);
		amplFieldI3.setToolTipText("from 0 to 60 mA");
		if(ampl_I0[2] != 0)
			amplFieldI3.setText(Double.toString(ampl_I0[2]));
		
		phase12Label = new JLabel("Interphase shift 1,2:");
		phase12Label.setBounds(610, 130, 160, 20);
		panel.add(phase12Label);
		phase12Field = new JTextField();
		phase12Field.setBounds(610, 150, 160, 20);
		panel.add(phase12Field);
		phase12Field.setToolTipText("from -180 to 180 degree.");
		phase12Field.setText(Double.toString(phase[0]));

		phase13Label = new JLabel("Interphase shift 1,3:");
		phase13Label.setBounds(610, 170, 160, 20);
		panel.add(phase13Label);
		phase13Field = new JTextField();
		phase13Field.setBounds(610, 190, 160, 20);
		panel.add(phase13Field);
		phase13Field.setToolTipText("from -180 to 180 degree.");
		phase13Field.setText(Double.toString(phase[1]));

		
		phaseIU1Label = new JLabel("Shift 1st phase:");
		phaseIU1Label.setBounds(610, 10, 160, 20);
		panel.add(phaseIU1Label);
		phaseIU1Field = new JTextField();
		phaseIU1Field.setBounds(610, 30, 160, 20);
		panel.add(phaseIU1Field);
		phaseIU1Field.setToolTipText("from -180 to 180 degree.");
		phaseIU1Field.setText(Double.toString(phaseIU[0]));
		
		phaseIU2Label = new JLabel("Shift 2nd phase:");
		phaseIU2Label.setBounds(610, 50, 160, 20);
		panel.add(phaseIU2Label);
		phaseIU2Field = new JTextField();
		phaseIU2Field.setBounds(610, 70, 160, 20);
		panel.add(phaseIU2Field);
		phaseIU2Field.setToolTipText("from -180 to 180 degree.");
		phaseIU2Field.setText(Double.toString(phaseIU[1]));
		
		phaseIU3Label = new JLabel("Shift 3rd phase:");
		phaseIU3Label.setBounds(610, 90, 160, 20);
		panel.add(phaseIU3Label);
		phaseIU3Field = new JTextField();
		phaseIU3Field.setBounds(610, 110, 160, 20);
		panel.add(phaseIU3Field);
		phaseIU3Field.setToolTipText("from -180 to 180 degree.");
		phaseIU3Field.setText(Double.toString(phaseIU[2]));	
		
		
		distortionCoefLabel = new JLabel("Distortion: ");
		distortionCoefLabel.setBounds(10, frameSizeY - 170, 180, 20);
		panel.add(distortionCoefLabel);
		distortionCoefField = new JTextField();
		distortionCoefField.setBounds(10, frameSizeY - 150, 180, 20);
		panel.add(distortionCoefField);
		distortionCoefField.setEditable(false);

		powerAct1Label = new JLabel("Active power: ");
		powerAct1Label.setBounds(200, frameSizeY - 250, 150, 20);
		panel.add(powerAct1Label);
		powerAct1Field = new JTextField();
		powerAct1Field.setBounds(200, frameSizeY - 230, 150, 20);
		panel.add(powerAct1Field);
		powerAct1Field.setEditable(false);
		
		powerAct2Label = new JLabel("Active power: ");
		powerAct2Label.setBounds(200, frameSizeY - 210, 150, 20);
		panel.add(powerAct2Label);
		powerAct2Field = new JTextField();
		powerAct2Field.setBounds(200, frameSizeY - 190, 150, 20);
		panel.add(powerAct2Field);
		powerAct2Field.setEditable(false);
		
		powerAct3Label = new JLabel("Active power: ");
		powerAct3Label.setBounds(200, frameSizeY - 170, 150, 20);
		panel.add(powerAct3Label);
		powerAct3Field = new JTextField();
		powerAct3Field.setBounds(200, frameSizeY - 150, 150, 20);
		panel.add(powerAct3Field);
		powerAct3Field.setEditable(false);
		
		powerReact1Label = new JLabel("Reactive power: ");
		powerReact1Label.setBounds(360, frameSizeY - 250, 150, 20);
		panel.add(powerReact1Label);
		powerReact1Field = new JTextField();
		powerReact1Field.setBounds(360, frameSizeY - 230, 150, 20);
		panel.add(powerReact1Field);
		powerReact1Field.setEditable(false);
		
		powerReact2Label = new JLabel("Reactive power: ");
		powerReact2Label.setBounds(360, frameSizeY - 210, 150, 20);
		panel.add(powerReact2Label);
		powerReact2Field = new JTextField();
		powerReact2Field.setBounds(360, frameSizeY - 190, 150, 20);
		panel.add(powerReact2Field);
		powerReact2Field.setEditable(false);
		
		powerReact3Label = new JLabel("Reactive power: ");
		powerReact3Label.setBounds(360, frameSizeY - 170, 150, 20);
		panel.add(powerReact3Label);
		powerReact3Field = new JTextField();
		powerReact3Field.setBounds(360, frameSizeY - 150, 150, 20);
		panel.add(powerReact3Field);
		powerReact3Field.setEditable(false);		
		
		energyAct1Label = new JLabel("Active energy: ");
		energyAct1Label.setBounds(530, frameSizeY - 250, 130, 20);
		panel.add(energyAct1Label);
		energyAct1Field = new JTextField();
		energyAct1Field.setBounds(530, frameSizeY - 230, 130, 20);
		panel.add(energyAct1Field);
		energyAct1Field.setEditable(false);

		energyAct2Label = new JLabel("Active energy: ");
		energyAct2Label.setBounds(530, frameSizeY - 210, 130, 20);
		panel.add(energyAct2Label);
		energyAct2Field = new JTextField();
		energyAct2Field.setBounds(530, frameSizeY - 190, 130, 20);
		panel.add(energyAct2Field);
		energyAct2Field.setEditable(false);
		
		energyAct3Label = new JLabel("Active energy: ");
		energyAct3Label.setBounds(530, frameSizeY - 170, 130, 20);
		panel.add(energyAct3Label);
		energyAct3Field = new JTextField();
		energyAct3Field.setBounds(530, frameSizeY - 150, 130, 20);
		panel.add(energyAct3Field);
		energyAct3Field.setEditable(false);
		
		energyReact1Label = new JLabel("Reactive energy: ");
		energyReact1Label.setBounds(670, frameSizeY - 250, 140, 20);
		panel.add(energyReact1Label);
		energyReact1Field = new JTextField();
		energyReact1Field.setBounds(670, frameSizeY - 230, 140, 20);
		panel.add(energyReact1Field);
		energyReact1Field.setEditable(false);

		energyReact2Label = new JLabel("Reactive energy: ");
		energyReact2Label.setBounds(670, frameSizeY - 210, 140, 20);
		panel.add(energyReact2Label);
		energyReact2Field = new JTextField();
		energyReact2Field.setBounds(670, frameSizeY - 190, 140, 20);
		panel.add(energyReact2Field);
		energyReact2Field.setEditable(false);
		
		energyReact3Label = new JLabel("Reactive energy: ");
		energyReact3Label.setBounds(670, frameSizeY - 170, 140, 20);
		panel.add(energyReact3Label);
		energyReact3Field = new JTextField();
		energyReact3Field.setBounds(670, frameSizeY - 150, 140, 20);
		panel.add(energyReact3Field);
		energyReact3Field.setEditable(false);
		
		linearVoltage12Label = new JLabel("Interphase voltage U12:");
		linearVoltage12Label.setBounds(10, frameSizeY - 250, 180, 20);
		panel.add(linearVoltage12Label);
		linearVoltage12Field = new JTextField();
		linearVoltage12Field.setBounds(10, frameSizeY - 230, 180, 20);
		panel.add(linearVoltage12Field);
		linearVoltage12Field.setToolTipText("Interphase voltage U12:");
		linearVoltage12Field.setEditable(false);
		
		linearVoltage13Label = new JLabel("Interphase voltage U13:");
		linearVoltage13Label.setBounds(10, frameSizeY - 210, 180, 20);
		panel.add(linearVoltage13Label);
		linearVoltage13Field = new JTextField();
		linearVoltage13Field.setBounds(10, frameSizeY - 190, 180, 20);
		panel.add(linearVoltage13Field);
		linearVoltage13Field.setToolTipText("Interphase voltage U13:");
		linearVoltage13Field.setEditable(false);
		
		butStart.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{				
				int l = 0, m = 0;
				long start = System.currentTimeMillis();
				
				try 
				{
					try 
					{
						for (l = 0; l < 5; l++) 
						{
							for (m = 0; m < 10; m++) 
							{
								coeff[l][m] = Double.parseDouble(table.getValueAt(l, m).toString());								
							}
						}
						
						// Main frequency
						if ((baseFreq = Double.parseDouble(baseFreqField.getText())) == 0.0
								|| (baseFreqField.getText()) == null) {
							baseFreq = 50;
							countError++;
							log.info("Base signal frequency is not defined. Default value is 50 Hz.");
							errorLabel.setText("Base signal frequency is not defined. Default value is 50 Hz.");
						}
						//Discrete frequency
						if ((freqDiscrete = Double.parseDouble(freqDiscreteField.getText())) == 0.0
								|| (freqDiscreteField.getText()) == null) {
							freqDiscrete = 5000;
							countError++;
							log.info("Discrete signal frequency is not defined. Default value is 5 KHz.");
							errorLabel.setText("Discrete signal frequency is not defined. Default value is 5 KHz.");
						}
						//Signal length
						if ((endSignal = Double.parseDouble(signalLengthField.getText())) == 0.0
								|| (signalLengthField.getText()) == null) {
							endSignal = 1;
							countError++;
							log.info("Signal Length is not defined. Default value is 1 sec.");
							errorLabel.setText("Signal Length is not defined. Default value is 1 sec.");
						}
						
						//Voltage amplitude
						if ((ampl_U0[0] = Double.parseDouble(amplFieldU1.getText())) == 0.0
								|| (ampl_U0[0] = Double.parseDouble(amplFieldU1.getText())) > 265
								|| (amplFieldU1.getText()) == null) {
							ampl_U0[0] = 265;
							countError++;
							log.info("Voltage amplitude is not true. Default value is 265 V.");
							errorLabel.setText("Voltage amplitude is not true. Default value is 265 V.");
						}
						if (((ampl_U0[1] = Double.parseDouble(amplFieldU2.getText())) == 0.0)
								|| ((ampl_U0[1] = Double.parseDouble(amplFieldU2.getText())) > 265)
								|| ((amplFieldU2.getText()) == null)) {
							ampl_U0[1] = 265;
							countError++;
							log.info("Voltage amplitude is not true. Default value is 265 V.");
							errorLabel.setText("Voltage amplitude is not true. Default value is 265 V.");
						}
						if ((ampl_U0[2] = Double.parseDouble(amplFieldU3.getText())) == 0.0
								|| (ampl_U0[2] = Double.parseDouble(amplFieldU3.getText())) > 265
								|| (amplFieldU3.getText()) == null) {
							ampl_U0[2] = 265;
							countError++;
							log.info("Voltage amplitude is not true. Default value is 265 V.");
							errorLabel.setText("Voltage amplitude is not true. Default value is 265 V.");
						}
						
						//Curernt amplitude
						if ((ampl_I0[0] = Double.parseDouble(amplFieldI1.getText())) == 0.0
								|| (ampl_I0[0] = Double.parseDouble(amplFieldI1.getText())) > 60
								|| (amplFieldI1.getText()) == null) {
							ampl_I0[0] = 60;
							countError++;
							log.info("Current amplitude is not true. Default value is 60 mA.");
							errorLabel.setText("Current amplitude is not true. Default value is 60 mA.");
						}
						if ((ampl_I0[1] = Double.parseDouble(amplFieldI2.getText())) == 0.0
								|| (ampl_I0[1] = Double.parseDouble(amplFieldI2.getText())) > 60
								|| (amplFieldI2.getText()) == null) {
							ampl_I0[1] = 60;
							countError++;
							log.info("Current amplitude is not true. Default value is 60 mA.");
							errorLabel.setText("Current amplitude is not true. Default value is 60 mA.");
						}
						if ((ampl_I0[2] = Double.parseDouble(amplFieldI3.getText())) == 0.0
								|| (ampl_I0[2] = Double.parseDouble(amplFieldI3.getText())) > 60
								|| (amplFieldI3.getText()) == null) {
							ampl_I0[2] = 60;
							countError++;
							log.info("Current amplitude is not true. Default value is 60 mA.");
							errorLabel.setText("Current amplitude is not true. Default value is 60 mA.");
						}
						
						//Phases
						if ((phase[0] = Double.parseDouble(phase12Field.getText())) > 180
							|| (phase[0] = Double.parseDouble(phase12Field.getText())) < -180
							|| (phase12Field.getText()) == null) {
							phase[0] = 0;
							countError++;
							log.info("Shift between 1 and 2 phases is not true. Default value is 120 degrees.");
							errorLabel.setText("Shift between 1 and 2 phases is not true. Default value is 120 degrees.");
						}
						if ((phase[1] = Double.parseDouble(phase13Field.getText())) > 180
							|| (phase[1] = Double.parseDouble(phase13Field.getText())) < -180
							|| (phase13Field.getText()) == null) {
							phase[1] = 0;
							countError++;
							log.info("Shift between 1 and 3 phases is not true. Default value is 120 degrees.");
							errorLabel.setText("Shift between 1 and 3 phases is not true. Default value is 120 degrees.");
						}
						if ((phaseIU[0] = Double.parseDouble(phaseIU1Field.getText())) > 180
								|| (phaseIU[0] = Double.parseDouble(phaseIU1Field.getText())) < -180
								|| (phaseIU1Field.getText()) == null) {
							phaseIU[0] = 0;
							countError++;
							log.info("Shift between current and voltage phases is not true. Default value is 0 degrees.");
							errorLabel.setText("Shift between current and voltage phases is not true. Default value is 0 degrees.");
						}
						if ((phaseIU[1] = Double.parseDouble(phaseIU2Field.getText())) > 180
								|| (phaseIU[1] = Double.parseDouble(phaseIU2Field.getText())) < -180
								|| (phaseIU2Field.getText()) == null) {
							phaseIU[1] = 0;
							countError++;
							log.info("Shift between current and voltage phases is not true. Default value is 0 degrees.");
							errorLabel.setText("Shift between current and voltage phases is not true. Default value is 0 degrees.");
						}
						if ((phaseIU[2] = Double.parseDouble(phaseIU3Field.getText())) > 180
								|| (phaseIU[2] = Double.parseDouble(phaseIU3Field.getText())) < -180
								|| (phaseIU3Field.getText()) == null) {
							phaseIU[2] = 0;
							countError++;
							log.info("Shift between current and voltage phases is not true. Default value is 0 degrees.");
							errorLabel.setText("Shift between current and voltage phases is not true. Default value is 0 degrees.");
						}
						
						//Time increment
						t_growth = 1.0 / freqDiscrete;
						System.out.printf("Signal length is %.5f s, time increment is %.5f ms\n", endSignal, t_growth * 1000);

						//masLen - number of values in one channel.
						masLen = (int) (endSignal / t_growth);
						time = new double[masLen + 1];			//Plus "time" in first row.
						value = new double[6][masLen + 1];		//Plus column name in first row.
					}

					catch (NumberFormatException ex) {
						countError++;
						log.info("Input data error: " + ex.getMessage());
						errorLabel.setText("Input data error: " + ex.getMessage());
						
					}
				}

				catch (Exception ex) {
					countError++;
					log.info("Application error: " + ex.getMessage());
					errorLabel.setText("Application error: " + ex.getMessage());
				}

				double distortionFactor = 0;
				try 
				{
					double res, resPrev = 0;
					int countChannels = 0;
			    	
			    	if(GUI.channel1.isSelected())
			    	{
			    		channels[0] = 1;
			    		countChannels++;
			    	}
			    	if(GUI.channel2.isSelected())
			    	{
			    		channels[1] = 1;
			    		countChannels++;
			    	}
			    	if(GUI.channel3.isSelected())
			    	{
			    		channels[2] = 1;
			    		countChannels++;
			    	}
			    	if(GUI.channel4.isSelected())
			    	{
			    		channels[3] = 1;
			    		countChannels++;
			    	}
			    	if(GUI.channel5.isSelected())
			    	{
			    		channels[4] = 1;
			    		countChannels++;
			    	}
			    	if(GUI.channel6.isSelected())
			    	{
			    		channels[5] = 1;
			    		countChannels++;
			    	}
			    	
			    	if(countChannels == 0)
			    	{
			    		countChannels = 6;					    		
			    		for(int j = 0; j < 6; j++)
			    			channels[j] = 1;    		
			    	}
			    	
		    		for(int j = 0; j < 6; j++)
		    			channelsCoef[j] = 0;   
			    	
			    	if(GUI.coefChannel1.isSelected())
			    	{
			    		channelsCoef[0] = 1;
			    	}
			    	if(GUI.coefChannel2.isSelected())
			    	{
			    		channelsCoef[1] = 1;
			    	}
			    	if(GUI.coefChannel3.isSelected())
			    	{
			    		channelsCoef[2] = 1;
			    	}
			    	if(GUI.coefChannel4.isSelected())
			    	{
			    		channelsCoef[3] = 1;
			    	}
			    	if(GUI.coefChannel5.isSelected())
			    	{
			    		channelsCoef[4] = 1;
			    	}
			    	if(GUI.coefChannel6.isSelected())
			    	{
			    		channelsCoef[5] = 1;
			    	}
			    	
					for (numChannel = 0; numChannel < 6; numChannel++) {
						if(channels[numChannel] != 1)
							continue;
						for (t = 0, j = 0; t < endSignal; t += t_growth, j++) {
							
							time[j] = t;
							res = Generate(t, numChannel);
							if(t > (endSignal - 0.01))
							{
								if(resPrev < 0 && res >= 0)
								{
									break;
								}
							}
							if(res == -1)
								break;					
							resPrev = res;
							value[numChannel][j] = res;
						}
					}					
				}
				catch(Exception ex)
				{
					countError++;
					log.info("Application error: " + ex.getMessage());	
					errorLabel.setText("Application error: " + ex.getMessage());				
				}
				
				float cntCoef = 1;
				
				for(int r = 0; r < 5; r++)
				{
					for(int s = 0; s < 10; s++)
					{
						if(r == 0 && s == 0)
							continue;
						cntCoef += (float)Math.pow((coeff[r][s] * 0.01), 2);
					}					
				}
				
				
				float [] powerAct = new float[3];
				float [] powerReact = new float[3];
				for(int f = 0; f < 3; f++)
				{
					powerAct[f] = (float) (ampl_U0[f] * ampl_I0[f] * 0.001 * Math.cos((Math.PI / 180) * phaseIU[f]));
					powerReact[f] = (float) (ampl_U0[f] * ampl_I0[f] * 0.001 * Math.sin((Math.PI / 180) * phaseIU[f]));
				}				
							
				
				distortionFactor = Math.sqrt(cntCoef);
				distortionCoefField.setText(String.format("%.6f", (float)Math.abs((distortionFactor - 1) * 100)) + "%");
				
				powerAct1Field.setText(String.format("%.3f", (float)(powerAct[0])) + " W");
				powerAct2Field.setText(String.format("%.3f", (float)(powerAct[1])) + " W");
				powerAct3Field.setText(String.format("%.3f", (float)(powerAct[2])) + " W");
				
				energyAct1Field.setText(String.format("%.3f", (float)(powerAct[0])) + " W*s");
				energyAct2Field.setText(String.format("%.3f", (float)(powerAct[1])) + " W*s");
				energyAct3Field.setText(String.format("%.3f", (float)(powerAct[2])) + " W*s");
				
				powerReact1Field.setText(String.format("%.3f", (float)(powerReact[0])) + " var");
				powerReact2Field.setText(String.format("%.3f", (float)(powerReact[1])) + " var");
				powerReact3Field.setText(String.format("%.3f", (float)(powerReact[2])) + " var");

				energyReact1Field.setText(String.format("%.3f", (float)(powerReact[0])) + " var*s");
				energyReact2Field.setText(String.format("%.3f", (float)(powerReact[1])) + " var*s");
				energyReact3Field.setText(String.format("%.3f", (float)(powerReact[2])) + " var*s");
				
				try
				{
					linearVoltage12Field.setText(String.format("%.3f", (float) (Math.sqrt((Math.pow(Double.parseDouble(amplFieldU1.getText()), 2) + Math.pow(Double.parseDouble(amplFieldU2.getText()), 2))  - 
							2 * Double.parseDouble(amplFieldU1.getText()) * Double.parseDouble(amplFieldU2.getText()) * Math.cos(Math.PI / 180 * phase[0])))) + " V");

					linearVoltage13Field.setText(String.format("%.3f", (float) (Math.sqrt((Math.pow(Double.parseDouble(amplFieldU1.getText()), 2) + Math.pow(Double.parseDouble(amplFieldU3.getText()), 2))  - 
							2 * Double.parseDouble(amplFieldU1.getText()) * Double.parseDouble(amplFieldU3.getText()) * Math.cos(Math.PI / 180 * phase[1])))) + " V");
					
				}
				catch(Exception ex)
				{
					countError++;
					System.out.println(ex);
					errorLabel.setText("Application error: " + ex.getMessage());	
				}

				long finish = System.currentTimeMillis();
				System.out.printf("Full time is %d m %d s %d ms\r\n", Math.abs(((finish % 100000000)-(start % 100000000)) / 60000), Math.abs((((finish % 100000)-(start % 100000)) % 60000) / 1000), Math.abs(((finish)-(start)) % 1000));
				if(countError == 0)
				{
					errorLabel.setText("");
				}
				else countError = 0;
			}
		});

		butSelDir = new JButton("Save");
		butSelDir.setBounds(frameSizeX - 240, frameSizeY - 70, 100, 20);
		butSelDir.setActionCommand("Button pressed");
		panel.add(butSelDir);

		butSelDir.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				JFileChooser fc = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Generator output files", "csv", "wav");
				
				fc.setFileFilter(filter);
				if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
					try (FileWriter fw = new FileWriter(fc.getSelectedFile())) 
					{
						String fileName = fc.getSelectedFile().getName();
						int index = fileName.length();
						String type = fileName.substring((index-3), index);

						if(type.matches("wav"))
						{
							try {
								new WavConverter(fc.getSelectedFile().getAbsolutePath(), value, masLen, freqDiscrete);
								SaveData(refDirect + fileName.substring(0, (index - 3)) + "txt");
								errorLabel.setText("");
							} catch (Exception ex) {
								countError++;
								errorLabel.setText("Application error: " + ex.getMessage());
								ex.printStackTrace();
							}			
						}
						else if(type.matches("csv"))
						{
				    		
				    		for(int j = 0; j < 6; j++)
				    			channels[j] = 0;   
							int countChannels = 0;
					    	
					    	if(GUI.channel1.isSelected())
					    	{
					    		channels[0] = 1;
					    		countChannels++;
					    	}
					    	if(GUI.channel2.isSelected())
					    	{
					    		channels[1] = 1;
					    		countChannels++;
					    	}
					    	if(GUI.channel3.isSelected())
					    	{
					    		channels[2] = 1;
					    		countChannels++;
					    	}
					    	if(GUI.channel4.isSelected())
					    	{
					    		channels[3] = 1;
					    		countChannels++;
					    	}
					    	if(GUI.channel5.isSelected())
					    	{
					    		channels[4] = 1;
					    		countChannels++;
					    	}
					    	if(GUI.channel6.isSelected())
					    	{
					    		channels[5] = 1;
					    		countChannels++;
					    	}
					    	
					    	if(countChannels == 0)
					    	{
					    		countChannels = 6;					    		
					    		for(int j = 0; j < 6; j++)
					    			channels[j] = 1;    		
					    	}
					    	
					    	
							try
							{
								fw.write("time, ");
								for(numChannel = 0; numChannel < 3; numChannel++)
								{
									if(channels[numChannel] == 1)
									{
										fw.write("Voltage" + (numChannel + 1) + "Phase, ");
									}
								}
								
								for(numChannel = 3; numChannel < 6; numChannel++)
								{
									if(channels[numChannel] == 1)
									{
										fw.write("Current" + ((numChannel + 1) % 4) + "Phase, ");
									}
								}
								int len = value[0].length;
								for (int s = 0; s < len; s++) 
								{
									fw.write("\r\n" + time[s]);
									for(numChannel = 0; numChannel < 6; numChannel++)
									{	
						            	if(channels[numChannel] == 0)
						            		continue;
										fw.write(", " + (int)(value[numChannel][s])/16000.00);								
									}	
									errorLabel.setText("");
								}
								
								SaveData(refDirect + fileName.substring(0, (index - 3)) + "txt");
							} 
							catch (Exception ex) {
								countError++;
								log.info(ex.getMessage());
								errorLabel.setText("Application error: " + ex.getMessage());
							}
						}
						else 
						{
							countError++;
							System.out.print(type);
				            System.out.println(" - unsupported file format.");
							errorLabel.setText(type + " - unsupported file format.");
						}
						
					} catch (IOException ex) {
						countError++;
						log.info(ex.getMessage());
						errorLabel.setText("Input data error: " + ex.getMessage());
					}
				}
			}
		});


		box.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				switch (box.getSelectedItem().toString()) {
				case "Voltage harmonic":
					coef_begin = "0.1";
					break;
				case "Current harmonic": 
					coef_begin = "0.3";
					break;						
				}

				coef1.setText(coef_begin + "% < K < 30%");
				coef2.setText(coef_begin + "% < K < 20%");
				coef3.setText(coef_begin + "% < K < 10%");
				coef4.setText(coef_begin + "% < K < 5%");
				coef5.setText(coef_begin + "% < K < 5%");
			}
		});
		
		

		table.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
			} // Click on the table.

			@Override
			public void mouseEntered(MouseEvent e) {
			} // Hovering on the table.

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				String newVal = JOptionPane.showInputDialog(e.getComponent(),
						"Enter value from " + coef_begin + "% to 30%:", "Distortion coefficient", 3);
				try {
					double v = Double.parseDouble(newVal);
					if(v > 30)
					{
						countError++;
						log.info("Error data input.");
						errorLabel.setText("Error data input.");
					}
					else if(v != 0 && v < Double.valueOf(coef_begin))
					{	
						countError++;
						log.info("Error data input.");
						errorLabel.setText("Error data input.");
					}
					else
					{
						data[table.getSelectedRow()][table.getSelectedColumn()] = Double.toString(v);
						table.repaint();						
					}
				} 
				catch (Exception ex) 
				{
					countError++;
					log.info("Error data input: " + ex.getMessage());
					errorLabel.setText("Input data error: " + ex.getMessage());
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}
		});

		frame.setVisible(true);
	}

	public double Generate(double time, int nChannel) {
		
		double c_cur, ampl_summ = 0;
		double FI = 0, FI0 = 0;
		int garmonicNum = 0;			
		
		if(nChannel > 2 && nChannel < 6)	
			garmonicNum = 51;
		else if(nChannel >= 0 && nChannel < 3)
			garmonicNum = 41;
		
		
		if(nChannel % 3 == 0)
		{
			FI = 0;
		}
		else
		{
			FI = Math.PI * phase[(nChannel % 3) - 1] / 180.00;
		}
		
		double ampl0 = 0;
		if(nChannel >= 0 && nChannel < 3)
		{
			ampl0 = ((ampl_U0[nChannel%3] / 265) * 16000);
		}
		else if(nChannel >= 3 && nChannel < 6)
		{
			ampl0 = ((ampl_I0[nChannel%3] / 60) * 16000);
		}
		
		
		FI0 = 0;

		if(phaseIU[nChannel % 3] < 0)
		{
			if(nChannel > 2)
				FI0 = Math.PI * Math.abs(phaseIU[nChannel % 3]) / 180.00;
		}
		else if(phaseIU[nChannel % 3] > 0)
		{
			if(nChannel < 3)
				FI0 = Math.PI * Math.abs(phaseIU[nChannel % 3]) / 180.00;
		}		
		
		for (int k = 1; k < garmonicNum; k++) 
		{
			if(time == 0 && k > 1)
				fi_garmonic = -0.1*Math.PI + (2 * Math.PI) * random.nextInt(100) * 0.001;
			
			c_cur = coeff[(k - 1) / 10][(k - 1) % 10];	//Current coefficient.

			if(nChannel > 2 && nChannel < 6 && c_cur < 0.3 && c_cur != 0)	
				c_cur = 0.3;
			else if(nChannel >= 0 && nChannel < 3 && c_cur < 0.1 && c_cur != 0)
				c_cur = 0.1;
			
			if(k != 1 && c_cur == 0)
			{
				continue;
			}
					
			if (k == 1 && c_cur != 100) 
			{
				countError++;
				log.info("First harmonic coefficient must be equal to 100.");
				errorLabel.setText("First harmonic coefficient must be equal to 100.");
				return -1;
			} 
			else if (k != 1 && c_cur > 30) 
			{
				countError++;
				log.info("Top border of " + k + " harmonic coefficient is broken.");
				errorLabel.setText("Top border of " + k + " harmonic coefficient is broken.");
				return -1;
			}
			
			ampl = ampl0 * c_cur * 0.01 * Math.sin(2 * Math.PI * (baseFreq * k * time) + fi_garmonic + FI + FI0);
			ampl_summ += ampl;
			
			if(channelsCoef[nChannel] != 1)
				break;
		}
		return ampl_summ;
	}
	
	public void SaveData(String FileSaveData)
	{

		String amplU1;
		String amplU2;
		String amplU3;
		String amplI1;
		String amplI2;
		String amplI3;
		try (FileWriter writer = new FileWriter(FileSaveData)) {
			
			String freq = freqDiscreteField.getText();
			String quant = signalLengthField.getText();
			String baseF = baseFreqField.getText();
			if(GUI.channel1.isSelected()) amplU1 = amplFieldU1.getText(); else amplU1 = "-";
			if(GUI.channel2.isSelected()) amplU2 = amplFieldU2.getText(); else amplU2 = "-";
			if(GUI.channel3.isSelected()) amplU3 = amplFieldU3.getText(); else amplU3 = "-";
			if(GUI.channel4.isSelected()) amplI1 = amplFieldI1.getText(); else amplI1 = "-";
			if(GUI.channel5.isSelected()) amplI2 = amplFieldI2.getText(); else amplI2 = "-";
			if(GUI.channel6.isSelected()) amplI3 = amplFieldI3.getText(); else amplI3 = "-";
			String phase12 = phase12Field.getText();
			String phase13 = phase13Field.getText();
			String phaseIU1 = phaseIU1Field.getText();
			String phaseIU2 = phaseIU2Field.getText();
			String phaseIU3 = phaseIU3Field.getText();

			if (freq != null && quant != null && baseF != null && amplU1 != null && amplU2 != null && amplU3 != null) 
			{
				writer.write("Discrete frequency, Hz : " + freq);
				writer.write("\r\n");
				writer.write("Signal duration, s : " + quant);
				writer.write("\r\n");
				writer.write("Base frequency, Hz : " + baseF);
				writer.write("\r\n");
				writer.write("Amplitude U1, V : " + amplU1);
				writer.write("\r\n");
				writer.write("Amplitude U2, V : " + amplU2);
				writer.write("\r\n");
				writer.write("Amplitude U3, V : " + amplU3);
				writer.write("\r\n");
				writer.write("Amplitude I1, mA : " + amplI1);
				writer.write("\r\n");
				writer.write("Amplitude I2, mA : " + amplI2);
				writer.write("\r\n");
				writer.write("Amplitude I3, mA : " + amplI3);
				writer.write("\r\n");
				writer.write("Shift between phases 1 and 2 : " + phase12);
				writer.write("\r\n");
				writer.write("Shift between phases 1 and 3 : " + phase13);
				writer.write("\r\n");
				writer.write("Phase shift between U1 and I1 : " + phaseIU1);
				writer.write("\r\n");
				writer.write("Phase shift between U2 and I2 : " + phaseIU2);
				writer.write("\r\n");
				writer.write("Phase shift between U3 and I3 : " + phaseIU3);
			}

			int index = 0;
			for (int l = 0; l < 5; l++) {
				for (int m = 0; m < 10; m++) {
					index = l * 10 + m + 1;
					writer.write("\r\n");
					try {
						c = table.getValueAt(l, m);
						if (c.toString() == "") {
							coeff[l][m] = 0;
						} 
						else if(index == 1)	{
							coeff[l][m] = 100;
						}
						else {
							coeff[l][m] = Double.parseDouble(c.toString());
						}

						writer.write("Coefficient " + index + ", % : " + Double.toString(coeff[l][m]));
					} catch (NumberFormatException num) {
						log.info("Input data error: " + num.getMessage());
						errorLabel.setText("Input data error: " + num.getMessage());

					}
				}
			}
		} 
		catch (IOException ex) {

			System.out.println(ex.getMessage());
			errorLabel.setText(ex.getMessage());
		}
	}

	public static void main(String[] Args) throws Exception 
	{
		GUI gui;
		Console console;
		String logInfo = "";		
		
		if(Args.length == 0)
		{
			gui = new GUI(); 						//Desktop application.
			logInfo = gui.getClass().getName();
			log.info(logInfo);
		}
		else if(Args.length == 2)
		{
			console = new Console(Args); 			//Console application.			
			logInfo = console.getClass().getName();
			System.out.println(logInfo);
		}
		else 
		{
			log.info("Input parameters number is not true. For start console application number parameters must be a zero. "
					+ "For start desktop application number parameters must be a two. First is full path to sourse *.ini file. "
					+ "Second is full path to output file.");;
		}
			
	}
}