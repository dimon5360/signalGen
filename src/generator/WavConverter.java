package generator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import generator.GUI;

public class WavConverter  {

    private long framesCount = 0;
    private int Rate, sampleSizeInBits, sampleSizeInBytes, numChannels;
    private byte[] data = null;  				
    private AudioFormat af = null;
    private ByteArrayInputStream bais = null;
    private AudioInputStream ais = null;
    private byte channels[] = {0,0,0,0,0,0};
    
    
    /********************************************************************************
     * @PARAM destFile - file saving directory
     * @PARAM srcData - data source for audio record
     * @PARAM length - data number in full time (full time * sample rate)
     * @PARAM rate - discrete frequency
     ********************************************************************************/
    public WavConverter(String destFile, double [][] srcData, int length, double rate)
    {
    	Rate = (int) rate;
    	sampleSizeInBits = 24;					//Sample size in bits.
    	sampleSizeInBytes = 3;					//Sample size in bytes.
    	numChannels = 6;					
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
    		countChannels = numChannels;
    		for(int j = 0; j < numChannels; j++)
    			channels[j] = 1;    		
    	}
    	
    	//Number bytes per sample * number frames * channels number.
    	data = new byte[sampleSizeInBytes * length * countChannels];   //Byte array contains audio-data.  	
    	
    	//Audio format encoding, discrete frequency, number bits per sample, channels number, frame size, number frames per second, default true
    	af = new AudioFormat(AudioFormat.Encoding.PCM_FLOAT, Rate, sampleSizeInBits, countChannels, sampleSizeInBits * countChannels,  1, true);

    	int cnt = 0, val = 0;

    	//Conversion float to byte.
        for(int m = 0; m < length; m+=1)
        {
            for(int n = 0; n < numChannels; n++)
            {
            	if(channels[n] == 0)
            		continue;
            	if(n >= 0 && n < 3)
            	{
            		srcData[n][m] *= (250);
            	}
            	else if(n > 2 && n < 6)
            	{
            		srcData[n][m] *= (250 / 1.9);
            	}
            	val = (((int) (srcData[n][m])) & 0xFF);
            	data[cnt] = (byte)(val);	
            	cnt++;
            	val = (((int) (srcData[n][m]) >> 8) & 0xFF);
            	data[cnt] = (byte)(val);	
            	cnt++;
            	val = (((int) (srcData[n][m]) >> 16) & 0xFF);
            	data[cnt] = (byte)(val);	
            	cnt++;
            }
        }

        //Input stream in byte array.
        bais = new ByteArrayInputStream(data);
        framesCount = cnt / (countChannels * sampleSizeInBits);
        //Write byte array stream in audio stream.
        ais = new AudioInputStream(bais, af, framesCount);
        try {
        	//Create audio file in WAVE format.
        	AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(destFile));
        	} 
        catch (IOException e) {
			e.printStackTrace();
			} 
    }
}
