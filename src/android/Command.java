package cordova.plugin.usbprinter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.format.Time;

public class Command {
	public static String enablebuzzer = "1F 1B 1F 50 40";
	public static String disablebuzzer = "1F 1B 1F 50 42";

	public static byte[] openCash = new byte[] { 0x1B, 0x70, 0x00, (byte) 0x80,
			(byte) 0x80 };
	public static byte[] cutPaper =  new byte[] {0x20,0x0A, 0x1D, 0x56, 0x42, 0x00 }; // 切纸； GS V
																					  // 66D 0D
	public static byte[] printStatus = new byte[] { 0x10, 0x04, 0x01 };
	public static byte[] printTest = new byte[] { 0x1F, 0x1B, 0x1F, 0x53 };

	final static byte[] CodepageData = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 13,
			14, 15, 16, 17, 18, 19, 21, 33, 34, 36, 37, 41, 46, 47, 48, 49, 50, 51,
			(byte) 254 };

	// 扩展字符集
	public static byte[] getCodepage(int cpIndex) {

		byte[] printText = new byte[5];

		printText[0] = 0x1F;
		printText[1] = 0x1B;
		printText[2] = 0x1F;
		printText[3] = (byte) 0xFF;
		printText[4] = CodepageData[cpIndex];

		return printText;
	}

	// 国际字符集
	public static byte[] getCharacterSet(int csIndex) {

		byte[] printText = new byte[5];

		printText[0] = 0x1F;
		printText[1] = 0x1B;
		printText[2] = 0x1F;
		printText[3] = (byte) 0xFD;
		printText[4] = (byte) csIndex;
		return printText;
	}
	
	// 中文字符集//////////////
	public static byte[] getResidentCharacterSet(int residentCsIndex) {

		byte[] printText = new byte[5];

		printText[0] = 0x1F;
		printText[1] = 0x1B;
		printText[2] = 0x1F;
		printText[3] = (byte) 0xFC;
		printText[4] = (byte) residentCsIndex;
		return printText;
	}

	// 中文模式
	public static byte[] getChineseMode(int type) {

		byte[] printText = new byte[5];

		printText[0] = 0x1F;
		printText[1] = 0x1B;
		printText[2] = 0x1F;
		printText[3] = (byte) 0xFE;

		if (type != 0) {
			printText[4] = 0x00; // 开启
		} else {
			printText[4] = 0x01; // 关闭
		}

		return printText;
	}

	// 蜂鸣器
	public static byte[] getBuzzer(int type) {

		byte[] printText = new byte[5];

		printText[0] = 0x1F;
		printText[1] = 0x1B;
		printText[2] = 0x1F;
		printText[3] = 0x50;

		if (type != 0) {
			// 打开蜂鸣器
			printText[4] = 0x40;
		} else {
			
			// 关闭蜂鸣器
			printText[4] = 0x42;
		}

		return printText;
	}
	
	 public static byte[] transToPrintText(String s){
			
			byte[] printText = new byte[4096];
			int iNum = 0;
			byte[] cmdData;
			String[] tmp=s.split(" ");
			
			for(int i=0;i<tmp.length ;i++){
				if(tmp[i].length()>0){
					cmdData=transCommandBytes(tmp[i]);
					System.arraycopy(cmdData, 0,  printText,  iNum,  cmdData.length);
					iNum += cmdData.length;
				}
			}
			
			cmdData = new byte[iNum];
			//cmdData = new byte[20];
			System.arraycopy(printText,0,cmdData,0,iNum);
			
			return cmdData;
		}
	
	
	
	public static String[] cmdBytes=new String[]
    		{
    			"NUL","SOH","STX","ETX","EOT","ENQ","ACK","BEL",
    		    "BS","HT","LF","VT","CLR","CR","SO","SI",
    		    "DEL","DC1","DC2","DC3","DC4","NAK","SYN","ETB",
    		    "CAN","EM","SUB","ESC","FS","GS","RS","US",
    		    "SP"
    		};
	
	
	//transCommandBytes
	public static byte[] transCommandBytes(String s){
		for(byte i=0;i<cmdBytes.length;i++){
			if(cmdBytes[i].equals(s)){
				return new byte[]{i};
			}
		}
		
		Pattern p1 = Pattern.compile("(\\d{1,3})([Dd]$)");  
		Pattern p2 = Pattern.compile("([0-9a-fA-F]{1,2})([Hh]$)");  
		Pattern p3=Pattern.compile("^0x([0-9a-fA-F]{1,2})");
		Matcher m;
		
		m=p1.matcher(s);
		if(m.matches()){
			int i=Integer.parseInt(m.group(1));
			if(i>255){
				return getGbk(s);
			}else{
				return new byte[]{(byte)i};				
			}
		}
		
		m=p2.matcher(s);
		if(m.matches()){
			return new byte[]{hexStringToByte(m.group(1))};
		}
		
		m=p3.matcher(s);
		if(m.matches()){
			return new byte[]{hexStringToByte(m.group(1))};
		}
		
		return getGbk(s);
	}
	
	  public static byte hexStringToByte(String hex) {  
		    char[] achar = hex.toUpperCase().toCharArray(); 
		    if(1==hex.length()){
		    	return toByte(achar[0]);
		    }else{
		    	return (byte) (toByte(achar[0]) << 4 | toByte(achar[1])); 	
		    }
	   }
	  
	  private static byte toByte(char c) {  
		    byte b = (byte) "0123456789ABCDEF".indexOf(c);  
		    return b;  
	   }  
	  
	  
	  //
	  public static byte[] getSetTimeCmd(){
			
			Time t=new Time(); // or Time t=new Time("GMT+8"); 加上Time Zone资料。
			t.setToNow(); // 取得系统时间。
			int year = t.year;
			int month = t.month + 1;
			int date = t.monthDay;
			int hour = t.hour; // 0-23
			int minute = t.minute;
			int second = t.second;
			
			byte[] arrayOfByte = new byte[9]; //
					
			arrayOfByte[0] = 0x02;
			arrayOfByte[1] = 0x54;
			
			arrayOfByte[2] = 20;
			if(year >= 2000)
			{
				arrayOfByte[3] = (byte)(year - 2000);
			}
			else
			{
				arrayOfByte[3] = 0;
			}
			
			arrayOfByte[4] = (byte)month;		
			arrayOfByte[5] = (byte)date;		
			arrayOfByte[6] = (byte)hour;
			arrayOfByte[7] = (byte)minute;
			arrayOfByte[8] = (byte)second;
									
			return arrayOfByte;
		}	
	  
	  //
	  public static byte[] getSendQRCmd(String s){
		
	
		int textlenth = s.length();
		byte[] arrayOfstring = null;
		if(textlenth > 0){
			try 
			{
				arrayOfstring = s.getBytes("GBK");
				textlenth = arrayOfstring.length;
				byte[] arrayOfByte = new byte[6 + textlenth]; //
				
				arrayOfByte[0] = 0x1B;
				arrayOfByte[1] = 0x51;	
				arrayOfByte[2] = 0x52;
				
				arrayOfByte[3] = 0x03;//纠错级别
				
				arrayOfByte[4] = (byte)(textlenth/256);		
				arrayOfByte[5] = (byte)(textlenth);
				
				System.arraycopy(arrayOfstring, 0,  arrayOfByte,  6,  textlenth);
				
				return arrayOfByte;
			}
				catch (Exception   ex) {
					ex.printStackTrace();
				}
			
		}
		return arrayOfstring;
	}	

	public static byte[] getPrintDemo() {

		int iNum = 0;

		byte[] printText = new byte[1024];
		String strTmp = "";

		byte[] oldText = setAlignCenter('1');
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);
		iNum += oldText.length;

		oldText = setWH('1');
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);
		iNum += oldText.length;

		strTmp = "NO.:0032";
		oldText = strTmp.getBytes();
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);
		iNum += oldText.length;

		oldText = setCusorPosition(160);
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);
		iNum += oldText.length;

		strTmp = new SimpleDateFormat("yyyy-MM-dd HH:mm",
				Locale.SIMPLIFIED_CHINESE).format(new Date());

		strTmp += "\n\n";
		oldText = strTmp.getBytes();
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);
		iNum += oldText.length;

		oldText = setAlignCenter('2');
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);
		iNum += oldText.length;

		oldText = setWH('4');
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);
		iNum += oldText.length;

		oldText = setBold(true);
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);
		iNum += oldText.length;

		strTmp = "Testing\n\n";
		oldText = strTmp.getBytes();
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);
		iNum += oldText.length;

		oldText = setWH('1');
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);
		iNum += oldText.length;

		oldText = setAlignCenter('1');
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);
		iNum += oldText.length;

		oldText = setBold(false);
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);
		iNum += oldText.length;

		strTmp = "      Name     Price Qty  Amount\n";
		oldText = strTmp.getBytes();
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);
		iNum += oldText.length;

		strTmp = "- - - - - - - - - - - - - - - -\n";
		oldText = strTmp.getBytes();
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);
		iNum += oldText.length;

		// 菜品名最多8个汉字（16个字符）；单价最多6个字符；数量最多4个字符；金额最多6个字符；中间分隔各2个空格

		strTmp = "iPhone 8";
		oldText = strTmp.getBytes();
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);
		iNum += oldText.length;

		oldText = setCusorPosition(180);
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);
		iNum += oldText.length;

		strTmp = " 12.00   1 12.00\n";
		oldText = strTmp.getBytes();
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);
		iNum += oldText.length;

		strTmp = "iPhone 8 plus";
		oldText = strTmp.getBytes();
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);
		iNum += oldText.length;

		strTmp = " 24.00   2 48.00\n";
		oldText = setCusorPosition(180);
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);
		iNum += oldText.length;

		oldText = strTmp.getBytes();
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);
		iNum += oldText.length;

		strTmp = "- - - - - - - - - - - - - - - -\n";
		oldText = strTmp.getBytes();
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);
		iNum += oldText.length;

		strTmp = "Total:";
		oldText = strTmp.getBytes();
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);
		iNum += oldText.length;

		strTmp = "2     60.00\n";
		oldText = setCusorPosition(384 - 12 * strTmp.length());
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);
		iNum += oldText.length;

		oldText = strTmp.getBytes();
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);
		iNum += oldText.length;

		oldText = setAlignCenter('2');
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);
		iNum += oldText.length;

		strTmp = new SimpleDateFormat("yyyyMMdd", Locale.SIMPLIFIED_CHINESE)
				.format(new Date()) + "0032";
		oldText = PrintBarcode(strTmp);
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);
		iNum += oldText.length;

		oldText = setAlignCenter('1');
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);
		iNum += oldText.length;

		strTmp = "\n\n\n\n";
		oldText = strTmp.getBytes();
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);

		return printText;
	}

	public static byte[] setAlignCenter(char paramChar) // Align： ESC a
	{
		byte[] arrayOfByte = new byte[3];
		arrayOfByte[0] = 0x1B;
		arrayOfByte[1] = 0x61;

		switch (paramChar) // 1-左对齐；2-居中对齐；3-右对齐
		{
		case '2':
			arrayOfByte[2] = 0x01;
			break;
		case '3':
			arrayOfByte[2] = 0x02;
			break;
		default:
			arrayOfByte[2] = 0x00;
			break;
		}
		return arrayOfByte;
	}

	public static byte[] setWH(char paramChar) // GS ! 设置打印字符大小
	{
		byte[] arrayOfByte = new byte[3]; // GS ! 11H 倍宽倍高
		arrayOfByte[0] = 0x1D;
		arrayOfByte[1] = 0x21;

		switch (paramChar) // 1-无；2-倍宽；3-倍高； 4-倍宽倍高
		{
		case '2':
			arrayOfByte[2] = 0x10;
			break;
		case '3':
			arrayOfByte[2] = 0x01;
			break;
		case '4':
			arrayOfByte[2] = 0x11;
			break;
		default:
			arrayOfByte[2] = 0x00;
			break;
		}

		return arrayOfByte;
	}

	public static byte[] getGbk(String paramString) {
		byte[] arrayOfByte = null;
		try {
			arrayOfByte = paramString.getBytes("GBK"); // 必须放在try内才可以
		} catch (Exception ex) {
			;
		}
		return arrayOfByte;

	}

	public static byte[] setCusorPosition(int position) {
		byte[] returnText = new byte[4]; // 当前行，设置绝对打印位置 ESC $ bL bH
		returnText[0] = 0x1B;
		returnText[1] = 0x24;
		returnText[2] = (byte) (position % 256);
		returnText[3] = (byte) (position / 256);
		return returnText;
	}

	public static byte[] setBold(boolean paramBoolean) // 加粗 ESC E
	{
		byte[] arrayOfByte = new byte[3];
		arrayOfByte[0] = 0x1B;
		arrayOfByte[1] = 0x45;
		if (paramBoolean) // 表示加粗
		{
			arrayOfByte[2] = 0x01;
		} else {
			arrayOfByte[2] = 0x00;
		}
		return arrayOfByte;
	}

	public static byte[] PrintBarcode(String paramString) // 条码 GS k
	{
		byte[] arrayOfByte = new byte[13 + paramString.length()];
		// 设置条码高度
		arrayOfByte[0] = 0x1D;
		arrayOfByte[1] = 'h';
		arrayOfByte[2] = 0x60; // 1到255

		// 设置条码宽度
		arrayOfByte[3] = 0x1D;
		arrayOfByte[4] = 'w';
		arrayOfByte[5] = 2; // 2到6

		// 设置条码文字打印位置
		arrayOfByte[6] = 0x1D;
		arrayOfByte[7] = 'H';
		arrayOfByte[8] = 2; // 0到3

		// 打印39条码
		arrayOfByte[9] = 0x1D;
		arrayOfByte[10] = 'k';
		arrayOfByte[11] = 0x45;
		arrayOfByte[12] = ((byte) paramString.length());
		System.arraycopy(paramString.getBytes(), 0, arrayOfByte, 13,
				paramString.getBytes().length);
		return arrayOfByte;
	}

	public static byte[] CutPaper() // 切纸； GS V 66D 0D
	{
		byte[] arrayOfByte = new byte[] { 0x1D, 0x56, 0x42, 0x00 };
		return arrayOfByte;
	}

	public static byte[] OpenCash() // 开钱箱； DLE DC4 n m t
	{
		byte[] arrayOfByte = new byte[] { 0x1B, 0x70, 0x00, (byte) 0xC0,
				(byte) 0xC0 };
		return arrayOfByte;
	}

	public static byte[] getPrintable(String string) {

		int iNum = 0;

		byte[] printText = new byte[1024];
		String strTmp = "";

		byte[] oldText = setAlignCenter('1');
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);
		iNum += oldText.length;

		oldText = setWH('1');
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);
		iNum += oldText.length;

		oldText = setAlignCenter('1');
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);
		iNum += oldText.length;

		strTmp = string;
		oldText = strTmp.getBytes();
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);
		iNum += oldText.length;

		strTmp = "\n\n\n\n";
		oldText = strTmp.getBytes();
		System.arraycopy(oldText, 0, printText, iNum, oldText.length);

		return printText;
	}

	/////////////////////
	public static final int RED= 1001;
	public static final int BLUE= 1002;
	public static final int GREEN= 1003;
	public static final int BLACK= 1004;
	public static final int WHITE= 1005;
	
	public static byte[] getColorCmd(int type)
	{
	
			byte[] arrayOfByte = new byte[16]; //
			
			arrayOfByte[0] = 0x02;
			arrayOfByte[1] = 0x50;	
			arrayOfByte[2] = 0x02;
			
			arrayOfByte[3] = 0x1F;
			arrayOfByte[4] = 0x46;		
			arrayOfByte[5] = 0x00;//X
			arrayOfByte[6] = 0x00;
			arrayOfByte[7] = 0x00;//y
			arrayOfByte[8] = 0x00;
			arrayOfByte[9] = 0x01;//w
			arrayOfByte[10] = (byte)0xE0;
			arrayOfByte[11] = 0x01;//h
			arrayOfByte[12] = 0x10;
			
			switch(type){
				case RED:
					arrayOfByte[13] = (byte)0xFF;//r
					arrayOfByte[14] = 0x00;//g
					arrayOfByte[15] = 0x00;//b
					break;
				case BLUE:
					arrayOfByte[13] = 0x00;//r
					arrayOfByte[14] = 0x00;//g
					arrayOfByte[15] = (byte)0xFF;//b
					break;
				case GREEN:
					arrayOfByte[13] = 0x00;//r
					arrayOfByte[14] = (byte)0xFF;//g
					arrayOfByte[15] = 0x00;//b
					break;
				case BLACK:
					arrayOfByte[13] = 0x00;//r
					arrayOfByte[14] = 0x00;//g
					arrayOfByte[15] = 0x00;//b
					break;
				default://white
					arrayOfByte[13] = (byte)0xFF;//r
					arrayOfByte[14] = (byte)0xFF;//g
					arrayOfByte[15] = (byte)0xFF;//b
					break;
			}
		return 	arrayOfByte;				
			
	}
	///////////////////////////
	
	//Picture
	/*
	 * 函数名：BmpToArray
	 * 参   数：Bitmap bm
	 * 作   用：实现图像转换为打印数据
	 */
	public static byte[] getPrintPictureCmd (Bitmap bm) {
		//获得图像的宽和高
		int width = bm.getWidth();
		int height= bm.getHeight();

		//获得原图的像素
		int pixR = 0;
		int pixG = 0;
		int pixB = 0;

		//定义像素数组
		int[] pixels = new int[width*height];
		
		int widArray = ((width - 1)/8) + 1;//横向字节数
		int lenArray = widArray * height;//纵向点数
		byte[] dataArray = new byte[lenArray + 8];//定义一个变换后的数据数组
		
		dataArray[0] = 0x1D;
		dataArray[1] = 0x76;
		dataArray[2] = 0x30;
		dataArray[3] = 0x00;
		
		dataArray[4] = (byte)widArray;//xL
		dataArray[5] = (byte)(widArray/256);//xH
		dataArray[6] = (byte)height;
		dataArray[7] = (byte)(height/256);
		
		//获得原图像素
		bm.getPixels(pixels, 0, width, 0, 0, width, height);
		
		int indexByte = 8;
		dataArray[indexByte] = 0;
		int indexBit = 0;
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {//每一行进行转换，转换完成后，可能最后一个字节需要将数据移到高位
				//获取当前像素值的r部分
				pixR = Color.red(pixels[i*width+j]);
				//获取当前像素值的g部分
				pixG = Color.green(pixels[i*width+j]);
				//获取当前像素值的b部分
				pixB = Color.blue(pixels[i*width+j]);
				//一个临时的变量，保存变换后的值
				//int temp = (int)(0.299*pixR + 0.587*pixG + 0.114*pixB + 0.5);
				if((pixR + pixG + pixB) < 384)
				{
					dataArray[indexByte] += 0x01; 
				}
				
				++indexBit;
				
				if(indexBit < 8)
				{
					dataArray[indexByte] *= 2;//相当于左移一位
				}
				else
				{
					indexBit = 0;
					++indexByte;
					if(indexByte < lenArray)
					{
						dataArray[indexByte] = 0;
					}
				}
			}
			
			if(indexBit != 0)//存在不完整字节，1－7有效位
			{
				while(indexBit < 8)
				{
					dataArray[indexByte] *= 2;//相当于左移一位
					++indexBit;
				}
				
				indexBit = 0;
				++indexByte;
				if(indexByte < lenArray)
				{
					dataArray[indexByte] = 0;
				}
			}
		}
		return dataArray;
	}
}
