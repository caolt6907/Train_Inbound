package com.sinovoice.jTTS;

public class jTTS_ML {
	public int FORMAT_WAV = 0;
	public int FORMAT_VOX_6K = 1;
	public int FORMAT_VOX_8K = 2;
	public int FORMAT_ALAW_8K = 3;
	public int FORMAT_ULAW_8K = 4;
	public int FORMAT_WAV_8K8B = 5;
	public int FORMAT_WAV_8K16B = 6;
	public int FORMAT_WAV_16K8B = 7;
	public int FORMAT_WAV_16K16B = 8;
	public int FORMAT_WAV_11K8B = 9;
	public int FORMAT_WAV_11K16B = 10;

	public int PLAYTOFILE_DEFAULT = 0;
	public int PLAYTOFILE_NOHEAD = 1;
	public int PLAYTOFILE_ADDHEAD = 2;

	public native  int jTTS_Init(String strLibPath, String strSerialNo);

	//public native int jTTS_PlayToFile(String strText, String strVoiceFile, int nFileHeadFlag, int nAudioFormat);
	public native int jTTS_PlayToFile(String strText, String strVoiceFile, int nFormat, JTTS_CONFIG config, int nFlag, String callback, long userdata);

	public native int jTTS_Set(JTTS_CONFIG config);	
	public native int jTTS_Get(JTTS_CONFIG config);	

	public native int jTTS_End();

	static 
	{
		System.loadLibrary("jTTS_Java");//libjTTS_Java
	}
}
