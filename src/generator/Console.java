package generator;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import generator.WavConverter;

public class Console
{
	private double freqDiscrete = 0, baseFreq;
	private double [] ampl_U0;
	private double [] ampl_I0;
	private double [] phase;
	private double [] phaseIU;
	private double ampl, t_count;
	private int g;
	public double endSignal = 0;				
	public double [][] coeff;						
	private Random random;
	public String [][] dict;
	private int numChannel;
	
	
	public Console(String[] inputParams) throws NumberFormatException, IOException
	{		
		coeff = new double [5][10];
		random = new Random();	
		dict = new String[200][200];
		ampl_U0 = new double [3];
		ampl_I0 = new double [3];
		phaseIU = new double [3];
		phase = new double [2];
				
		try(FileReader reader = new FileReader(inputParams[0]))
        {
			
            int c, count = 0, q;
            for(int p = 0; p < dict.length; p++)
            {
            	for(q = 0; q < 2; q++)
            	{
                	dict[p][q] = "";
            	}
            }            
            g = 0;
            q = 0;
            while((c = reader.read()) != -1)
            { 
            	if(c == 0x00)
            		continue;            	            	
            	if(c == 0x0A && count > 1)			// 0x0A - '\n'		
            	{
            		g++;
					q = 0;
					continue;
            	}              	
            	if(c == 0x3A)						// 0x3A - ':'	
            	{
            		q++;
					continue;
            	}   
            	
            	dict[g][q] += (char)c;
            	if(dict[g][0] == "")
            		break;
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
			}
			catch(NumberFormatException num)
			{
				System.out.println("Some data in the *.ini file is missing." + num);
				
			}
        }
        catch(IOException ex){
             
            System.out.println(ex.getMessage());
        }   
		
		if(freqDiscrete != 0)
		{
			t_count = (double) (1.00 / freqDiscrete);
		}
		else 
		{
			t_count = (double) (1.00 / 5000);
		}
		
		int len = (int) (endSignal / t_count) + 1;
        System.out.println(len);		
		
		double [][] val = new double [6][len];
		int cnt = 0;

		try 
		{
			for(double t = 0; t < endSignal; t += t_count, cnt++)
			{
				for (numChannel = 0; numChannel < 6; numChannel++)
				{
					double res = Generate(t, numChannel);
					val[numChannel][cnt] = res;							
				}				
			}						
		}
		catch(Exception ex)
		{
			System.out.println("Error application: " + ex.getMessage());					
		}
		
		String args = inputParams[1];
		int index = inputParams[1].length();
		String type = inputParams[1].substring((index-3), index);
     
        
		if(type.matches("wav"))
		{
			try 
			{
				for (int s = 0; s < len; s++) 
				{
					for(numChannel = 0; numChannel < 6; numChannel++)
					{	
						val[numChannel][s] *= 250;							
					}	
				}
				new WavConverter(args, val, len, freqDiscrete);
			} catch (Exception e1) {
				e1.printStackTrace();
			}			
		}
		else if(type.matches("csv"))
		{
			try(FileWriter writer = new FileWriter(args))
			{	
				writer.write("time, Voltage1stPhase, Voltage2ndPhase, Voltage3rdPhase, Current1stPhase, Current2ndPhase, Current3rdPhase");
				cnt = 0;
				for(double t = 0; t < endSignal; t += t_count)
				{				
					writer.write("\r\n" + Double.toString(t));
					for (numChannel = 0; numChannel < 6; numChannel++)
					{
						writer.write(", " + Double.toString((int)val[numChannel][cnt]));
					}
					cnt++;
				}
	        }
	        catch(IOException ex)
	        {             
	            System.out.println("Record is disable.");
	        }			
		}
		else 
		{
			System.out.println(type);
            System.out.println("Unsupported file format.");
		}
	}
		
	
	public double Generate(double time, int nChannel)
	{			
		double c_cur, ampl_summ = 0, fi_garmonic = 0;
		double FI = 0, FI0 = 0;
		int n = 14;
		int garmonicNum = 0;
		
		if(nChannel > 2 && nChannel < 6)		//current	
			garmonicNum = 51;
		else if(nChannel >= 0 && nChannel < 3)	//voltage
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
			ampl0 = (ampl_U0[nChannel%3]  / 220) * 16000;
		else if(nChannel >= 3 && nChannel < 6)
			ampl0 = (ampl_I0[nChannel%3] / 50.00) * 16000;
		
	
		FI0 = 0;
		if(phaseIU[nChannel % 3] < 0 &&  nChannel > 3)
		{
			FI0 -= Math.PI * phaseIU[nChannel % 3] / 180.00;
		}
		else if(phaseIU[nChannel % 3] > 0 &&  nChannel < 3)
		{
			FI0 += Math.PI * phaseIU[nChannel % 3] / 180.00;
		}		
		
		for(int k = 1; k < garmonicNum; k++)
		{
			c_cur = Double.parseDouble(dict[n][1]);
			
			if(nChannel > 2 && nChannel < 6 && c_cur < 0.3 && c_cur != 0)	
				c_cur = 0.3;
			else if(nChannel >= 0 && nChannel < 3 && c_cur < 0.1 && c_cur != 0)
				c_cur = 0.1;
			
			if (k != 1 && c_cur == 0) 
			{
				continue;
			} 		
			
			if (k == 1 && c_cur != 100) 
			{
				System.out.println("First harmonic coefficient must be equal to 100.");
				c_cur = 100;
			} 
			else if(c_cur > 30)
			{
				System.out.println("Top border of " + k + " harmonic coefficient is broken.");
				return -1;
			}
			else if(k > 1)
			{
				fi_garmonic = - 0.05*Math.PI + (2 * Math.PI) * random.nextInt(100) * 0.001;
			}
			c_cur *= 0.01;
			
			ampl = ampl0 * c_cur * Math.sin(2 * Math.PI * (baseFreq * k * time) + fi_garmonic + FI + FI0);	
			ampl_summ += ampl;
			n++;
		}
		return ampl_summ;
	}	
};
