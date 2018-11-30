package redis_proto;



import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;




public class RedisClient {

    private final static int STATUSREPLY = '+';
    private final static int ERRORREPLY = '-';
    private final static int INTEGERREPLY = ':';
    private final static int DOLLER = '$';
    private final static int ESTAR = '*';
    private final static String OK = "OK";
    private final static String PONG = "PONG";

    public static Object getReply(byte[] reply){
        int replyType = reply[0];
        byte[] rest = Arrays.copyOfRange(reply,1,reply.length);
        switch (replyType){
            case STATUSREPLY:
                return doStatusReply(rest);

            case ERRORREPLY:
                return doErrorReply(rest);

            case INTEGERREPLY:
                return doIntegerReply(rest);
            case DOLLER:
                return doArrayReply(rest);

            case ESTAR:
                return doMultiArrayReply(rest);
            default:
                return null;
        }
    }

    private static String doStatusReply(byte[] rest){
        if(rest[0]=='O' && rest[1]=='K'){
            return OK;
        }
        if(rest[0]=='P' && rest[1]=='O' && rest[2]=='N' && rest[3]=='G'){
            return PONG;
        }
        return new String(rest);
    }

    private static String doErrorReply(byte[] rest){
        return new String(rest);
    }

    private static Integer doIntegerReply(byte[] rest){
        String str = new String(rest);
        int index = str.indexOf('\r');
        return Integer.parseInt(str.substring(0,index));
    }
    private static String doArrayReply(byte[] rest){
        if(rest[0]==45){
            return new String("nil");
        }


        String res = new String(rest);
        int index = res.indexOf('\r');
        return res.substring(index+2,rest.length-2);

    }

    private static ArrayList<String> doMultiArrayReply(byte[] rest){
        ArrayList<String> list = new ArrayList<>();
        String str = new String(rest);
        str = str.substring(3,str.length());
        String[] strings = str.split("\r\n");
        for(String s:strings){
            if(s.startsWith("$")){
                continue;
            }
            list.add(s);
        }

        return list;
    }


    public static byte[] getRequest(String[] args){
        StringBuffer data = new StringBuffer();
        data.append("*"+args.length).append("\r\n");
        for(String arg:args){
            data.append("$"+arg.length()).append("\r\n");
            data.append(arg).append("\r\n");
        }
        return data.toString().getBytes(Charset.forName("UTF-8"));
    }

    public static void main(String[] args) throws Exception{
        if(args.length<=0){
            System.out.println("parameter is empty!");
        }

        byte[] req = getRequest(args);

        try{
            Socket socket = new Socket("127.0.0.1",7100);
            OutputStream out = socket.getOutputStream();
            InputStream input = socket.getInputStream();
            out.write(req);

            byte[] res = new byte[1024];
            int n = input.read(res);
//            System.out.println(new String(res));
            byte[] re = Arrays.copyOfRange(res,0,n);
            Object obj = getReply(re);
            System.out.println(obj);


        }catch (Exception e){
            System.out.println("client con err");
            e.printStackTrace();
        }


    }
}

