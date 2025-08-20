import java.util.Scanner;

public class GUGUGAGA {
    public String startcode = "AMY=0512";
    //按ASCII转换为二进制字符串
    public String startcodeBinary = "0100000101001101010110010011110100110000001101010011000100110010";

    public static final String PC1 = "57 49 41 33 25 17 9 1 " +
            "58 50 42 34 26 18 10 2 " +
            "59 51 43 35 27 19 11 3 " +
            "60 52 44 36 63 55 47 39 " +
            "31 23 15 7 62 54 46 38 " +
            "30 22 14 6 61 53 45 37 " +
            "29 21 13 5 28 20 12 4";
    public static final String PC2 = "14 17 11 24 1 5 3 28 " +
            "15 6 21 10 23 19 12 4 " +
            "26 8 16 7 27 20 13 2 " +
            "41 52 31 37 47 55 30 40 " +
            "51 45 33 48 44 49 39 32" +
            " 35 46 43 50 42 38 36 29";
    public static final String E = "32 1 2 3 4 5 " +
            "4 5 6 7 8 9 " +
            "8 9 10 11 12 13 " +
            "12 13 14 15 16 17 " +
            "16 17 18 19 20 21 " +
            "20 21 22 23 24 25 " +
            "24 25 26 27 28 29 " +
            "28 29 30 31 32 1 ";
    public static final String[] S_BOXES = {
            "14 4 13 1 2 15 11 8 3 10 6 12 5 9 0 7 ",
            "0 15 7 4 14 2 13 1 10 6 12 11 9 5 3 8 ",
            "4 1 14 8 13 6 2 11 15 12 9 7 3 10 5 0 ",
            "15 12 8 2 4 9 1 7 5 11 3 14 10 0 6 13 "
    };
    public static final String P = "16 7 20 21 29 12 28 17 " +
            "1 15 23 26 5 18 31 10 " +
            "2 8 24 14 32 27 3 9 " +
            "19 13 30 6 22 11 4 25";

    public String[] generateKey(String code) {
        // 使用PC-1表进行置换
        String[] pc1Array = PC1.split(" ");
        StringBuilder permutedKey = new StringBuilder();
        for (String index : pc1Array) {
            int idx = Integer.parseInt(index) - 1; // 转换为0基索引
            if (idx < 64) {
                permutedKey.append(code.charAt(idx));
            }
        }

        // 分割成C0和D0
        String c0 = permutedKey.substring(0, 28); // 前28位
        String d0 = permutedKey.substring(28); // 后28位
        // 生成16轮密钥
        String[] roundKeys = new String[16];
        for (int i = 0; i < 16; i++){
            c0 = c0.substring(1) + c0.charAt(0); // 循环左移1位
            d0 = d0.substring(1) + d0.charAt(0); // 循环左移1位
            String combinedKey = c0 + d0; // 合并C和D
            StringBuilder roundKey = new StringBuilder();
            // 使用PC-2表进行置换
            String[] pc2Array = PC2.split(" ");
            for (String index : pc2Array) {
                int idx = Integer.parseInt(index) - 1; // 转换为0基索引
                if (idx < 56) {
                    roundKey.append(combinedKey.charAt(idx));
                }
            }
            roundKeys[i] = roundKey.toString(); // 存储第i轮密钥
        }
        return roundKeys; // 返回16轮密钥
    }

