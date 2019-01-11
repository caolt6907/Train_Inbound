package com.strategy.tools;

import com.sinovoice.jTTS.JTTS_CONFIG;
import com.sinovoice.jTTS.jTTS_ML;

public class TtsUtils {
	static jTTS_ML jTTS;
	static{
		int nRet = 0;
		 jTTS = new jTTS_ML();
		
		nRet = jTTS.jTTS_Init("10.168.1.2:3000", "");
		System.out.printf("jTTS_Init nRet is " + nRet + "\n");
	
		JTTS_CONFIG config = new JTTS_CONFIG();
		
		config.wVersion = 4;
		config.nCodePage = (char) 65001;
		config.szVoiceID = "47FF1422-796F-427F-8408-EC5FD3367729";
		config.nDomain = 0;
		config.nSpeed = 5;
		config.nPitch = 5;
		config.nVolume = 8;
		config.nDigitMode = 0;
		config.nEngMode = 0;
		config.nTagMode = 0;
		config.nTryTimes = 10;
		config.bLoadBalance = false;
		config.nVoiceStyle = 0;
		config.nBackAudioVolume = 0;
		config.wBackAudioFlag = 0;
		config.nInsertInfoSize = 100;	
		config.nVoiceBufSize = 256;
		
		nRet = jTTS.jTTS_Set(config);
		System.out.println("jTTS_Set nRet is " + nRet);	
	
		JTTS_CONFIG config_get = new JTTS_CONFIG();
		nRet = jTTS.jTTS_Get(config_get);
		System.out.println("jTTS_Get nRet is " + nRet + " voiceID is " + config_get.nVolume);
	}
	public static jTTS_ML getjTTS(){
		return jTTS;
	}
}
