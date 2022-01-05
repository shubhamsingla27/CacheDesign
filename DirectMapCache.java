import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.ArrayList;


public class DirectMapCache {

    static int tSuccAccess=0;
    static int tNoOfCycles=0;


// Helper function for hex to binary 
String helperHexToBinary(String bin_char){
    int len = bin_char.length();
    if(len == 8){ 
    return bin_char;
    }
    String zero_pad = "0";

    for(int i=1;i<8-len;i++){
        zero_pad = zero_pad + "0";
    } 
    return zero_pad + bin_char;
}

//To fetch data from instruction address
static HashMap<String, String> DirectMCache = new HashMap<String, String>();
static HashMap<String, String> MemHash = new HashMap<String, String>();
String findInstLoc(String memHex,String lenData) throws Exception{
    int a;
    String blockAddress=memHex.substring(0,28);
    String blockAddressComp=blockAddress+"0000";

    String noOfIndex=blockAddress.substring(19,28);
    
    Object val=DirectMCache.get(blockAddress);
    String val1=DirectMCache.get(blockAddress);
    String val2=MemHash.get(blockAddress);
    // To check if given address is already present in cache
    if(val!=null){
        a=0;
    }else a=1;
    switch(a){
        case 0:
            tSuccAccess=tSuccAccess+1;
            tNoOfCycles=tNoOfCycles+1;
            return this.rInst(blockAddressComp,memHex,val1,lenData);
        case 1:
        {
            int k=0;
            String locatedindex="";
            tNoOfCycles=tNoOfCycles+15;

            //Comparing Instruction index bit with the index bit of the given cache line. Also replacing the data on found.
            for (Map.Entry<String, String> set : DirectMCache.entrySet()) {                    
                if(set.getKey().substring(19,28).equals(noOfIndex))
                {
                    locatedindex=set.getKey();
                    break;
                }
                k=k+1;
            }
            int x;
            int y=DirectMCache.size();
            if(k==y){
                x=0;
            }else{
                x=1;
            }
            switch(x){
                case 0:
                DirectMCache.put(blockAddress,val2);
                String val3=DirectMCache.get(blockAddress);
                return this.rInst(blockAddressComp,memHex,val3,lenData);

                case 1:
                DirectMCache.put(locatedindex,val2);
                String locatedindexComp=locatedindex+"0000";
                String val4=DirectMCache.get(locatedindex);
                return this.rInst(locatedindexComp,memHex,val4,lenData);
            }
            return "";
        }
    }
    return "";
}

// To read the bytes starting from the given address, Input(start address to read, first byte address,128 bit data, number of bytes)
static int tNoOfReads=0;
String rInst(String sAdd,String rAdd,String val,String len) throws Exception
{
    String subs=rAdd.substring(28,32);
    tNoOfReads=tNoOfReads+1;
    len=len.substring(0,len.length());

    // Finding number of bytes read
    int nb=Integer.parseInt(len)/2;
    int noOfD=(nb)*2; 
    
    int i=32;

    // Finding the index
    int noOfBytes=Integer.parseInt(subs,2); 
    int x=i-(noOfBytes*2)-noOfD;
    int y;

    int shrt =Integer.parseInt(sAdd.substring(0,28),2);
    shrt=shrt+1;
    String comb=String.format("%28s", Integer.toBinaryString(shrt));
    String comby=comb.replace(" ", "0");
    String combyFull=comby+"0000";
    if(x>=0){
        y=1;
    }
    else{
        y=0;
    }
    switch(y){
        case 1:
            return val.substring(x,i-(noOfBytes*2));
        case 0:
            String l=this.findInstLoc(combyFull,String.valueOf(nb-((i/2)-noOfBytes)));
            String lval=val.substring(0,i-(noOfBytes*2));
            return l+lval;
    }
    return "";
}

// Converting hexadecimal to binary
String hexdecimalToBinary(String hex)
{
    String hex_char,bin_char,binary;
    binary = "";
    int len = hex.length()/2;
    
    for(int i=0;i<len;i++){
        hex_char = hex.substring(2*i,2*i+2);
        int conv_int = Integer.parseInt(hex_char,16);
        bin_char = Integer.toBinaryString(conv_int);
        bin_char = helperHexToBinary(bin_char);
        if(i==0) {
            binary = bin_char;
        } 
        else {
            binary = binary+bin_char;
        }
    }
    return binary;
}

public static void main(String args[]) throws Exception{
    int toNoOfIstAd=0;
    String itn1,itn2,itn3="";
    File myObj = new File("data_trace_output.txt");
    myObj.createNewFile();
    
    //Reading instructions from files
    Scanner memAd= new Scanner(new File("inst_addr_trace_hex_project_1.txt"))
    .useDelimiter(System.getProperty("line.separator"));
    ArrayList<String> listHxadecimal = new ArrayList<String>();
    DirectMapCache memC= new DirectMapCache();
    do{
        itn1 = memAd.next();
        listHxadecimal.add(memC.hexdecimalToBinary(itn1));
    }
    while(memAd.hasNext());

    String[] memInLoc; 
    memInLoc = listHxadecimal.toArray(new String[0]);
    memAd.close();

    Scanner mem= new Scanner(new File("inst_mem_hex_16byte_wide.txt"))
    .useDelimiter(System.getProperty("line.separator"));
    ArrayList<String> listInstreads = new ArrayList<String>();
    do{
        itn3 = mem.next();
        listInstreads.add(itn3);
    }
    while(mem.hasNext());

    String[] memInst;
    memInst=listInstreads.toArray(new String[0]);
    mem.close();

    
    int temp=0;
    //Constructing main memory hash table
    for(int i=0;i<memInst.length;i++){
        String mem_Inst=memInst[i];
        String x=String.format("%28s", Integer.toBinaryString(temp));
        String y=x.replace(" ", "0");
        MemHash.put(y,mem_Inst);
        temp=temp+1;
    }
    temp=0;

    Scanner nBytes= new Scanner(new File("inst_data_size_project_1.txt"))
    .useDelimiter(System.getProperty("line.separator"));
    ArrayList<String> listBytereads = new ArrayList<String>();
    do{
        itn2 = nBytes.next();
        listBytereads.add(itn2.substring(0,itn2.length()-1));
    }
    while(nBytes.hasNext());

    String[] length;
    length=listBytereads.toArray(new String[0]);
    nBytes.close();

    
    // Writting memory data to trace output file
    BufferedWriter buffer;
    buffer= new BufferedWriter(new FileWriter(myObj));
    int i = 0;
    while(i<memInLoc.length){
        String mem_Loc=memInLoc[i];
        toNoOfIstAd=toNoOfIstAd+1;
        buffer.write(memC.findInstLoc(mem_Loc,length[temp])+System.getProperty("line.separator"));
        temp=temp+1;
        i=i+1;
    }
    buffer.close();


    //Formating the Outputs
    float instpers=(float)toNoOfIstAd/tNoOfCycles;
    String instpersFormatted=String.format("%.6f", instpers);
    float hitr=(float)tSuccAccess/tNoOfReads;
    String hitrFormatted= String.format("%.6f", hitr);


    //Console Outputs
    System.out.println("Total cache accesses="+ tNoOfReads);
    System.out.println("Total hits="+ tSuccAccess);
    System.out.println("Hit ratio="+ hitrFormatted);
    System.out.println("Total Instruction addresses="+ toNoOfIstAd);
    System.out.println("Total clock cycles="+ tNoOfCycles);
    System.out.println("Instruction per cycle (IPC)="+ instpersFormatted);
}
}