    public String changePass(String pass, String[] keys, int roundNumber){
        String l0 = pass.substring(0,32);
        String r0 = pass.substring(32);
        String l1 = r0;
        String rE = "";
        // 使用E表进行扩展
        String[] eArray = E.split(" ");
        for (String index : eArray) {
            int idx = Integer.parseInt(index) - 1; // 转换为0基索引
            if (idx < 32) {
                rE += r0.charAt(idx);
            }
        }
        // 将扩展后的右半部分与第roundNumber轮密钥进行异或
        StringBuilder xorResult = new StringBuilder();
        String roundKey = keys[roundNumber];
        for (int i = 0; i < rE.length(); i++) {
            xorResult.append(rE.charAt(i) == roundKey.charAt(i) ? '0' : '1'); // 异或操作
        }
        // 将异或结果分为8个6位块
        String[] xorBlocks = new String[8];
        for (int i = 0; i < 8; i++) {
            xorBlocks[i] = xorResult.substring(i * 6, (i + 1) * 6);
        }
        // 将异或结果进行S盒替换
        StringBuilder sBoxResult = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            String block = xorBlocks[i];
            int row = Integer.parseInt(block.charAt(0) + "" + block.charAt(5), 2); // 第一位和第六位作为行
            int col = Integer.parseInt(block.substring(1, 5), 2); // 中间四位作为列
            String sBoxValue = S_BOXES[row].split(" ")[col]; // 获取S盒值
            String binaryValue = String.format("%4s", Integer.toBinaryString(Integer.parseInt(sBoxValue))).replace(' ', '0'); // 转换为4位二进制
            sBoxResult.append(binaryValue);
        }
        // 使用P表进行置换
        StringBuilder pResult = new StringBuilder();
        String[] pArray = P.split(" ");
        for (String index : pArray) {
            int idx = Integer.parseInt(index) - 1; // 转换为0基索引
            if (idx < 32) {
                pResult.append(sBoxResult.charAt(idx));
            }
        }
        // 将置换后的结果与左半部分进行异或
        StringBuilder r1 = new StringBuilder();
        for (int i = 0; i < l0.length(); i++) {
            r1.append(l0.charAt(i) == pResult.charAt(i) ? '0' : '1'); // 异或操作
        }
        // 返回新的左半部分和右半部分
        return l1 + r1.toString(); // 返回新的左半部分和右半部分
    }

    public static void main(String[] args) {
        System.out.println("Hello, GUGUGAGA!");
        // 创建Scanner对象以读取用户输入
        System.out.println("请输入需要加密的字符串：");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        // 使用ASCII转换为二进制字符串
        StringBuilder binaryInput = new StringBuilder();
        for (char c : input.toCharArray()) {
            String binaryChar = String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0');
            binaryInput.append(binaryChar);
        }
        System.out.println("输入的字符串的二进制表示为：" + binaryInput.toString());
        GUGUGAGA gugugaga = new GUGUGAGA();
        // DES加密
        String[] keys = gugugaga.generateKey(gugugaga.startcodeBinary);
        String output = "";
        // 如果需要加密的字符串长度大于64位，则截断
        if (binaryInput.length()>= 64) {
            String[] segments = binaryInput.toString().split("(?<=\\G.{64})"); // 按64位分段
            String finalOutput = "";
            for (String segment : segments) {
                // 不足64位的部分补0
                if (segment.length() < 64) {
                    segment = String.format("%-64s", segment).replace(' ', '0');
                }
                //进行DES的十六次变换
                for (int i = 0; i < 16; i++) {
                    segment = gugugaga.changePass(segment, keys, i);
                    System.out.println("第" + (i + 1) + "轮变换后的结果：" + segment);
                }
                finalOutput += segment; // 拼接加密结果
                // 换行
            }
            for(int i = 0; i < finalOutput.length(); i++) {
                if(finalOutput.charAt(i) == '1'){
                    output += "咕";
                }
                else {
                    output += "嘎";
                }
            }
        }
        else {
            // 不足64位的部分补0
            String paddedInput = String.format("%-64s", binaryInput.toString()).replace(' ', '0');
            //进行DES的十六次变换
            for (int i = 0; i < 16; i++) {
                paddedInput = gugugaga.changePass(paddedInput, keys, i);
                System.out.println("第" + (i + 1) + "轮变换后的结果：" + paddedInput);
            }
            for(int i = 0; i < paddedInput.length(); i++) {
                if(paddedInput.charAt(i) == '1'){
                    output += "咕";
                }
                else {
                    output += "嘎";
                }
            }
        }
        // 关闭Scanner对象
        scanner.close();
        System.out.println("加密后的字符串为：" + output);
    }
}
