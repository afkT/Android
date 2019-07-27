public class BuilderDimensData {

    public static void main(String[] args) {
        StringBuilder stringBuilder = new StringBuilder();

//        String format = "<dimen name=\"%s\">%sdp</dimen>";
//        // 生成正数的
//        for (float i = 0.5f; i <= 50f; i += 0.5f) {
//            // 判断是否整数
//            if ((i / 0.5f) %2 == 0){ // 1, 2, 3
//                stringBuilder.append(String.format(format, "dp_" + String.valueOf((int) i), String.valueOf((int) i)));
//            } else { // 0.5, 1.5, 2.5
//                stringBuilder.append(String.format(format, "dp_" + String.valueOf((int) i) + "_5", String.valueOf(i)));
//            }
//        }


//        String mFormat = "<dimen name=\"%s\">-%sdp</dimen>";
//        // 生成负数的
//        for (float i = 0.5f; i <= 20f; i += 0.5f) {
//            // 判断是否整数
//            if ((i / 0.5f) %2 == 0){ // 1, 2, 3
//                stringBuilder.append(String.format(mFormat, "dp_m_" + String.valueOf((int) i), String.valueOf((int) i)));
//            } else { // 0.5, 1.5, 2.5
//                stringBuilder.append(String.format(mFormat, "dp_m_" + String.valueOf((int) i) + "_5", String.valueOf(i)));
//            }
//        }

        // 生成字体
        String spFormat = "<dimen name=\"%s\">%ssp</dimen>";
        // 生成正数的
        for (float i = 10f; i <= 50f; i += 0.5f) {
            // 判断是否整数
            if ((i / 0.5f) %2 == 0){ // 1, 2, 3
                stringBuilder.append(String.format(spFormat, "sp_" + String.valueOf((int) i), String.valueOf((int) i)));
            } else { // 0.5, 1.5, 2.5
                stringBuilder.append(String.format(spFormat, "sp_" + String.valueOf((int) i) + "_5", String.valueOf(i)));
            }
        }

        System.out.println(stringBuilder.toString());
    }
}
