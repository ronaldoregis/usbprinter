package cordova.plugin.usbprinter;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import java.util.HashMap;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import static android.content.Context.USB_SERVICE;

/**
 * This class echoes a string called from JavaScript.
 */
public class USBPrinter extends CordovaPlugin {
    
    private static final String TAG = "USB_HOST";
    private static final String ACTION_USB_PERMISSION = "com.droid.printer.USB_PERMISSION";

    private static String GPIO_PRINTER_POWER = "/sys/class/gpio/gpio123/value";  //power
    private static String GPIO_PRINTER_RESET = "/sys/class/gpio/gpio20/value";  //reset
    private static String GPIO_PRINTER_BOOT0 = "/sys/class/gpio/gpio116/value"; //boot0

    private static File filePrinterReset  = new File(GPIO_PRINTER_RESET);
    private static File filePrinterBoot0  = new File(GPIO_PRINTER_BOOT0);
    private static File filePrinterPower = new File(GPIO_PRINTER_POWER);
    
    private UsbManager myUsbManager = null;
    private UsbDevice myUsbDevice;
    private UsbInterface myInterface;
    private static UsbDeviceConnection myDeviceConnection;

    private UsbEndpoint epOut = null;
	private UsbEndpoint epIn = null;
    
    private String tag = "Cordova";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("print")) {
            String message = args.getString(0);
            this.print(message, callbackContext);
            return true;
        }
        return false;
    }

    private void print(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {

            gpioOutputControl(filePrinterPower,"1");//power on
            openUSBPrinter();
            byte[] data;
            
//            data = new byte[]{0x0A, 0x0A};
//            data = Command.getPrintDemo();

            data = Command.transToPrintText(message);
            
            if(epOut != null) {
                if (myDeviceConnection.bulkTransfer(epOut, data, data.length, 0) < 0) {
                    Log.d(this.tag, "BulkOut send error！\n");
                }else {
                    Log.d(this.tag, "Data send OK！\n");
                }
            }else {
                    Log.d(this.tag, "Data can not sent!\n");
            }
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
    
    /**
     * enumerate USB printer
     */
    public boolean enumerateDevice() {
        if (myUsbManager == null) {
            return false;
        }

        myUsbDevice = null;
        myInterface = null;

        HashMap<String, UsbDevice> deviceList = myUsbManager.getDeviceList();//Get all USB device
        if (!deviceList.isEmpty()) { // deviceList is not empty
            Log.d(this.tag, "USB device list:\n");
            for (UsbDevice device : deviceList.values()) {
                // show information of the USB device
                Log.d(this.tag, device.toString());
                Log.d(this.tag, "\n");

                for (int i = 0; i < device.getInterfaceCount(); i++) {//check every interface if it is a USB printer
                    UsbInterface intfTemp = device.getInterface(i);

                    if (intfTemp.getInterfaceClass() == 7) {	//if it is a USB printer device
                        Log.d(this.tag, "\nHave USB printer:\n");
                        Log.d(this.tag, device.toString());
                        Log.d(this.tag, "\n");

                        myUsbDevice = device;
                        myInterface = intfTemp;
                        return true;
                    }
                }
            }
        }

        Log.d(this.tag, "Can not find USB printer...\n");
        return false;
    }
    
    /**
     * Open USB port of Printer
     */
    private boolean openDevice() {
        if (myInterface != null) {
            UsbDeviceConnection conn = null;

            if(myUsbDevice == null) return false;
            if (myUsbManager.hasPermission(myUsbDevice)) {
                conn = myUsbManager.openDevice(myUsbDevice);
            }else{
//                PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
//                myUsbManager.requestPermission(myUsbDevice, mPermissionIntent);
            }

            if (conn == null) {
                return false;
            }

            if (conn.claimInterface(myInterface, true)) {
                myDeviceConnection = conn; //
                Log.d(this.tag, "USB Printer opened！\n");
                return true;
            } else {
                conn.close();
            }
        }

        return false;
    }
    
    private boolean assignEndpoint() {

        boolean ret = true;
        if ((myInterface != null) && (myDeviceConnection != null)) {
			int EPCount = myInterface.getEndpointCount();

            //UsbEndpoint epTemp = null;
            if (myInterface.getEndpoint(1) != null) {
                epOut = myInterface.getEndpoint(1);
                if(epOut.getDirection() == UsbConstants.USB_DIR_OUT){
//                    mUSBSendButton.setVisibility(View.VISIBLE);
                    //return true;
                }else{
                    ret = false;
                }
            }else {
                ret = false;
            }

            if (myInterface.getEndpoint(0) != null) {
//                epIn = myInterface.getEndpoint(0);
//                if(epIn != null){
//                    mUSBReadThread = new USBReadThread();
//                    mUSBReadThread.start();
//                }else{
//                    ret = false;
//                }

                // Two way to read data from printer:
                //1
                /*byte[] byte2 = new byte[64];
                int ret = myDeviceConnection.bulkTransfer(epIn, byte2, byte2.length, 3000);
                Log.e("ret", "ret:"+ret);
                for(Byte byte1 : byte2){
                    System.err.println(byte1);
                }*/

                //2
                /*int outMax = outEndpoint.getMaxPacketSize();
                int inMax = inEndpoint.getMaxPacketSize();
                ByteBuffer byteBuffer = ByteBuffer.allocate(inMax);
                UsbRequest usbRequest = new UsbRequest();
                usbRequest.initialize(connection, inEndpoint);
                usbRequest.queue(byteBuffer, inMax);
                if(connection.requestWait() == usbRequest){
                    byte[] retData = byteBuffer.array();
                    for(Byte byte1 : retData){
                        System.err.println(byte1);
                    }
                }*/
            }else{
                ret = false;
            }
        }else{
            ret = false;
        }
        return ret;
    }
    
    private boolean openUSBPrinter(){

        if(myUsbManager == null) {
            myUsbManager = (UsbManager) cordova.getActivity().getSystemService(USB_SERVICE);
        }

        if(myUsbManager != null) {
            Log.d(this.tag, "Start enumerate USB printer：\n");
            if(enumerateDevice()) {//enumerate USB printer
                if(openDevice()) {
                    assignEndpoint();
                }
            }
        }

        return false;
    }
    
    public static int gpioOutputControl(File fileGPIO, String onff) {

        OutputStream writePoint = null;

        byte[] b = onff.getBytes();

        int ret = 0;
        try {
            writePoint = new FileOutputStream(fileGPIO);

            writePoint.write(b);
            writePoint.flush();

            ret = 1;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                writePoint.close();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }
    
}
