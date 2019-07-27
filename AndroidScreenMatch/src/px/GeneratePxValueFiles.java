package px;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Details: 生成 适配 px 值
 * https://github.com/mengzhinan/PhoneScreenMatch
 * https://blog.csdn.net/lmj623565791/article/details/45460089
 */
final class GeneratePxValueFiles {

    // ==================
    // ==== 调用方法 ====
    // ==================

    public static void main(String[] args) {
        // 实际只要修改三个地方

        // dirStr => 生成的文件保存地址
        // baseW => UI 设计对应的 屏幕宽度
        // baseH => UI 设计对应的 屏幕高度

        // 生成文件
        new GeneratePxValueFiles().generate();
    }

    // ========================================
    // ========================================
    // ========================================

    // 保存地址
    // private final String dirStr = "d:/res";
    private final String dirStr = new File(System.getProperty("user.dir") + "/res/").getAbsolutePath();

    // 根据某基准尺寸, 生成所有需要适配分辨率的values文件, 做到了编写布局文件时，可以参考屏幕的分辨率
    // 在UI给出的设计图，可以快速的按照其标识的px单位进行编写布局。基本解决了适配的问题。

    // 以多少宽度为基准
    private final int baseW = 750; // 750

    // 以多少的高度为基准
    private final int baseH = 1334; // 1334

    // 生成的文件格式 如: values-480x320
    private final static String VALUE_TEMPLATE = "values-{0}x{1}";

    // 生成的格式 如: <dimen name="x1">1px</dimen>
    private final static String WTemplate = "\t<dimen name=\"x{0}\">{1}px</dimen>\n";

    // 生成的格式 如: <dimen name="y1">1px</dimen>
    private final static String HTemplate = "\t<dimen name=\"y{0}\">{1}px</dimen>\n";

    // 保存需要生成处理的尺寸
    private static final ArrayList<String> listDimesions = new ArrayList<>();

    static {
        // http://screensiz.es/
        // 初始化需要的尺寸  宽,高
        listDimesions.add("320,480");
        listDimesions.add("480,800");
        listDimesions.add("480,854");
        listDimesions.add("540,888");
        listDimesions.add("540,960");
        listDimesions.add("600,1024");
        listDimesions.add("640,1136");
        listDimesions.add("720,1184");
        listDimesions.add("720,1196");
        listDimesions.add("720,1280");
        listDimesions.add("750,1334");
        listDimesions.add("768,1024");
        listDimesions.add("768,1280");
        listDimesions.add("800,1280");
        listDimesions.add("1080,1776");
        listDimesions.add("1080,1812");
        listDimesions.add("1080,1920");
        listDimesions.add("1440,2560");
    }

    /**
     * 构造函数
     */
    public GeneratePxValueFiles() {
        // 如果文件夹不存在, 则创建
        File dir = new File(dirStr);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    /**
     * 进行生成处理
     */
    public void generate() {
        for (String val : listDimesions) {
            String[] wh = val.split(",");
            // 生成xml文件
            generateXmlFile(Integer.parseInt(wh[0]), Integer.parseInt(wh[1]));
        }
    }

    /**
     * 生成xml文件
     * @param width
     * @param height
     */
    private void generateXmlFile(int width, int height) {
        // == 宽度基准值 ==
        StringBuffer widthBuffer = new StringBuffer();
        // == 生成 lay_x.xml ==
        widthBuffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        widthBuffer.append("<resources>\n");
        float cellw = width * 1.0f / baseW;
        for (int i = 1; i <= baseW; i++) {
            widthBuffer.append(WTemplate.replace("{0}", String.valueOf(i)).replace("{1}", String.valueOf(change(cellw * i) + "")));
        }
        widthBuffer.append("</resources>");

        // == 高度基准值 ==
        StringBuffer heightBuffer = new StringBuffer();
        // == 生成 lay_y.xml ==
        heightBuffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        heightBuffer.append("<resources>\n");
        float cellh = height * 1.0f / baseH;
        for (int i = 1; i <= baseH; i++) {
            heightBuffer.append(HTemplate.replace("{0}", String.valueOf(i)).replace("{1}", String.valueOf(change(cellh * i) + "")));
        }
        heightBuffer.append("</resources>");

        // 文件夹名 values-HxW => values-1334x750
        String folder = VALUE_TEMPLATE.replace("{0}", String.valueOf(height)).replace("{1}", String.valueOf(width));
        // 判断文件夹是否创建
        File fileDir = new File(dirStr + File.separator + folder);
        fileDir.mkdir();
        // 生成的两个文件
        File layXFile = new File(fileDir.getAbsolutePath(), "lay_x.xml");
        File layYFile = new File(fileDir.getAbsolutePath(), "lay_y.xml");
        try {
            // lay_x.xml
            PrintWriter pw = new PrintWriter(new FileOutputStream(layXFile));
            pw.print(widthBuffer.toString());
            pw.close();
            // lay_y.xml
            pw = new PrintWriter(new FileOutputStream(layYFile));
            pw.print(heightBuffer.toString());
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 计算适配值
     * @param val
     * @return
     */
    private float change(float val) {
        int temp = (int) (val * 100);
        return temp / 100f;
    }
}